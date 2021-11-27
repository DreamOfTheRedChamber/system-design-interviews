
- [Naive implementation](#naive-implementation)
- [A more extensible architecture according to scrapy](#a-more-extensible-architecture-according-to-scrapy)
- [Appendix](#appendix)
  - [Multithread programs for standalone crawler](#multithread-programs-for-standalone-crawler)
    - [Problematic impl with lock](#problematic-impl-with-lock)
    - [First workable solution with Condition](#first-workable-solution-with-condition)
    - [Threadsafe queue](#threadsafe-queue)

# Naive implementation

```
// pseudo code
function run
    while ( UrlPrioritizerQueue not empty )
        url = UrlPrioritizerQueue.dequeue()
        html = DownloadWorker.download(url)
        extractedNewUrls, extractedItems = urlExtractor.extract(html)
        UrlPrioritizerQueue.enqueue_all(extracted_new_urls)
    end
```

```
                                                                       ┌────────────────┐     
                                                                       │                │     
                                                                       │    Storage     │     
                                                                       │                │     
                                                                       │                │     
                                                                       └────────────────┘     
                                                                                ▲             
                                                                                │             
                                                                                │             
                                                                        Save extracted items  
                                                                                │             
                                                                                │             
┌───────────────────┐           ┌───────────────────┐                 ┌───────────────────┐   
│                   │           │                   │                 │                   │   
│  Url prioritizer  │           │ Downloader worker │                 │ Extractor worker  │   
│       queue       │──────────▶│                   │────────────────▶│                   │   
│                   │           │                   │                 │                   │   
└───────────────────┘           └───────────────────┘                 └───────────────────┘   
          ▲                                                                     │             
          │                                                                     │             
          └────────────────────────Pass extracted new urls──────────────────────┘
```

# A more extensible architecture according to scrapy

* Scrapy: [https://docs.scrapy.org/en/latest/topics/architecture.html](https://docs.scrapy.org/en/latest/topics/architecture.html)
* Middleware:
  * Download middleware: [https://docs.scrapy.org/en/latest/topics/downloader-middleware.html#topics-downloader-middleware](https://docs.scrapy.org/en/latest/topics/downloader-middleware.html#topics-downloader-middleware)
  * Extractor middleware: [https://docs.scrapy.org/en/latest/topics/spider-middleware.html#topics-spider-middleware](https://docs.scrapy.org/en/latest/topics/spider-middleware.html#topics-spider-middleware)

```
                                                    ┌──────────────────────┐                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │      Extractor       │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    └────┬────────────▲────┘                                                                         
                                                         │            │                                                                              
                                                         │            │                                                                              
                                               Step5. Extract       Step4. Send html response to                                                     
                                                  1) items            │       extractor                                                              
                                                 2) new Urls          │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                 ┌ ─ ─ ─ ┴ ─ ─ ─ ─ ─ ─│─ ─ ─ ─                                                                       
                                                      Extractor Middleware    │                                                                      
                                                 │  (aka spider middleware)                                                                          
                                                                              │                                                                      
                                                 └ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─│─ ─ ─ ─                                                                       
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
┌──────────────────────┐                             ┌───▼────────────┴─────┐  ┌ ─ ─ ─ ─ ─                                   ┌──────────────────────┐
│                      │                             │                      │             │                                  │                      │
│                      │        Step6. Save items to │                      │  │             Step3. Download the entire      │                      │
│                      ◀───────────────storage───────┤                      ◀───          ├──────html page response──────────┤                      │
│       Storage        │                             │        Engine        │  │Downloader                                   │      Downloader      │
│                      │                             │                      │   Middleware│                                  │                      │
│                      │                             │                      │  │                                             │                      │
│                      │                             │                      ├───          ├─────Step2. Send Url to ──────────▶                      │
│                      │                             │                      │  │                    downloader               │                      │
└──────────────────────┘                             └────┬──────────▲──────┘             │                                  └──────────────────────┘
                                                          │          │         └ ─ ─ ─ ─ ─                                                           
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                               Step7. Pass new       │                                                                               
                                              Urls to scheduler     Step1. Get url from scheduler                                                    
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                      ┌───▼──────────┴──────┐                                                                        
                                                      │                     │                                                                        
                                                      │      Scheduler      │                                                                        
                                                      │                     │                                                                        
                                                      │                     │                                                                        
                                                      └─────────────────────┘
```



# Appendix

## Multithread programs for standalone crawler

* [Producer-consumer implementation in Python](http://agiliq.com/blog/2013/10/producer-consumer-problem-in-python/)
* Different coordination mechanisms in multithreads:
  * sleep: Stop a random interval and come back to see whether the resource is available to use. 
  * condition variable: As soon as the resource is released by other threads, you could get it immediately.
  * semaphore: Allowing multiple number of threads to occupy a resource simultaneously. Number of semaphore set to 1. 
* Note: More threads doesn't necessarily mean more performance. The number of threads on a single machine is limited because:
  * Context switch cost ( CPU number limitation )
  * Thread number limitation
    * TCP/IP limitation on number of threads
  * Network bottleneck for single machine

### Problematic impl with lock

* Problems of this implementation: 
  * Consumers could not identify queue empty state and continue running. 
* Correct behavior: 
  * When there was nothing in the queue, consumer should have stopped running and waited instead of continuing consuming from the queue. 
  * And once producer adds something to the queue, there should be a way for it to notify the consumer telling it has added something to queue. 

```python
from threading import Thread, Lock
import time
import random

queue = []
lock = Lock()

# Producer keeps on adding to the queue 
class ProducerThread(Thread):
    def run(self):
        nums = range(5) #Will create the list [0, 1, 2, 3, 4]
        global queue
        while True:
            num = random.choice(nums) #Selects a random number from list [0, 1, 2, 3, 4]

            # queue is kept inside lock to avoid race condition
            lock.acquire()
            queue.append(num)
            print "Produced", num 
            lock.release()

            time.sleep(random.random())

# Consumer keeps on removing from the queue
class ConsumerThread(Thread):
    def run(self):
        global queue
        while True:

            # queue is kept inside lock to avoid race condition
            lock.acquire()
            if not queue:
                print "Nothing in queue, but consumer will try to consume"
            num = queue.pop(0)
            print "Consumed", num 
            lock.release()

            time.sleep(random.random())

# start one producer thread and one consumer thread
ProducerThread().start()
ConsumerThread().start()
```

### First workable solution with Condition

* Use case of condition: Condition object allows one or more threads to wait until notified by another thread. 
  * Consumer should wait when the queue is empty and resume only when it gets notified by the producer. 
  * Producer should notify only after it adds something to the queue. 
* Internal mechanism of condition: Condition uses a lock internally
  * A condition has acquire() and release() methods that call the corresponding methods of the associated lock internally. 
  * Consumer needs to wait using a condition instance and producer needs to notify the consumer using the same condition instance.

```python
from threading import Condition

condition = Condition()
queue = []

class ConsumerThread(Thread):
    def run(self):
        global queue
        while True:
            condition.acquire()

            # Check if the queue is empty before consuming. If yes then call wait() on condition instance. 
            # wait() blocks the consumer and also releases the lock associated with the condition. This lock was held by consumer, so basically consumer loses hold of the lock.
            # Now unless consumer is notified, it will not run.
            if not queue:
                print "Nothing in queue, consumer is waiting"
                condition.wait()
                print "Producer added something to queue and notified the consumer"
            num = queue.pop(0)
            print "Consumed", num 
            condition.release()
            time.sleep(random.random())

class ProducerThread(Thread):
    def run(self):
        nums = range(5)
        global queue
        while True:
            # Producer can acquire the lock because lock was released by consumer
            condition.acquire()

            # Producer puts data in queue and calls notify() on the condition instance.
            num = random.choice(nums)
            queue.append(num)
            print "Produced", num 

            # Once notify() call is made on condition, consumer wakes up. But waking up doesn't mean it starts executing. notify() does not release the lock. Even after notify(), lock is still held by producer.
            condition.notify()

            # Producer explicitly releases the lock by using condition.release().
            condition.release()

            # And consumer starts running again. Now it will find data in queue and no IndexError will be raised.
            time.sleep(random.random())
```

### Threadsafe queue

* Queue encapsulates the behaviour of Condition, wait(), notify(), acquire() etc.

```python
from threading import Thread
import time
import random
from Queue import Queue

queue = Queue(10)

class ProducerThread(Thread):
    def run(self):
        nums = range(5)
        global queue
        while True:
            num = random.choice(nums)
            # Producer uses put available on queue to insert data in the queue.
            # put() has the logic to acquire the lock before inserting data in queue.
            # Also put() checks whether the queue is full. If yes, then it calls wait() internally and so producer starts waiting.
            queue.put(num)
            print "Produced", num
            time.sleep(random.random())


class ConsumerThread(Thread):
    def run(self):
        global queue
        while True:
            # Consumer uses get.
            # get() acquires the lock before removing data from queue.
            # get() checks if the queue is empty. If yes, it puts consumer in waiting state.
            # get() and put() has proper logic for notify() too. Why don't you check the source code for Queue now?
            num = queue.get()
            queue.task_done()
            print "Consumed", num
            time.sleep(random.random())


ProducerThread().start()
ConsumerThread().start()
```
