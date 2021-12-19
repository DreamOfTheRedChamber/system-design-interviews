- [Redis blocking operations](#redis-blocking-operations)
- [CPU and NUMA architecture](#cpu-and-numa-architecture)
- [Redis key configurations](#redis-key-configurations)
- [Redis memory fragments](#redis-memory-fragments)
- [Redis Buffer](#redis-buffer)
  - [RESP](#resp)
  - [Expiration strategy](#expiration-strategy)
    - [History](#history)
    - [Types](#types)
    - [Commands](#commands)

# Redis blocking operations

![](../.gitbook/assets/redisBlockingOperations.png)

![](../.gitbook/assets/redis_MultiSelectIO.png)

# CPU and NUMA architecture

# Redis key configurations

# Redis memory fragments

# Redis Buffer


## RESP

## Expiration strategy

### History

* Lazy free
  * Timer function and perform the eviction. Difficulties: Adaptive speed for freeing memory. Found an adaptive strategy based on the following two standards: 1. Check the memory tendency: it is raising or lowering? In order to adapt how aggressively to free. 2. Also adapt the timer frequency itself based on “1”, so that we don’t waste CPU time when there is little to free, with continuous interruptions of the event loop. At the same time the timer could reach ~300 HZ when really needed.
  * For the above strategy, during busy times it only serves 65% QPS. However, the internal Redis design is heavily geared towards sharing objects around. Many data structures within Redis are based on the shared object structure robj. As an effort, the author changed it to SDS. 
* [http://antirez.com/news/93](http://antirez.com/news/93)

### Types

* Timing deletion: While setting the expiration time of the key, create a timer. Let the timer immediately perform the deletion of the key when the expiration time of the key comes.
* Inert deletion: Let the key expire regardless, but every time the key is retrieved from the key space, check whether the key is expired, if it is expired, delete the key; if it is not expired, return the key.
* Periodic deletion: Every once in a while, the program checks the database and deletes the expired keys. How many expired keys to delete and how many databases to check are determined by the algorithm.
* Inert deletion and periodic deletion are used within Redis. 
  * Inert deletion \(expireIfNeeded function\) serves as a filter before executing any key operation
  * Periodic deletion of key occurs in Redis’s periodic execution task \(server Cron, default every 100ms\), and is the master node where Redis occurs, because slave nodes synchronize to delete key through the DEL command of the primary node. Each DB is traversed in turn \(the default configuration number is 16\). For each db, 20 keys \(ACTIVE\_EXPIRE\_CYCLE\_LOOKUPS\_PER\_LOOP\) are selected randomly for each cycle to determine whether they are expired. If the selected keys in a round are less than 25% expired, the iteration is terminated. In addition, if the time limit is exceeded, the process of expired deletion is terminated.
* [https://developpaper.com/an-in-depth-explanation-of-key-expiration-deletion-strategy-in-redis/](https://developpaper.com/an-in-depth-explanation-of-key-expiration-deletion-strategy-in-redis/)

### Commands

* Proactive commands: Unlink, FlushAll Async, FlushDB Async
* Reactive commands: 
  * Slve-lazy-flush: Clear data options after slave receives RDB files
  * Lazy free-lazy-eviction: full memory ejection option
  * Lazy free-lazy-expire: expired key deletion option
  * Lazyfree-lazy-server-del: Internal deletion options, such as rename oldkey new key, need to be deleted if new key exists
