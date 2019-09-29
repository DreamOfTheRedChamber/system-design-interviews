# Cache system
## Data structure
### SDS (Simple dynamic string)
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

### Ziplist
#### Goal
* Memory efficient when compared with LinkedList

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

#### Ziplist conversion conditions
* Convert ziplist to hashset
* 


* Why sorted set uses ziplist implementation:
    1.  They are not very memory intensive. It's up to you basically. Changing parameters about the probability of a node to have a given number of levels will make then less memory intensive than btrees.
    2. A sorted set is often target of many ZRANGE or ZREVRANGE operations, that is, traversing the skip list as a linked list. With this operation the cache locality of skip lists is at least as good as with other kind of balanced trees.
    3. They are simpler to implement, debug, and so forth. For instance thanks to the skip list simplicity I received a patch (already in Redis master) with augmented skip lists implementing ZRANK in O(log(N)). It required little changes to the code.
* https://github.com/antirez/redis/blob/90a6f7fc98df849a9890ab6e0da4485457bf60cd/src/ziplist.c

### Hash
#### Structure
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

#### Incremental resizing
* Load factor = total_elements / total_buckets
* Scale up condition: load factor >= 1 (or load factor > 5) and no heavy background process (BGSAVE or BGREWRITEAOF) is happening
* Scale down condition: load factor < 0.1
* Condition to stop rehash:
    1. Incremental hashing usually follows these conditions: dictAddRaw / dictGenericDelete / dictFind / dictGetRandomKey
    2. Incremental hashing is also scheduled in server cron job.
* During the resizing process, all add / update / remove operations need to be performed on two tables. 

#### Encoding 


### Skiplist
### IntSet
### Object
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

## Advanced data structures
### HyperLogLog
### Bloomberg filter
### Bitmap
### Stream

## Implement a cache system on a single machine
### Server
### Client
### Processing logic
#### Event
##### File Event
##### Time event
#### I/O multi-plexing
### Interaction modes between server and client
#### RESP 
#### Pipeline
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

#### Transaction
#### Script mode
#### PubSub model
### Expiration strategy
#### History
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

#### Commands
* Proactive commands: Unlink, FlushAll Async, FlushDB Async
* Reactive commands: 
    * Slve-lazy-flush: Clear data options after slave receives RDB files
    * Lazy free-lazy-eviction: full memory ejection option
    * Lazy free-lazy-expire: expired key deletion option
    * Lazyfree-lazy-server-del: Internal deletion options, such as rename oldkey new key, need to be deleted if new key exists

#### Eviction options
* LRU vs LFU
* https://redis.io/topics/lru-cache
* Improve LRU cache algorithm http://antirez.com/news/109

### Persistence options
#### COW 
* Both RDB and AOF relies on Unix Copy on Write mechanism
* http://oldblog.antirez.com/post/a-few-key-problems-in-redis-persistence.html

#### Pros and Cons between RDB and AOF
* https://redis.io/topics/persistence

#### RDB
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

#### AOF
* FlushAppendOnlyFile's behavior depends on appendfsync param:
    * always: Write aof_buf to AOF file and sync to slaves on each file event loop.
    * everysec: Write aof_buf to AOF file on each file event loop. If the last synchronization happens before 1 sec, then sync to slaves on each file event loop. 
    * no: Write aof_buf to AOF file on each file event loop. Depend on the OS to determine when to sync.
* AOF rewrite:
    * Goal: Reduce the size of AOF file.
    * AOF rewrite doesn't need to read the original AOF file. It directly reads from database. 
    * Redis fork a child process to execute AOF rewrite dedicatedly. Redis opens a AOF rewrite buffer to keep all the instructions received during the rewriting process. At the end of rewriting AOF file, all instructions within AOF rewrite buffer will be flushed to the new AOF file. 

## Implement a cache system on a distributed scale
### Consistency - Replication and failover
### Availability - Sentinel
### Scalability - Sharding
### Existing solutions
#### Codis
#### Redis cluster

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

