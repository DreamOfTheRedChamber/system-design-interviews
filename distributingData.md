<!-- MarkdownTOC -->

- [Replication](#replication)
	- [Use cases](#use-cases)
		- [Use case 1: Increase availability](#use-case-1-increase-availability)
		- [Use case 2: Increase read throughput](#use-case-2-increase-read-throughput)
		- [Use case 3: Reduce access latency](#use-case-3-reduce-access-latency)
	- [When not to use - Scale writes](#when-not-to-use---scale-writes)
	- [Replication mode](#replication-mode)
		- [Synchronous](#synchronous)
		- [Asynchronous](#asynchronous)
		- [Semi-synchronous](#semi-synchronous)
	- [Problems with replication lag](#problems-with-replication-lag)
		- [Read your own writes](#read-your-own-writes)
		- [Monotonic reads](#monotonic-reads)
		- [Consistent prefix reads](#consistent-prefix-reads)
	- [Replication Topology](#replication-topology)
		- [Single leader replication](#single-leader-replication)
		- [Multi-leader replication](#multi-leader-replication)
			- [Use cases](#use-cases-1)
			- [Handling write conflicts](#handling-write-conflicts)
		- [Leaderless replication](#leaderless-replication)
		- [Master-slave vs peer-to-peer](#master-slave-vs-peer-to-peer)
		- [Master-slave replication](#master-slave-replication)
			- [Number of slaves](#number-of-slaves)
		- [Peer-to-peer replication](#peer-to-peer-replication)
			- [Planning for failures](#planning-for-failures)

<!-- /MarkdownTOC -->

## Replication 
### Use cases
#### Use case 1: Increase availability
* Replica could function as hot standby, which could take over immediately if the original component fails. 

#### Use case 2: Increase read throughput
* Increase the number of machine which could serve read queries

#### Use case 3: Reduce access latency
* Keep data geographically close to your users

### When not to use - Scale writes
* No matter what topology you use, all of your writes need to go through a single machine.
	- Although a dual master architecture appears to double the capacity for handling writes (because there are two masters), it actually doesn't. Writes are just as expensive as before because each statement has to be executed twice: once when it is received from the client and once when it is received from the other master. All the writes done by the A clients, as well as B clients, are replicated and get executed twice, which leaves you in no better position than before. 

### Replication mode
#### Synchronous
* Def: The master and slaves are always in sync and a transaction is not allowed to be committed on the master unless the slaves agrees to commit it as well (i.e. synchronous replication makes the master wait for all the slaves to keep up with the writes.)
* Pros: Consistency. The follower is guaranteed to have an up-to-date copy of the data that is consistent with the leader. If the leader suddenly fails, we could be sure that the data is still on the follower. 
* Cons: Performance. If the synchronous follower does not respond, then the leader must block all other writes and white until the synchronous follower is available again. 

#### Asynchronous
* Def: The master does not wait for the slaves to apply the changes, but instead just dispatches each change request to the slaves and assume they will catch up eventually and replicate all the changes. 
* Pros: Performance. the transaction is reported as committed immediately, without waiting for any acknowledgement from the slave. 
* Cons: Consistency. The follower and the leader will not be in sync. 

#### Semi-synchronous
* Only a subset of followers are configured to be synchrnous and the others are asynchronous. This setting is commonly seen in cross data center replications. The replicas within a single data center are synchronous and the replicas in other data center are asynchronous. 

### Problems with replication lag
#### Read your own writes
* Scenario: A user makes a write, followed by a read from replica. 
* Read-after-write consistency to rescue. Several possible implementation scenarios: 
	1. When reading something that the user may have modified, read it from the leader; Otherwise, read it from a follower. This means you have some way of knowing whether something might have been modified, without actually querying it. For example, user profile informaiton on a social network website is normally only editable by the owner of the profile, not by anybody else. Thus, a simple rule is: Always read the user's own profile from the leader, and any other users' profile from a follower. 
	2. If most things in the application are potentially editable by the user, that approach won't be effective, as most of the things would have to be read from the leader. In that case, other criteria could be used. 
		- For example, you could track the time of the last update and, for one minute after the last update, make all reads from the leader. You could also monitor the replication lag on followers and prevent queries on any follower that is more than one minute behind the leader. 
		- The client could remember the timestamp of its most recent write - then the system can ensure that the replica serving any reads for that user reflects updates at least until that timestamp. If a replica is not sufficiently up to date, either teh read can be handled by another replica or the query could wait until the replica has caught up. 

* Scenario: The same user is accessing your service from multiple devices, for example a desktop web browser and a mobile app. The user enters information on one device and then views it on another device. 
* Cross-device read-after-write consistency to rescue. Several adaptation from read-after-write consistency:
	1. Approaches that require remembering the timestamp of the user's last update become more difficult, because the code running on one device doesn't know what updates have happended on the other device. This metadata will need to be centralized. 
	2. If your replicas are distributed across different data centers, there is no guarantee that connections from different devices will be routed to the same datacenter. (For example, if the user's desktop computer uses the home broadband connection and their mobile devices use the cellular network, the devices' network routes may be completely different.) If your approach requires reading from the leader, you may first need to route requests from all of a user's devices to the same datacenter. 

#### Monotonic reads
* Scenario: A user makes several reads from different replicas and it's possible for a user to see things moving backward in time.
* Monotonic reads consistency to rescue. It's a lesser guarantee than strong consistency, but a stronger guarantee than eventual consistency. 
	1. Each user always makes their reads from the same replica. For example, the replica can be chosen based on a hash of the user ID, rather than randomly. 

#### Consistent prefix reads
* Scenario: Mr Poon is asking a question and Mrs Cake is answering it. However, from the observer perspective it could be Mrs Cake is answering the question even before Mr Poon asks for it. The reason is that the things said by Mrs Cake go through a follower with little lag but the things said by Mr. Poons have a longer replication delay. 
* Consistent prefix reads to rescue: If a sequence of writes happens in a certain order, then anyone reading those writes will see them appear in the same order. 
	1. The reason for inconsistency is that different partitions operate independently, so there is no global ordering of writes: when a user reads from the database, they may see some parts of the database in an order state and some in a newer state. One solution is to make sure that any writes that are casually related to each other are written to the same partition. 

### Replication Topology
#### Single leader replication
* Pros: Fewer consistent issues
* Cons: All writes need to go through master

#### Multi-leader replication
##### Use cases
* Multi-datacenter operations
	- A leader in each data center. Within each datacenter, regular leader-follower replication is used; between datacenters, each datacenter's leader replicates its changes to the leaders in other datacenters. 
	- Cons: Need to resolve write conflicts
	- Pros: 

|   | performance  | tolerance of datacenter outages  | tolerance of network partitions  |
|---|---|---|---|
| single-leader config  | Every write must go over the internet to the datacenter with the leader. This could add significant latency to writes and might contravene the purpose of having multiple datacenters in the first place  |  A follower in another datacenter needs to be promoted as leader | single leader replication is sensitive to problems in inter-datacenter link which goes through public network |
| multi-leader config  | Every write can be processed in the local datacenter and is replicated asynchronously to the other datacenters. Thus the internet delay is hidden from users.  | Each datacenter could continue operating independently of the others | Could tolerate network interruption better because data is replicated asynchronously |

* Clients with offline operation: If you have app that needs to continue working while it is disconnected from the internet. 
* Collaborative editing: When one user edits a document, the changes are instantly applied to their local replica and asynchronously replicated to the server and any other users who are editing the same document. 

##### Handling write conflicts
* Conflict avoidance
	- Def: If the application ensure that all writes for a particular record go through the same leader, then conflicts cannot occur. 
	- Example case: In an application where the user could edit their own data, you can ensure that requests from a particular user are always routed to the same DC and use the leader in that DC for reading and writing; However, in cases when one DC fails or the user changes the location, this approach could break.  
* Converging toward a consistent state. There are several popular approaches: 
	- Gives each write a unique id (timestamp, random number, a hash of the key and value) and then pick the write with the highest ID as the winner and throw away the other writes. This approach suffers from data loss. 
	- Give each replica a unique ID, and let writes that originated at a higher numbered replica always take precedence over writes that originated at a lower numbered replica. This approach suffers from data loss. 
	- Somehow merge the value together. 
* Custom conflict resolution logic
	- Record the conflict in an explicit data structure that preserves all information, and write application code that resolves the conflict at some later time. 
* Question: [TODO] How does Amazon resolves the conflict within the shopping cart? 

#### Leaderless replication

#### Master-slave vs peer-to-peer 

|     Types    |    Strengths     |      Weakness       | 
| ------------ |:----------------:|:-------------------:|
| Master-slave | <ul><li>Helpful for scaling when you have a read-intensive dataset. Can scale horizontally to handle more read requests by adding more slave nodes and ensuring that all read requests are routed to the slaves.</li><li>Helpful for read resilience. Should the master fail, the slaves can still handle read requests.</li><li>Increase availability by reducing the time needed to replace the broken database. Having slaves as replicas of the master does speed up recovery after a failure of the master since a slave can be appointed a new master very quickly. </li></ul> | <ul><li>Not a good scheme for datasets with heavy write traffic, although offloading the read traffic will help a little bit with handling the write load. All of your writes need to go through a single machine </li><li>The failure of the master does eliminate the ability to handle writes until either the master is restored or a new master is appointed.</li><li>Inconsistency. Different clients reading different slaves will see different values because the changes haven't all propagated to the slaves. In the worst case, that can mean that a client cannot read a write it just made. </li></ul> | 
| p2p: Master-master |  <ul><li> Faster master failover. In case of master A failure, or anytime you need to perform long-lasting maintainence, your application can be quickly reconfigured to direct all writes to master B.</li><li>More transparent maintainance. Switch between groups with minimal downtime.</li></ul>| 	Not a viable scalability technique. <ul><li>Need to use auto-increment and UUID() in a specific way to make sure you never end up with the same sequence number being generated on both masters at the same time.</li><li>Data inconsistency. For example, updating the same row on both masters at the same time is a classic race condition leading to data becoming inconsistent between masters.</li><li>Both masters have to perform all the writes. Each of the master needs to execute every single write statement either coming from your application or via the replication. To make it worse, each master will need to perform additional I/O to write replicated statements into the relay log.</li><li> Both masters have the same data set size. Since both masters have the exact same data set, both of them will need more memory to hold ever-growing indexes and to keep enough of the data set in cache.</li></ul> | 
| p2p: Ring-based    | Chain three or more masters together to create a ring. | <ul><li> All masters need to execute all the write statements. Does not help scale writes.</li><li> Reduced availability and more difficult failure recovery: Ring topology makes it more difficult to replace servers and recover from failures correctly. </li><li>Increase the replication lag because each write needs to jump from master to master until it makes a full circle.</li></ul> | 


#### Master-slave replication 
* Responsibility: 
	- Master is reponsible for all data-modifying commands like updates, inserts, deletes or create table statements. The master server records all of these statements in a log file called a binlog, together with a timestamp, and a sequence number to each statement. Once a statement is written to a binlog, it can then be sent to slave servers. 
	- Slave is responsible for all read statements.
* Replication process: The master server writes commands to its own binlog, regardless if any slave servers are connected or not. The slave server knows where it left off and makes sure to get the right updates. This asynchronous process decouples the master from its slaves - you can always connect a new slave or disconnect slaves at any point in time without affecting the master.
	1. First the client connects to the master server and executes a data modification statement. The statement is executed and written to a binlog file. At this stage the master server returns a response to the client and continues processing other transactions. 
	2. At any point in time the slave server can connect to the master server and ask for an incremental update of the master' binlog file. In its request, the slave server provides the sequence number of the last command that it saw. 
	3. Since all of the commands stored in the binlog file are sorted by sequence number, the master server can quickly locate the right place and begin streaming the binlog file back to the slave server.
	4. The slave server then writes all of these statements to its own copy of the master's binlog file, called a relay log.
	5. Once a statement is written to the relay log, it is executed on the slave data set, and the offset of the most recently seen command is increased.  

##### Number of slaves 
* It is a common practice to have two or more slaves for each master server. Having more than one slave machine have the following benefits:
	- Distribute read-only statements among more servers, thus sharding the load among more servers
	- Use different slaves for different types of queries. E.g. Use one slave for regular application queries and another slave for slow, long-running reports.
	- Losing a slave is a nonevent, as slaves do not have any information that would not be available via the master or other slaves.

#### Peer-to-peer replication 
* Dual masters 
	- Two masters replicate each other to keep both current. This setup is very simple to use because it is symmetric. Failing over to the standby master does not require any reconfiguration of the main master, and failing back to the main master again when the standby master fails in turn is very easy.
		+ Active-active: Writes go to both servers, which then transfer changes to the other master.
		+ Active-passive: One of the masters handles writes while the other server, just keeps current with the active master
	- The most common use of active-active dumal masters setup is to have the servers geographically close to different sets of users - for example, in branch offices at different places in the world. The users can then work with local server, and the changes will be replicated over to the other master so that both masters are kept in sync.

* Circular replication 

##### Planning for failures
* Slave failures 
    - Because the slaves are used only for read quires, it is sufficient to inform the load balancer that the slave is missing. Then we can take the failing slave out of rotation. rebuild it and put it back. 

* Master failures 
	- Problems:
		+ All the slaves have stale data.
		+ Some queries may block if they are waiting for changes to arrive at the slave. Some queries may make it into the relay log of the slave and therefore will eventually be executed by the slave. No special consideration has to be taken on the behalf of these queries.
		+ For queries that are waiting for events that did not leave the master before it crashed, they are usually reported as failures so users should reissue the query.
	- Solutions: 
		+ If simply restart does not work
		+ First find out which of your slaves is most up to date. 
		+ Then reconfigure it to become a master. 
		+ Finally reconfigure all remaining slaves to replicate from the new master.

* Relay failures 
	- For servers acting as relay servers, the situation has to be handled specially. If they fail, the remaining slaves have to be redirected to use some other relay or the master itself. 

* Disaster recovery 
	- Disaster does not have to mean earthquakes or floods; it just means that something went very bad for the computer and it is not local to the machine that failed. Typical examples are lost power in the data center (not necessarily because the power was lost in the city; just losing power in the building is sufficient.) 
	- The nature of a disaster is that many things fail at once, making it impossible to handle redundancy by duplicating servers at a single data center. Instead, it is necessary to ensure data is kept safe at another geographic location, and it is quite common for companies to ensure high availability by having different components at different offices. 

