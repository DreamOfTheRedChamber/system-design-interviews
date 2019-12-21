
<!-- MarkdownTOC -->

- [Distributed transactions](#distributed-transactions)
	- [Motivation](#motivation)
	- [ACID consistency model](#acid-consistency-model)
		- [Definition](#definition)
		- [Use cases](#use-cases)
		- [XA standard](#xa-standard)
			- [Two phase commit](#two-phase-commit)
				- [Steps](#steps)
			- [Three phase commit](#three-phase-commit)
	- [BASE consistency model](#base-consistency-model)
		- [Definition](#definition-1)
		- [Synchronous implementations](#synchronous-implementations)
			- [Use cases](#use-cases-1)
			- [TCC](#tcc)
			- [Saga](#saga)
		- [Asynchronous implementations](#asynchronous-implementations)
			- [Use cases](#use-cases-2)
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

### Use cases

### XA standard
#### Two phase commit
##### Steps
1. Prepare phase: Transaction manager coordinates all of the transaction resources to commit or abort
	1. Records information in the redo logs so that it can subsequently either commit or roll back the transaction, regardless of intervening failures
	2. Places a distributed lock on modified tables, which prevents reads
2. Commit phase: Transaction manager decides to finalize operation by committing or aborting according to the votes of the each transaction resource

* References: ![Reasoning behind two phase commit](./files/princeton-2phasecommit.pdf)

#### Three phase commit

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