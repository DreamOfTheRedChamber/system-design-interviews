- [Single machine rate limit](#single-machine-rate-limit)
  - [Leaky bucket](#leaky-bucket)
  - [Token bucket](#token-bucket)
    - [Semaphore based impl](#semaphore-based-impl)
    - [Guava ratelimiter based on token bucket](#guava-ratelimiter-based-on-token-bucket)
    - [Leaky vs token bucket](#leaky-vs-token-bucket)
  - [Fixed window](#fixed-window)
  - [Sliding log](#sliding-log)
  - [Sliding window](#sliding-window)

## Single machine rate limit

### Leaky bucket

* The leaky bucket limits the constant outflow rate, which is set to a fixed value. Imagine a bucket partially filled with water and which has some fixed capacity (τ). The bucket has a leak so that some amount of water is escaping at a constant rate (T)
* Steps
  1. Initialize the counter to N at every tick of the clock
     1. If N is greater than the size of the packet in front of the queue send the packet to network and decrement the counter by the size of the packet.
     2. Reset the counter and go to Step - 1. 
* Pros:
  * The leaky bucket produces a very smooth rate limiting effect. A user can still exhaust their entire quota by filling their entire bucket nearly instantaneously, but after realizing the error, they should still have access to more quota quickly as the leak starts to drain the bucket. 
* Cons:
  * When compared with token bucket, packet will be discarded instead of token.
  * The leaky bucket is normally implemented using a background process that simulates a leak. It looks for any active buckets that need to be drained, and drains each one in turn. The naive leaky bucket’s greatest weakness is its “drip” process. If it goes offline or gets to a capacity limit where it can’t drip all the buckets that need to be dripped, then new incoming requests might be limited incorrectly. There are a number of strategies to help avoid this danger, but if we could build an algorithm without a drip, it would be fundamentally more stable.

![](../.gitbook/assets/ratelimiter_leakyBucket.jpeg)

### Token bucket

* The token bucket limits the average inflow rate and allows sudden increase in traffic. 
  * Steps
    1. A token is added every t time.
    2. The bucket can hold at most b tokens. If a token arrive when bucket is full the token will be discarded.
    3. When a packet of m bytes arrived m tokens are removed from the bucket and the packet is sent to the network.
    4. If less than n tokens are available no tokens will be removed from the bucket and the packet is considered to be non-comformant.
  * Pros
    * Smooth out the requests and process them at an approximately average rate. 
  * Cons
    * A burst of request could fill up the queue with old requests and starve the more recent requests from being processed. Does not guarantee that requests get processed within a fixed amount of time. Consider an antisocial script that can make enough concurrent requests that it can exhaust its rate limit in short order and which is regularly overlimit. Once an hour as the limit resets, the script bombards the server with a new series of requests until its rate is exhausted once again. In this scenario the server always needs enough extra capacity to handle these short intense bursts and which will likely go to waste during the rest of the hour. 

![](../.gitbook/assets/ratelimiter_tokenBucket.jpeg)

#### Semaphore based impl

* It's based on the simple idea that we can have one java.util.concurrent.Semaphore to store current permissions and all user threads will call semaphore.tryAcquire method, while we will have an additional internal thread and it will call semaphore.release when new limitRefreshPeriod starts.
* Reference: [https://dzone.com/articles/rate-limiter-internals-in-resilience4j](https://dzone.com/articles/rate-limiter-internals-in-resilience4j)

![](../.gitbook/assets/ratelimiter_semaphore.png)

#### Guava ratelimiter based on token bucket

* Implemented on top of token bucket. It has two implementations:
* SmoothBursty / SmoothWarmup (The RateLimiterSmoothWarmingUp method has a warm-up period after teh startup. It gradually increases the distribution rate to the configured value. This feature is suitable for scenarios where the system needs some time to warm up after startup.)

**Warm up - Smooth ratelimiter**

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

**Token bucket impl1: Producer consumer pattern**

* Idea: Use a producer thread to add token to the queue according to a timer, and consumer thread pull token from the queue before consuming. 
* Cons: 
  * Rate limiting are usually used under high server loads. During such peak traffic time the server timer might not be that accurate and reliable. Furthermore, the timer will require to create a dedicated thread. 

**Token bucket impl2: Record the next time a token is available**

* Each time a token is expected, first take from the storedPermits; If not enough, then compare against nextFreeTicketMicros (update simultaneously using resync function) to see whether freshly generated tokens could satisfy the requirement. If not, sleep until nextFreeTicketMicros to acquire the next available fresh token. 
* [Link to the subpage](code/RateLimiter_TokenBucket.md)

#### Leaky vs token bucket

* Use case: 
  * The token bucket allows for sudden increase in traffic to some extent, while the leaky bucket is mainly used to ensure the smooth outflow rate.
  * The token bucket adds tokens to the bucket at a fixed rate. Whether the request is processed depends on whether the token in the bucket is sufficient or not. When the number of tokens decreases to zero, the new request is rejected. The leaky bucket outflows the request at a constant fixed rate at any rate of incoming request. When the number of incoming requests accumulates to the capacity of the leaky bucket, the new incoming request is rejected.
  * The token bucket limits the average inflow rate, allowing burst requests to be processed as long as there are tokens, supporting three tokens and four tokens at a time; the leaky bucket limits the constant outflow rate, that is, the outflow rate is a fixed constant value, such as the rate of all 1, but not one at a time and two at a time, so as to smooth the burst inflow rate;

### Fixed window

* Steps
  1. A window of size N is used to track the requests. 
  2. Each request increments the counter for the window.
  3. If the counter exceeds a threshold, the request is discarded. 
* Pros
  * It ensures recent requests get processed without being starved by old requests.
* Cons
  * Stamping elephant problem: A single burst of traffic that occurs near the boundary of a window can result in twice the rate of requests being processed, because it will allow requests for both the current and next windows within a short time. 
  * If many consumers wait for a reset window, for example at the top of the hour, then they may stampede your API at the same time.

### Sliding log

* Steps
  1. Tracking a time stamped log for each consumer’s request. 
  2. These logs are usually stored in a hash set or table that is sorted by time. Logs with timestamps beyond a threshold are discarded. 
  3. When a new request comes in, we calculate the sum of logs to determine the request rate. If the request would exceed the threshold rate, then it is held.
* Pros
  * It does not suffer from the boundary conditions of fixed windows. The rate limit will be enforced precisely. - Since the sliding log is tracked for each consumer, you don’t have the stampede effect that challenges fixed windows
* Cons
  * It can be very expensive to store an unlimited number of logs for every request. It’s also expensive to compute because each request requires calculating a summation over the consumer’s prior requests, potentially across a cluster of servers.

### Sliding window

* Steps
  1. Like the fixed window algorithm, we track a counter for each fixed window. 
  2. Next, we account for a weighted value of the previous window’s request rate based on the current timestamp to smooth out bursts of traffic.
* Pros
  * It avoids the starvation problem of leaky bucket.
  * It also avoids the bursting problems of fixed window implementations.
* Please see the section on [https://hechao.li/2018/06/25/Rate-Limiter-Part1/](https://hechao.li/2018/06/25/Rate-Limiter-Part1/) for detailed rate limiter implementations.
