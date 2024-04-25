- [SETNX command](#setnx-command)
  - [NX](#nx)
  - [randomValue](#randomvalue)
  - [PX](#px)
    - [Motivation](#motivation)
- [Use cases - AP model](#use-cases---ap-model)
- [Flowchart](#flowchart)
  - [Retry case](#retry-case)
  - [Resume lock](#resume-lock)
- [Options to avoid distributed lock](#options-to-avoid-distributed-lock)
  - [Optimistic lock](#optimistic-lock)
  - [Consistent hashing](#consistent-hashing)
- [Redlock](#redlock)
  - [Motivation](#motivation-1)
  - [Limitations](#limitations)
  - [References](#references)
- [References](#references-1)
  - [Redisson](#redisson)

# SETNX command
* Example: SET resourceName randomValue NX PX 30000

## NX
* Succeed only if key does not exist; Otherwise fail the operation. Use the atomic property of NX to guarantee that only one client could configure it successfully.

## randomValue
* Definition: Used for validation when releasing lock among different threads. Only release lock if the randomValue is the same. Typically set as UUID. 

![Use case for randomValue](.gitbook/assets/distributedlock_randomValue_purpose.png)

## PX
* Automatic expiration time in case there are some exceptions happening (Thread crash)

![Use case for auto expiration](.gitbook/assets/distributedlock_px.png)

### Motivation

# Use cases - AP model
* Is actually an AP model. Applicable for scenarios that prioritize efficiency over correctness.

# Flowchart
## Retry case
* If failed to acquire the lock, 
  * Retry every certain period (typically set to 99 percentile of lock usage duration)
  * Monitor deletes events of the key

## Resume lock
* The lock is about to expire but business logic needs more time, resume lock to rescue. 
* If failed to acquire lock, one option is to use interrupt 

```c
// Interrupt sample flow in loop case (while/for)

for condition {
  // Interrupt signal
  if interrupted {
    break;
  }
  // Business logic
  DoSomething()
}
```

```c
// Interrupt sample flow for no-loop case

step1()
if interrupted {
  return
}
step2()
if interrupted {
  return
}
```

# Options to avoid distributed lock
## Optimistic lock
* For scenarios below, it could be improved with optimistic lock.

```
addDistributedLock()

compute()

updateDatabase()
```

## Consistent hashing
* Use consistent hashing to guarantee the same key is always routed to the same node. There will be no need for distributed lock. 

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

