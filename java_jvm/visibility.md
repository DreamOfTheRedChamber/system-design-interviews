- [Happens-Before relationship](#happens-before-relationship)
  - [Solution with lock (intrinsic or explicit)](#solution-with-lock-intrinsic-or-explicit)
- [Visibility](#visibility)
  - [Volatile keyword](#volatile-keyword)
  - [Final keyword](#final-keyword)

# Happens-Before relationship

* Def: Happens-before relationship is a guarantee that action performed by one thread is visible to another action in different thread.
* Why it matters to reordering: Happens-before defines a partial ordering on all actions within the program. To guarantee that the thread executing action Y can see the results of action X (whether or not X and Y occur in different threads), there must be a happens-before relationship between X and Y. In the absence of a happens-before ordering between two operations, the JVM is free to reorder them as it wants (JIT compiler optimization).
* Sample happens-before relationship:
  * Single thread rule: Each action in a single thread happens-before every action in that thread that comes later in the program order.
  * Monitor lock rule: An unlock on a monitor lock (exiting synchronized method/block) happens-before every subsequent acquiring on the same monitor lock.
  * Volatile variable rule: A write to a volatile field happens-before every subsequent read of that same field. Writes and reads of volatile fields have similar memory consistency effects as entering and exiting monitors (synchronized block around reads and writes), but without actually aquiring monitors/locks.
  * Thread start rule: A call to Thread.start() on a thread happens-before every action in the started thread. Say thread A spawns a new thread B by calling threadA.start(). All actions performed in thread B's run method will see thread A's calling threadA.start() method and before that (only in thread A) happened before them.
  * Thread join rule: All actions in a thread happen-before any other thread successfully returns from a join on that thread. Say thread A spawns a new thread B by calling threadA.start() then calls threadA.join(). Thread A will wait at join() call until thread B's run method finishes. After join method returns, all subsequent actions in thread A will see all actions performed in thread B's run method happened before them.
  * Transitivity: If A happens-before B, and B happens-before C, then A happens-before C.
* References:
  * [https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/happens-before.html](https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/happens-before.html)

## Solution with lock (intrinsic or explicit)

* The reasons of reordering problems may vary. It might be delayed write (due to any reasons, including how low level OS mechanism handles threads) to main memory which makes the code appears to be reordered or might be because of real code ordering as a results of JIT compiler/processor code optimization.
* Java Memory Model doesn't require a programmer to figure out the real low level reasons because those reasons vary on different JIT compilers and on different machine architectures. It requires a programmer to recognize the situations where reordering might happen and do proper synchronization.
* Reference: [https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/thread-reordering.html](https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/thread-reordering.html)

# Visibility

## Volatile keyword

* Using volatile forces all accesses (read or write) to occur to the main memory, effectively not caching volatile in CPU. This can be useful for the actions where visibility of the variable is important and order of accesses is not important.
* More specifically, In case of volatile reference object, it is ensured that the reference itself will be visible to other threads in timely manner but the same is not true for its member variables. There is no guarantee that data contained within the object will be visible consistently if accessed individually.

## Final keyword

* Use cases
  * Forbids the overriding of classes and methods, as well as changes a variable that has been already initialized
  * Guarantees visibility in a multi-threaded application
  * Safe initialization for objects, arrays, and collections
* References:
  * [https://dzone.com/articles/final-keyword-and-jvm-memory-impact](https://dzone.com/articles/final-keyword-and-jvm-memory-impact)
