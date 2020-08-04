
<!-- MarkdownTOC -->

- [Large number names](#large-number-names)
	- [2 base](#2-base)
	- [10 base](#10-base)
	- [Translate](#translate)
- [Latency numbers](#latency-numbers)
	- [CPU](#cpu)
	- [Cache](#cache)
	- [Disk](#disk)
	- [Network](#network)
- [C10K](#c10k)
	- [Definition](#definition)
	- [Initial proposal](#initial-proposal)
	- [Next stage - C10M](#next-stage---c10m)
- [Server loads](#server-loads)
	- [I/O bound](#io-bound)
	- [CPU bound](#cpu-bound)
	- [Typical load](#typical-load)
	- [Estimate RPS](#estimate-rps)
- [Monthly active user](#monthly-active-user)
	- [Unsuitable cases](#unsuitable-cases)
	- [Changes in MAU](#changes-in-mau)
		- [Increase in MAU](#increase-in-mau)
		- [Decrease in MAU](#decrease-in-mau)
- [Statistics](#statistics)
	- [Facebook \(Dec 2019\)](#facebook-dec-2019)
	- [Slack](#slack)
	- [Example: Design load balancing mechanism for an application with 10M DAU](#example-design-load-balancing-mechanism-for-an-application-with-10m-dau)
	- [Single machine capability](#single-machine-capability)

<!-- /MarkdownTOC -->

# Large number names
## 2 base

```
1PB = 10^15 bytes
1TB = 10^12 bytes
1GB = 10^9 bytes
1MB = 10^6 bytes
1KB = 10^3 bytes
```

## 10 base

```
1 trillion = 10^12
1 billion = 10^9
1 million = 10^6
1 thousand = 10^3
```

## Translate

```
2^30 ~ 1GB
2^32 = 4GB
2^64 = 
```

# Latency numbers
## CPU
* Mutex lock/unlock 25 ns
* Compress 1K bytes with Zippy 3,000 ns

## Cache
* L1 cache reference 0.5 ns
* L2 cache reference 7 ns
* Main memory reference 100 ns
* Read from distributed cache 
* Read 1 MB sequentially from memory 250,000 ns

## Disk
* Disk seek 10,000,000 ns
* Read 1 MB sequentially from disk 20,000,000 ns

## Network
* Send 2K bytes over 1 Gbps network 20,000 ns
* Round trip within same datacenter 500,000 ns
* Send package CA->Netherlands->CA: 100ms
	* Using New York as an example: https://wondernetwork.com/pings/
		- New York to Washington: 10ms
		- New York to Paris: 75ms
		- New York to Tokyo: 210ms
		- New York to Barcelona: 103ms

# C10K
## Definition
* Handle 10,000 concurrent connections
	- vs RPS: 
		+ RPS requires high throughput (Process them quickly). 
		+ A system which could handle high number of connections is not necessarily a high throughput system.
* This became known as the C10K problem. Engineers solved the C10K scalability problems by fixing OS kernels and moving away from threaded servers like Apache to event-driven servers like Nginx and Node.

## Initial proposal
* http://www.kegel.com/c10k.html

## Next stage - C10M
* http://highscalability.com/blog/2013/5/13/the-secret-to-10-million-concurrent-connections-the-kernel-i.html

# Server loads
## I/O bound
* RPS = (memory / worker memory)  * (1 / Task time)

![I/O bound](./images/scaleNumbers_IOBoundRPS.png)


## CPU bound
* RPS = Num. cores * (1 /Task time)

![CPU bound](./images/scaleNumbers_CPUBoundRPS.png)

## Typical load
* 1,000 RPS is not difficult to achieve on a normal server for a regular service.
* 2,000 RPS is a decent amount of load for a normal server for a regular service.
* More than 2K either need big servers, lightweight services, not-obvious optimisations, etc (or it means you’re awesome!). Less than 1K seems low for a server doing typical work (this means a request that is simple and not doing a lot of work) these days.

## Estimate RPS
* Assume that all the expected requests in a day are going to be done in 4 hours.
	- DAU => Number of requests

# Monthly active user
## Unsuitable cases
* It is an unreliable metric for just-launched start-ups
	- Putting stock in MAU early in a start-up’s life is a mistake. Given the definition of MAU, all of the promotional activities that are associated with a launch such as PR, being featured in app stores and publications, word of mouth, advertising, etc., can highly inflate MAU figures. It would instead be better to assess MAU once traffic has normalized over a few months.
* Depth of usage isn’t accounted for 
	- To qualify for MAU, according to some definitions, a user just has to log in and doesn’t need to engage with the product beyond that. So having a high MAU doesn’t necessarily mean that all those users are engaging with your product. From a monetization point of view, you can only monetize users who engage with your app. So it’s good practice to measure unique users who interact with a core feature of your product.
* Quality of users isn’t accounted for
	- Not all users are the same. Users obtained from different sources tend to exhibit different engagement behaviors. Some sources, for example, may allow for installs quickly or cheaply, but if those users don’t engage with key features of the product then the source isn’t very useful. In fact, obtaining a large number of users from such sources only serves to inflate MAU numbers but does not provide much else in value.

## Changes in MAU
### Increase in MAU
* This tends to happen when the number of new users and reactivations is greater than the number of existing users that have churned.
* (New users + Reactivations of lapsed users) > Churn of existing users
	- New users – A new advertising campaign, positive press, or the app being featured in the app store can drive an increase in downloads and new users; in turn, they drive an increase in monthly active users.
	- Reactivations – Start-ups that have a large base of users that are no longer active can reactivate them via email campaigns or push notifications.
	- Churn – Addressing issues which have put off users via a new release or feature can reduce churn rates among existing users. This, in turn, would help increase MAU.

### Decrease in MAU
* A decrease in MAU occurs then the number of new users and reactivations of existing users is less than the number of existing users that have churned.
* (New users + Reactivations of lapsed users) < Churn of existing users
	- New users – The number of new users may fall due to expiring subscriptions, reductions in advertising or promotions, or the app no longer being featured on publications or in app stores.
	- Reactivations – A decrease in reactivation or engagement campaigns can also result in a decrease in the number of reactivations
	- Churn – An increase in churn rates due to technical problems, features that put users off, or other issues can cause an increase in churn rates, which in turn lead to lower MAU.

# Statistics
## Facebook (Dec 2019)
* Facebook core apps: Facebook, WhatsApp, Instagram, Messenger 
	- MAU 
		+ 2.5 billion MAUs
		+ 1.74 billion mobile MAUs
	- DAU
		+ 1.66 billion DAUs
* Data
	- Likes: 
		+ In total 1.13 trillion like since 2004. 
		+ 4.5 billion Facebook like every day. 
		+ Each minute 3,135,000 new likes.  
	- Photos:
		+ In total 250 billion photo since 2004. 
		+ Photo uploads total 300 million per day
		+ 243,055 new photos uploaded per minute
		+ 127 photos uploaded on average per Facebook user
	- Posts:
		+ 10 billion Facebook messages per day. 
		+ Every minute 510,000 comments, 293,000 statues are updated. 
* Estimated RPS.
	- Each session like 10 times, send 50 msgs, upload 10 times, comments 30 times. Each session 100 operations RPS: 
	- 1.66 billion = 1.66 * 1000 M * 100 / (4 * 3600) ~ 12 million RPS

* Reference
	- https://blog.wishpond.com/post/115675435109/40-up-to-date-facebook-facts-and-stats

## Slack 
* To be finished ??? 
* 12 million DAU
* Number of messages sent weekly on Slack: 1 billion
* Number of paid Slack accounts: 3 million
* Number of organizations that use Slack: 600,000
* Number of teams that use the free version of Slack: 550,000
* Number of paid Teams on slack 88,000



* Facebook: 2.5 Billion

## Example: Design load balancing mechanism for an application with 10M DAU
* 10M DAU will be normal for applications such as Github. 

* Traffic voluem estimation
1. 10M DAU. Suppose each user operate 10 times a day. Then the QPS will be roughly ~ 1160 QPS
2. Peak value 10 times average traffic ~ 11600 QPS
3. Suppose volume need to increase due to static resource, microservices. Suppose 10. QPS ~ 116000 QPS. 

* Capacity planning
1. Multiple DC: QPS * 2 = 232000
2. Half-year volume increase: QPS * 1.5 = 348000

* Mechanism
1. No DNS layer 
2. LVS

## Single machine capability
* For a 32 core 64GB machine, it could at mmost process 20K "hello world" per second. 
* For the actual business logic, the RPS will be much lower, several hundreds per second. 