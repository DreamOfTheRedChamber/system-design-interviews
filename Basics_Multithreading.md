<!-- MarkdownTOC -->

- [Multithreading](#multithreading)
	- [Big picture first](#big-picture-first)
		- [Comparing Multi-processing, multi-threading and coroutine](#comparing-multi-processing-multi-threading-and-coroutine)
		- [Concurrent and parallel](#concurrent-and-parallel)
		- [Scheduling](#scheduling)
			- [Thread scheduling algorithms](#thread-scheduling-algorithms)
			- [Process scheduling algorithms](#process-scheduling-algorithms)
	- [Thread](#thread)
		- [Thread lifecycle](#thread-lifecycle)
		- [Thread Pool](#thread-pool)
	- [JMM (Java memory model)](#jmm-java-memory-model)
		- [Atomic](#atomic)
		- [Reorder](#reorder)
		- [Visibility](#visibility)
			- [Volatile keyword](#volatile-keyword)
		- [Happens Before principles](#happens-before-principles)
		- [Final keyword](#final-keyword)
		- [Counter example](#counter-example)
	- [Monitor](#monitor)
		- [Def](#def)
		- [Relationship with Mutex and semaphore](#relationship-with-mutex-and-semaphore)
			- [Mutex](#mutex)
			- [Semaphore](#semaphore)
			- [Mutex vs Semaphore](#mutex-vs-semaphore)
		- [Mesa, Hasen and Hoare model](#mesa-hasen-and-hoare-model)
		- [JDK 1.5 Implementation with synchronized, wait and notify](#jdk-15-implementation-with-synchronized-wait-and-notify)
			- [synchronized for accessing mutual exclusive resources](#synchronized-for-accessing-mutual-exclusive-resources)
				- [Scope of lock](#scope-of-lock)
				- [Internals](#internals)
				- [Downsides](#downsides)
					- [Could only perform operation on a single variable, not multiples](#could-only-perform-operation-on-a-single-variable-not-multiples)
					- [Could not break deadlock by releasing the lock proactively](#could-not-break-deadlock-by-releasing-the-lock-proactively)
				- [Optimization after JDK 1.6](#optimization-after-jdk-16)
					- [Bias, lightweight and heavyweight lock](#bias-lightweight-and-heavyweight-lock)
					- [Lock coarsening and elision](#lock-coarsening-and-elision)
					- [Adaptive spinning](#adaptive-spinning)
			- [Wait and notify methods for coordinating threads](#wait-and-notify-methods-for-coordinating-threads)
				- [Use cases](#use-cases)
					- [Asynchronous programming](#asynchronous-programming)
		- [JDK 1.6 Improved implementation with Lock and Condition](#jdk-16-improved-implementation-with-lock-and-condition)
			- [Motivation](#motivation)
				- [Four necessary conditions for deadlock](#four-necessary-conditions-for-deadlock)
				- [How to avoid deadlock by breaking its conditions](#how-to-avoid-deadlock-by-breaking-its-conditions)
				- [Limitation of synchronized keyword](#limitation-of-synchronized-keyword)
			- [Lock for accessing mutual exclusive resources](#lock-for-accessing-mutual-exclusive-resources)
			- [Condition for coordinating threads](#condition-for-coordinating-threads)
		- [References](#references)
	- [CAS](#cas)
		- [Cons](#cons)
			- [ABA problem](#aba-problem)
			- [Spin lock CPU consumption](#spin-lock-cpu-consumption)
		- [Impl](#impl)
			- [Atomic classes](#atomic-classes)
	- [AQS](#aqs)
		- [Motivation](#motivation-1)
		- [Internals](#internals-1)
		- [Create impl inheriting AQS](#create-impl-inheriting-aqs)
	- [Lock](#lock)
		- [ReentrantLock](#reentrantlock)
		- [ReadWriteLock](#readwritelock)
		- [StampedLock](#stampedlock)
	- [Concurrency control](#concurrency-control)
		- [Semaphore](#semaphore-1)
		- [CountdownLatch](#countdownlatch)
		- [CyclicBarrier](#cyclicbarrier)
	- [Java Concurrent Utilities - JCU](#java-concurrent-utilities---jcu)
		- [List](#list)
			- [CopyOnWriteArrayList](#copyonwritearraylist)
		- [Map](#map)
			- [ConcurrentHashMap](#concurrenthashmap)
			- [ConcurrentSkiplistMap](#concurrentskiplistmap)
		- [Set](#set)
			- [CopyOnWriteArraySet](#copyonwritearrayset)
			- [ConcurrentSkipListSet](#concurrentskiplistset)
		- [Queue](#queue)
			- [BlockingQueue](#blockingqueue)
				- [ArrayBlockingQueue](#arrayblockingqueue)
				- [LinkedBlockingQueue](#linkedblockingqueue)
				- [SynchronousQueue](#synchronousqueue)
				- [LinkedTransferQueue](#linkedtransferqueue)
				- [PriorityBlockingQueue](#priorityblockingqueue)
				- [DelayQueue](#delayqueue)
			- [BlockingDeque](#blockingdeque)
			- [ConcurrentLinkedQueue](#concurrentlinkedqueue)
			- [ConcurrentLinkedDeque](#concurrentlinkeddeque)
	- [Lock alternatives](#lock-alternatives)
		- [Thread confinement](#thread-confinement)
			- [ThreadLocal](#threadlocal)
			- [Stack confinement](#stack-confinement)
			- [Adhoc confinement](#adhoc-confinement)
			- [Disruptor](#disruptor)
		- [CopyOnWrite](#copyonwrite)
	- [Reduce lock](#reduce-lock)
		- [Flyweight pattern](#flyweight-pattern)
	- [Future task](#future-task)
	- [Design small utils](#design-small-utils)
		- [Singleton pattern](#singleton-pattern)
		- [Blocking queue](#blocking-queue)
		- [Delayed scheduler](#delayed-scheduler)
		- [ConcurrentHashmap](#concurrenthashmap-1)
		- [ThreadPool](#threadpool)
	- [References](#references-1)

<!-- /MarkdownTOC -->

# Multithreading
## Big picture first 
### Comparing Multi-processing, multi-threading and coroutine
* References: https://sekiro-j.github.io/post/tcp/

| `Criteria`|`    Process    `|`   Thread   `|`    Coroutine    `|
| --------------------- |:-------------:|:--------:|:---------:|
| Def  | A process runs in CPU core  | A thread lives within a process | A coroutine lives in a thread |
| Resources |  Each process has independent system resources. Inter process mechanism such as pipes, sockets, sockets need to be used to share resources. | Multiple threads within the same process will share the same heap space but each thread still has its own registers and its own stack. | Coroutine is managed by user, multi-threading is managed by kernel. Developers have better control of the execution flow by using coroutine, for example, a coroutine won’t be forced to yield. |
| Overhead for creation/termination/task switching  |  Slower because the whole process space needs to be copied. | Faster due to very little memory copying (just thread stack) and less cpu cache to be evicted | Coroutine is extremely light, which means much cheaper, faster than multi-threading.  |
| Synchronization overhead |  No synchronization needed | Shared data that is modified requires special handling | TO BE ADDED |
| Use cases  | CPU intensive tasks. For example, rendering or printing complicated file formats (such as PDF) can involve significant memory and I/O requirements. Using a single-threaded process and using one process per file to process allows for better throughput vs. using one process with multiple threads. | IO intensive tasks. Threads are a useful choice when you have a workload that consists of lightweight tasks (in terms of processing effort or memory size) that come in, for example with a web server servicing page requests. |  IO intensive tasks. Same threads  |
| Supported frameworks | Spark, Hadoop, distributed computing. | Web servers, Tornado, Gevent | Use coroutine lib like asyncio, gevent, or framework like Tornado for I/O-intensive tasks. Java does not support coroutine yet 07/21/2021 |


### Concurrent and parallel
* Concurrent: Thread A and Thread B are in same process, takes turns to own the process, yield when waiting for resources or scheduled cpu time is used up. Use case is dealing with I/O-intensive tasks.
* Parallel: Thread A and Thread B are in different processes, execute at the same. Use case is dealing with CPU(Data)-intensive tasks.

### Scheduling
#### Thread scheduling algorithms
#### Process scheduling algorithms

## Thread

### Thread lifecycle
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/ThreadLifeCycle.md)

### Thread Pool
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/ThreadPool.md)


## JMM (Java memory model)
### Atomic

### Reorder
### Visibility
#### Volatile keyword
### Happens Before principles
### Final keyword

### Counter example
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/Counter.md)

## Monitor
### Def
* Monitor in Java is not a special object. It's synchronization mechanism placed at class hierarchy root: java.lang.Object. This synchronization mechanism manages how to operate on shared variables. 
* There are many methods on the Object class including wait(), notify() and their siblings e.g. notifyAll().
* References: http://pages.cs.wisc.edu/~sschang/OS-Qual/process/Mesa_monitor.htm

### Relationship with Mutex and semaphore
#### Mutex
* A mutex is attached to every object in Java. 
* Within a mutex, only two states are available: unlocked and locked. 
* Java has no mechanism that would let you set the mutex value directly, something similar to below

```java
Object myObject = new Object();
Mutex mutex = myObject.getMutex();
mutex.free();
```

#### Semaphore
* A semaphore is a tool for synchronizing access to some resource. Its distinctive feature is that it uses a counter to create the synchronization mechanism. 
* Semaphores in Java are represented by the Semaphore class. 
* When creating semaphore objects, we can use the following constructors:

```java
//parameters
//int permits — the initial and maximum value of the counter. In other words, this parameter determines how many threads can simultaneously access the shared resource;
//boolean fair — establishes the order in which threads will gain access. If fair is true, then access is granted to waiting threads in the order in which they requested it. If it is false, then the order is determined by the thread scheduler.

Semaphore(int permits)
Semaphore(int permits, boolean fair)
```

#### Mutex vs Semaphore
* Mutex and semaphore have the same mission: To synchronize access to some resource.
* The only difference is that an object's mutex can be acquired by only one thread at a time, while in the case of a semaphore, which uses a thread counter, several threads can access the resource simultaneously. This isn't just a coincidence :) 
* A mutex is actually a semaphore with a count of 1. In other words, it's a semaphore that can accommodate a single thread. It's also known as a "binary semaphore" because its counter can have only 2 values — 1 ("unlocked") and 0 ("locked"). 

### Mesa, Hasen and Hoare model
* Java uses Mesa model
* References:
  * http://www.cs.cornell.edu/courses/cs4410/2018su/lectures/lec09-mesa-monitors.html
  * https://pages.mtu.edu/~shene/NSF-3/e-Book/MONITOR/monitor-types.html

### JDK 1.5 Implementation with synchronized, wait and notify
#### synchronized for accessing mutual exclusive resources
* A monitor is an additional "superstructure" over a mutex. 

##### Scope of lock
* When applied on instance variable or method, lock the object. 
* When applied on a code block, lock the object. 
* When applied on static method, lock the entire class. 

##### Internals
* Java uses the synchronized keyword to express a monitor.

```java
// Original program
public class Main {

   private Object obj = new Object();

   public void doSomething() {

       // ...some logic, available for all threads

       // Logic available to just one thread at a time
       synchronized (obj) {

           /* Do important work that requires that the object
           be accessed by only one thread */
           obj.someImportantMethod();
       }
   }
}

// Converted program: Java will compile the original code above to something below. 
public class Main {

   private Object obj = new Object();

   public void doSomething() throws InterruptedException {

       // ...some logic, available for all threads

       // Logic available to just one thread at a time:

       /* as long as the object's mutex is busy,
       all the other threads (except the one that acquired it) are put to sleep */
       while (obj.getMutex().isBusy()) {
           Thread.sleep(1);
       }

       // Mark the object's mutex as busy
       obj.getMutex().isBusy() = true;

       /* Do important work that requires that the object
       be accessed by only one thread */
       obj.someImportantMethod();

       // Free the object's mutex
       obj.getMutex().isBusy() = false;
   }
}
```

##### Downsides
###### Could only perform operation on a single variable, not multiples
* Please see a counter impl based on UNSAFE: https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/Counter.md#unsafe-class-implementation

###### Could not break deadlock by releasing the lock proactively
* For synchronized keyword usage, when the thread could not get all resources, it will enter blocked state and could not do anything else. 

##### Optimization after JDK 1.6
* References: 
  * https://www.infoq.com/articles/java-threading-optimizations-p1/


###### Bias, lightweight and heavyweight lock
* Everyone knows that before JDK 1.6, synchronized was a heavyweight lock with low efficiency. So the official started in JDK 1.6, in order to reduce the performance consumption caused by obtaining and releasing locks, we optimized synchronized and introduced the concepts of biased lock and lightweight lock.
* These four states will gradually upgrade with competition. But once it is upgraded, it cannot be downgraded. But these conversions are transparent to users who use locks.
  * Bias lock: only one thread enters the critical section;
  * Lightweight lock: multiple threads enter the critical section alternately, and the execution ends quickly;
  * Heavyweight lock: Multiple threads enter the critical section at the same time.

###### Lock coarsening and elision

###### Adaptive spinning

#### Wait and notify methods for coordinating threads
* [Example code for usage](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/master/code/multithreads/ObjectMethods)

##### Use cases
###### Asynchronous programming

### JDK 1.6 Improved implementation with Lock and Condition

#### Motivation
##### Four necessary conditions for deadlock
* A deadlock is a situation where a thread is waiting for an object lock that another thread holds, and this second thread is waiting for an object lock that the first thread holds. Since each thread is waiting for the other thread to relinquish a lock, they both remain waiting forever. 

* There are four necessary conditions for deadlock to happen
  * **Mutal Exclusion**: Only one process can access a resource at a given time. (Or more accurately, there is limited access to a resource. A deadlock could also occur if a resource has limited quantity. )
  * **Hold and Wait**: Processes already holding a resource can request additional resources, without relinquishing their current resources. 
  * **No Preemption**: One process cannot forcibly remove another process' resource.
  * **Circular Wait**: Two or more processes form a circular chain where each process is waiting on another resource in the chain. 
* Reference: https://afteracademy.com/blog/what-is-deadlock-and-what-are-its-four-necessary-conditions*

##### How to avoid deadlock by breaking its conditions
* Mutual exclusion: Cann't avoid because it is the nature of the problem.
* Hold and wait: Avoid by applying all resources at once.
* No preemption: Avoid by proactively releasing its resources if not getting all necessary resources.
* Circular wait: Avoid by ordering the resources and only acquiring resources by the order.

##### Limitation of synchronized keyword
* For synchronized keyword usage, when the thread could not get all resources, it will enter blocked state and could not do anything else. 

| `Criteria`  | `synchronized`  | `ReentrantLock`  |
|---|---|---|
| Usage  | implicitly acquire/release  | explicitly acquire/release, best practice to put release inside finally  |
| Competition strategy  | Pessimistic. Will enter blocked state if failing to acquire resource  | Optimistic. Will not enter blocked state by interruption, timeout and tryLock|
| Number of conditional variable  |  Single condition varialbe  | Multiple condition variables |
| Fairness | Java's synchronized code block makes no guarantee about the sequence in which threads waiting to enter the synchronized block are allowed to enter. | Support both faiir and unfair lock. By default unfair.|

#### Lock for accessing mutual exclusive resources
* Idea: Among the four conditions to break deadlock, three of them are possible. For the no preemption condition, it could be broken by proactively releasing its resources if not getting all necessary resources. There are three ways to achieve this:
  1. Support interruption
  2. Support timeout
  3. Support trying to acquire lock without entering blocked state in failure case
* Lock provides three method for implementing this

```
//  1. Support interruption
void lockInterruptibly() throws InterruptedException;

//  2. Support timeout
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

//  3. Support trying to acquire lock without entering blocked state in failure case
boolean tryLock();
```

#### Condition for coordinating threads
* Please see a sample of using Lock + Condition to implement producing-consuming pattern: https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/BlockingQueue.md#condition-locks-impl

### References
* https://techdifferences.com/difference-between-semaphore-and-monitor-in-os.html
* https://cs.stackexchange.com/questions/43721/why-would-you-use-a-monitor-instead-of-a-semaphore
* https://codegym.cc/groups/posts/220-whats-the-difference-between-a-mutex-a-monitor-and-a-semaphore

## CAS
* sun.misc.Unsafe class
  * CompareAndSwapInt
  * CompareAndSwapLong

### Cons
#### ABA problem
* CompareAndSwap only compares the actual value, but it does not guarantee that there are no thread changing this. This means that within the 
* For example
  1. Thread 1 change i from 0 => 1
  2. Thread 1 change i from 1 => 0
  3. Thread 2 changes i from 0 => 1, originally expected to fail. However, since CSA only uses the value comparison, it won't detect such changes. 

![](./images/multithread-cas-abaproblem.png)

* Solution: Add a version number

#### Spin lock CPU consumption
* CAS is usually combined together with loop implementation. This is similar to a long-running spinlock, end up consuming lots of resource. 

* Def: If a lock is a spin lock, it means that when the lock has been occupied by a thread, another thread trying to acquire it will constantly circulating to see whether the lock has been released (constantly causing CPU cycles) insteading entering a blocking state such as sleep. 
* Internals:
  * Implementation based on CAS: https://programmer.help/blogs/java-lock-spin-lock.html
  * Usually spin lock is associated with a timeout. And this timeout threshold is usually set to typical context swap time. 
* Applicable cases: Reduce the CPU thread context swap cost because the waiting thread never enters blocked state. Applicable for cases where the lock time is relatively low, or where there isn't much lock contention so that CPU context switch time could be saved. 

### Impl
#### Atomic classes
* AtomicBoolean, AtomicInteger, AtomicLong
* AtomicIntegerArray, AtomicLongArray, AtomicReferenceArray
* AtomicIntegerFieldUpdater, AtomicLongFieldUpdater, AtomicReferenceFieldUpdater
* AtomicReference, AtomicStampedReference, AtomicMarkableReference

## AQS
### Motivation
* Many utilities such as Semahpore, CountdownLatch, etc. need to rely on a common set of methods: Lock, synchronizer, etc. 

![](./images/multithreads-aqs-subclasses.png)

### Internals
* AQS is an abstract queue synchronizer. It mains a volatile int state variable and a FIFO queue. 
* There are three methods to visit the state variable
  * getState()
  * setState()
  * compareAndSetState(): Internally relies on UnSafe compareAndSwapInt

### Create impl inheriting AQS
* AQS defines two ways to access a resource:
  * Exclusive: Concrete implementation such as ReentrantLock
  * Share: Concrete implementation such as Semaphore and CountDownLatch
* Both of these two approaches rely on a number of methods

```java
// methods
isHeldExclusively()
tryAcquire(int)
tryRelease(int)
tryAcquireShared(int)
tryReleaseShared(int)
```

## Lock
### ReentrantLock

### ReadWriteLock

### StampedLock

## Concurrency control
### Semaphore
### CountdownLatch
### CyclicBarrier


## Java Concurrent Utilities - JCU 
### List
#### CopyOnWriteArrayList
### Map
#### ConcurrentHashMap
#### ConcurrentSkiplistMap
### Set
#### CopyOnWriteArraySet
#### ConcurrentSkipListSet
### Queue
#### BlockingQueue
##### ArrayBlockingQueue
##### LinkedBlockingQueue
##### SynchronousQueue
##### LinkedTransferQueue
##### PriorityBlockingQueue
##### DelayQueue

#### BlockingDeque
#### ConcurrentLinkedQueue
#### ConcurrentLinkedDeque


* BoundedBlockingQueue
* See src dir for details


## Lock alternatives
### Thread confinement
#### ThreadLocal
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/ThreadLocal.md)

#### Stack confinement
* Stack confinement is confining a variable, or an object, to the stack of the thread. This is much stronger than Ad-hoc thread confinement, as it is limiting the scope of the object even more, by defining the state of the variable in the stack itself. For example, consider the following piece of code:

```java
private long numberOfPeopleNamedJohn(List<Person> people) 
{
  List<Person> localPeople = new ArrayList<>(); // Confined within the stack space
  localPeople.addAll(people);

  return localPeople.stream().filter(person -> person.getFirstName().equals("John")).count();
}
```
#### Adhoc confinement
* Ad-hoc thread confinement describes a way of thread confinement, where it is the total responsibility of the developer, or the group of developers working on that program, to ensure that the use of the object is restricted to a single thread. This approach is very very fragile and should be avoided in most cases.
* One special case that comes under Ad-hoc thread confinement applies to volatile variables. It is safe to perform read-modify-write operations on the shared volatile variable as long as you ensure that the volatile variable is only written from a single thread. In this case, you are confining the modification to a single thread to prevent race conditions, and the visibility guarantees for volatile variables ensure that other threads see the most up to date value.

###
#### Disruptor

### CopyOnWrite

## Reduce lock
### Flyweight pattern

## Future task
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/Future.md)

## Design small utils
### Singleton pattern
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/SingletonPattern.md)

### Blocking queue
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/BlockingQueue.md)

### Delayed scheduler
* [Link to the subpage](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/DelayedQueue.md)

### ConcurrentHashmap

### ThreadPool

## References
* [并发多线程常见面试题](https://docs.qq.com/doc/DSVNyZ2FNWWFkeFpO)