
<!-- MarkdownTOC -->

- [Distributed cache](#distributed-cache)
    - [Intuition](#intuition)
    - [Factors for hit ratio](#factors-for-hit-ratio)
    - [Applicable scenarios](#applicable-scenarios)
    - [Access pattern](#access-pattern)
        - [Cache aside](#cache-aside)
            - [Use case](#use-case)
            - [Potential issues](#potential-issues)
                - [Data inconsistency](#data-inconsistency)
                - [Cache hit ratio](#cache-hit-ratio)
            - [Flowchart](#flowchart)
                - [What if update cache instead of invalidate cache after writing to DB](#what-if-update-cache-instead-of-invalidate-cache-after-writing-to-db)
                - [What if invalidate cache first and then write to DB](#what-if-invalidate-cache-first-and-then-write-to-db)
        - [Read/Write through](#readwrite-through)
            - [Use case](#use-case-1)
            - [Potential issues](#potential-issues-1)
                - [Low performance](#low-performance)
            - [Flowchart](#flowchart-1)
        - [Write behind/back cache](#write-behindback-cache)
            - [Use case](#use-case-2)
            - [Potential issues](#potential-issues-2)
                - [Lose update](#lose-update)
            - [Flowchart](#flowchart-2)
        - [Write around cache](#write-around-cache)
    - [High availability](#high-availability)
        - [Client layer solution](#client-layer-solution)
            - [Sharding](#sharding)
                - [Consistency hashing](#consistency-hashing)
                - [Memcached master-slave](#memcached-master-slave)
                - [Multiple copies](#multiple-copies)
        - [Proxy layer solution](#proxy-layer-solution)
        - [Server layer solution](#server-layer-solution)
    - [Popular issues](#popular-issues)
        - [Cache penetration](#cache-penetration)
            - [Cache empty/default values](#cache-emptydefault-values)
            - [Bloomberg filter](#bloomberg-filter)
                - [Use case](#use-case-3)
                - [Potential issues](#potential-issues-3)
                    - [False positives](#false-positives)
                    - [No support for delete](#no-support-for-delete)
                - [Read](#read)
                - [Write](#write)
        - [Cache avalanch](#cache-avalanch)
            - [Solutions](#solutions)
                - [Distributed lock](#distributed-lock)
                - [Background refresh](#background-refresh)
        - [Hot key](#hot-key)
            - [Solutions](#solutions-1)
        - [Consistency between DB and distributed cache](#consistency-between-db-and-distributed-cache)
            - [Solutions](#solutions-2)
                - [Native cache aside pattern](#native-cache-aside-pattern)
                - [Transaction](#transaction)
                - [Messge queue](#messge-queue)
                - [Subscribe MySQL binlog as a slave](#subscribe-mysql-binlog-as-a-slave)
        - [Consistency between local and distributed cache](#consistency-between-local-and-distributed-cache)
            - [Solutions](#solutions-3)
    - [Scaling Memcached at Facebook](#scaling-memcached-at-facebook)

<!-- /MarkdownTOC -->


# Distributed cache
## Intuition
* Locality of reference
* Long tail

## Factors for hit ratio 
* Size of cache key space
    - The more unique cache keys your application generates, the less chance you have to reuse any one of them. Always consider ways to reduce the number of possible cache keys. 
* The number of items you can store in cache
    - The more objects you can physically fit into your cache, the better your cache hit ratio.
* Longevity
    - How long each object can be stored in cache before expiring or being invalidated. 

## Applicable scenarios
* short answer
  * How many times a cached piece of data can and is reused by the application
  * the proportion of response time that is alleviated by caching
* In applications that are I/O bound, most of the response time is getting data from a database.

## Access pattern
### Cache aside
#### Use case
* Most widely used pattern in distributed applications. Popular cache frameworks such as Redis / Memcached opt this approach by default. 

#### Potential issues
##### Data inconsistency
* Possibility of data inconsistency. However, the scenario doesn't happen frequently because the read operation need to happen before write and finish after write and it is unlikely that the read operation is slower than write operation. 

```
// data inconsistency
┌───────────┐         ┌───────────┐         ┌───────────┐         ┌───────────┐
│ Request A │         │ Request B │         │   Cache   │         │ Database  │
└───────────┘         └───────────┘         └───────────┘         └───────────┘
                                                                               
      │                     │                     │                     │      
      │                     │     cache miss      │                     │      
      │─────────────────────┼────────────────────▶│                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼read 20 from database┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │─────────Update database value to 21───────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │      invalidate     │                     │      
      │                     ├─────────cache───────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├──────────update cache value to 20────────▶│                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
```

##### Cache hit ratio
* When write operation happens frequently, cache data will be invalidated frequently. As a result, the cache hit ratio might suffer. Two possible solutions:
    - Update cache while update database, and put two operations in a distributed lock. 
    - Update cache while update database, and set a low expiration time for cache.

#### Flowchart

![Cache aside pattern](./images/cache_cacheaside_pattern.png)

##### What if update cache instead of invalidate cache after writing to DB
* It will cause the following two problems: 
    - Data inconsistency
    - Lose update

```
// Data inconsistency
┌───────────┐         ┌───────────┐         ┌───────────┐         ┌───────────┐
│ Request A │         │ Request B │         │   Cache   │         │ Database  │
└───────────┘         └───────────┘         └───────────┘         └───────────┘
                                                                               
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├───────────────────Update database value to 20───────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │───────Update database value to 21─────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │    update cache     │                     │      
      │                     │─────value to 21─────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├────────────Update cache value to 20───────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
```

```
// Lose update
┌───────────┐         ┌───────────┐         ┌───────────┐         ┌───────────┐
│ Request A │         │ Request B │         │   Cache   │         │ Database  │
└───────────┘         └───────────┘         └───────────┘         └───────────┘
                                                                               
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├────────────Get value from cache───────────┼▶                    │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │     Get value       │                     │      
      │                     │─────from cache──────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │───────────Increment 1 and update value────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼─Increment 1 and update value──────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
```

* How to solve the above problem? There are three possible ways: 
    1. A single transaction coordinator. 2PC
    2. Many transaction coordinators, with an elected master via Paxos or Raft consensus algorithm. Paxos
    3. Deletion of elements from memcached on DB updates
* 3 above is selected because 1 and 2 will cause performance and stability cost. 

##### What if invalidate cache first and then write to DB
* It will result in data inconsistency problems

```
┌───────────┐         ┌───────────┐         ┌───────────┐         ┌───────────┐
│ Request A │         │ Request B │         │   Cache   │         │ Database  │
└───────────┘         └───────────┘         └───────────┘         └───────────┘
                                                                               
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├───────────Invalidate cache value──────────┼▶                    │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │─────cache miss──────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │─────────Read value 20 from database───────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │     update cache    │                     │      
      │                     ├─────────to 20───────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├────────────────────update database value to 21──────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
```

### Read/Write through
#### Use case
* Client does not need to manage two connections towards cache and repository, separately. Everything could be managed by the cache itself. 
* Used more often in local cache (e.g. Guava cache's loading cache)

#### Potential issues
##### Low performance
* Write to database and write to cache happens synchronously. 

#### Flowchart
* def: write go through the cache and write is confirmed as success only if writes to DB and the cache both succeed.

![Read write through pattern](./images/cache_readwritethrough_pattern.png)

### Write behind/back cache
#### Use case
* Used more often in operating system's write to cache
* When you need quick write latency and high write throughput. 
    - Linux page cache algorithm
    - Asynchronously write message to disk in message queue

#### Potential issues
##### Lose update
* Message is not persisted to disk asynchronouly

#### Flowchart

![write behind pattern](./images/cache_writebehind_pattern.png)

### Write around cache
* def: write directly goes to the DB. The cache reads the info from DB in case of a miss
* use-case: lower write load to cache and faster writes, but can lead to higher read latency in case of applications which write and re-read the information quickly

## High availability
### Client layer solution
#### Sharding
##### Consistency hashing
* Pros: 
    + Low impact on hit ratio
* Cons: 
    + Cache node is not distributed evenly inside the ring
    + Dirty data: Suppose there are two nodes A and B in cluster. Initially pair (k,3) exists within cache A. Now a request comes to update k's value to 4 and cache A goes offline so the update load on cache B. Then cache A comes back online. Next time when client gets value, it will read 3 inside cache A instead of 4 inside cache B. 
        - Must set cache expiration time

##### Memcached master-slave 

![write behind pattern](./images/cache_clientHA_masterSlave.jpg)

##### Multiple copies

![multiple copies](./images/cache_clientHA_multipleCopies.jpg)

### Proxy layer solution
* All client read/write requests will come through the proxy layer. 
* The high availability strategy is implemented within the proxy layer.
* E.g. Facebook's Mcrouter, Twitter's Twemproxy, Codis

![Proxy layer HA](./images/cache_proxyHA.jpg)

### Server layer solution
* Redis Sentinel

![Server layer HA](./images/cache_serverHA.jpg)

## Popular issues

### Cache penetration
#### Cache empty/default values
* Cons: Might need large space for empty values

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

#### Bloomberg filter

##### Use case
* Time complexity: O(1) read/write
* Space complexity: To store 100 million users
    - 100M / 8 / 1024 / 1024 = 238M

##### Potential issues
###### False positives
* Solution: Use multiple hash algorithm to calculate multiple hash values

###### No support for delete
* Solution: Store a counter for each entry 


##### Read

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

##### Write

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

### Cache avalanch
* Many readers read an empty value from the cache and subseqeuntly try to load it from the database. The result is unnecessary database load as all readers simultaneously execute the same query against the database.

#### Solutions
##### Distributed lock
* Set a distributed lock on distributed cache. Only the request which gets distributed lock could reach to database.
* As an example: Assume key K expires
    1. A request A comes and hits cache miss
    2. Write an entry lock.K into the distributed cache and load from database
    3. A request B comes and has cache miss. Then it checks lock.K and finds its existence. It could retry later.

##### Background refresh
* The first client to request data past the stale date is asked to refresh the data, while subsequent requests are given the stale but not-yet-expired data as if it were fresh, with the understanding that it will get refreshed in a 'reasonable' amount of time by that initial request.

### Hot key
#### Solutions
1. Detect hot key (step2/3)
2. Randomly hash to multiple nodes instead of only one (step4)
3. Enable local cache for hot keys (step5)
4. Circuit breaker kicks in if detecting cache failure (step6)

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

### Consistency between DB and distributed cache
#### Solutions
##### Native cache aside pattern
* Cons:
    - If updating to database succeed and updating to cache fails, 

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

##### Transaction
* Put redis and mySQL update inside a transaction
    - Performance cost

##### Messge queue
* Cons:
    - Additional cost for maintaining a message queue
    - If there are multiple updates to the DB, its sequence in message queue might be mixed.

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

##### Subscribe MySQL binlog as a slave

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

### Consistency between local and distributed cache
#### Solutions

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

## Scaling Memcached at Facebook
* In a cluster:
    - Reduce latency
        + Problem: Items are distributed across the memcached servers through consistent hashing. Thus web servers have to rountinely communicate with many memcached servers to satisfy a user request. As a result, all web servers communicate with every memcached server in a short period of time. This all-to-all communication pattern can cause incast congestion or allow a single server to become the bottleneck for many web servers. 
        + Solution: Focus on the memcache client. 
    - Reduce load
        + Problem: Use memcache to reduce the frequency of fetching data among more expensive paths such as database queries. Web servers fall back to these paths when the desired data is not cached. 
        + Solution: Leases; Stale values;
    - Handling failures
        + Problem: 
            * A small number of hosts are inaccessible due to a network or server failure.
            * A widespread outage that affects a significant percentage of the servers within the cluster.
        + Solution: 
            * Small outages: Automated remediation system.
            * Gutter pool
    - In a region: Replication
    - Across regions: Consistency

