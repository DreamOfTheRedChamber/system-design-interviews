<!-- MarkdownTOC -->

- [Microservices](#microservices)
	- [Problems](#problems)
		- [Data distribution](#data-distribution)
			- [Use case](#use-case)
			- [Approaches](#approaches)
				- [Transactional outbox](#transactional-outbox)
				- [CDC \(Change data capture\)](#cdc-change-data-capture)
		- [Data join](#data-join)
			- [Use case](#use-case-1)
			- [Approaches](#approaches-1)
				- [CQRS](#cqrs)
		- [Distributed transactions](#distributed-transactions)
			- [Definition](#definition)
			- [Approaches](#approaches-2)
				- [2PC](#2pc)
				- [TCC](#tcc)
				- [Saga pattern](#saga-pattern)
		- [Breakdown monolithic](#breakdown-monolithic)
			- [Definition](#definition-1)
			- [Steps to migration](#steps-to-migration)
			- [Key difficult points](#key-difficult-points)
			- [Example](#example)

<!-- /MarkdownTOC -->


# Microservices
## Problems
### Data distribution
* Definition: Double write. How to guarantee the ACID of two writes

#### Use case
* Data replication
* Database migration
* Implement CQRS or remove join from database
* Implement distributed transactions

#### Approaches
##### Transactional outbox
* Flowchart

![MySQL HA github](./images/microservices_transactionalOutbox.png)

* Implementation: Killbill common queue

![MySQL HA github](./images/microservices_transactionalOutbox_implementation.png)

##### CDC (Change data capture)
* Flowchart

![MySQL HA github](./images/microservices_changeDataCapture.png)

* Implementation:
	- Alibaba Canal (recommended)
	- Redhat Debezium
	- Zendesk Maxell
	- Airbnb SpinalTap

### Data join

#### Use case
* N+1 problem
* Data volume
* Performance

#### Approaches

##### CQRS
* Flowchart

![MySQL HA github](./images/microservices_join_cqrs.png)

* Possible problems: Eventual consistency

![MySQL HA github](./images/microservices_join_cqrs_problem.png)

* Possible solutions:

![MySQL HA github](./images/microservices_join_cqrs_problem_solution.png)

### Distributed transactions
#### Definition 
* https://www.codingapi.com/blog/2020/02/01/txlcn002/

![MySQL HA github](./images/microservices_distributedtransaction_guarantee.png)

#### Approaches
##### 2PC
- Implementation: Alibaba Seata - 2PC commit. Not used
- Grow ups don't use distributed transactions

##### TCC
- Implementation: Not used

##### Saga pattern
- High entry bar: First need to build a state machine
- Two types of sagar:
	- Choreography Saga
	- Orchestration Saga
- Implementation: Uber Cadence
- Limitation: Does not guarantee the separation
	- Solution 1: Semantic lock

### Breakdown monolithic
#### Definition
* Velocity slow
* Hard to continue scale vertically
* Independent deployment capability

#### Steps to migration

#### Key difficult points
* Understand the APIs
* Database migration
	- Incremental and rollbackable
	- Data 
* Use data distribution and remove join

#### Example
* StichFix - Scaling your architecture with services and events






