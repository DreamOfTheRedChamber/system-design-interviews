
- [Sliding log implementation using ZSet](#sliding-log-implementation-using-zset)
- [Real world](#real-world)
  - [TokenBucket](#tokenbucket)
    - [Semaphore based impl](#semaphore-based-impl)
    - [Guava ratelimiter based on token bucket](#guava-ratelimiter-based-on-token-bucket)
      - [Warm up - Smooth ratelimiter](#warm-up---smooth-ratelimiter)
      - [Token bucket impl1: Producer consumer pattern](#token-bucket-impl1-producer-consumer-pattern)
      - [Token bucket impl2: Record the next time a token is available](#token-bucket-impl2-record-the-next-time-a-token-is-available)
      - [Token bucket impl3:](#token-bucket-impl3)
  - [Sliding window](#sliding-window)
- [Nginx based rate limiting](#nginx-based-rate-limiting)
  - [Challenges](#challenges)
    - [Synchronization issues](#synchronization-issues)

# Sliding log implementation using ZSet

* See [Dojo engineering blog for details](https://engineering.classdojo.com/blog/2015/02/06/rolling-rate-limiter/)
  1. Each identifier/user corresponds to a sorted set data structure. The keys and values are both equal to the (microsecond) times at which actions were attempted, allowing easy manipulation of this list.
  2. When a new action comes in for a user, all elements in the set that occurred earlier than (current time - interval) are dropped from the set.
  3. If the number of elements in the set is still greater than the maximum, the current action is blocked.
  4. If a minimum difference has been set and the most recent previous element is too close to the current time, the current action is blocked.
  5. The current action is then added to the set.
  6. Note: if an action is blocked, it is still added to the set. This means that if a user is continually attempting actions more quickly than the allowed rate, all of their actions will be blocked until they pause or slow their requests.
  7. If the limiter uses a redis instance, the keys are prefixed with namespace, allowing a single redis instance to support separate rate limiters.
  8. All redis operations for a single rate-limit check/update are performed as an atomic transaction, allowing rate limiters running on separate processes or machines to share state safely.


# Real world 
## TokenBucket
### Semaphore based impl

* It's based on the simple idea that we can have one java.util.concurrent.Semaphore to store current permissions and all user threads will call semaphore.tryAcquire method, while we will have an additional internal thread and it will call semaphore.release when new limitRefreshPeriod starts.
* Reference: [https://dzone.com/articles/rate-limiter-internals-in-resilience4j](https://dzone.com/articles/rate-limiter-internals-in-resilience4j)

![](../.gitbook/assets/ratelimiter_semaphore.png)

### Guava ratelimiter based on token bucket

* Implemented on top of token bucket. It has two implementations:
* SmoothBursty / SmoothWarmup (The RateLimiterSmoothWarmingUp method has a warm-up period after teh startup. It gradually increases the distribution rate to the configured value. This feature is suitable for scenarios where the system needs some time to warm up after startup.)

#### Warm up - Smooth ratelimiter

* Motivation: How to gracefully deal past underutilization
  * Past underutilization could mean that excess resources are available. Then, the RateLimiter should speed up for a while, to take advantage of these resources. This is important when the rate is applied to networking (limiting bandwidth), where past underutilization typically translates to "almost empty buffers", which can be filled immediately.
  * Past underutilization could mean that "the server responsible for handling the request has become less ready for future requests", i.e. its caches become stale, and requests become more likely to trigger expensive operations (a more extreme case of this example is when a server has just booted, and it is mostly busy with getting itself up to speed).
* Implementation
  * When the RateLimiter is not used, this goes right (up to maxPermits)
  * When the RateLimiter is used, this goes left (down to zero), since if we have storedPermits, we serve from those first
  * When _unused_, we go right at a constant rate! The rate at which we move to the right is chosen as maxPermits / warmupPeriod. This ensures that the time it takes to go from 0 to maxPermits is equal to warmupPeriod.
  * When _used_, the time it takes, as explained in the introductory class note, is equal to the integral of our function, between X permits and X-K permits, assuming we want to spend K saved permits.

```
             ^ throttling
             |
       cold  +                  /
    interval |                 /.
             |                / .
             |               /  .   ← "warmup period" is the area of the trapezoid between
             |              /   .     thresholdPermits and maxPermits
             |             /    .
             |            /     .
             |           /      .
      stable +----------/  WARM .
    interval |          .   UP  .
             |          . PERIOD.
             |          .       .
           0 +----------+-------+--------------→ storedPermits
             0 thresholdPermits maxPermits
```

* References
  1. [https://segmentfault.com/a/1190000012875897?spm=a2c65.11461447.0.0.74817a50Dt3FUO](https://segmentfault.com/a/1190000012875897?spm=a2c65.11461447.0.0.74817a50Dt3FUO)
  2. [https://www.alibabacloud.com/blog/detailed-explanation-of-guava-ratelimiters-throttling-mechanism\_594820](https://www.alibabacloud.com/blog/detailed-explanation-of-guava-ratelimiters-throttling-mechanism\_594820)

#### Token bucket impl1: Producer consumer pattern

* Idea: Use a producer thread to add token to the queue according to a timer, and consumer thread pull token from the queue before consuming. 
* Cons: 
  * Rate limiting are usually used under high server loads. During such peak traffic time the server timer might not be that accurate and reliable. Furthermore, the timer will require to create a dedicated thread. 

#### Token bucket impl2: Record the next time a token is available

* Each time a token is expected, first take from the storedPermits; If not enough, then compare against nextFreeTicketMicros (update simultaneously using resync function) to see whether freshly generated tokens could satisfy the requirement. If not, sleep until nextFreeTicketMicros to acquire the next available fresh token. 
* [Link to the subpage](code/RateLimiter_TokenBucket.md)

#### Token bucket impl3: 
* [https://github.com/vladimir-bukhtoyarov/bucket4j](https://github.com/vladimir-bukhtoyarov/bucket4j)

## Sliding window
* Please see the section on [https://hechao.li/2018/06/25/Rate-Limiter-Part1/](https://hechao.li/2018/06/25/Rate-Limiter-Part1/) for detailed rate limiter implementations.


# Nginx based rate limiting
## Challenges
### Synchronization issues

* Problem: When one server is not able to serve all requests, multiple rate limiter servers will need to be introduced. Then it comes the problem for synchronization between different rate limiter servers. 
* Solution: 
  * Sticky sessions: The simplest way to enforce the limit is to set up sticky sessions in your load balancer so that each consumer gets sent to exactly one node. The disadvantages include a lack of fault tolerance and scaling problems when nodes get overloaded.
  * Redis: See below for more information. 
