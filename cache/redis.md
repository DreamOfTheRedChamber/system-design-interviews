- [Redis (REmote DIctionary Server)](#redis-remote-dictionary-server)
- [Features](#features)
- [Memory Management Technique](#memory-management-technique)
- [Use cases](#use-cases)
- [Limitations](#limitations)

# Redis (REmote DIctionary Server)
One of the most popular cache, in memory key value store ever known in the market.
Redis is often referred as a data structures server. What this means is that Redis provides access to mutable data structures via a set of commands, which are sent using a server-client model with TCP sockets and a simple protocol. So different processes can query and modify the same data structures in a shared way.

# Features
* Redis supports native mutable data structures namely — list, set, sorted set, string, hash. It also supports range queries, bitmap, hyperloglogs, geo-spatial indexes with radius queries.
* Redis stores all data in memory, essentially redis is a big in-memory dictionary. So it’s very fast. It can run commands in pipeline.
* Data can be asynchronously saved on disk after a configured interval or a specific number of operations.
* Redis is typically known as single threaded. It means the application logic that directly serves the clients is a single thread only. While syncing data on disk, redis spawns background thread which does not directly deal with clients.
* Redis supports out of the box master slave replication. It’s just a configuration settings & replication is up & running.
* Redis supports transaction. All commands in a transaction are serialized & they run sequentially. As usual, redis transactions also guarantee either all commands will pass or none are processed.
* Redis keys are TTL or expiration time supported.
* Redis has out of the box support for pub-sub mechanism. It has commands that enable pub-sub.
* Automatic failover is supported by Redis Sentinel.
* Redis supports server side Lua scripting. So a batch of commands can run without much hassle of communication between server & client.
* Redis is portable, works on almost all varities of Linux, Windows, Mac etc.
* Support for size of value upto 512 MB per key.
* Also Redis enterprise edition supports a lot more features.

# Memory Management Technique
* Redis supports following techniques:
  * allkeys-lru: Evicts the least recently used keys out of all keys.
  * allkeys-random: Randomly evicts keys out of all keys.
  * volatile-lru: Evicts the least recently used keys out of all keys with an “expire” field set.
  * volatile-ttl: Evicts the shortest time to live keys (out of all keys with an “expire” field set).
  * volatile-random: Evicts keys with an “expire” field set.
  * no-eviction: Redis will not evict any keys and no writes will be possible until more memory is freed.

# Use cases
* Redis hash can be used in place of relational tables if you can model your data accordingly & your use cases don’t require any transactional guarantee.
* Redis pub-sub can be used to broad cast messages to multiple subscribers.
* Redis list can be used as queue of messages. Celery — a distributed task processing system leverages redis data structures to manage tasks.
* Session store is a very popular use case for redis. Persistent ability of redis makes it suitable for such case.
* Redis sorted sets can be used to manage leader boards in online gaming.
* Redis can store, increment, decrement integers. It can be used to generate global id for any use cases.

# Limitations
* Redis does not support secondary index.
* Redis offers querying / scanning of data based on regular expression search on key names. So before choosing to use Redis data structures like hash, sorted sets etc, try to think in terms of how your applications fits into Redis & what is your access pattern of data in case you use these data structures. For simple key value use cases, it’s chill, you don’t need to think a lot.
* Notable Redis Users: Twitter, GitHub, Weibo, Pinterest, Snapchat, Craigslist, Digg, StackOverflow, Flickr
