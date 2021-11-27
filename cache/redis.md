
- [Implement a cache system on a single machine](#implement-a-cache-system-on-a-single-machine)
  - [Server](#server)
  - [Client](#client)
  - [Processing logic](#processing-logic)
  - [Interaction modes between server and client](#interaction-modes-between-server-and-client)
  - [Expiration strategy](#expiration-strategy)
  - [Persistence options](#persistence-options)
- [Implement a cache system on a distributed scale](#implement-a-cache-system-on-a-distributed-scale)
  - [Consistency - Replication and failover](#consistency---replication-and-failover)
  - [Availability - Sentinel](#availability---sentinel)
  - [Scalability Redis cluster](#scalability-redis-cluster)
  - [Application Components:](#application-components)


# Implement a cache system on a single machine

## Server

## Client

## Processing logic

**Event**

**File Event**

**Time event**

**I/O multi-plexing**

## Interaction modes between server and client

**RESP**

**Pipeline**

* Definition: A Request/Response server can be implemented so that it is able to process new requests even if the client didn't already read the old responses. This way it is possible to send multiple commands to the server without waiting for the replies at all, and finally read the replies in a single step.
* Benefits:
  * Reduce latency because many round trip times are avoided
  * Improves a huge amount of total operations you could perform per second on a single redis server because it avoids many context switch. From the point of view of doing the socket I/O, this involves calling the read\(\) and write\(\) syscall, that means going from user land to kernel land. The context switch is a huge speed penalty. More detailed explanation: \(why a busy loops are slow even on the loopback interface?\) processes in a system are not always running, actually it is the kernel scheduler that let the process run, so what happens is that, for instance, the benchmark is allowed to run, reads the reply from the Redis server \(related to the last command executed\), and writes a new command. The command is now in the loopback interface buffer, but in order to be read by the server, the kernel should schedule the server process \(currently blocked in a system call\) to run, and so forth. So in practical terms the loopback interface still involves network-alike latency, because of how the kernel scheduler works. 
  * [https://redis.io/topics/pipelining](https://redis.io/topics/pipelining)
* Usage:
  * Under transaction commands such as MULTI, EXEC
  * The commands which take multiple arguments: MGET, MSET, HMGET, HMSET, RPUSH/LPUSH, SADD, ZADD
  * [https://redislabs.com/ebook/part-2-core-concepts/chapter-4-keeping-data-safe-and-ensuring-performance/4-5-non-transactional-pipelines/](https://redislabs.com/ebook/part-2-core-concepts/chapter-4-keeping-data-safe-and-ensuring-performance/4-5-non-transactional-pipelines/)
* Internal: Pipeline is purely a client-side implementation. 
  * Buffer the redis commands/operations on the client side
  * Synchronously or asynchronously flush the buffer periodically depending on the client library implementation.
  * Redis executes these operations as soon as they are received at the server side. Subsequent redis commands are sent without waiting for the response of the previous commands. Meanwhile, the client is generally programmed to return a constant string for every operation in the sequence as an immediate response
  * The tricky part: the final responses. Very often it is wrongly interpreted that all the responses are always read at one shot and that the responses maybe completely buffered on server's side. Even though the response to all the operations seem to arrive at one shot when the client closes the pipeline, it is actually partially buffered on both client and the server. Again, this depends on the client implementation. There does exist a possibility that the client reads the buffered response periodically to avoid a huge accumulation on the server side. But it is also possible that it doesn't. For example: the current implementation of the Jedis client library reads all responses of a pipeline sequence at once. 
  * [http://blog.nachivpn.me/2014/11/redis-pipeline-explained.html](http://blog.nachivpn.me/2014/11/redis-pipeline-explained.html)
* Pipeline vs transaction:
  * Whether the commands are executed atomically
* How does stackexchange implements pipelines: 
  * [https://stackexchange.github.io/StackExchange.Redis/PipelinesMultiplexers.html](https://stackexchange.github.io/StackExchange.Redis/PipelinesMultiplexers.html)

**Transaction**

**Script mode**

**PubSub model**

## Expiration strategy

**History**

* Lazy free
  * Timer function and perform the eviction. Difficulties: Adaptive speed for freeing memory. Found an adaptive strategy based on the following two standards: 1. Check the memory tendency: it is raising or lowering? In order to adapt how aggressively to free. 2. Also adapt the timer frequency itself based on “1”, so that we don’t waste CPU time when there is little to free, with continuous interruptions of the event loop. At the same time the timer could reach ~300 HZ when really needed.
  * For the above strategy, during busy times it only serves 65% QPS. However, the internal Redis design is heavily geared towards sharing objects around. Many data structures within Redis are based on the shared object structure robj. As an effort, the author changed it to SDS. 
* [http://antirez.com/news/93](http://antirez.com/news/93)

**Types**

* Timing deletion: While setting the expiration time of the key, create a timer. Let the timer immediately perform the deletion of the key when the expiration time of the key comes.
* Inert deletion: Let the key expire regardless, but every time the key is retrieved from the key space, check whether the key is expired, if it is expired, delete the key; if it is not expired, return the key.
* Periodic deletion: Every once in a while, the program checks the database and deletes the expired keys. How many expired keys to delete and how many databases to check are determined by the algorithm.
* Inert deletion and periodic deletion are used within Redis. 
  * Inert deletion \(expireIfNeeded function\) serves as a filter before executing any key operation
  * Periodic deletion of key occurs in Redis’s periodic execution task \(server Cron, default every 100ms\), and is the master node where Redis occurs, because slave nodes synchronize to delete key through the DEL command of the primary node. Each DB is traversed in turn \(the default configuration number is 16\). For each db, 20 keys \(ACTIVE\_EXPIRE\_CYCLE\_LOOKUPS\_PER\_LOOP\) are selected randomly for each cycle to determine whether they are expired. If the selected keys in a round are less than 25% expired, the iteration is terminated. In addition, if the time limit is exceeded, the process of expired deletion is terminated.
* [https://developpaper.com/an-in-depth-explanation-of-key-expiration-deletion-strategy-in-redis/](https://developpaper.com/an-in-depth-explanation-of-key-expiration-deletion-strategy-in-redis/)

**Commands**

* Proactive commands: Unlink, FlushAll Async, FlushDB Async
* Reactive commands: 
  * Slve-lazy-flush: Clear data options after slave receives RDB files
  * Lazy free-lazy-eviction: full memory ejection option
  * Lazy free-lazy-expire: expired key deletion option
  * Lazyfree-lazy-server-del: Internal deletion options, such as rename oldkey new key, need to be deleted if new key exists

**Eviction options**

* LRU vs LFU
* [https://redis.io/topics/lru-cache](https://redis.io/topics/lru-cache)
* Improve LRU cache algorithm [http://antirez.com/news/109](http://antirez.com/news/109)

## Persistence options

**COW**

* Both RDB and AOF relies on Unix Copy on Write mechanism
* [http://oldblog.antirez.com/post/a-few-key-problems-in-redis-persistence.html](http://oldblog.antirez.com/post/a-few-key-problems-in-redis-persistence.html)

**Pros and Cons between RDB and AOF**

* [https://redis.io/topics/persistence](https://redis.io/topics/persistence)

**RDB**

* Command: SAVE vs BGSAVE. Whether a child process is forked to create RDB file. 
* BGSAVE - Automatic save condition
  * saveparam format: save   seconds   changes
  * dirty attribute: How many databse operations have been performed after the last time.
  * lastsave: A unix timestamp - the last time the server executes SAVE or BGSAVE. 

```text
def serverCron():
    for saveParam in server.saveparams:
        save_internal = unixtime_now() - server.lastsave

        if server.dirty >= saveparam.changes 
            and save_internal > saveparams.seconds:
                BGSAVE()
```

**AOF**

* FlushAppendOnlyFile's behavior depends on appendfsync param:
  * always: Write aof\_buf to AOF file and sync to slaves on each file event loop.
  * everysec: Write aof\_buf to AOF file on each file event loop. If the last synchronization happens before 1 sec, then sync to slaves on each file event loop. 
  * no: Write aof\_buf to AOF file on each file event loop. Depend on the OS to determine when to sync.
* AOF rewrite:
  * Goal: Reduce the size of AOF file.
  * AOF rewrite doesn't need to read the original AOF file. It directly reads from database. 
  * Redis fork a child process to execute AOF rewrite dedicatedly. Redis opens a AOF rewrite buffer to keep all the instructions received during the rewriting process. At the end of rewriting AOF file, all instructions within AOF rewrite buffer will be flushed to the new AOF file. 

# Implement a cache system on a distributed scale

## Consistency - Replication and failover

## Availability - Sentinel

**Definition**

* Sentinel is Redis' resiliency solution to standalone redis instance. 
  * See [Compare redis deployments](https://blog.octo.com/en/what-redis-deployment-do-you-need/) for details.
* It will monitor your master & slave instances, notify you about changed behaviour, handle automatic failover in case a master is down and act as a configuration provider, so your clients can find the current master instance.
* Redis sentinel was born in 2012 and first released when Redis 2.4 was stable. 

**Advanced concepts**

* Initialization: Sentinel is a redis server running on a special mode
  * Sentinel will not load RDB or AOF file.
  * Sentinel will load a special set of Sentinel commands.
* Downstate
  * Subjective downstate: An SDOWN condition is reached when it does not receive a valid reply to PING requests for the number of seconds specified in the configuration as is-master-down-after-milliseconds parameter.
  * Objectively downstate: When enough Sentinels \(at least the number configured as the quorum parameter of the monitored master\) have an SDOWN condition, and get feedback from other Sentinels using the SENTINEL is-master-down-by-addr command.
* Sentinels and slaves auto discovery
  * You don't need to configure a list of other Sentinel addresses in every Sentinel instance you run, as Sentinel uses the Redis instances Pub/Sub capabilities in order to discover the other Sentinels that are monitoring the same masters and slaves. Similarly you don't need to configure what is the list of the slaves attached to a master, as Sentinel will auto discover this list querying Redis.
  * Process
    * Every Sentinel publishes a message to every monitored master and slave Pub/Sub channel **sentinel**:hello, every two seconds, announcing its presence with ip, port, runid.
    * Every Sentinel is subscribed to the Pub/Sub channel **sentinel**:hello of every master and slave, looking for unknown sentinels. When new sentinels are detected, they are added as sentinels of this master.
    * Hello messages also include the full current configuration of the master. If the receiving Sentinel has a configuration for a given master which is older than the one received, it updates to the new configuration immediately.
* Slave selection: Relies on [Raft protocol](http://thesecretlivesofdata.com/raft/)
  * The slave selection process evaluates the following information about slaves:
    * Disconnection time from the master.
    * Slave priority.
    * Replication offset processed.
    * Run ID.
  * A slave that is found to be disconnected from the master for more than ten times the configured master timeout \(down-after-milliseconds option\), plus the time the master is also not available from the point of view of the Sentinel doing the failover, is considered to be not suitable for the failover and is skipped.
  * The slave selection only considers the slaves that passed the above test, and sorts it based on the above criteria, in the following order.
    * The slaves are sorted by slave-priority as configured in the redis.conf file of the Redis instance. A lower priority will be preferred.
    * If the priority is the same, the replication offset processed by the slave is checked, and the slave that received more data from the master is selected.
    * If multiple slaves have the same priority and processed the same data from the master, a further check is performed, selecting the slave with the lexicographically smaller run ID. Having a lower run ID is not a real advantage for a slave, but is useful in order to make the process of slave selection more deterministic, instead of resorting to select a random slave.
* Sentinel's server cron operations:
  * Detect instance's objective and subjective downstate by sending PING commands
  * Automatically discover sentinel and slave nodes by subscribing to channel **sentinel**:hello
  * Leader selection and failover

**Algorithms and internals**

* Quorum
  * Use cases: Considering a master as objectively downstate; Authorizing the failover process
  * Quorum could be used to tune sentinel in two ways:
    * If a the quorum is set to a value smaller than the majority of Sentinels we deploy, we are basically making Sentinel more sensible to master failures, triggering a failover as soon as even just a minority of Sentinels is no longer able to talk with the master.
    * If a quorum is set to a value greater than the majority of Sentinels, we are making Sentinel able to failover only when there are a very large number \(larger than majority\) of well connected Sentinels which agree about the master being down.
* Configuratin epochs
  * Epoch is similar to Raft algorithm's term.
  * When a Sentinel is authorized, it gets a unique configuration epoch for the master it is failing over. This is a number that will be used to version the new configuration after the failover is completed. Because a majority agreed that a given version was assigned to a given Sentinel, no other Sentinel will be able to use it.
* [https://redis.io/topics/sentinel](https://redis.io/topics/sentinel)

## Scalability Redis cluster

**Main properties and rational of the design**

**Redis cluster goals**

* High performance and linear scalability to 1000 nodes
  * No proxies
  * Asynchronous replication
  * No merge operations on values
* Acceptable degree of write safety
  * The system tries to retain all writes originating from clients
* Availability: 
  * Redis Cluster is able to survive partitions where the majority of the master nodes are reachable and there is at least one reachable slave for every master node that is no longer reachable.
  * Moreover using replicas migration, masters no longer replicated by any slave will receive one from a master which is covered by multiple slaves.

**Implemented subset**

* Same as standalone redis: Single key operations and multi-key operations
* Not supported: multi-database operations such as SELECT
* Additional supported: Hashtag which forces keys to be stored in the same slot

**Clients and Servers roles in the Redis Cluster protocol**

* Server: Holding the data, taking state of the cluster \(Including mapping keys to the correct shard\), auto-discover nodes, detect non-working nodes and promoting slaves to master when needed
  * Servers communicate to each other using a TCP bus and a binary protocol, called Redis Cluster Bus. 
* Client: The clients is free to send requests to any node within the cluster. 

**Write safety**

* There are two cases where write will fail
  * Clients write to master. While the master may be able to reply to the clients, the write may not be propogated to slaves and the master dies. As a result of master becomes unreacheable beyond a fixed amount of time, one of its slave is promoted.
  * A master becomes unavailable because of network partitions and failed over by ones of its slaves. The clients  continues talking to the old master. However, it is unlikely to happen because:
    1. Master which fails to communicate with majority of nodes will reject writes and after the partition heals writes are still rejected to allow other nodes informing configuration changes. 
    2. The clients haven't updated its routing table. 
    3. Writes to the minority of a cluster has a higher chance of being lost.

**Availability**

* Redis Cluster is designed to survive failures of a few nodes in the cluster, but it is not a suitable solution for applications that require availability in the event of large net splits.

**Redis cluster main components**

**Key distribution model**

* HASH\_SLOT = CRC16\(key\) mod 16384
* Hashtag could force multiple keys are allocated in the same slots and it is used to implement multi-key operations in redis cluster ???

**Cluster node attributes**

* The node ID, IP and port of the node, a set of flags, what is the master of the node if it is flagged as slave, last time the node was pinged and the last time the pong was received, the current configuration epoch of the node \(explained later in this specification\), the link state and finally the set of hash slots served.
* [https://redis.io/commands/cluster-nodes](https://redis.io/commands/cluster-nodes)

**Message and implementation**

* MEET/PING/PONG: Implemented using Gossip protocol. ???
* FAIL: Broadcast because Gossip Protocol takes time.
* PUBLISH: When client sends a Publish command to the node, the node will publish this message to the channel.  

**Redirection and resharding**

**Resharding condition**

* To add a new node to the cluster an empty node is added to the cluster and some set of hash slots are moved from existing nodes to the new node.
* To remove a node from the cluster the hash slots assigned to that node are moved to other existing nodes.
* To rebalance the cluster a given set of hash slots are moved between nodes.
* All the above three conditions could be abstracted as moving slots between different shards. 

**Resharding commands**

* CLUSTER ADDSLOTS slot1 \[slot2\] ... \[slotN\]
* CLUSTER DELSLOTS slot1 \[slot2\] ... \[slotN\]
* CLUSTER SETSLOT slot NODE node
* CLUSTER SETSLOT slot MIGRATING node
* CLUSTER SETSLOT slot IMPORTING node

**Resharding internals**

1. redis-trib sends target node CLUSTER SETSLOT $slot IMPORTING $source\_id so that target node is prepared to import key value pairs from slot. 
   * On the node side, there is a bitmap 

```text
typedef struct clusterState
{
    // ...
    clusterNode *importing_slots_from[16384];

    // ...
}
```

1. redis-trib sends source node CLUSTER SETSLOT $slot MIGRATING $target\_id so that source node is prepared to migrate key value pairs to slot.
   * On the node side, there is a bitmap

```text
typedef struct clusterState
{
    // ...
    clusterNode *migrating_slots_to[16384];

    // ...
}
```

1. redis-trib sends source node CLUSTER GETKEYSINSLOT $slot $count to get at most count number of key names belonging to slot.
2. for every key name obtained in step 3, redis-trib will send source node a MIGRATE $target\_ip $target\_port $key\_name 0 $time\_out command to migrate the slots from source to dest node.
3. Repeat step 3 and 4 until all key-value pairs belong to the slots have been migrated.
4. redis-trib sends CLUSTER SETSLOT $slot NODE $target\_id which will be broadcasted to all the nodes within the cluster.

**Move and Ask redirection**

* MOVED means that we think the hash slot is permanently served by a different node and the next queries should be tried against the specified node, ASK means to send only the next query to the specified node.
* ASK semantics for client:
  * If ASK redirection is received, send only the query that was redirected to the specified node but continue sending subsequent queries to the old node.
  * Start the redirected query with the ASKING command.
  * Don't yet update local client tables to map hash slot 8 to B.
* ASK semantics for server:
  * If the client has flag REDIS\_ASKING and clusterStates\_importing\_slots\_from\[i\] shows node is importing key value i, then node will execute the the client command once. 

**Smart client**

* Redis Cluster clients should try to be smart enough to memorize the slots configuration. However this configuration is not required to be up to date. Since contacting the wrong node will simply result in a redirection, that should trigger an update of the client view.
* Clients usually need to fetch a complete list of slots and mapped node addresses in two different situations:
  * At startup in order to populate the initial slots configuration.
  * When a MOVED redirection is received.

**Fault tolerance**

**Heartbeat and gossip messages**

* Usually nodes send ping packets that will trigger the receivers to reply with pong packets. However this is not necessarily true. 
* Every node makes sure to ping every other node that hasn't sent a ping or received a pong for longer than half the NODE\_TIMEOUT time.
  * For example in a 100 node cluster with a node timeout set to 60 seconds, every node will try to send 99 pings every 30 seconds, with a total amount of pings of 3.3 per second. Multiplied by 100 nodes, this is 330 pings per second in the total cluster.

**Failure detection**

* When a PFAIL could be escalated to FAIL
* The conditions when FAIL conditions could be cleared.
* Eventually all the nodes should agree about the state of a given node.

**PFAIL to FAIL state**

* PFAIL: PFAIL means Possible failure, and is a non-acknowledged failure type. A node flags another node with the PFAIL flag when the node is not reachable for more than NODE\_TIMEOUT time. Both master and slave nodes can flag another node as PFAIL, regardless of its type. 
* FAIL: FAIL means that a node is failing and that this condition was confirmed by a majority of masters within a fixed amount of time. 
* PFAIL =&gt; FAIL: A PFAIL condition is escalated to a FAIL condition when the following set of conditions are met:
  * Some node, that we'll call A, has another node B flagged as PFAIL.
  * Node A collected, via gossip sections, information about the state of B from the point of view of the majority of masters in the cluster.
  * The majority of masters signaled the PFAIL or FAIL condition within NODE\_TIMEOUT \* FAIL\_REPORT\_VALIDITY\_MULT time. \(The validity factor is set to 2 in the current implementation, so this is just two times the NODE\_TIMEOUT time\).
* FAIL =&gt; Normal: FAIL flag can only be cleared in the following situations:
  * The node is already reachable and is a slave. In this case the FAIL flag can be cleared as slaves are not failed over.
  * The node is already reachable and is a master not serving any slot. In this case the FAIL flag can be cleared as masters without slots do not really participate in the cluster and are waiting to be configured in order to join the cluster.
  * The node is already reachable and is a master, but a long time \(N times the NODE\_TIMEOUT\) has elapsed without any detectable slave promotion. It's better for it to rejoin the cluster and continue in this case. 

**Weak agreement**

* PFAIL =&gt; FAIL is a week agreement because:
  * Nodes collect views of other nodes over some time period, so even if the majority of master nodes need to agree, actually this is just state that we collected from different nodes at different times and we are not sure, nor we require, that at a given moment the majority of masters agreed.
  * While every node detecting the FAIL condition will force that condition on other nodes in the cluster using the FAIL message, there is no way to ensure the message will reach all the nodes. For instance a node may detect the FAIL condition and because of a partition will not be able to reach any other node.
* PFAIL =&gt; FAIL is an eventually consistency agreement because:
  * Eventually all the nodes should agree about the state of a given node. There are two cases that can originate from split brain conditions. Either some minority of nodes believe the node is in FAIL state, or a minority of nodes believe the node is not in FAIL state. In both the cases eventually the cluster will have a single view of the state of a given node:
  * Case 1: If a majority of masters have flagged a node as FAIL, because of failure detection and the chain effect it generates, every other node will eventually flag the master as FAIL, since in the specified window of time enough failures will be reported.
  * Case 2: When only a minority of masters have flagged a node as FAIL, the slave promotion will not happen \(as it uses a more formal algorithm that makes sure everybody knows about the promotion eventually\) and every node will clear the FAIL state as per the FAIL state clearing rules above \(i.e. no promotion after N times the NODE\_TIMEOUT has elapsed\).

**FAIL propogation**

* The FAIL message will force every receiving node to mark the node in FAIL state, whether or not it already flagged the node in PFAIL state.

**Configuration handling, propogation and failovers**

**Cluster current epoch**

* currentEpoch lifetime
  * At node creation every Redis Cluster node, both slaves and master nodes, set the currentEpoch to 0.
  * Every time a packet is received from another node, if the epoch of the sender \(part of the cluster bus messages header\) is greater than the local node epoch, the currentEpoch is updated to the sender epoch.
  * Because of these semantics, eventually all the nodes will agree to the greatest configEpoch in the cluster.
* currentEpoch use case
  * Currently this happens only during slave promotion, as described in the next section. Basically the epoch is a logical clock for the cluster and dictates that given information wins over one with a smaller epoch.

**Configuration epoch**

* configEpoch lifetime
  * The configEpoch is set to zero in masters when a new node is created.
  * A new configEpoch is created during slave election. Slaves trying to replace failing masters increment their epoch and try to get authorization from a majority of masters. When a slave is authorized, a new unique configEpoch is created and the slave turns into a master using the new configEpoch.
* configEpoch use case
  * configEpoch helps to resolve conflicts when different nodes claim divergent configurations \(a condition that may happen because of network partitions and node failures\).

**Slave election and promotion**

1. Condition to start the election
   * The slave's master is in FAIL state. As soon as a master is in FAIL state, a slave waits a short period of time before trying to get elected. That delay is computed as follows:
     * DELAY = 500 milliseconds + random delay between 0 and 500 milliseconds + SLAVE\_RANK \* 1000 milliseconds.
     * The fixed delay ensures that we wait for the FAIL state to propagate across the cluster, otherwise the slave may try to get elected while the masters are still unaware of the FAIL state, refusing to grant their vote.
     * The random delay is used to desynchronize slaves so they're unlikely to start an election at the same time.
     * The SLAVE\_RANK is the rank of this slave regarding the amount of replication data it has processed from the master. Slaves exchange messages when the master is failing in order to establish a \(best effort\) rank: the slave with the most updated replication offset is at rank 0, the second most updated at rank 1, and so forth. In this way the most updated slaves try to get elected before others.
   * The master was serving a non-zero number of slots.
   * The slave replication link was disconnected from the master for no longer than a given amount of time, in order to ensure the promoted slave's data is reasonably fresh. This time is user configurable.
2. A slave increments its currentEpoch counter, and requests votes from master instances.
   * Votes are requested by the slave by broadcasting a FAILOVER\_AUTH\_REQUEST packet to every master node of the cluster. Then it waits for a maximum time of two times the NODE\_TIMEOUT for replies to arrive \(but always for at least 2 seconds\).
   * Once the slave receives ACKs from the majority of masters, it wins the election. Otherwise if the majority is not reached within the period of two times NODE\_TIMEOUT \(but always at least 2 seconds\), the election is aborted and a new one will be tried again after NODE\_TIMEOUT \* 4 \(and always at least 4 seconds\).
3. A master grant the vote if the following conditions are met
   * A master only votes a single time for a given epoch, and refuses to vote for older epochs: every master has a lastVoteEpoch field and will refuse to vote again as long as the currentEpoch in the auth request packet is not greater than the lastVoteEpoch. When a master replies positively to a vote request, the lastVoteEpoch is updated accordingly, and safely stored on disk.
   * A master votes for a slave only if the slave's master is flagged as FAIL.
   * Auth requests with a currentEpoch that is less than the master currentEpoch are ignored. Because of this the master reply will always have the same currentEpoch as the auth request. If the same slave asks again to be voted, incrementing the currentEpoch, it is guaranteed that an old delayed reply from the master can not be accepted for the new vote.
4. Once a master has voted for a given slave, replying positively with a FAILOVER\_AUTH\_ACK, it can no longer vote for   another slave of the same master for a period of NODE\_TIMEOUT \* 2. In this period it will not be able to reply to other authorization requests for the same master.
   * A slave discards any AUTH\_ACK replies with an epoch that is less than the currentEpoch at the time the vote request was sent. This ensures it doesn't count votes intended for a previous election.
5. Once a slave wins the election, it obtains a new unique and incremental configEpoch which is higher than that of any other existing master. It starts advertising itself as master in ping and pong packets, providing the set of served slots with a configEpoch that will win over the past ones.

**Hash slots configuration propagation**

* Two types of messages 
  * Heartbeat messages: The sender of a ping or pong packet always adds information about the set of hash slots it \(or its master, if it is a slave\) serves.
  * Update messages: Since in every heartbeat packet there is information about the sender configEpoch and set of hash slots served, if a receiver of a heartbeat packet finds the sender information is stale, it will send a packet with new information, forcing the stale node to update its info.
* Rules to update configuration
  * Rule 1: If a hash slot is unassigned \(set to NULL\), and a known node claims it, I'll modify my hash slot table and associate the claimed hash slots to it.
  * Rule 2: If a hash slot is already assigned, and a known node is advertising it using a configEpoch that is greater than the configEpoch of the master currently associated with the slot, I'll rebind the hash slot to the new node.
* Rules to choose configuration
  * So the actual Redis Cluster node role switch rule is: A master node will change its configuration to replicate \(be a slave of\) the node that stole its last hash slot.

**Replica migration**

* Def: Replica migration is the process of automatic reconfiguration of a slave in order to migrate to a master that has no longer coverage \(no working slaves\).
* Algorithm:
  1. To start we need to define what is a good slave in this context: a good slave is a slave not in FAIL state from the point of view of a given node.
  2. The execution of the algorithm is triggered in every slave that detects that there is at least a single master without good slaves. 
     * However among all the slaves detecting this condition, only a subset should act. The subset is usually a single slave. The acting slave is the slave among the masters with the maximum number of attached slaves, that is not in FAIL state and has the smallest node ID.
  3. If the race happens in a way that will leave the ceding master without slaves, as soon as the cluster is stable again the algorithm will be re-executed again and will migrate a slave back to the original master.
  4. The algorithm is controlled by a user-configurable parameter called cluster-migration-barrier: the number of good slaves a master must be left with before a slave can migrate away. For example, if this parameter is set to 2, a slave can try to migrate only if its master remains with two working slaves.

**ConfigEpoch conflicts resolution algorithm**

1. IF a master node detects another master node is advertising itself with the same configEpoch.
2. AND IF the node has a lexicographically smaller Node ID compared to the other node claiming the same configEpoch.
3. THEN it increments its currentEpoch by 1, and uses it as the new configEpoch.

## Application Components:

**Bulkhead**

* Thread Isolation: The standard approach is to hand over all requests to component C to a separate thread pool with a fixed number of threads and no \(or a small\) request queue.
  * Drawbacks: The primary drawback of thread pools is that they add computational overhead. Each command execution involves the queueing, scheduling, and context switching involved in running a command on a separate thread.
    * Costs of threads: At the 90th percentile there is a cost of 3ms for having a separate thread; At the 99th percentile there is a cost of 9ms for having a separate thread.
  * Advantages: The advantage of the thread pool approach is that requests that are passed to C can be timed out, something that is not possible when using semaphores.
* Semaphore Isolation: The other approach is to have all callers acquire a permit \(with 0 timeout\) before requests to C. If a permit can't be acquired from the semaphore, calls to C are not passed through.
* Further references
  * [https://stackoverflow.com/questions/34519/what-is-a-semaphore](https://stackoverflow.com/questions/34519/what-is-a-semaphore)
  * [A little book about semaphore](http://greenteapress.com/semaphores/LittleBookOfSemaphores.pdf)
  * [https://github.com/Netflix/Hystrix/wiki/How-it-Works](https://github.com/Netflix/Hystrix/wiki/How-it-Works)

**Distributed locking**

* Please see [Distributed lock](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/distributedLock.md)

**Pubsub**

**Properties of pubsub**

* Pub/Sub works under the premise of “fire and forget”. This essentially means that every published message will be delivered to as many subscribers as there are then it will be lost from the buffer
* All messages will be delivered to all subscribers. Mind you, you can have subscribers listening for different channels, which would prevent this from happening. But if you have more than one subscriber on the same channel, then all of them would get the same message. It would be up to them then, to decide what to do about that.
* There is no ACK message. Some communication protocols deal with an acknowledge message, in order for the subscribers to let the publisher know the message was received. In this case, there is nothing like that, so if your subscriber gets the message and then crashes, that data will be lost for good.

**When to use Pubsub**

* Chat servers, allowing you to create chat rooms easily by letting Redis take care of all the hard work of distributing messages amongst users. By default, these chat rooms would not persist messages, but you could find a way around that by adding some storage logic to your chat server
* Notification service: Another interesting use case, where you can subscribe to a set of notifications you’d like to receive, and then it’s a matter of publishers sending them to the right channel
* Log centralization. You could easily build a logging hub, where your own app is the publisher and different services make sure they send the information to the right destination. This would allow you to have a very flexible logging scheme, being able to swap from storing to disk to sending everything to an ELK instance or to a cloud service, or even all of them at once! Think about the possibilities!

**Blocklist vs Pubsub**

* Messages aren’t distributed to all subscribers, in fact, every message is only delivered to one subscriber thanks to the fact that the first one to be notified, pops it out
* The fact that messages are stored in a list in Redis, they are stored inside it until a subscriber is connected. And if you configure Redis to store data in the disk, you can get a pretty reliable queueing system

**Stream**

* Add data to stream: Because streams are an append only data structure, the fundamental write command, called XADD, appends a new entry into the specified stream. A stream entry is not just a string, but is instead composed of one or multiple field-value pairs. 
* Get data from stream: 
  * Access mode 1: With streams we want that multiple consumers can see the new messages appended to the Stream, like many tail -f processes can see what is added to a log. Using the traditional terminology we want the streams to be able to fan out messages to multiple clients.
  * Access mode 2: Get messages by ranges of time, or alternatively to iterate the messages using a cursor to incrementally check all the history. 
  * Access mode 3: as a stream of messages that can be partitioned to multiple consumers that are processing such messages, so that groups of consumers can only see a subset of the messages arriving in a single stream. In this way, it is possible to scale the message processing across different consumers, without single consumers having to process all the messages: each consumer will just get different messages to process. 
* Reference: 
  * [https://redis.io/topics/streams-intro](https://redis.io/topics/streams-intro)
  * [https://blog.logrocket.com/why-are-we-getting-streams-in-redis-8c36498aaac5/](https://blog.logrocket.com/why-are-we-getting-streams-in-redis-8c36498aaac5/)

**Info**

**Scan**

**Sorting**

