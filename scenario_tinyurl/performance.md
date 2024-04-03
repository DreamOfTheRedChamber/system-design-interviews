- [Performance](#performance)
  - [Scale read with cache](#scale-read-with-cache)
    - [Cluster size](#cluster-size)
    - [One master multiple slaves](#one-master-multiple-slaves)
      - [Pros](#pros)
      - [Cons](#cons)
    - [Optimize based on geographical info](#optimize-based-on-geographical-info)


# Performance

## Scale read with cache

### Cluster size
* Suppose cache (e.g. redis) has a retention time of 7 days. 
* Every 7 days, there needs to be 0.25 billion urls being accessed.
* Each entry has a 1KB size. Then cluster size will be 1KB * 0.25 billion = 0.25 * 10^12 bytes = 250 GB.

### One master multiple slaves
* Write to master, streamly replicated to slaves, usually less than one second Read from slaves.

#### Pros
* Increase availability to handle single point of failure
* Reduce read pressure on a single node

#### Cons
* Replication lag
  * Solution1: We can write to memcache when creating a new tiny url. Service can get tiny url from memcache instead of database. 
  * Solution2: We can implement Raft protocol against relational database or use opensource or commercial realted database. 

### Optimize based on geographical info

* Web server
  * Different web servers deployed in different geographical locations
  * Use DNS to parse different web servers to different geographical locations
* Database 
  * Centralized MySQL + Distributed memcached server
  * Cache server deployed in different geographical locations

