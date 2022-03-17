- [Vector clock](#vector-clock)
- [Tunable consistency model - Quorum NWR](#tunable-consistency-model---quorum-nwr)

# Vector clock

* Vector clock: Published by Lesie Lamport in 1978. [Time, Clocks and the Ordering of Events in a Distributed System](https://www.microsoft.com/en-us/research/publication/time-clocks-ordering-events-distributed-system/)
* Clock synchronization: [UMass course](http://lass.cs.umass.edu/\~shenoy/courses/spring05/lectures/Lec10.pdf)
* [Why vector clocks are easy](https://riak.com/posts/technical/why-vector-clocks-are-easy/)
* [Why vector clocks are hard](https://riak.com/posts/technical/why-vector-clocks-are-hard/)

# Tunable consistency model - Quorum NWR
* Dynamo DB / Cassandra
* Quorum NWR Definition:
  * N: The number of replicas
  * W: A write quorum of size W. For a write operation to be considered as successful, write operation must be acknowledged from W replicas
  * R: A read quorum of size W. For a read operation to be considered as successful, read operation must be acknowledged from R replicas
* If W+R > N, could guarantee strong consistency because there must be at least one overlapping node that has the latest data to ensure consistency
* Typical setup:
  * If R = 1 and W = N, the system is optimized for a fast read
  * If R = N and W = 1, the system is optimized for a fast write
  * If W + R > N, strong consistency is guaranteed (Usually N = 3, W = R = 2)
