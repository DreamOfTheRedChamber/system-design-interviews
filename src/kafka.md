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
	- [The consumer](#the-consumer)
		- [Push vs pull](#push-vs-pull)
		- [Consumer position](#consumer-position)
		- [Offline data load](#offline-data-load)
		- [Static membership](#static-membership)
	- [Message delivery semantics](#message-delivery-semantics)
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

### The consumer
#### Push vs pull
#### Consumer position
#### Offline data load
#### Static membership

### Message delivery semantics
### Replication
### Log compaction

## Broker
## Controller
## Producer
## Consumer
## Storage layer
## Stream processing
