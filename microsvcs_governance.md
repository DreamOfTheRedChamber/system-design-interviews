# MicroSvcs_Governance-\[TODO]

* [MicroSvcs governance](microsvcs_governance.md#microsvcs-governance)
  * [Overview](microsvcs_governance.md#overview)
  * [Real world](microsvcs_governance.md#real-world)
    * [Netflix](microsvcs_governance.md#netflix)

## Overview

* Rate limit and fallback
  * [https://time.geekbang.org/course/detail/100003901-2278](https://time.geekbang.org/course/detail/100003901-2278))
  * [https://time.geekbang.org/column/article/40908](https://time.geekbang.org/column/article/40908)
* Circuit breaker and timeout
  * [https://time.geekbang.org/column/article/41297](https://time.geekbang.org/column/article/41297)

##

* [Microservices](broken-reference)
  * [Architecture](broken-reference)
  * [Capacity planning \[Todo\]](broken-reference)
  * [Org architecture \[Todo\]](broken-reference)
  * [Microservice layers](broken-reference)
  * [Tech architecture \[Todo\]](broken-reference)
  * [Problems](broken-reference)
    * [Data distribution](broken-reference)
      * [Use case](broken-reference)
      * [Approaches](broken-reference)
        * [Transactional outbox](broken-reference)
        * [CDC (Change data capture)](broken-reference)
    * [Data join](broken-reference)
      * [Use case](broken-reference)
      * [Approaches](broken-reference)
        * [CQRS](broken-reference)
    * [Breakdown monolithic](broken-reference)
      * [Definition](broken-reference)
      * [Steps to migration](broken-reference)
      * [Key difficult points](broken-reference)
      * [Example](broken-reference)
  * [Service mesh \[Todo\]](broken-reference)

## Microservices

### Architecture

* RPC vs message queue based: [https://time.geekbang.org/column/article/73368](https://time.geekbang.org/column/article/73368)

### Capacity planning \[Todo]

* [https://time.geekbang.org/column/article/44118](https://time.geekbang.org/column/article/44118)

### Org architecture \[Todo]

* [TODO: Kangwei principle](https://time.geekbang.org/course/detail/100003901-2154)
* [TODO: when to introduce microservices](https://time.geekbang.org/course/detail/100003901-2186)
* [TODO: when to breakdown monolithic](https://time.geekbang.org/column/article/13882)
* [TODO: devops, management platform](https://time.geekbang.org/column/article/41873)
* [TODO: how to divide](https://time.geekbang.org/column/article/72090)
* [TODO: servicemesh在复杂环境下的落地](https://tech.meituan.com/2020/12/03/service-mesh-in-meituan.html)

### Microservice layers

* [TODO: Alibaba's big Middletier, small front end](https://time.geekbang.org/course/detail/100003901-2188)
* [TODO: Microservice divides to different layers](https://time.geekbang.org/course/detail/100003901-2189)
* TODO: Microservice challenges
  * [https://time.geekbang.org/column/article/13891](https://time.geekbang.org/column/article/13891)
  * [https://time.geekbang.org/column/article/14222](https://time.geekbang.org/column/article/14222)

### Tech architecture \[Todo]

* [Routing architecture](https://time.geekbang.org/course/detail/100003901-2272)
* [Components](https://time.geekbang.org/course/detail/100003901-2222)
* Microservices management
  * [https://time.geekbang.org/course/detail/100003901-2275](https://time.geekbang.org/course/detail/100003901-2275)
  * [https://time.geekbang.org/column/article/18651](https://time.geekbang.org/column/article/18651)
  * [https://time.geekbang.org/column/article/41758](https://time.geekbang.org/column/article/41758)

### Problems

#### Data distribution

* Definition: Double write. How to guarantee the ACID of two writes

**Use case**

* Data replication
* Database migration
* Implement CQRS or remove join from database
* Implement distributed transactions

**Approaches**

**Transactional outbox**

* Flowchart

![MySQL HA github](images/microservices_transactionalOutbox.png)

* Implementation: Killbill common queue

![MySQL HA github](.gitbook/assets/microservices_transactionalOutbox_implementation.png)

**CDC (Change data capture)**

* Flowchart

![MySQL HA github](.gitbook/assets/microservices_changeDataCapture.png)

* Implementation:
  * Alibaba Canal (recommended)
  * Redhat Debezium
  * Zendesk Maxell
  * Airbnb SpinalTap

#### Data join

**Use case**

* N+1 problem
* Data volume
* Performance

**Approaches**

**CQRS**

* Flowchart

![MySQL HA github](images/microservices_join_cqrs.png)

* Possible problems: Eventual consistency

![MySQL HA github](images/microservices_join_cqrs_problem.png)

* Possible solutions:

![MySQL HA github](images/microservices_join_cqrs_problem_solution.png)

#### Breakdown monolithic

**Definition**

* Velocity slow
* Hard to continue scale vertically
* Independent deployment capability

**Steps to migration**

**Key difficult points**

* Understand the APIs
* Database migration
  * Incremental and rollbackable
  * Data 
* Use data distribution and remove join

**Example**

* StichFix - Scaling your architecture with services and events

### Service mesh \[Todo]

* Overview: [https://time.geekbang.org/column/article/65132](https://time.geekbang.org/column/article/65132)
* Istio: [https://time.geekbang.org/column/article/67172](https://time.geekbang.org/column/article/67172)
* Weibo implements service mesh:
  * [https://time.geekbang.org/column/article/67548](https://time.geekbang.org/column/article/67548)
  * [https://time.geekbang.org/column/article/67940](https://time.geekbang.org/column/article/67940)



## Real world

### Netflix

* Load shedding at Netflix: [https://netflixtechblog.com/keeping-netflix-reliable-using-prioritized-load-shedding-6cc827b02f94](https://netflixtechblog.com/keeping-netflix-reliable-using-prioritized-load-shedding-6cc827b02f94)
* Auto scaling: [https://netflixtechblog.com/auto-scaling-production-services-on-titus-1f3cd49f5cd7](https://netflixtechblog.com/auto-scaling-production-services-on-titus-1f3cd49f5cd7)
* Netflix Load balancing: [https://netflixtechblog.com/netflix-edge-load-balancing-695308b5548c](https://netflixtechblog.com/netflix-edge-load-balancing-695308b5548c)
* Netflix Application gateway: 
  * [https://netflixtechblog.com/open-sourcing-zuul-2-82ea476cb2b3](https://netflixtechblog.com/open-sourcing-zuul-2-82ea476cb2b3)
  * [https://netflixtechblog.com/zuul-2-the-netflix-journey-to-asynchronous-non-blocking-systems-45947377fb5c](https://netflixtechblog.com/zuul-2-the-netflix-journey-to-asynchronous-non-blocking-systems-45947377fb5c)
* Netflix failover: [https://netflixtechblog.com/project-nimble-region-evacuation-reimagined-d0d0568254d4](https://netflixtechblog.com/project-nimble-region-evacuation-reimagined-d0d0568254d4)
* Netflix DDOS simulation: [https://netflixtechblog.com/starting-the-avalanche-640e69b14a06](https://netflixtechblog.com/starting-the-avalanche-640e69b14a06)
* Netflix Chaos: [https://netflixtechblog.com/chap-chaos-automation-platform-53e6d528371f](https://netflixtechblog.com/chap-chaos-automation-platform-53e6d528371f)
* Netflix cluster management: [https://netflixtechblog.com/dynomite-manager-managing-dynomite-clusters-dfb6874228e4](https://netflixtechblog.com/dynomite-manager-managing-dynomite-clusters-dfb6874228e4)
* Netflix resource scheduling: [https://netflixtechblog.com/distributed-resource-scheduling-with-apache-mesos-32bd9eb4ca38](https://netflixtechblog.com/distributed-resource-scheduling-with-apache-mesos-32bd9eb4ca38)
* Netflix automated failure testing: [https://netflixtechblog.com/automated-failure-testing-86c1b8bc841f](https://netflixtechblog.com/automated-failure-testing-86c1b8bc841f)
* Netflix Chaos monkey: [https://netflixtechblog.com/netflix-chaos-monkey-upgraded-1d679429be5d](https://netflixtechblog.com/netflix-chaos-monkey-upgraded-1d679429be5d)
* Netflix open source projects: [https://netflixtechblog.com/evolution-of-open-source-at-netflix-d05c1c788429](https://netflixtechblog.com/evolution-of-open-source-at-netflix-d05c1c788429)
* 知名博主的分布式系列：[https://www.cnblogs.com/wt645631686/category/1793274.html](https://www.cnblogs.com/wt645631686/category/1793274.html)
* [https://time.geekbang.org/course/detail/100003901-2269](https://time.geekbang.org/course/detail/100003901-2269)
* Netflix's microservice architecture: [https://time.geekbang.org/course/detail/100003901-2272](https://time.geekbang.org/course/detail/100003901-2272)
