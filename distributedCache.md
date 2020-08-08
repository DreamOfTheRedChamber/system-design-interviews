
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
    - [Frequent issues](#frequent-issues)
        - [Thundering herd problem](#thundering-herd-problem)
            - [Def](#def)
            - [Solutions](#solutions)
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

![write behind pattern](./images/cache_clientHA_masterSlave.png)

##### Multiple copies

![multiple copies](./images/cache_clientHA_multipleCopies.png)

### Proxy layer solution
* All client read/write requests will come through the proxy layer. 
* The high availability strategy is implemented within the proxy layer.
* E.g. Facebook's Mcrouter, Twitter's Twemproxy, Codis

![Proxy layer HA](./images/cache_proxyHA.png)

### Server layer solution
* Redis Sentinel

![Server layer HA](./images/cache_serverHA.png)

## Frequent issues
### Thundering herd problem
#### Def
* Many readers read an empty value from the cache and subseqeuntly try to load it from the database. The result is unnecessary database load as all readers simultaneously execute the same query against the database.

* Let's say you have [lots] of webservers all hitting a single memcache key that caches the result of a slow database query, say some sort of stat for the homepage of your site. When the memcache key expires, all the webservers may think "ah, no key, I will calculate the result and save it back to memcache". Now you have [lots] of servers all doing the same expensive DB query. 

```java
/* read some data, check cache first, otherwise read from SoR */
public V readSomeData(K key) {
  Element element;
  if ((element = cache.get(key)) != null) {
    return element.getValue();
  }

  // note here you should decide whether your cache
  // will cache 'nulls' or not
  if (value = readDataFromDataStore(key)) != null) {
    cache.put(new Element(key, value));
  }

  return value;
}
```

#### Solutions
* Stale date solution: The first client to request data past the stale date is asked to refresh the data, while subsequent requests are given the stale but not-yet-expired data as if it were fresh, with the understanding that it will get refreshed in a 'reasonable' amount of time by that initial request

    - When a cache entry is known to be getting close to expiry, continue to server the cache entry while reloading it before it expires. 
    - When a cache entry is based on an underlying data store and the underlying data store changes in such a way that the cache entry should be updated, either trigger an (a) update or (b) invalidation of that entry from the data store. 

* Add entropy back into your system: If your system doesn’t jitter then you get thundering herds. 
    - For example, cache expirations. For a popular video they cache things as best they can. The most popular video they might cache for 24 hours. If everything expires at one time then every machine will calculate the expiration at the same time. This creates a thundering herd.
    - By jittering you are saying  randomly expire between 18-30 hours. That prevents things from stacking up. They use this all over the place. Systems have a tendency to self synchronize as operations line up and try to destroy themselves. Fascinating to watch. You get slow disk system on one machine and everybody is waiting on a request so all of a sudden all these other requests on all these other machines are completely synchronized. This happens when you have many machines and you have many events. Each one actually removes entropy from the system so you have to add some back in.

* No expire solution: If cache items never expire then there can never be a recalculation storm. Then how do you update the data? Use cron to periodically run the calculation and populate the cache. Take the responsibility for cache maintenance out of the application space. This approach can also be used to pre-warm the the cache so a newly brought up system doesn't peg the database. 
    - The problem is the solution doesn't always work. Memcached can still evict your cache item when it starts running out of memory. It uses a LRU (least recently used) policy so your cache item may not be around when a program needs it which means it will have to go without, use a local cache, or recalculate. And if we recalculate we still have the same piling on issues.
    - This approach also doesn't work well for item specific caching. It works for globally calculated items like top N posts, but it doesn't really make sense to periodically cache items for user data when the user isn't even active. I suppose you could keep an active list to get around this limitation though.


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



