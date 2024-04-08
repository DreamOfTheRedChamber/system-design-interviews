- [JDK 1.6 Improved implementation with Lock and Condition](#jdk-16-improved-implementation-with-lock-and-condition)
  - [Limitation of synchronized keyword](#limitation-of-synchronized-keyword)
  - [Lock for accessing mutual exclusive resources](#lock-for-accessing-mutual-exclusive-resources)
  - [Lock implementations](#lock-implementations)
    - [ReentrantLock](#reentrantlock)
    - [ReadWriteLock](#readwritelock)
    - [StampedLock](#stampedlock)
    - [Condition for coordinating threads](#condition-for-coordinating-threads)
    - [Example usage with producer consumer pattern](#example-usage-with-producer-consumer-pattern)
      - [References](#references)


# JDK 1.6 Improved implementation with Lock and Condition

## Limitation of synchronized keyword

* For synchronized keyword usage, when the thread could not get all resources, it will enter blocked state and could not do anything else.

|                                |                                                                                                                                                     |                                                                               |
| ------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| `Criteria`                     | `synchronized (intrinsic lock)`                                                                                                                     | `ReentrantLock`                                                               |
| Usage                          | implicitly acquire/release                                                                                                                          | explicitly acquire/release, best practice to put release inside finally       |
| Competition strategy           | Pessimistic. Will enter blocked state if failing to acquire resource                                                                                | Optimistic. Will not enter blocked state by interruption, timeout and tryLock |
| Number of conditional variable | Single condition varialbe                                                                                                                           | Multiple condition variables                                                  |
| Fairness                       | Java's synchronized code block makes no guarantee about the sequence in which threads waiting to enter the synchronized block are allowed to enter. | Support both faiir and unfair lock. By default unfair.                        |

## Lock for accessing mutual exclusive resources

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

## Lock implementations

### ReentrantLock
### ReadWriteLock

* Requirements:
  * Allow multiple threads to read shared variables together.
  * Allow a single thread to write shared variable.
  * When a write operation is going on, no read operations will be required.
* It does not support lock upgrade: if you get a read lock first, you could not get a write lock without releasing the read lock.
* It supports lock downgrade: if you get a write lock first, you could get a read lock implicitly.

### StampedLock

* Support three types of lock
  * Write / pessimstic read
    * Share same semantics with ReadWriteLock
    * Difference: Needs to pass an additional parameter "stamp"
  * Optimistic read
    * For ReadWriteLock, when multiple threads are reading, no write operation is allowed at all; For StampedLock, when multiple threads are reading, a single thread is allowed to write.
* StampedLock is a subclass of ReadWriteLock, and ReentrantReadWriteLock is also a subclass of ReadWriteLock. StampedLock is a non-reentrant lock.
* It is suitable for the situation of more reading and less writing. If it is not the case, please use it with caution, the performance may not be as good as synchronized.
* The pessimistic read lock and write lock of StampedLock do not support condition variables.
* Never interrupt a blocked pessimistic read lock or write lock. If you call interrupt() of a blocked thread, it will cause the cpu to soar. If you want StampedLock to support interrupt operations, please use readLockInterruptibly( Pessimistic read lock) and writeLockInterruptibly (write lock).

### Condition for coordinating threads

* Condition's await(), signal() and signalAll() are the same as wait()、notify()、notifyAll() from functional perspective.

```java
class TaskQueue {
    private final Lock lock = new ReentrantLock();

    // Condition instance needs to be obtained from lock
    private final Condition condition = lock.newCondition();
    private Queue<String> queue = new LinkedList<>();

    public void addTask(String s) {
        lock.lock();
        try {
            queue.add(s);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public String getTask() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                condition.await();
            }
            return queue.remove();
        } finally {
            lock.unlock();
        }
    }
}
```

### Example usage with producer consumer pattern

* Please see a sample of using Lock + Condition to implement producing-consuming pattern: [https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/BlockingQueue.md#condition-locks-impl](code/multithreads/BlockingQueue.md#condition-locks-impl)

#### References

* [https://techdifferences.com/difference-between-semaphore-and-monitor-in-os.html](https://techdifferences.com/difference-between-semaphore-and-monitor-in-os.html)
* [https://cs.stackexchange.com/questions/43721/why-would-you-use-a-monitor-instead-of-a-semaphore](https://cs.stackexchange.com/questions/43721/why-would-you-use-a-monitor-instead-of-a-semaphore)
* [https://codegym.cc/groups/posts/220-whats-the-difference-between-a-mutex-a-monitor-and-a-semaphore](https://codegym.cc/groups/posts/220-whats-the-difference-between-a-mutex-a-monitor-and-a-semaphore)
