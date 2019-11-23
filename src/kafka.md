# Reading Kafka source code
<!-- MarkdownTOC -->

- [Use cases](#use-cases)
	- [Message broker](#message-broker)
		- [Comparison with other msg brokers](#comparison-with-other-msg-brokers)
	- [Website activity tracking](#website-activity-tracking)
	- [Metrics](#metrics)
	- [Log aggregation](#log-aggregation)
	- [Stream processing](#stream-processing)
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

### Website activity tracking
### Metrics
### Log aggregation
### Stream processing
## Design priniples
## Broker
## Controller
## Producer
## Consumer
## Storage layer
## Stream processing
