# Table of contents

* [README](README.md)
* [ArchitectureTradeOffAnalysis](architectureAnalysis/overview.md)
  * [Middleware](architectureAnalysis/middleware.md)
  * [Network](architectureAnalysis/network.md)
  * [Server](architectureAnalysis/server.md)
  * [Storage](architectureAnalysis/storage.md)
* [Conversion cheat sheet](https://docs.google.com/spreadsheets/d/18Hjr0f5msuCp_FCoFATEOU0jPqpsLwXjm7QDxdUtgJw/edit#gid=0)

## Typical scenarios

* [DistributedLock](scenario_distributedLock/overview.md)
  * [Single machine](scenario_distributedLock/singleMachineLock.md)
  * [AP model based](scenario_distributedLock/ap-Model.md)
  * [CP model based](scenario_distributedLock/cp-Model.md)
  * [Chubby-TODO]
* [File system](scenario_fileSystem/overview.md)
  * [GFS 文件系统](scenario_fileSystem/GFS.md)
  * [Haystack](scenario_fileSystem/haystack.md)
* [KV store](scenario_KvStore/overview.md)
  * [Standalone concurrent](scenario_KvStore/concurrent_KV.md)
  * [Master-slave KV](scenario_KvStore/masterSlave_KV.md)
  * [P2P KV-TODO](scenario_KvStore/peerToPeer_KV.md)
  * [Big table](scenario_KvStore/bigTable.md)
  * [Distributed cache](scenario_KvStore/distributedCache.md)
* [Observability](scenario_observability/overview.md)
  * [TimeSeries data](scenario_observability/timeSeriesData.md)
  * [Distributed traces](scenario_observability/distributedTraces.md)
  * [Logs](scenario_observability/log.md)
  * [Metrics](scenario_observability/metrics.md)
  * [NonFunc requirments](scenario_observability/nonFunc.md)
* [RateLimiter](scenario\_rateLimiter/overview.md)
  * [Rules](scenario_rateLimiter/rules.md)
  * [Algorithm](scenario_rateLimiter/algorithm.md)
  * [Deployment mode](scenario_rateLimiter/mode.md)
* [Big data](scenario_bigData/overview.md)
  * [TopK algorithm](scenario_bigData/topKalgorithm.md)
  * [Lambda overview](scenario_bigData/lambda-overview.md)
  * [Lambda batch layer](scenario_bigData/lambda-batch.md)
  * [Lambda serving layer](scenario_bigData/lambda-serving.md)
  * [Lambda speed layer](scenario_bigData/lambda-speed.md)
  * [storm架构](scenario_bigData/storm.md)
* [Crawler](scenario_webcrawler/overview.md)
  * [Multithreaded](scenario_webcrawler/multithreaded_crawler.md)
  * [Components](scenario_webcrawler/components.md)
  * [Dedupe](scenario_webcrawler/dedupe.md)
  * [Scaling](scenario_webcrawler/scaling.md)
  * [DHT crawler TODO](scenario_webcrawler/DHT-crawler.md)
* [LocationBasedService](scenario_locationBased/overview.md)
  * [Geohash](scenario_locationBased/geohash.md)
* [GoogleDoc](scenario_googleDoc/overview.md)
  * [CRDT](scenario_googleDoc/CRDT.md)
* [GoogleDrive](scenario\_googledrive.md)
* [IDGenerator](scenario_idGenerator/overview.md)
  * [Database based](scenario_idGenerator/database-based.md)
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
* [Online video](scenario_onlinevideo/overview.md)
  * [Netflix](scenario_onlinevideo/netflix.md)
* [Payment system](scenario_payment/overview.md)
  * [Resilience](scenario_payment/nonFunc-Resilience.md)
  * [Correctness-Saga&Outbox](scenario_payment/nonFunc-Correctness.md)
  * [Correctness-DX Comp](https://docs.google.com/spreadsheets/d/1Sw0T4R6-Bb3orF0abwkmiZRBbCioevH1jvyjnw7aqhs/edit?usp=sharing)
* [TaskScheduler](scenario\_taskScheduler/overview.md)
  * [Timer TimingWheel](scenario_taskScheduler/timer_timingWheel.md)
  * [Timer PriorityQueue](scenario_taskScheduler/timer_priorityQueue.md)
  * [Industrial Scheduler](scenario_taskScheduler/IndustrialScheduler.md)
  * [Workflow Engine](scenario_taskScheduler/workflowEngine.md)
* [TinyURL](scenario\_tinyurl.md)
* [Typeahead](scenario\_typeahead.md)
* [RecommendationSystem-\[TODO\]](scenario\_recommendationsystem-todo.md)
* [SearchEngine-\[TODO\]](scenario\_searchengine-todo.md)
* [Instagram-\[TODO\]](scenario\_instagram-todo.md)
* [SessionServer-[TODO]](scenario\_sessionserver.md)

## Protocols
* [ApiDesign](protocols/apidesign/README.md)
  * [REST](protocols/apidesign/rest.md)
  * [RPC](protocols/apidesign/rpc.md)
* [Websockets](protocols/websockets.md)
* [Serialization](protocols/serialization/overview.md)
  * [Thrift](protocols/serialization/thrift.md)
* [HTTP](protocols/http.md)
* [HTTPS](protocols/https.md)
* [Netty-TODO](protocols/network_netty-todo.md)

## Statistical data structure
* [BloomFilter](statisticalDataStructure/bloomFilter.md)
* [HyperLoglog](statisticalDataStructure/hyperLogLog.md)
* [CountMinSketch](statisticalDataStructure/countMinSketch.md)

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

* [DDIA\_Studying-\[TODO\]](ddia\_studying.md)
* [DistributedAcidDatabase-\[TODO\]](storage\_distributedaciddatabase.md)
* [ElasticSearch-\[TODO\]](storage\_elasticsearch.md)
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

## Distributed algorithm

* [Clock](algorithm\_clock.md)
* [Consistency-\[TODO\]](algorithm\_consistency-todo.md)
* [ConsistentHashing](algorithm\_consistenthashing.md)
* [DistributedConsensus](algorithm\_distributedconsensus.md)
* [ErrorRecovery-\[TODO\]](algorithm\_errorrecovery-todo.md)

## Computation
* [Akka](computation_akka.md)

## DevOps

* [Container\_Docker](container\_docker.md)
* [Container\_Kubernetes-\[TODO\]](container\_kubernetes.md)

## Network component
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

## Design pattern
* [StateMachine](designpattern_statemachine.md)
* [Factory](designpattern_factory.md)

## Hardware level
* [Disk](infrastructure_disk.md)

## Blockchain
* [Bitcoin](scenario_bitcoin.md)

## Templates

* [interviewRecord](interviewrecord.md)
* [code](code/README.md)
  * [RateLimiter\_TokenBucket](code/ratelimiter\_tokenbucket.md)
  * [消息队列高手课：动手实现一个简单的RPC框架](code/simple-rpc-framework-master.md)
