# Reading Kafka source code
<!-- MarkdownTOC -->

- [Use cases](#use-cases)
	- [Message broker](#message-broker)
		- [Comparison with other msg brokers](#comparison-with-other-msg-brokers)
	- [Stream processing](#stream-processing)
	- [Storage](#storage)
- [Design priniples](#design-priniples)
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
## Broker
## Controller
## Producer
## Consumer
## Storage layer
## Stream processing
