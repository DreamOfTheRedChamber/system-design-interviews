<!-- MarkdownTOC -->

- [Reading "Designing Data-Intensive Applications"](#reading-designing-data-intensive-applications)
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
				- [Responsibility](#responsibility)
				- [Replication process: The master server writes commands to its own binlog, regardless if any slave servers are connected or not. The slave server knows where it left off and makes sure to get the right updates. This asynchronous process decouples the master from its slaves - you can always connect a new slave or disconnect slaves at any point in time without affecting the master.](#replication-process-the-master-server-writes-commands-to-its-own-binlog-regardless-if-any-slave-servers-are-connected-or-not-the-slave-server-knows-where-it-left-off-and-makes-sure-to-get-the-right-updates-this-asynchronous-process-decouples-the-master-from-its-slaves---you-can-always-connect-a-new-slave-or-disconnect-slaves-at-any-point-in-time-without-affecting-the-master)
				- [Pros](#pros)
				- [Cons](#cons)
				- [Number of slaves](#number-of-slaves)
			- [Multi-leader replication](#multi-leader-replication)
				- [Use cases](#use-cases-1)
				- [Handling write conflicts](#handling-write-conflicts)
				- [Topology](#topology)
			- [Leaderless replication](#leaderless-replication)

<!-- /MarkdownTOC -->

# Reading "Designing Data-Intensive Applications"

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
##### Responsibility
- Master is reponsible for all data-modifying commands like updates, inserts, deletes or create table statements. The master server records all of these statements in a log file called a binlog, together with a timestamp, and a sequence number to each statement. Once a statement is written to a binlog, it can then be sent to slave servers. 
- Slave is responsible for all read statements.

##### Replication process: The master server writes commands to its own binlog, regardless if any slave servers are connected or not. The slave server knows where it left off and makes sure to get the right updates. This asynchronous process decouples the master from its slaves - you can always connect a new slave or disconnect slaves at any point in time without affecting the master.
1. First the client connects to the master server and executes a data modification statement. The statement is executed and written to a binlog file. At this stage the master server returns a response to the client and continues processing other transactions. 
2. At any point in time the slave server can connect to the master server and ask for an incremental update of the master' binlog file. In its request, the slave server provides the sequence number of the last command that it saw. 
3. Since all of the commands stored in the binlog file are sorted by sequence number, the master server can quickly locate the right place and begin streaming the binlog file back to the slave server.
4. The slave server then writes all of these statements to its own copy of the master's binlog file, called a relay log.
5. Once a statement is written to the relay log, it is executed on the slave data set, and the offset of the most recently seen command is increased.  

##### Pros
1. Increase read throughput. Can scale horizontally to handle more read requests by adding more slave nodes and ensuring that all read requests are routed to the slaves.
2. Increase read availability. Should the master fail, the slaves can still handle read requests.
3. Reduce read latency. 

##### Cons 
1. Not help write throughput / availability. 
2. Inconsistency between replicas.

##### Number of slaves 
* It is a common practice to have two or more slaves for each master server. Having more than one slave machine have the following benefits:
	- Distribute read-only statements among more servers, thus sharding the load among more servers
	- Use different slaves for different types of queries. E.g. Use one slave for regular application queries and another slave for slow, long-running reports.
	- Losing a slave is a nonevent, as slaves do not have any information that would not be available via the master or other slaves.

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

##### Topology
* Circular / star
	- Pros: 
		* Less network traffic.
	- Cons:	
		* A read needs to pass through several nodes before it reaches all replicas. If just one node fails, it can interrupt the flow of replication messages between other nodes, causing them to be unable to communicate until the node is fixed. 

* All-to-all topology
	- Pros:
		* Resilient to single node failure
	- Cons: 
		* Some network links may be faster than others, could lead to problems discussed in "consistent prefix read"

#### Leaderless replication


