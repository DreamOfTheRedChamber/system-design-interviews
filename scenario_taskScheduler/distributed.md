- [Timer + Database](#timer--database)
- [Redis + MySQL](#redis--mysql)
  - [Algorithm](#algorithm)
  - [Components](#components)
- [Pager duty task scheduler](#pager-duty-task-scheduler)
  - [Cassandra + WorkerQueue](#cassandra--workerqueue)
  - [Cassandra + Kafka + Akka](#cassandra--kafka--akka)
    - [Dynamic load](#dynamic-load)
      - [Kafka](#kafka)
      - [Consumer service](#consumer-service)
      - [Cassandra](#cassandra)
    - [Outages](#outages)
      - [Kafka](#kafka-1)
      - [Cassandra](#cassandra-1)
      - [Service](#service)
    - [Task ordering](#task-ordering)
- [Timing wheel](#timing-wheel)
  - [Algorithms](#algorithms)
    - [Simple timing wheel](#simple-timing-wheel)
    - [Hashed wheel](#hashed-wheel)
    - [Hierarchical wheels](#hierarchical-wheels)
  - [High level design](#high-level-design)
    - [Step1. Insert pending task](#step1-insert-pending-task)
    - [Step2. Pull task out from slot](#step2-pull-task-out-from-slot)
    - [Step3. Have a central clock](#step3-have-a-central-clock)
    - [Step4. Guarantee that each task is only executed once](#step4-guarantee-that-each-task-is-only-executed-once)
    - [Step5. Scheduler component for global clock and only once execution](#step5-scheduler-component-for-global-clock-and-only-once-execution)
    - [Step6. Thread pool](#step6-thread-pool)
    - [Step7. Decoupling with message queue](#step7-decoupling-with-message-queue)
  - [Reliability guarantee](#reliability-guarantee)
    - [Scan missed expiry tasks](#scan-missed-expiry-tasks)
    - [Callback service error](#callback-service-error)
- [References](#references)

# Timer + Database

* Initial solution: Creates a table within a database, uses a timer thread to scan the table periodically. 
* Cons:
  * If the volume of data is large and there is a high frequency of insertion rate, then it won't be efficient to lookup and update records. 
  * There is a difference between when task is scheduled to be executed and when the task should be executed. 
* How to optimize: 
  * Shard the table according to task id to boost the lookup efficiency. 

```
INT taskId
TIME expired
INT maxRetryAllowed
INT job status (0: newly created; 1: started; 2: failed; 3: succeeded)
```

# Redis + MySQL

## Algorithm

```
redis> ZADD delayqueue <future_timestamp> "messsage"
redis> MULTI
redis> ZRANGEBYSCORE delayqueue 0 <current_timestamp>
redis> ZREMRANGEBYSCORE delayqueue 0 <current_timestamp>
redis> EXEC
```

## Components

![Delay Queue Components](.gitbook/assets/../../../images/messageQueue_delayqueue.png)

* JobPool: Store all metadata about jobs
  * Stores as key value pairs. Key is job id and value is job struct. 
  * Job struct contains the following:
    1. topic: job category. Needed because each category will has its own callback function. 
    2. id: job unique identifier
    3. delayTime: time to delay before executing the task
    4. ttr: timeout duration for this job to be executed
    5. body: job content
    6. callback: http url for calling a specific function
* Timer: Scan delay bucket and put expired jobs into ready queue
* Delay queue: A list of ordered queues which store all delayed/reserved jobs (only stores job Id)
* Ready queue: A list of ordered queues which store jobs in Ready state.
  * Topic: The same category of job collections
* Response queue: Stores the responses
* Database: Stores the message content
* Dispatcher: It will poll the delay queue and move items to the corresponding topic within ready queues if the tasks are ready. 
* Worker: Workers use BLPOP on the ready queue and process the message. Once done, the response could be put in a response queue and send to consumer. 


# Pager duty task scheduler
* https://www.youtube.com/watch?v=s3GfXTnzG_Y&ab_channel=StrangeLoopConference
* The main problem is it uses Cassandra and Kafka; we don’t have any experience for both neither do we have other use cases than the scheduler which will need Cassandra or Kafka. I’m always reluctant to hosting new database systems, database systems are complex by nature and are not easy when it comes to scaling them. It’s a no go then.

## Cassandra + WorkerQueue
  * A queue is a column in Cassandra and time is the row.
  * Another component pulls tasks from Cassandra and schedule using a worker pool. 
  * Improved with partition logic

![](../.gitbook/assets/taskScheduler_pagerDuty_old.png)

![](../.gitbook/assets/taskScheduler_pagerDuty_old_partitioned.png)

* Difficulties with old solutions
  * Partition logic is complex and custom
  * Low throughput due to IOs

## Cassandra + Kafka + Akka
* Production statistics:
  * Execute 3.1 million jobs per months
  * 8,000 task hourly spikes
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

### Dynamic load
#### Kafka
* Dynamic load in Kafka: Improve Kafka automatically rebalances. 
  * Initial setup
  * Increase in number of broker needs to be triggered manually. Increase to 3.
  * Increase to 6.
  * Should not increase the number of partitions unlimited ??? 

![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_1.png)
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_2.png)
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_3.png)

#### Consumer service
* Dynamic load in service itself
  * Consumers are grouped and healthiness is tracked by Kafka.
  * How fast this process could be actually depends on the how quickly services could respond. 
  * Initial setup
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_service_1.png)

  * Increase service node to 3
![](../.gitbook/assets/taskScheduler_pagerDuty_dynamicLoad_service_2.png)

#### Cassandra
* Dynamic load in Cassandra
  * Ring based load balancing

### Outages
#### Kafka
* Setup:
  * 6 brokers evenly split across 3 DCs.
  * 3 replicas per parition, one in each DC. 
  * Writes replicated to >= 2 DCs. Min in-sync replica: 2
  * Partition leadership failsover automatically

* Outage scenario: Lost Data Center 3. 
  * Broker1 becomes leader for partition P3. 
  * Broker4 becomes leader for partition P6. 
  * However, since only requires 2 in-sync replica, writes still succeed. 

![](../.gitbook/assets/taskScheduler_pagerDuty_outage_kafka_1.png)

![](../.gitbook/assets/taskScheduler_pagerDuty_outage_kafka_2.png)

#### Cassandra
* Setup
  * 5 nodes in 3 DCs.
  * Replication factor of 5
  * Quorum writes guarantee replication to >= 2 DCs.
  * Quorum reads will get latest written value. 
* Outage scenario: Lost DC1
  * Quoram read. Although nodes 4/5 has stale data, Cassandra's policy for last write wins. 

![](../.gitbook/assets/taskScheduler_pagerDuty_outage_cassandra_1.png)

![](../.gitbook/assets/taskScheduler_pagerDuty_outage_cassandra_2.png)

#### Service
* Kafka will detect the healthiness of consumers and reassigns partitions to healthy instances. 
* This will work because:
  * Any service instance can work any task. 
  * Idempotency means that task may be repeated. 
* Outage scenario: Lost DC3
  * Reassign partition3 to service instances 1. 

![](../.gitbook/assets/taskScheduler_pagerDuty_outage_service_1.png)

![](../.gitbook/assets/taskScheduler_pagerDuty_outage_service_2.png)

### Task ordering
* Task defined for any single logical queue

![](../.gitbook/assets/taskScheduler_pagerDuty_ordering_1.png)

* Solution:
  * Logical queue is executed by one service instance. 
  * But one service instance is executing multiple logical queues
  * A failing task stops its logical queue
  * How to prevent all queues being stopped?

![](../.gitbook/assets/taskScheduler_pagerDuty_ordering_2.png)


# Timing wheel
## Algorithms
### Simple timing wheel

* Keep a large timing wheel
* A curser in the timing wheel moves one location every time unit (just like a seconds hand in the clock)
* If the timer interval is within a rotation from the current curser position then put the timer in the corresponding location
* Requires exponential amount of memory

![](../.gitbook/assets/taskScheduler_timingWheel_SimpleAlgo.png)

### Hashed wheel
* Unsorted list
  * Unsorted list in each bucket
  * List can be kept unsorted to avoid worst case O(n) latency for START_TIMER
  * However worst case PER_TICK_BOOKKEEPING = O(n)
  * Again, if n < WheelSize then average O(1)
* Sorted list
  * Sorted Lists in each bucket
  * The list in each bucket can be insertion sorted
  * Hence START_TIMER takes O(n) time in the worst case
  * If  n < WheelSize then average O(1)

![](../.gitbook/assets/taskScheduler_timingWheel_hashAlgo.png)

### Hierarchical wheels
* START_TIMER = O(m) where m is the number of wheels. The bucket value on each wheel needs to be calculated
* STOP_TIMER = O(1)
* PER_TICK_BOOKKEEPING = O(1)  on avg.

![](../.gitbook/assets/taskScheduler_timingWheel_hierarchicalAlgo.png)

## High level design
### Step1. Insert pending task
* We are going to implement Hashed Timing Wheel algorithm with TableKV, supposing there are 10m buckets, and current time is 2021:08:05 11:17:33 +08=(the UNIX timestamp is =1628176653), there is a timer task which is going to be triggered 10s later with start_time = 1628176653 + 10 (or 100000010s later, start_time = 1628176653 + 10 + 100000000), these tasks both will be stored into bucket start_time % 100000000 = 28176663

![](../.gitbook/assets/taskScheduler_timingWheel_InsertPending.png)

### Step2. Pull task out from slot
* As clock tick-tacking to 2021:08:05 11:17:43 +08(1628176663), we need to pull tasks out from slot by calculating the bucket number: current_timestamp(1628176663) % 100000000 = 28176663. After locating the bucket number, we find all tasks in bucket 28176663 with start_time < currenttimestamp=, then we get all expected expiry tasks.

![](../.gitbook/assets/taskScheduler_timingWheel_PullTask.png)

### Step3. Have a central clock
* In order to get the correct time, it's necessary to maintain a monotonic global clock(Of course, it's not the only way to go, there are several ways to handle time and order). Since everything we care about clock is Unix timestamp, we could maintain a global system clock represented by Unix timestamp. All machines request the global clock every second to get the current time, fetching the expiry tasks later.

![](../.gitbook/assets/taskScheduler_timingWheel_centralServer.png)

### Step4. Guarantee that each task is only executed once
* Steps:
  1. All machines fetch global timestamp(timestamp A) with version
  2. All machines increase timestamp(timestamp B) and update version(optimistic locking), only one machine will success because of optimistic locking.
  3. Then the machine acquired mutex is authorized to fetch expiry tasks with timestamp A, the other machines failed to acquire mutex is suspended to wait for 1 seconds.
  4. Loop back to step 1 with timestamp B.

![](../.gitbook/assets/taskScheduler_timingWheel_OnlyExecuteOnce.png)

### Step5. Scheduler component for global clock and only once execution
* We could encapsulate the role who keep acquiring lock and fetch expiry data as an individual component named scheduler.
* Expiry processing is responsible for invoked the user-supplied callback or other user requested action. In distributed computing, it's common to execute a procedure by RPC(Remote Procedure Call). In our case, A RPC request is executed when timer task is expiry, from timer service to callback service. Thus, the caller(user) needs to explicitly tell the timer, which service should I execute with what kind of parameters data while the timer task is triggered.
* We could pack and serialize this meta information and parameters data into binary data, and send it to the timer. When pulling data out from slot, the timer could reconstruct Request/Response/Client type and set it with user-defined data, the next step is a piece of cake, just executing it without saying.

![](../.gitbook/assets/taskScheduler_timingWheel_ExpiryProcessing.png)

### Step6. Thread pool
* Perhaps there are many expiry tasks needed to triggered, in order to handle as many tasks as possible, you could create a thread pool, process pool, coroutine pool to execute RPC concurrently.

![](../.gitbook/assets/taskScheduler_timingWheel_Decoupling.png)

### Step7. Decoupling with message queue
* Supposing the callback service needs tons of operation, it takes a hundred of millisecond. Even though you have created a thread/process/coroutine pool to handle the timer task, it will inevitably hang, resulting in the decrease of throughout.
* As for this heavyweight processing case, Message Queue is a great answer. Message queues can significantly simplify coding of decoupled services, while improving performance, reliability and scalability. It's common to combine message queues with Pub/Sub messaging design pattern, timer could publish task data as message, and timer subscribes the same topic of message, using message queue as a buffer. Then in subscriber, the RPC client executes to request for callback service.

![](../.gitbook/assets/taskScheduler_timingWheel_messageQueue.png)

## Reliability guarantee
### Scan missed expiry tasks
* A missed expiry of tasks may occur because of the scheduler process being shutdown or being crashed, or because of other unknown problems. One important job is how to locate these missed tasks and re-execute them. Since we are using global `currenttimestamp` to fetch expiry data, we could have another scheduler to use `delay10mintimestamp` to fetch missed expiry data.

![](../.gitbook/assets/taskScheduler_timingWheel_missed_expiry_tasks.png)

### Callback service error
* Since the distributed systems are shared-nothing systems, they communicate via message passing through a network(asynchronously or synchronously), but the network is unreliable. When invoking the user-supplied callback, the RPC request might fail if the network is cut off for a while or the callback service is temporarily down.
* Retries are a technique that helps us deal with transient errors, i.e. errors that are temporary and are likely to disappear soon. Retries help us achieve resiliency by allowing the system to send a request repeatedly until it gets an explicit response(success or fail). By leveraging message queue, you obtain the ability for retrying for free. In the meanwhile, the timer could handle the user-requested retries: It's not the proper time to execute callback service, retry it later.

![](../.gitbook/assets/taskScheduler_timingWheel_callbackServiceErrors.png)

# References
* Timing wheel: https://0x709394.me/How-To%20Design%20A%20Reliable%20Distributed%20Timer
