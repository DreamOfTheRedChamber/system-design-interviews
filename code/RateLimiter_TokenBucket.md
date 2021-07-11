
- [Implement token bucket algorithm](#implement-token-bucket-algorithm)
  - [Assume the token bucket size is 1](#assume-the-token-bucket-size-is-1)
  - [Assume the token bucket size is bigger than 1](#assume-the-token-bucket-size-is-bigger-than-1)

## Implement token bucket algorithm
* Reference: [Chinese article](https://time.geekbang.org/column/article/97231)

### Assume the token bucket size is 1

```
class SimpleLimiter 
{
  // The moment to generate the next available token
  long next = System.nanoTime();
  // Duration for issuing token in ms
  long interval = 1000_000_000;

  // Def: Reserve the token
  // Return: The next moment when the token could be obtained
  synchronized long reserve(long now)
  {
    if (now > next)
    {
      // If current time is already older than the previous next available token time
      // Assign it to now
      next = now;
    }

    // Calculate the next moment that a token could be generated generate token
    long at=next;
    next += interval;

    // Return the duration that thread will need to wait
    return Math.max(at, 0L);
  }

  // Def: Acquire token
  void acquire() 
  {
    long now = System.nanoTime();

    // Reserve a token
    long at=reserve(now);
    long waitTime=max(at-now, 0);

    // If need to wait
    if(waitTime > 0) 
    {
      try 
      {
        TimeUnit.NANOSECONDS
          .sleep(waitTime);
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
```

### Assume the token bucket size is bigger than 1

```
class SimpleLimiter 
{
  // Current token number in the bucket
  long storedPermits = 0;
  // Capacity of token bucket
  long maxPermits = 3;
  // The next moment a token will be generated and available for reserve
  long next = System.nanoTime();
  // The duration to issue token
  long interval = 1000_000_000;
  
  // If current time is already older than the previous next available token time, assign it to now and update the number of tokens
  void resync(long now) 
  {
    if (now > next) 
    {
      // Newly generated token
      long newPermits=(now-next)/interval;
      // Add new tokens to the existing token number
      storedPermits=min(maxPermits, 
        storedPermits + newPermits);
      // Set the next moment a token will be generated and available for reserve as now
      next = now;
    }
  }

  // Def: Reserve the token
  // Return: The next moment when the token could be obtained
  synchronized long reserve(long now)
  {
    resync(now);
    long at = next;
    // The number of tokens in the token bucket
    long fb=min(1, storedPermits);
    // Calculate the net token requirement
    long nr = 1 - fb;
    // Calculate the next moment a token will be generated
    next = next + nr*interval;
    // Recalculate the number of tokens
    this.storedPermits -= fb;
    return at;
  }
  
  // Def: Acquire token
  void acquire() 
  {
    long now = System.nanoTime();

    // Reserve a token
    long at=reserve(now);
    long waitTime=max(at-now, 0);

    // If need to wait
    if(waitTime > 0) 
    {
      try 
      {
        TimeUnit.NANOSECONDS
          .sleep(waitTime);
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
```