- [Use cases](#use-cases)
- [Functional requirements](#functional-requirements)
  - [Core](#core)
  - [Optional](#optional)
- [Assumptions](#assumptions)
- [Nonfunctional requirements](#nonfunctional-requirements)
- [Real world](#real-world)
  - [Netflix delay queue](#netflix-delay-queue)
  - [Delay queue in RabbitMQ](#delay-queue-in-rabbitmq)
  - [Redisson (Redis Java client with rich feature set)](#redisson-redis-java-client-with-rich-feature-set)
    - [Naive impl in Java](#naive-impl-in-java)
  - [Netflix Fenzo](#netflix-fenzo)
  - [CoachPro (RabbitMQ + MongoDB)](#coachpro-rabbitmq--mongodb)
  - [Spring based distributed scheduling](#spring-based-distributed-scheduling)
  - [Sparrow scheduling (Data analytics)](#sparrow-scheduling-data-analytics)
  - [Airflow](#airflow)
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

# Nonfunctional requirements
* Fault tolerant: Could not miss tasks
* Latency
* High throughput

# Real world

## Netflix delay queue
* Netflix delay queue: [https://netflixtechblog.com/distributed-delay-queues-based-on-dynomite-6b31eca37fbc](https://netflixtechblog.com/distributed-delay-queues-based-on-dynomite-6b31eca37fbc)


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

## Spring based distributed scheduling
* https://www.youtube.com/watch?v=6IQg6oQD6Ew&ab_channel=SpringI%2FO

## Sparrow scheduling (Data analytics)
* https://www.youtube.com/watch?v=A4k0WqjUY9A&ab_channel=AssociationforComputingMachinery%28ACM%29
* Scheduling for data analytics applications
  * Assign it to 

![](../.gitbook/assets/taskScheduler_sparrow.png)

![](../.gitbook/assets/taskScheduler_sparrow.png)

## Airflow
* https://airflow.apache.org/docs/apache-airflow/stable/concepts/dags.html

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
* [How we designed Dropbox ATF: an async task framework](https://dropbox.tech/infrastructure/asynchronous-task-scheduling-at-dropbox)