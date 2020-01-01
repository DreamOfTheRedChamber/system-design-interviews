<!-- MarkdownTOC -->

- [Message Queue](#message-queue)
	- [RocketMQ](#rocketmq)
		- [Definition](#definition)
		- [Time series data](#time-series-data)
		- [Storage model](#storage-model)
- [Delay message queue](#delay-message-queue)
	- [Use cases](#use-cases)
	- [Timer + Database](#timer--database)
	- [DelayQueue](#delayqueue)
		- [Delayed interface](#delayed-interface)
		- [Test with Producer/Consumer pattern](#test-with-producerconsumer-pattern)
		- [Reference](#reference)
	- [HashedWheelTimer](#hashedwheeltimer)
		- [Interface](#interface)
		- [Data structure](#data-structure)
			- [Simple wheel](#simple-wheel)
			- [Hashed wheel \(sorted\)](#hashed-wheel-sorted)
			- [Hashed wheel \(unsorted\)](#hashed-wheel-unsorted)
			- [Hierarchical wheels](#hierarchical-wheels)
			- [Reference](#reference-1)
	- [Redis + MySQL](#redis--mysql)
		- [Beanstalk](#beanstalk)
	- [Revise MQ](#revise-mq)

<!-- /MarkdownTOC -->


# Message Queue
## RocketMQ
### Definition
* A broker contains a master node and a slave node
	- Broker 1 has topic 1 to 5
	- Broker 2 has topic 6 to 10
* NameNode cluster contains the mapping from Topic=>Broker

* Scenario
	1. Consumer group tells name node cluster which topic it subscribe to
	2. Broker pulls from name node cluster about the heartbeat message (whether I am alive / topic mapping on the broker)
	3. Producer group pushes events to the broker
	4. Broker push events to consumer group

### Time series data
* For time series data, RocketMQ must be configured in a standalone mode. There is no HA solution available.
	- Broker 1 and 2 all have the same topic. 
	- Consumer is talking to broker 1. Broker 1 has Message 1-10. Broker 2 has message 1-9.
	- When broker 1 dies, if switched to broker 2 then message 10 will be lost. 
	- It works in non-time series scenarios but not in time-series scenarios. 
* RocketMQ high availability ??? To be read: 
	1. RocketMQ architecture https://rocketmq.apache.org/docs/rmq-arc/
	2. RocketMQ deployment https://rocketmq.apache.org/docs/rmq-deployment/
	3. RocketMQ high availability http://www.iocoder.cn/RocketMQ/high-availability/

### Storage model
* Each consumer consumes an index list
* IndexList
	- Each index contains
		+ OffSet
		+ Size
		+ TagsCode: checksum
* MessageBodyList

# Delay message queue
## Use cases
* In payment system, if a user has not paid within 30 minutes after ordering. Then this order should be expired and the inventory needs to be reset. 
* A user scheduled a smart device to perform a specific task at a certain time. When the time comes, the instruction will be pushed to the user's device from the server. 
* Control packet lifetime in networks

## Timer + Database
* Initial solution: Creates a table within a database, uses a timer thread to scan the table periodically. 
	- Cons: If the volume of data is large and there is a high frequency of insertion rate, then it won't be efficient to lookup and update records. 
* How to optimize: 
	- Shard the table according to task id to boost the lookup efficiency. 

```
INT taskId
TIME expired
```

## DelayQueue
* Def: DelayQueue is a specialized PriorityQueue that orders elements based on their delay time.
* Characteristics: When the consumer wants to take an element from the queue, they can take it only when the delay for that particular element has expired.
 
### Delayed interface
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

### Test with Producer/Consumer pattern

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

### Reference
* https://www.baeldung.com/java-delay-queue

## HashedWheelTimer
### Interface

### Data structure
#### Simple wheel
* Keep a large timing wheel
* A curser in the timing wheel moves one location every time unit (just like a seconds hand in the clock)
* If the timer interval is within a rotation from the current curser position then put the timer in the corresponding location
* Requires exponential amount of memory

#### Hashed wheel (sorted)
* Sorted Lists in each bucket
* The list in each bucket can be insertion sorted
* Hence START_TIMER takes O(n) time in the worst case
* If  n < WheelSize then average O(1)

#### Hashed wheel (unsorted)
* Unsorted list in each bucket
* List can be kept unsorted to avoid worst case O(n) latency for START_TIMER
* However worst case PER_TICK_BOOKKEEPING = O(n)
* Again, if n < WheelSize then average O(1)

#### Hierarchical wheels
* START_TIMER = O(m) where m is the number of wheels. The bucket value on each wheel needs to be calculated
* STOP_TIMER = O(1)
* PER_TICK_BOOKKEEPING = O(1)  on avg.

#### Reference
* A hashed timer implementation https://github.com/ifesdjeen/hashed-wheel-timer
* http://www.cloudwall.io/hashed-wheel-timers

## Redis + MySQL

* MySQL: stores the message content
* Redis stores sorted timestamp set
	- Delay queue: 20 bucket. Each bucket is a sorted set. 
		+ Ways to implement timer: Infinite loop
		+ Ways to implement timer: Wait/Notify mechanism
	- Ready queue: 
* A server provides 
	- Server scans the 20 bucket and put the message expired to ready queue based on timer
		+ There needs to be a leader among server nodes. Otherwise message might be put into ready queue repeatedly. 
	- HTTP/RPC interfaces
		+ Send
		+ Pull
		+ Consumption acknowledgement
* A client pulls ready queue via Http long pulling / RPC
	- For a message in ready queue, if server has not received acknowledgement within certain period (e.g. 5min), the message will be put inside Ready queue again. 
* Pros and cons:
	- Pros: Easy to implement.
	- Cons: A new client needs to be incorporated into the client side.
* Assumption: QPS 1000, maximum retention period 7 days, 

### Beanstalk
* Cons
	- Not convenient when deleting a msg. 
	- Developed based on C language, not Java and PHP. 

## Revise MQ
* Why it is 
* Schedule log is split on an hourly basis
	- Only the current schedule log segment needs to be loaded into memory
	- Build a hashwheel based on the loaded segment. Hashwheel timer is sorted and split again on a minute basis
* Hashwheel timer
	- 孙玄，时间轮wechat blog


* 