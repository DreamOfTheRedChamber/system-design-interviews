<!-- MarkdownTOC -->

- [Message queue](#message-queue)
	- [Benefits](#benefits)
	- [Components](#components)
	- [Routing methods](#routing-methods)
	- [Protocols](#protocols)
	- [Metrics to decide which message broker to use](#metrics-to-decide-which-message-broker-to-use)
	- [Challenges](#challenges)
	- [RocketMQ](#rocketmq)
		- [Definition](#definition)
		- [Time series data](#time-series-data)
		- [Storage model](#storage-model)
- [Delay message queue](#delay-message-queue)
	- [Redis + MySQL](#redis--mysql)
	- [Revise MQ](#revise-mq)

<!-- /MarkdownTOC -->

# Message queue 
## Benefits 
* **Enabling asynchronous processing**: 
	- Defer processing of time-consuming tasks without blocking our clients. Anything that is slow or unpredictable is a candidate for asynchronous processing. Example include
		+ Interact with remote servers
		+ Low-value processing in the critical path
		+ Resource intensive work
		+ Independent processing of high- and low- priority jobs
	- Message queues enable your application to operate in an asynchronous way, but it only adds value if your application is not built in an asynchronous way to begin with. If you developed in an environment like Node.js, which is built with asynchronous processing at its core, you will not benefit from a message broker that much. What is good about message brokers is that they allow you to easily introduce asynchronous processing to other platforms, like those that are synchronous by nature (C, Java, Ruby)
* **Easier scalability**: 
	- Producers and consumers can be scaled separately. We can add more producers at any time without overloading the system. Messages that cannot be consumed fast enough will just begin to line up in the message queue. We can also scale consumers separately, as now they can be hosted on separate machines and the number of consumers can grow independently of producers.
* **Decoupling**: 
	- All that publishers need to know is the format of the message and where to publish it. Consumers can become oblivious as to who publishes messages and why. Consumers can focus solely on processing messages from the queue. Such a high level decoupling enables consumers and producers to be developed indepdently. They can even be developed by different teams using different technologies. 
* **Evening out traffic spikes**:
	- You should be able to keep accepting requests at high rates even at times of increased traffic. Even if your publishing generates messages much faster than consumers can keep up with, you can keep enqueueing messages, and publishers do not have to be affected by a temporary capacity problem on the consumer side.
* **Isolating failures and self-healing**:
	- The fact that consumers' availability does not affect producers allows us to stop message processing at any time. This means that we can perform maintainance and deployments on back-end servers at any time. We can simply restart, remove, or add servers without affecting producer's availability, which simplifies deployments and server management. Instead of breaking the entire application whenever a back-end server goes offline, all that we experience is reduced throughput, but there is no reduction of availability. Reduced throughput of asynchronous tasks is usually invisible to the user, so there is no consumer impact. 

## Components 
* Message producer 
	- Locate the message queue and send a valid message to it
* Message broker - where messages are sent and buffered for consumers. 
	- Be available at all times for producers and to accept their messages. 
	- Buffering messages and allowing consumers to consume related messages.
* Message consumer
	- Receive and process message from the message queue. 
	- The two most common ways of implement consumers are a "cron-like" and a "daemon-like" approach. 
		+ Connects periodically to the queue and checks the status of the queue. If there are messages, it consumes them and stops when the queue is empty or after consuming a certain amount of messages. This model is common in scripting languages where you do not have a persistenly running application container, such as PHP, Ruby, or Perl. Cron-like is also referred to as a pull model because the consumers pulls messages from the queue. It can also be used if messages are added to the queue rarely or if network connectivity is unreliable. For example, a mobile application may try to pull the queue from time to time, assuming that connection may be lost at any point in time.
		+ A daemon-like consumer runs constantly in an infinite loop, and it usually has a permanent connection to the message broker. Instead of checking the status of the queue periodically, it simply blocks on the socket read operation. This means that the consumer is waiting idly until messages are pushed by the message broker in the connection. This model is more common in languages with persistent application containers, such as Java, C#, and Node.js. This is also referred to as a push model because messages are pushed by the message broker onto the consumer as fast as the consumer can keep processing them. 

## Routing methods 
* Direct worker queue method
	- Consumers and producers only have to know the name of the queue. 
	- Well suited for the distribution of time-consuming tasks such as sending out e-mails, processing videos, resizing images, or uploading content to third-party web services.
* Publish/Subscribe method
	- Producers publish message to a topic, not a queue. Messages arriving to a topic are then cloned for each consumer that has a declared subscription to that topic. 
* Custom routing rules
	- A consumer can decide in a more flexible way what messages should be routed to its queue. 
	- Logging and alerting are good examples of custom routing based on pattern matching. 

## Protocols 
* AMQP: A standardized protocol accepted by OASIS. Aims at enterprise integration and interoperability. 
* STOMP: A minimalist protocol. 
	- Simplicity is one of its main advantages. It supports fewer than a dozen operations, so implementation and debugging of libraries are much easier. It also means that the protocol layer does not add much performance overhead. 
	- But interoperability can be limited because there is no standard way of doing certain things. A good example of impaired is message prefetch count. Prefetch is a great way of increasing throughput because messages are received in batches instead of one message at a time. Although both RabbitMQ and ActiveMQ support this feature, they both implement it using different custom STOMP headers. 
* JMS
	- A good feature set and is popular
	- Your ability to integrate with non-JVM-based languages will be very limited. 

## Metrics to decide which message broker to use 
* Number of messages published per second
* Average message size
* Number of messages consumed per second (this can be much higher than publishing rate, as multiple consumers may be subscribed to receive copies of the same message)
* Number of concurrent publishers
* Number of concurrent consumers
* If message persistence is needed (no message loss during message broker crash)
* If message acknowledgement is need (no message loss during consumer crash)

## Challenges 
* No message ordering: Messages are processed in parallel and there is no synchronization between consumers. Each consumer works on a single message at a time and has no knowledge of other consumers running in parallel to it. Since your consumers are running in parallel and any of them can become slow or even crash at any point in time, it is difficult to prevent messages from being occasionally delivered out of order. 
	- Solutions:
		+ Limit the number of consumers to a single thread per queue
		+ Build the system to assume that messages can arrive in random order
		+ Use a messaging broker that supports partial message ordering guarantee. 
	- It is best to depend on the message broker to deliver messages in the right order by using partial message guarantee (ActiveMQ) or topic partitioning (Kafka). If your broker does not support such functionality, you will need to ensure that your application can handle messages being processed in an unpredictable order.
		+ Partial message ordering is a clever mechanism provided by ActiveMQ called message groups. Messages can be published with a special label called a message group ID. The group ID is defined by the application developer. Then all messages belonging to the same group are guaranteed to be consumed in the same order they were produced. Whenever a message with a new group ID gets published, the message broker maps the new group Id to one of the existing consumers. From then on, all the messages belonging to the same group are delivered to the same consumer. This may cause other consumers to wait idly without messages as the message broker routes messages based on the mapping rather than random distribution. 
	- Message ordering is a serious issue to consider when architecting a message-based application, and RabbitMQ, ActiveMQ and Amazon SQS messaging platform cannot guarantee global message ordering with parallel workers. In fact, Amazon SQS is known for unpredictable ordering messages because their infrastructure is heavily distributed and ordering of messages is not supported. 
* Message requeueing
	- By allowing messages to be delivered to your consumers more than once, you make your system more robust and reduce constraints put on the message queue and its workers. For this approach to work, you need to make all of your consumers idempotent. 
		+ But it is not an easy thing to do. Sending emails is, by nature, not an idempotent operation. Adding an extra layer of tracking and persistence could help, but it would add a lot of complexity and may not be able to handle all of the faiulres. 
		+ Idempotent consumers may be more sensitive to messages being processed out of order. If we have two messages, one to set the product's price to $55 and another one to set the price of the same product to $60, we could end up with different results based on their processing order. 
* Race conditions become more likely
* Risk of increased complexity
	- When integrating applications using a message broker, you must be very diligent in documenting dependencies and the overarching message flow. Without good documentation of the message routes and visibility of how the message flow through the system, you may increase the complexity and make it much harder for developers to understand how the system works. 

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


## Revise MQ
* Why it is 
* Schedule log is split on an hourly basis
	- Only the current schedule log segment needs to be loaded into memory
	- Build a hashwheel based on the loaded segment. Hashwheel timer is sorted and split again on a minute basis
* Hashwheel timer
	- 孙玄，时间轮wechat blog


* 