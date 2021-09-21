# RegistryCenter\_Zookeeper

* [Zookeeper](registrycenter_zookeeper.md#zookeeper)
  * [Characteristics](registrycenter_zookeeper.md#characteristics)
  * [Limitations](registrycenter_zookeeper.md#limitations)
    * [CP model](registrycenter_zookeeper.md#cp-model)
    * [Scalability](registrycenter_zookeeper.md#scalability)
    * [Storage](registrycenter_zookeeper.md#storage)
  * [Industrial usage](registrycenter_zookeeper.md#industrial-usage)
  * [Zookeeper vs Consul vs Etcd vs Doozer](registrycenter_zookeeper.md#zookeeper-vs-consul-vs-etcd-vs-doozer)
  * [Concepts](registrycenter_zookeeper.md#concepts)
    * [Session](registrycenter_zookeeper.md#session)
    * [Data model](registrycenter_zookeeper.md#data-model)
      * [Znode](registrycenter_zookeeper.md#znode)
    * [ACL](registrycenter_zookeeper.md#acl)
    * [Tracing time](registrycenter_zookeeper.md#tracing-time)
    * [Watch mechanism](registrycenter_zookeeper.md#watch-mechanism)
    * [Design considerations](registrycenter_zookeeper.md#design-considerations)
    * [Pros and Cons](registrycenter_zookeeper.md#pros-and-cons)
  * [Cluster](registrycenter_zookeeper.md#cluster)
    * [Configuration](registrycenter_zookeeper.md#configuration)
    * [Monitor](registrycenter_zookeeper.md#monitor)
    * [Consistency - ZAB](registrycenter_zookeeper.md#consistency---zab)
      * [Corruption recovery](registrycenter_zookeeper.md#corruption-recovery)
      * [Data synchronization](registrycenter_zookeeper.md#data-synchronization)
      * [Leader election](registrycenter_zookeeper.md#leader-election)
  * [Applications](registrycenter_zookeeper.md#applications)
    * [Configuration management center](registrycenter_zookeeper.md#configuration-management-center)
    * [Master election](registrycenter_zookeeper.md#master-election)
    * [Cluster management](registrycenter_zookeeper.md#cluster-management)
      * [Requirements](registrycenter_zookeeper.md#requirements)
      * [Flowchart](registrycenter_zookeeper.md#flowchart)
    * [Service discovery](registrycenter_zookeeper.md#service-discovery)
      * [Dubbo's service registration](registrycenter_zookeeper.md#dubbos-service-registration)
    * [Distributed job](registrycenter_zookeeper.md#distributed-job)
      * [Requirements](registrycenter_zookeeper.md#requirements-1)
      * [Flowchart](registrycenter_zookeeper.md#flowchart-1)
    * [Distributed lock](registrycenter_zookeeper.md#distributed-lock)

## Zookeeper

### Characteristics

* Zookeeper keeps data in memory as a tree structure.

### Limitations

#### CP model

* Zookeeper is in essence a CP model, not an AP model. 
  * Service discovery is an AP scenario, not a CP scenario. For example, when new nodes come online, it is fine if there is a delay in discovering them. 
  * [Why you shouldn't use Zookeeper for service discovery](https://medium.com/knerd/eureka-why-you-shouldnt-use-zookeeper-for-service-discovery-4932c5c7e764)
* Example
  * Setup: 
    * There are five nodes deployed in three clusters
    * ZK1/ZK2 in Cluster 1, ZK3/ZK4 in Cluster 2, ZK5 in Cluster 3. 
    * Suddenly there is a network partition between Cluster1/2 and Cluster3. 
    * Within each cluster, there is an application cluster. Application service A is calling service B. 
  * If using CP model
    * ZK5 is not readable or writable. All clients connected to Cluster3 will fail. 
    * Then Service B cannot be scaled up. 
    * Then Service A cannot be rebooted.
  * Should use AP model

#### Scalability

* All writes will come to master node, then it will be replicated to the majority of slave nodes synchronously \(two phase commit is used for replication\). 
  * Possible solution: Split Zookeeper cluster according to the business functionalities. Different business units will not talk to each other. 
  * Limitation: Due to the reorg and evolution, different business units will need to talk to each other. 

#### Storage

* Since Zookeeper is based on ZAB protocol. ZAB will maintain a commit log on each node which records every write request. On a regular basis the in-memory records will be dumped to disk. This means that the entire history of write change will be recorded. 
* From the perspective of registration center, it does not need to access history of node changes
  * It only needs to access information such as epoch number, weight of different nodes

### Industrial usage

* HBase: Use Zookeeper for leader election
* Solr: Use Zookeeper for cluster management, configuration management, leader election
* Dubbo: Service discovery [http://dubbo.apache.org/en-us/docs/user/references/registry/zookeeper.html](http://dubbo.apache.org/en-us/docs/user/references/registry/zookeeper.html)
* Mycat: Cluster management, configuration management
* Sharding-sphere: Cluster management, configuration management

### Zookeeper vs Consul vs Etcd vs Doozer

* **Concepts**

  **Session**

  **Data model**

  **Znode**

  **ACL**

  **Tracing time**

  **Watch mechanism**

#### Design considerations

* How would the client know that it successfully created the child znode if there is a partial failure \(e.g. due to connection loss\) during znode creation
  * The solution is to embed the client ZooKeeper session IDs in the child znode names, for example child--; a failed-over client that retains the same session \(and thus session ID\) can easily determine if the child znode was created by looking for its session ID amongst the child znodes.
* How to avoid herd effect? 
  * In our earlier algorithm, every client sets a watch on the parent lock znode. But this has the potential to create a "herd effect" - if every client is watching the parent znode, then every client is notified when any changes are made to the children, regardless of whether a client would be able to own the lock. If there are a small number of clients this probably doesn't matter, but if there are a large number it has the potential for a spike in network traffic. For example, the client owning child-9 need only watch the child immediately preceding it, which is most likely child-8 but could be an earlier child if the 8th child znode somehow died. Then, notifications are sent only to the client that can actually take ownership of the lock.

#### Pros and Cons

* Reliable
* Need to create ephemeral nodes which are not as efficient

### Cluster

#### Configuration

#### Monitor

#### Consistency - ZAB

**Corruption recovery**

**Data synchronization**

**Leader election**

### Applications

#### Configuration management center

#### Master election

#### Cluster management

**Requirements**

* How many nodes are online.
* How many nodes are operating correctly.
* What are the resource \(CPU, memory, disk\) usage states for these online nodes.
* If resource usage exceed the threshold, receive an alert. 

**Flowchart**

```text
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

#### Service discovery

**Dubbo's service registration**

![Comparison](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/zookeeper-inDubbo-ServiceDiscovery.png)

#### Distributed job

**Requirements**

* Only when the node is master node, enable the deployment. 

**Flowchart**

```text
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

#### Distributed lock

* Please see the distributed lock section in [Zookeeper](https://github.com/DreamOfTheRedChamber/system-design/blob/master/distributedLock.md#zookeeper)

