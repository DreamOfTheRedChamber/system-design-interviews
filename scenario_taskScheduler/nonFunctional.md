- [Scan missed expiry tasks](#scan-missed-expiry-tasks)
- [Callback service error](#callback-service-error)
- [At-least-once task execution](#at-least-once-task-execution)
- [No concurrent task execution](#no-concurrent-task-execution)
- [Isolation](#isolation)
- [Delivery latency](#delivery-latency)

## Scan missed expiry tasks
* A missed expiry of tasks may occur because of the scheduler process being shutdown or being crashed, or because of other unknown problems. One important job is how to locate these missed tasks and re-execute them. Since we are using global `currenttimestamp` to fetch expiry data, we could have another scheduler to use `delay10mintimestamp` to fetch missed expiry data.

![](../.gitbook/assets/taskScheduler_timingWheel_missed_expiry_tasks.png)

## Callback service error
* Since the distributed systems are shared-nothing systems, they communicate via message passing through a network(asynchronously or synchronously), but the network is unreliable. When invoking the user-supplied callback, the RPC request might fail if the network is cut off for a while or the callback service is temporarily down.
* Retries are a technique that helps us deal with transient errors, i.e. errors that are temporary and are likely to disappear soon. Retries help us achieve resiliency by allowing the system to send a request repeatedly until it gets an explicit response(success or fail). By leveraging message queue, you obtain the ability for retrying for free. In the meanwhile, the timer could handle the user-requested retries: It's not the proper time to execute callback service, retry it later.

![](../.gitbook/assets/taskScheduler_timingWheel_callbackServiceErrors.png)

## At-least-once task execution
* At-least-once execution is guaranteed in ATF by retrying a task until it completes execution (which is signaled by a Success or a FatalFailure state). All ATF system errors are implicitly considered retriable failures, and lambda owners have an option of marking tasks with a RetriableFailure state. Tasks might be dropped from the ATF execution pipeline in different parts of the system through transient RPC failures and failures on dependencies like Edgestore or SQS. These transient failures at different parts of the system do not affect the at-least-once guarantee, though, because of the system of timeouts and re-polling from Store Consumer.

## No concurrent task execution
* Concurrent task execution is avoided through a combination of two methods in ATF. First, tasks are explicitly claimed through an exclusive task state (Claimed) before starting execution. Once the task execution is complete, the task status is updated to one of Success, FatalFailure or RetriableFailure. A task can be claimed only if its existing task state is Enqueued (retried tasks go to the Enqueued state as well once they are re-pushed onto SQS).
* However, there might be situations where once a long running task starts execution, its heartbeats might fail repeatedly yet the task execution continues. ATF would retry this task by polling it from the store consumer because the heartbeat timeouts would’ve expired. This task can then be claimed by another worker and lead to concurrent execution. 
* To avoid this situation, there is a termination logic in the Executor processes whereby an Executor process terminates itself as soon as three consecutive heartbeat calls fail. Each heartbeat timeout is large enough to eclipse three consecutive heartbeat failures. This ensures that the Store Consumer cannot pull such tasks before the termination logic ends them—the second method that helps achieve this guarantee.

## Isolation
* Isolation of lambdas is achieved through dedicated worker clusters, dedicated queues, and dedicated per-lambda scheduling quotas. In addition, isolation across different priorities within the same lambda is likewise achieved through dedicated queues and scheduling bandwidth.

## Delivery latency
* ATF use cases do not require ultra-low task delivery latencies. Task delivery latencies on the order of a couple of seconds are acceptable. Tasks ready for execution are periodically polled by the Store Consumer and this period of polling largely controls the task delivery latency. Using this as a tuning lever, ATF can achieve different delivery latencies as required. Increasing poll frequency reduces task delivery latency and vice versa. Currently, we have calibrated ATF to poll for ready tasks once every two seconds.