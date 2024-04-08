- [Comparing Multi-processing, multi-threading and coroutine](#comparing-multi-processing-multi-threading-and-coroutine)
- [Concurrent and parallel](#concurrent-and-parallel)
- [Actor model vs Thread](#actor-model-vs-thread)
- [Scheduling](#scheduling)
  - [Thread scheduling algorithms](#thread-scheduling-algorithms)
  - [Process scheduling algorithms](#process-scheduling-algorithms)
- [Thread lifecycle](#thread-lifecycle)
- [Thread Pool](#thread-pool)
- [Thread liveness](#thread-liveness)
- [Deadlock](#deadlock)
  - [Four necessary conditions for deadlock](#four-necessary-conditions-for-deadlock)
  - [How to avoid deadlock by breaking its conditions](#how-to-avoid-deadlock-by-breaking-its-conditions)
  - [Livelock](#livelock)
  - [Starvation](#starvation)


# Comparing Multi-processing, multi-threading and coroutine

* References: [https://sekiro-j.github.io/post/tcp/](https://sekiro-j.github.io/post/tcp/)

|                                                  |                                                                                                                                                                                                                                                                                                         |                                                                                                                                                                                                                                 |                                                                                                                                                                                                 |
| ------------------------------------------------ | :-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| `Criteria`                                       |                                                                                                                                                `Process`                                                                                                                                                |                                                                                                             `Thread`                                                                                                            |                                                                                           `Coroutine`                                                                                           |
| Def                                              |                                                                                                                                        A process runs in CPU core                                                                                                                                       |                                                                                                 A thread lives within a process                                                                                                 |                                                                                  A coroutine lives in a thread                                                                                  |
| Resources                                        |                                                                                Each process has independent system resources. Inter process mechanism such as pipes, sockets, sockets need to be used to share resources.                                                                               |                                              Multiple threads within the same process will share the same heap space but each thread still has its own registers and its own stack.                                             | Coroutine is managed by user, multi-threading is managed by kernel. Developers have better control of the execution flow by using coroutine, for example, a coroutine won’t be forced to yield. |
| Overhead for creation/termination/task switching |                                                                                                                        Slower because the whole process space needs to be copied.                                                                                                                       |                                                                  Faster due to very little memory copying (just thread stack) and less cpu cache to be evicted                                                                  |                                                       Coroutine is extremely light, which means much cheaper, faster than multi-threading.                                                      |
| Synchronization overhead                         |                                                                                                                                        No synchronization needed                                                                                                                                        |                                                                     Shared data that is modified requires special handling based on monitor MESA model impl                                                                     |                                                                                Communicating Sequential Processes                                                                               |
| Use cases                                        | CPU intensive tasks. For example, rendering or printing complicated file formats (such as PDF) can involve significant memory and I/O requirements. Using a single-threaded process and using one process per file to process allows for better throughput vs. using one process with multiple threads. | IO intensive tasks. Threads are a useful choice when you have a workload that consists of lightweight tasks (in terms of processing effort or memory size) that come in, for example with a web server servicing page requests. |                                                                                 IO intensive tasks. Same threads                                                                                |
| Supported frameworks                             |                                                                                                                                  Spark, Hadoop, distributed computing.                                                                                                                                  |                                                                                                   Web servers, Tornado, Gevent                                                                                                  |                            Use coroutine lib like asyncio, gevent, or framework like Tornado for I/O-intensive tasks. Java does not support coroutine yet 07/21/2021                            |

# Concurrent and parallel

* Concurrent: Thread A and Thread B are in same process, takes turns to own the process, yield when waiting for resources or scheduled cpu time is used up. Use case is dealing with I/O-intensive tasks.
* Parallel: Thread A and Thread B are in different processes, execute at the same. Use case is dealing with CPU(Data)-intensive tasks.

# Actor model vs Thread

* Thread is a JVM concept, whereas an Actor is a normal java class that runs in the JVM and hence the question is not so much about Actor vs Thread, its more about how Actor uses Threads.
* At a very simple level, an Actor is an entity that receives messages, one at a time, and reacts to those messages.
* When Actor receives a message, it performs some action in response. How does the action code run in the JVM? Again, if you simplify the situation, you could imagine the Action executing the action task on the current thread. Also, it is possible that the Actor decides to perform the action task on a thread pool. It does not really matter as long as the Actor makes sure that only one message is processed at a time.

# Scheduling

## Thread scheduling algorithms

* Reference: [http://www.cs.cornell.edu/courses/cs4410/2015su/lectures/lec04-scheduling.html](http://www.cs.cornell.edu/courses/cs4410/2015su/lectures/lec04-scheduling.html)

## Process scheduling algorithms

* Reference: [https://www.tutorialspoint.com/operating\_system/os\_process\_scheduling\_algorithms.htm](https://www.tutorialspoint.com/operating\_system/os\_process\_scheduling\_algorithms.htm)

 Thread

# Thread lifecycle

* [Link to the subpage](code/multithreads/ThreadLifeCycle.md)

# Thread Pool

* [Link to the subpage](code/multithreads/ThreadPool.md)

# Thread liveness

# Deadlock

## Four necessary conditions for deadlock

* A deadlock is a situation where a thread is waiting for an object lock that another thread holds, and this second thread is waiting for an object lock that the first thread holds. Since each thread is waiting for the other thread to relinquish a lock, they both remain waiting forever.
* There are four necessary conditions for deadlock to happen
  * **Mutal Exclusion**: Only one process can access a resource at a given time. (Or more accurately, there is limited access to a resource. A deadlock could also occur if a resource has limited quantity. )
  * **Hold and Wait**: Processes already holding a resource can request additional resources, without relinquishing their current resources.
  * **No Preemption**: One process cannot forcibly remove another process' resource.
  * **Circular Wait**: Two or more processes form a circular chain where each process is waiting on another resource in the chain.
* Reference: [https://afteracademy.com/blog/what-is-deadlock-and-what-are-its-four-necessary-conditions\*](https://afteracademy.com/blog/what-is-deadlock-and-what-are-its-four-necessary-conditions\*)

## How to avoid deadlock by breaking its conditions

* Mutual exclusion: Cann't avoid because it is the nature of the problem.
* Hold and wait: Avoid by applying all resources at once.
* No preemption: Avoid by proactively releasing its resources if not getting all necessary resources.
* Circular wait: Avoid by ordering the resources and only acquiring resources by the order.

## Livelock

* Def: A livelock is a recursive situation where two or more threads would keep repeating a particular code logic. The intended logic is typically giving opportunity to the other threads to proceed in favor of 'this' thread.
* Examples:
  * A real-world example of livelock occurs when two people meet in a narrow corridor, and each tries to be polite by moving aside to let the other pass, but they end up swaying from side to side without making any progress because they both repeatedly move the same way at the same time.
  * For example consider a situation where two threads want to access a shared common resource via a Worker object but when they see that other Worker (invoked on another thread) is also 'active', they attempt to hand over the resource to other worker and wait for it to finish. If initially we make both workers active they will suffer from livelock.
* How to avoid:
  * Tries a random duration before acquiring resources.

## Starvation

* Def: In multithreaded application starvation is a situation when a thread is constantly ignored to gain possession of the intrinsic lock in favor of other threads.
* Examples:
  * Starvation describes a situation where a thread is unable to gain regular access to shared resources and is unable to make progress. This happens when shared resources are made unavailable for long periods by "greedy" threads. For example, suppose an object provides a synchronized method that often takes a long time to return. If one thread invokes this method frequently, other threads that also need frequent synchronized access to the same object will often be blocked.
* How to avoid:
  * Fairness is the situation when all threads are given equal opportunity for intrinsic lock acquisition.
