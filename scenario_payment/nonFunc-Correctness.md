- [Correctness](#correctness)
- [Distributed transactions](#distributed-transactions)
  - [Applicable scenarios](#applicable-scenarios)
  - [Use transactional outbox to implement SAGA](#use-transactional-outbox-to-implement-saga)
- [References](#references)
  - [Overview of distributed transactions](#overview-of-distributed-transactions)
  - [ACID distributed transactions](#acid-distributed-transactions)
    - [Assumptions](#assumptions)
    - [Process](#process)
  - [Transaction message based distributed transaction](#transaction-message-based-distributed-transaction)
  - [Distributed Sagas](#distributed-sagas)
    - [Motivation](#motivation)
    - [Definition](#definition)
    - [Approaches](#approaches)
    - [References](#references-1)
  - [Distributed transactional middleware - Seata](#distributed-transactional-middleware---seata)
  - [Uber Cadence](#uber-cadence)


# Correctness
* Any payment bugs that are related to correctness would cause an unacceptable customer experience. When an error occurs it needs to be corrected immediately. Further, the process to remediate such mistakes is time consuming, and usually is complicated due to various legal and compliance constraints.

# Distributed transactions
## Applicable scenarios
* In general, there are three scenarios for distributed transactions:
  * Cross-database distributed transactions
  * Cross-service distributed transactions
  * Hybrid distributed transactions

![](../.gitbook/assets/distributedTransaction_scenarios.png)

## Use transactional outbox to implement SAGA

* The transaction initiator maintains a local message table.
* Business and local message table operations are executed in the same local transaction.
  * If the service is successfully executed, a message with the "to be sent" state is also recorded in the local message table.
  * The system starts a scheduled task to regularly scan the local message table for messages that are in the "to be sent" state annd sends them to MQ. If the sending fails or times out, the message will be resent until it is sent successfully.
* Then, the task will delete the state record from the local message table. The subsequent consumption and subscription process is similar to that of the transactional message mode.

![](../.gitbook/assets/microsvcs\_DistributedTransactions\_localMessageTable.png)



# References
## Overview of distributed transactions
* [In depth analysis](https://www.alibabacloud.com/blog/an-in-depth-analysis-of-distributed-transaction-solutions\_597232)

## ACID distributed transactions
* 2PC:
  1. [Reasoning behind two phase commit](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/files/princeton-2phasecommit.pdf)
  2. [Discuss failure cases of two phase commits](https://www.the-paper-trail.org/post/2008-11-27-consensus-protocols-two-phase-commit/)
  3. [Lecture](https://slideplayer.com/slide/4626345/)
  4. [2PC improvement NewSQL Percolator/PGXC GoldenDB](https://time.geekbang.org/column/article/278949)
* 3PC - Three phase commit
  * [https://www.the-paper-trail.org/post/2008-11-29-consensus-protocols-three-phase-commit/](https://www.the-paper-trail.org/post/2008-11-29-consensus-protocols-three-phase-commit/)
  * [http://courses.cs.vt.edu/\~cs5204/fall00/distributedDBMS/sreenu/3pc.html](http://courses.cs.vt.edu/\~cs5204/fall00/distributedDBMS/sreenu/3pc.html)

### Assumptions

* The protocol works in the following manner:
  1. One node is designated the coordinator, which is the master site, and the rest of the nodes in the network are called cohorts.
  2. Stable storage at each site and use of a write ahead log by each node.
  3. The protocol assumes that no node crashes forever, and eventually any two nodes can communicate with each other. The latter is not a big deal since network communication can typically be rerouted. The former is a much stronger assumption; suppose the machine blows up!

### Process
* Success case

![](../.gitbook/assets/microsvcs\_distributedtransactions\_2pc\_success.png)

* Failure case

![](../.gitbook/assets/microsvcs\_distributedtransactions\_2pc\_failure.png)

## Transaction message based distributed transaction
* [Rocket MQ supports transactional message](https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/)

![](../.gitbook/assets/microsvcs\_DistributedTransaction\_rocketMQ.png)

## Distributed Sagas
### Motivation
* Using distributed transaction to maintain data consistency suffers from the following two pitfalls
  * Many modern technologies including NoSQL databases such as MongoDB and Cassandra don't support them. Distributed transactions aren't supported by modern message brokers such as RabbitMQ and Apache Kafka.
  * It is a form of syncronous IPC, which reduces availability. In order for a distributed transaction to commit, all participating services must be available. If a distributed transaction involves two services that are 99.5% available, then the overall availability is 99\\%. Each additional service involved in a distributed transaction further reduces availability.
* Sagas are mechanisms to maintain data consistency in a microservice architecture without having to use distributed transactions.
* Distributed sagas execute transactions that span multiple physical databases by breaking them into smaller transactions and compensating transactions that operate on single databases.

### Definition

* High entry bar: First need to build a state machine. A saga is a state machine.
* A distributed saga is a collection of requests. Each request has a compensating request on failure. A dsitributed saga guarantees the following properties:
  1. Either all Requests in the Saga are succesfully completed, or
  2. A subset of Requests and their Compensating Requests are executed.
* Limitation: Does not guarantee the separation
  * Solution 1: Semantic lock

### Approaches

* Event-driven choreography: When there is no central coordination, each service produces and listen to other service’s events and decides if an action should be taken or not.
* Command/Orchestration: When a coordinator service is responsible for centralizing the saga’s decision making and sequencing business logic.
  * infoq.com/articles/saga-orchestration-outbox/

### References

* [https://dzone.com/articles/distributed-sagas-for-microservices](https://dzone.com/articles/distributed-sagas-for-microservices)
* [https://chrisrichardson.net/post/antipatterns/2019/07/09/developing-sagas-part-1.html](https://chrisrichardson.net/post/antipatterns/2019/07/09/developing-sagas-part-1.html)

## Distributed transactional middleware - Seata
* Seata is an implementation of variants 2PC.
* [https://github.com/seata/seata](https://github.com/seata/seata)

## Uber Cadence
* [TODO in Chinese](https://time.geekbang.org/course/detail/100053601-264150)
* [Cadence: The only workflow platform you'll ever need](https://www.youtube.com/watch?v=llmsBGKOuWI\&t=792s\&ab\_channel=UberEngineering)
* [Cadence meetup: Introduction to Cadence](https://www.youtube.com/watch?v=-BuIkhlc-RM\&ab\_channel=UberEngineering)
  * Use case: Long transaction example - UberEATS
* [Uber Cadence: Fault Tolerant Actor Framework](https://www.youtube.com/watch?v=qce\_AqCkFys\&ab\_channel=AICamp)
  * Use case: Long transaction example

