
<!-- MarkdownTOC -->

- [Zookeeper](#zookeeper)
  - [Characteristics](#characteristics)
  - [Limitations](#limitations)
    - [CP model](#cp-model)
    - [Scalability](#scalability)
    - [Storage](#storage)
  - [Industrial usage](#industrial-usage)
  - [Zookeeper vs Consul vs Etcd vs Doozer](#zookeeper-vs-consul-vs-etcd-vs-doozer)
  - [Concepts](#concepts)
    - [Session](#session)
    - [Data model](#data-model)
      - [Znode](#znode)
    - [ACL](#acl)
    - [Tracing time](#tracing-time)
    - [Watch mechanism](#watch-mechanism)
  - [Cluster](#cluster)
    - [Configuration](#configuration)
    - [Monitor](#monitor)
    - [Consistency - ZAB](#consistency---zab)
      - [Corruption recovery](#corruption-recovery)
      - [Data synchronization](#data-synchronization)
      - [Leader election](#leader-election)
  - [Applications](#applications)
    - [Configuration management center](#configuration-management-center)
    - [Master election](#master-election)
    - [Cluster management](#cluster-management)
      - [Requirements](#requirements)
      - [Flowchart](#flowchart)
    - [Service discovery](#service-discovery)
      - [Requirements](#requirements-1)
      - [Flowchart](#flowchart-1)
      - [Dubbo's service registration](#dubbos-service-registration)
    - [Distributed job](#distributed-job)
      - [Requirements](#requirements-2)
      - [Flowchart](#flowchart-2)
    - [Distributed lock](#distributed-lock)
- [Consensus protocol](#consensus-protocol)
  - [2PC](#2pc)
    - [Cons](#cons)
  - [3PC](#3pc)
    - [Cons](#cons-1)
  - [PAXOS](#paxos)
  - [Raft](#raft)
  - [ZAB algorithm](#zab-algorithm)
      - [Algorithm](#algorithm)
      - [Design considerations:](#design-considerations)
      - [Pros and Cons](#pros-and-cons)

<!-- /MarkdownTOC -->


# Zookeeper

## Characteristics
* Zookeeper keeps data in memory as a tree structure.

## Limitations
### CP model
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

### Scalability
* All writes will come to master node, then it will be replicated to the majority of slave nodes synchronously (two phase commit is used for replication). 
	- Possible solution: Split Zookeeper cluster according to the business functionalities. Different business units will not talk to each other. 
	- Limitation: Due to the reorg and evolution, different business units will need to talk to each other. 

### Storage
* Since Zookeeper is based on ZAB protocol. ZAB will maintain a commit log on each node which records every write request. On a regular basis the in-memory records will be dumped to disk. This means that the entire history of write change will be recorded. 
* From the perspective of registration center, it does not need to access history of node changes
	- It only needs to access information such as epoch number, weight of different nodes


## Industrial usage
* HBase: Use Zookeeper for leader election
* Solr: Use Zookeeper for cluster management, configuration management, leader election
* Dubbo: Service discovery http://dubbo.apache.org/en-us/docs/user/references/registry/zookeeper.html
* Mycat: Cluster management, configuration management
* Sharding-sphere: Cluster management, configuration management

## Zookeeper vs Consul vs Etcd vs Doozer
* 

## Concepts
### Session
### Data model
#### Znode
### ACL
### Tracing time
### Watch mechanism

## Cluster
### Configuration
### Monitor
### Consistency - ZAB
#### Corruption recovery
#### Data synchronization
#### Leader election

## Applications

### Configuration management center


### Master election


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




### Service discovery
* It is becoming popular because it is the default registration center for Dubbo framework. 

#### Requirements
* Register:
* Subscription: 
* Reliable: 
* Fault tolerant: In case when servers come down, registration center could detect such changes. 


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

#### Dubbo's service registration

![Comparison](./images/zookeeper-inDubbo-ServiceDiscovery.png)



### Distributed job
#### Requirements
* Only when the node is master node, enable the deployment. 

#### Flowchart

```
┌─────────────────┐               ┌─────────────────┐              ┌─────────────────┐
│     Node 1      │               │    Node ...     │              │     Node N      │
│                 │               │                 │              │                 │
│Job scheduled to │               │Job scheduled to │              │Job scheduled to │
│ run at certain  │               │ run at certain  │              │ run at certain  │
│      time       │               │      time       │              │      time       │
└─────────────────┘               └─────────────────┘              └─────────────────┘
         │                                                                            
         │                                                                            
         └───────1. create a new node──────────┐                                      
                2. Listen to root node         │                                      
                                               ▼                                      
                 ┌──────────────────────────────────────────────────────────┐         
                 │                        Zookeeper                         │         
                 │                                                          │         
                 │      ┌──────────────────────────────────────────────┐    │         
                 │      │                                              │    │         
                 │      │    Root dir (the smallest node is master)    │    │         
                 │      │                  ---node1:                   │    │         
                 │      │                  ---node2:                   │    │         
                 │      │                     ...                      │    │         
                 │      │                  ---nodeN:                   │    │         
                 │      │                                              │    │         
                 │      └──────────────────────────────────────────────┘    │         
                 │                                                          │         
                 └──────────────────────────────────────────────────────────┘         
                                                                                      
                                                                                      
                                                                                      
                         ┌───────────┐                                                
                         │           │                                                
                         │   start   │                                                
                         │           │                                                
                         └───────────┘                                                
                               │                                                      
                               │                                                      
                               │                                                      
                               ▼                                                      
                  ┌─────────────────────────┐                                         
                  │                         │                                         
                  │ create ephemeral nnode  │                                         
                  │                         │                                         
                  └─────────────────────────┘                                         
                               │                                                      
                               │                                                      
                               │                                                      
                               ▼                                                      
                    ┌─────────────────────┐             ┌────────────┐                
                    │ judge whether there │             │  election  │                
                    │  is a master node   │─────No─────▶│            │                
                    │                     │             └────────────┘                
                    └─────────────────────┘                                           
                               │                                                      
                               │                                                      
                              Yes                                                     
                               │                                                      
                               ▼                                                      
                    ┌─────────────────────┐                                           
                    │   watch the nodes   │                                           
                    │   change of Root    │                                           
                    │      directory      │                                           
                    └─────────────────────┘                                           
```


### Distributed lock

* Please see the distributed lock section in [Zookeeper](https://github.com/DreamOfTheRedChamber/system-design/blob/master/distributedLock.md#zookeeper)

# Consensus protocol
## 2PC
### Cons

## 3PC
### Cons

## PAXOS

## Raft

## ZAB algorithm
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


#### Pros and Cons
* Reliable
* Need to create ephemeral nodes which are not as efficient
