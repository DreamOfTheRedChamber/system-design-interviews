
<!-- MarkdownTOC -->

- [Distributed transactions](#distributed-transactions)
	- [Motivation](#motivation)
	- [ACID consistency model](#acid-consistency-model)
		- [Definition](#definition)
		- [Two phase commit](#two-phase-commit)
			- [Assumptions](#assumptions)
			- [Algorithm](#algorithm)
			- [Proof of correctness](#proof-of-correctness)
			- [Limitation](#limitation)
			- [References:](#references)
		- [Three phase commit](#three-phase-commit)
	- [BASE consistency model](#base-consistency-model)
		- [Definition](#definition-1)
		- [Synchronous implementations](#synchronous-implementations)
			- [Use cases](#use-cases)
			- [TCC](#tcc)
			- [Saga](#saga)
		- [Asynchronous implementations](#asynchronous-implementations)
			- [Use cases](#use-cases-1)
			- [MQ](#mq)

<!-- /MarkdownTOC -->

# Distributed transactions
## Motivation
* Database is partitioned across multiple machines for scalability. A transaction might touch more than one partition. How do we guarantee that all of the partitions commit the transaction or none commit the transactions?
* Example: Transfer money from A to B
	1. Debit at A, credit at B, tell the client Okay
	2. Require both banks to do it, or neither
	3. Require that one bank never act alone

## ACID consistency model
### Definition
* Atomic: Everything in a transaction succeeds or the entire transaction is rolled back.
* Consistent: A transaction cannot leave the database in an inconsistent state.
* Isolated: Transactions cannot interfere with each other.
* Durable: Completed transactions persist, even when servers restart etc.

### Two phase commit
#### Assumptions
* The protocol works in the following manner: 
	1. One node is designated the coordinator, which is the master site, and the rest of the nodes in the network are called cohorts.  
	2. Stable storage at each site and use of a write ahead log by each node. 
	3. The protocol assumes that no node crashes forever, and eventually any two nodes can communicate with each other. The latter is not a big deal since network communication can typically be rerouted. The former is a much stronger assumption; suppose the machine blows up!

#### Algorithm
1. PREPARE phase
	1. COORDINATOR: The COORDINATOR sends the message to each COHORT. The COORDINATOR is now in the preparing transaction state. Now the COORDINATOR waits for responses from each of the COHORTS. 
	2. COHORTS:  	
		* If a "PREPARE" message is received for some transaction t which is unknown at the COHORT ( never ran, wiped out by crash, etc ), reply "ABORT". 
		* Otherwise write the new state of the transaction to the UNDO and REDO log in permanent memory. This allows for the old state to be recovered ( in event of later abort ) or committed on demand regardless of crashes. The read locks of a transaction may be released at this time; however, the write locks are still maintained. Now send "AGREED" to the COORDINATOR.
	3. (Optional) COORDINATOR: If after some time period some COHORT has not responded COORDINATOR will retransmit the "PREPARE" message and go to step 1.1.

2. Commit phase
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

#### Proof of correctness
* We assert the claim that if one COHORT completes the transaction all COHORTS complete the transaction eventually. The proof for correctness proceeds somewhat informally as follows: If a COHORT is completing a transaction, it is so only because the COORDINATOR sent it a COMMT message. This message is only sent when the COORDINATOR is in the commit phase, in which case all COHORTS have responded to the COORDINATOR AGREED. This means all COHORTS have prepared the transaction, which implies any crash at this point will not harm the transaction data because it is in permanent memory. Once the COORDINATOR is completing, it is insured every COHORT completes before the COORDINATOR's data is erased. Thus crashes of the COORDINATOR do not interfere with the completion.
* Therefore if any COHORT completes, then they all do. The abort sequence can be argued in a similar manner. Hence the atomicity of the transaction is guaranteed to fail or complete globally.

#### Limitation
1. Deadlock
	- Coordinator failures could become a single point of failure, leading to infinite resource blocking. More specifically, if a cohort has sent an agreement message to the coordinator, it will block until a commit or rollback is received. If the coordinator is permanently down, the cohort will block indefinitely, unless it can obtain the global commit/abort decision from some other cohort. When the coordinator has sent "Query-to-commit" to the cohorts, it will block until all cohorts have sent their local decision.
2. Data inconsistency
	- If in the "Commit phase" after COORDINATOR send "COMMIT" to COHORTS, some COHORTS don't receive the command because of timeout then there will be inconsistency between different nodes. 

#### References: 
1. [Reasoning behind two phase commit](./files/princeton-2phasecommit.pdf)
2. [Discuss failure cases of two phase commits](https://www.the-paper-trail.org/post/2008-11-27-consensus-protocols-two-phase-commit/)

### Three phase commit

## BASE consistency model
### Definition
* Basic availability: The database appears to work most of the time
* Soft-state: Stores don't have to be write-consistent, nor do different replicas have to be mutally consistent all the time
* Eventual consistency: The datastore exhibit consistency at some later point (e.g. lazily at read time)

### Synchronous implementations

#### Use cases
#### TCC
#### Saga

### Asynchronous implementations
#### Use cases
#### MQ