- [Approach 1: Cache locations and search in memory](#approach-1-cache-locations-and-search-in-memory)
  - [Flowchart](#flowchart)
- [Approach 2: Create partitions in memory \& search partition wise](#approach-2-create-partitions-in-memory--search-partition-wise)
  - [Flowchart](#flowchart-1)
    - [Static partition managed by metadata registry](#static-partition-managed-by-metadata-registry)
    - [Get the city name](#get-the-city-name)
    - [Audit support](#audit-support)
  - [Data modeling](#data-modeling)
    - [City-based sharding](#city-based-sharding)
    - [Use logical shard to solve small / big city problems](#use-logical-shard-to-solve-small--big-city-problems)
    - [Schema example](#schema-example)
    - [Cons of city based partition](#cons-of-city-based-partition)

# Approach 1: Cache locations and search in memory
* Assumption: Suppose the total number of locations could suit inside a single machine. 

## Flowchart
![](../.gitbook/assets/uber_inmemory-cache.png.png)

* Write path
  1. Client_B will write to master database 
  2. Client_B will write to cache. This needs to happen after step1 because database is the source of truth.
  3. Every few minutes, cache loader will load entries after the last timestamp/id from the database follower. 
     * Cache loader reads the data from a “Follower machine” to avoid adding overhead to master database.  
  4. Cache loader will update the cache entries if needed. 
     * In case the data is already present there, depending on the timestamp of the in-memory object, the loader can decide whether to update the data or skip it — if the database has more updated data / higher timestamp, update the data in cache. 

# Approach 2: Create partitions in memory & search partition wise

## Flowchart

![](../.gitbook/assets/geosearch_partition_InMemory.png)

### Static partition managed by metadata registry
* Both the read & write request first talk to the metadata registry, using the IP address of the request’s origin, we determine in which city the delivery agent exactly is or from where the customer request came. Using the resolved city, shard id is identified, then using shard id, index server is identified. Finally the request is directed towards that particular index server. So we don’t need to query all the cache / index server any more. Note that, in this architecture, neither read nor write requests talk to the data store directly, this reduces the API latency even further.
* Managing static partition in a central registry is a simpler way of managing partitions. In case we see one of the cache server is getting hot, we can move some of the logical shards from that machine to other machine or completely allocate a new machine itself. Also since it’s not automated & human intervention is required to manipulate or move partitions, usually chances of operational mistake is very less. Although there is a trade off here — with increasing growth or insane growth, when there are thousands of physical machines, managing static partition can become a pain, hence automated partitioning scheme should be explored & tools to be developed when those use cases arrive.

### Get the city name
* Google Maps provides reverse Geo-Coding API to identify current city & related location information. The API can be integrated both in Android & iOS Apps.

### Audit support
* Also in the above architecture, we are putting the data to a queue from which a consumer picks up those location data & updates the database with proper metadata like timestamp or order id etc. This is done only for tracking the history & tracing the delivery agent’s journey in case it’s required, it’s a secondary part to our discussion though.

## Data modeling
### City-based sharding
* We have introduced a location data sharding scheme based on city name (It could be city id as well in case there is a risk of city name collision). We divide location data into several logical shard. A server or physical machine can contain many logical shards. A logical shard contains all locations data belonging to a particular city only. 
* Motivation:
  * Well, there is no standard partitioning that can be implemented for different use cases, it depends on what kind of application we are talking about & how the data access pattern looks like.
  * Let’s consider a hyper-local food delivery application. We need to assign delivery agents efficiently to customers so that orders can be dispatched in short time. There can be different kind of parameters which will determine whether a delivery agent can be assigned to an order e.g; how far the agent is from the restaurant & customer location, is he in transit & can take another order on his way and many others. But before applying all these filters, we need to fetch a limited number of delivery agents. Now most of the delivery agents will be bound to a city if not a specific locality. A delivery agent from Bangalore won’t usually deliver any order to a customer residing at Hyderabad. Also there will be finite number of agents in a city and possibly that number can hardly touch a maximum of few thousands only. And searching through these few thousands locations for a batch of orders ( orders from a locality or city can be batched & dispatched together for better system performance ) can be a good idea. So, with this theory, we can use city name as the partition key for a hyper local system.

### Use logical shard to solve small / big city problems
* In this allocation strategy, it might happen that a big city contains lots of locations whereas a smaller city contains less number of locations. In order to manage these shards & balance the load evenly across all possible cache servers, we are using a central metadata registry. The metadata registry contains mapping from city name to logical shard id, logical shard id to in-memory index server id mapping ( using service discovery mechanism, we can get the IP address of a server using the server id, the discussion is out of scope of this article ), something like below:

### Schema example
```
City to shard mapping:
----------------------
city           shard_id
------------------------
Bangalore        101
Hyderabad        103
Mumbai           109
New York         908
San Francisco    834

Shard to Physical server mapping
--------------------------------
shard_id       index_server
----------------------------
101             index-1
103             index-2
109             index-1
908             index-3
834             index-2

A shard (say index-1) content (location object) looks like below:
"San Francisco": [
    {
        "agent_id": 7897894,
        "lat": 89.678,
        "long": 67.894
    }, 
    {
        "agent_id": 437833,
        "lat": 88.908,
        "long": 67.109
    }, 
    ...
 ]
```

### Cons of city based partition
* City border problem: It’s quite possible that one of our delivery agents is currently located near the boarder of two cities — say he is at city A, an order comes from a neighbour city B & the agent’s distance from the customer at city B is quite less, but unfortunately we can’t dispatch the agent as he is not in city B. So at times, city based partitioning may not be optimal for all use cases. Also with growing demand from a city for a particular occasion like Christmas or New Year, a city based shard can become very hot. This strategy might work for hyper local systems but not for a system like Uber due to its very high scale.
* One possible solution: Use more complex sharding keys. For example, Uber uses City + Product sharding. Uber employs similar partitioning strategy but it’s not only on city/region — it’s region + product type (pool, XL or Go whatever). Uber has geographically distributed products across countries. So partitioning by a combination of product type & city works fine for them. To search for available Uber pool cabs in a region, you just go to the pool bucket for that region & retrieve all the cabs currently available there & likewise for all other use cases.
