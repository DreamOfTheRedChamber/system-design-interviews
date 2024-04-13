- [Intuition](#intuition)
- [Factors for hit ratio](#factors-for-hit-ratio)
- [Applicable scenarios](#applicable-scenarios)
  - [Selection criteria](#selection-criteria)
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

## Selection criteria
* Performance: Cache should be able to constantly sustain the throughput requirements in terms of read or write queries from application. So the more it is able to take advantage of resources like — RAM, SSD or Flash, CPU etc, the better it is at producing output.
* Scalability: Caching system has to be able to maintain steady performance even if number of operations, requests, users & amount of data flow increases. It must be able to scale linearly without any adverse impact. So elastically growing up or down is an important characteristic.
* Availability: High availability is the utmost requirement in today’s systems. It’s fine to get stale data ( depending on use case ) but unavailable systems are not desired. Whether there is a planned or unplanned outage, or a portion of system is crashed or due to natural calamity some data centre is non-operational, cache has to be available all the time.
* Manageability: Easy deployment, monitoring, useful dashboard, real-time matrices make every developer & SRE’s life simple.
* Simplicity: All other things equal, simplicity is always better. Adding a cache to your deployment should not introduce unnecessary complexity or make more work for developers.
* Affordability: Cost is always a consideration with any IT decision, both upfront implementation as well as ongoing costs. Your evaluation should consider total cost of ownership, including license fees as well as hardware, services, maintenance, and support.

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

