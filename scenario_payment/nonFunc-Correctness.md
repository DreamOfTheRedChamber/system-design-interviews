- [Distributed transactions](#distributed-transactions)
  - [ABA](#aba)
  - [Motivation](#motivation)
  - [Distributed algorithm comparison](#distributed-algorithm-comparison)
    - [BFT (Byzantine fault tolerance)](#bft-byzantine-fault-tolerance)
  - [ACID consistency model](#acid-consistency-model)
      - [Assumptions](#assumptions)
      - [Process](#process)
    - [Seata](#seata)
  - [BASE consistency model](#base-consistency-model)
    - [Local message based distributed transactions](#local-message-based-distributed-transactions)
    - [RocketMQ transactional message based distributed transactions](#rocketmq-transactional-message-based-distributed-transactions)
      - [Concept](#concept)
      - [Process](#process-1)
    - [Distributed Sagas](#distributed-sagas)
      - [Motivation](#motivation-1)
      - [Definition](#definition)
      - [Assumptions](#assumptions-1)
      - [Approaches](#approaches)
      - [Examples](#examples)
      - [Pros](#pros)
      - [Cons](#cons)
      - [References](#references)
    - [Real world](#real-world)
      - [Uber Cadence](#uber-cadence)
      - [TODO](#todo)
    - [References](#references-1)
      - [Distributed transactions](#distributed-transactions-1)

# Distributed transactions
* Any payment bugs that are related to correctness would cause an unacceptable customer experience. When an error occurs it needs to be corrected immediately. Further, the process to remediate such mistakes is time consuming, and usually is complicated due to various legal and compliance constraints.

## ABA
* Update
  * Example (Update to absolute value): Update user set age = 18 where uid = 58.
    * Suffers from ABA problem in multi-thread environment
      1. current age = 17
      2. operation A: set age = 18
      3. operation B: set age = 19
      4. operation A: set age = 18
    * Needs optimistic concurrency control (version number) to guarantee idempotence
      1. current age = 17
      2. operation A: set age = 19, v++ where v = 1;
      3. Operation B: set age = 18, v++ where v = 1;
  * Example (Update to relative value): Update user set age++ where uid = 58
    * Convert to absolute example

## Motivation

* Database is partitioned across multiple machines for scalability. A transaction might touch more than one partition. How do we guarantee that all of the partitions commit the transaction or none commit the transactions?
* Example:
  * Transfer money from A to B
    1. Debit at A, credit at B, tell the client Okay
    2. Require both banks to do it, or neither
    3. Require that one bank never act alone
  * A travel booking edge service invokes several low level services (car rental service, hotel reservation service, airline reservation service)

## Distributed algorithm comparison

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

### BFT (Byzantine fault tolerance)

* Within a distributed system, there are no malicious behaviors but could be fault behaviors such as process crashing, hardware bugs, etc.

## ACID consistency model
#### Assumptions

* The protocol works in the following manner:
  1. One node is designated the coordinator, which is the master site, and the rest of the nodes in the network are called cohorts.
  2. Stable storage at each site and use of a write ahead log by each node.
  3. The protocol assumes that no node crashes forever, and eventually any two nodes can communicate with each other. The latter is not a big deal since network communication can typically be rerouted. The former is a much stronger assumption; suppose the machine blows up!

#### Process
* Success case

![](../.gitbook/assets/microsvcs\_distributedtransactions\_2pc\_success.png)

* Failure case

![](../.gitbook/assets/microsvcs\_distributedtransactions\_2pc\_failure.png)

### Seata

* Seata is an implementation of variants 2PC.
* [https://github.com/seata/seata](https://github.com/seata/seata)

## BASE consistency model

### Local message based distributed transactions

* The transaction initiator maintains a local message table.
* Business and local message table operations are executed in the same local transaction.
  * If the service is successfully executed, a message with the "to be sent" state is also recorded in the local message table.
  * The system starts a scheduled task to regularly scan the local message table for messages that are in the "to be sent" state annd sends them to MQ. If the sending fails or times out, the message will be resent until it is sent successfully.
* Then, the task will delete the state record from the local message table. The subsequent consumption and subscription process is similar to that of the transactional message mode.

![](../.gitbook/assets/microsvcs\_DistributedTransactions\_localMessageTable.png)

### RocketMQ transactional message based distributed transactions
* Transactional message is similar to local message table.

#### Concept
* Half(Prepare) Message: Refers to a message that cannot be delivered temporarily. When a message is successfully sent to the MQ server, but the server did not receive the second acknowledgement of the message from the producer, then the message is marked as “temporarily undeliverable”. The message in this status is called a half message.
* Message Status Check: Network disconnection or producer application restart may result in the loss of the second acknowledgement of a transactional message. When MQ server finds that a message remains a half message for a long time, it will send a request to the message producer, checking the final status of the message (Commit or Rollback).

#### Process
1. Producer send half message to MQ server.
2. After send half message succeed, execute local transaction.
3. Send commit or rollback message to MQ Server based on local transaction results.
4. If commit/rollback message missed or producer pended during the execution of local transaction，MQ server will send check message to each producers in the same group to obtain transaction status.
5. Producer reply commit/rollback message based on local transaction status.
6. Committed message will be delivered to consumer but rolled back message will be discarded by MQ server.

![](../.gitbook/assets/microsvcs\_DistributedTransaction\_rocketMQ.png)

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

### Distributed Sagas
#### Motivation
* Using distributed transaction to maintain data consistency suffers from the following two pitfalls
  * Many modern technologies including NoSQL databases such as MongoDB and Cassandra don't support them. Distributed transactions aren't supported by modern message brokers such as RabbitMQ and Apache Kafka.
  * It is a form of syncronous IPC, which reduces availability. In order for a distributed transaction to commit, all participating services must be available. If a distributed transaction involves two services that are 99.5% available, then the overall availability is 99\\%. Each additional service involved in a distributed transaction further reduces availability.
* Sagas are mechanisms to maintain data consistency in a microservice architecture without having to use distributed transactions.
* Distributed sagas execute transactions that span multiple physical databases by breaking them into smaller transactions and compensating transactions that operate on single databases.

#### Definition

* High entry bar: First need to build a state machine. A saga is a state machine.
* A distributed saga is a collection of requests. Each request has a compensating request on failure. A dsitributed saga guarantees the following properties:
  1. Either all Requests in the Saga are succesfully completed, or
  2. A subset of Requests and their Compensating Requests are executed.
* Limitation: Does not guarantee the separation
  * Solution 1: Semantic lock

#### Assumptions

* For distributed sagas to work, both requests and compensating requests need to obey certain characteristics:
  1. Requests and Compensating Requests must be idempotent, because the same message may be delivered more than once. However many times the same idempotent request is sent, the resulting outcome must be the same. An example of an idempotent operation is an UPDATE operation. An example of an operation that is NOT idempotent is a CREATE operation that generates a new id every time.
  2. Compensating Requests must be commutative, because messages can arrive in order. In the context of a distributed saga, it’s possible that a Compensating Request arrives before its corresponding Request. If a BookHotel completes after CancelHotel, we should still arrive at a cancelled hotel booking (not re-create the booking!)
  3. Requests can abort, which triggers a Compensating Request. Compensating Requests CANNOT abort, they have to execute to completion no matter what.

#### Approaches

* Event-driven choreography: When there is no central coordination, each service produces and listen to other service’s events and decides if an action should be taken or not.
* Command/Orchestration: When a coordinator service is responsible for centralizing the saga’s decision making and sequencing business logic.

#### Examples

tation: Uber Cadence

#### Pros

* Support for long-lived transactions. Because each microservice focuses only on its own local atomic transaction, other microservices are not blocked if a microservice is running for a long time. This also allows transactions to continue waiting for user input. Also, because all local transactions are happening in parallel, there is no lock on any object.

#### Cons

* Difficult to debug, especially when many microservices are involved.
* The event messages could become difficult to maintain if the system gets complex.
* It does not have read isolation. For example, the customer could see the order being created, but in the next second, the order is removed due to a compensation transaction.

#### References

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
* infoq.com/articles/saga-orchestration-outbox/

### References
#### Distributed transactions
* 2PC:
  1. [Reasoning behind two phase commit](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/files/princeton-2phasecommit.pdf)
  2. [Discuss failure cases of two phase commits](https://www.the-paper-trail.org/post/2008-11-27-consensus-protocols-two-phase-commit/)
  3. [Lecture](https://slideplayer.com/slide/4626345/)
  4. [2PC improvement NewSQL Percolator/PGXC GoldenDB](https://time.geekbang.org/column/article/278949)
* 3PC - Three phase commit
  * [https://www.the-paper-trail.org/post/2008-11-29-consensus-protocols-three-phase-commit/](https://www.the-paper-trail.org/post/2008-11-29-consensus-protocols-three-phase-commit/)
  * [http://courses.cs.vt.edu/\~cs5204/fall00/distributedDBMS/sreenu/3pc.html](http://courses.cs.vt.edu/\~cs5204/fall00/distributedDBMS/sreenu/3pc.html)