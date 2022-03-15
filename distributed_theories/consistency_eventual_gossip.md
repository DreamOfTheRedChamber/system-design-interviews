- [Use cases](#use-cases)
- [Use examples](#use-examples)
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
- [References](#references)
  - [Real life](#real-life)

# Use cases

* Database replication
* Information dissemination
* Cluster membership
* Failure Detectors
* Overlay Networks
* Aggregations (e.g calculate average, sum, max)

# Use examples

* Riak uses a gossip protocol to share and communicate ring state and bucket properties around the cluster.
* In CASSANDRA nodes exchange information using a Gossip protocol about themselves and about the other nodes that they have gossiped about, so all nodes quickly learn about all other nodes in the cluster. \[9]
* Dynamo employs a gossip based distributed failure detection and membership protocol. It propagates membership changes and maintains an eventually consistent view of membership. Each node contacts a peer chosen at random every second and the two nodes efficiently reconcile their persisted membership change histories \[6].
* Dynamo gossip protocol is based on a scalable and efficient failure detector introduced by Gupta and Chandra in 2001 \[8]
*   Consul uses a Gossip protocol called SERF for two purposes \[10]:

     – discover new members and failures 

     – reliable and fast event broadcasts for events like leader election.
*   The Gossip protocol used in Consul is called SERF and is based on “SWIM:  Scalable Weakly-consistent Infection-style Process Group Membership Protocol”

    Amazon s3 uses a Gossip protocol to spread server state to the system \[8].

## Motivation - Multicast problem

* Multicast is a process of sending messages or exchanging information to a particular group or destination in a network. The requirements for multicast protocols are:
  * Fault tolerance — The nodes in the network can be faulty, they may crash and the packets may be dropped. Despite all these problems, the nodes should communicate a multicast message with each other in the network.
  * Scalable — The nodes in the network should be scalable even if the network is having thousands of nodes the overhead should be minimum at the root node.

### Centralized multicasting

* The one node will act as a sender and it sends the multicast messages to all nodes in the network.
* Cons:
  * The nodes can be faulty and after sending multicast messages to the few nodes using for loop there can be a situation where some of the nodes will receive the multicast messages and others not and also the overhead at the root node will be high if there are thousands of nodes in the network and the latency will be very high.

### Spanning tree based multicasting

* In tree-based multicast protocols:
  * Buiding a spanning tree among the processes of the multicast group.
  * Use spanning tree to spread multicast messages.
  * Use either ACK or NAK to repair the multicast not received.
* Examples: The IPmulticast, SRM, RMTP, TRAM, TMTP are examples of tree-based multicast protocols.
* Pros:
  * Complexity at O(logN), where N is the total number of nodes in the tree. 
* Cons:
  * If an intermediate node in the tree doesn't get the multicast message then the descendants of that node cannot get the multicast message. To overcome such problems the ACK and NAK messages are used to acknowledge the sender that the intended receiver doesn’t get the multicast message.

## States of a node

* Infective: A node with an update it is willing to share.
* Susceptible: A node that has not received the update yet (It is not infected).
* Removed: A node that has already received the update but it is not willing to share it.

### Removed state

* Removed is trickier than rest. It’s not easy to determine when a node should stop sharing the info/update. Ideally a node should stop sharing the update when all the nodes is linked with have the update. But that would mean that node would have to have knowledge of the status of the other nodes.

# Types

## Direct mail

* The one they had in place initially, each new update is immediately emailed from its entry site to all other sites but it presented several problems:
  * The sending node was a bottleneck O(n).
  * Each update was propagated to all the nodes, so each node had to know all the nodes in the system.
  * Messages could be discarded when a node was unresponsive for a long time or due to queue overflow.

## Anti-entropy (SI model)

* In Anti-entropy (SI model) a node that has an infective info is trying to share it in every cycle. A node not only shares the last update but the whole database, there are some techniques like checksum, recent update list, merkle trees, etc that allow a node to know if there are any differences between the two nodes before sending the database, it guarantees, eventual,  perfect dissemination.
* There is not termination, so It sends an unbounded number of messages.
* Cons:
  * Require per pair node data exchange, not suitable for environments with lots of nodes. 
  * Require knowledge of existing nodes, not suitable in dynamic changing environment. 

## Rumor mongering (SIR model)

* Rumor Mongering cycles can be more frequent than anti-entropy cycles because they require fewer resources, as the node only send the new update or a list of infective updates. Rumour mongering spreads updates fast with low traffic network.
* A rumor at some point is marked as removed and it’s not shared any more, because of that, the number of messages is bounded and there is some chance that the update will not reach all the sites, although this probability can be made arbitrarily small as we’ll see later. First let’s see how to decide when a node should be in state “removed”.

# Communication process

## Three way communication

* The gossip process runs every second for every node and exchange state messages with up to three other nodes in the cluster (This is for Cassandra). Since the whole process is decentralized, there is nothing or no one that coordinates each node to gossip. Each node independently will always select one to three peers to gossip with. It will always select a live peer (if any) in the cluster, it will probabilistically pick a seed node from the cluster or maybe it will probabilistically select an unavailable node.
* A three way communication similar to TCP handshake: 
  1. SYN: The node initiating the round of gossip sends the SYN message which contains a compendium of the nodes in the cluster. It contains tuples of the IP address of a node in the cluster, the generation and the heartbeat version of the node.
  2. ACK: The peer after receiving SYN message compares its own metadata information with the one sent by the initiator and produces a diff. ACK contains two kinds of data. One part consists of updated metadata information (AppStates) that the peer has but the initiator doesn't, and the other part consists of digest of nodes the initiator has that the peer doesn't.
  3. ACK2: The initiator receives the ACK from peer and updates its metadata from the AppStates and sends back ACK2 containing the metadata information the peer has requested for. The peer receives ACK2, updates its metadata and the round of gossip concludes.
* You could refer to [Confluence Gossip Architecture](https://cwiki.apache.org/confluence/display/CASSANDRA2/ArchitectureGossip) for example message format. 

![](.gitbook/assets/algorithm_consensus_gossip_format.png)

## Push and Pull

* PUSH: infective nodes are the ones sending/infecting susceptible nodes.
  * infective nodes are the ones infecting susceptible nodes.
  * very efficient where there are few updates.
* PULL: all nodes are actively pulling for updates. (A node can’t know in advance new updates, so it has to pull all continuously).
  * all nodes are actively pulling for updates.
  * very efficient where there are many updates.
* PUSH-PULL: It pushes when it has updates and it also pulls for new updates.
  * The node and selected node exchange their information.

# Code impl

* Please refer to [Implement Cassandra Gossip Protocol](https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49)

![](.gitbook/assets/algorithm_consensus_gossip_codeImpl.png)

# References
* [Understanding Gossip](https://www.youtube.com/watch?v=FuP1Fvrv6ZQ\&ab_channel=PlanetCassandra)
* [Visualization](https://rrmoelker.github.io/gossip-visualization/)
* [The Gossip Protocol - Inside Apache Cassandra](https://www.linkedin.com/pulse/gossip-protocol-inside-apache-cassandra-soham-saha/)
* [Implement Gossip protocol with code](https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49)
* [Multicast problem](https://tharunravuri.medium.com/multicast-problem-b1321c62233f)
* [SWIM protocol](https://www.brianstorti.com/swim/)
* [Gossip protocol data structure](https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49)
* [Coursera multicast problem](https://www.coursera.org/lecture/cloud-computing/1-1-multicast-problem-G75ld)
* [Coursera gossip protocol](https://www.coursera.org/lecture/cloud-computing/1-2-the-gossip-protocol-5AOex)
* [UIUC disemination protocols](https://www.coursera.org/lecture/cloud-computing/2-6-dissemination-and-suspicion-OQF73)

## Real life

* [Uber RingPop Membership Protocol](https://eng.uber.com/ringpop-open-source-nodejs-library/)
* [Serf with Gossip-based membership](https://www.serf.io)
