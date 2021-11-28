- [Bulkhead](#bulkhead)
- [Distributed locking](#distributed-locking)
- [Pubsub](#pubsub)
  - [Properties of pubsub](#properties-of-pubsub)
  - [When to use Pubsub](#when-to-use-pubsub)
  - [Blocklist vs Pubsub](#blocklist-vs-pubsub)
- [Stream](#stream)

# Bulkhead
* Thread Isolation: The standard approach is to hand over all requests to component C to a separate thread pool with a fixed number of threads and no \(or a small\) request queue.
  * Drawbacks: The primary drawback of thread pools is that they add computational overhead. Each command execution involves the queueing, scheduling, and context switching involved in running a command on a separate thread.
    * Costs of threads: At the 90th percentile there is a cost of 3ms for having a separate thread; At the 99th percentile there is a cost of 9ms for having a separate thread.
  * Advantages: The advantage of the thread pool approach is that requests that are passed to C can be timed out, something that is not possible when using semaphores.
* Semaphore Isolation: The other approach is to have all callers acquire a permit \(with 0 timeout\) before requests to C. If a permit can't be acquired from the semaphore, calls to C are not passed through.
* Further references
  * [https://stackoverflow.com/questions/34519/what-is-a-semaphore](https://stackoverflow.com/questions/34519/what-is-a-semaphore)
  * [A little book about semaphore](http://greenteapress.com/semaphores/LittleBookOfSemaphores.pdf)
  * [https://github.com/Netflix/Hystrix/wiki/How-it-Works](https://github.com/Netflix/Hystrix/wiki/How-it-Works)

# Distributed locking

* Please see [Distributed lock](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/distributedLock.md)

# Pubsub

## Properties of pubsub

* Pub/Sub works under the premise of “fire and forget”. This essentially means that every published message will be delivered to as many subscribers as there are then it will be lost from the buffer
* All messages will be delivered to all subscribers. Mind you, you can have subscribers listening for different channels, which would prevent this from happening. But if you have more than one subscriber on the same channel, then all of them would get the same message. It would be up to them then, to decide what to do about that.
* There is no ACK message. Some communication protocols deal with an acknowledge message, in order for the subscribers to let the publisher know the message was received. In this case, there is nothing like that, so if your subscriber gets the message and then crashes, that data will be lost for good.

## When to use Pubsub

* Chat servers, allowing you to create chat rooms easily by letting Redis take care of all the hard work of distributing messages amongst users. By default, these chat rooms would not persist messages, but you could find a way around that by adding some storage logic to your chat server
* Notification service: Another interesting use case, where you can subscribe to a set of notifications you’d like to receive, and then it’s a matter of publishers sending them to the right channel
* Log centralization. You could easily build a logging hub, where your own app is the publisher and different services make sure they send the information to the right destination. This would allow you to have a very flexible logging scheme, being able to swap from storing to disk to sending everything to an ELK instance or to a cloud service, or even all of them at once! Think about the possibilities!

## Blocklist vs Pubsub

* Messages aren’t distributed to all subscribers, in fact, every message is only delivered to one subscriber thanks to the fact that the first one to be notified, pops it out
* The fact that messages are stored in a list in Redis, they are stored inside it until a subscriber is connected. And if you configure Redis to store data in the disk, you can get a pretty reliable queueing system

# Stream

* Add data to stream: Because streams are an append only data structure, the fundamental write command, called XADD, appends a new entry into the specified stream. A stream entry is not just a string, but is instead composed of one or multiple field-value pairs. 
* Get data from stream: 
  * Access mode 1: With streams we want that multiple consumers can see the new messages appended to the Stream, like many tail -f processes can see what is added to a log. Using the traditional terminology we want the streams to be able to fan out messages to multiple clients.
  * Access mode 2: Get messages by ranges of time, or alternatively to iterate the messages using a cursor to incrementally check all the history. 
  * Access mode 3: as a stream of messages that can be partitioned to multiple consumers that are processing such messages, so that groups of consumers can only see a subset of the messages arriving in a single stream. In this way, it is possible to scale the message processing across different consumers, without single consumers having to process all the messages: each consumer will just get different messages to process. 
* Reference: 
  * [https://redis.io/topics/streams-intro](https://redis.io/topics/streams-intro)
  * [https://blog.logrocket.com/why-are-we-getting-streams-in-redis-8c36498aaac5/](https://blog.logrocket.com/why-are-we-getting-streams-in-redis-8c36498aaac5/)

**Info**

**Scan**

**Sorting**

