- [Use cases](#use-cases)
- [Functional requirements](#functional-requirements)
  - [Core](#core)
  - [Optional](#optional)
- [Assumptions](#assumptions)
- [Real world](#real-world)
  - [Netflix delay queue](#netflix-delay-queue)
  - [Pager duty task scheduler](#pager-duty-task-scheduler)
    - [Initial solution](#initial-solution)
    - [New solution](#new-solution)
      - [Dynamic load](#dynamic-load)
      - [Outages](#outages)
  - [Delay queue in RabbitMQ](#delay-queue-in-rabbitmq)
  - [Redisson (Redis Java client with rich feature set)](#redisson-redis-java-client-with-rich-feature-set)
    - [Naive impl in Java](#naive-impl-in-java)
  - [Netflix Fenzo](#netflix-fenzo)
  - [CoachPro (RabbitMQ + MongoDB)](#coachpro-rabbitmq--mongodb)
  - [Beanstalk](#beanstalk)
  - [Others](#others)
- [References](#references)

# Use cases

* In payment system, if a user has not paid within 30 minutes after ordering. Then this order should be expired and the inventory needs to be reset. Please see the following flowchart:

```
┌────────────────┐                                               ┌────────────────┐  
│                │               Step2. Add                      │                │  
│ Order Service  │───────────────a message ─────────────────────▶│  Delay Queue   │  
│                │                to queue                       │                │  
└────────────────┘                                               └────────────────┘  
         │                                                                │          
         │                                                                │          
         │                                                                │          
         │                                                                │          
         │                                                       Step3. Service read 
         │                                                       message from queue  
         │                                                                │          
      Step1.                                                              │          
    Save order                                                            │          
         │                                                                │          
         │                                                                │          
         │                                                                │          
         ▼                                                                ▼          
┌────────────────┐              ┌───────────┐                   ┌──────────────────┐ 
│ Order Database │              │   Cache   │    Step4. Check   │  Order TimeOut   │ 
│                │◀─────────────│           │◀───order status───│  Check Service   │ 
└────────────────┘              └───────────┘                   └──────────────────┘
```

* A user scheduled a smart device to perform a specific task at a certain time. When the time comes, the instruction will be pushed to the user's device from the server. 
* Control packet lifetime in networks such as Netty.
* Cron job: 
* Use case: https://youtu.be/ttmzQbaYjjk?t=367

# Functional requirements
## Core
* Ordering. Task1 is only guaranteed to execute before task2 if these three conditions are true:
  * Developers specify that a set of operations should be ordered by using the same orderingId. 
  * Time of task1 schedule time < Time of task2 schedule time.

## Optional
* Schedule granularity: Execution up to 60x an hour. Set up as many cronjobs as you like. Each of your jobs can be executed up to 60 times an hour. Flexibly configure the execution intervals. Password-protected and SSL-secured URLs are supported.
* Status notifications: If you like, we can inform you by email in case a cronjobs execution fails or is successful again after prior failure. You can find detailed status details in the members area. 
* Execution history: View the latest executions of your cronjobs including status, date and time, durations, and response (header and body). You can also view the three next planned execution dates.

# Assumptions
* Tasks are idempotent: If they run second time, nothing bad will happen. 

# Real world

## Netflix delay queue
* Netflix delay queue: [https://netflixtechblog.com/distributed-delay-queues-based-on-dynomite-6b31eca37fbc](https://netflixtechblog.com/distributed-delay-queues-based-on-dynomite-6b31eca37fbc)

## Pager duty task scheduler
* https://www.youtube.com/watch?v=s3GfXTnzG_Y&ab_channel=StrangeLoopConference
* The main problem is it uses Cassandra and Kafka; we don’t have any experience for both neither do we have other use cases than the scheduler which will need Cassandra or Kafka. I’m always reluctant to hosting new database systems, database systems are complex by nature and are not easy when it comes to scaling them. It’s a no go then.

### Initial solution
  * A queue is a column in Cassandra and time is the row.
  * Another component pulls tasks from Cassandra and schedule using a worker pool. 
  * Improved with partition logic

![](../.gitbook/assets/taskScheduler_pagerDuty_old.png)

![](../.gitbook/assets/taskScheduler_pagerDuty_old_partitioned.png)

* Difficulties with old solutions
  * Partition logic is complex and custom
  * Low throughput due to IOs

### New solution
  * Components
    * Kafka - for task buffering and execution
    * Cassandra - for task persistence
    * Akka - for task execution
  * In-memory tasks from Kafka and regularly pulling tasks from Cassandra.

* Challenges
  * Dynamic load
  * Datacenter outages
  * Task ordering

![](../.gitbook/assets/taskScheduler_pagerDuty_new.png)

#### Dynamic load
* Dynamic load in Kafka: Improve Kafka automatically rebalances. 
  * Initial setup
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_1.png)
  * Increase in number of broker needs to be triggered manually. Increase to 3.
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_2.png)
  * Increase to 6.
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_3.png)
  * Should not increase the number of partitions unlimited ??? 

* Dynamic load in service itself
  * Consumers are grouped and healthiness is tracked by Kafka.
  * How fast this process could be actually depends on the how quickly services could respond. 
  * Initial setup
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_service_1.png)

  * Increase service node to 3
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_service_2.png)

* Dynamic load in Cassandra
  * Ring based load balancing

#### Outages
* Kafka
  * 

## Delay queue in RabbitMQ

* RabbitMQ does not have a delay queue. But could use timeout as a workaround. 
  1. When put message into a queue, add a timeout value
  2. When the timeout reaches, the message will be put inside a deadqueue
  3. Then the consumer could pull from the deadqueue

## Redisson (Redis Java client with rich feature set)
* Redisson scheduler sectin: https://github.com/redisson/redisson/wiki/9.-distributed-services/#94-distributed-scheduled-executor-service

### Naive impl in Java
* https://medium.com/nerd-for-tech/distributed-task-scheduler-redis-329475df9dcf

## Netflix Fenzo
* https://github.com/Netflix/Fenzo

## CoachPro (RabbitMQ + MongoDB)
* https://dev.to/imclem/building-a-distributed-scheduler-oap

## Beanstalk
* Cons
  * Not convenient when deleting a msg. 
  * Developed based on C language, not Java and PHP. 

## Others

* db-scheduler / cron.io
* killbill notification queue
* Quartz (Java)
* Xxl-job (Java)
* Celery (Python)
*   Hangfire (C#)

# References

* [https://github.blog/2009-11-03-introducing-resque/](https://github.blog/2009-11-03-introducing-resque/)
* [http://tutorials.jenkov.com/java-concurrency/thread-signaling.html](http://tutorials.jenkov.com/java-concurrency/thread-signaling.html)
* [https://hacpai.com/article/1565796946371](https://hacpai.com/article/1565796946371)
* [https://stackoverflow.com/questions/10868552/scalable-delayed-task-execution-with-redis](https://stackoverflow.com/questions/10868552/scalable-delayed-task-execution-with-redis)
* [https://juejin.im/post/5b5e52ecf265da0f716c3203](https://juejin.im/post/5b5e52ecf265da0f716c3203)
* [https://tech.youzan.com/queuing_delay/](https://tech.youzan.com/queuing_delay/)
* [http://www.throwable.club/2019/09/01/redis-delay-task-second/](http://www.throwable.club/2019/09/01/redis-delay-task-second/)
* [Building a distributed scheduler](https://dev.to/imclem/building-a-distributed-scheduler-oap)