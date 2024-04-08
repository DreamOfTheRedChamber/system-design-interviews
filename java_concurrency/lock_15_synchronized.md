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
    - [Fundation for asynchronous programming - Future task](#fundation-for-asynchronous-programming---future-task)


# JDK 1.5 Implementation with synchronized, wait and notify
## synchronized for accessing mutual exclusive resources

* A monitor is an additional "superstructure" over a mutex.

## Scope of lock

* When applied on instance variable or method, lock the object.
* When applied on a code block, lock the object.
* When applied on static method, lock the entire class.

### Internals

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

## Downsides

### Could only perform operation on a single variable, not multiples

* Please see a counter impl based on UNSAFE: [https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/code/multithreads/Counter.md#unsafe-class-implementation](code/multithreads/Counter.md#unsafe-class-implementation)

### Could not break deadlock by releasing the lock proactively

* For synchronized keyword usage, when the thread could not get all resources, it will enter blocked state and could not do anything else.

### Optimization after JDK 1.6

* References:
  * [https://www.infoq.com/articles/java-threading-optimizations-p1/](https://www.infoq.com/articles/java-threading-optimizations-p1/)

### Bias, lightweight and heavyweight lock
* Everyone knows that before JDK 1.6, synchronized was a heavyweight lock with low efficiency. So the official started in JDK 1.6, in order to reduce the performance consumption caused by obtaining and releasing locks, we optimized synchronized and introduced the concepts of biased lock and lightweight lock.
* These four states will gradually upgrade with competition. But once it is upgraded, it cannot be downgraded. But these conversions are transparent to users who use locks.
  * Bias lock: only one thread enters the critical section;
  * Lightweight lock: multiple threads enter the critical section alternately, and the execution ends quickly;
  * Heavyweight lock: Multiple threads enter the critical section at the same time.

### Lock coarsening and elision

### Adaptive spinning

### Wait and notify methods for coordinating threads

* [Example code for usage](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/master/code/multithreads/ObjectMethods)

### Fundation for asynchronous programming - Future task

* [Link to the subpage](code/multithreads/Future.md)