# Cache system
## Data structure


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

