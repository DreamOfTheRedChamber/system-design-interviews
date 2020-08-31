<!-- MarkdownTOC -->

- [Multi-DC](#multi-dc)
	- [Motivation](#motivation)
	- [Two DCs deployment mode](#two-dcs-deployment-mode)
		- [Differences between deployment mode](#differences-between-deployment-mode)
		- [Two DCs in the same city](#two-dcs-in-the-same-city)
		- [Two DCs in different cities](#two-dcs-in-different-cities)
		- [Architecture](#architecture)
		- [Change process](#change-process)
		- [Routing key](#routing-key)
			- [Failover process](#failover-process)
		- [Data synchronization](#data-synchronization)
			- [Cache synchronization](#cache-synchronization)
			- [MySQL data replication](#mysql-data-replication)
				- [DRC architecture](#drc-architecture)
				- [SCN](#scn)
			- [NoSQL data replication](#nosql-data-replication)
			- [NewSQL data replication](#newsql-data-replication)
				- [Oceanbase data replication](#oceanbase-data-replication)
				- [TiDB data replication](#tidb-data-replication)
				- [How to avoid circular replication](#how-to-avoid-circular-replication)
				- [How to recover from replication failure](#how-to-recover-from-replication-failure)
				- [How to avoid conflict](#how-to-avoid-conflict)
				- [How to avoid conflict](#how-to-avoid-conflict-1)
		- [Global zone service](#global-zone-service)
		- [Global eZone](#global-ezone)
	- [Multi DC in same city](#multi-dc-in-same-city)
		- [Architecture](#architecture-1)
	- [Multi DC in different city](#multi-dc-in-different-city)
		- [Three DC in two cities](#three-dc-in-two-cities)
			- [Initial design](#initial-design)
			- [Improved design](#improved-design)
		- [Five DC in three cities](#five-dc-in-three-cities)
	- [Typical architecture](#typical-architecture)
		- [Read intensive](#read-intensive)
		- [Read/write balanced](#readwrite-balanced)
		- [CRG Units](#crg-units)

<!-- /MarkdownTOC -->


# Multi-DC
## Motivation
* Reduce latency
* Disaster recovery

## Two DCs deployment mode

### Differences between deployment mode
* Typically the service latency should be smaller than 200ms. 

|        Name      |  Round trip latency |              Example       | Num of cross DC calls  |
|------------------|---------------------|----------------------------|------------------------|
| Within same city |      1ms-3ms        |  Two DCs within same city  | Smaller than a hundred |
| Across region    |      10ms-50ms      |  New York and Los Angeles  | Smaller than a couple  |
| Across continent |      100ms-200ms    |  Australia and USA.        | Avoid completely       |

* The following table summarizes the differences of these two modes

|     Dimensions   |  Round trip latency |              Example       |
|------------------|---------------------|----------------------------|
| Within same city |      1ms-3ms        |  Two DCs within same city  |
| Across region    |      10ms-50ms      |  New York and Los Angeles  |
| Across continent |      100ms-200ms    |  Australia and USA.        |

### Two DCs in the same city

```
┌───────────────────────────────────────────────┐     ┌────────────────────────────────────┐
│                      DC1                      │     │                DC2                 │
│                                               │     │                                    │
│      ┌─────────────────────────────────────┐  │     │    ┌─────────────────────────────┐ │
│      │                                     │  │     │    │                             │ │
│      │         Application Servers         │  │     │    │     Application Servers     │ │
│      │                                     │  │     │    │                             │ │
│      └─────────────────────────────────────┘  │     │    └─────────────────────────────┘ │
│                        │                      │     │                    │      │        │
│                        │                      │     │                    │      │        │
│                        │                      │     │                    │      │        │
│                        │                      │     │                    │      │        │
│                        │                      │     │                    │      │        │
│            ┌───read────┘           ┌──────────┼─────┼─────write──────────┘      │        │
│            │                       │          │     │                         read       │
│            │                       │          │     │                           │        │
│            │                       │          │     │                           │        │
│            │                       │          │     │                           │        │
│            │                       │          │     │                           │        │
│            ▼                       ▼          │     │                           ▼        │
│  ┌──────────────────┐    ┌──────────────────┐ │     │              ┌──────────────────┐  │
│  │    read slave    │    │   write master   │ │     │              │    read slave    │  │
│  │    components    │    │    components    │ │     │              │    components    │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │┌───────────┐     │    │  ┌───────────┐   │ │     │              │    ┌───────────┐ │  │
│  ││   Slave   │     │    │  │  Master   │   │ │     │              │    │  Service  │ │  │
│  ││  service  ◀─synchronize─┤  service  │ ──┼─┼─────synchronize────┼──▶ │ discovery │ │  │
│  ││ discovery │     │    │  │ discovery │   │ │     │              │    │           │ │  │
│  │└───────────┘     │    │  └───────────┘   │ │     │              │    └───────────┘ │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │                  │    │                  │ │     │              │                  │  │
│  │┌───────────┐     │    │  ┌───────────┐   │ │     │              │    ┌───────────┐ │  │
│  ││   Slave   │     │    │  │  Master   │   │ │     │              │    │           │ │  │
│  ││ Database ◀┼synchronize──┼─  Cache   │───┼─┼───synchronize──────┼──▶ │Slave Cache│ │  │
│  ││           │     │    │  │           │   │ │     │              │    │           │ │  │
│  │└───────────┘     │    │  └───────────┘   │ │     │              │    └───────────┘ │  │
│  │                  │    │         │        │ │     │              │                  │  │
│  │                  │    │         │        │ │     │              │                  │  │
│  │                  │    │         │        │ │     │              │                  │  │
│  │                  │    │         │        │ │     │              │                  │  │
│  │                  │    │         │        │ │     │              │                  │  │
│  │                  │    │         ▼        │ │     │              │                  │  │
│  │┌───────────┐     │    │   ┌───────────┐  │ │     │              │    ┌───────────┐ │  │
│  ││   Slave   │     │    │   │  Master   │  │ │     │              │    │   Slave   │ │  │
│  ││ Database  ◀─synchronize──│ Database  │──┼─┼───synchronize──────┼───▶│ Database  │ │  │
│  ││           │     │    │   │           │  │ │     │              │    │           │ │  │
│  │└───────────┘     │    │   └───────────┘  │ │     │              │    └───────────┘ │  │
│  │                  │    │                  │ │     │              │                  │  │
│  └──────────────────┘    └──────────────────┘ │     │              └──────────────────┘  │
│                                               │     │                                    │
│                                               │     │                                    │
└───────────────────────────────────────────────┘     └────────────────────────────────────┘
```

### Two DCs in different cities

```
┌───────────────────────────────────────────────┐     ┌──────────────────────────────────────────────┐
│                      DC1                      │     │                     DC2                      │
│                                               │     │                                              │
│  ┌─────────────────────────────────────────┐  │     │ ┌─────────────────────────────────────────┐  │
│  │                                         │  │     │ │                                         │  │
│  │           Application Servers           │  │     │ │           Application Servers           │  │
│  │                                         │  │     │ │                                         │  │
│  └─────────────────────────────────────────┘  │     │ └─────────────────────────────────────────┘  │
│           │                     │             │     │                                │             │
│           │                     │             │     │             │                  │             │
│           │                     │             │     │             │                  │             │
│           │                     │             │     │             │                  │             │
│           │                     │             │     │             │                  │             │
│         read                 write            │     │             │               write            │
│           │                     │             │     │           read                 │             │
│           │                     │             │     │             │                  │             │
│           │                     │             │     │             │                  │             │
│           │                     │             │     │             │                  │             │
│           │                     │             │     │             │                  │             │
│           ▼                     ▼             │     │             ▼                  ▼             │
│  ┌────────────────┐  ┌────────────────┐       │     │     ┌───────────────┐   ┌──────────────────┐ │
│  │   read slave   │  │  write master  │       │     │     │ write master  │   │    read slave    │ │
│  │   components   │  │   components   │       │     │     │  components   │   │    components    │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │  ┌────┴─────┴─┐   │               │   │                  │ │
│  │┌───────────┐   │  │ ┌───────────┐  │  │            │   │ ┌───────────┐ │   │  ┌───────────┐   │ │
│  ││   Slave   │   │  │ │  Master   │  │  │  message   │   │ │  Master   │ │   │  │   Slave   │   │ │
│  ││  service  ◀──sync┼─┼─ service  │◀─┼──┤ queue for ─┼───┼▶│  service ─┼─sync┼──▶  service  │   │ │
│  ││ discovery │   │  │ │ discovery │  │  │    sync    │   │ │ discovery │ │   │  │ discovery │   │ │
│  │└───────────┘   │  │ └───────────┘  │  │            │   │ └───────────┘ │   │  └───────────┘   │ │
│  │                │  │                │  │            │   │               │   │                  │ │
│  │                │  │                │  └────┬─────┬─┘   │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │┌───────────┐   │  │ ┌───────────┐  │       │     │     │ ┌───────────┐ │   │  ┌───────────┐   │ │
│  ││   Slave   │   │  │ │  Master   │  │      direct db    │ │  Master   │ │   │  │           │   │ │
│  ││ Database ◀┼─sync─┼─┤   Cache   │  │◀──────┼sync─┼────▶│ │ Database  ├─sync┼──▶Slave Cache│   │ │
│  ││           │   │  │ │           │  │       │     │     │ │           │ │   │  │           │   │ │
│  │└───────────┘   │  │ └───────────┘  │       │     │     │ └───────────┘ │   │  └───────────┘   │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  │┌───────────┐   │  │ ┌───────────┐  │       │     │     │ ┌───────────┐ │   │   ┌───────────┐  │ │
│  ││   Slave   │   │  │ │  Master   │  │     direct db     │ │  Master   │ │   │   │   Slave   │  │ │
│  ││ Database  ◀─sync─┼─│ Database  │  ◀───────sync──┼────▶│ │ Database  │─┼sync──▶│ Database  │  │ │
│  ││           │   │  │ │           │  │       │     │     │ │           │ │   │   │           │  │ │
│  │└───────────┘   │  │ └───────────┘  │       │     │     │ └───────────┘ │   │   └───────────┘  │ │
│  │                │  │                │       │     │     │               │   │                  │ │
│  └────────────────┘  └────────────────┘       │     │     └───────────────┘   └──────────────────┘ │
│                                               │     │                                              │
│                                               │     │                                              │
└───────────────────────────────────────────────┘     └──────────────────────────────────────────────┘
```


### Architecture

![Differences](./images/multiDC-sameCityMultiCityDiff.jpg)

### Change process
1. Categorize the business
2. Categorize the data
3. Pick the correct synchronization mechanism
	* method1
4. Exception handling
	* method1

### Routing key

![Routing architecture](./images/multiDC-routingArchitecture.jpg)

![Routing key](./images/multiDC-routingKey.jpg)

![Gslb](./images/multiDC-routingGslb.png)

![Gslb refined](./images/multiDC-routingGslbRefined.png)



#### Failover process

![Failover process](./images/multiDC-routing-failover.jpg)



### Data synchronization

![Data synchronization](./images/multiDC-datasynchronization.jpg)

#### Cache synchronization

![Data synchronization](./images/multiDC-mySQLInternals.jpg)

![Data synchronization approaches](./images/multiDC-datasynchronizationMethods.png)



#### MySQL data replication

##### DRC architecture

![DRC architecture](./images/multiDC-DRC-architecture.jpg)

##### SCN

![SCN](./images/multiDC-SCN.jpg)

![Replicator](./images/multiDC-replicator.jpg)

![Data apply](./images/multiDC-dataapply.jpg)

#### NoSQL data replication

![noSQL](./images/multiDC-noSQL.webp)

#### NewSQL data replication

##### Oceanbase data replication

![Oceanbase](./images/multiDC-oceanbaseFiveDCsInThreeCities.jpg)

##### TiDB data replication

![newSQL1](./images/multiDC-newSQL1.webp)

![newSQL2](./images/multiDC-newSQL2.webp)

![newSQL3](./images/multiDC-newSQL3.webp)

![newSQL4](./images/multiDC-newSQL4.webp)


##### How to avoid circular replication

![Circular replication](./images/multiDC-avoidCircularReplication.jpg)

##### How to recover from replication failure

![Circular replication](./images/multiDC-recoverFromFailure.jpg)

##### How to avoid conflict

![Multi DC avoid conflict](./images/multiDC-resolveConflict.jpg)

##### How to avoid conflict

![Multi DC resolve conflict](./images/multiDC-avoidConflict.jpg)

### Global zone service

![Global zone service](./images/multiDC-GZSArchitecture.jpg)

### Global eZone 

![Global zone service](./images/multiDC-globalEZone.jpg)

## Multi DC in same city 

### Architecture
![Multi DC same city](./images/multiDC-sameCity.jpg)


## Multi DC in different city
### Three DC in two cities

#### Initial design

![Initial design](./images/multiDC-threeDcTwoCities.png)

#### Improved design

![Initial design](./images/multiDC-threeDcTwoCitiesImproved.png)

![Final design](./images/multiDC-threeDcTwoCitiesImprovedFinal.png)

### Five DC in three cities

![Final design](./images/multiDC-fiveDCThreeCities.png)

![Zhi fu bao](./images/multiDC-zhifubao-fiveDcThreeCities.png)

## Typical architecture

![elemo architecture](./images/multiDC-multiDC-elemo.jpg)

### Read intensive

![read intensive ](./images/multiDC-multiDC-readintensive.png)

### Read/write balanced

![read intensive ](./images/ultiDC-multiDC-writeintensive.png)



### CRG Units


* References:
	- 饿了吗：https://zhuanlan.zhihu.com/p/32009822
	- 异地多活架构： https://www.infoq.cn/video/PSpYkO6ygNb4tdmFGs0G
	- 微博异地多活：https://mp.weixin.qq.com/s?__biz=MzAwMDU1MTE1OQ==&mid=402920548&idx=1&sn=45cd62b84705fdd853bdd108b9301a17&3rd=MzA3MDU4NTYzMw==&scene=6#rd
	- Overview: https://www.modb.pro/db/12798
	- golden ant: 
		* https://www.infoq.cn/article/xYEWLWBSc1L9H4XvzGl0
		* https://static001.geekbang.org/con/33/pdf/1703863438/file/%E7%BB%88%E7%A8%BF-%E6%97%B6%E6%99%96-%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB%E5%8D%95%E5%85%83%E5%8C%96%E6%9E%B6%E6%9E%84%E4%B8%8B%E7%9A%84%E5%BE%AE%E6%9C%8D%E5%8A%A1%E4%BD%93%E7%B3%BB.pdf
	- 甜橙： https://mp.weixin.qq.com/s?__biz=MzIzNjUxMzk2NQ==&mid=2247489336&idx=1&sn=0a078591dbacda3e892d21ac0525de67&chksm=e8d7e8fadfa061eca5ff5b0c8f0035f7eec9abc6a6e8336a07cc2ea95ed0e9de1a8e3f19e508&scene=27#wechat_redirect
	- More: https://www.infoq.cn/article/kihSqp_twV16tiiPa1LO
	- https://s.geekbang.org/search/c=0/k=%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB/t=
	- 魅族：http://www.ttlsa.com/linux/meizu-mutil-loaction-soul/
	- 迁移角度：https://melonshell.github.io/2020/01/24/tech3_multi_room_living/
	- 李运华：https://time.geekbang.org/column/article/12408
	- 唐杨：https://time.geekbang.org/column/article/171115
	- 微服务多机房：https://time.geekbang.org/column/article/64301
	- 缓存多机房：https://time.geekbang.org/course/detail/100051101-253459
	- Google Ads 异地多活的高可用架构：https://zhuanlan.zhihu.com/p/103391944
	- TiDB: https://docs.pingcap.com/zh/tidb/dev/multi-data-centers-in-one-city-deployment
	- 支付宝架构：https://www.hi-linux.com/posts/39305.html#1-%E8%83%8C%E6%99%AF
	- 三地五中心：https://www.jianshu.com/p/aff048130bed