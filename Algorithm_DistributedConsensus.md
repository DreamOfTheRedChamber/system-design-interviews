- [Consensus algorithm](#consensus-algorithm)
  - [Adoption in real life](#adoption-in-real-life)
    - [Strong consistency model - Paxos/Raft](#strong-consistency-model---paxosraft)
    - [Tunable consistency model - Quorum NWR](#tunable-consistency-model---quorum-nwr)
    - [Eventual consistency model - Gossip](#eventual-consistency-model---gossip)
      - [Use cases](#use-cases)
      - [Use examples](#use-examples)
  - [Paxos](#paxos)
  - [ZAB](#zab)
  - [Gossip](#gossip)
    - [Motivation - Multicast problem](#motivation---multicast-problem)
      - [Centralized multicasting](#centralized-multicasting)
      - [Spanning tree based multicasting](#spanning-tree-based-multicasting)
    - [States of a node](#states-of-a-node)
      - [Removed state](#removed-state)
    - [Types](#types)
      - [Direct mail](#direct-mail)
      - [Anti-entropy (SI model)](#anti-entropy-si-model)
      - [Rumor mongering (SIR model)](#rumor-mongering-sir-model)
    - [Communication process](#communication-process)
      - [Three way communication](#three-way-communication)
      - [Push and Pull](#push-and-pull)
    - [Code impl](#code-impl)
  - [Quorum NWR](#quorum-nwr)
  - [Raft](#raft)
    - [Overview](#overview)
    - [Concept foundations](#concept-foundations)
      - [State machine](#state-machine)
      - [Committed & Uncommitted log](#committed--uncommitted-log)
      - [Roles](#roles)
      - [RPC based node communication](#rpc-based-node-communication)
        - [RequestVote (RV)](#requestvote-rv)
        - [AppendEntries (AE)](#appendentries-ae)
      - [Term](#term)
      - [Random timeout](#random-timeout)
    - [Algorithm](#algorithm)
      - [Leader election](#leader-election)
      - [Log replication](#log-replication)
        - [Replication location](#replication-location)
        - [Flowchart](#flowchart)
      - [Avoid brain split during membership change](#avoid-brain-split-during-membership-change)
    - [Read and write paths](#read-and-write-paths)
    - [Enumeration of possible cases](#enumeration-of-possible-cases)
      - [1. Replicate a client command successfully with majority](#1-replicate-a-client-command-successfully-with-majority)
      - [2. Many followers crash together & no majority followers exists](#2-many-followers-crash-together--no-majority-followers-exists)
      - [3. Before replicating to the majority, the leader crashes](#3-before-replicating-to-the-majority-the-leader-crashes)
      - [4. Leader crashes just before committing a command to the state machine](#4-leader-crashes-just-before-committing-a-command-to-the-state-machine)
      - [5. Leader crashes after committing a command to itself but before sending commit request to the followers](#5-leader-crashes-after-committing-a-command-to-itself-but-before-sending-commit-request-to-the-followers)
      - [6. Leader crashes, comes back after sometime — the Split Vote Problem](#6-leader-crashes-comes-back-after-sometime--the-split-vote-problem)
      - [7. A follower has more logs than the current leader](#7-a-follower-has-more-logs-than-the-current-leader)
    - [Additional](#additional)
      - [Raft replication performance cons](#raft-replication-performance-cons)
      - [Raft single transaction replication process](#raft-single-transaction-replication-process)
      - [Raft multiple transaction replication process](#raft-multiple-transaction-replication-process)
      - [Ways to optimize Raft replication performance](#ways-to-optimize-raft-replication-performance)
  - [References](#references)
    - [Raft](#raft-1)
    - [Gossip](#gossip-1)
    - [Vector clock](#vector-clock)
    - [Real life](#real-life)

# Consensus algorithm
## Adoption in real life
### Strong consistency model - Paxos/Raft 
![](./images/algorithm_consensus_implementationn.png)

* The acronyms under usage patterns stand for server replication (SR), log replication (LR), synchronisation service (SS), barrier orchestration (BO), service discovery (SD), leader election (LE), metadata management (MM), and Message Queues (Q).
* References: https://blog.container-solutions.com/raft-explained-part-1-the-consenus-problem

### Tunable consistency model - Quorum NWR
* Dynamo DB / Cassandra

### Eventual consistency model - Gossip
#### Use cases
* Database replication
* Information dissemination
* Cluster membership
* Failure Detectors
* Overlay Networks
* Aggregations (e.g calculate average, sum, max)

#### Use examples
* Riak uses a gossip protocol to share and communicate ring state and bucket properties around the cluster.
* In CASSANDRA nodes exchange information using a Gossip protocol about themselves and about the other nodes that they have gossiped about, so all nodes quickly learn about all other nodes in the cluster. [9]
* Dynamo employs a gossip based distributed failure detection and membership protocol. It propagates membership changes and maintains an eventually consistent view of membership. Each node contacts a peer chosen at random every second and the two nodes efficiently reconcile their persisted membership change histories [6].
* Dynamo gossip protocol is based on a scalable and efficient failure detector introduced by Gupta and Chandra in 2001 [8]
* Consul uses a Gossip protocol called SERF for two purposes [10]:
   – discover new members and failures 
   – reliable and fast event broadcasts for events like leader election.
* The Gossip protocol used in Consul is called SERF and is based on “SWIM:  Scalable Weakly-consistent Infection-style Process Group Membership Protocol”
Amazon s3 uses a Gossip protocol to spread server state to the system [8].


## Paxos
* [Paxos Made Live - An Engineering Perspective](https://static.googleusercontent.com/media/research.google.com/en//archive/paxos_made_live.pdf)
* [Net algorithms](http://harry.me/blog/2014/12/27/neat-algorithms-paxos/)

## ZAB
* Consistency algorithm: ZAB algorithm
* To build the lock, we'll create a persistent znode that will serve as the parent. Clients wishing to obtain the lock will create sequential, ephemeral child znodes under the parent znode. The lock is owned by the client process whose child znode has the lowest sequence number. In Figure 2, there are three children of the lock-node and child-1 owns the lock at this point in time, since it has the lowest sequence number. After child-1 is removed, the lock is relinquished and then the client who owns child-2 owns the lock, and so on.
* The algorithm for clients to determine if they own the lock is straightforward, on the surface anyway. A client creates a new sequential ephemeral znode under the parent lock znode. The client then gets the children of the lock node and sets a watch on the lock node. If the child znode that the client created has the lowest sequence number, then the lock is acquired, and it can perform whatever actions are necessary with the resource that the lock is protecting. If the child znode it created does not have the lowest sequence number, then wait for the watch to trigger a watch event, then perform the same logic of getting the children, setting a watch, and checking for lock acquisition via the lowest sequence number. The client continues this process until the lock is acquired.
* Reference: https://nofluffjuststuff.com/blog/scott_leberknight/2013/07/distributed_coordination_with_zookeeper_part_5_building_a_distributed_lock

## Gossip
* Reference: https://managementfromscratch.wordpress.com/2016/04/01/introduction-to-gossip/#applications

### Motivation - Multicast problem
* Multicast is a process of sending messages or exchanging information to a particular group or destination in a network. The requirements for multicast protocols are:
  * Fault tolerance — The nodes in the network can be faulty, they may crash and the packets may be dropped. Despite all these problems, the nodes should communicate a multicast message with each other in the network.
  * Scalable — The nodes in the network should be scalable even if the network is having thousands of nodes the overhead should be minimum at the root node.

#### Centralized multicasting
* The one node will act as a sender and it sends the multicast messages to all nodes in the network.
* Cons:
  * The nodes can be faulty and after sending multicast messages to the few nodes using for loop there can be a situation where some of the nodes will receive the multicast messages and others not and also the overhead at the root node will be high if there are thousands of nodes in the network and the latency will be very high.

#### Spanning tree based multicasting
* In tree-based multicast protocols:
  - Buiding a spanning tree among the processes of the multicast group.
  - Use spanning tree to spread multicast messages.
  - Use either ACK or NAK to repair the multicast not received.
* Examples: The IPmulticast, SRM, RMTP, TRAM, TMTP are examples of tree-based multicast protocols.
* Pros:
  * Complexity at O(logN), where N is the total number of nodes in the tree. 
* Cons:
  * If an intermediate node in the tree doesn't get the multicast message then the descendants of that node cannot get the multicast message. To overcome such problems the ACK and NAK messages are used to acknowledge the sender that the intended receiver doesn’t get the multicast message.

### States of a node
* Infective: A node with an update it is willing to share.
* Susceptible: A node that has not received the update yet (It is not infected).
* Removed: A node that has already received the update but it is not willing to share it.

#### Removed state
* Removed is trickier than rest. It’s not easy to determine when a node should stop sharing the info/update. Ideally a node should stop sharing the update when all the nodes is linked with have the update. But that would mean that node would have to have knowledge of the status of the other nodes.

### Types
#### Direct mail
* The one they had in place initially, each new update is immediately emailed from its entry site to all other sites but it presented several problems:
  * The sending node was a bottleneck O(n).
  * Each update was propagated to all the nodes, so each node had to know all the nodes in the system.
  * Messages could be discarded when a node was unresponsive for a long time or due to queue overflow.

#### Anti-entropy (SI model)
* In Anti-entropy (SI model) a node that has an infective info is trying to share it in every cycle. A node not only shares the last update but the whole database, there are some techniques like checksum, recent update list, merkle trees, etc that allow a node to know if there are any differences between the two nodes before sending the database, it guarantees, eventual,  perfect dissemination.
* There is not termination, so It sends an unbounded number of messages.
* Cons:
  * Require per pair node data exchange, not suitable for environments with lots of nodes. 
  * Require knowledge of existing nodes, not suitable in dynamic changing environment. 

#### Rumor mongering (SIR model)
* Rumor Mongering cycles can be more frequent than anti-entropy cycles because they require fewer resources, as the node only send the new update or a list of infective updates. Rumour mongering spreads updates fast with low traffic network.
* A rumor at some point is marked as removed and it’s not shared any more, because of that, the number of messages is bounded and there is some chance that the update will not reach all the sites, although this probability can be made arbitrarily small as we’ll see later. First let’s see how to decide when a node should be in state “removed”.

### Communication process
#### Three way communication
* The gossip process runs every second for every node and exchange state messages with up to three other nodes in the cluster (This is for Cassandra). Since the whole process is decentralized, there is nothing or no one that coordinates each node to gossip. Each node independently will always select one to three peers to gossip with. It will always select a live peer (if any) in the cluster, it will probabilistically pick a seed node from the cluster or maybe it will probabilistically select an unavailable node.
* A three way communication similar to TCP handshake: 
  1. SYN: The node initiating the round of gossip sends the SYN message which contains a compendium of the nodes in the cluster. It contains tuples of the IP address of a node in the cluster, the generation and the heartbeat version of the node.
  2. ACK: The peer after receiving SYN message compares its own metadata information with the one sent by the initiator and produces a diff. ACK contains two kinds of data. One part consists of updated metadata information (AppStates) that the peer has but the initiator doesn't, and the other part consists of digest of nodes the initiator has that the peer doesn't.
  3. ACK2: The initiator receives the ACK from peer and updates its metadata from the AppStates and sends back ACK2 containing the metadata information the peer has requested for. The peer receives ACK2, updates its metadata and the round of gossip concludes.
* You could refer to [Confluence Gossip Architecture](https://cwiki.apache.org/confluence/display/CASSANDRA2/ArchitectureGossip) for example message format. 

![](./images/algorithm_consensus_gossip_format.png)

#### Push and Pull
* PUSH: infective nodes are the ones sending/infecting susceptible nodes.
  * infective nodes are the ones infecting susceptible nodes.
  * very efficient where there are few updates.
* PULL: all nodes are actively pulling for updates. (A node can’t know in advance new updates, so it has to pull all continuously).
  * all nodes are actively pulling for updates.
  * very efficient where there are many updates.
* PUSH-PULL: It pushes when it has updates and it also pulls for new updates.
  * The node and selected node exchange their information.

### Code impl
* Please refer to [Implement Cassandra Gossip Protocol](https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49)

![](./images/algorithm_consensus_gossip_codeImpl.png)

## Quorum NWR
* Quorum NWR Definition:
	- N: The number of replicas
	- W: A write quorum of size W. For a write operation to be considered as successful, write operation must be acknowledged from W replicas
	- R: A read quorum of size W. For a read operation to be considered as successful, read operation must be acknowledged from R replicas
* If W+R > N, could guarantee strong consistency because there must be at least one overlapping node that has the latest data to ensure consistency
* Typical setup:
			- If R = 1 and W = N, the system is optimized for a fast read
			- If R = N and W = 1, the system is optimized for a fast write
			- If W + R > N, strong consistency is guaranteed (Usually N = 3, W = R = 2)

## Raft
* Original paper: https://raft.github.io/raft.pdf
* Translated in Chinese: https://infoq.cn/article/raft-paper

### Overview
* Process:
  * Step 1: Client ( i.e; a distributed database system ) sends a command ( i.e; something like an INSERT command in SQL) to the server.
  * Step 2: The consensus module at the leader handles the command: puts it into the leader’s log file & sends it to all other nodes in parallel.
  * Step 3: If majority of the nodes including the leader replicate the command successfully to their local log & acknowledge to the leader, the leader then commits the command to its own state machine.
  * Step 4: The leader acknowledges the status of the commitment to the client.

![](./images/algorithm_replicatedStateModel.png)


### Concept foundations
#### State machine
* State persisted on the nodes

![](./images/algorithm_consensus_raft_state.png)

#### Committed & Uncommitted log
* A log entry is committed only when it gets replicated by the majority nodes in the cluster. A committed log never gets overridden. A committed log is durable & eventually gets executed by all the nodes in the Raft cluster.
* If a client command / log entry is not yet replicated to the majority of the cluster nodes, it’s called uncommitted log. Uncommitted logs can be overridden in a follower node.

#### Roles
* Follower: Followers only respond to RPCs, but do not initiate any communication.
* Candidate: 
  * Candidates start a new election, incrementing the term, requesting a vote, and voting for themselves. 
  * Depending on the outcome of the election, become leader, follower (be outvoted or receive RPC from valid leader), or restart these steps (within a new term). 
  * Only a candidate with a log that contains all committed commands can become leader.
* Leader: 
  * The leader sends heartbeats (empty AppendEntries RPCs) to all followers, thereby preventing timeouts in idle periods. 
  * For every command from the client, append to local log and start replicating that log entry, in case of replication on at least a majority of the servers, commit, apply commited entry to its own leader state machine, and then return the result to the client. 
  * If logIndex is higher than the nextIndex of a follower, append all log entries at the follower using RPC, starting from the his nextIndex.

![](./images/algorithm_raft.png)

* Introducing preVote role
  * Motivation: To avoid unmeaningful elections. 

![](./images/algorithm_consensus_precandidate.png)

#### RPC based node communication
##### RequestVote (RV)
* When a node wants to become a leader, it asks other nodes to vote for it by sending this request.

![](./images/algorithm_consensus_raft_Rpc_RV.png)

##### AppendEntries (AE)
* Through this message, a leader asks the followers to add an entry to their log file. The leader can send empty message as well as a heartbeat indicating to the followers that it’s still alive.

![](./images/algorithm_consensus_raft_Rpc_AE.png)


#### Term
* Raft's term servers as many roles
  * Global logical clock within Raft algorithm. 
  * Impact leader election and request processing. 
    * If a candidate or leader realizes that its term is smaller than other nodes, then it will become follower. 
      * For example, after network partition recovery, a leader (term 3) receives a heartbeat message from another leader (term 4), then the original leader (term 3) will become follower. 
    * If a node receives a request from another node with smaller term ID, then it will directly reject the request. 
      * For example, if node C with term 4 receives a RPC request from node with term 3, then it will directly reject the message. 

* Term 1 starts when the cluster starts up. A leader is elected for term 1 & normal operations like log replication, heartbeat continues till the term 1 ends. The leader dies. Node X increases its term to 2 , gets elected as the new leader & term 2 continues. Now X also dies at some point, some other node Y starts the election process, increases the term to 3, but unfortunately the election fails to choose a leader resulting in term 3 termination. And the process goes on.

![](./images/algorithm_consensus_term.png)

#### Random timeout
* In Raft there are two timeout settings which control elections.
  * Election timeout: The election timeout is the amount of time a follower waits until becoming a candidate. The election timeout is randomized to be between 150ms and 300ms. After the election timeout the follower becomes a candidate and starts a new election term.
  * Heartbeat interval: The interval during which leader will send followers a heartbeat message. 

### Algorithm
#### Leader election
* Leader crash scenario:
  1. Leader node A becomes abnormal. 
  2. Whens follower B does not receive leader's msgHeartbeat after election timeout (heartbeat-interval 100ms, election timeout 1000ms), it will become candidate. 
  3. Candidate B will start election process, self-increment term number, vote for themselves and send other nodes msgVote.  
  4. After Node C receives follower B's election message for leader. There are two possible conditions: 
     * Condition 1: C will vote for B if all of the following conditions satisfy:
       *  B's data is at least as new as it has.
       *  B's term is bigger than C's current term. 
       *  C has not voted in this term for other candidates yet. 
     * Condition 2: C also misses leader's msgHeartbeat after election timeout, and C already has started election and voted for itself. Then it will reject to vote for B. In this case if no nodes could get majority votes, then a new round of vote will start. 
  5. Old leader node A restarts after crash. 
     * Condition 1: If A remains in network partition with majority of node, then it will become 
     * Condition 2: When the old leader A finds a new term number, it will need to transit to the follower role. 

#### Log replication
##### Replication location

![](./images/raft_log_replication_consistency.png)

##### Flowchart
* Process
  * Step1: Leader gets the request from client. 
  * Step2: Leader replicates the log entry to other followers through RPC. 
  * Step3: When leader replicates this log entry successfully to other followers, leader will apply the log entry to its local state machine. 
  * Step4: Leader returns the execution results to clients. 
  * Step5: After follower receives heartbeat message or log replication msg, if it realizes that provider already submitted some log and it has not, then it will apply the log entry to its local state machine. 

![](./images/raft_log_replication.png)

#### Avoid brain split during membership change

### Read and write paths
* A write operation has to always go through the leader.
* A read path can be configured based on the system’s read consistency guarantee, couple of options:
  1. Only the leader can serve the read request — it checks the result of the client query in its local state machine & answers accordingly. Since the leader handles all the write operation, this read is strongly consistent read.
  2. Any node can serve read- it means faster read but possibly stale at times. Any node disconnected from the cluster due to network partition can potentially serve the client query & the result might be stale.

### Enumeration of possible cases
* References: https://codeburst.io/making-sense-of-the-raft-distributed-consensus-algorithm-part-3-9f3a5cdba514

#### 1. Replicate a client command successfully with majority
* The leader node S2 gets a command from the client. It adds the entry to its own log at index 1( The logs in the following diagrams are 1-based ). The dotted line around the rectangle at position 1 in S2 bucket represents that the entry is uncommitted. The orange colour arrows indicate that the leader is sending AppendEntries RPC to the rest of the nodes with the intention to store the data in the majority of the nodes.

![](./images/algorithm_consensus_raft_success_1.png)

* The starting index of the follower logs is also 1. All the followers receive the message, adds the log command to their individual logs, reset their election timer & acknowledges to the leader affirmatively.

![](./images/algorithm_consensus_raft_success_2.png)

* At this point, leader & all the followers have added the command to their disk based persistent log.
* Since all the followers responded positively, the leader got clear majority & commits the command to its local state machine. The solid black line around the rectangle in S2 bucket at index 1 in the following diagram indicates that the command is now is permanently committed by the leader. The leader can safely communicate to the client that the command has been written successfully in the system.
* The followers have not committed the command yet since they are unaware of the leader’s commitment status.

![](./images/algorithm_consensus_raft_success_3.png)

* In the next AppendEntries RPC, the followers get updated commit index from the leader & they commit too in their local state machines.

![](./images/algorithm_consensus_raft_success_4.png)

* As seen in the above diagram, entries are committed in the followers now & they acknowledge back to the leader with success.

![](./images/algorithm_consensus_raft_success_5.png)

#### 2. Many followers crash together & no majority followers exists
* Before returning error to the client, the leader retries replication few times. Since it clearly does not get the majority in this case, there would be no change in commitIndex of the leader. Hence no actual replication actually happens immediately. However, typically the leader holds the entry in its log, with future replication, this entry would get replicated.
* This scenario is highly unlikely as we would like to place followers across multiple availability zone & unless our data centre or cloud provider badly screws up something, we won’t get into this situation.

#### 3. Before replicating to the majority, the leader crashes
* With leader, the data may also get lost. Since data is not replicated to the majority, Raft does not give any guarantee on data retention.
* Corner Case: Say the leader successfully replicated the log entry to the follower S1. The leader dies. Now in the absence of the leader, if S1 starts the leader election process & wins, since possibly it has more log than other followers, the log entries copied earlier won’t get lost.

#### 4. Leader crashes just before committing a command to the state machine
* S1 is the leader which already replicated a log entry at index 1 to all the nodes in this diagram. However, S1 crashes before committing it to the local state machine.

![](./images/algorithm_consensus_raft_case4_1.png)

* Next time when the election happens, any of the other nodes except S1 can become the leader. Since the entry is already replicated to the majority by S1 , it’s logically as good as a committed entry, by the rules of Request Vote process in Algorithm 4 described earlier, at least one node would be there which contains this entry & that would be elected as the new leader.

* However, the new leader now won’t directly commit the log entry since after the new leader election, the entry belongs to a previous term — in the following figure, the new leader is elected with term 4 but the log entry belongs to the term 2 — all entries are surrounded by dotted rectangles meaning they are not committed yet.

![](./images/algorithm_consensus_raft_case4_2.png)

* Remember, Raft never commits entries from previous terms directly even though the entry is there in majority nodes. Raft always commits entries from the current term by counting replicas as shown in Algorithm 1, from line 77 to 94. When entries from the current term are replicated, entries from previous terms indirectly get replicated as shown below:

* In the above figure, a new log entry gets added to the new leader S2 in term 4, when it gets committed, the previous entry with term 2 also gets committed. Both entries at index 1 & 2 are within solid rectangles indicating they are committed.

![](./images/algorithm_consensus_raft_case4_3.png)

#### 5. Leader crashes after committing a command to itself but before sending commit request to the followers
* This is also same as case 4. As long as a log entry is replicated to the majority of the nodes in the cluster, it does not really matter whether the leader crashes before or after committing the log to its state machine. The next leader would be elected from one of the majority nodes only since they have higher log index than non-majority nodes. So no data loss happens here.

#### 6. Leader crashes, comes back after sometime — the Split Vote Problem
* If a leader suddenly disappears from the raft cluster ( but the client can still interact with it ) due to network partition or some error, a new leader would be potentially chosen by the majority. Ideally, all the new write operations have to be redirected to the new leader — this entirely depends on how you design the system to make the new leader discover-able by the client.

* How does a client discover th nnew leader: Three options available
  * **Redirect the operation internally in the cluster**: The write request can land on any node. If it lands on a follower node, it is redirected to the current leader by the follower; if it lands on the leader, the leader can serve it. However, to handle potential split-brain problem, before serving the request, the leader has to verify with other nodes in the cluster whether its leadership is still valid — it requires some extra check / RPC call resulting into higher write latency, but the client remains light since it does not need to bother who the current leader is.
  * **Cluster-aware client**: The client always gets update from the cluster about the current state. May be with a very short interval of heartbeat, the client keeps on updating the cluster state in its record & verifies existence of the current leader all the time by confirming with the majority nodes. The client becomes heavy in this case.
  * **Manage a central registry or configuration store**: You can manage a central registry which would be always updated with the current leader & other metadata of the cluster. Every time a new leader is elected, the configuration gets updated. So clients can contact the configuration store first to find the current leader & then sends a read / write request to the leader. However, the configuration store becomes a single point of failure now.

* What happens if a write operation is still received by the old leader?
  * The situation ideally should be rare. However, if it happens in some edge case, the data might get lost if it gets accepted by the old leader. Before accepting a write the leader can contact other nodes to validate whether it’s still a valid leader, however it makes the write operation very heavy but it prevents data loss since on error, the client can re-try the operation and the new request may land on the correct leader or valid cluster node.

#### 7. A follower has more logs than the current leader
* As stated earlier, follower logs can be overridden. In case a follower gets some extra log probably from an earlier leader but the logs don’t exist in majority node, Raft can safely override them.



### Additional
#### Raft replication performance cons
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
* Batch: Leader ccaches multiple requests from clients, and then pass this batch of log to follower
* Pipeline: Leader adds a local variable called nextIndex, each time after sending a batch, update nextIndex to record the next batch position. It does nt wait for follower to return and immediately send the next batch. 
* Append log parallelly: When leader send batch info to follower, it executes local append operation in the mean time. 
* Asynchronous apply: Applying the log entry locally is not a necessary condition for success and any log entry in committed state will not lose. 

## References
### Raft
* Talks
  * [You must build a Raft](https://www.youtube.com/watch?v=Hm_m4MIXn9Q&ab_channel=HashiCorp)
  * [Distributed Consensus with Raft - CodeConf 2016](https://www.youtube.com/watch?v=RHDP_KCrjUc&ab_channel=GitHub)
  * [Scale By The Bay 2018: Yifan Xing, Consensus Algorithms in Distributed Systems](https://www.youtube.com/watch?v=9QTcD8RrBP8&ab_channel=FunctionalTV)
  * [Understanding Distributed Consensus in etcd and Kubernetes - Laura Frank, CloudBees](https://www.youtube.com/watch?v=n9VKAKwBj_0&ab_channel=CNCF%5BCloudNativeComputingFoundation%5D)
  * [Designing for Understandability: The Raft Consensus Algorithm](https://www.youtube.com/watch?v=vYp4LYbnnW8&ab_channel=DiegoOngaro)
* Blogs:
  * [Raft deep dive](https://codeburst.io/making-sense-of-the-raft-distributed-consensus-algorithm-part-1-3ecf90b0b361)
* Raft protocol demo
  * [Raft - The Secret Lives of Data](http://thesecretlivesofdata.com/raft/)
  * [Raft Consensus Algorithm](https://raft.github.io/)
  * [Raft Distributed Consensus Algorithm Visualization](http://kanaka.github.io/raft.js/)

### Gossip
* [Understanding Gossip](https://www.youtube.com/watch?v=FuP1Fvrv6ZQ&ab_channel=PlanetCassandra)
* [Visualization](https://rrmoelker.github.io/gossip-visualization/)
* [The Gossip Protocol - Inside Apache Cassandra](https://www.linkedin.com/pulse/gossip-protocol-inside-apache-cassandra-soham-saha/)
* [Implement Gossip protocol with code](https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49)
* [Multicast problem](https://tharunravuri.medium.com/multicast-problem-b1321c62233f)
* [SWIM protocol](https://www.brianstorti.com/swim/)
* [Gossip protocol data structure](https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49)
* [Coursera multicast problem](https://www.coursera.org/lecture/cloud-computing/1-1-multicast-problem-G75ld)
* [Coursera gossip protocol](https://www.coursera.org/lecture/cloud-computing/1-2-the-gossip-protocol-5AOex)
* [UIUC disemination protocols](https://www.coursera.org/lecture/cloud-computing/2-6-dissemination-and-suspicion-OQF73)

### Vector clock
* Vector clock: Published by Lesie Lamport in 1978. [Time, Clocks and the Ordering of Events in a Distributed System](https://www.microsoft.com/en-us/research/publication/time-clocks-ordering-events-distributed-system/)
* Clock synchronization: [UMass course](http://lass.cs.umass.edu/~shenoy/courses/spring05/lectures/Lec10.pdf)
* [Why vector clocks are easy](https://riak.com/posts/technical/why-vector-clocks-are-easy/)
* [Why vector clocks are hard](https://riak.com/posts/technical/why-vector-clocks-are-hard/)

### Real life
* [Uber RingPop Membership Protocol](https://eng.uber.com/ringpop-open-source-nodejs-library/)
* [Serf with Gossip-based membership](https://www.serf.io/)