- [Consensus algorithm](#consensus-algorithm)
  - [Paxos](#paxos)
  - [Gossip](#gossip)
    - [History](#history)
    - [Vector clock](#vector-clock)
    - [Gossip protocol demo](#gossip-protocol-demo)
  - [Raft](#raft)
    - [ZAB algorithm](#zab-algorithm)
    - [Design considerations](#design-considerations)
    - [Pros and Cons](#pros-and-cons)
    - [Raft protocol demo](#raft-protocol-demo)
    - [Raft replication performance cons](#raft-replication-performance-cons)
      - [Raft single transaction replication process](#raft-single-transaction-replication-process)
      - [Raft multiple transaction replication process](#raft-multiple-transaction-replication-process)
      - [Ways to optimize Raft replication performance](#ways-to-optimize-raft-replication-performance)
        - [Batch](#batch)
        - [Pipeline](#pipeline)
        - [Append log parallelly](#append-log-parallelly)
        - [Asynchronous apply](#asynchronous-apply)

# Consensus algorithm
## Paxos
* [Paxos Made Live - An Engineering Perspective](https://static.googleusercontent.com/media/research.google.com/en//archive/paxos_made_live.pdf)
* [Net algorithms](http://harry.me/blog/2014/12/27/neat-algorithms-paxos/)

## Gossip
### History 
* [Amazon Dynamo DB](http://bnrg.eecs.berkeley.edu/~randy/Courses/CS294.F07/Dynamo.pdf)
* Within this paper, it introduces two concepts vector clock and gossip. 

### Vector clock
* Vector clock: Published by Lesie Lamport in 1978. [Time, Clocks and the Ordering of Events in a Distributed System](https://www.microsoft.com/en-us/research/publication/time-clocks-ordering-events-distributed-system/)
* Clock synchronization: [UMass course](http://lass.cs.umass.edu/~shenoy/courses/spring05/lectures/Lec10.pdf)
* [Why vector clocks are easy](https://riak.com/posts/technical/why-vector-clocks-are-easy/)
* [Why vector clocks are hard](https://riak.com/posts/technical/why-vector-clocks-are-hard/)

### Gossip protocol demo
* [Understanding Gossip](https://www.youtube.com/watch?v=FuP1Fvrv6ZQ&ab_channel=PlanetCassandra)
* [Visualization](https://rrmoelker.github.io/gossip-visualization/)

## Raft
* Original paper: https://raft.github.io/raft.pdf
* Translated in Chinese: https://infoq.cn/article/raft-paper

### ZAB algorithm
* Consistency algorithm: ZAB algorithm
* To build the lock, we'll create a persistent znode that will serve as the parent. Clients wishing to obtain the lock will create sequential, ephemeral child znodes under the parent znode. The lock is owned by the client process whose child znode has the lowest sequence number. In Figure 2, there are three children of the lock-node and child-1 owns the lock at this point in time, since it has the lowest sequence number. After child-1 is removed, the lock is relinquished and then the client who owns child-2 owns the lock, and so on.
* The algorithm for clients to determine if they own the lock is straightforward, on the surface anyway. A client creates a new sequential ephemeral znode under the parent lock znode. The client then gets the children of the lock node and sets a watch on the lock node. If the child znode that the client created has the lowest sequence number, then the lock is acquired, and it can perform whatever actions are necessary with the resource that the lock is protecting. If the child znode it created does not have the lowest sequence number, then wait for the watch to trigger a watch event, then perform the same logic of getting the children, setting a watch, and checking for lock acquisition via the lowest sequence number. The client continues this process until the lock is acquired.
* Reference: https://nofluffjuststuff.com/blog/scott_leberknight/2013/07/distributed_coordination_with_zookeeper_part_5_building_a_distributed_lock

### Design considerations
* How would the client know that it successfully created the child znode if there is a partial failure (e.g. due to connection loss) during znode creation
	- The solution is to embed the client ZooKeeper session IDs in the child znode names, for example child-<sessionId>-; a failed-over client that retains the same session (and thus session ID) can easily determine if the child znode was created by looking for its session ID amongst the child znodes.
* How to avoid herd effect? 
	- In our earlier algorithm, every client sets a watch on the parent lock znode. But this has the potential to create a "herd effect" - if every client is watching the parent znode, then every client is notified when any changes are made to the children, regardless of whether a client would be able to own the lock. If there are a small number of clients this probably doesn't matter, but if there are a large number it has the potential for a spike in network traffic. For example, the client owning child-9 need only watch the child immediately preceding it, which is most likely child-8 but could be an earlier child if the 8th child znode somehow died. Then, notifications are sent only to the client that can actually take ownership of the lock.

### Pros and Cons
* Reliable
* Need to create ephemeral nodes which are not as efficient

### Raft protocol demo
* [Raft - The Secret Lives of Data](http://thesecretlivesofdata.com/raft/)
* [Raft Consensus Algorithm](https://raft.github.io/)
* [Raft Distributed Consensus Algorithm Visualization](http://kanaka.github.io/raft.js/)

### Raft replication performance cons
* When comparing Paxos and Raft, Raft is typically slower in replication efficiency. Raft requires sequential vote.

#### Raft single transaction replication process
1. Leader receives client's requests
2. Leader appends the request (log entry) to local log. 
3. Leader forwards the log entry to other followers. 
4. Leader waits follower's result. If majority nodes have submitted the log, then this log entry becomes committed entry, and leader could apply it to its local machine.
5. Leader returns success to clients.
6. Leader continues to the next request. 

#### Raft multiple transaction replication process
1. Transaction T1 set X as 1 and all five nodes append successfully. Leader node appends the result to local and return success to client. 
2. For transaction T2, although there is one follower not responding, it still gets majority nodes to respond. So it returns success to clients. 
3. For transaction T3, it does not get responses from more than half. Now leader must wait for a explicit failure such as timing out before it could terminate this operation. Since there is the requirement on sequential vote, T3 will block all subsequent transactions. So both T4 and T5 are blocked (T4 operates on the same data. Although T5 operates on different data, it also becomes blocked.)

![](./images/relational_distributedDb_raft_replication_perf.png)

#### Ways to optimize Raft replication performance
##### Batch
* Leader ccaches multiple requests from clients, and then pass this batch of log to follower

##### Pipeline
* Leader adds a local variable called nextIndex, each time after sending a batch, update nextIndex to record the next batch position. It does nt wait for follower to return and immediately send the next batch. 

##### Append log parallelly
* When leader send batch info to follower, it executes local append operation in the mean time. 

##### Asynchronous apply
* Applying the log entry locally is not a necessary condition for success and any log entry in committed state will not lose. 
