- [Intuition](#intuition)
- [Factors for hit ratio](#factors-for-hit-ratio)
- [Applicable scenarios](#applicable-scenarios)
- [High availability](#high-availability)
  - [Client layer solution](#client-layer-solution)
    - [Sharding](#sharding)
    - [Consistency hashing](#consistency-hashing)
    - [Memcached master-slave](#memcached-master-slave)
    - [Multiple copies](#multiple-copies)
    - [Proxy layer solution](#proxy-layer-solution)
      - [Server layer solution](#server-layer-solution)
- [Real world](#real-world)
    - [TODO](#todo)

# Intuition

* Locality of reference
* Long tail

# Factors for hit ratio

* Size of cache key space
  * The more unique cache keys your application generates, the less chance you have to reuse any one of them. Always consider ways to reduce the number of possible cache keys. 
* The number of items you can store in cache
  * The more objects you can physically fit into your cache, the better your cache hit ratio.
* Longevity
  * How long each object can be stored in cache before expiring or being invalidated. 

# Applicable scenarios

* short answer
  * How many times a cached piece of data can and is reused by the application
  * the proportion of response time that is alleviated by caching
* In applications that are I/O bound, most of the response time is getting data from a database.


# High availability

## Client layer solution

### Sharding

### Consistency hashing

* Pros: 
  * Low impact on hit ratio
* Cons: 
  * Cache node is not distributed evenly inside the ring
  * Dirty data: Suppose there are two nodes A and B in cluster. Initially pair (k,3) exists within cache A. Now a request comes to update k's value to 4 and cache A goes offline so the update load on cache B. Then cache A comes back online. Next time when client gets value, it will read 3 inside cache A instead of 4 inside cache B. 
    * Must set cache expiration time

### Memcached master-slave

![write behind pattern](images/cache_clientHA_masterSlave.jpg)

### Multiple copies

![multiple copies](.gitbook/assets/cache_clientHA_multipleCopies.jpg)

### Proxy layer solution

* All client read/write requests will come through the proxy layer. 
* The high availability strategy is implemented within the proxy layer.
* E.g. Facebook's Mcrouter, Twitter's Twemproxy, Codis

![Proxy layer HA](images/cache_proxyHA.jpg)

#### Server layer solution

* Redis Sentinel

![Server layer HA](images/cache_serverHA.jpg)

# Real world
* Cache warming at Netflix: [https://netflixtechblog.com/cache-warming-agility-for-a-stateful-service-2d3b1da82642](https://netflixtechblog.com/cache-warming-agility-for-a-stateful-service-2d3b1da82642)
* From RAM to SSD Data caching at Netflix: [https://netflixtechblog.com/evolution-of-application-data-caching-from-ram-to-ssd-a33d6fa7a690](https://netflixtechblog.com/evolution-of-application-data-caching-from-ram-to-ssd-a33d6fa7a690)
* Netflix SSD data caching: [https://netflixtechblog.com/application-data-caching-using-ssds-5bf25df851ef](https://netflixtechblog.com/application-data-caching-using-ssds-5bf25df851ef)
* Netflix global caching: [https://netflixtechblog.com/caching-for-a-global-netflix-7bcc457012f1](https://netflixtechblog.com/caching-for-a-global-netflix-7bcc457012f1)
* Instagram thundering problem: [https://instagram-engineering.com/thundering-herds-promises-82191c8af57d](https://instagram-engineering.com/thundering-herds-promises-82191c8af57d)
* Instagram using Redis: [https://instagram-engineering.com/storing-hundreds-of-millions-of-simple-key-value-pairs-in-redis-1091ae80f74c](https://instagram-engineering.com/storing-hundreds-of-millions-of-simple-key-value-pairs-in-redis-1091ae80f74c)
* Background data fetching: [https://instagram-engineering.com/improving-performance-with-background-data-prefetching-b191acb39898](https://instagram-engineering.com/improving-performance-with-background-data-prefetching-b191acb39898)
* Scaling Memcached at Facebook:
  * Reduce latency
    * Problem: Items are distributed across the memcached servers through consistent hashing. Thus web servers have to rountinely communicate with many memcached servers to satisfy a user request. As a result, all web servers communicate with every memcached server in a short period of time. This all-to-all communication pattern can cause incast congestion or allow a single server to become the bottleneck for many web servers. 
    * Solution: Focus on the memcache client. 
  * Reduce load
    * Problem: Use memcache to reduce the frequency of fetching data among more expensive paths such as database queries. Web servers fall back to these paths when the desired data is not cached. 
    * Solution: Leases; Stale values;
  * Handling failures
    * Problem: 
      * A small number of hosts are inaccessible due to a network or server failure.
      * A widespread outage that affects a significant percentage of the servers within the cluster.
    * Solution: 
      * Small outages: Automated remediation system.
      * Gutter pool
  * In a region: Replication
  * Across regions: Consistency


### TODO

* [All things caching- use cases, benefits, strategies, choosing a caching technology, exploring some popular products](https://medium.datadriveninvestor.com/all-things-caching-use-cases-benefits-strategies-choosing-a-caching-technology-exploring-fa6c1f2e93aa)
