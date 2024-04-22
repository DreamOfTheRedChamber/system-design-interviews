
- [Benefits](#benefits)
  - [Asynchronous vs low performance](#asynchronous-vs-low-performance)
    - [Flash sale scenario](#flash-sale-scenario)
  - [Decouple vs low extensibility](#decouple-vs-low-extensibility)
  - [Smooth traffic vs low fault tolerance](#smooth-traffic-vs-low-fault-tolerance)
    - [Broadcast room](#broadcast-room)
    - [Example - MQ based Distributed transaction](#example---mq-based-distributed-transaction)
- [References](#references)
  - [TODO: Message ordering in MQ](#todo-message-ordering-in-mq)
  - [TODO: After Kafka: Pulsar](#todo-after-kafka-pulsar)

# Benefits
## Asynchronous vs low performance
* Defer processing of time-consuming tasks without blocking our clients. Anything that is slow or unpredictable is a candidate for asynchronous processing. Example include
  * Interact with remote servers
  * Low-value processing in the critical path
  * Resource intensive work
  * Independent processing of high- and low- priority jobs

### Flash sale scenario
* For example, in flash sale scenarios. Fast operations (deduct inventory number inside cache) could be performed in real time while slow operations (Whether the user has participated in flash sale before, deduct inventory number inside DB) could be put inside a queue. 

![](../.gitbook/assets/messageQueue_benefits_flashSale.png)

## Decouple vs low extensibility
## Smooth traffic vs low fault tolerance


### Broadcast room

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                Message Queue                                                │
│                                                                                                             │
│                                                                                                             │
│   ┌──────────────┐      ┌──────────────┐                            ┌──────────────┐     ┌──────────────┐   │
│   │    Topic     │      │    Topic     │                            │    Topic     │     │    Topic     │   │
│   │              │      │              │                            │              │     │              │   │
│   │  msg_room_0  │      │  msg_room_1  │                            │ msg_room_N-1 │     │  msg_room_N  │   │
│   └──────────────┘      └──────────────┘                            └──────────────┘     └──────────────┘   │
│                                                                                                             │
│                                                                                                             │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘






        ┌─────────────┐                      ┌─────────────┐                    ┌─────────────┐                
        │             │                      │             │                    │             │                
        │Web server 1 │                      │    ....     │                    │Web server N │                
        │             │                      │             │                    │             │                
        └─────────────┘                      └─────────────┘                    └─────────────┘                
               ▲                                                                                               
               │                                                                                               
               ├─────────────────────┐                                                                         
               │                     │                                                                         
               │                     │                                                                         
               │                     │                                                                         
        ┌─────────────┐        ┌─────────────┐                                  ┌─────────────┐                
        │             │        │             │                                  │             │                
        │   Client1   │        │   Client2   │                                  │   ClientN   │                
        │             │        │             │                                  │             │                
        └─────────────┘        └─────────────┘                                  └─────────────┘
```

### Example - MQ based Distributed transaction

* Reliable message producing
  * Data needs to be persisted
  * Confirmation needs to be obtained
  * Producer needs to retry
* Reliable message consumption
  * Ack mechanism
* Please see this [link](https://github.com/DreamOfTheRedChamber/system-design/blob/master/distributedTransactions.md#message-queue-based-implementation)

# References
* Enterprise integration patterns: https://www.enterpriseintegrationpatterns.com/index.html

## TODO: Message ordering in MQ
* https://zhuanlan.zhihu.com/p/59000202

## TODO: After Kafka: Pulsar
* https://zhuanlan.zhihu.com/p/64901908