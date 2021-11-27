# Algorithm\_DistributedTransactions

* [Managing transactions](algorithm\_distributedtransactions.md#managing-transactions)
  * [Motivation](algorithm\_distributedtransactions.md#motivation)
  * [Distributed algorithm comparison](algorithm\_distributedtransactions.md#distributed-algorithm-comparison)
    * [BFT (Byzantine fault tolerance)](algorithm\_distributedtransactions.md#bft-byzantine-fault-tolerance)
  * [ACID consistency model](algorithm\_distributedtransactions.md#acid-consistency-model)
    * [Strong consistency with XA model](algorithm\_distributedtransactions.md#strong-consistency-with-xa-model)
      * [MySQL XA example](algorithm\_distributedtransactions.md#mysql-xa-example)
    * [2PC - Two phase commit](algorithm\_distributedtransactions.md#2pc---two-phase-commit)
      * [Assumptions](algorithm\_distributedtransactions.md#assumptions)
      * [Process](algorithm\_distributedtransactions.md#process)
        * [Success case](algorithm\_distributedtransactions.md#success-case)
        * [Failure case](algorithm\_distributedtransactions.md#failure-case)
      * [Pros](algorithm\_distributedtransactions.md#pros)
      * [Cons](algorithm\_distributedtransactions.md#cons)
      * [References](algorithm\_distributedtransactions.md#references)
    * [2PC improvement](algorithm\_distributedtransactions.md#2pc-improvement)
    * [3PC - Three phase commit](algorithm\_distributedtransactions.md#3pc---three-phase-commit)
      * [Motivation](algorithm\_distributedtransactions.md#motivation-1)
      * [Compare with 2PC](algorithm\_distributedtransactions.md#compare-with-2pc)
        * [Composition](algorithm\_distributedtransactions.md#composition)
        * [Safety and livesness](algorithm\_distributedtransactions.md#safety-and-livesness)
      * [Failure handling](algorithm\_distributedtransactions.md#failure-handling)
      * [Limitation - 3PC can still fail](algorithm\_distributedtransactions.md#limitation---3pc-can-still-fail)
      * [References](algorithm\_distributedtransactions.md#references-1)
    * [Seata](algorithm\_distributedtransactions.md#seata)
  * [BASE consistency model](algorithm\_distributedtransactions.md#base-consistency-model)
    * [Definition](algorithm\_distributedtransactions.md#definition)
    * [Eventual consistency with TCC model](algorithm\_distributedtransactions.md#eventual-consistency-with-tcc-model)
    * [Local message based distributed transactions](algorithm\_distributedtransactions.md#local-message-based-distributed-transactions)
    * [RocketMQ transactional message based distributed transactions](algorithm\_distributedtransactions.md#rocketmq-transactional-message-based-distributed-transactions)
      * [Concept](algorithm\_distributedtransactions.md#concept)
      * [Process](algorithm\_distributedtransactions.md#process-1)
    * [Distributed Sagas](algorithm\_distributedtransactions.md#distributed-sagas)
      * [Motivation](algorithm\_distributedtransactions.md#motivation-2)
      * [Definition](algorithm\_distributedtransactions.md#definition-1)
      * [Assumptions](algorithm\_distributedtransactions.md#assumptions-1)
      * [Approaches](algorithm\_distributedtransactions.md#approaches)
      * [Examples](algorithm\_distributedtransactions.md#examples)
      * [Pros](algorithm\_distributedtransactions.md#pros-1)
      * [Cons](algorithm\_distributedtransactions.md#cons-1)
      * [References](algorithm\_distributedtransactions.md#references-2)
  * [Real world](algorithm\_distributedtransactions.md#real-world)
    * [Uber Cadence](algorithm\_distributedtransactions.md#uber-cadence)

## Managing transactions

### Motivation

* Database is partitioned across multiple machines for scalability. A transaction might touch more than one partition. How do we guarantee that all of the partitions commit the transaction or none commit the transactions?
* Example:
  * Transfer money from A to B
    1. Debit at A, credit at B, tell the client Okay
    2. Require both banks to do it, or neither
    3. Require that one bank never act alone
  * A travel booking edge service invokes several low level services (car rental service, hotel reservation service, airline reservation service)

### Distributed algorithm comparison

|             |                         |                      |               |                |
| ----------- | ----------------------- | -------------------- | ------------- | -------------- |
| `Algorithm` | `Crash fault tolerance` | `Consistency`        | `Performance` | `Availability` |
| 2PC         | No                      | Strong consistency   | Low           | Low            |
| TCC         | No                      | Eventual consistency | Low           | Low            |
| Paxos       | No                      | Strong consistency   | Middle        | Middle         |
| ZAB         | No                      | Eventual consistency | Middle        | Middle         |
| Raft        | No                      | Strong consistency   | Middle        | Middle         |
| Gossip      | No                      | Eventual consistency | High          | High           |
| Quorum NWD  | No                      | Strong consistency   | Middle        | Middle         |
| PBFT        | Yes                     | N/A                  | Low           | Middle         |
| POW         | Yes                     | N/A                  | Low           | Middle         |

#### BFT (Byzantine fault tolerance)

* Within a distributed system, there are no malicious behaviors but could be fault behaviors such as process crashing, hardware bugs, etc.

### ACID consistency model

#### Strong consistency with XA model

* 2PC is an implementation of XA standard. XA standard defines how two components of DTP model (Distributed Transaction Processing) - Resource manager and transaction manager interact with each other.

![](images/microsvcs\_distributedtransactions\_xastandards.png)

![](.gitbook/assets/microsvcs\_distributedtransactions\_xaprocess.png)

**MySQL XA example**

* As long as databases support XA standards, it will support distributed transactions.
* XA start

![](.gitbook/assets/microsvcs\_distributedtransactions\_xa\_start.png)

* XA prepare

![](.gitbook/assets/microsvcs\_distributedtransactions\_xa\_prepare.png)

* XA commit

![](images/microsvcs\_distributedtransactions\_xa\_commit.png)

#### 2PC - Two phase commit

**Assumptions**

* The protocol works in the following manner:
  1. One node is designated the coordinator, which is the master site, and the rest of the nodes in the network are called cohorts.
  2. Stable storage at each site and use of a write ahead log by each node.
  3. The protocol assumes that no node crashes forever, and eventually any two nodes can communicate with each other. The latter is not a big deal since network communication can typically be rerouted. The former is a much stronger assumption; suppose the machine blows up!

**Process**

**Success case**

![](.gitbook/assets/microsvcs\_distributedtransactions\_2pc\_success.png)

**Failure case**

![](images/microsvcs\_distributedtransactions\_2pc\_failure.png)

**Pros**

1. 2pc is a strong consistency protocol. First, the prepare and commit phases guarantee that the transaction is atomic. The transaction will end with either all microservices returning successfully or all microservices have nothing changed.
2. 2pc allows read-write isolation. This means the changes on a field are not visible until the coordinator commits the changes.

**Cons**

1. Not suitable for high volume scenarios due to resource locking. The protocol will need to lock the object that will be changed before the transaction completes.
   * For example, if a customer places an order, the “fund” field will be locked for the customer. This prevents the customer from applying new orders. This makes sense because if a “prepared” object changed after it claims it is “prepared,” then the commit phase could possibly not work.
   * The 2PC is initially designed for databases. It is suitable for database scenarios because transactions tend to be fast—normally within 50 ms. However, microservices have long delays with RPC calls, especially when integrating with external services such as a payment service. The lock could become a system performance bottleneck. Also, it is possible to have two transactions mutually lock each other (deadlock) when each transaction requests a lock on a resource the other requires.
2. Single point of failure. Coordinator failures could become a single point of failure, leading to infinite resource blocking.
   * For example, if a cohort has sent an agreement message to the coordinator, it will block until a commit or rollback is received. If the coordinator is permanently down, the cohort will block indefinitely, unless it can obtain the global commit/abort decision from some other cohort. When the coordinator has sent "Query-to-commit" to the cohorts, it will block until all cohorts have sent their local decision.
3. Data inconsistency. There is no mechanism to rollback the other transaction if one micro service goes unavailable in commit phase. If in the "Commit phase" after COORDINATOR send "COMMIT" to COHORTS, some COHORTS don't receive the command because of timeout then there will be inconsistency between different nodes.
   * Once coordinator sends message to Commit, each participant does not commit without considering other participants.
   * When coordinator and all participants finished committing goes down, then the rest doesn't know the state of the system because
     * All that knew are dead.
     * Cannot just abort, since the commit action might have completed at some and cannot be rolled back.
     * Also cannot commit, since the original decision might have been to abort.

**References**

1. [Reasoning behind two phase commit](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/files/princeton-2phasecommit.pdf)
2. [Discuss failure cases of two phase commits](https://www.the-paper-trail.org/post/2008-11-27-consensus-protocols-two-phase-commit/)
3. [Lecture](https://slideplayer.com/slide/4626345/)

#### 2PC improvement

* NewSQL Percolator
* PGXC GoldenDB
* [Todo: too complex and succinct -\_-](https://time.geekbang.org/column/article/278949)

#### 3PC - Three phase commit

**Motivation**

* The fundamental difficulty with 2PC is that, once the decision to commit has been made by the co-ordinator and communicated to some replicas, the replicas go right ahead and act upon the commit statement without checking to see if every other replica got the message. Then, if a replica that committed crashes along with the co-ordinator, the system has no way of telling what the result of the transaction was (since only the co-ordinator and the replica that got the message know for sure). Since the transaction might already have been committed at the crashed replica, the protocol cannot pessimistically abort - as the transaction might have had side-effects that are impossible to undo. Similarly, the protocol cannot optimistically force the transaction to commit, as the original vote might have been to abort.
* We break the second phase of 2PC - ‘commit’ - into two sub-phases. The first is the ‘prepare to commit’ phase. The co-ordinator sends this message to all replicas when it has received unanimous ‘yes’ votes in the first phase. On receipt of this messages, replicas get into a state where they are able to commit the transaction - by taking necessary locks and so forth - but crucially do not do any work that they cannot later undo. They then reply to the co-ordinator telling it that the ‘prepare to commit’ message was received.

**Compare with 2PC**

* Similarities:
  * Coordinator will need to send CanCommit requests to participants, asking them whether transactions could be committed;

**Composition**

* Phase 1 as in 2PC; Phase 2 is now split into two:
  * PreCommit: First send Ready-to-Commit
  * DoCommit: When it receives all Yes votes. Then send commit message
* The reason for the extra step is to let all the participants know what the decision is, in case of failure everyone then knows and the state can be recovered.

**Safety and livesness**

* FLP states you cannot have both safety and liveness.
* Livenss:
  * 2PC can block
  * 3PC will always make progress
* Safety:
  * 2PC is safe.
  * 3PC is safeish, as seen in the network partitioning case one can get to the wrong result.

**Failure handling**

* If coordinator times out before receiving Prepared from all participants, it decides to abort.
* Coordinator ignores participants that don't ack its Ready-to-Commit
* Participants that voted Prepared and timed out waiting for Ready-to-Commit or Commit use the termination protocol.

**Limitation - 3PC can still fail**

* Network partition failure
  * All the ones that gets Ready-to-Commit is on one side.
  * All the rest on the other side.
* Recovery will take place on both sides
  * One side will commit
  * Other side will abort
* When network merges back, you have an inconsistent state

**References**

* [https://www.the-paper-trail.org/post/2008-11-29-consensus-protocols-three-phase-commit/](https://www.the-paper-trail.org/post/2008-11-29-consensus-protocols-three-phase-commit/)
* [http://courses.cs.vt.edu/\~cs5204/fall00/distributedDBMS/sreenu/3pc.html](http://courses.cs.vt.edu/\~cs5204/fall00/distributedDBMS/sreenu/3pc.html)

#### Seata

* Seata is an implementation of variants 2PC.
* [https://github.com/seata/seata](https://github.com/seata/seata)

### BASE consistency model

#### Definition

* Basic availability: The database appears to work most of the time
* Soft-state: Stores don't have to be write-consistent, nor do different replicas have to be mutally consistent all the time
* Eventual consistency: The datastore exhibit consistency at some later point (e.g. lazily at read time)

#### Eventual consistency with TCC model

* TCC is a design pattern and not associated with any particular technologies.
* Pros: TCC implements eventual consistency model and has better performance than XA.
* Cons: TCC is a transaction protocol on the business layer and XA standard is just for database. TCC model has lots of invasion on the business layer.

#### Local message based distributed transactions

* The transaction initiator maintains a local message table.
* Business and local message table operations are executed in the same local transaction.
  * If the service is successfully executed, a message with the "to be sent" state is also recorded in the local message table.
  * The system starts a scheduled task to regularly scan the local message table for messages that are in the "to be sent" state annd sends them to MQ. If the sending fails or times out, the message will be resent until it is sent successfully.
* Then, the task will delete the state record from the local message table. The subsequent consumption and subscription process is similar to that of the transactional message mode.

![](images/microsvcs\_DistributedTransactions\_localMessageTable.png)

#### RocketMQ transactional message based distributed transactions

* Transactional message is similar to local message table.

**Concept**

* Half(Prepare) Message: Refers to a message that cannot be delivered temporarily. When a message is successfully sent to the MQ server, but the server did not receive the second acknowledgement of the message from the producer, then the message is marked as “temporarily undeliverable”. The message in this status is called a half message.
* Message Status Check: Network disconnection or producer application restart may result in the loss of the second acknowledgement of a transactional message. When MQ server finds that a message remains a half message for a long time, it will send a request to the message producer, checking the final status of the message (Commit or Rollback).

**Process**

1. Producer send half message to MQ server.
2. After send half message succeed, execute local transaction.
3. Send commit or rollback message to MQ Server based on local transaction results.
4. If commit/rollback message missed or producer pended during the execution of local transaction，MQ server will send check message to each producers in the same group to obtain transaction status.
5. Producer reply commit/rollback message based on local transaction status.
6. Committed message will be delivered to consumer but rolled back message will be discarded by MQ server.

![](.gitbook/assets/microsvcs\_DistributedTransaction\_rocketMQ.png)

* Refernces: [https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/](https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/)

```
                                                                                                      ─                    




                                                                                     (Optional)     ┌─────────────────┐    
                                                                                     Step10. Put    │                 │    
                                                                                      into dead     │Dead letter queue│    
                                                                                      queue if  ─ ─▶│                 │    
                                                                                      retry too     │                 │    
                                                                                     many times     └─────────────────┘    
              ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                                                                    
                                                       │                              │                                    
            (Optional) Step4. If  ┌────────────────────▼────────────────────┐                                              
            confirm not received  │  ┌───────────────────────────────────┐  │         │                                    
            within certain time,  │  │                                   │  │                                              
                    retry   ┌─────┼─▶│             Exchange              │  │         │                                    
              │             │     │  │                                   │  │             (Optional)                       
        ┌──────────┐        │     │  └───────────────────────────────────┘  │         │  Step9. Queue                      
        │Ecommerce │    Step2.    │                    │                    │            retry if ack                      
     ┌──│  Order   │───RabbitMQ   │                    │                    │         ├ ─not received ─ ─                  
     │  │ Service  │   Confirm    │                    │                    │           within certain   │                 
     │  └──────────┘  mechanism   │                    ▼                    │         │      time        ▼                 
     │                            │┌───────────────────────────────────────┐│                      ┌───────────┐           
     │                            ││                 Queue                 ││         │            │           │           
     │                            ││                                       ││                      │           │           
     │                            ││     Step5. Store message in queue     ││         │            │ Ecommerce │           
     │                            ││                                       ││                      │ Shipment  │           
     │                            ││┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ││         │   Step6.   │  Service  │           
     │                            ││   OrderId | UserId | Order Content  │ ││              Send    │           │           
  step1.                          │││  --------------------------------   ─┼┼─────────┴──message──▶│(Idempotenc│────────┐  
  Create                          ││   10001   |  tom   |   toothpaste   │ ││                      │ y for the │        │  
order and                         ││└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ││                      │   same    │        │  
 message                          ││                                       ││                      │ message)  │        │  
   row.                           │└───────────────────────────────────────┘│                      │           │        │  
     │                            │                    ▲                    │                      │           │        │  
     │                            │                Rabb│t MQ                │                      │           │        │  
     │                            └────────────────────┼────────────────────┘                      └───────────┘  step7.   
     │                                                 │                                                 │     Create order
     │                                                 │            Step8. Send Ack for RabbitMQ         │              │  
     │                                                 └──────────────to delete msg from queue───────────┘              │  
     │  ┌─────────────────────────────────────────────────────────────┐  ┌───────────────────────────────────────────┐  │  
     │  │                  Ecommerce Order Database                   │  │        Ecommerce Shipment Database        │  │  
     │  │                                                             │  │                                           │  │  
     │  │                       ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │  │┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │  
     │  │                          OrderId | UserId | Order Content  ││  │   OrderId | ShipperId | Shipment status │ │  │  
     │  │                       │  --------------------------------   │  ││  --------------------------------------  │  │  
     │  │                          10001   |  tom   |   toothpaste   ││  │   10001   |  david    | out for delivery│ │  │  
     └─▶│                       └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │  │└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │◀─┘  
        │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │  │                                           │     
        │    MessageId | Message Content | status |  creation time    │  │                                           │     
        │ │-------------------------------------------------------- │ │  │                                           │     
        │    9890      | {orderId:10001} | sent   |  2018111420       │  │                                           │     
        │ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─ ─ ─ ┘ │  │                                           │     
        └─────────────────────────────────────────┼───────────────────┘  └───────────────────────────────────────────┘     
                                                  │                                                                        
                                     Step3. change to Acked upon                                                           
                                     receiving RabbitMQ Confirm                                                            
                                                  │                                                                        
          ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┼ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                                              
             MessageId | Message Content | Message│status |  creation time   │                                             
          │---------------------------------------▼------------------------                                                
             9890      | {orderId:10001} |       Acked    |  2018111420      │                                             
          └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
```

#### Distributed Sagas

**Motivation**

* Using distributed transaction to maintain data consistency suffers from the following two pitfalls
  * Many modern technologies including NoSQL databases such as MongoDB and Cassandra don't support them. Distributed transactions aren't supported by modern message brokers such as RabbitMQ and Apache Kafka.
  * It is a form of syncronous IPC, which reduces availability. In order for a distributed transaction to commit, all participating services must be available. If a distributed transaction involves two services that are 99.5% available, then the overall availability is 99\\%. Each additional service involved in a distributed transaction further reduces availability.
* Sagas are mechanisms to maintain data consistency in a microservice architecture without having to use distributed transactions.
* Distributed sagas execute transactions that span multiple physical databases by breaking them into smaller transactions and compensating transactions that operate on single databases.

**Definition**

* High entry bar: First need to build a state machine. A saga is a state machine.
* A distributed saga is a collection of requests. Each request has a compensating request on failure. A dsitributed saga guarantees the following properties:
  1. Either all Requests in the Saga are succesfully completed, or
  2. A subset of Requests and their Compensating Requests are executed.
* Limitation: Does not guarantee the separation
  * Solution 1: Semantic lock

**Assumptions**

* For distributed sagas to work, both requests and compensating requests need to obey certain characteristics:
  1. Requests and Compensating Requests must be idempotent, because the same message may be delivered more than once. However many times the same idempotent request is sent, the resulting outcome must be the same. An example of an idempotent operation is an UPDATE operation. An example of an operation that is NOT idempotent is a CREATE operation that generates a new id every time.
  2. Compensating Requests must be commutative, because messages can arrive in order. In the context of a distributed saga, it’s possible that a Compensating Request arrives before its corresponding Request. If a BookHotel completes after CancelHotel, we should still arrive at a cancelled hotel booking (not re-create the booking!)
  3. Requests can abort, which triggers a Compensating Request. Compensating Requests CANNOT abort, they have to execute to completion no matter what.

**Approaches**

* Event-driven choreography: When there is no central coordination, each service produces and listen to other service’s events and decides if an action should be taken or not.
* Command/Orchestration: When a coordinator service is responsible for centralizing the saga’s decision making and sequencing business logic.

**Examples**

tation: Uber Cadence

**Pros**

* Support for long-lived transactions. Because each microservice focuses only on its own local atomic transaction, other microservices are not blocked if a microservice is running for a long time. This also allows transactions to continue waiting for user input. Also, because all local transactions are happening in parallel, there is no lock on any object.

**Cons**

* Difficult to debug, especially when many microservices are involved.
* The event messages could become difficult to maintain if the system gets complex.
* It does not have read isolation. For example, the customer could see the order being created, but in the next second, the order is removed due to a compensation transaction.

**References**

* [https://dzone.com/articles/distributed-sagas-for-microservices](https://dzone.com/articles/distributed-sagas-for-microservices)
* [https://chrisrichardson.net/post/antipatterns/2019/07/09/developing-sagas-part-1.html](https://chrisrichardson.net/post/antipatterns/2019/07/09/developing-sagas-part-1.html)
* [https://www.alibabacloud.com/blog/an-in-depth-analysis-of-distributed-transaction-solutions\_597232](https://www.alibabacloud.com/blog/an-in-depth-analysis-of-distributed-transaction-solutions\_597232)

### Real world

#### Uber Cadence

* [TODO in Chinese](https://time.geekbang.org/course/detail/100053601-264150)
  * [Cadence: The only workflow platform you'll ever need](https://www.youtube.com/watch?v=llmsBGKOuWI\&t=792s\&ab\_channel=UberEngineering)
  * [Cadence meetup: Introduction to Cadence](https://www.youtube.com/watch?v=-BuIkhlc-RM\&ab\_channel=UberEngineering)
    * Use case: Long transaction example - UberEATS
  * [Uber Cadence: Fault Tolerant Actor Framework](https://www.youtube.com/watch?v=qce\_AqCkFys\&ab\_channel=AICamp)
    * Use case: Long transaction example

#### TODO
* https://gsmadan.medium.com/building-distributed-high-value-transaction-systems-2076d5d757ad
