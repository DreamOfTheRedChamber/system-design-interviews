- [Middleware](#middleware)
  - [Cache](#cache)
    - [Single Redis instance](#single-redis-instance)
  - [Message queue](#message-queue)
    - [Kafka benchmark perf](#kafka-benchmark-perf)

# Middleware
## Cache

| Deployment           | Capacity / Performance | Other criteria               |
| -------------------- | ---------------------- | ---------------------------- |
| Replication mode     | Item size              | Cold vs hot data ratio       |
| Failover strategy    | Item number            | Cache penetration            |
| Persistence strategy | Item expiration date   | Cache big items              |
| Eviction strategy    | Data structure         | How to handle race condition |
| Thread model         | Peak write traffic     | Whether use Lua script       |
| Warm up strategy     | Peak read traffic      | Sharing tool (Proxy/Client)  |
| Sharding strategy    |                        |                              |

### Single Redis instance
* Read: 50k (20K \~ 100K)
* Write: 50K (20K \~ 100K)
* Capacity: 32 GB

## Message queue

| Deployment           | Capacity / Performance      | Other criteria             |
| -------------------- | --------------------------- | -------------------------- |
| Replication mode     | Daily incremental data size | Consumer thread pool model |
| Failover strategy    | Persistence duration        | Sharding strategy          |
| Persistence strategy | Peak read traffic           | Reliable msg delivery      |
|                      | Peak write traffic          | Consumer strategy          |
|                      | Average latency             | Consumer strategy          |
|                      | Max latency                 | Consumer strategy          |


### Kafka benchmark perf
* https://developer.confluent.io/learn/kafka-performance/
* Peak Throughput	605 MB/s
* p99 Latency	5 ms (200 MB/s load)
