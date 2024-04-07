- [Fixed window](#fixed-window)
- [Sliding window](#sliding-window)
- [Leaky bucket](#leaky-bucket)
  - [Queue-based asynchronous implementation](#queue-based-asynchronous-implementation)
  - [Synchronous implementation](#synchronous-implementation)
- [Token bucket](#token-bucket)

# Fixed window

![Fixed window](../.gitbook/assets/ratelimiter_fixedwindow.png)

# Sliding window

![Sliding window](../.gitbook/assets/ratelimiter_slidingwindow.png)

# Leaky bucket

## Queue-based asynchronous implementation
* This implementation is asynchronous, and it is not suitable for scenario such as gateway rate limiting.

![Sliding window](../.gitbook/assets/ratelimiter_leakybucket.png)

## Synchronous implementation

```c
// initialization:
time_interval = 100; // ms
number_of_blocking_threads = 0;
most_recent_request_timestamp = 0

while(true)
{
  sleep_time = sleep_time()
  if sleep_time == MAX_TIME
  {
    return 503;
  }
  elif sleep_time == 0
  {
    thread_do_its_job;
  }
  else
  {
    thread.sleep(sleep_time);
    most_recent_request_timestamp = now;
    number_of_blocking_threads -= 1
  }
}

// thread
long sleep_time()
{
  // Don't block if there is no recent request
  if( (now - most_recent_request_timestamp) >= time_interval 
      and number_of_blocking_threads <= 0）
  {
    most_recent_request_timestamp = now;
    return 0; // Don't block
  }

  // When there are too many threads blocked, the bucket will be leaky.
  if(number_of_blocking_threads > max_number_of_blocking) 
  {
    // max_number_of_blocking means that the blocking time will be too much
    // MAX_TIME means that the current request is rate limited
    return MAX_TIME;
  }

  // Wait in blocking mode
  number_of_blocking_threads += 1
  
  return time_interval * number_of_blocking_threads - (now - most_recent_request_timestamp);
}
```

# Token bucket
* Use case: 
  * The token bucket allows for sudden increase in traffic to some extent, while the leaky bucket is mainly used to ensure the smooth outflow rate.
 The leaky bucket outflows the request at a constant fixed rate at any rate of incoming request. When the number of incoming requests accumulates to the capacity of the leaky bucket, the new incoming request is rejected.
  * The token bucket limits the average inflow rate, allowing burst requests to be processed as long as there are tokens, supporting three tokens and four tokens at a time; the leaky bucket limits the constant outflow rate, that is, the outflow rate is a fixed constant value, such as the rate of all 1, but not one at a time and two at a time, so as to smooth the burst inflow rate;

```c
//initialization
most_recent_token_timestamp = 0
num_token = 0
interval_token_generation = 100 // ms

boolean acquireToken()
{
  // direct take token if there is token in token buckert
  if(num_token >= 1)
  {
    num_token -= 1；
    return true;
  }

  // when there is no token in bucket
  // recalculate the num of token in bucket
  num_token = min(MAX_TOKEN, num_token + (now - most_recent_token_timestamp) / interval_token_generation)
  if(num_token >= 1)
  {
    num_token -= 1；
    most_recent_token_timestamp = now;
    return true;
  }
  else
  {
    return false；
  }
}
```