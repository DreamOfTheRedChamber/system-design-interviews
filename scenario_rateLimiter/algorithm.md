- [Real world](#real-world)
  - [TokenBucket](#tokenbucket)
    - [Semaphore based impl](#semaphore-based-impl)
    - [Guava ratelimiter based on token bucket](#guava-ratelimiter-based-on-token-bucket)
      - [Warm up - Smooth ratelimiter](#warm-up---smooth-ratelimiter)
      - [Token bucket impl1: Producer consumer pattern](#token-bucket-impl1-producer-consumer-pattern)
      - [Token bucket impl2: Record the next time a token is available](#token-bucket-impl2-record-the-next-time-a-token-is-available)
      - [Token bucket impl3:](#token-bucket-impl3)
  - [Sliding window](#sliding-window)

## Leaky vs token bucket

* Use case: 
  * The token bucket allows for sudden increase in traffic to some extent, while the leaky bucket is mainly used to ensure the smooth outflow rate.
 The leaky bucket outflows the request at a constant fixed rate at any rate of incoming request. When the number of incoming requests accumulates to the capacity of the leaky bucket, the new incoming request is rejected.
  * The token bucket limits the average inflow rate, allowing burst requests to be processed as long as there are tokens, supporting three tokens and four tokens at a time; the leaky bucket limits the constant outflow rate, that is, the outflow rate is a fixed constant value, such as the rate of all 1, but not one at a time and two at a time, so as to smooth the burst inflow rate;

