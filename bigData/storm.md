- [History](#history)
- [Model](#model)
- [Architecture](#architecture)
  - [Nimbus](#nimbus)
  - [Supervisor](#supervisor)
  - [Worker](#worker)
  - [Zookeeper](#zookeeper)
- [Fault tolerant (At least once)](#fault-tolerant-at-least-once)
  - [AckerBolt](#ackerbolt)
  - [XOR](#xor)
- [Example ads stream architecture](#example-ads-stream-architecture)
  - [Ads model](#ads-model)
  - [Tasks](#tasks)
  - [Architecture](#architecture-1)
    - [AdsCtrBolt](#adsctrbolt)
    - [ClientSpentBolt](#clientspentbolt)
- [NonFunc requirement](#nonfunc-requirement)
  - [Exactly once delivery](#exactly-once-delivery)
    - [Example conditions that will trigger duplicated messages:](#example-conditions-that-will-trigger-duplicated-messages)
    - [Dedupe with bloomfilter](#dedupe-with-bloomfilter)
  - [Fault tolerant (In-memory machine reboot)](#fault-tolerant-in-memory-machine-reboot)
  - [Time window accuracy](#time-window-accuracy)
    - [Processing time instead of event time](#processing-time-instead-of-event-time)
    - [Use cases need event time](#use-cases-need-event-time)
    - [Challenges in using the event time](#challenges-in-using-the-event-time)
      - [Maintain two instead of one level mapping](#maintain-two-instead-of-one-level-mapping)
      - [When to persist memory data](#when-to-persist-memory-data)
- [Real world](#real-world)

# History
* Storm is an improvements on Yahoo S4. It solved the following pain points:
  * Yahoo S4 will create a huge number of PE, consuming huge number of memory and GC cost. 
  * Yahoo S4 needs to embed data distribution logic into business logic layer. 

# Model
* Spout: Data source. 
* Tuple: The minimum unit for data transmission. A key, value pair. 
* Streams: A stream contain huge number of tuples. 
* Bolts: The place where business logic is calculated. 

# Architecture

![](../.gitbook/assets/storm_architecture.png)

![](../.gitbook/assets/storm_architecture_analogy.png)

## Nimbus
* Master node in cluster. Resource manager and job scheduler. 

## Supervisor
* Receive jobs from Nimus. 
* Monitor whether workers are alive. 
* Assign jobs to workers. 

## Worker
* Each worker process is an independent JVM. 

## Zookeeper
* Nimbus write corresponding tasks to Zookeeper for durability and high availability. 

# Fault tolerant (At least once)

![](../.gitbook/assets/storm_faultolerant_tupleTree.png)

## AckerBolt
* When Spout sends out a message, it will also notify AckerBolt. 
* Once Bolt finished processing root tuple, it will notify AckerBolt.
* Bolt will tell AckerBolt two pieces of information:
  * It has finished processing a tuple. 
  * What derivative downstream tuples it has already sent out. 
* Last layer bolt will notify that there are no additional tuples. 

## XOR
* It could only guarantee that each tuple sent out by spout is processed at least once. 

![](../.gitbook/assets/storm_faulttolerant_xor.png)

# Example ads stream architecture 
## Ads model
* Each log entry represents an ad display: Ad location + Ad customer location + Ad ID
* Event type: Impression means display, or click
* UID: 
* Event ID: Unique ID for retry. 
* Last timestamp: 

![](../.gitbook/assets/storm_ads_model.png)

## Tasks
1. Calculate realtime ads fee for each customer. 
2. Calculate ads click rate. For an ad with low click rate, stop ad display. 

## Architecture
1. KafkaSpout which pulls log from Kafka, parse segments and send to downstream bolt. 
2. For each log from Kafka spout, it will send to the following two types of bolts: AdsCounterBolt and ClientSpentBolt
3. AckerBolt guarantees that each message is processed at least once. 

![](../.gitbook/assets/storm_ads_architecture.png)

### AdsCtrBolt
* AdsCtrBolt: Calculate click rate for different type of ads
* Maintains an in-memory map of Ads ID => (Num of display, Num of click, AD cost) and update to HBase every minute. 

### ClientSpentBolt
* ClientSpentBolt: Calculate cost for each client and update HBase
* Update the cost data in HBase according to a higher frequency, even update HBase with each click. 

# NonFunc requirement
## Exactly once delivery
### Example conditions that will trigger duplicated messages: 
* A KafkaSpout has hardware failure and need to reboot
  * The offset in Kafka is stored inside Zookeeper. 
  * Zookeeper is designed as a coarse distributed lock, not as a high-throughput KV so usually only a batch offset update will be recorded by Zookeeper. 
  * A KafkaSpout reboot usually indicates that a large number of messages need to be resent. 
* A ClientSpentBolt has high latency when write message. As a result of Storm AckerBolt, it will resend the message. 

![](../.gitbook/assets/storm_update_by_spout.png)

### Dedupe with bloomfilter
* Put all message id inside this bloomfilter.
* Parition the global bloomfilter into multiple time window. 
  * Typically for a resend, it won't span beyond 30 minutes. 

![](../.gitbook/assets/storm_update_by_spout_bloomFilter.png)

## Fault tolerant (In-memory machine reboot)
* There are some states maintained inside memory. Take the example above, AdsCtrBolt maintains an in-memory map of Ads ID => (Num of display, Num of click, AD cost). 
* To make scaling easier, these in-memory states need to be persisted in an external datastore. 

![](../.gitbook/assets/storm_fault_tolerant.png)

## Time window accuracy
### Processing time instead of event time
* Use the example of calculating ads click rate every min, Storm realizes it by TickTuple. 
  * Storm will send a signal to both bolt and spout according to the specified timestamp. 
  * However, here we are using the timestamp where message is transmitted to bolts/spouts instead of when the ad click happen. 
  * In other words, Storm is using processing time instead of event time. 

### Use cases need event time
* Advertisment click: If a customer does not allocate any budget for December, an ad click happens on 11:59:59 11/30. 
* Replay logs: In some analytics cases, kafka log replay is needed

### Challenges in using the event time
* If using the event time to replace the processing time, there will be some challenges.
* Use the example of calculating ads click rate every min, 

#### Maintain two instead of one level mapping
* A time window bounded mapping needed to be maintained. 
  * Instead of Ads ID => (Num of display, Num of click, AD cost),
  * [TimeWindow1: Ads ID => (Num of display, Num of click, AD cost)], ..., [TimeWindowN: Ads ID => (Num of display, Num of click, AD cost)]

#### When to persist memory data
* The log sent from upstream is not strictly ordered by timestamp
  * After persisting data in one time window, if another record in that time window comes again, how will we respond to that (Discard / or read data from DB and update)

# Real world

* [Storm near real time](https://www.michael-noll.com/blog/2013/01/18/implementing-real-time-trending-topics-in-storm/)
