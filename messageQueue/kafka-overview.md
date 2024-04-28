- [History](#history)
  - [Goal](#goal)
  - [Approach V0: Cronjob upload](#approach-v0-cronjob-upload)
  - [Approach V1: Write via HDFS client](#approach-v1-write-via-hdfs-client)
  - [Approach V2: Log collector such as Scribe/Flume](#approach-v2-log-collector-such-as-scribeflume)
  - [Approach V3: Kafka to rescue](#approach-v3-kafka-to-rescue)
- [References](#references)

# History
## Goal
* Hadoop MapReduce could only process data stored on HDFS cluster. 
* Upload logs from servers to HDFS cluster.

## Approach V0: Cronjob upload
* Idea: Use Linux Cronjob to periodically upload files to HDFS
* Downside: No failure tolerance. If a machine has failures, its log will be lost. 

## Approach V1: Write via HDFS client
* Idea: Once log is being written to HDFS, it will has three copies and no lost. 
* Downsides:
  * Concurrent write pressure: 
    * Lots of concurrency pressure on HDFS. If there are 100 application servers, then there will be 100 clients writing data to HDFS 24*7. 
    * There will be concurrent write competition on HDFS chunck server because it will be queued in chunkserver. 
  * Overhead: 
    * If each server writes their own log file, then there will be large number of small log files created. 
    * For HDFS, each block will be at least 64MB and lots of small files will waste huge amount of storage space. 
    * For MapReduce, each independent file need a separate map task to read. 
* In summary, HDFS is only applicable for scenarios where there is a single client writing sequentially in large chunks. 

## Approach V2: Log collector such as Scribe/Flume
* Idea: 
  * On each server, there will be a log collector. Multiple log collectors could upload their log to log aggregator
  * There will be a tree like hierarchy where in the end only few log aggregators are writting data to HDFS. 
  * It could solve the problem that there will be large number of small files. 
* Downsides: 
  * The final aggregator will still produce a new HDFS file on a frequent basis. 
  * MapReduce tasks cannot assume that final aggregator data is already in place and need to handle the failure scenarios. 

## Approach V3: Kafka to rescue
* Architecture

![architecture](../.gitbook/assets/messageQueue_kafka_architecture.png)


# References
* [Kafka的历史](https://time.geekbang.org/column/article/464267?cid=100091101)
* [Youtube Kafka源码到面试题](https://www.youtube.com/watch?v=HLSQDk2asjY&list=PLmOn9nNkQxJEDjzl0iBYZ3WuXUuUStxZl&ab_channel=%E5%B0%9A%E7%A1%85%E8%B0%B7IT%E5%9F%B9%E8%AE%AD%E5%AD%A6%E6%A0%A1)
* [东哥IT笔记](https://donggeitnote.com/category/kafka/)
