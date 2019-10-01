#Cache system

<!-- MarkdownTOC -->

- Data structure
    - SDS \(Simple dynamic string\)
    - Hash
        - Structure
        - Incremental resizing
        - Encoding
    - Skiplist
        - Skiplist vs balanced tree in ZSet
    - Memory efficient data structures
        - Ziplist
            - Structure
            - Complexity
        - IntSet
            - Structure
            - Upgrade and downgrade
    - Object
- Advanced data structures
    - HyperLogLog
    - Bloomberg filter
    - Bitmap
    - Stream
- Implement a cache system on a single machine
    - Server
    - Client
    - Processing logic
        - Event
            - File Event
            - Time event
        - I/O multi-plexing
    - Interaction modes between server and client
        - RESP
        - Pipeline
        - Transaction
        - Script mode
        - PubSub model
    - Expiration strategy
        - History
            - Types
        - Commands
        - Eviction options
    - Persistence options
        - COW
        - Pros and Cons between RDB and AOF
        - RDB
        - AOF
- Implement a cache system on a distributed scale
    - Consistency - Replication and failover
    - Availability - Sentinel
        - Definition
        - Advanced concepts
        - Algorithms and internals
    - Scalability Redis cluster
        - Main properties and rational of the design
            - Redis cluster goals
            - Implemented subset
            - Clients and Servers roles in the Redis Cluster protocol
            - Write safety
            - Availability
        - Redis cluster main components
            - Key distribution model
            - Cluster node attributes
            - Message and implementation
        - Redirection and resharding
            - Resharding condition
            - Resharding commands
            - Resharding internals
            - Move and Ask redirection
            - Smart client
            - Multikey operations
            - Scaling reads using slave nodes
        - Fault tolerance
            - Heartbeat and gossip messages
            - Heartbeat package content
            - Failure detection
        - Configuration handling, propogation and failovers
            - Cluster current epoch
            - Configuration epoch
            - Slave election and promotion
            - Slave rank
            - Masters reply to slave vote request
            - Practical example of configuration epoch usefulness during partitions
            - Hash slots configuration propagation
            - UPDATE messages, a closer look
            - How nodes rejoin the cluster
            - Replica migration
            - Replica migration algorithm
            - ConfigEpoch conflicts resolution algorithm
            - Node resets
            - Removing nodes from a cluster
    - Application Components:
        - Autocomplete
        - Counting metaphores
        - Redis cell rate limiting
        - Info
        - Scan
        - Distributed locking
        - Sorting
    - Common cache problems
        - Cache big values
        - hot spot

<!-- /MarkdownTOC -->


# Data structure
## SDS (Simple dynamic string)
* Redis implements SDS on top of c string because of the following reasons:
    1. Reduce the strlen complexity from O(n) to O(1)
    2. Avoid buffer overflow because C needs to check string has enough capacity before executing operations such as strcat. 
* SDS has the following data structure

```
struct sdshdr 
{
    int len;
    int free;
    char buf[];
};
```

* SDS relies on the following two mechanisms for unused space.
    1. Space preallocation. The preallocation algorithm used is the following: every time the string is reallocated in order to hold more bytes, the actual allocation size performed is two times the minimum required. So for instance if the string currently is holding 30 bytes, and we concatenate 2 more bytes, instead of allocating 32 bytes in total SDS will allocate 64 bytes. However there is an hard limit to the allocation it can perform ahead, and is defined by SDS_MAX_PREALLOC. SDS will never allocate more than 1MB of additional space (by default, you can change this default).
    2. Lazy free: When space is freed, it is marked as free but not immediately disallocated.
    3. Binary safety. C structure requires char comply with ASCII standards. 
    4. Compatible with C string functions. SDS will always allocate an additional char as terminating character so that SDS could reuse some C string functions. 

## Hash
### Structure
* dict in Redis is a wrapper on top of hashtable

```
typedef struct dict 
{
    dictType *type;
    void *privdata;

    // hash table
    dictht ht[2];

    // rehash index
    // rehashing not in progress if rehashidx == -1
    int trehashidx;
}
```

### Incremental resizing
* Load factor = total_elements / total_buckets
* Scale up condition: load factor >= 1 (or load factor > 5) and no heavy background process (BGSAVE or BGREWRITEAOF) is happening
* Scale down condition: load factor < 0.1
* Condition to stop rehash:
    1. Incremental hashing usually follows these conditions: dictAddRaw / dictGenericDelete / dictFind / dictGetRandomKey
    2. Incremental hashing is also scheduled in server cron job.
* During the resizing process, all add / update / remove operations need to be performed on two tables. 

### Encoding 
## Skiplist
### Skiplist vs balanced tree in ZSet
* They are not very memory intensive. It's up to you basically. Changing parameters about the probability of a node to have a given number of levels will make then less memory intensive than btrees.
* A sorted set is often target of many ZRANGE or ZREVRANGE operations, that is, traversing the skip list as a linked list. With this operation the cache locality of skip lists is at least as good as with other kind of balanced trees.
* They are simpler to implement, debug, and so forth. For instance thanks to the skip list simplicity I received a patch (already in Redis master) with augmented skip lists implementing ZRANK in O(log(N)). It required little changes to the code.
* https://github.com/antirez/redis/blob/90a6f7fc98df849a9890ab6e0da4485457bf60cd/src/ziplist.c


## Memory efficient data structures
### Ziplist
#### Structure
* zlbytes: Is a 4 byte unsigned integer, used to store the entire ziplist number of bytes used.
* zltail: Is a 4 byte unsigned integer, used to store the ziplist of the last node relative to the ziplist first address offset.
* zllen: Is a 2 byte unsigned integer, the number of nodes stored in ziplist, maximum value for (2^16 - 2), when zllen is greater than the maximum number of value when, need to traverse the whole ziplist to obtain the ziplist node.
* zlend: Is a 1 byte unsigned integer, value 255 (11111111), as the end of the ziplist match.
* entryX: Node ziplist, each node could represent a length-limited int or char.
    1. prev_entry_bytes_length: 
    2. content:

#### Complexity
* Insert operation. Worst case: O(N^2). Best case: O(1). Average case o(N)
    * Cascade update: When an entry is inserted, we need to set the prevlen field of the next entry to equal the length of the inserted entry. It can occur that this length cannot be encoded in 1 byte and the next entry needs to be grow a bit larger to hold the 5-byte encoded prevlen. This can be done for free, because this only happens when an entry is already being inserted (which causes a realloc and memmove). However, encoding the prevlen may require that this entry is grown as well. This effect may cascade throughout the ziplist when there are consecutive entries with a size close to ZIP_BIGLEN, so we need to check that the prevlen can be encoded in every consecutive entry.
* Delete operation. Worst case: O(N^2). Best case: O(1). Average case o(N)
    * Cascade update: Note that this effect can also happen in reverse, where the bytes required to encode the prevlen field can shrink. This effect is deliberately ignored, because it can cause a "flapping" effect where a chain prevlen fields is first grown and then shrunk again after consecutive inserts. Rather, the field is allowed to stay larger than necessary, because a large prevlenfield implies the ziplist is holding large entries anyway.
* Iterate operation. 

* https://redisbook.readthedocs.io/en/latest/compress-datastruct/ziplist.html

### IntSet
#### Structure

```
typedef struct intset 
{
    uint32_t encoding; // INSET_ENC_INT16, INTSET_ENC_INT32, INTSET_ENC_INT64
    uint32_t length;
    int8_t contents[]; 
}
```

#### Upgrade and downgrade
* As long as there is one item in the content which has bigger size, the entire content array will be upgraded.
* No downgrade is provided. 

## Object
* Definition

```
typedef struct redisObject
{
    unsigned type:4;
    unsigned encoding:4;
    void *ptr;
} robj;
```

* Type and encoding. Encoding gives Type the flexibility to use differnt object type under different scenarios. 

| type         | encoding                  |
|--------------|---------------------------|
| Redis_String | REDIS_ENCODING_INT        |
| Redis_String | REDIS_ENCODING_EMBSTR     |
| Redis_String | REDIS_ENCODING_RAW        |
| Redis_List   | REDIS_ENCODING_ZIPLIST    |
| Redis_List   | REDIS_ENCODING_LINKEDLIST |
| Redis_Hash   | REDIS_ENCODING_ZIPLIST    |
| Redis_Hash   | REDIS_ENCODING_HT         |
| Redis_Set    | REDIS_ENCODING_INTSET     |
| Redis_Set    | REDIS_ENCODING_HT         |
| Redis_ZSet   | REDIS_ENCODING_ZIPLIST    |
| Redis_ZSet   | REDIS_ENCODING_SKIPLIST   |

* string
    * Three coding formats:
        1. Int: if the target could be represented using a long.
        2. Embstr: If the target is smaller than 44 bytes. Embstr is read-only but it only needs one-time to allocate free space. Embstr could also better utilizes local-cache. Represented using SDS.
        3. Raw: Longer than 45 bytes. Represented using SDS.

* Data structure to be memory efficient (https://redis.io/topics/memory-optimization)

```
hash-max-zipmap-entries 512 (hash-max-ziplist-entries for Redis >= 2.6)
hash-max-zipmap-value 64  (hash-max-ziplist-value for Redis >= 2.6)
list-max-ziplist-entries 512
list-max-ziplist-value 64
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
set-max-intset-entries 512
```

# Advanced data structures
## HyperLogLog
* pfadd/pfcount/pfmerge
* pf means Philippe Flajolet

## Bloomberg filter
* bf.add/bf.exists/bf.madd/bf.mexists

## Bitmap
* Commands: setbit/getbit/bitcountt/bitpos/bitfield

## Stream

# Implement a cache system on a single machine
## Server
## Client
## Processing logic
### Event
#### File Event
#### Time event
### I/O multi-plexing
## Interaction modes between server and client
### RESP 
### Pipeline
* Definition: A Request/Response server can be implemented so that it is able to process new requests even if the client didn't already read the old responses. This way it is possible to send multiple commands to the server without waiting for the replies at all, and finally read the replies in a single step.
* Benefits:
    * Reduce latency because many round trip times are avoided
    * Improves a huge amount of total operations you could perform per second on a single redis server because it avoids many context switch. From the point of view of doing the socket I/O, this involves calling the read() and write() syscall, that means going from user land to kernel land. The context switch is a huge speed penalty. More detailed explanation: (why a busy loops are slow even on the loopback interface?) processes in a system are not always running, actually it is the kernel scheduler that let the process run, so what happens is that, for instance, the benchmark is allowed to run, reads the reply from the Redis server (related to the last command executed), and writes a new command. The command is now in the loopback interface buffer, but in order to be read by the server, the kernel should schedule the server process (currently blocked in a system call) to run, and so forth. So in practical terms the loopback interface still involves network-alike latency, because of how the kernel scheduler works. 
    * https://redis.io/topics/pipelining
* Usage:
    * Under transaction commands such as "MULTI", "EXEC"
    * The commands which take multiple arguments: MGET, MSET, HMGET, HMSET, RPUSH/LPUSH, SADD, ZADD
    * https://redislabs.com/ebook/part-2-core-concepts/chapter-4-keeping-data-safe-and-ensuring-performance/4-5-non-transactional-pipelines/
* Internal: Pipeline is purely a client-side implementation. 
    * Buffer the redis commands/operations on the client side
    * Synchronously or asynchronously flush the buffer periodically depending on the client library implementation.
    * Redis executes these operations as soon as they are received at the server side. Subsequent redis commands are sent without waiting for the response of the previous commands. Meanwhile, the client is generally programmed to return a constant string for every operation in the sequence as an immediate response
    * The tricky part: the final responses. Very often it is wrongly interpreted that all the responses are always read at one shot and that the responses maybe completely buffered on server's side. Even though the response to all the operations seem to arrive at one shot when the client closes the pipeline, it is actually partially buffered on both client and the server. Again, this depends on the client implementation. There does exist a possibility that the client reads the buffered response periodically to avoid a huge accumulation on the server side. But it is also possible that it doesn't. For example: the current implementation of the Jedis client library reads all responses of a pipeline sequence at once. 
    * http://blog.nachivpn.me/2014/11/redis-pipeline-explained.html
* Pipeline vs transaction:
    * Whether the commands are executed atomically
* How does stackexchange implements pipelines: 
    * https://stackexchange.github.io/StackExchange.Redis/PipelinesMultiplexers.html

### Transaction
### Script mode
### PubSub model
## Expiration strategy
### History
* Lazy free
    * Timer function and perform the eviction. Difficulties: Adaptive speed for freeing memory. Found an adaptive strategy based on the following two standards: 1. Check the memory tendency: it is raising or lowering? In order to adapt how aggressively to free. 2. Also adapt the timer frequency itself based on “1”, so that we don’t waste CPU time when there is little to free, with continuous interruptions of the event loop. At the same time the timer could reach ~300 HZ when really needed.
    * For the above strategy, during busy times it only serves 65% QPS. However, the internal Redis design is heavily geared towards sharing objects around. Many data structures within Redis are based on the shared object structure robj. As an effort, the author changed it to SDS. 
* http://antirez.com/news/93

#### Types
* Timing deletion: While setting the expiration time of the key, create a timer. Let the timer immediately perform the deletion of the key when the expiration time of the key comes.
* Inert deletion: Let the key expire regardless, but every time the key is retrieved from the key space, check whether the key is expired, if it is expired, delete the key; if it is not expired, return the key.
* Periodic deletion: Every once in a while, the program checks the database and deletes the expired keys. How many expired keys to delete and how many databases to check are determined by the algorithm.
* Inert deletion and periodic deletion are used within Redis. 
    * Inert deletion (expireIfNeeded function) serves as a filter before executing any key operation
    * Periodic deletion of key occurs in Redis’s periodic execution task (server Cron, default every 100ms), and is the master node where Redis occurs, because slave nodes synchronize to delete key through the DEL command of the primary node. Each DB is traversed in turn (the default configuration number is 16). For each db, 20 keys (ACTIVE_EXPIRE_CYCLE_LOOKUPS_PER_LOOP) are selected randomly for each cycle to determine whether they are expired. If the selected keys in a round are less than 25% expired, the iteration is terminated. In addition, if the time limit is exceeded, the process of expired deletion is terminated.
* https://developpaper.com/an-in-depth-explanation-of-key-expiration-deletion-strategy-in-redis/

### Commands
* Proactive commands: Unlink, FlushAll Async, FlushDB Async
* Reactive commands: 
    * Slve-lazy-flush: Clear data options after slave receives RDB files
    * Lazy free-lazy-eviction: full memory ejection option
    * Lazy free-lazy-expire: expired key deletion option
    * Lazyfree-lazy-server-del: Internal deletion options, such as rename oldkey new key, need to be deleted if new key exists

### Eviction options
* LRU vs LFU
* https://redis.io/topics/lru-cache
* Improve LRU cache algorithm http://antirez.com/news/109

## Persistence options
### COW 
* Both RDB and AOF relies on Unix Copy on Write mechanism
* http://oldblog.antirez.com/post/a-few-key-problems-in-redis-persistence.html

### Pros and Cons between RDB and AOF
* https://redis.io/topics/persistence

### RDB
* Command: SAVE vs BGSAVE. Whether a child process is forked to create RDB file. 
* BGSAVE - Automatic save condition
    * saveparam format: save   seconds   changes
    * dirty attribute: How many databse operations have been performed after the last time.
    * lastsave: A unix timestamp - the last time the server executes SAVE or BGSAVE. 

```
def serverCron():
    for saveParam in server.saveparams:
        save_internal = unixtime_now() - server.lastsave

        if server.dirty >= saveparam.changes 
            and save_internal > saveparams.seconds:
                BGSAVE()
```

### AOF
* FlushAppendOnlyFile's behavior depends on appendfsync param:
    * always: Write aof_buf to AOF file and sync to slaves on each file event loop.
    * everysec: Write aof_buf to AOF file on each file event loop. If the last synchronization happens before 1 sec, then sync to slaves on each file event loop. 
    * no: Write aof_buf to AOF file on each file event loop. Depend on the OS to determine when to sync.
* AOF rewrite:
    * Goal: Reduce the size of AOF file.
    * AOF rewrite doesn't need to read the original AOF file. It directly reads from database. 
    * Redis fork a child process to execute AOF rewrite dedicatedly. Redis opens a AOF rewrite buffer to keep all the instructions received during the rewriting process. At the end of rewriting AOF file, all instructions within AOF rewrite buffer will be flushed to the new AOF file. 

# Implement a cache system on a distributed scale
## Consistency - Replication and failover
## Availability - Sentinel
### Definition
* Sentinel is Redis' resiliency solution to standalone redis instance. 
    * See [Compare redis deployments](https://blog.octo.com/en/what-redis-deployment-do-you-need/) for details.
* It will monitor your master & slave instances, notify you about changed behaviour, handle automatic failover in case a master is down and act as a configuration provider, so your clients can find the current master instance.
* Redis sentinel was born in 2012 and first released when Redis 2.4 was stable. 

### Advanced concepts
* Initialization: Sentinel is a redis server running on a special mode
    * Sentinel will not load RDB or AOF file.
    * Sentinel will load a special set of Sentinel commands.
* Downstate
    * Subjective downstate: An SDOWN condition is reached when it does not receive a valid reply to PING requests for the number of seconds specified in the configuration as is-master-down-after-milliseconds parameter.
    * Objectively downstate: When enough Sentinels (at least the number configured as the quorum parameter of the monitored master) have an SDOWN condition, and get feedback from other Sentinels using the SENTINEL is-master-down-by-addr command.
* Sentinels and slaves auto discovery
    * You don't need to configure a list of other Sentinel addresses in every Sentinel instance you run, as Sentinel uses the Redis instances Pub/Sub capabilities in order to discover the other Sentinels that are monitoring the same masters and slaves. Similarly you don't need to configure what is the list of the slaves attached to a master, as Sentinel will auto discover this list querying Redis.
    * Process
        - Every Sentinel publishes a message to every monitored master and slave Pub/Sub channel __sentinel__:hello, every two seconds, announcing its presence with ip, port, runid.
        - Every Sentinel is subscribed to the Pub/Sub channel __sentinel__:hello of every master and slave, looking for unknown sentinels. When new sentinels are detected, they are added as sentinels of this master.
        - Hello messages also include the full current configuration of the master. If the receiving Sentinel has a configuration for a given master which is older than the one received, it updates to the new configuration immediately.
* Slave selection: Relies on [Raft protocol](http://thesecretlivesofdata.com/raft/)
    * The slave selection process evaluates the following information about slaves:
        - Disconnection time from the master.
        - Slave priority.
        - Replication offset processed.
        - Run ID.
    * A slave that is found to be disconnected from the master for more than ten times the configured master timeout (down-after-milliseconds option), plus the time the master is also not available from the point of view of the Sentinel doing the failover, is considered to be not suitable for the failover and is skipped.
    * The slave selection only considers the slaves that passed the above test, and sorts it based on the above criteria, in the following order.
        - The slaves are sorted by slave-priority as configured in the redis.conf file of the Redis instance. A lower priority will be preferred.
        - If the priority is the same, the replication offset processed by the slave is checked, and the slave that received more data from the master is selected.
        - If multiple slaves have the same priority and processed the same data from the master, a further check is performed, selecting the slave with the lexicographically smaller run ID. Having a lower run ID is not a real advantage for a slave, but is useful in order to make the process of slave selection more deterministic, instead of resorting to select a random slave.
* Sentinel's server cron operations:
    * Detect instance's objective and subjective downstate by sending PING commands
    * Automatically discover sentinel and slave nodes by subscribing to channel __sentinel__:hello
    * Leader selection and failover

### Algorithms and internals
* Quorum
    * Use cases: Considering a master as objectively downstate; Authorizing the failover process
    * Quorum could be used to tune sentinel in two ways:
        - If a the quorum is set to a value smaller than the majority of Sentinels we deploy, we are basically making Sentinel more sensible to master failures, triggering a failover as soon as even just a minority of Sentinels is no longer able to talk with the master.
        - If a quorum is set to a value greater than the majority of Sentinels, we are making Sentinel able to failover only when there are a very large number (larger than majority) of well connected Sentinels which agree about the master being down.
* Configuratin epochs
    * Epoch is similar to Raft algorithm's term.
    * When a Sentinel is authorized, it gets a unique configuration epoch for the master it is failing over. This is a number that will be used to version the new configuration after the failover is completed. Because a majority agreed that a given version was assigned to a given Sentinel, no other Sentinel will be able to use it.
* https://redis.io/topics/sentinel

## Scalability Redis cluster
### Main properties and rational of the design
#### Redis cluster goals
* High performance and linear scalability to 1000 nodes
    - No proxies
    - Asynchronous replication
    - No merge operations on values
* Acceptable degree of write safety
    - The system tries to retain all writes originating from clients
* Availability: 
    - Redis Cluster is able to survive partitions where the majority of the master nodes are reachable and there is at least one reachable slave for every master node that is no longer reachable.
    - Moreover using replicas migration, masters no longer replicated by any slave will receive one from a master which is covered by multiple slaves.

#### Implemented subset
* Same as standalone redis: Single key operations and multi-key operations
* Not supported: multi-database operations such as SELECT
* Additional supported: Hashtag which forces keys to be stored in the same slot

#### Clients and Servers roles in the Redis Cluster protocol
* Server: Holding the data, taking state of the cluster (Including mapping keys to the correct shard), auto-discover nodes, detect non-working nodes and promoting slaves to master when needed
    - Servers communicate to each other using a TCP bus and a binary protocol, called Redis Cluster Bus. 
* Client: The clients is free to send requests to any node within the cluster. 

#### Write safety
* There are two cases where write will fail
    - Clients write to master. While the master may be able to reply to the clients, the write may not be propogated to slaves and the master dies. As a result of master becomes unreacheable beyond a fixed amount of time, one of its slave is promoted.
    - A master becomes unavailable because of network partitions and failed over by ones of its slaves. The clients  continues talking to the old master. However, it is unlikely to happen because:
        1. Master which fails to communicate with majority of nodes will reject writes and after the partition heals writes are still rejected to allow other nodes informing configuration changes. 
        2. The clients haven't updated its routing table. 
        3. Writes to the minority of a cluster has a higher chance of being lost.

#### Availability
* Redis Cluster is designed to survive failures of a few nodes in the cluster, but it is not a suitable solution for applications that require availability in the event of large net splits.

### Redis cluster main components
#### Key distribution model
* HASH_SLOT = CRC16(key) mod 16384
* Hashtag could force multiple keys are allocated in the same slots and it is used to implement multi-key operations in redis cluster ???

#### Cluster node attributes
* The node ID, IP and port of the node, a set of flags, what is the master of the node if it is flagged as slave, last time the node was pinged and the last time the pong was received, the current configuration epoch of the node (explained later in this specification), the link state and finally the set of hash slots served.
* https://redis.io/commands/cluster-nodes

#### Message and implementation
* MEET/PING/PONG: Implemented using Gossip protocol. ???
* FAIL: Broadcast because Gossip Protocol takes time.
* PUBLISH: When client sends a Publish command to the node, the node will publish this message to the channel.  

### Redirection and resharding
#### Resharding condition
* To add a new node to the cluster an empty node is added to the cluster and some set of hash slots are moved from existing nodes to the new node.
* To remove a node from the cluster the hash slots assigned to that node are moved to other existing nodes.
* To rebalance the cluster a given set of hash slots are moved between nodes.
* All the above three conditions could be abstracted as moving slots between different shards. 

#### Resharding commands
* CLUSTER ADDSLOTS slot1 [slot2] ... [slotN]
* CLUSTER DELSLOTS slot1 [slot2] ... [slotN]
* CLUSTER SETSLOT slot NODE node
* CLUSTER SETSLOT slot MIGRATING node
* CLUSTER SETSLOT slot IMPORTING node

#### Resharding internals
1. redis-trib sends target node "CLUSTER SETSLOT $slot IMPORTING $source_id" so that target node is prepared to import key value pairs from slot. 
    - On the node side, there is a bitmap 

```
typedef struct clusterState
{
    // ...
    clusterNode *importing_slots_from[16384];

    // ...
}
```

2. redis-trib sends source node "CLUSTER SETSLOT $slot MIGRATING $target_id" so that source node is prepared to migrate key value pairs to slot.
    - On the node side, there is a bitmap


```
typedef struct clusterState
{
    // ...
    clusterNode *migrating_slots_to[16384];

    // ...
}
```

3. redis-trib sends source node "CLUSTER GETKEYSINSLOT $slot $count" to get at most count number of key names belonging to slot.
4. for every key name obtained in step 3, redis-trib will send source node a "MIGRATE $target_ip $target_port $key_name 0 $time_out" command to migrate the slots from source to dest node.
5. Repeat step 3 and 4 until all key-value pairs belong to the slots have been migrated.
6. redis-trib sends "CLUSTER SETSLOT $slot NODE $target_id" which will be broadcasted to all the nodes within the cluster.

#### Move and Ask redirection
* MOVED means that we think the hash slot is permanently served by a different node and the next queries should be tried against the specified node, ASK means to send only the next query to the specified node.
* ASK semantics for client:
    - If ASK redirection is received, send only the query that was redirected to the specified node but continue sending subsequent queries to the old node.
    - Start the redirected query with the ASKING command.
    - Don't yet update local client tables to map hash slot 8 to B.
* ASK semantics for server:
    - If the client has flag REDIS_ASKING and clusterStates_importing_slots_from[i] shows node is importing key value i, then node will execute the the client command once. 

#### Smart client
* Redis Cluster clients should try to be smart enough to memorize the slots configuration. However this configuration is not required to be up to date. Since contacting the wrong node will simply result in a redirection, that should trigger an update of the client view.
* Clients usually need to fetch a complete list of slots and mapped node addresses in two different situations:
    - At startup in order to populate the initial slots configuration.
    - When a MOVED redirection is received.

#### Multikey operations
#### Scaling reads using slave nodes
### Fault tolerance
#### Heartbeat and gossip messages
#### Heartbeat package content
#### Failure detection
### Configuration handling, propogation and failovers
#### Cluster current epoch
#### Configuration epoch
#### Slave election and promotion
#### Slave rank
#### Masters reply to slave vote request
#### Practical example of configuration epoch usefulness during partitions
#### Hash slots configuration propagation
#### UPDATE messages, a closer look
#### How nodes rejoin the cluster
#### Replica migration
#### Replica migration algorithm
#### ConfigEpoch conflicts resolution algorithm
#### Node resets
#### Removing nodes from a cluster


* History: http://antirez.com/news/79
* Redis Cluster is a data sharding solution with automatic management, handling failover and replication.



## Application Components:
### Autocomplete
### Counting metaphores
### Redis cell rate limiting
### Info
### Scan
### Distributed locking
### Sorting

## Common cache problems
### Cache big values
### hot spot

