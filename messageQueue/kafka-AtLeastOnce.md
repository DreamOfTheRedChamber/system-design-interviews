- [Write to Kafka](#write-to-kafka)
- [In-Sync replicas](#in-sync-replicas)
  - [Def](#def)
  - [Always caught-up to leader](#always-caught-up-to-leader)
  - [Persist inside Zookeeper](#persist-inside-zookeeper)

# Write to Kafka

![](../.gitbook/assets/messageQueue_backlog_acksConfig.png)


# In-Sync replicas
## Def

* Followers consume messages from the leader just as a normal Kafka consumer would and apply them to their own log. 
* There are two types of replicas: synchronous (performance) and asynchronous (consistency) replication. 
* In-sync are defined by broker config replica.lag.time.max.ms, which means the longest duration follower replica could be behind leader replica. 


## Always caught-up to leader
* Kafka dynamically maintains a set of in-sync replicas (ISR) that are caught-up to the leader. Only members of this set are eligible for election as leader. A write to a Kafka partition is not considered committed until all in-sync replicas have received the write. 

## Persist inside Zookeeper
* This ISR set is persisted to ZooKeeper whenever it changes. Because of this, any replica in the ISR is eligible to be elected leader. This is an important factor for Kafka's usage model where there are many partitions and ensuring leadership balance is important. With this ISR model and f+1 replicas, a Kafka topic can tolerate f failures without losing committed messages.
