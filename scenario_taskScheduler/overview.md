- [Use cases](#use-cases)
- [Functional requirements](#functional-requirements)
  - [Advanced requirements](#advanced-requirements)
- [Nonfunctional requirements](#nonfunctional-requirements)
- [Callback logic requirements](#callback-logic-requirements)
- [Timer algorithm](#timer-algorithm)
- [Real world](#real-world)
  - [Single machine timer comparison](#single-machine-timer-comparison)
  - [Single machine delayed scheduler](#single-machine-delayed-scheduler)
  - [Comparison between distributed ones](#comparison-between-distributed-ones)
  - [Netflix delay queue](#netflix-delay-queue)
  - [Bigben](#bigben)
  - [Shedlock](#shedlock)
  - [ElasticJob](#elasticjob)
  - [Kubernetes](#kubernetes)
  - [Delay queue in RabbitMQ](#delay-queue-in-rabbitmq)
  - [Redisson (Redis Java client with rich feature set)](#redisson-redis-java-client-with-rich-feature-set)
    - [Naive impl in Java](#naive-impl-in-java)
  - [Netflix Fenzo](#netflix-fenzo)
  - [CoachPro (RabbitMQ + MongoDB)](#coachpro-rabbitmq--mongodb)
  - [Spring based distributed scheduling](#spring-based-distributed-scheduling)
  - [Sparrow scheduling (Data analytics)](#sparrow-scheduling-data-analytics)
  - [Airflow](#airflow)
  - [Dropbox](#dropbox)
  - [Others](#others)
  - [Cron](#cron)
  - [微信实现定时器](#微信实现定时器)
  - [Quartz应用实践](#quartz应用实践)
  - [美图](#美图)
  - [微服务编排](#微服务编排)
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

## Advanced requirements
* **Periodic task execution**: Currently, ATF is a system for one-time task scheduling. Building support for periodic task execution as an extension to this framework would be useful in unlocking new capabilities for our clients.
* **Better support for task chaining**: Currently, it is possible to chain tasks on ATF by scheduling a task onto ATF that then schedules other tasks onto ATF during its execution. Although it is possible to do this in the current ATF setup, visibility and control on this chaining is absent at the framework level. Another natural extension here would be to better support task chaining through framework-level visibility and control, to make this use case a first class concept in the ATF model.
* **Dead letter queues for misbehaving tasks**: 
  * One common source of maintenance overhead we observe on ATF is that some tasks get stuck in infinite retry loops due to occasional bugs in lambda logic. This requires manual intervention from the ATF framework owners in some cases where there are a large number of tasks stuck in such loops, occupying a lot of the scheduling bandwidth in the system. Typical manual actions in response to such a situation include pausing execution of the lambdas with misbehaving tasks, or dropping them outright.
  * One way to reduce this operational overhead and provide an easy interface for lambda owners to recover from such incidents would be to create dead letter queues filled with such misbehaving tasks. The ATF framework could impose a maximum number of retries before tasks are pushed onto the dead letter queue. We could create and expose tools that make it easy to reschedule tasks from the dead letter queue back into the ATF system, once the associated lambda bugs are fixed.

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

# Timer algorithm
* https://paulcavallaro.com/blog/hashed-and-hierarchical-timing-wheels/

# Real world
## Single machine timer comparison
* https://www.modb.pro/db/107974

## Single machine delayed scheduler
* https://soulmachine.gitbooks.io/system-design/content/cn/task-scheduler.html
* https://zhuanlan.zhihu.com/p/228420432

## Comparison between distributed ones
* https://www.cnblogs.com/javastack/p/15025904.html

## Netflix delay queue
* Netflix delay queue: [https://netflixtechblog.com/distributed-delay-queues-based-on-dynomite-6b31eca37fbc](https://netflixtechblog.com/distributed-delay-queues-based-on-dynomite-6b31eca37fbc)

## Bigben
* https://medium.com/walmartglobaltech/an-approach-to-designing-distributed-fault-tolerant-horizontally-scalable-event-scheduler-278c9c380637

## Shedlock
* https://github.com/lukas-krecan/ShedLock

## ElasticJob
* https://shardingsphere.apache.org/elasticjob/current/en/overview/

## Kubernetes
* https://www.youtube.com/watch?v=Vt1iS5q1uzk&ab_channel=NDCConferences

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

## Dropbox
* https://dropbox.tech/infrastructure/asynchronous-task-scheduling-at-dropbox#architecture

## Others

* db-scheduler / cron.io
* killbill notification queue
* Quartz (Java)
* Xxl-job (Java)
* Celery (Python)
*   Hangfire (C#)

## Cron
* Cron and crontab: https://www.hostgator.com/help/article/what-are-cron-jobs

## 微信实现定时器
* 有许多nonFunc的设计和整体的流程图：https://cloud.tencent.com/developer/article/1807494

## Quartz应用实践
* MeituanJust soso 基于数据库行锁的分布式调度器: https://tech.meituan.com/2014/08/31/mt-crm-quartz.html
  * Also touched on JDK based timer impl pros and cons
* IBM series:
  1. 企业级任务调度框架Quartz(1) --企业应用中的任务调度介绍: https://www.shuzhiduo.com/A/8Bz8o8yNdx/
* Aliyun: https://developer.aliyun.com/article/355202 
* https://cxybb.com/article/zhongwumao/81077503

## 美图
* https://zhuanlan.zhihu.com/p/94082947


## 微服务编排
* https://zhuanlan.zhihu.com/p/67244072

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
* [Leetcode design job scheduler-TODO](https://leetcode.com/discuss/general-discussion/1082786/System-Design%3A-Designing-a-distributed-Job-Scheduler-or-Many-interesting-concepts-to-learn)