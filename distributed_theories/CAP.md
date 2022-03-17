- [History](#history)
- [Definition](#definition)
  - [Consistency](#consistency)
  - [Availability](#availability)
  - [Partition tolerance](#partition-tolerance)
- [Use case](#use-case)
- [Limitations](#limitations)
  - [Not choose two from three](#not-choose-two-from-three)
  - [Limited model](#limited-model)
    - [Consistency model](#consistency-model)
    - [Fault tolerant model](#fault-tolerant-model)
- [References](#references)

# History
* Applications don't require linearizability can be more tolerant of network problems \(CAP\)

# Definition
## Consistency
* Every read would get the most recent write. Actually means linearizability. 

## Availability
* Availability: Every request received by the nonfailing node in the system must result in a response. 

## Partition tolerance
* Partition tolerance: The cluster can survive communication breakages in the cluster that separate the cluster into multiple partitions unable to communicate with each other. 

# Use case
* Simplified theory for elementary distributed system learners. 

# Limitations
## Not choose two from three
* Def: Either choose consistency or availability when partitioned. 

## Limited model
### Consistency model
* It only considers one consistency model \(namely linearizability\) 

### Fault tolerant model
* It only considers one kind of fault \(network partitions\). It doesn't say anything about network delays, dead nodes, or other trade-offs. 

# References
* https://martin.kleppmann.com/2015/05/11/please-stop-calling-databases-cp-or-ap.html