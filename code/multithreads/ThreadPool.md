
- [Thread Pool](#thread-pool)
  - [Motivation](#motivation)
  - [Types](#types)
  - [Threadpool constructor](#threadpool-constructor)
    - [Create threadpool](#create-threadpool)
      - [Notes](#notes)
    - [Recycle thread](#recycle-thread)
    - [Reject task](#reject-task)
  - [Stop threadpool](#stop-threadpool)
    - [Shutdown and ShutdownNow](#shutdown-and-shutdownnow)
    - [IsShutdown and IsTerminate](#isshutdown-and-isterminate)
  - [Number of threads](#number-of-threads)

## Thread Pool

### Motivation
* Efficiency: Thread is an object in Java. Creating / Destroying objects will all take time. If creating + destroying time > execution time, then it is not that efficient. No need to swtich between different thread contexts. 
* Better CPU and memory utilitzation: According to JVM, the maximum stack size for a thread is 1M inside system memory. Creating too many threads will occupy much space. 
* Easier management

### Types
* FixedThreadPool
  * It adopts LinkedBlockingQueue internally because there are limited number of threads which could be created.
* SingleThread
  * It adopts LinkedBlockingQueue internally because there are limited number of threads which could be created.
* CachedThreadPool
  * It uses SynchronousQueue internally because there is no limit on the maxPoolSize
* ScheduledThreadPool
  * It uses DelayedQueue
* WorkStealingPool
  * After JDK 1.8

![](./images/../../../images/multithread-threadpool-parameters.png)



### Threadpool constructor

```
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) 
```

#### Create threadpool

![](./images/../../../images/multithread-threadpool-flowchart.png)

![](./images/../../../images/multithread-threadpool-poolsize.png)

* corePoolSize: 
  - The number of core threads in the thread pool will not be recycled, even if there is no task execution, it will remain idle. If the number of threads in the thread pool is less than this, it is created when the task is executed.

* maximumPoolSize
  - The maximum number of threads allowed in the pool. When the number of threads reaches corepoolsize and the workqueue queue is full of tasks, continue to create threads.

* WorkQueue
  - When the current number of threads exceeds the corepoolsize, the new task will be in the waiting state and exist in the workqueue. There are several different types of queue:
    - SynchronousQueue: Size == 0
    - LinkedBlockingQueue: Unbounded
    - ArrayBlockingQueue: Bounded

* threadFactory
    - To create a factory class of a thread, we usually set the name of the thread from the top of a threadfactory, so that we can know which factory class the thread is created by and quickly locate it.

##### Notes
* If corePoolSize is same as the maximumPoolSize, then the size of threadpool will always be fixed. 
* Only when work queue is full, additional thread will be created out of CorePoolSize. If the worker queue is an unbounded queue, then no the pool size will not exceed corePoolSize.

#### Recycle thread
 * keepAliveTime
  - The lifetime of the temporary thread after corepoolsize is exceeded.

#### Reject task
* handler
    - When the number of lines reaches the maximum poolsize and the workqueue is full of tasks, the thread pool will call the handler rejection policy to process the request.

* The default rejection policies are as follows:
    - Abortpolicy: the default rejection policy of thread pool, which directly throws exception handling.
    - Discardpolicy: discard directly without processing.
    - Discardoldestpolicy: discards the oldest task in the queue.
    - Callerrunspolicy: assign the task to the current execute method thread to process.

We can also customize the rejection policy by implementing the rejectedexecutionhandler interface. The friendly rejection policy implementation is as follows:

Save the data to the data and process it when the system is idle
Record the data with log, and then handle it manually


### Stop threadpool
#### Shutdown and ShutdownNow
* Shutdown: Will shutdown gracefully
* ShutdownNow: Shutdown forcefully

```java
public class ShutDown {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executorService.execute(new ShutDownTask());
        }
        Thread.sleep(1500);

        executorService.shutdown();
        executorService.execute(new ShutDownTask());
    }
}

class ShutDownTask implements Runnable {


    @Override
    public void run() {
        try {
            Thread.sleep(500);
            System.out.println(Thread.currentThread().getName());
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + "被中断了");
        }
    }
}

```

#### IsShutdown and IsTerminate
* IsShutdown: Decide whether already inside shutdown state.
* IsTerminated: Whether every shutdown operation has been finished. 


### Number of threads

```
Number of threads 
= number of CPU cores * (1 + Average waiting time / Average working time)
```

* If CPU intensive - set the number of threads to 1-2 * the number of CPU cores
* If IO intensive (Network, file, database, etc
  
