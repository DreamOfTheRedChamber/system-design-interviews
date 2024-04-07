- [Leaky vs token bucket](#leaky-vs-token-bucket)

# Leaky vs token bucket

* Use case: 
  * The token bucket allows for sudden increase in traffic to some extent, while the leaky bucket is mainly used to ensure the smooth outflow rate.
 The leaky bucket outflows the request at a constant fixed rate at any rate of incoming request. When the number of incoming requests accumulates to the capacity of the leaky bucket, the new incoming request is rejected.
  * The token bucket limits the average inflow rate, allowing burst requests to be processed as long as there are tokens, supporting three tokens and four tokens at a time; the leaky bucket limits the constant outflow rate, that is, the outflow rate is a fixed constant value, such as the rate of all 1, but not one at a time and two at a time, so as to smooth the burst inflow rate;

