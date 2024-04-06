- [Redis implementation](#redis-implementation)
  - [Requirements](#requirements)
  - [Redis SortedSet](#redis-sortedset)

# Redis implementation
## Requirements
1. Current location of an object (drivers in case of Uber, delivery agent’s location in case of a food delivery app).
2. Mapping from a Geo-Hash prefix to the objects
3. Proper expiry of the dynamic location data since in this use case, we are dealing with dynamic objects.

## Redis SortedSet
2. For requirements 2 & 3 above, we can implement Redis sorted set (priority queue). The key of the sorted set will be the Geo-Hash prefix of length L. The member is objects’s id which are currently sharing the Geo-Hash prefix (basically they are withing the region represented by the Geo-Hash). And the score is current timestamp, we use the score to delete older data.

```shell
# This is how we set Redis sorted set for a given object location belonging to a Geo-Hash prefix:
$ ZADD key score member
$ ZADD geo_hash_prefix current_timestamp object_id

# Example:
$ ZADD 6e10h 1603013034 7619
$ ZADD 6e10h 1603013050 2781
$ ZADD a72b8 1603013089 9082

# Let's say our expiry time is 30 seconds, so just before retrieving current objects for a request belonging to a Geo-Hash prefix, we can delete all data older than current timestamp - 30 seconds, this way, expiration will happen gradually over time:

$ ZREMRANGEBYSCORE geo_hash_prefix -INF current_timestamp - 30 seconds
# -INF = Redis understands it as the lowest value
```
