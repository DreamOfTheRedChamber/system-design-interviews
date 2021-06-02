- [MicroSvcs monitoring](#microsvcs-monitoring)
	- [Overview](#overview)
	- [Rate limiter](#rate-limiter)
		- [Scenario](#scenario)
		- [Storage](#storage)
		- [Initial solution](#initial-solution)
		- [Final solution](#final-solution)
	- [Data dog](#data-dog)
		- [Scenario](#scenario-1)
		- [Storage](#storage-1)
	- [Health check](#health-check)
		- [Domain model](#domain-model)
		- [Architecture](#architecture)
		- [Delayed schedule queue](#delayed-schedule-queue)
			- [Check model](#check-model)
			- [Architecture](#architecture-1)
			- [Industrial implementationn](#industrial-implementationn)
		- [Distributed lock](#distributed-lock)
			- [Optimistic / Pessimistic lock](#optimistic--pessimistic-lock)
			- [Pessimistic lock - Fencing token](#pessimistic-lock---fencing-token)
			- [Industrial implementation](#industrial-implementation)
		- [Distributed rate limiting](#distributed-rate-limiting)
			- [Use case](#use-case)
			- [Deployment mode](#deployment-mode)
				- [Centralized](#centralized)
				- [Distributed](#distributed)
			- [Industrial implementation](#industrial-implementation-1)
		- [Top K system to prevent attack from crawler / DDos](#top-k-system-to-prevent-attack-from-crawler--ddos)
	- [Real world applications](#real-world-applications)
		- [Netflix](#netflix)
	- [References](#references)

<!-- /MarkdownTOC -->

# MicroSvcs monitoring
## Overview
* [Monitoring layer and overall architecture](https://time.geekbang.org/course/detail/100003901-2276)
* [Monitoring overall arch](https://time.geekbang.org/column/article/15109)
* [Popular solution comparison](https://time.geekbang.org/column/article/39907)

## Rate limiter
### Scenario
* Limit according to IP / UserId / Email
* If passing certain times / certain period, return 4XX error codes.
	- 2/s, 5/m, 10/h, 100d
	- Do not need too fine granularity

### Storage
* Need to log who did what at when
	- Event + feature + timestamp as memcached key
		+ event = url_shorten
		+ feature = 192.168.0.1
		+ timestamp
* Memcached
	- Fast
	- Do not need persistence

### Initial solution
* Algorithm
	- memcached.increment(key, ttl=60s)
	- Increment corresponding bucket, set invalid after 60s
* Check whether over the limits in the last minute

```
// Check the visiting sum in the last minute
for t in 0~59 do
	key = event + feature + (current_timestamp - t)
	sum += memcached.get(key, default=0)
```

### Final solution
* Multi-level bucket
	- Use 1 minute as a unit 
	- Use 1 day as a unit

## Data dog
### Scenario
* A user visiting a link is recorded as once visiting.
* Know the total number of visits.
* Can check the latest X day/month/years
* Typically a user always asks from a certain ts to current

### Storage
* NoSQL system
	- Need persistent storage
	- key is tinyUrl short_key, value is the visiting statistics
* Reduce 2K write QPS by aggregation: 
	- Need to aggregate the latest 15s visiting records together and then save in memory.
* Retention: Multi-level bucket
	- Use 1-minute as a unit
	- Use 5-minute as a unit
	- use 1-hour as a unit

## Health check
* Healthchecks.io
	- 120 paying customer
	- 1600 monthly recurring revenue
	- 10M pings per day
* Industrial implementation:
	- Sentry

### Domain model

![MySQL HA github](./images/monitorSystem_HealthCheck_domainModel.png)

### Architecture

![MySQL HA github](./images/monitorSystem_HealthCheck_domainModel.png)

### Delayed schedule queue
#### Check model
* Code
* Schedule
* Last_ping
* Alert_after
* Status

#### Architecture
![MySQL HA github](./images/monitorSystem_HealthCheck_delayedScheduleQueue.png)

#### Industrial implementationn
* db-scheduler / cron.io
* killbill notification queue
* Quartz (Java)
* Xxl-job (Java)
* Celery (Python)
* Hangfire (C#)

### Distributed lock
* Use case: Payment in SaaS platform

#### Optimistic / Pessimistic lock
#### Pessimistic lock - Fencing token
![MySQL HA github](./images/monitorSystem_HealthCheck_distributedlock_fencingToken.png)

#### Industrial implementation
* ShedLock

### Distributed rate limiting
#### Use case
* Github API rate limiting
* Bitly API rate limiting
* LinkedIn rate limiting

#### Deployment mode
##### Centralized

![MySQL HA github](./images/monitorSystem_HealthCheck_distributedratelimiting_centralized.png)

##### Distributed

![MySQL HA github](./images/monitorSystem_HealthCheck_distributedratelimiting_distributed.png)


#### Industrial implementation
* Hystrix

### Top K system to prevent attack from crawler / DDos
* Use case: prevent attack from crawlers

![MySQL HA github](./images/monitorSystem_HealthCheck_topk_crawler.png)

## Real world applications
### Netflix 
* Application monitoring: https://netflixtechblog.com/telltale-netflix-application-monitoring-simplified-5c08bfa780ba
* Distributed tracing: https://netflixtechblog.com/building-netflixs-distributed-tracing-infrastructure-bb856c319304
* Edgar solving mysterious: https://netflixtechblog.com/edgar-solving-mysteries-faster-with-observability-e1a76302c71f
* Self-serve dashboard: https://netflixtechblog.com/lumen-custom-self-service-dashboarding-for-netflix-8c56b541548c
* Build observability tools: https://netflixtechblog.com/lessons-from-building-observability-tools-at-netflix-7cfafed6ab17
* Netflix On instance trace: https://netflixtechblog.com/introducing-bolt-on-instance-diagnostic-and-remediation-platform-176651b55505
* Netflix system intuition: https://netflixtechblog.com/flux-a-new-approach-to-system-intuition-cf428b7316ec


## References
* Datadog and Opentracing: https://www.datadoghq.com/blog/opentracing-datadog-cncf/
* 美团技术博客字节码：https://tech.meituan.com/2019/09/05/java-bytecode-enhancement.html
* 美团技术深入分析开源框架CAT: https://tech.meituan.com/2018/11/01/cat-in-depth-java-application-monitoring.html
* Metrics, logging and tracing: https://peter.bourgon.org/blog/2017/02/21/metrics-tracing-and-logging.html
* Which trace to collect: 
  * https://news.ycombinator.com/item?id=15326272
  * Tail-based sampling: https://github.com/jaegertracing/jaeger/issues/425
* 阿里云分布式链路文档：https://help.aliyun.com/document_detail/133635.html
* 美团分布式追踪MTrace：https://zhuanlan.zhihu.com/p/23038157
