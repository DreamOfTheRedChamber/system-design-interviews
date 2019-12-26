<!-- MarkdownTOC -->

- [Message Queue](#message-queue)
	- [RocketMQ](#rocketmq)
		- [Definition](#definition)
		- [Time series data](#time-series-data)
		- [Storage model](#storage-model)
- [Delay message queue](#delay-message-queue)
	- [Redis + MySQL](#redis--mysql)
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