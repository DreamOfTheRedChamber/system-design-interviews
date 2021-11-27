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

# Cache aside
## Use case

* Most widely used pattern in distributed applications. Popular cache frameworks such as Redis / Memcached opt this approach by default. 

## Potential issues

### Data inconsistency

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

### Cache hit ratio

* When write operation happens frequently, cache data will be invalidated frequently. As a result, the cache hit ratio might suffer. Two possible solutions:
  * Update cache while update database, and put two operations in a distributed lock. 
  * Update cache while update database, and set a low expiration time for cache.

### Flowchart

![Cache aside pattern](../.gitbook/assets/cache_cacheaside_pattern.png)

### What if update cache instead of invalidate cache after writing to DB

* It will cause the following two problems: 
  * Data inconsistency
  * Lose update

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

### What if invalidate cache first and then write to DB

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

# Read/Write through

## Use case

* Client does not need to manage two connections towards cache and repository, separately. Everything could be managed by the cache itself. 
* Used more often in local cache (e.g. Guava cache's loading cache)

## Potential issues

### Low performance

* Write to database and write to cache happens synchronously. 

## Flowchart

* def: write go through the cache and write is confirmed as success only if writes to DB and the cache both succeed.

![Read write through pattern](.gitbook/assets/cache_readwritethrough_pattern.png)

# Write behind/back cache

## Use case

* Used more often in operating system's write to cache
* When you need quick write latency and high write throughput. 
  * Linux page cache algorithm
  * Asynchronously write message to disk in message queue

## Potential issues

### Lose update

* Message is not persisted to disk asynchronouly

## Flowchart

![write behind pattern](images/cache_writebehind_pattern.png)

# Write around cache

* def: write directly goes to the DB. The cache reads the info from DB in case of a miss
* use-case: lower write load to cache and faster writes, but can lead to higher read latency in case of applications which write and re-read the information quickly
