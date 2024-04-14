- [Cache aside](#cache-aside)
  - [Flowchart](#flowchart)
  - [Cons: Data inconsistency](#cons-data-inconsistency)
- [Read through](#read-through)
  - [Flowchart](#flowchart-1)
  - [Cons: Data inconsistency](#cons-data-inconsistency-1)
  - [Asynchronous version](#asynchronous-version)
- [Write through](#write-through)
  - [Flowchart](#flowchart-2)
  - [Cons: Data inconsistency](#cons-data-inconsistency-2)
  - [Asynchronous version](#asynchronous-version-1)
- [Write back](#write-back)
  - [Flowchart](#flowchart-3)
  - [Pros](#pros)
    - [Data consistency](#data-consistency)
    - [Performance](#performance)
  - [Cons: Data loss](#cons-data-loss)
- [Refresh ahead](#refresh-ahead)
  - [Flowchart](#flowchart-4)
  - [Pros](#pros-1)
  - [Cons](#cons)
- [References](#references)

# Cache aside
## Flowchart
* Doing nothing special when using cache. Treat the database and cache as independent data sources. 
* First write DB, then cache; read from cache, then DB.  
* Most widely used pattern in distributed applications. Popular cache frameworks such as Redis / Memcached opt this approach by default. 

![Cache aside pattern](../.gitbook/assets/cache_cacheaside_pattern.png)

## Cons: Data inconsistency
* Data inconsistency could happen in multiple cases:
  * Two parallel write 
  * One read and one write

![Cache aside data inconistency](../.gitbook/assets/cache_cacheaside_cons.png)

# Read through

## Flowchart
* In read path, cache will act on behalf of client; on write path, it is the same as cache aside. 

![Cache read through](../.gitbook/assets/cache_readthrough.png)

## Cons: Data inconsistency
* Since it shares the same write path as cache aside, it has the same data inconsistency issue with cache aside. 

## Asynchronous version
* For the step to update cache, put it in an asynchronous job. This step will only improve the perf a lot when the cached item is large. 

![Cache read through](../.gitbook/assets/cache_readthrough_improve.png)

# Write through
* In the read path, it is the same as cache aside; in the write path, cache will act on behalf of client.

## Flowchart

![Cache write through](../.gitbook/assets/cache_writethrough.png)

## Cons: Data inconsistency
* It has similar problem.  

## Asynchronous version
* For the step to update cache, put it in an asynchronous job. This step will only improve the perf a lot when the cached item is large. 

![Cache read through](../.gitbook/assets/cache_writethrough_improve.png)

# Write back
* Suitable for high read & write throughput system. Used more often in operating system's write to cache
  * Linux page cache algorithm
  * Asynchronously write message to disk in message queue

## Flowchart
* When updating data, only update cache. When cache keys expire, these entries will be persisted to DB. 

![write back pattern](../.gitbook/assets/cache_write_back.png)

## Pros
### Data consistency
* This pattern could greatly reduce the inconsistency problem if handled properly. 
  * When there is no data in DB, then cache is source of truth
  * When there is data in DB, as long as SETNX is used, the "write back" operation on read path will not result in cache & DB inconsistency. 

![write back pattern](../.gitbook/assets/cache_write_back_consistency.png)

### Performance
* Since the application writes only to the caching service, it does not need to wait till data is written to the underlying data source. Read and write both happens at the caching side. Thus it improves performance.

## Cons: Data loss
* If cache suddenly crashes, then the data cached inside will all be lost. 

# Refresh ahead
## Flowchart
So what refresh ahead caching does is it essentially refreshes the cache at a configured interval just before the next possible cache access although it might take some time due to network latency to refresh the data & meanwhile few thousand read operation already might have happened in a very highly read heavy system in just a duration of few milliseconds.

## Pros
* It’s useful when large number of users are using the same cache keys. Since the data is refreshed periodically & frequently, staleness of data is not a permanent problem.
* Reduced latency than other technique like Read Through cache.

## Cons
* Probably a little hard to implement since cache service takes extra pressure to refresh all the keys as and when they are accessed. But in a read heavy environment, it’s worth it.

# References
* https://medium.datadriveninvestor.com/all-things-caching-use-cases-benefits-strategies-choosing-a-caching-technology-exploring-fa6c1f2e93aa