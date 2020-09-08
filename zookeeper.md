
<!-- MarkdownTOC -->

- [Zookeeper](#zookeeper)
	- [Data model](#data-model)
	- [Operations](#operations)
	- [Characteristics of a node](#characteristics-of-a-node)
		- [Node attribute](#node-attribute)
	- [Watch mechanism](#watch-mechanism)
	- [Cluster](#cluster)
	- [ZAB algorithm](#zab-algorithm)
	- [Applications](#applications)
		- [Cluster management](#cluster-management)
			- [Requirements](#requirements)
			- [Flowchart](#flowchart)
		- [Distributed lock](#distributed-lock)
		- [Service discovery](#service-discovery)
			- [Flowchart](#flowchart-1)
			- [Definition](#definition)
			- [Functionality](#functionality)
			- [Limitations](#limitations)
				- [CP model](#cp-model)
				- [Scalability](#scalability)
				- [Storage](#storage)
				- [Service health check](#service-health-check)
				- [Nonfault tolerant native client](#nonfault-tolerant-native-client)
			- [Applicable scenarios](#applicable-scenarios)
			- [Algorithm](#algorithm)
			- [Design considerations:](#design-considerations)
			- [Implementation](#implementation)
			- [Pros and Cons](#pros-and-cons)
		- [etcd](#etcd)
			- [Operations](#operations-1)

<!-- /MarkdownTOC -->


# Zookeeper

## Data model
* Tree sheet

## Operations
* CRUD

## Characteristics of a node
* Ephemoral node: Get created an deleted together with session
* Persistent node: 
* Ephemoral sequential node: Used for distributed lock. Will be automatically deleted. 
* Persistent sequential node: 

### Node attribute
* using stat command, could list out all the attributes of the node. 
	* ephemeralOwner: emphemeral node if emphemeralOwner field is not empty. 
	* 

## Watch mechanism
* 

## Cluster


## ZAB algorithm

## Applications
### Cluster management
#### Requirements
* How many nodes are online.
* How many nodes are operating correctly.
* What are the resource (CPU, memory, disk) usage states for these online nodes.
* If resource usage exceed the threshold, receive an alert. 

#### Flowchart

```
                                     ┌─────────────────────────┐                                            
                                     │                         │                                            
                                     │    Monitoring Center    │                                            
                                     │                         │                                            
                                     └─────────────────────────┘                                            
                                                  ▲                                                         
                                                  │                                                         
                                               Step3.                                                       
                                         Watch mechanism for                                                
                                        directory file change                                               
                                                  │                                                         
                                                  │                                                         
                    ┌──────────────────────────────────────────────────────────┐                            
                    │                        Zookeeper                         │                            
                    │                                                          │                            
                    │    ┌──────────────────────────────────────────────┐      │                            
                    │    │                   Root dir                   │      │                            
                    │    │     ---server001: json blob for resource     │      │                            
                    │    │     ---server002: json blob for resource     │      │                            
                    │    │     ---server003: json blob for resource     │      │                            
                    │    │                     ...                      │      │                            
                    │    │     ---server00N: json blob for resource     │      │                            
                    │    │                                              │      │                            
                    │    └──────────────────────────────────────────────┘      │                            
                    │                                                          │                            
                    └──────────────────────────────────────────────────────────┘                            
                                                  ▲                                                         
                                                  │                                                         
                                                  │                                                         
                      step2.                      │                                step1.                   
          ┌─report resource health via ─┬─────────┴─────────────────┬────────create an ephemeral──┐         
          │        heartbeat msg        │                           │          node upon start    │         
          │                             │                           │                             │         
          │                             │                           │                             │         
          │                             │                           │                             │         
┌──────────────────┐          ┌──────────────────┐        ┌──────────────────┐          ┌──────────────────┐
│  Server node 1   │          │  Server node 2   │        │ Server node ...  │          │  Server node N   │
│                  │          │                  │        │                  │          │                  │
│                  │          │                  │        │                  │          │                  │
│  ┌────────────┐  │          │  ┌────────────┐  │        │  ┌────────────┐  │          │  ┌────────────┐  │
│  │   Agent    │  │          │  │   Agent    │  │        │  │   Agent    │  │          │  │   Agent    │  │
│  │            │  │          │  │            │  │        │  │            │  │          │  │            │  │
│  └────────────┘  │          │  └────────────┘  │        │  └────────────┘  │          │  └────────────┘  │
│                  │          │                  │        │                  │          │                  │
└──────────────────┘          └──────────────────┘        └──────────────────┘          └──────────────────┘
```

### Distributed lock
* Zookeeper as distributed lock: https://ke.qq.com/webcourse/index.html#cid=1466958&term_id=101565022&taid=6908076939960910
* How will the node be deleted:
	- Client deletes the node proactively
		+ How will the previous node get changed?
			1. Watch mechanism get -w /gupao. 
			2. 
	- Too many notifications:
		+ Each node only needs to monitor the previous node
	- Temporary node


### Service discovery
* It is becoming popular because it is the default registration center for Dubbo framework. 

#### Flowchart
```
                                       ┌────────────────┐                             
                                       │Service consumer│                             
                                       │                │                             
                                       └────────────────┘                             
                                           │       ▲                                  
                                           │       │                                  
              Step 3. Create a node        │       │                                  
                "consumer1" under          │       │                                  
          /service/consumer directory.     │       │   Step 4. ZooKeeper notifies the 
                                           │       │       client that a new node     
            And watch all nodes under      │       │   "providerN" is added under the 
                /service/provider          │       │   /service/provider registration 
                                           │       │                                  
                                           │       │                                  
                                           ▼       │                                  
                          ┌───────────────────────────────────────┐                   
                          │                                       │                   
                          │               Zookeeper               │                   
                          │                                       │                   
                          │           /service/provider           │                   
                          │      /service/provider/provider1      │                   
   Step 1. Create a root  │                  ...                  │                   
       service path       │      /service/provider/providerN      │                   
                          │                                       │                   
     /service/provider    │                                       │                   
     /service/consumer    │           /service/consumer           │                   
                          │      /service/consumer/consumer1      │                   
                          │                  ...                  │                   
                          │      /service/consumer/consumerN      │                   
                          │                                       │                   
                          └───────────────────────────────────────┘                   
                                               ▲                                      
                                               │                                      
                                               │                                      
                                               │                                      
                                               │  Step 2. Create a node under         
                                               │       service provider               
                                               │                                      
                                               │  /service/provider/provider1         
                                               │                                      
                                               │                                      
                                               │                                      
                                               │                                      
                                      ┌────────────────┐                              
                                      │Service provider│                              
                                      │                │                              
                                      └────────────────┘                              
```

#### Definition
* Apache Zookeeper is an effort to develop and maintain an open-source server which enables highly reliable distributed coordination.
* Zookeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services. 
* Zookeeper is an open source implementation of Google Chubby.
* In essence, it is a file system with notification capabilities. 

#### Functionality
* Leader election: Two nodes watch the same node. 
* Configuration management
* Distributed lock
* Service registration
* Service discovery

#### Limitations
##### CP model
* Zookeeper is in essence a CP model, not an AP model. 
	* Service discovery is an AP scenario, not a CP scenario. For example, when new nodes come online, it is fine if there is a delay in discovering them. 
	* [Why you shouldn't use Zookeeper for service discovery](https://medium.com/knerd/eureka-why-you-shouldnt-use-zookeeper-for-service-discovery-4932c5c7e764)
* Example
	* Setup: 
		- There are five nodes deployed in three clusters
		- ZK1/ZK2 in Cluster 1, ZK3/ZK4 in Cluster 2, ZK5 in Cluster 3. 
		- Suddenly there is a network partition between Cluster1/2 and Cluster3. 
		- Within each cluster, there is an application cluster. Application service A is calling service B. 
	* If using CP model
		- ZK5 is not readable or writable. All clients connected to Cluster3 will fail. 
		- Then Service B cannot be scaled up. 
		- Then Service A cannot be rebooted.
	* Should use AP model

##### Scalability
* All writes will come to master node, then it will be replicated to the majority of slave nodes synchronously (two phase commit is used for replication). 
	- Possible solution: Split Zookeeper cluster according to the business functionalities. Different business units will not talk to each other. 
	- Limitation: Due to the reorg and evolution, different business units will need to talk to each other. 

##### Storage
* Since Zookeeper is based on ZAB protocol. ZAB will maintain a commit log on each node which records every write request. On a regular basis the in-memory records will be dumped to disk. This means that the entire history of write change will be recorded. 
* From the perspective of registration center, it does not need to access history of node changes
	- It only needs to access information such as epoch number, weight of different nodes

##### Service health check
* Zookeeper is using Keep-Alive heart-beat message functionality to detect the liveness of the node. 
	- When working thread within business logic unit dies, the Zookeeper will not be informed. 
* Ideal solution:
	- The business unit should send a health info to an Zookeeper healthcheck API. 

##### Nonfault tolerant native client
* Native Zookeeper client does not have resiliency built-in. It won't fall back to client snapshot/cache when Zookeeper registration center is down. 

#### Applicable scenarios
* It is the king of coordination for big data. These scenarios don't require high concurrency support. 
	- e.g. kafka will only use Zookeeper during leader election scenarios. 
	- e.g. Hadoop will need Zookeeper for map-reduce coordination scenarios.


#### Algorithm
* Consistency algorithm: ZAB algorithm
* To build the lock, we'll create a persistent znode that will serve as the parent. Clients wishing to obtain the lock will create sequential, ephemeral child znodes under the parent znode. The lock is owned by the client process whose child znode has the lowest sequence number. In Figure 2, there are three children of the lock-node and child-1 owns the lock at this point in time, since it has the lowest sequence number. After child-1 is removed, the lock is relinquished and then the client who owns child-2 owns the lock, and so on.
* The algorithm for clients to determine if they own the lock is straightforward, on the surface anyway. A client creates a new sequential ephemeral znode under the parent lock znode. The client then gets the children of the lock node and sets a watch on the lock node. If the child znode that the client created has the lowest sequence number, then the lock is acquired, and it can perform whatever actions are necessary with the resource that the lock is protecting. If the child znode it created does not have the lowest sequence number, then wait for the watch to trigger a watch event, then perform the same logic of getting the children, setting a watch, and checking for lock acquisition via the lowest sequence number. The client continues this process until the lock is acquired.
* Reference: https://nofluffjuststuff.com/blog/scott_leberknight/2013/07/distributed_coordination_with_zookeeper_part_5_building_a_distributed_lock

#### Design considerations:
* How would the client know that it successfully created the child znode if there is a partial failure (e.g. due to connection loss) during znode creation
	- The solution is to embed the client ZooKeeper session IDs in the child znode names, for example child-<sessionId>-; a failed-over client that retains the same session (and thus session ID) can easily determine if the child znode was created by looking for its session ID amongst the child znodes.
* How to avoid herd effect? 
	- In our earlier algorithm, every client sets a watch on the parent lock znode. But this has the potential to create a "herd effect" - if every client is watching the parent znode, then every client is notified when any changes are made to the children, regardless of whether a client would be able to own the lock. If there are a small number of clients this probably doesn't matter, but if there are a large number it has the potential for a spike in network traffic. For example, the client owning child-9 need only watch the child immediately preceding it, which is most likely child-8 but could be an earlier child if the 8th child znode somehow died. Then, notifications are sent only to the client that can actually take ownership of the lock.

#### Implementation
* https://time.geekbang.org/course/detail/100034201-119499

#### Pros and Cons
* Reliable
* Need to create ephemeral nodes which are not as efficient

### etcd
#### Operations
1. business logic layer apply for lock by providing (key, ttl)
2. etcd will generate uuid, and write (key, uuid, ttl) into etcd
3. etcd will check whether the key already exist. If no, then write it inside. 
4. After getting the lock, the heartbeat thread starts and heartbeat duration is ttl/3. It will compare and swap uuid to refresh lock

```
// acquire lock
curl http://127.0.0.1:2379/v2/keys/foo -XPUT -d value=bar -d ttl=5 prevExist=false

// renew lock based on CAS
curl http://127.0.0.1；2379/v2/keys/foo?prevValue=prev_uuid -XPUT -d ttl=5 -d refresh=true -d prevExist=true

// delete lock
curl http://10.10.0.21:2379/v2/keys/foo?prevValue=prev_uuid -XDELETE
```