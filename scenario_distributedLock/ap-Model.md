- [Redis based distributed lock](#redis-based-distributed-lock)
  - [Flowchart](#flowchart)
  - [Use cases - AP model](#use-cases---ap-model)
- [Options to avoid distributed lock](#options-to-avoid-distributed-lock)
  - [Optimistic lock](#optimistic-lock)
- [Redlock](#redlock)
  - [Motivation](#motivation)
  - [Limitations](#limitations)
  - [References](#references)
- [References](#references-1)
  - [Redisson](#redisson)

# Redis based distributed lock
## Flowchart
* SET resourceName randomValue NX PX 30000
  * resourceName: key
  * randomValue: UUID, used for validation when releasing lock among different threads. Only release lock if the randomValue is the same.
  * NX: succeed only if key does not exist; Otherwise fail the operation. Use the atomic property of NX to guarantee that only one client could configure it successfully.
  * PX: automatic expiration time in case there are some exceptions happening

## Use cases - AP model
* Is actually an AP model. Only applicable for efficiency use cases, not for correctness use cases.
* You could use a single Redis instance, of course you will drop some locks if the power suddenly goes out on your Redis node, or something else goes wrong. But if you’re only using the locks as an efficiency optimization, and the crashes don’t happen too often, that’s no big deal. This “no big deal” scenario is where Redis shines. At least if you’re relying on a single Redis instance, it is clear to everyone who looks at the system that the locks are approximate, and only to be used for non-critical purposes.
* Add on top of the single application case, you could use master-slave setup for high availability. 

# Options to avoid distributed lock
## Optimistic lock


# Redlock
## Motivation
* There is an obvious race condition with this model:
  * Client A acquires the lock in the master.
  * The master crashes before the write to the key is transmitted to the slave.
  * The slave gets promoted to master.
  * Client B acquires the lock to the same resource A already holds a lock for. SAFETY VIOLATION!

## Limitations
* Redlock does not have any facility to generate fencing tokens. And it is not straightforward to repurpose Redlock for generating fencing tokens. 
* Relying on expiration time to avoid deadlock is not reliable. 
  * What if the lock owner dies? The lock will be held forever and we could be in a deadlock. To prevent this issue Redis will set an expiration time on the lock, so the lock will be auto-released. However, if the time expires before the task handled by the owner isn't yet finish, another microservice can acquire the lock, and both lock holders can now release the lock causing inconsistency. 
  * A fencing token needed to be used to avoid race conditions. Please see [this post](https://medium.com/@davidecerbo/everything-i-know-about-distributed-locks-2bf54de2df71) for details. 
* Redlock depends on a lot of timing assumptions
  1. All Redis nodes hold keys for approximately the right length of time before expiring
  2. The network delay is small compared to the expiry duration
  3. Process pauses are much shorter than the expiry duration
* References: https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html

## References
* [Failures of distributed locks](https://redislabs.com/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-2-distributed-locking/6-2-2-simple-locks/)
* [A hot debate on the security perspective of RedLock algorithm](http://zhangtielei.com/posts/blog-redlock-reasoning.html).

# References
## Redisson

* Relationship with Redis could be thought as similar to Curator to Zookeeper

