- [Monitor](#monitor)
  - [Def](#def)
  - [Relationship with Mutex and semaphore](#relationship-with-mutex-and-semaphore)
    - [Mutex](#mutex)
    - [Semaphore](#semaphore)
    - [Mutex vs Semaphore](#mutex-vs-semaphore)
  - [Mesa, Hasen and Hoare model](#mesa-hasen-and-hoare-model)
- [Counter Sdks](#counter-sdks)
  - [Semaphore](#semaphore-1)
  - [CountdownLatch and CyclicBarrier](#countdownlatch-and-cyclicbarrier)
- [Lock alternatives](#lock-alternatives)
  - [Thread confinement](#thread-confinement)
    - [ThreadLocal](#threadlocal)
    - [Stack confinement](#stack-confinement)
    - [Adhoc confinement](#adhoc-confinement)
  - [Disruptor](#disruptor)
  - [Flyweight pattern](#flyweight-pattern)
- [Design small utils](#design-small-utils)
  - [Singleton pattern](#singleton-pattern)
  - [Blocking queue](#blocking-queue)
  - [Delayed scheduler](#delayed-scheduler)
  - [ConcurrentHashmap](#concurrenthashmap)
- [References](#references)

# Monitor

## Def

* Monitor in Java is not a special object. It's synchronization mechanism placed at class hierarchy root: java.lang.Object. This synchronization mechanism manages how to operate on shared variables.
* There are many methods on the Object class including wait(), notify() and their siblings e.g. notifyAll().
* References: [http://pages.cs.wisc.edu/\~sschang/OS-Qual/process/Mesa\_monitor.htm](http://pages.cs.wisc.edu/\~sschang/OS-Qual/process/Mesa\_monitor.htm)

## Relationship with Mutex and semaphore

### Mutex

* A mutex is attached to every object in Java.
* Within a mutex, only two states are available: unlocked and locked.
* Java has no mechanism that would let you set the mutex value directly, something similar to below

```java
Object myObject = new Object();
Mutex mutex = myObject.getMutex();
mutex.free();
```

### Semaphore

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

### Mutex vs Semaphore

* Mutex and semaphore have the same mission: To synchronize access to some resource.
* The only difference is that an object's mutex can be acquired by only one thread at a time, while in the case of a semaphore, which uses a thread counter, several threads can access the resource simultaneously. This isn't just a coincidence :)
* A mutex is actually a semaphore with a count of 1. In other words, it's a semaphore that can accommodate a single thread. It's also known as a "binary semaphore" because its counter can have only 2 values — 1 ("unlocked") and 0 ("locked").

## Mesa, Hasen and Hoare model

* Java uses Mesa model
* References:
  * [http://www.cs.cornell.edu/courses/cs4410/2018su/lectures/lec09-mesa-monitors.html](http://www.cs.cornell.edu/courses/cs4410/2018su/lectures/lec09-mesa-monitors.html)
  * [https://pages.mtu.edu/\~shene/NSF-3/e-Book/MONITOR/monitor-types.html](https://pages.mtu.edu/\~shene/NSF-3/e-Book/MONITOR/monitor-types.html)



# Counter Sdks

## Semaphore

* One counter, one waiting queue and three methods
  * init()
  * down()
  * up()

```java
class Semaphore
{
  // counter
  int count;
  // waiting queue
  Queue queue;

  // constructor
  Semaphore(int c)
  {
    this.count=c;
  }

  void down()
  {
    this.count--;
    if(this.count<0)
    {
      // put current thread into queue
      // put current thread into blocked state
    }
  }

  void up()
  {
    this.count++;
    if(this.count<=0) 
    {
        // dequeue a thread in the waiting queue 
        // wait up the thread from blocked state
    }
  }
}
```

## CountdownLatch and CyclicBarrier

|            |                                                                                             |                                                                                                                                                                        |
| ---------- | ------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Criteria` | `CountdownLatch`                                                                            | `CyclicBarrier`                                                                                                                                                        |
| Goal       | CountDownLatch keeps up a count of tasks                                                    | CyclicBarrier keeps up a count of threads                                                                                                                              |
| Reuse      | CountDownLatch cannot be reused, when count arrives at zero it can’t be reset               | CyclicBarrier can be reused after holding threads are released                                                                                                         |
| Exception  | In CountDownLatch just the current thread that has an issue throws a special case/exception | In a CyclicBarrier, if a thread experiences an issue (timeout, interruption), the wide range of various threads that have reached await() get a special case/exception |


# Lock alternatives

## Thread confinement

### ThreadLocal

* [Link to the subpage](code/multithreads/ThreadLocal.md)

### Stack confinement

* Stack confinement is confining a variable, or an object, to the stack of the thread. This is much stronger than Ad-hoc thread confinement, as it is limiting the scope of the object even more, by defining the state of the variable in the stack itself. For example, consider the following piece of code:

```java
private long numberOfPeopleNamedJohn(List<Person> people) 
{
  List<Person> localPeople = new ArrayList<>(); // Confined within the stack space
  localPeople.addAll(people);

  return localPeople.stream().filter(person -> person.getFirstName().equals("John")).count();
}
```

### Adhoc confinement

* Ad-hoc thread confinement describes a way of thread confinement, where it is the total responsibility of the developer, or the group of developers working on that program, to ensure that the use of the object is restricted to a single thread. This approach is very very fragile and should be avoided in most cases.
* One special case that comes under Ad-hoc thread confinement applies to volatile variables. It is safe to perform read-modify-write operations on the shared volatile variable as long as you ensure that the volatile variable is only written from a single thread. In this case, you are confining the modification to a single thread to prevent race conditions, and the visibility guarantees for volatile variables ensure that other threads see the most up to date value.

## Disruptor

* No contention = no locks = it's very fast.
* Having everything track its own sequence number allows multiple producers and multiple consumers to use the same data structure.
* Tracking sequence numbers at each individual place (ring buffer, claim strategy, producers and consumers), plus the magic cache line padding, means no false sharing and no unexpected contention.

## Flyweight pattern

# Design small utils

## Singleton pattern

* [Link to the subpage](../code/multithreads/SingletonPattern.md)

## Blocking queue

* [Link to the subpage](../code/multithreads/BlockingQueue.md)

## Delayed scheduler

* [Link to the subpage](../code/multithreads/DelayedQueue.md)

## ConcurrentHashmap

* [Link to the subpage](../code/multithreads/ConcurrentHashmap.md)

# References

* [并发多线程常见面试题](https://docs.qq.com/doc/DSVNyZ2FNWWFkeFpO)
