- [Redis blocking operations](#redis-blocking-operations)
- [CPU and NUMA architecture](#cpu-and-numa-architecture)
- [Redis key configurations](#redis-key-configurations)
- [Redis memory fragments](#redis-memory-fragments)
- [Redis Buffer](#redis-buffer)
  - [RESP](#resp)
  - [Pipeline](#pipeline)
    - [Definition](#definition)
    - [Benefits](#benefits)
    - [Use case](#use-case)
  - [Expiration strategy](#expiration-strategy)
    - [History](#history)
    - [Types](#types)
    - [Commands](#commands)

# Redis blocking operations

# CPU and NUMA architecture

# Redis key configurations

# Redis memory fragments

# Redis Buffer


## RESP
## Pipeline
### Definition
* A Request/Response server can be implemented so that it is able to process new requests even if the client didn't already read the old responses. This way it is possible to send multiple commands to the server without waiting for the replies at all, and finally read the replies in a single step.

### Benefits
* Reduce latency because many round trip times are avoided
* Improves a huge amount of total operations you could perform per second on a single redis server because it avoids many context switch. From the point of view of doing the socket I/O, this involves calling the read\(\) and write\(\) syscall, that means going from user land to kernel land. The context switch is a huge speed penalty. More detailed explanation: \(why a busy loops are slow even on the loopback interface?\) processes in a system are not always running, actually it is the kernel scheduler that let the process run, so what happens is that, for instance, the benchmark is allowed to run, reads the reply from the Redis server \(related to the last command executed\), and writes a new command. The command is now in the loopback interface buffer, but in order to be read by the server, the kernel should schedule the server process \(currently blocked in a system call\) to run, and so forth. So in practical terms the loopback interface still involves network-alike latency, because of how the kernel scheduler works. 
* [https://redis.io/topics/pipelining](https://redis.io/topics/pipelining)

### Use case
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
