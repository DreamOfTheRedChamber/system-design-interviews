# Reading Kafka source code
<!-- MarkdownTOC -->

- [Use cases](#use-cases)
	- [Message broker](#message-broker)
		- [Comparison with other msg brokers](#comparison-with-other-msg-brokers)
	- [Stream processing](#stream-processing)
	- [Storage](#storage)
- [Design priniples](#design-priniples)
	- [High IO throughput](#high-io-throughput)
		- [Sequential read and pageCache](#sequential-read-and-pagecache)
		- [ZeroCopy](#zerocopy)
		- [Batching](#batching)
	- [The producer](#the-producer)
		- [Load balancing](#load-balancing)
		- [Compression](#compression)
		- [Push-based produer](#push-based-produer)
	- [The consumer](#the-consumer)
		- [Pull-based consumer](#pull-based-consumer)
		- [Consumer position](#consumer-position)
	- [Message delivery semantics](#message-delivery-semantics)
		- [Message delivery](#message-delivery)
		- [At least once delivery](#at-least-once-delivery)
		- [At most once delivery](#at-most-once-delivery)
		- [Exactly once delivery](#exactly-once-delivery)
	- [Replication](#replication)
	- [Log compaction](#log-compaction)
- [Broker](#broker)
- [Controller](#controller)
- [Producer](#producer)
- [Consumer](#consumer)
- [Storage layer](#storage-layer)
- [Stream processing](#stream-processing-1)

<!-- /MarkdownTOC -->

## Use cases
### Message broker
#### Comparison with other msg brokers
* Kafka 
	- Pros
		1. Integrate well with other components in big data processing and stream processing ecosystem
		2. Super high performance in async send/write. The extreme processing roughly 20 million msg / seconds
	- Cons
		1. A little low performance in sync send/write due to the batch upgrade inside. 
* RocketMQ
	- Pros
		1. Active community and plenty Chinese documentation
		2. Low performance latency
	- Cons
		1. Not so popular internationally. Less compatibility with the surrounding community
* RabbitMQ
	- Pros 
		1. Lightweight
		2. There is an exchange module between producer and queue which enables flexible routing.
	- Cons
		1. Written in Erlang, steep language learning curve.
		2. Relative low performance, 10K to 100K processed msg / second.
		3. Performance downgrade when large amounts of message accumulated and unprocessed.  
* ActiveMQ: Only choice 10 years ago
* ZeroMQ: Multi-threading network library
* Pulsar: Separation of storage and computation 

### Stream processing
* Many users of Kafka process data in processing pipelines consisting of multiple stages, where raw input data is consumed from Kafka topics and then aggregated, enriched, or otherwise transformed into new topics for further consumption or follow-up processing. For example, a processing pipeline for recommending news articles might crawl article content from RSS feeds and publish it to an "articles" topic; further processing might normalize or deduplicate this content and publish the cleansed article content to a new topic; a final processing stage might attempt to recommend this content to users. Such processing pipelines create graphs of real-time data flows based on the individual topics. Starting in 0.10.0.0, a light-weight but powerful stream processing library called Kafka Streams is available in Apache Kafka to perform such data processing as described above. Apart from Kafka Streams, alternative open source stream processing tools include Apache Storm and Apache Samza.

### Storage
* Traditional msg queues could not be used as storage because
	- Reading the msg also removes it
	- Messaging systems scale poorly as data accumulates beyond what fits in memory
	- Messaging systems generally lack robust replication features
* How Kafka is different from traditional msg queue
	- Kafka stores a persistent log which can be re-read and kept indefinitely
	- Kafka is built as a modern distributed system: it runs as a cluster, can expand or contract elastically, and replicates data internally for fault-tolerance and high-availability.
	- Kafka is built to allow real-time stream processing, not just processing of a single message at a time. This allows working with data streams at a much higher level of abstraction. 
* Whether Kafka could become a kind of universal database? No because
	- Databases are mostly about queries, Kafka does not benefit from any kind of random access against the log
	- The mission for Kafka is to make streams of data and stream processing a mainstream development paradihm. 
* Reference: [It's Okay to Store Data in Apache Kafka](https://www.confluent.io/blog/okay-store-data-apache-kafka/)

## Design priniples
### High IO throughput
#### Sequential read and pageCache
* Design: 
	- Each partition is stored sequentially on disk. Kafka storage is designed to be read / write sequentially. 
	- Rather than maintain as much as possible in-memory and flush it all out to the filesystem in a panic when we run out of space, we invert that. All data is immediately written to a persistent log on the filesystem without necessarily flushing to disk. In effect this just means that it is transferred into the kernel's pagecache.
* Reason: 
	- The key fact about disk performance is that the throughput of hard drives has been diverging from the latency of a disk seek for the last decade. As a result the performance of linear writes on a JBOD configuration with six 7200rpm SATA RAID-5 array is about 600MB/sec but the performance of random writes is only about 100k/sec—a difference of over 6000X. These linear reads and writes are the most predictable of all usage patterns, and are heavily optimized by the operating system. A modern operating system provides read-ahead and write-behind techniques that prefetch data in large block multiples and group smaller logical writes into large physical writes
	- To compensate for this performance divergence, modern operating systems have become increasingly aggressive in their use of main memory for disk caching. A modern OS will happily divert all free memory to disk caching with little performance penalty when the memory is reclaimed. All disk reads and writes will go through this unified cache. This feature cannot easily be turned off without using direct I/O, so even if a process maintains an in-process cache of the data, this data will likely be duplicated in OS pagecache, effectively storing everything twice.
	- Kafka is built on top of JVM: The memory overhead of objects is very high, often doubling the size of the data stored (or worse). Java garbage collection becomes increasingly fiddly and slow as the in-heap data increases. 
	- As a result of these factors using the filesystem and relying on pagecache is superior to maintaining an in-memory cache or other structure—we at least double the available cache by having automatic access to all free memory, and likely double again by storing a compact byte structure rather than individual objects. Doing so will result in a cache of up to 28-30GB on a 32GB machine without GC penalties. Furthermore, this cache will stay warm even if the service is restarted, whereas the in-process cache will need to be rebuilt in memory (which for a 10GB cache may take 10 minutes) or else it will need to start with a completely cold cache (which likely means terrible initial performance). This also greatly simplifies the code as all logic for maintaining coherency between the cache and filesystem is now in the OS, which tends to do so more efficiently and more correctly than one-off in-process attempts. If your disk usage favors linear reads then read-ahead is effectively pre-populating this cache with useful data on each disk read.

#### ZeroCopy
* At low message rates this is not an issue, but under load the impact is significant. To avoid this we employ a standardized binary message format that is shared by the producer, the broker, and the consumer (so data chunks can be transferred without modification between them).
* The message log maintained by the broker is itself just a directory of files, each populated by a sequence of message sets that have been written to disk in the same format used by the producer and consumer. Maintaining this common format allows optimization of the most important operation: network transfer of persistent log chunks.
* To understand the impact of sendfile, it is important to understand the common data path for transfer of data from file to socket:
	1. The operating system reads data from the disk into pagecache in kernel space
	2. The application reads the data from kernel space into a user-space buffer
	3. The application writes the data back into kernel space into a socket buffer
	4. The operating system copies the data from the socket buffer to the NIC buffer where it is sent over the network
* This is clearly inefficient, there are four copies and two system calls. Modern unix operating systems offer a highly optimized code path for transferring data out of pagecache to a socket; in Linux this is done with the sendfile system call. Using sendfile, this re-copying is avoided by allowing the OS to send the data from pagecache to the network directly. So in this optimized path, only the final copy to the NIC buffer is needed. 
* Using the zero-copy optimization above, data is copied into pagecache exactly once and reused on each consumption instead of being stored in memory and copied out to user-space every time it is read. This allows messages to be consumed at a rate that approaches the limit of the network connection.

#### Batching
* The small I/O problem happens both between the client and the server and in the server's own persistent operations.
* To avoid this, our protocol is built around a "message set" abstraction that naturally groups messages together. This allows network requests to group messages together and amortize the overhead of the network roundtrip rather than sending a single message at a time. The server in turn appends chunks of messages to its log in one go, and the consumer fetches large linear chunks at a time.
* This simple optimization produces orders of magnitude speed up. Batching leads to larger network packets, larger sequential disk operations, contiguous memory blocks, and so on, all of which allows Kafka to turn a bursty stream of random message writes into linear writes that flow to the consumers.

### The producer
#### Load balancing
* The producer controls which partition it publishes to. It sends data directly to the broker that is the leader for the partition without any intervening routing tier. 
	- Partition strategy
		* Round-robin: 
		* Randomized
		* Based on message key: or keyed This can be done at random, implementing a kind of random load balancing, or it can be done by some semantic partitioning function. 
		* Based on location: 
* To help the producer do this all Kafka nodes can answer a request for metadata about which servers are alive and where the leaders for the partitions of a topic are at any given time to allow the producer to appropriately direct its requests.

#### Compression
* In some cases the bottleneck is actually not CPU or disk but network bandwidth. This is particularly true for a data pipeline that needs to send messages between data centers over a wide-area network. Of course, the user can always compress its messages one at a time without any support needed from Kafka, but this can lead to very poor compression ratios as much of the redundancy is due to repetition between messages of the same type (e.g. field names in JSON or user agents in web logs or common string values). Efficient compression requires compressing multiple messages together rather than compressing each message individually.
* Kafka supports GZIP, Snappy, LZ4 and ZStandard compression protocols.
* Message will be compressed on producer, maintained on broker and decompressed on consumer. 

#### Push-based produer
* You could imagine other possible designs which would be only pull, end-to-end. The producer would locally write to a local log, and brokers would pull from that with consumers pulling from them. A similar type of "store-and-forward" producer is often proposed. This is intriguing but we felt not very suitable for our target use cases which have thousands of producers. Our experience running persistent data systems at scale led us to feel that involving thousands of disks in the system across many applications would not actually make things more reliable and would be a nightmare to operate. And in practice we have found that we can run a pipeline with strong SLAs at large scale without a need for producer persistence.

### The consumer
#### Pull-based consumer
* Pros:
	- A pull-based system has the nicer property that the consumer simply falls behind and catches up when it can. This can be mitigated with some kind of backoff protocol by which the consumer can indicate it is overwhelmed, but getting the rate of transfer to fully utilize (but never over-utilize) the consumer is trickier than it seems.
	- Another advantage of a pull-based system is that it lends itself to aggressive batching of data sent to the consumer. A push-based system must choose to either send a request immediately or accumulate more data and then send it later without knowledge of whether the downstream consumer will be able to immediately process it. If tuned for low latency, this will result in sending a single message at a time only for the transfer to end up being buffered anyway, which is wasteful. A pull-based design fixes this as the consumer always pulls all available messages after its current position in the log (or up to some configurable max size). So one gets optimal batching without introducing unnecessary latency.
* Cons: 
	- The deficiency of a naive pull-based system is that if the broker has no data the consumer may end up polling in a tight loop, effectively busy-waiting for data to arrive. To avoid this we have parameters in our pull request that allow the consumer request to block in a "long poll" waiting until data arrives (and optionally waiting until a given number of bytes is available to ensure large transfer sizes).

#### Consumer position
* Most messaging systems keep metadata about what messages have been consumed on the broker. 
	- Cons: Getting broker and consumer to agree about what has been consumed is not a trivival problem.
		* If the broker records a message as consumed immediately every time it is handed out over the network, then if the consumer fails to process the message (say because it crashes or the request times out or whatever) that message will be lost.
		* To solve this problem, many messaging systems add an acknowledgement feature which means that messages are only marked as sent not consumed when they are sent; the broker waits for a specific acknowledgement from the consumer to record the message as consumed.
		* This strategy fixes the problem of losing messages, but creates new problems.
			1. If the consumer processes the message but fails before it can send an acknowledgement then the message will be consumed twice.
			2. Now the broker must keep multiple states about every single message (first to lock it so it is not given out a second time, and then to mark it as permanently consumed so that it can be removed). Tricky problems must be dealt with, like what to do with messages that are sent but never acknowledged.
* Kafka keeps metadata about what messages have been consumed on the consumer group level. 
	-  The position of a consumer in each partition is just a single integer, the offset of the next message to consume. This makes the state about what has been consumed very small, just one number for each partition. This state can be periodically checkpointed.
	- A consumer can deliberately rewind back to an old offset and re-consume data. This violates the common contract of a queue, but turns out to be an essential feature for many consumers. For example, if the consumer code has a bug and is discovered after some messages are consumed, the consumer can re-consume those messages once the bug is fixed.

### Message delivery semantics
#### Message delivery
* When publishing a message we have a notion of the message being "committed" to the log. Once a published message is committed it will not be lost as long as one broker that replicates the partition to which this message was written remains "alive". 
	- Commited message: 
	- Alive partition: 
	- Types of failure attempted to handle: 

#### At least once delivery
* Kafka guarantees at-least-once delivery by default.
* It can read the messages, process the messages, and finally save its position. In this case there is a possibility that the consumer process crashes after processing messages but before saving its position. In this case when the new process takes over the first few messages it receives will already have been processed. This corresponds to the "at-least-once" semantics in the case of consumer failure. In many cases messages have a primary key and so the updates are idempotent (receiving the same message twice just overwrites a record with another copy of itself).

#### At most once delivery
* Kafka allows the user to implement at-most-once delivery by disabling retries on the producer and committing offsets in the consumer prior to processing a batch of messages.
	- It can read the messages, then save its position in the log, and finally process the messages. In this case there is a possibility that the consumer process crashes after saving its position but before saving the output of its message processing. In this case the process that took over processing would start at the saved position even though a few messages prior to that position had not been processed. This corresponds to "at-most-once" semantics as in the case of a consumer failure messages may not be processed.

#### Exactly once delivery
* Idempotent producer
	- Since 0.11.0.0, the Kafka producer also supports an idempotent delivery option which guarantees that resending will not result in duplicate entries in the log by setting "enable.idempotence" to true. To achieve this, the broker assigns each producer an ID and deduplicates messages using a sequence number that is sent by the producer along with every message.
	- Limitations of idempotent producer
		1. Only gaurantee the idempotence within a single partition
		2. Only gaurantee the idempotence within a single session (session means one life time of a producer process. If the producer restart, the idempotence will be lost)
	- Ways to guarantee idempotency within the business logic
		1. Use database unique constraint
		2. Have a prerequisite for data update operation
		3. Record and check operation to guarantee only executed once

* Transactional producer
	- Beginning with 0.11.0.0, the producer supports the ability to send messages to multiple topic partitions using transaction-like semantics: i.e. either all messages are successfully written or none of them are. The main use case for this is exactly-once processing between Kafka topics (described below).
		1. enable.idempotence = true
		2. set the parameter “transactional.id” on the producer end
	- Isolation level: 
		1. In the default "read_uncommitted" isolation level, all messages are visible to consumers even if they were part of an aborted transaction.
		2. In "read_committed," the consumer will only return messages from transactions which were committed (and any messages which were not part of a transaction).
	- Additional APIs: initTransaction / beginTransaction / commitTransaction / abortTransaction

* So effectively Kafka supports exactly-once delivery in Kafka Streams, and the transactional producer/consumer can be used generally to provide exactly-once delivery when transferring and processing data between Kafka topics. Exactly-once delivery for other destination systems generally requires cooperation with such systems, but Kafka provides the offset which makes implementing this feasible (see also Kafka Connect). 

### Replication
* 

### Log compaction

## Broker
## Controller
## Producer
## Consumer
## Storage layer
## Stream processing
