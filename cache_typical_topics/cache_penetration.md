- [Cache penetration](#cache-penetration)
  - [Cache empty/default values](#cache-emptydefault-values)
  - [Bloomberg filter](#bloomberg-filter)
    - [Read](#read)
    - [Write](#write)
- [Cache avalanch](#cache-avalanch)


# Cache penetration

* Note: All 1-4 bullet points could be used separately.
* Cache key validation (step1)
* Cache empty values (step2)
* Bloom filter (step3)
* Cache entire dataset in cache (step4)

```
┌───────┐   ┌──────────┐   ┌─────────────┐  ┌────────┐   ┌────────┐  ┌────────┐
│       │   │  step1:  │   │step2: cache │  │ step3: │   │ Step4. │  │        │
│Client │──▶│ Request  │──▶│empty values │─▶│ bloom  │──▶│ Cache  │─▶│ Cache  │
│       │   │validation│   │             │  │ filter │   │everythi│  │        │
└───────┘   └──────────┘   └─────────────┘  └──────*─┘   └────────┘  └────────┘
```

## Cache empty/default values

* Cons: Might need large space for empty values. As a result, cache entries for non-empty entries might be purged out. 

```
Object nullValue = new Object();
try 
{
  Object valueFromDB = getFromDB(uid); 
  if (valueFromDB == null) 
  {
    cache.set(uid, nullValue, 10);   
  } 
  else 
  {
    cache.set(uid, valueFromDB, 1000);
  }
} 
catch(Exception e) 
{
  cache.set(uid, nullValue, 10);
}
```

## Bloomberg filter

* Use case
  * Time complexity: O(1) read/write
  * Space complexity: Within 1 billion records (roughly 1.2GB memory)
* Potential issues
  * False positives
    * Solution: Use multiple hash algorithm to calculate multiple hash values
  * No support for delete
    * Solution: Store a counter for each entry 

### Read

```
┌───────────┐         ┌───────────┐         ┌───────────┐         ┌───────────┐
│ Request A │         │ Request B │         │   Cache   │         │ Database  │
└───────────┘         └───────────┘         └───────────┘         └───────────┘

      │     lookup bloom    │                     │                     │      
      │        filter       │                     │                     │      
      │─────────────────────▶                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├────────────────lookup cache──────────────▶│                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼──lookup database────┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├──────────────write to cache───────────────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │
```

### Write

```
┌───────────┐       ┌───────────────┐       ┌───────────┐         ┌───────────┐
│  Client   │       │ Bloom Filter  │       │   Cache   │         │ Database  │
└───────────┘       └───────────────┘       └───────────┘         └───────────┘

      │                     │                     │                     │      
      │ write bloom filter  │                     │                     │      
      │─────────────────────▶                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼───write database────┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
                                                                        │
```


# Cache avalanch
1. Jitter to expiration time
2. Rate limiting / Circuit breaker to DB
3. Open distributed cache persistence option for fast recovery
4. Background refresh
   * The first client to request data past the stale date is asked to refresh the data, while subsequent requests are given the stale but not-yet-expired data as if it were fresh, with the understanding that it will get refreshed in a 'reasonable' amount of time by that initial request.

```
                      ┌─────────────────────────────────────────────────────────────────────────────────┐
                      │                                Distributed cache                                │
                      │                                                                         Step4.  │
                      │                                                                   ┌──────back ─┐│
    .─────────.       │                                                                   │     ground ││
 ,─'           '─.    │                                                                   ▼            ││
;   step 1. add   :   │  ┌──────────────────────┐  ┌──────────────────────┐   ┌──────────────────────┐ ││
:    jitter to    ;   │  │Entry A               │  │Entry ..              │   │Entry N               │ ││
 ╲expiration time╱    │  │Expiration with jitter│  │Expiration with jitter│   │Expiration with jitter│ ││
  '─.         ,─'     │  └──────────────────────┘  └──────────────────────┘   └──────────────────────┘ ││
     `───────'        │               │                                                   │            ││
                      │               │                                                   │            ││
                      │               ▼                                                   └────────────┘│
    .─────────.       │   ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─   │
 ,─'           '─.    │                            Step 3. Persistent to disk                        │  │
; step 2. circuit :   │   │                                                                             │
: breaker / rate  ;   │    ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
 ╲   limiting    ╱    │                                                                                 │
  '─.         ,─'     └─────────────────────────────────────────────────────────────────────────────────┘
     `───────'                                                                                           
                        ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─      
                                               circuit breaker / rate limiter                      │     
                        │                                                                                
                         ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘     

                      ┌────────────────────────────────────────────────────────────────────────────────┐ 
                      │                                                                                │ 
                      │                                    Database                                    │ 
                      │                                                                                │ 
                      └────────────────────────────────────────────────────────────────────────────────┘
```

