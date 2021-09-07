
- [Clock and Time](#clock-and-time)
  - [Overview](#overview)
  - [TrueTime](#truetime)
  - [Hybrid logical clock (HLC)](#hybrid-logical-clock-hlc)
    - [Adopters](#adopters)
    - [CockroachDB's implementation](#cockroachdbs-implementation)
  - [Timestamp Oracle (TSO)](#timestamp-oracle-tso)
    - [Adopters](#adopters-1)
    - [TiDB's implementation](#tidbs-implementation)
    - [Cons](#cons)
  - [SequoiaDB Time Protocol (STP)](#sequoiadb-time-protocol-stp)
  - [Conflict resolution - Vector clock](#conflict-resolution---vector-clock)
  - [TODO](#todo)

# Clock and Time

## Overview

|              |  `Physical clock - Multiple time source`   |  `Physical clock - Single time source` | `Logical clock - Multiple time source`  | `Logical clock - Single time source`  |
|--------------|--------------------|---|---|---|
| `Single point assigns time` |   NA  |  NA  |  NA  | TSO (TIDB)  |
| `Multiple point assigns time` |  TrueTime (Spanner)  |  NTP  | HLC (CockroachDB)  | STP |

## TrueTime
* Spanner uses TrueTime. The time source is GPS and atomic clock. And it relies on hardware. 
* Pros
  * High reliability and performance. Remove the centralized design and there is no SPF. 
  * Support global deployment. The distance between time server and clients could be controlled. 
* Cons
  * There is margin of error for 7ms. 

## Hybrid logical clock (HLC)

### Adopters
* CockroachDB and YugabyteDB both uses hybrid logical clocks. 
* This originates from Lamport stamp. 

### CockroachDB's implementation

![](./images/relational_distributedDb_HLC.png)

## Timestamp Oracle (TSO)
* A single incremental logical timestamp.

### Adopters
* TiDB / OceanBase / GoldenDB / TBase. 

### TiDB's implementation
* Global clock consists of two parts: High bits are physical clock and low bits (18) are logical clock. 
* How to solve the SPF? 
  * Multiple placement driver becomes a raft group. And a new master will be elected when the original node becomes down. 
* How to make sure the new master timestamp is bigger than old master timestamp?
  * Store the timestamp inside etcd. 
* How to avoid saving each timestamp inside etcd due to performance reasons?
  * Preallocate a time window for timestamps

![](./images/relational_distributedDb_TSO_TiDB.png)

### Cons
* Upper limit on performance and could not deploy on a large scale. 


## SequoiaDB Time Protocol (STP)
* STP 

## Conflict resolution - Vector clock
* Pros:
	- Not requiring clock synchronization across all nodes, and helps us identify transactions that might be in conflict.
* Cons:
	- Need to send the entire Vector to each process for every message sent, in order to keep the vector clocks in sync. When there are a large number of processes this technique can become extremely expensive, as the vector sent is extremely large.
* Update rules:
	- Rule 1: before executing an event (excluding the event of receiving a message) process Pi increments the value v[i] within its local vector by 1. This is the element in the vector that refers to Processor(i)’s local clock.
	- Rule 2: when receiving a message (the message must include the senders vector) loop through each element in the vector sent and compare it to the local vector, updating the local vector to be the maximum of each element. Then increment your local clock within the vector by 1 [Figure 5].

```
// Rule 1
local_vector[i] = local_vector[i] + 1

// Rule 2
1. for k = 1 to N: local_vector[k] = max(local_vector[k], sent_vector[k])
2. local_vector[i] = local_vector[i] + 1
3. message becomes available.
```
* A sample flow chart

![Vector clock](./images/keyValue-database-vectorclock.png)

## TODO
* [All Things Clock, Time and Order in Distributed Systems: Physical Time in Depth](https://medium.com/geekculture/all-things-clock-time-and-order-in-distributed-systems-physical-time-in-depth-3c0a4389a838)
* [All Things Clock, Time and Order in Distributed Systems: Logical Clocks in Real Life](https://medium.com/geekculture/all-things-clock-time-and-order-in-distributed-systems-logical-clocks-in-real-life-2-ad99aa64753)
* [All Things Clock, Time and Order in Distributed Systems: Hybrid Logical Clock in Depth](https://medium.com/geekculture/all-things-clock-time-and-order-in-distributed-systems-hybrid-logical-clock-in-depth-7c645eb03682)
* [All Things Clock, Time and Order in Distributed Systems: Logical Clock vs Google True Time](https://medium.com/geekculture/all-things-clock-time-and-order-in-distributed-systems-logical-clock-vs-google-true-time-dba552f2d842)