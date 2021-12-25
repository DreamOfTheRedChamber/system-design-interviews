- [Cache penetration](#cache-penetration)
  - [Cache empty/default values](#cache-emptydefault-values)
  - [Bloomberg filter](#bloomberg-filter)
    - [Read](#read)
    - [Write](#write)
  - [Cache everything](#cache-everything)
- [Cache avalanch](#cache-avalanch)
- [Race condition](#race-condition)
  - [Distributed lock](#distributed-lock)
- [Hot key](#hot-key)
- [Data inconsistency](#data-inconsistency)
  - [Native cache aside pattern](#native-cache-aside-pattern)
  - [Transaction](#transaction)
  - [Messge queue](#messge-queue)
  - [Subscribe MySQL binlog as a slave](#subscribe-mysql-binlog-as-a-slave)
  - [inconsistency between local and distributed cache](#inconsistency-between-local-and-distributed-cache)
- [Big key](#big-key)
  - [Scenarios](#scenarios)
  - [Diagnose](#diagnose)
  - [Solutions: Delete big keys in the background**](#solutions-delete-big-keys-in-the-background)
  - [Solutions: Compression](#solutions-compression)
  - [Solutions: Split key](#solutions-split-key)
  - [TODO](#todo)

# Cache penetration

* Note: All 1-4 bullet points could be used separately.
* Cache key validation (step1)
* Cache empty values (step2)
* Bloom filter (step3)
* Cache entire dataset in cache (step4)

```
┌───────┐   ┌──────────┐   ┌─────────────┐  ┌────────┐   ┌────────┐  ┌────────┐
│       │   │  step1:  │   │step2: cache │  │ step3: │   │ Step4. │  │        │
│Client │──▶│ Request  │──▶│empty values │─▶│ bloom  │──▶│ Cache  │─▶│ Cache  │
│       │   │validation│   │             │  │ filter │   │everythi│  │        │
└───────┘   └──────────┘   └─────────────┘  └──────*─┘   └────────┘  └────────┘
```

## Cache empty/default values

* Cons: Might need large space for empty values. As a result, cache entries for non-empty entries might be purged out. 

```
Object nullValue = new Object();
try 
{
  Object valueFromDB = getFromDB(uid); 
  if (valueFromDB == null) 
  {
    cache.set(uid, nullValue, 10);   
  } 
  else 
  {
    cache.set(uid, valueFromDB, 1000);
  }
} 
catch(Exception e) 
{
  cache.set(uid, nullValue, 10);
}
```

## Bloomberg filter

* Use case
  * Time complexity: O(1) read/write
  * Space complexity: Within 1 billion records (roughly 1.2GB memory)
* Potential issues
  * False positives
    * Solution: Use multiple hash algorithm to calculate multiple hash values
  * No support for delete
    * Solution: Store a counter for each entry 

### Read

```
┌───────────┐         ┌───────────┐         ┌───────────┐         ┌───────────┐
│ Request A │         │ Request B │         │   Cache   │         │ Database  │
└───────────┘         └───────────┘         └───────────┘         └───────────┘

      │     lookup bloom    │                     │                     │      
      │        filter       │                     │                     │      
      │─────────────────────▶                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├────────────────lookup cache──────────────▶│                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼──lookup database────┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├──────────────write to cache───────────────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │
```

### Write

```
┌───────────┐       ┌───────────────┐       ┌───────────┐         ┌───────────┐
│  Client   │       │ Bloom Filter  │       │   Cache   │         │ Database  │
└───────────┘       └───────────────┘       └───────────┘         └───────────┘

      │                     │                     │                     │      
      │ write bloom filter  │                     │                     │      
      │─────────────────────▶                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼───write database────┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
                                                                        │
```

## Cache everything

* In especially high traffic scenario (e.g. Amazon black Friday), even a small volume of cache penetration could still cause DB to go down. 
* Please refer to [DB and distributed cache consistency](https://github.com/DreamOfTheRedChamber/system-design/blob/master/distributedCache.md#consistency-between-db-and-distributed-cache) for ways to keep the two storage consistent. 

# Cache avalanch
1. Jitter to expiration time
2. Rate limiting / Circuit breaker to DB
3. Open distributed cache persistence option for fast recovery
4. Background refresh
   * The first client to request data past the stale date is asked to refresh the data, while subsequent requests are given the stale but not-yet-expired data as if it were fresh, with the understanding that it will get refreshed in a 'reasonable' amount of time by that initial request.

```
                      ┌─────────────────────────────────────────────────────────────────────────────────┐
                      │                                Distributed cache                                │
                      │                                                                         Step4.  │
                      │                                                                   ┌──────back ─┐│
    .─────────.       │                                                                   │     ground ││
 ,─'           '─.    │                                                                   ▼            ││
;   step 1. add   :   │  ┌──────────────────────┐  ┌──────────────────────┐   ┌──────────────────────┐ ││
:    jitter to    ;   │  │Entry A               │  │Entry ..              │   │Entry N               │ ││
 ╲expiration time╱    │  │Expiration with jitter│  │Expiration with jitter│   │Expiration with jitter│ ││
  '─.         ,─'     │  └──────────────────────┘  └──────────────────────┘   └──────────────────────┘ ││
     `───────'        │               │                                                   │            ││
                      │               │                                                   │            ││
                      │               ▼                                                   └────────────┘│
    .─────────.       │   ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─   │
 ,─'           '─.    │                            Step 3. Persistent to disk                        │  │
; step 2. circuit :   │   │                                                                             │
: breaker / rate  ;   │    ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
 ╲   limiting    ╱    │                                                                                 │
  '─.         ,─'     └─────────────────────────────────────────────────────────────────────────────────┘
     `───────'                                                                                           
                        ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─      
                                               circuit breaker / rate limiter                      │     
                        │                                                                                
                         ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘     

                      ┌────────────────────────────────────────────────────────────────────────────────┐ 
                      │                                                                                │ 
                      │                                    Database                                    │ 
                      │                                                                                │ 
                      └────────────────────────────────────────────────────────────────────────────────┘
```

# Race condition

* Many readers read an empty value from the cache and subseqeuntly try to load it from the database. The result is unnecessary database load as all readers simultaneously execute the same query against the database.
* Solutions:
  * Distributed lock

## Distributed lock

* Scenario: Multiple requests get value from cache and all have cache miss. Then they simultaneously fetch value from DB to update distributed cache. 
* To make sure there are no race conditions, the following two conditions need to be met to order the updates.
  1. Every instance need to get a distributed lock before updating value in cache.
  2. Each value also has a corresponding timestamp which is obtained from DB.

```
                      ┌─────────────────┐                       
                      │                 │                       
          ┌──────────▶│Distributed Cache│◀──────────┐           
          │           │                 │           │           
          │           └─────────────────┘           │           
          │                    ▲                    │           
        Value1,                │                ValueN,         
       timestamp1              │               timestampN       
          │                Value2,                  │           
          │               timeStamp2                │           
          │                    │                    │           
          │                    │                    │           
    ┌──────────┐         ┌──────────┐         ┌──────────┐      
    │          │         │          │         │          │      
    │ Client A │         │Client ...│         │ Client N │      
    │          │         │          │         │          │      
    └──────────┘         └──────────┘         └──────────┘      
          │                    │                    │           
          │                    │                    │           
          │                    │                    │           
          │          Get distributed lock:          │           
          │                     Failed              │           
          │                    │                    │           
Get distributed lock:          │          Get distributed lock: 
       Succeed                 ▼                 Succeed        
          │           ┌─────────────────┐           │           
          │           │                 │           │           
          └──────────▶│    Zookeeper    │◀──────────┘           
                      │                 │                       
                      └─────────────────┘
```

# Hot key
* Solutions
  * Note: All 1-4 bullet points could be used separately.
  * Detect hot key (step2/3)
    * The one showed in the flowchart is a dynamic approach. There are several ways to decide hot keys:
      * Within proxy layer
      * Within client
      * Use redis shipped commands ./redis-cli --hotkeys
  * Randomly hash to multiple nodes instead of only one (step4)
  * Enable local cache for hot keys (step5)
  * Circuit breaker kicks in if detecting cache failure (step6)
  * References: [https://juejin.im/post/6844903765733015559](https://juejin.im/post/6844903765733015559)

```
   ┌───────────────┐                                                                                    
   │               │                                                                                    
   │    Client     │                                                                                    
   │               │                                                                                    
   │               │                                                                                    
   └───────────────┘                                                                                    
     │    │     │                                                                                       
     │    │     │                                                                                       
     │    │     │                                                               ┌──────────────────────┐
     │    │     │                                                               │ Configuration center │
     │    │     │    ─ ─ ─ ─ ─ ─ ─ step0. subscribe to hot key changes ─ ─ ─ ─ ▶│                      │
     │    │     │   │                                                           │   (e.g. Zookeeper)   │
     │  Step1:  │                                                               └┬─────────────────────┘
     │ Requests │   │                                                            │          ▲           
     │ come in  │                                                                │          │           
     │    │     │   │                                                            │          │           
     │    │     │   ┌─────────────Step3. Hot key change is published─────────────┘          │           
     │    │     │   │                                                                       │           
     │    │     │   │                                                                       │           
     │    │     │   │                                                                     Yes           
     │    │     │   │                                                                       │           
     ▼    ▼     ▼   ▼                                                                       │           
   ┌─────────────────────────────────┐                                                      │           
   │           App Cluster           │                                                      │           
   │                                 │    step 2:    ┌─────────────────────────┐       .─────────.      
   │ ┌ ─ ─ ─ ┐  ┌ ─ ─ ─ ┐  ┌ ─ ─ ─ ┐ │   aggregate   │    Stream processing    │      ╱           ╲     
   │   local      local      local   ├───to detect ─▶│                         │────▶(Is it hot key)    
   │ │ cache │  │ cache │  │ cache │ │    hot keys   │      (e.g. Flink)       │      `.         ,'     
   │  ─ ─ ─ ─    ─ ─ ─ ─    ─ ─ ─ ─  │               └─────────────────────────┘        `───────'       
   │     ▲                           │                                                                  
   │ ┌ ─ ╬ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │                                                                  
   │     ║  step 6. circuit breaker  │                                                                  
   │ │   ║                         │ │                                                                  
   │  ─ ─║─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │                                                                  
   └─────╬───────────────────────────┘                                                                  
         ║          │                  step4. For the same hot key,                                     
         ║          │                 randomly map to multiple nodes                                    
┌──────────────┐    │                        instead of only 1                                          
│ Step5. Cache │    └───────────────┬──────────────────────────────────────┐                            
│hot key within│                    │                                      │                            
│ local cache  │                    │                                      │                            
└──────────────┘         ┌──────────▼──────────────────────────────────────▼──────────┐                 
                         │  ┌ ─ ─ ─ ─ ─ ─ ─     ┌ ─ ─ ─ ─ ─ ─ ─    ┌ ─ ─ ─ ─ ─ ─ ─    │                 
                         │    distributed  │      distributed  │     distributed  │   │                 
                         │  │ cache node A      │ cache node B     │ cache node C     │                 
                         │   ─ ─ ─ ─ ─ ─ ─ ┘     ─ ─ ─ ─ ─ ─ ─ ┘    ─ ─ ─ ─ ─ ─ ─ ┘   │                 
                         │                                                            │                 
                         │                       Cache Cluster                        │                 
                         └────────────────────────────────────────────────────────────┘
```

# Data inconsistency

* Inconsistency between DB and distributed cache

* Solutions
  * Native cache aside pattern

## Native cache aside pattern

* Cons:
  * If updating to database succeed and updating to cache fails, 

```
┌───────────┐       ┌───────────────┐                             ┌───────────┐
│  Client   │       │  distributed  │                             │ Database  │
│           │       │     cache     │                             │           │
└───────────┘       └───────────────┘                             └───────────┘

      │                     │                                           │      
      │                     │                                           │      
      ├─────────────────────┼────write database─────────────────────────▶      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     ◀──────────────invalidate cache─────────────┤      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │
```

## Transaction

* Put redis and mySQL update inside a transaction
  * Performance cost

## Messge queue

* Cons:
  * Additional cost for maintaining a message queue
  * If there are multiple updates to the DB, its sequence in message queue might be mixed.

```
┌───────────┐       ┌───────────────┐       ┌───────────┐         ┌───────────┐
│  Client   │       │  distributed  │       │  Message  │         │ Database  │
│           │       │     cache     │       │   Queue   │         │           │
└───────────┘       └───────────────┘       └───────────┘         └───────────┘

      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼────write database───┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │        Send a       │      
      │                     │                     │◀─────message to─────┤      
      │                     │                     │      invalidate     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │      invalidate     │                     │      
      │                     ◀─────────cache───────┤                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │
```

## Subscribe MySQL binlog as a slave

```
┌───────────┐    ┌───────────────┐     ┌───────────────┐    ┌─────────────┐      ┌─────────────┐
│           │    │               │     │               │    │Fake db slave│      │  Database   │
│  Client   │    │  distributed  │     │ Message queue │    │             │      │             │
│           │    │     cache     │     │               │    │(e.g. canal) │      │(e.g. MySQL) │
│           │    │               │     │               │    │             │      │             │
└───────────┘    └───────────────┘     └───────────────┘    └─────────────┘      └─────────────┘
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      ├──────────Subscribe to MQ────────────▶                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │    subscribe to     │       
      │                 │                   │                     ├──binlog as a slave──▶       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      ├─────────────────┼──────────────write database─────────────┼─────────────────────▶       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │    publish binlog   │       
      │                 │                   │                     │◀──────to slave──────┤       
      │                 │                   │        convert      │                     │       
      │                 │                   │       binlog to     │                     │       
      │                 │                   ◀──────message and ───┤                     │       
      │                 │                   │        publish      │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      ◀───────receive published message─────┤                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │   update        │                   │                     │                     │       
      ├───cache─────────▶                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │
```

## inconsistency between local and distributed cache

```
// Scenario: update distributed cache as administrator operations
┌───────────┐       ┌───────────────┐       ┌───────────┐         ┌───────────┐
│application│       │  local cache  │       │distributed│         │ Database  │
│           │       │               │       │   cache   │         │           │
└───────────┘       └───────────────┘       └───────────┘         └───────────┘

      │                     │                     │                     │      
      │                     │                     │                     │      
      ├──────────────subscribe to change──────────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │        update       │      
      │                     │                     │◀──────value as ─────┤      
      │                     │                     │        admin        │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ◀──────────receive published message────────┤                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │     update          │                     │                     │      
      ├───local cache───────▶                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │
```

# Big key

## Scenarios

* Star's follower list
* Comments under hot topic
* Value stores too many items(e.g. redis Hash/List/Set/SortedSet)
  * The upper limit size is 2^32
  * As long as number of items inside collection >= 1 million, the latency is roughly 1s. 

## Diagnose

* Using redis as example

```
>= redis 4.0, memory usage command
< redis 4.0
    1. bgsave, redis-rdb-tool: export rdb file and analyse
    2. redis-cli --bigkeys: find big keys
    3. debug object key: look for the length of serialized key
```

## Solutions: Delete big keys in the background**

* Using redis as example

```
// Redis introduced Lazyfreeze commands "unlink"/"flushallasync"/"flushdbasync" commands to delete the item in the 
// background. When deleting an object, only logical deletion is made and then the object is thrown to the background. 

Slve-lazy-flush: Clear data options after slave receives RDB files
Lazy free-lazy-eviction: full memory ejection option
Lazy free-lazy-expire: expired key deletion option
lazyfree-lazy-server-del: Internal deletion options, such as rename oldkey new key, need to be deleted if new key exists
```

## Solutions: Compression

* When cache value is bigger than a certain size, use compression. 

## Solutions: Split key

* Under the same key, limit the size of buckets. 

## TODO
* https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=404202261&idx=1&sn=1b8254ba5013952923bdc21e0579108e&scene=21#wechat_redirect
* 