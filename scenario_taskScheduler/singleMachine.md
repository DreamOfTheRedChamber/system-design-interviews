
- [Single machine](#single-machine)
  - [PriorityQueue](#priorityqueue)
  - [Timing wheel](#timing-wheel)

# Single machine

## PriorityQueue

**DelayQueue implementation in JDK**

* Internal structure: DelayQueue is a specialized PriorityQueue that orders elements based on their delay time.
* Characteristics: When the consumer wants to take an element from the queue, they can take it only when the delay for that particular element has expired.
* Pros:
  * Not introduce other dependencies 
* Cons: 
  * It is only a data structure implementation and all queue elements will be stored within JVM memory. It would require large amounts of efforts to build a scalable delay queue implementation on top of it. 

**Delayed interface**

* Algorithm: When the consumer tries to take an element from the queue, the DelayQueue will execute getDelay() to find out if that element is allowed to be returned from the queue. If the getDelay() method will return zero or a negative number, it means that it could be retrieved from the queue.
* Data structure:

```
public class DelayQueue<E extends Delayed>
                    extends AbstractQueue<E>
                    implements BlockingQueue<E>
```

```
// Each element we want to put into the DelayQueue needs to implement the Delayed interface
public class DelayObject implements Delayed {
    private String data;
    private long startTime;

    public DelayObject(String data, long delayInMilliseconds) {
        this.data = data;
        this.startTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    // It will return the remaining delay associated with the item in the top of the PriorityQueue in the given time unit. 
    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    // The elements in the DelayQueue will be sorted according to the expiration time. The item that will expire first is kept at the head of the queue and the element with the highest expiration time is kept at the tail of the queue:
    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(
          this.startTime - ((DelayObject) o).startTime);
    }
}
```

**Test with Producer/Consumer pattern**

```
// DelayedQueue is a blocking queue. When delayedQueue.take() method is called, it will only return when there is an item to be returned. 
public class DelayQueueProducer implements Runnable 
{  
    private BlockingQueue<DelayObject> queue;
    private Integer numberOfElementsToProduce;
    private Integer delayOfEachProducedMessageMilliseconds;

    // standard constructor

    @Override
    public void run() 
    {
        for (int i = 0; i < numberOfElementsToProduce; i++) 
        {
            DelayObject object
              = new DelayObject(
                UUID.randomUUID().toString(), delayOfEachProducedMessageMilliseconds);
            System.out.println("Put object: " + object);
            try 
            {
                queue.put(object);
                Thread.sleep(500);
            } 
            catch (InterruptedException ie) 
            {
                ie.printStackTrace();
            }
        }
    }
}

public class DelayQueueConsumer implements Runnable 
{
    private BlockingQueue<DelayObject> queue;
    private Integer numberOfElementsToTake;
    public AtomicInteger numberOfConsumedElements = new AtomicInteger();

    // standard constructors

    @Override
    public void run() {
        for (int i = 0; i < numberOfElementsToTake; i++) 
        {
            try 
            {
                DelayObject object = queue.take();
                numberOfConsumedElements.incrementAndGet();
                System.out.println("Consumer take: " + object);
            } 
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }
}
```

**Reference**

* [https://www.baeldung.com/java-delay-queue](https://www.baeldung.com/java-delay-queue)

## Timing wheel

**Simple wheel**

* Keep a large timing wheel
* A curser in the timing wheel moves one location every time unit (just like a seconds hand in the clock)
* If the timer interval is within a rotation from the current curser position then put the timer in the corresponding location
* Requires exponential amount of memory

**Hashed wheel (sorted)**

* Sorted Lists in each bucket
* The list in each bucket can be insertion sorted
* Hence START_TIMER takes O(n) time in the worst case
* If  n < WheelSize then average O(1)

**Hashed wheel (unsorted)**

* Unsorted list in each bucket
* List can be kept unsorted to avoid worst case O(n) latency for START_TIMER
* However worst case PER_TICK_BOOKKEEPING = O(n)
* Again, if n < WheelSize then average O(1)

**Hierarchical wheels**

* START_TIMER = O(m) where m is the number of wheels. The bucket value on each wheel needs to be calculated
* STOP_TIMER = O(1)
* PER_TICK_BOOKKEEPING = O(1)  on avg.

**Reference**

* A hashed timer implementation [https://github.com/ifesdjeen/hashed-wheel-timer](https://github.com/ifesdjeen/hashed-wheel-timer)
* [http://www.cloudwall.io/hashed-wheel-timers](http://www.cloudwall.io/hashed-wheel-timers)
* Implementation in Netty: [https://www.jianshu.com/p/f009666ef55c](https://www.jianshu.com/p/f009666ef55c)