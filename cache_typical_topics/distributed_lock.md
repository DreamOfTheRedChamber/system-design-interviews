- [Race condition](#race-condition)
  - [Distributed lock](#distributed-lock)


# Race condition

* Many readers read an empty value from the cache and subseqeuntly try to load it from the database. The result is unnecessary database load as all readers simultaneously execute the same query against the database.
* Solutions:
  * Distributed lock

## Distributed lock

* Scenario: Multiple requests get value from cache and all have cache miss. Then they simultaneously fetch value from DB to update distributed cache. 
* To make sure there are no race conditions, the following two conditions need to be met to order the updates.
  1. Every instance need to get a distributed lock before updating value in cache.
  2. Each value also has a corresponding timestamp which is obtained from DB.

```
                      ┌─────────────────┐                       
                      │                 │                       
          ┌──────────▶│Distributed Cache│◀──────────┐           
          │           │                 │           │           
          │           └─────────────────┘           │           
          │                    ▲                    │           
        Value1,                │                ValueN,         
       timestamp1              │               timestampN       
          │                Value2,                  │           
          │               timeStamp2                │           
          │                    │                    │           
          │                    │                    │           
    ┌──────────┐         ┌──────────┐         ┌──────────┐      
    │          │         │          │         │          │      
    │ Client A │         │Client ...│         │ Client N │      
    │          │         │          │         │          │      
    └──────────┘         └──────────┘         └──────────┘      
          │                    │                    │           
          │                    │                    │           
          │                    │                    │           
          │          Get distributed lock:          │           
          │                     Failed              │           
          │                    │                    │           
Get distributed lock:          │          Get distributed lock: 
       Succeed                 ▼                 Succeed        
          │           ┌─────────────────┐           │           
          │           │                 │           │           
          └──────────▶│    Zookeeper    │◀──────────┘           
                      │                 │                       
                      └─────────────────┘
```
