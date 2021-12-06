# MessageQueue_Kafka

* [Comparison of typical message queues](messagequeue_kafka.md#comparison-of-typical-message-queues)
* [ActiveMQ](messagequeue_kafka.md#activemq)
  * [JMS](messagequeue_kafka.md#jms)
  * [Persistence](messagequeue_kafka.md#persistence)
  * [Transaction support](messagequeue_kafka.md#transaction-support)
  * [Supported protocols](messagequeue_kafka.md#supported-protocols)
  * [High availability](messagequeue_kafka.md#high-availability)
* [RabbitMQ](messagequeue_kafka.md#rabbitmq)
  * [Concepts](messagequeue_kafka.md#concepts)
  * [Persistence](messagequeue_kafka.md#persistence-1)
    * [Memory control](messagequeue_kafka.md#memory-control)
  * [High availability](messagequeue_kafka.md#high-availability-1)
  * [Reliability](messagequeue_kafka.md#reliability)
    * [Send reliability](messagequeue_kafka.md#send-reliability)
    * [Storage reliability](messagequeue_kafka.md#storage-reliability)
    * [Consumption](messagequeue_kafka.md#consumption)
* [Kafka](messagequeue_kafka.md#kafka)
  * [Architecture](messagequeue_kafka.md#architecture)
    * [Storage layer](messagequeue_kafka.md#storage-layer)
      * [Evolution of message format](messagequeue_kafka.md#evolution-of-message-format)
      * [Log cleaning](messagequeue_kafka.md#log-cleaning)
  * [Design priniples](messagequeue_kafka.md#design-priniples)
    * [High IO throughput](messagequeue_kafka.md#high-io-throughput)
      * [Sequential read and pageCache](messagequeue_kafka.md#sequential-read-and-pagecache)
      * [ZeroCopy](messagequeue_kafka.md#zerocopy)
      * [Batching](messagequeue_kafka.md#batching)
    * [The producer](messagequeue_kafka.md#the-producer)
      * [Load balancing](messagequeue_kafka.md#load-balancing)
      * [Compression](messagequeue_kafka.md#compression)
      * [Push-based produer](messagequeue_kafka.md#push-based-produer)
    * [The consumer](messagequeue_kafka.md#the-consumer)
      * [Pull-based consumer](messagequeue_kafka.md#pull-based-consumer)
      * [Consumer position](messagequeue_kafka.md#consumer-position)
    * [Message delivery semantics](messagequeue_kafka.md#message-delivery-semantics)
      * [Message delivery](messagequeue_kafka.md#message-delivery)
      * [At least once delivery](messagequeue_kafka.md#at-least-once-delivery)
      * [At most once delivery](messagequeue_kafka.md#at-most-once-delivery)
      * [Exactly once delivery](messagequeue_kafka.md#exactly-once-delivery)
    * [In-sync Replica](messagequeue_kafka.md#in-sync-replica)
      * [Consistency model](messagequeue_kafka.md#consistency-model)
      * [Availability model](messagequeue_kafka.md#availability-model)
      * [High watermark / Log end offset](messagequeue_kafka.md#high-watermark--log-end-offset)
        * [Definition](messagequeue_kafka.md#definition)
      * [When to update high watermark](messagequeue_kafka.md#when-to-update-high-watermark)
      * [When to update log end offset](messagequeue_kafka.md#when-to-update-log-end-offset)
      * [Leader epoch](messagequeue_kafka.md#leader-epoch)
        * [Existing flaws in replica protocol](messagequeue_kafka.md#existing-flaws-in-replica-protocol)
        * [Epoch concept](messagequeue_kafka.md#epoch-concept)
        * [Epoch in data lose scenario](messagequeue_kafka.md#epoch-in-data-lose-scenario)
        * [Epoch in data diverge scenario](messagequeue_kafka.md#epoch-in-data-diverge-scenario)
      * [Unclean leader election](messagequeue_kafka.md#unclean-leader-election)
  * [Use cases](messagequeue_kafka.md#use-cases)
    * [Message broker](messagequeue_kafka.md#message-broker)
    * [Stream processing](messagequeue_kafka.md#stream-processing)
    * [Storage](messagequeue_kafka.md#storage)
* [RocketMQ](messagequeue_kafka.md#rocketmq)
  * [Architecture](messagequeue_kafka.md#architecture-1)
  * [Definition](messagequeue_kafka.md#definition-1)
  * [Supported advanced message types](messagequeue_kafka.md#supported-advanced-message-types)
    * [FIFO message](messagequeue_kafka.md#fifo-message)
    * [Delayed message](messagequeue_kafka.md#delayed-message)
    * [Transaction message](messagequeue_kafka.md#transaction-message)
      * [Example](messagequeue_kafka.md#example)
      * [Concept](messagequeue_kafka.md#concept)
      * [Algorithm](messagequeue_kafka.md#algorithm)
    * [Batch message](messagequeue_kafka.md#batch-message)
  * [Real world](messagequeue_kafka.md#real-world)

## Comparison of typical message queues

| Messaging Product | Client SDK           | Protocol and Specification                           | Ordered Message                                                 | Scheduled Message | Batched Message                                 | BroadCast Message | Message Filter                                          | Server Triggered Redelivery | Message Storage                                                                                         | Message Retroactive                          | Message Priority | High Availability and Failover                                                 | Message Track | Configuration                                                                                                             | Management and Operation Tools                                  |
| ----------------- | -------------------- | ---------------------------------------------------- | --------------------------------------------------------------- | ----------------- | ----------------------------------------------- | ----------------- | ------------------------------------------------------- | --------------------------- | ------------------------------------------------------------------------------------------------------- | -------------------------------------------- | ---------------- | ------------------------------------------------------------------------------ | ------------- | ------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| ActiveMQ          | Java, .NET, C++ etc. | Push model, support OpenWire, STOMP, AMQP, MQTT, JMS | Exclusive Consumer or Exclusive Queues can ensure ordering      | Supported         | Not Supported                                   | Supported         | Supported                                               | Not Supported               | Supports very fast persistence using JDBC along with a high performance journal，such as levelDB, kahaDB | Supported                                    | Supported        | Supported, depending on storage,if using kahadb it requires a ZooKeeper server | Not Supported | The default configuration is low level, user need to optimize the configuration parameters                                | Supported                                                       |
| Kafka             | Java, Scala etc.     | Pull model, support TCP                              | Ensure ordering of messages within a partition                  | Not Supported     | Supported, with async producer                  | Not Supported     | Supported, you can use Kafka Streams to filter messages | Not Supported               | High performance file storage                                                                           | Supported offset indicate                    | Not Supported    | Supported, requires a ZooKeeper server                                         | Not Supported | Kafka uses key-value pairs format for configuration. These values can be supplied either from a file or programmatically. | Supported, use terminal command to expose core metrics          |
| RocketMQ          | Java, C++, Go        | Pull model, support TCP, JMS, OpenMessaging          | Ensure strict ordering of messages,and can scale out gracefully | Supported         | Supported, with sync mode to avoid message loss | Supported         | Supported, property filter expressions based on SQL92   | Supported                   | High performance and low latency file storage                                                           | Supported timestamp and offset two indicates | Not Supported    | Supported, Master-Slave model, without another kit                             | Supported     | Work out of box,user only need to pay attention to a few configurations                                                   | Supported, rich web and terminal command to expose core metrics |

* ActiveMQ: 
  * Pros:
    1. Support large range of protocols. 
  *   Cons:

        1\. 
* RabbitMQ
  * Pros 
    1. Maintained by the same company as Spring, best support from Spring community
    2. Lightweight
    3. There is an exchange module between producer and queue which enables flexible routing.
    4. Support AMQP
  * Cons
    1. Written in Erlang, steep language learning curve.
    2. Relative low performance, 10K to 100K processed msg / second.
    3. Performance downgrade when large amounts of message accumulated and unprocessed.  
* Kafka 
  * Pros
    1. Integrate well with other components in big data processing and stream processing ecosystem
    2. Super high performance in async send/write. The extreme processing roughly 20 million msg / seconds
  * Cons
    1. A little low performance in sync send/write due to the batch upgrade inside. 
* RocketMQ
  * Pros
    1. Integrate learnings from Kafka, RabbitMQ to its own design. 
    2. Active community and plenty Chinese documentation
    3. Low performance latency
  * Cons
    1. Not so popular internationally. 
    2. New. Less compatibility with the surrounding community
* ZeroMQ: Multi-threading network library
* Pulsar: Separation of storage and computation 

## ActiveMQ

### JMS

* Fully support JMS standard. JMS standards defined API client set will use. JMS defines the  following: 
  * Concept: Connection/Session/MessageProducer/MessageConsumer/Broker/Message
  * Message model: Point-to-Point, Pub/Sub
  * Message structure: Header, body

### Persistence

* Support JDBC, AMQ, KahaDB, LevelDB
  * JDBC: easy to manage but low performant
  * AMQ: File based storage mechanism. Performance better than JDBC
  * KahaDB: Default persistence storage
    * Write batch size
    * Cache size
    * File size
  * LevelDB: KV storage

### Transaction support

### Supported protocols

* Supported protocols:
  * AUTO, OpenWire, AMQP, Stomp, MQTT
* Transmission mechanism
  * VM, TCP, SSL, UDP, Peer, Multicast, HTTPS
  * Failover, fanout, discovery, zeroConf

### High availability

* Shared storage master slave (file system/database/sharded database)
* Broker cluster

## RabbitMQ

### Concepts

![Concepts](../.gitbook/assets/messagequeue_rabbitmq_concepts.png)

### Persistence

* Queue persistence
* Message persistence
* Exchange persistence

#### Memory control

* Max memory config

### High availability

* Metadata replication and forward
* Cluster mirrored queues
* Federation plugin
* Shovel plugin

### Reliability

#### Send reliability

* Definition: Message could arrive at broker successfully
  * At most once: Supported. Usually not used because there might be message loss. 
  * At least once: Supported. There are two mechanism: Publisher confirm and transaction. The latter one is slow and seldomly used. 
  * Exactly once: Not supported
* Publisher confirm is a mechanism to return 

![Sender reliability](../.gitbook/assets/messageQueue_rabbitmq_sendreliability.png)

#### Storage reliability

#### Consumption

* Storage reliability: Broker could persist the message and guarantee no loss
* Consumption reliability: Message could be successfully consumed

## Kafka

### Architecture

* Topics and logs

![Topics and logs](../.gitbook/assets/messageQueue_kafka_concepts_topic.png)

* Partitions

![Partitions](../.gitbook/assets/messageQueue_kafka_concepts_partition.png)

* Consumers

![Consumers](../.gitbook/assets/messageQueue_kafka_concepts_consumers.png)

* Replication

![Replication](../.gitbook/assets/messageQueue_kafka_concepts_replication.png)

* Architecture

![architecture](../.gitbook/assets/messageQueue_kafka_architecture.png)

#### Storage layer

* File structure

![file structure 1](../.gitbook/assets/kafka_filestructure1.png) ![file structure 2](images/kafka_filestructure2.png)

* Index
  * .index
  * .timeindex

![.index definition](../.gitbook/assets/kafka_indexDefinition.png) 
![.index flowchart](../.gitbook/assets/kafka_indexFlowchart.png) 
![.timeindex definition](../.gitbook/assets/kafka_timeindexDefinition.png) 
![.timeindex flowchart](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/kafka_timeindexFlowchart.png)

* Reference: 深入理解Kafka：核心设计与实践原理

**Evolution of message format**

* V0 message format
  * Kafka relies on Java NIO's ByteBuffer to save message, and relies on pagecache instead of Java's heap to store message. 
    * Within Java Memory Model, sometimes it will consume 2 times space to save the data. 

```
CRC:
magic: version number. V0 magic=0, V1 magic=1, V2 magic=2
attribute: Only uses the lower three bit to represent the compression type. (0x00:no compression, 0x01:GZIP, 0x02:Snappy, 0x03:LZ4)
key length: If no key, then -1;
key:
value length:
value:
```

![V0 message format](../.gitbook/assets/kafka_msgV0\_format.png)

* V1 message format
  * Downsides of V0 message format
    * There is no timestamp. When Kafka deletes expired logs, it could only rely on the last modified timestamp, which is easy to be modified by external operations. 
    * Many stream processing frameworks need timestamp to perform time-based aggregation operations
  * Changes when compared with V1
    * Introduce a 8 bits timestamp.
    * The last bit of attribute is being used to specify the type of timestamp: CReATE_TIME or LOG_APPEND_TIME

![V1 message format](../.gitbook/assets/kafka_msgV1\_format.png)

* V2
  * Downsides of V0/V1 message set
    * Low space utilization: Use fixed size 4 bytes to save length
    * Only saves the latest message offset: If compressed, then the offset will be the offset of the last message compressed
    * Redundant CRC checking: CRC is executed on a per message basis
    * Not saving message length: Each time the total length needs to be computed.
  * Changes when compared with V1:
    * Introduced Protocol Buffer's Varints and Zigzag coding. 
    * Message set added several other fields:
      1. first offset: 
      2. length:
      3. partition leader epoch:
      4. magic:
      5. attributes: 
      6. last offset delta:
      7. first timestamp:
      8. max timestamp:
      9. producer id:
      10. producer epoch:
      11. first sequence:
      12. records count

![V2 message format](../.gitbook/assets/kafka_msgV2\_format.png)

**Log cleaning**

* Log retention
  * Based on time / size / initial offset
* Log compaction
  * For entries having the same key but different value.

### Design priniples

#### High IO throughput

**Sequential read and pageCache**

* Design: 
  * Each partition is stored sequentially on disk. Kafka storage is designed to be read / write sequentially. 
  * Rather than maintain as much as possible in-memory and flush it all out to the filesystem in a panic when we run out of space, we invert that. All data is immediately written to a persistent log on the filesystem without necessarily flushing to disk. In effect this just means that it is transferred into the kernel's pagecache.
* Reason: 
  * The key fact about disk performance is that the throughput of hard drives has been diverging from the latency of a disk seek for the last decade. As a result the performance of linear writes on a JBOD configuration with six 7200rpm SATA RAID-5 array is about 600MB/sec but the performance of random writes is only about 100k/sec—a difference of over 6000X. These linear reads and writes are the most predictable of all usage patterns, and are heavily optimized by the operating system. A modern operating system provides read-ahead and write-behind techniques that prefetch data in large block multiples and group smaller logical writes into large physical writes
  * To compensate for this performance divergence, modern operating systems have become increasingly aggressive in their use of main memory for disk caching. A modern OS will happily divert all free memory to disk caching with little performance penalty when the memory is reclaimed. All disk reads and writes will go through this unified cache. This feature cannot easily be turned off without using direct I/O, so even if a process maintains an in-process cache of the data, this data will likely be duplicated in OS pagecache, effectively storing everything twice.
  * Kafka is built on top of JVM: The memory overhead of objects is very high, often doubling the size of the data stored (or worse). Java garbage collection becomes increasingly fiddly and slow as the in-heap data increases. 
  * As a result of these factors using the filesystem and relying on pagecache is superior to maintaining an in-memory cache or other structure—we at least double the available cache by having automatic access to all free memory, and likely double again by storing a compact byte structure rather than individual objects. Doing so will result in a cache of up to 28-30GB on a 32GB machine without GC penalties. Furthermore, this cache will stay warm even if the service is restarted, whereas the in-process cache will need to be rebuilt in memory (which for a 10GB cache may take 10 minutes) or else it will need to start with a completely cold cache (which likely means terrible initial performance). This also greatly simplifies the code as all logic for maintaining coherency between the cache and filesystem is now in the OS, which tends to do so more efficiently and more correctly than one-off in-process attempts. If your disk usage favors linear reads then read-ahead is effectively pre-populating this cache with useful data on each disk read.

**ZeroCopy**

* At low message rates this is not an issue, but under load the impact is significant. To avoid this we employ a standardized binary message format that is shared by the producer, the broker, and the consumer (so data chunks can be transferred without modification between them).
* The message log maintained by the broker is itself just a directory of files, each populated by a sequence of message sets that have been written to disk in the same format used by the producer and consumer. Maintaining this common format allows optimization of the most important operation: network transfer of persistent log chunks.
* To understand the impact of sendfile, it is important to understand the common data path for transfer of data from file to socket:
  1. The operating system reads data from the disk into pagecache in kernel space
  2. The application reads the data from kernel space into a user-space buffer
  3. The application writes the data back into kernel space into a socket buffer
  4. The operating system copies the data from the socket buffer to the NIC buffer where it is sent over the network
* This is clearly inefficient, there are four copies and two system calls. Modern unix operating systems offer a highly optimized code path for transferring data out of pagecache to a socket; in Linux this is done with the sendfile system call. Using sendfile, this re-copying is avoided by allowing the OS to send the data from pagecache to the network directly. So in this optimized path, only the final copy to the NIC buffer is needed. 
* Using the zero-copy optimization above, data is copied into pagecache exactly once and reused on each consumption instead of being stored in memory and copied out to user-space every time it is read. This allows messages to be consumed at a rate that approaches the limit of the network connection.

**Batching**

* The small I/O problem happens both between the client and the server and in the server's own persistent operations.
* To avoid this, our protocol is built around a "message set" abstraction that naturally groups messages together. This allows network requests to group messages together and amortize the overhead of the network roundtrip rather than sending a single message at a time. The server in turn appends chunks of messages to its log in one go, and the consumer fetches large linear chunks at a time.
* This simple optimization produces orders of magnitude speed up. Batching leads to larger network packets, larger sequential disk operations, contiguous memory blocks, and so on, all of which allows Kafka to turn a bursty stream of random message writes into linear writes that flow to the consumers.

#### The producer

**Load balancing**

* The producer controls which partition it publishes to. It sends data directly to the broker that is the leader for the partition without any intervening routing tier. 
  * Partition strategy
    * Round-robin
    * Randomized
    * Based on message key: or keyed This can be done at random, implementing a kind of random load balancing, or it can be done by some semantic partitioning function. 
    * Based on location: 
* To help the producer do this all Kafka nodes can answer a request for metadata about which servers are alive and where the leaders for the partitions of a topic are at any given time to allow the producer to appropriately direct its requests.

**Compression**

* In some cases the bottleneck is actually not CPU or disk but network bandwidth. This is particularly true for a data pipeline that needs to send messages between data centers over a wide-area network. Of course, the user can always compress its messages one at a time without any support needed from Kafka, but this can lead to very poor compression ratios as much of the redundancy is due to repetition between messages of the same type (e.g. field names in JSON or user agents in web logs or common string values). Efficient compression requires compressing multiple messages together rather than compressing each message individually.
* Kafka supports GZIP, Snappy, LZ4 and ZStandard compression protocols.
* Message will be compressed on producer, maintained on broker and decompressed on consumer. 

**Push-based produer**

* You could imagine other possible designs which would be only pull, end-to-end. The producer would locally write to a local log, and brokers would pull from that with consumers pulling from them. A similar type of "store-and-forward" producer is often proposed. This is intriguing but we felt not very suitable for our target use cases which have thousands of producers. Our experience running persistent data systems at scale led us to feel that involving thousands of disks in the system across many applications would not actually make things more reliable and would be a nightmare to operate. And in practice we have found that we can run a pipeline with strong SLAs at large scale without a need for producer persistence.

#### The consumer

**Pull-based consumer**

* Pros:
  * A pull-based system has the nicer property that the consumer simply falls behind and catches up when it can. This can be mitigated with some kind of backoff protocol by which the consumer can indicate it is overwhelmed, but getting the rate of transfer to fully utilize (but never over-utilize) the consumer is trickier than it seems.
  * Another advantage of a pull-based system is that it lends itself to aggressive batching of data sent to the consumer. A push-based system must choose to either send a request immediately or accumulate more data and then send it later without knowledge of whether the downstream consumer will be able to immediately process it. If tuned for low latency, this will result in sending a single message at a time only for the transfer to end up being buffered anyway, which is wasteful. A pull-based design fixes this as the consumer always pulls all available messages after its current position in the log (or up to some configurable max size). So one gets optimal batching without introducing unnecessary latency.
* Cons: 
  * The deficiency of a naive pull-based system is that if the broker has no data the consumer may end up polling in a tight loop, effectively busy-waiting for data to arrive. To avoid this we have parameters in our pull request that allow the consumer request to block in a "long poll" waiting until data arrives (and optionally waiting until a given number of bytes is available to ensure large transfer sizes).

**Consumer position**

* Most messaging systems keep metadata about what messages have been consumed on the broker. 
  * Cons: Getting broker and consumer to agree about what has been consumed is not a trivival problem.
    * If the broker records a message as consumed immediately every time it is handed out over the network, then if the consumer fails to process the message (say because it crashes or the request times out or whatever) that message will be lost.
    * To solve this problem, many messaging systems add an acknowledgement feature which means that messages are only marked as sent not consumed when they are sent; the broker waits for a specific acknowledgement from the consumer to record the message as consumed.
    * This strategy fixes the problem of losing messages, but creates new problems.
      1. If the consumer processes the message but fails before it can send an acknowledgement then the message will be consumed twice.
      2. Now the broker must keep multiple states about every single message (first to lock it so it is not given out a second time, and then to mark it as permanently consumed so that it can be removed). Tricky problems must be dealt with, like what to do with messages that are sent but never acknowledged.
* Kafka keeps metadata about what messages have been consumed on the consumer group level. 
  * The position of a consumer in each partition is just a single integer, the offset of the next message to consume. This makes the state about what has been consumed very small, just one number for each partition. This state can be periodically checkpointed.
  * A consumer can deliberately rewind back to an old offset and re-consume data. This violates the common contract of a queue, but turns out to be an essential feature for many consumers. For example, if the consumer code has a bug and is discovered after some messages are consumed, the consumer can re-consume those messages once the bug is fixed.

#### Message delivery semantics

**Message delivery**

* When publishing a message we have a notion of the message being "committed" to the log. Once a published message is committed it will not be lost as long as one broker that replicates the partition to which this message was written remains "alive". 
  * Commited message: when all in sync replicas for that partition have applied it to their log.
  * Alive partition: liveness has two conditions
    1. A node must be able to maintain its session with ZooKeeper (via ZooKeeper's heartbeat mechanism)
    2. If it is a follower it must replicate the writes happening on the leader and not fall "too far" behind
  * Types of failure attempted to handle: Kafka only attempt to handle a "fail/recover" model of failures where nodes suddenly cease working and then later recover (perhaps without knowing that they have died). Kafka does not handle so-called "Byzantine" failures in which nodes produce arbitrary or malicious responses (perhaps due to bugs or foul play).

**At least once delivery**

* Kafka guarantees at-least-once delivery by default.
* It can read the messages, process the messages, and finally save its position. In this case there is a possibility that the consumer process crashes after processing messages but before saving its position. In this case when the new process takes over the first few messages it receives will already have been processed. This corresponds to the "at-least-once" semantics in the case of consumer failure. In many cases messages have a primary key and so the updates are idempotent (receiving the same message twice just overwrites a record with another copy of itself).

**At most once delivery**

* Kafka allows the user to implement at-most-once delivery by disabling retries on the producer and committing offsets in the consumer prior to processing a batch of messages.
  * It can read the messages, then save its position in the log, and finally process the messages. In this case there is a possibility that the consumer process crashes after saving its position but before saving the output of its message processing. In this case the process that took over processing would start at the saved position even though a few messages prior to that position had not been processed. This corresponds to "at-most-once" semantics as in the case of a consumer failure messages may not be processed.

**Exactly once delivery**

* Idempotent producer
  * Since 0.11.0.0, the Kafka producer also supports an idempotent delivery option which guarantees that resending will not result in duplicate entries in the log by setting "enable.idempotence" to true. To achieve this, the broker assigns each producer an ID and deduplicates messages using a sequence number that is sent by the producer along with every message.
  * Limitations of idempotent producer
    1. Only gaurantee the idempotence within a single partition
    2. Only gaurantee the idempotence within a single session (session means one life time of a producer process. If the producer restart, the idempotence will be lost)
  * Ways to guarantee idempotency within the business logic
    1. Use database unique constraint
    2. Have a prerequisite for data update operation
    3. Record and check operation to guarantee only executed once
* Transactional producer
  * Beginning with 0.11.0.0, the producer supports the ability to send messages to multiple topic partitions using transaction-like semantics: i.e. either all messages are successfully written or none of them are. The main use case for this is exactly-once processing between Kafka topics (described below).
    1. enable.idempotence = true
    2. set the parameter “transactional.id” on the producer end
  * Isolation level: 
    1. In the default "read_uncommitted" isolation level, all messages are visible to consumers even if they were part of an aborted transaction.
    2. In "read_committed," the consumer will only return messages from transactions which were committed (and any messages which were not part of a transaction).
  * Additional APIs: initTransaction / beginTransaction / commitTransaction / abortTransaction
* So effectively Kafka supports exactly-once delivery in Kafka Streams, and the transactional producer/consumer can be used generally to provide exactly-once delivery when transferring and processing data between Kafka topics. Exactly-once delivery for other destination systems generally requires cooperation with such systems, but Kafka provides the offset which makes implementing this feasible (see also Kafka Connect).

#### In-sync Replica

* A balance between synchronous (performance) and asynchronous (consistency) replication. As long as all in-sync replicas have been replicated, the replication will be considered successfully. 
  * in-sync are defined by broker config replica.lag.time.max.ms, which means the longest duration follower replica could be behind leader replica. 
  * The unit of replication is the topic partition. Under non-failure conditions, each partition in Kafka has a single leader and zero or more followers. The total number of replicas including the leader constitute the replication factor. 
  * Followers consume messages from the leader just as a normal Kafka consumer would and apply them to their own log. Having the followers pull from the leader has the nice property of allowing the follower to naturally batch together log entries they are applying to their log.

**Consistency model**

* Master-write slave-read model
  * Operation: 
  * Pros: Read performance (elastic read capacity / low read latency by putting data closer to users)
  * Cons: Consistency (there is a replication delay from master to slave); It does not necessary help 
* Kafka adopted master-write master-read model
  * Operation: All reads/writes go to the leader of partition. Follows only pull offsets and messages from leader. 
  * Pros: 
    1. Support "Read your writes": You could immedately use consumer api to read the msg written by producer api.
    2. Support "Monotonic reads": For a single consumer client, the existence of a message will be determined.
  * Cons: Kafka does not need read replica to optimize its read performance because it is optimized by topic partition. 

**Availability model**

* Basic guarantee: If we tell the client a message is committed, and the leader fails, the new leader we elect must also have that message. This yields a tradeoff: if the leader waits for more followers to acknowledge a message before declaring it committed then there will be more potentially electable leaders.
* Qurom based leader election
  * Let's say we have 2f+1 replicas. If f+1 replicas must receive a message prior to a commit being declared by the leader, and if we elect a new leader by electing the follower with the most complete log from at least f+1 replicas, then, with no more than f failures, the leader is guaranteed to have all committed messages. This is because among any f+1 replicas, there must be at least one replica that contains all committed messages. That replica's log will be the most complete and therefore will be selected as the new leader    
  * Pros: This majority vote approach has a very nice property: the latency is dependent on only the fastest servers. That is, if the replication factor is three, the latency is determined by the faster follower not the slower one.
  * Cons: The downside of majority vote is that it doesn't take many failures to leave you with no electable leaders. To tolerate one failure requires three copies of the data, and to tolerate two failures requires five copies of the data. In our experience having only enough redundancy to tolerate a single failure is not enough for a practical system, but doing every write five times, with 5x the disk space requirements and 1/5th the throughput, is not very practical for large volume data problems. This is likely why quorum algorithms more commonly appear for shared cluster configuration such as ZooKeeper but are less common for primary data storage. For example in HDFS the namenode's high-availability feature is built on a majority-vote-based journal, but this more expensive approach is not used for the data itself.
  * Implementation: ZooKeeper's Zab, Raft, and Viewstamped Replication. The most similar academic publication we are aware of to Kafka's actual implementation is PacificA from Microsoft.
* In-sync replica
  * Kafka dynamically maintains a set of in-sync replicas (ISR) that are caught-up to the leader. Only members of this set are eligible for election as leader. A write to a Kafka partition is not considered committed until all in-sync replicas have received the write. This ISR set is persisted to ZooKeeper whenever it changes. Because of this, any replica in the ISR is eligible to be elected leader. This is an important factor for Kafka's usage model where there are many partitions and ensuring leadership balance is important. With this ISR model and f+1 replicas, a Kafka topic can tolerate f failures without losing committed messages.
  * Pros: One important design distinction is that Kafka does not require that crashed nodes recover with all their data intact. It is not uncommon for replication algorithms in this space to depend on the existence of "stable storage" that cannot be lost in any failure-recovery scenario without potential consistency violations. There are two primary problems with this assumption. First, disk errors are the most common problem we observe in real operation of persistent data systems and they often do not leave data intact. Secondly, even if this were not a problem, we do not want to require the use of fsync on every write for our consistency guarantees as this can reduce performance by two to three orders of magnitude. Our protocol for allowing a replica to rejoin the ISR ensures that before rejoining, it must fully re-sync again even if it lost unflushed data in its crash.
  * Cons: To tolerate f failures, both the majority vote and the ISR approach will wait for the same number of replicas to acknowledge before committing a message (e.g. to survive one failure a majority quorum needs three replicas and one acknowledgement and the ISR approach requires two replicas and one acknowledgement). The ability to commit without the slowest servers is an advantage of the majority vote approach. However, we think it is ameliorated by allowing the client to choose whether they block on the message commit or not, and the additional throughput and disk space due to the lower required replication factor is worth it.

**High watermark / Log end offset**

**Definition**

* High watermark <= HW means all the commited messages available to consumers. 
* The offset position value for the next log

**When to update high watermark**

* Follower side: Each time after follower writes log. It will take the minimum(LEO, HW in leader's response)
* Leader side: 
  1. When the replica becomes a leader
  2. When broker crashes and replica is kicked out of ISR
  3. When producer writes a message to leader replica
  4. When leader process follower's FETCh request

**When to update log end offset**

* Follwer side: Each time follower writes a log 
* Leader side: 
  * Leader side stored followers' LEO: After receiving a follower FETCH request but before returns response to follower. 
  * Leader side stored leader's LEO: Each time leader writes a log

**Leader epoch**

**Existing flaws in replica protocol**

* The replication protocol in Kafka has two phases. Initially the the follower fetches messages. So it might fetch message m2. On the next round of RPC it will confirm receipt of message m2 and, assuming other replicas have confirmed successfully, the leader will progress the High Watermark. This is then passed back to the followers in the responses to their fetch requests. So the leader controls the progression rate of the High Watermark, which is propagated back to followers in subsequent rounds of RPC.
* The replication protocol also includes a phase where, on initialisation of a follower, the follower will truncate its log to the High Watermark it has recorded, then fetch messages from the leader. The problem is that, should that follower become leader before it has caught up, some messages may be lost due to the truncation.

**Epoch concept**

* We can solve both of these issues by introducing the concept of a Leader Epoch. This allocates an identifier to a period of leadership, which is then added to each message by the leader. Each replica keeps a vector of \[LeaderEpoch => StartOffset] to mark when leaders changed throughout the lineage of its log. This vector then replaces the high watermark when followers need to truncate data (and will be stored in a file for each replica).  So instead of a follower truncating to the High Watermark, the follower gets the appropriate LeaderEpoch from the leader’s vector of past LeaderEpochs and uses this to truncate only messages that do not exist in the leader’s log. So the leader effectively tells the follower what offset it needs to truncate to.

![Kafka epoch concept](../.gitbook/assets/kafka_epoch_concept.png)

**Epoch in data lose scenario**

* High watermark truncation followed by immediate leader election
  * Let’s take an example. Imagine we have two brokers A & B. B is the leader initially as in the below figure. (A) fetches message m2 from the leader (B). So the follower (A) has message m2, but has not yet got confirmation from the leader (B) that m2 has been committed (the second round of replication, which lets (A) move forward its high watermark past m2, has yet to happen). At this point the follower (A) restarts. It truncates its log to the high watermark and issues a fetch request to the leader (B). (B) then fails and A becomes the new leader. Message m2 has been lost permanently (regardless of whether B comes back or not).

![High water mark data lose scenario](../.gitbook/assets/kafka_epoch_scenario1.png)

* How does it help    
  * In this solution the follower makes a request to the leader to determine if it has any divergent epochs in its log. It sends a LeaderEpochRequest to the leader for its current LeaderEpoch. In this case the leader returns the log end offset, although if the follower was lagging by more than one Leader Epoch, the leader would return the first offset in (Follower Leader Epoch + 1). So that’s to say the LeaderEpoch response contains the offset where the requested LeaderEpoch ends.
  * In this case the LeaderEpochResponse returns offset 2. Note this is different to the high watermark which, on the follower, is offset 0. Thus the follower does not truncate any messages and hence message m2 is not lost.

![Epoch in data lose scenario](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/kafka_epoch_solution1.png)

**Epoch in data diverge scenario**

* Replica divergence on restart after multiple hard failures
  * Imagine again we have two brokers, but this time we have a power outage which affects both of them. It is acceptable, in such a case, that we could lose data if n replicas are lost (Kafka guarantees durability for n-1 replicas). Unfortunately there is an opportunity for the logs, on different machines, to diverge and even, in the worst case, for replication to become stuck.
  * The underlying issue is that messages are flushed to disk asynchronously. This means, after a crash, machines can be an arbitrary number of messages behind one another. When they come back up, any one might become leader. If the leader happens to be the machine with the fewest messages in its log we’ll lose data. Whilst that is within the durability contract of the system, the issue is that the replicas can diverge, with different message lineage in different replicas.
  * As we support compressed message sets this can, at worst, lead to an inability for replicas to make progress. This happens when the offset for a compressed message set in one replica points to the midpoint of a compressed message set in another.

![High watermark data diverge scenario](../.gitbook/assets/kafka_epoch_scenario2.png)

* How does it help
  * When the two brokers restart after a crash, broker B becomes leader. It accepts message m3 but with a new Leader Epoch, LE1. Now when broker A starts, and becomes a follower, it sends a LeaderEpoch request to the leader. This returns the first offset of LE1, which is offset 1. The follower knows that m2 is orphaned and truncates it. It then fetches from offset 1 and the logs are consistent.

![Epoch in data divergence scenario](../.gitbook/assets/kafka_epoch_solution2.png)

* [Reference](https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation)

**Unclean leader election**

* Kafka's guarantee with respect to data loss is predicated on at least one replica remaining in sync. If all the nodes replicating a partition die, this guarantee no longer holds.
* If you are unlucky enough to have this occur, it is important to consider what will happen. There are two behaviors that could be implemented:
  1. Wait for a replica in the ISR to come back to life and choose this replica as the leader (hopefully it still has all its data).
  2. Choose the first replica (not necessarily in the ISR) that comes back to life as the leader.
* This is a simple tradeoff between availability and consistency. If we wait for replicas in the ISR, then we will remain unavailable as long as those replicas are down. If such replicas were destroyed or their data was lost, then we are permanently down. If, on the other hand, a non-in-sync replica comes back to life and we allow it to become leader, then its log becomes the source of truth even though it is not guaranteed to have every committed message.

### Use cases

#### Message broker

#### Stream processing

* Many users of Kafka process data in processing pipelines consisting of multiple stages, where raw input data is consumed from Kafka topics and then aggregated, enriched, or otherwise transformed into new topics for further consumption or follow-up processing. For example, a processing pipeline for recommending news articles might crawl article content from RSS feeds and publish it to an "articles" topic; further processing might normalize or deduplicate this content and publish the cleansed article content to a new topic; a final processing stage might attempt to recommend this content to users. Such processing pipelines create graphs of real-time data flows based on the individual topics. Starting in 0.10.0.0, a light-weight but powerful stream processing library called Kafka Streams is available in Apache Kafka to perform such data processing as described above. Apart from Kafka Streams, alternative open source stream processing tools include Apache Storm and Apache Samza.

#### Storage

* Traditional msg queues could not be used as storage because
  * Reading the msg also removes it
  * Messaging systems scale poorly as data accumulates beyond what fits in memory
  * Messaging systems generally lack robust replication features
* How Kafka is different from traditional msg queue
  * Kafka stores a persistent log which can be re-read and kept indefinitely
  * Kafka is built as a modern distributed system: it runs as a cluster, can expand or contract elastically, and replicates data internally for fault-tolerance and high-availability.
  * Kafka is built to allow real-time stream processing, not just processing of a single message at a time. This allows working with data streams at a much higher level of abstraction. 
* Whether Kafka could become a kind of universal database? No because
  * Databases are mostly about queries, Kafka does not benefit from any kind of random access against the log
  * The mission for Kafka is to make streams of data and stream processing a mainstream development paradihm. 
* Reference: [It's Okay to Store Data in Apache Kafka](https://www.confluent.io/blog/okay-store-data-apache-kafka/)

## RocketMQ

### Architecture

![Architecture](../.gitbook/assets/messageQueue_rocketMQ_architecture.png)

### Definition

* A broker contains a master node and a slave node
  * Broker 1 has topic 1 to 5
  * Broker 2 has topic 6 to 10
* NameNode cluster contains the mapping from Topic=>Broker
* Scenario 1. Consumer group tells name node cluster which topic it subscribe to 2. Broker pulls from name node cluster about the heartbeat message (whether I am alive / topic mapping on the broker) 3. Producer group pushes events to the broker 4. Broker push events to consumer group

### Supported advanced message types

#### FIFO message

* The processing of message follows the producing order. 
* Order types
  * Global order
  * Partition order
* How to guarantee

#### Delayed message

* Not support any granularity. There are a couple granularity level such as 1s, 5s, 10s, 1 minute, 2 minute, ... 1 hour, 5 hour. 

#### Transaction message

* [https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/](https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/)

**Example**

* Example: A user is purchasing items on an ecommerce website. There are two operations
  1. Create an order in the database
  2. Delete ordered items from the shopping cart. Since this step is not a necessary step to be completed within the order operation, the command could be processed asynchronously, e.g. putting into a message queue. 

**Concept**

* Half (prepare) message: Refers to a message that cannot be delivered temporarily. When a message is successfully sent to the MQ server, but the server did not receive the second acknowledgement of the message from the producer, then the message is marked as “temporarily undeliverable”. The message in this status is called a half message.
* Message status check: Network disconnection or producer application restart may result in the loss of the second acknowledgement of a transactional message. When MQ server finds that a message remains a half message for a long time, it will send a request to the message producer, checking the final status of the message (Commit or Rollback).

**Algorithm**

1. Producer send half message to MQ server.
2. After send half message succeed, execute local transaction.
3. Send commit or rollback message to MQ Server based on local transaction results.
4. If commit/rollback message missed or producer pended during the execution of local transaction，MQ server will send check message to each producers in the same group to obtain transaction status.
5. Producer reply commit/rollback message based on local transaction status.
6. Committed message will be delivered to consumer but rolled back message will be discarded by MQ server.
7. ![Execute flow chart](../.gitbook/assets/mq_transactions_flowchart.png)

#### Batch message

### Real world

* Kafka at Netflix: [https://netflixtechblog.com/kafka-inside-keystone-pipeline-dd5aeabaf6bb](https://netflixtechblog.com/kafka-inside-keystone-pipeline-dd5aeabaf6bb)
