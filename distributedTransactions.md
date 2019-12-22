
<!-- MarkdownTOC -->

- [Managing transactions](#managing-transactions)
	- [Motivation](#motivation)
	- [ACID consistency model](#acid-consistency-model)
		- [Definition](#definition)
		- [XA standards - Distributed transactions](#xa-standards---distributed-transactions)
			- [Two phase commit](#two-phase-commit)
				- [Assumptions](#assumptions)
				- [Algorithm](#algorithm)
				- [Proof of correctness](#proof-of-correctness)
				- [Pros](#pros)
				- [Cons](#cons)
				- [References](#references)
			- [Three phase commit](#three-phase-commit)
				- [Motivation](#motivation-1)
	- [BASE consistency model](#base-consistency-model)
		- [Definition](#definition-1)
		- [Synchronous implementations](#synchronous-implementations)
			- [Use cases](#use-cases)
			- [TCC](#tcc)
			- [Distributed Sagas](#distributed-sagas)
				- [Motivation](#motivation-2)
				- [Definition](#definition-2)
				- [Assumptions](#assumptions-1)
				- [Approaches](#approaches)
				- [Pros](#pros-1)
				- [Cons](#cons-1)
				- [References](#references-1)
		- [Asynchronous implementations](#asynchronous-implementations)
			- [Use cases](#use-cases-1)
			- [RocketMQ as an example](#rocketmq-as-an-example)
				- [Concept](#concept)
				- [Algorithm](#algorithm-1)
				- [References](#references-2)

<!-- /MarkdownTOC -->

# Managing transactions
## Motivation
* Database is partitioned across multiple machines for scalability. A transaction might touch more than one partition. How do we guarantee that all of the partitions commit the transaction or none commit the transactions?
* Example: 
	* Transfer money from A to B
		1. Debit at A, credit at B, tell the client Okay
		2. Require both banks to do it, or neither
		3. Require that one bank never act alone
	* A travel booking edge service invokes several low level services (car rental service, hotel reservation service, airline reservation service)

## ACID consistency model
### Definition
* Atomic: Everything in a transaction succeeds or the entire transaction is rolled back.
* Consistent: A transaction cannot leave the database in an inconsistent state.
* Isolated: Transactions cannot interfere with each other.
* Durable: Completed transactions persist, even when servers restart etc.

### XA standards - Distributed transactions
#### Two phase commit
##### Assumptions
* The protocol works in the following manner: 
	1. One node is designated the coordinator, which is the master site, and the rest of the nodes in the network are called cohorts.  
	2. Stable storage at each site and use of a write ahead log by each node. 
	3. The protocol assumes that no node crashes forever, and eventually any two nodes can communicate with each other. The latter is not a big deal since network communication can typically be rerouted. The former is a much stronger assumption; suppose the machine blows up!

##### Algorithm
1. PREPARE phase
	1. COORDINATOR: The COORDINATOR sends the message to each COHORT. The COORDINATOR is now in the preparing transaction state. Now the COORDINATOR waits for responses from each of the COHORTS. 
	2. COHORTS:  	
		* If a "PREPARE" message is received for some transaction t which is unknown at the COHORT ( never ran, wiped out by crash, etc ), reply "ABORT". 
		* Otherwise write the new state of the transaction to the UNDO and REDO log in permanent memory. This allows for the old state to be recovered ( in event of later abort ) or committed on demand regardless of crashes. The read locks of a transaction may be released at this time; however, the write locks are still maintained. Now send "AGREED" to the COORDINATOR.
	3. (Optional) COORDINATOR: If after some time period some COHORT has not responded COORDINATOR will retransmit the "PREPARE" message and go to step 1.1.

2. COMMIT phase
	1. COORDINATOR: 
		* If any COHORT responds ABORT then the transaction must be aborted, 
			- Send the ABORT message to each COHORT.
		* If all COHORTS respond AGREED then the transaction may be commited
			- Record in the logs a COMPLETE to indication the transaction is now completing. 
			- Send COMMIT message to each of the COHORTS and then erase all associated information from permanent memory ( COHORT list, etc. ).
	2. COHORTS: 
		* If the COHORT receives a "COMMIT" message from COORDINATOR
			- Each cohort undoes the transaction using the undo log, and releases the resources and locks held during the transaction.
			- Each cohort sends an acknowledgement to the coordinator.
		* If the COHORT receives an "ABORT" message from COORDINATOR
			- Each cohort completes the operation, and releases all the locks and resources held during the transaction.
			- Each cohort sends an acknowledgment to the coordinator.
	3. (Optional) COORDINATOR: If after some time period all COHORTS do not respond the COORDINATOR can either transmit "ABORT" messages or "COMMIT" to all COHORTS to the COHORTS that have not responded. In either case the COORDINATOR will eventually go to state 2.1. 
	4. COORDINATOR: The coordinator completes the transaction when acknowledgements have been received.

##### Proof of correctness
* We assert the claim that if one COHORT completes the transaction all COHORTS complete the transaction eventually. The proof for correctness proceeds somewhat informally as follows: If a COHORT is completing a transaction, it is so only because the COORDINATOR sent it a COMMT message. This message is only sent when the COORDINATOR is in the commit phase, in which case all COHORTS have responded to the COORDINATOR AGREED. This means all COHORTS have prepared the transaction, which implies any crash at this point will not harm the transaction data because it is in permanent memory. Once the COORDINATOR is completing, it is insured every COHORT completes before the COORDINATOR's data is erased. Thus crashes of the COORDINATOR do not interfere with the completion.
* Therefore if any COHORT completes, then they all do. The abort sequence can be argued in a similar manner. Hence the atomicity of the transaction is guaranteed to fail or complete globally.

##### Pros
1. 2pc is a very strong consistency protocol. First, the prepare and commit phases guarantee that the transaction is atomic. The transaction will end with either all microservices returning successfully or all microservices have nothing changed.
2. 2pc allows read-write isolation. This means the changes on a field are not visible until the coordinator commits the changes.

##### Cons
1. Performance bottleneck. 
	- Synchrounous blocking pattern could be a performance bottleneck. The protocol will need to lock the object that will be changed before the transaction completes. In the example above, if a customer places an order, the “fund” field will be locked for the customer. This prevents the customer from applying new orders. This makes sense because if a “prepared” object changed after it claims it is “prepared,” then the commit phase could possibly not work. This is not good. In a database system, transactions tend to be fast—normally within 50 ms. However, microservices have long delays with RPC calls, especially when integrating with external services such as a payment service. The lock could become a system performance bottleneck. Also, it is possible to have two transactions mutually lock each other (deadlock) when each transaction requests a lock on a resource the other requires.
	- The whole system is bound by the slowest resource since any ready node will have to wait for confirmation from a slower node which is yet to confirm its status.
2. Single point of failure. Coordinator failures could become a single point of failure, leading to infinite resource blocking. More specifically, if a cohort has sent an agreement message to the coordinator, it will block until a commit or rollback is received. If the coordinator is permanently down, the cohort will block indefinitely, unless it can obtain the global commit/abort decision from some other cohort. When the coordinator has sent "Query-to-commit" to the cohorts, it will block until all cohorts have sent their local decision.
3. Data inconsistency. There is no mechanism to rollback the other transaction if one micro service goes unavailable in commit phase. If in the "Commit phase" after COORDINATOR send "COMMIT" to COHORTS, some COHORTS don't receive the command because of timeout then there will be inconsistency between different nodes. 

##### References
1. [Reasoning behind two phase commit](./files/princeton-2phasecommit.pdf)
2. [Discuss failure cases of two phase commits](https://www.the-paper-trail.org/post/2008-11-27-consensus-protocols-two-phase-commit/)

#### Three phase commit
##### Motivation


## BASE consistency model
### Definition
* Basic availability: The database appears to work most of the time
* Soft-state: Stores don't have to be write-consistent, nor do different replicas have to be mutally consistent all the time
* Eventual consistency: The datastore exhibit consistency at some later point (e.g. lazily at read time)

### Synchronous implementations

#### Use cases
#### TCC
#### Distributed Sagas
##### Motivation
* Using distributed transaction to maintain data consistency suffers from the following two pitfalls
	- Many modern technologies including NoSQL databases such as MongoDB and Cassandra don't support them. Distributed transactions aren't supported by modern message brokers such as RabbitMQ and Apache Kafka. 
	- It is a form of syncronous IPC, which reduces availability. In order for a distributed transaction to commit, all participating services must be available. If a distributed transaction involves two services that are 99.5% available, then the overall availability is 99\%. Each additional service involved in a distributed transaction further reduces availability. 
* Sagas are mechanisms to maintain data consistency in a microservice architecture without having to use distributed transactions. 
* Distributed sagas execute transactions that span multiple physical databases by breaking them into smaller transactions and compensating transactions that operate on single databases.

##### Definition
* A saga is a state machine. 
* A distributed saga is a collection of requests. Each request has a compensating request on failure. A dsitributed saga guarantees the following properties:
	1. Either all Requests in the Saga are succesfully completed, or
	2. A subset of Requests and their Compensating Requests are executed.

##### Assumptions
* For distributed sagas to work, both requests and compensating requests need to obey certain characteristics:
	1. Requests and Compensating Requests must be idempotent, because the same message may be delivered more than once. However many times the same idempotent request is sent, the resulting outcome must be the same. An example of an idempotent operation is an UPDATE operation. An example of an operation that is NOT idempotent is a CREATE operation that generates a new id every time.
	2. Compensating Requests must be commutative, because messages can arrive in order. In the context of a distributed saga, it’s possible that a Compensating Request arrives before its corresponding Request. If a BookHotel completes after CancelHotel, we should still arrive at a cancelled hotel booking (not re-create the booking!)
	3. Requests can abort, which triggers a Compensating Request. Compensating Requests CANNOT abort, they have to execute to completion no matter what.

##### Approaches
* Event-driven choreography: When there is no central coordination, each service produces and listen to other service’s events and decides if an action should be taken or not.
* Command/Orchestration: When a coordinator service is responsible for centralizing the saga’s decision making and sequencing business logic.

##### Pros
* Support for long-lived transactions. Because each microservice focuses only on its own local atomic transaction, other microservices are not blocked if a microservice is running for a long time. This also allows transactions to continue waiting for user input. Also, because all local transactions are happening in parallel, there is no lock on any object.

##### Cons
* Difficult to debug, especially when many microservices are involved. 
* The event messages could become difficult to maintain if the system gets complex. 
* It does not have read isolation. For example, the customer could see the order being created, but in the next second, the order is removed due to a compensation transaction.

##### References
* https://dzone.com/articles/distributed-sagas-for-microservices
* https://chrisrichardson.net/post/antipatterns/2019/07/09/developing-sagas-part-1.html

### Asynchronous implementations
#### Use cases
* Example: A user is purchasing items on an ecommerce website. There are two operations
	1. Create an order in the database
	2. Delete ordered items from the shopping cart. Since this step is not a necessary step to be completed within the order operation, the command could be processed asynchronously, e.g. putting into a message queue. 

#### RocketMQ as an example
##### Concept
* Half (prepare) message: Refers to a message that cannot be delivered temporarily. When a message is successfully sent to the MQ server, but the server did not receive the second acknowledgement of the message from the producer, then the message is marked as “temporarily undeliverable”. The message in this status is called a half message.
* Message status check: Network disconnection or producer application restart may result in the loss of the second acknowledgement of a transactional message. When MQ server finds that a message remains a half message for a long time, it will send a request to the message producer, checking the final status of the message (Commit or Rollback).

##### Algorithm

1. Producer send half message to MQ server.
2. After send half message succeed, execute local transaction.
3. Send commit or rollback message to MQ Server based on local transaction results.
4. If commit/rollback message missed or producer pended during the execution of local transaction，MQ server will send check message to each producers in the same group to obtain transaction status.
5. Producer reply commit/rollback message based on local transaction status.
6. Committed message will be delivered to consumer but rolled back message will be discarded by MQ server.

* ![Execute flow chart](./images/mq_transactions_flowchart.png)


##### References
* https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/








