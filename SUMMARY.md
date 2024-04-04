# Table of contents

* [README](README.md)
* [ArchitectureTradeOffAnalysis](architectureAnalysis/overview.md)
  * [Estimation](architectureAnalysis/estimation_basics.md)
  * [Middleware](architectureAnalysis/middleware.md)
  * [Network](architectureAnalysis/network.md)
  * [Server](architectureAnalysis/server.md)
  * [Storage](architectureAnalysis/storage.md)
* [Conversion cheat sheet](https://docs.google.com/spreadsheets/d/18Hjr0f5msuCp_FCoFATEOU0jPqpsLwXjm7QDxdUtgJw/edit#gid=0)

## Distributed system theories
* [Elementary school with CAP](distributed_theories/CAP.md)
* [Consistency](distributed_theories/consistency_overview.md)
  * [Eventual with Gossip](distributed_theories/consistency_eventual_gossip.md)
  * [Strong with Raft](distributed_theories/consistency_strong_raft.md)
  * [Tunable with Quorum](distributed_theories/consistency_tunable_quorum.md)
  * [Fault tolerant with BFT-TODO](distributed_theories/consistency_faultTolerant.md)
  * [AutoMerge with CRDT](distributed_theories/CRDT.md)
  * [Distributed trans comparison](https://docs.google.com/spreadsheets/d/1Sw0T4R6-Bb3orF0abwkmiZRBbCioevH1jvyjnw7aqhs/edit?usp=sharing)
* [Time in distributed system](distributed_theories/time_overview.md)
  * [Logical time](distributed_theories/time_logicalClock.md)
  * [Physical time](distributed_theories/time_physicalClock.md)
* [ConsistentHashing](distributed_theories/consistenthashing.md)

* [DDIA\_Studying-\[TODO\]](distributed_theories/ddia\_studying.md)

## Protocols
* [ApiDesign](protocols/apidesign/README.md)
  * [REST](protocols/apidesign/rest.md)
  * [RPC](protocols/apidesign/rpc.md)
* [Websockets](protocols/websockets.md)
* [Serialization](protocols/serialization/overview.md)
  * [Thrift](protocols/serialization/thrift.md)
  * [Avro](protocols/serialization/avro.md)
* [HTTP](protocols/http.md)
* [HTTPS](protocols/https.md)
* [Netty-TODO](protocols/network_netty-todo.md)

## Statistical data structure
* [BloomFilter](statisticalDataStructure/bloomFilter.md)
* [HyperLoglog](statisticalDataStructure/hyperLogLog.md)
* [CountMinSketch](statisticalDataStructure/countMinSketch.md)

## Unique ID
* [TinyURL](./scenario_tinyurl/overview.md).
  * [Estimation](./scenario_tinyurl/estimation.md)
  * [Flowchart](./scenario_tinyurl/flowchart.md)
  * [Shortening mechanisms](./scenario_tinyurl/shorteningMechanism.md)
  * [Rest API](./scenario_tinyurl/restApi.md)
  * [Performance](./scenario_tinyurl/performance.md)
  * [Storage](./scenario_tinyurl/storage.md)
  * [Follow-up](./scenario_tinyurl/follow-up.md)
* [IDGenerator](scenario_idGenerator/overview.md)
  * [Database based](scenario_idGenerator/database-based.md)

## Parallel tasks
* [TaskScheduler](scenario\_taskScheduler/overview.md)
  * [Timer TimingWheel](scenario_taskScheduler/timer_timingWheel.md)
  * [Timer PriorityQueue](scenario_taskScheduler/timer_priorityQueue.md)
  * [Industrial Scheduler](scenario_taskScheduler/IndustrialScheduler.md)
  * [Workflow Engine](scenario_taskScheduler/workflowEngine.md)MD5 value
  * [Airflow Arch](scenario_taskScheduler/airflow_arch.md)
* [Distributed crawler](scenario_webcrawler/overview.md)
  * [Estimation](scenario_webcrawler/estimation.md)
  * [Flowchart](scenario_webcrawler/flowchart.md)
  * [Efficiency](scenario_webcrawler/efficiency.md)
  * [Robustness](scenario_webcrawler/robustness.md)
  * [Performance](scenario_webcrawler/performance.md)
  * [Storage](scenario_webcrawler/storage.md)
  * [Standalone implementation](scenario_webcrawler/standalone_impl.md)
  * [Python Scrapy framework](scenario_webcrawler/pythonScrapy.md)

## Google products
* [GoogleDrive](scenario_googleDrive/overview.md)
  * [Estimation](scenario_googleDrive/estimation.md)
  * [Flowchart](scenario_googleDrive/flowchart.md)
  * [Storage](scenario_googleDrive/storage.md)
  * [Follow-up](scenario_googleDrive/follow-up.md)
* [Youtube](scenario_onlinevideo/overview.md)
  * [Estimation](scenario_onlinevideo/estimation.md)
  * [Flowchart](scenario_onlinevideo/flowchart.md)
  * [Storage](scenario_onlinevideo/storage.md)
  * [Follow-up](scenario_onlinevideo/follow-up.md)
  * [Netflix](scenario_onlinevideo/netflix.md)
* [GoogleDoc](scenario_googleDoc/overview.md)
  * [CRDT](scenario_googleDoc/CRDT.md)

## Middleware
* [RateLimiter](scenario\_rateLimiter/overview.md)
  * [Rules](scenario_rateLimiter/rules.md)
  * [Algorithm comparison](https://docs.google.com/spreadsheets/d/1JlsYv0iUravuVMaf8re-qGhmCrmJeFe4vC99bfD9Rn4/edit?usp=sharing)
  * [Sliding window](scenario_rateLimiter/slidingWindow.md)
  * [Industrial impl](scenario_rateLimiter/industrialImpl.md)

## Search scenarios
* [ElasticSearch](search/elasticsearch.md)
* [Stream search](search/searchOnStreams.md)

## High volume/available
* [KV store](scenario_KvStore/overview.md)
  * [Standalone concurrent](scenario_KvStore/concurrent_KV.md)
  * [Master-slave KV](scenario_KvStore/masterSlave_KV.md)
  * [P2P KV-TODO](scenario_KvStore/peerToPeer_KV.md)
  * [Big table](scenario_KvStore/bigTable.md)
  * [Distributed cache](scenario_KvStore/distributedCache.md)
* [DistributedLock](scenario_distributedLock/overview.md)
  * [Single machine](scenario_distributedLock/singleMachineLock.md)
  * [AP model based](scenario_distributedLock/ap-Model.md)
  * [CP model based](scenario_distributedLock/cp-Model.md)
  * [Chubby-TODO](scenario_distributedLock/chubby.md)

## Time series scenarios
* [Observability](scenario_observability/overview.md)
  * [TimeSeries data](scenario_observability/timeSeriesData.md)
  * [Distributed traces](scenario_observability/distributedTraces.md)
  * [Logs](scenario_observability/log.md)
  * [Metrics](scenario_observability/metrics.md)
  * [NonFunc requirments](scenario_observability/nonFunc.md)

## Dynamic load scenarios
* [Payment system](scenario_payment/overview.md)
  * [Resilience](scenario_payment/nonFunc-Resilience.md)
  * [Consistency](scenario_payment/nonFunc-Consistency.md)

## Low latency scenarios
* [Typeahead](scenario\_typeahead.md)
* [LocationBasedService](scenario_locationBased/overview.md)
  * [Geohash](scenario_locationBased/geohash.md)

## Object storage scenarios
* [File system](scenario_fileSystem/overview.md)
  * [GFS 文件系统](scenario_fileSystem/GFS.md)
* [Instagram-\[TODO\]](scenario\_instagram-todo.md)
  * [Haystack](scenario_fileSystem/haystack.md)

## Batch/Stream processing scenarios
* [TopK](bigData/topK.md)
* [Stateful stream](bigData/statefulStream.md)
* [Lambda architecture](bigData/lambda.md)
* [storm架构](bigData/storm.md)
* [Beam架构](bigData/beam.md)
* [Comparing stream frameworks](https://docs.google.com/spreadsheets/d/1eHqhvDkXeGQAphZpTXgC0mrCpp6sz0fhVQxCblguiXU/edit?usp=sharing)

## Notifications scenarios
* [InstantMessenger](scenario_messenger/productOverview.md)
  * [Architecture overview](scenario_messenger/architectureOverview.md)
  * [Presence](scenario_messenger/presence.md)
  * [Unread count](scenario_messenger/unreadCount.md)
  * [Notifications](scenario_messenger/notifications.md)
  * [Read receipt](scenario_messenger/readreceipt.md)
  * [Large group chat](scenario_messenger/largeGroupChat.md)
  * [Storage-Offline 1:1 Chat](scenario_messenger/storageOfflineOneToOneChat.md)
  * [Storage-Offline group chat](scenario_messenger/storageOfflineGroupChat.md)
  * [Storage-Message roaming](scenario_messenger/storageMessageRoaming.md)
  * [NonFunc-Realtime](scenario_messenger/nonFuncRealtime.md)
  * [NonFunc-Reliability](scenario_messenger/nonFuncReliability.md)
  * [NonFunc-Ordering](scenario_messenger/nonFuncOrdering.md)
  * [NonFunc-Security](scenario_messenger/nonFuncSecurity.md)
  * [Livecast-LinkedIn](scenario_messenger/livecastLinkedIn.md)
* [NewsFeed-\[TODO\]](scenario\_newsfeed.md)

## Message queue
* [Overview](messageQueue/overview.md)
* [Kafka components](messageQueue/kafka-components.md)
* [Kafka nonFunc](messageQueue/kafka-nonFunc.md)
* [ActiveMQ-TODO](messageQueue/activeMQ.md)
* [RabbitMQ-TODO](messageQueue/rabbitMQ.md)
* [RocketMQ-TODO](messageQueue/rocketMQ.md)
* [Comparison between MQ](https://docs.google.com/spreadsheets/d/1Mgo3VqiHSpWJJV8ew9kgf2-Y1HlnMutLXOp040DTy-Y/edit#gid=0)

## Server
* [Nginx-\[TODO\]](server\_nginx-todo.md)

## Cache

* [Distributed cache overview](cache/distributedcache.md)
  * [Access patterns](cache/distributedcacheAccessPatterns.md)
  * [Common issues](cache/distributedcacheCommonIssues.md)
  * [Eviction strategy](cache/distributedCacheEviction.md)
* [Cache\_Redis](cache/redis.md)
  * [Data structure](cache/redisDataStructure.md)
  * [ACID](cache/redisAcid.md)
  * [Performance](cache/redisPerformance.md)
  * [Availability](cache/redisAvailability.md)
  * [Cluster](cache/redisCluster.md)
  * [Applications](cache/redisApplications.md)
* [Cache\_Memcached](cache/memcached.md)

## Storage

* [DistributedAcidDatabase-\[TODO\]](storage\_distributedaciddatabase.md)
* [MySQL](storage_mySQL/README.md)
  * [Data structure](storage_mySQL/mysql_datastructure.md)
  * [Schema design](storage_mySQL/mysql_schemadesign.md)
  * [Perf optimization](storage_mySQL/mysql_perfOptimization.md)
  * [ACID](storage_mySQL/mysql_ACID.md)
  * [High availability](storage_mySQL/mysql_highavailability.md)
  * [Scalability](storage_mySQL/mysql_scalability.md)
  * [Partition and sharding](storage_mySQL/mysql_partitionAndSharding.md)
* [NoSQL](storage_nosql/README.md)
  * [Rum guess](storage_nosql/rumconjecture.md)
  * [Data structure](storage_nosql/nosql_datastructure.md)
  * [MySQL based key value](storage_nosql/mysql_keyValue.md)
  * [KeyValueStore](storage_nosql/storage_keyvaluestore.md)
  * [ObjectStore](storage_nosql/storage_objectstore.md)
  * [TableStore-\[TODO\]](storage_nosql/storage_tablestore-todo.md)
  * [Time series DB](storage_nosql/timeSeriesDB.md)

## MicroSvcs

* [MicroSvcs\_ApiGateway-\[TODO\]](microsvcs\_apigateway-todo.md)
* [MicroSvcs\_ConfigCenter-\[TODO\]](microsvcs\_configcenter-todo.md)
* [MicroSvcs\_DDD-\[TODO\]](microsvcs\_ddd-todo.md)
* [MicroSvcs\_Governance-\[TODO\]](microsvcs\_governance.md)
* [MicroSvcs\_LoadBalancing](microsvcs\_loadbalancing.md)
* [MicroSvcs\_Security](microsvcs/microsvcs\_security/README.md)
  * [Authentication](microsvcs/microsvcs\_security/authentication.md)
  * [Authorization](microsvcs/microsvcs\_security/authorization.md)
  * [Privacy](microsvcs/microsvcs\_security/privacy.md)
* [MicroSvcs\_BreakMonolithic-\[TODO\]](microsvcs\_breakingmonolithic-todo.md)
* [RegistryCenter-Overview](registrycenter-overview.md)
* [RegistryCenter\_Zookeeper](registrycenter\_zookeeper.md)
* [RegistryCenter-Etcd-\[TODO\]](registrycenter-etcd-todo.md)

## DevOps

* [Container\_Docker](container\_docker.md)
* [Container\_Kubernetes-\[TODO\]](container\_kubernetes.md)

## Network components
* [CDN](networkComponents/cdn.md)
* [DNS](networkComponents/dns.md)
* [Load balancer](networkComponents/loadbalancer.md)
* [Reverse proxy](networkComponents/reverse_proxy.md)
* [云中网络-TODO](networkComponents/cloud-network-todo.md)

## Multi-threading

* [Overview](algorithm\_multithreading.md)
* [Codes](code/multithreads/README.md)
  * [ThreadLocal](code/multithreads/threadlocal.md)
  * [ThreadPool](code/multithreads/threadpool.md)
  * [ThreadLifeCycle](code/multithreads/threadlifecycle.md)
  * [SingletonPattern](code/multithreads/singletonpattern.md)
  * [Future](code/multithreads/future.md)
  * [BlockingQueue](code/multithreads/blockingqueue.md)
  * [Counter](code/multithreads/counter.md)
  * [ConcurrentHashmap](code/multithreads/concurrenthashmap.md)
  * [DelayedQueue](code/multithreads/delayedqueue.md)

## Templates

* [interviewRecord](interviewrecord.md)
* [code](code/README.md)
  * [RateLimiter\_TokenBucket](code/ratelimiter\_tokenbucket.md)
  * [消息队列高手课：动手实现一个简单的RPC框架](code/simple-rpc-framework-master.md)

## TODO
* [RecommendationSystem-\[TODO\]](scenario\_recommendationsystem-todo.md)
* [SearchEngine-\[TODO\]](scenario\_searchengine-todo.md)
* [SessionServer-[TODO]](scenario\_sessionserver.md)
* [Disk](infrastructure_disk.md)
* [Unix philosophy and Kafka](unix/philosophy_andkafka.md)
* [Bitcoin](scenario_bitcoin.md)
* Design pattern
  * [StateMachine](designpattern_statemachine.md)
  * [Factory](designpattern_factory.md)
* [Akka](computation_akka.md)
