- [Use cases](#use-cases)
- [Functional requirements](#functional-requirements)
- [Nonfunctional requirements](#nonfunctional-requirements)
- [Callback logic requirements](#callback-logic-requirements)
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
* **Task scheduling**: Clients can schedule tasks to execute at a specified time. Tasks can be scheduled for immediate execution, or delayed to fit the use case.
* **Priority based execution**: Tasks should be associated with a priority. Tasks with higher priority should get executed before tasks with a lower priority once they are ready for execution.
* **Track task status**: Clients can query the status of a scheduled task.
* **Task gating**: ATF enables the the gating of tasks based on lambda, or a subset of tasks on a lambda based on collection. Tasks can be gated to be completely dropped or paused until a suitable time for execution.* Schedule granularity: Execution up to 60x an hour. Set up as many cronjobs as you like. Each of your jobs can be executed up to 60 times an hour. Flexibly configure the execution intervals. Password-protected and SSL-secured URLs are supported.

# Nonfunctional requirements
* **At-least once task execution**: The ATF system guarantees that a task is executed at least once after being scheduled. Execution is said to be complete once the user-defined callback signals task completion to the ATF system.
* **Delivery latency**: 95% of tasks begin execution within five seconds from their scheduled execution time.
* **High availability for task scheduling**: The ATF service is 99.9% available to accept task scheduling requests from any client.
* **No concurrent task execution**: The ATF system guarantees that at most one instance of a task will be actively executing at any given in point. This helps users write their callbacks without designing for concurrent execution of the same task from different locations.
* **Isolation**: Tasks in a given lambda are isolated from the tasks in other lambdas. This isolation spans across several dimensions, including worker capacity for task execution and resource use for task scheduling. Tasks on the same lambda but different priority levels are also isolated in their resource use for task scheduling.

# Callback logic requirements
* **Idempotence**: A single task on a lambda can be executed multiple times within the ATF system. Developers should ensure that their lambda logic and correctness of task execution in clients are not affected by this.
* **Resiliency**: Worker processes which execute tasks might die at any point during task execution. ATF retries abruptly interrupted tasks, which could also be retried on different hosts. Lambda owners must design their lambdas such that retries on different hosts do not affect lambda correctness.
* **Terminal state handling**: ATF retries tasks until they are signaled to be complete from the lambda logic. Client code can mark a task as successfully completed, fatally terminated, or retriable. It is critical that lambda owners design clients to signal task completion appropriately to avoid misbehavior such as infinite retries. 



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