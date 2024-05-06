- [High throughput](#high-throughput)
  - [Pull-based consumer](#pull-based-consumer)
    - [Pro 1: Automatic best batching if there is enough message](#pro-1-automatic-best-batching-if-there-is-enough-message)
    - [Pro 2: No internal states for consumer positions](#pro-2-no-internal-states-for-consumer-positions)
- [Consistency](#consistency)
  - [Pros](#pros)
  - [Cons](#cons)
- [Reliability](#reliability)
  - [Qurom based leader election](#qurom-based-leader-election)
    - [Use case](#use-case)
- [Failure handling](#failure-handling)
- [Limitations](#limitations)
  - [No transactional message](#no-transactional-message)
  - [Ordering](#ordering)
- [Details](#details)
  - [High watermark / Log end offset](#high-watermark--log-end-offset)
    - [Definition\*\*](#definition)
    - [When to update high watermark](#when-to-update-high-watermark)
    - [When to update log end offset](#when-to-update-log-end-offset)
  - [Leader epoch](#leader-epoch)
    - [Existing flaws in replica protocol](#existing-flaws-in-replica-protocol)
    - [Epoch concept](#epoch-concept)
    - [Epoch in data lose scenario](#epoch-in-data-lose-scenario)
    - [Epoch in data diverge scenario](#epoch-in-data-diverge-scenario)
    - [Unclean leader election](#unclean-leader-election)

# High throughput
## Pull-based consumer
### Pro 1: Automatic best batching if there is enough message
* A push-based system must choose to either send a request immediately or accumulate more data and then send it later without knowledge of whether the downstream consumer will be able to immediately process it. A pull-based design typically gets optimal batching when compared with push. Consumers always pull all available messages after its current position in the log (or up to some configurable max size). 
* Pull assumptions:
  * The deficiency of a naive pull-based system is that if the broker has no data the consumer may end up polling in a tight loop, effectively busy-waiting for data to arrive. To avoid this we have parameters in our pull request that allow the consumer request to block in a "long poll" waiting until data arrives (and optionally waiting until a given number of bytes is available to ensure large transfer sizes).

### Pro 2: No internal states for consumer positions
* Typically, many messaging systems add an acknowledgement feature which means that messages are only marked as sent not consumed when they are sent; the broker waits for a specific acknowledgement from the consumer to record the message as consumed.
* However, it could be a lot of internal states to keep if there are a large number of consumers. Kafka does not need to maintain an internal state to guarantee at least once delivery.
* Kafka keeps metadata about what messages have been consumed on the consumer group level. 

# Consistency
* Kafka adopted master-write master-read model
* Operation: All reads/writes go to the leader of partition. Follows only pull offsets and messages from leader. 

## Pros
1. Support "Read your writes": You could immedately use consumer api to read the msg written by producer api.
2. Support "Monotonic reads": For a single consumer client, the existence of a message will be determined.

## Cons
* Kafka does not need read replica to optimize its read performance because it is optimized by topic partition. 

# Reliability
* If we tell the client a message is committed, and the leader fails, the new leader we elect must also have that message. This yields a tradeoff: if the leader waits for more followers to acknowledge a message before declaring it committed then there will be more potentially electable leaders.

## Qurom based leader election
* Let's say we have 2f+1 replicas. If f+1 replicas must receive a message prior to a commit being declared by the leader, and if we elect a new leader by electing the follower with the most complete log from at least f+1 replicas, then, with no more than f failures, the leader is guaranteed to have all committed messages. This is because among any f+1 replicas, there must be at least one replica that contains all committed messages. That replica's log will be the most complete and therefore will be selected as the new leader    .
* Pros: This majority vote approach has a very nice property: the latency is dependent on only the fastest servers. That is, if the replication factor is three, the latency is determined by the faster follower not the slower one.
* Cons: The downside of majority vote is that it doesn't take many failures to leave you with no electable leaders. To tolerate one failure requires three copies of the data, and to tolerate two failures requires five copies of the data. 

### Use case
* Quorum based storage is not very practical for large volume data problems. This is likely why quorum algorithms more commonly appearing in shared cluster configuration such as ZooKeeper but are less common for primary data storage. For example in HDFS the namenode's high-availability feature is built on a majority-vote-based journal, but this more expensive approach is not used for the data itself.

* Implementation: ZooKeeper's Zab, Raft, and Viewstamped Replication. The most similar academic publication we are aware of to Kafka's actual implementation is PacificA from Microsoft.

# Failure handling

* When publishing a message we have a notion of the message being "committed" to the log. Once a published message is committed it will not be lost as long as one broker that replicates the partition to which this message was written remains "alive". 
  * Commited message: when all in sync replicas for that partition have applied it to their log.
  * Alive partition: liveness has two conditions
    1. A node must be able to maintain its session with ZooKeeper (via ZooKeeper's heartbeat mechanism)
    2. If it is a follower it must replicate the writes happening on the leader and not fall "too far" behind
  * Types of failure attempted to handle: Kafka only attempt to handle a "fail/recover" model of failures where nodes suddenly cease working and then later recover (perhaps without knowing that they have died). Kafka does not handle so-called "Byzantine" failures in which nodes produce arbitrary or malicious responses (perhaps due to bugs or foul play).

# Limitations
## No transactional message 
* Kafka guarantees at-least-once delivery by default.
* However, it does not maintain a state on whether message has been consumed. 

## Ordering
* There is no guarantee that if a message A is sent to a broker first, it will also be delivered to consumer first. 

# Details
## High watermark / Log end offset

### Definition**

* High watermark <= HW means all the commited messages available to consumers. 
* The offset position value for the next log

### When to update high watermark

* Follower side: Each time after follower writes log. It will take the minimum(LEO, HW in leader's response)
* Leader side: 
  1. When the replica becomes a leader
  2. When broker crashes and replica is kicked out of ISR
  3. When producer writes a message to leader replica
  4. When leader process follower's FETCh request

### When to update log end offset

* Follwer side: Each time follower writes a log 
* Leader side: 
  * Leader side stored followers' LEO: After receiving a follower FETCH request but before returns response to follower. 
  * Leader side stored leader's LEO: Each time leader writes a log

## Leader epoch
### Existing flaws in replica protocol
* The replication protocol in Kafka has two phases. Initially the the follower fetches messages. So it might fetch message m2. On the next round of RPC it will confirm receipt of message m2 and, assuming other replicas have confirmed successfully, the leader will progress the High Watermark. This is then passed back to the followers in the responses to their fetch requests. So the leader controls the progression rate of the High Watermark, which is propagated back to followers in subsequent rounds of RPC.
* The replication protocol also includes a phase where, on initialisation of a follower, the follower will truncate its log to the High Watermark it has recorded, then fetch messages from the leader. The problem is that, should that follower become leader before it has caught up, some messages may be lost due to the truncation.

### Epoch concept
* We can solve both of these issues by introducing the concept of a Leader Epoch. This allocates an identifier to a period of leadership, which is then added to each message by the leader. Each replica keeps a vector of \[LeaderEpoch => StartOffset] to mark when leaders changed throughout the lineage of its log. This vector then replaces the high watermark when followers need to truncate data (and will be stored in a file for each replica).  So instead of a follower truncating to the High Watermark, the follower gets the appropriate LeaderEpoch from the leader’s vector of past LeaderEpochs and uses this to truncate only messages that do not exist in the leader’s log. So the leader effectively tells the follower what offset it needs to truncate to.

![Kafka epoch concept](../.gitbook/assets/kafka_epoch_concept.png)

### Epoch in data lose scenario
* High watermark truncation followed by immediate leader election
  * Let’s take an example. Imagine we have two brokers A & B. B is the leader initially as in the below figure. (A) fetches message m2 from the leader (B). So the follower (A) has message m2, but has not yet got confirmation from the leader (B) that m2 has been committed (the second round of replication, which lets (A) move forward its high watermark past m2, has yet to happen). At this point the follower (A) restarts. It truncates its log to the high watermark and issues a fetch request to the leader (B). (B) then fails and A becomes the new leader. Message m2 has been lost permanently (regardless of whether B comes back or not).

![High water mark data lose scenario](../.gitbook/assets/kafka_epoch_scenario1.png)

* How does it help    
  * In this solution the follower makes a request to the leader to determine if it has any divergent epochs in its log. It sends a LeaderEpochRequest to the leader for its current LeaderEpoch. In this case the leader returns the log end offset, although if the follower was lagging by more than one Leader Epoch, the leader would return the first offset in (Follower Leader Epoch + 1). So that’s to say the LeaderEpoch response contains the offset where the requested LeaderEpoch ends.
  * In this case the LeaderEpochResponse returns offset 2. Note this is different to the high watermark which, on the follower, is offset 0. Thus the follower does not truncate any messages and hence message m2 is not lost.

![Epoch in data lose scenario](../.gitbook/assets/kafka_epoch_solution1.png)

### Epoch in data diverge scenario

* Replica divergence on restart after multiple hard failures
  * Imagine again we have two brokers, but this time we have a power outage which affects both of them. It is acceptable, in such a case, that we could lose data if n replicas are lost (Kafka guarantees durability for n-1 replicas). Unfortunately there is an opportunity for the logs, on different machines, to diverge and even, in the worst case, for replication to become stuck.
  * The underlying issue is that messages are flushed to disk asynchronously. This means, after a crash, machines can be an arbitrary number of messages behind one another. When they come back up, any one might become leader. If the leader happens to be the machine with the fewest messages in its log we’ll lose data. Whilst that is within the durability contract of the system, the issue is that the replicas can diverge, with different message lineage in different replicas.
  * As we support compressed message sets this can, at worst, lead to an inability for replicas to make progress. This happens when the offset for a compressed message set in one replica points to the midpoint of a compressed message set in another.

![High watermark data diverge scenario](../.gitbook/assets/kafka_epoch_scenario2.png)

* How does it help
  * When the two brokers restart after a crash, broker B becomes leader. It accepts message m3 but with a new Leader Epoch, LE1. Now when broker A starts, and becomes a follower, it sends a LeaderEpoch request to the leader. This returns the first offset of LE1, which is offset 1. The follower knows that m2 is orphaned and truncates it. It then fetches from offset 1 and the logs are consistent.

![Epoch in data divergence scenario](../.gitbook/assets/kafka_epoch_solution2.png)

* [Reference](https://cwiki.apache.org/confluence/display/KAFKA/KIP-101+-+Alter+Replication+Protocol+to+use+Leader+Epoch+rather+than+High+Watermark+for+Truncation)

### Unclean leader election

* Kafka's guarantee with respect to data loss is predicated on at least one replica remaining in sync. If all the nodes replicating a partition die, this guarantee no longer holds.
* If you are unlucky enough to have this occur, it is important to consider what will happen. There are two behaviors that could be implemented:
  1. Wait for a replica in the ISR to come back to life and choose this replica as the leader (hopefully it still has all its data).
  2. Choose the first replica (not necessarily in the ISR) that comes back to life as the leader.
* This is a simple tradeoff between availability and consistency. If we wait for replicas in the ISR, then we will remain unavailable as long as those replicas are down. If such replicas were destroyed or their data was lost, then we are permanently down. If, on the other hand, a non-in-sync replica comes back to life and we allow it to become leader, then its log becomes the source of truth even though it is not guaranteed to have every committed message.