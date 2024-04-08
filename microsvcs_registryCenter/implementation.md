- [Zookeeper based implementation](#zookeeper-based-implementation)
  - [Characteristics](#characteristics)
  - [Limitations](#limitations)
    - [CP model](#cp-model)
    - [Scalability](#scalability)
    - [Storage](#storage)
  - [Industrial usage](#industrial-usage)
    - [Pros and Cons](#pros-and-cons)
- [DNS based implementation](#dns-based-implementation)
  - [Put service providers under a domain](#put-service-providers-under-a-domain)
  - [DNS service points to a load balancer address](#dns-service-points-to-a-load-balancer-address)
  - [Ali DNS implementation](#ali-dns-implementation)
- [Proxy based](#proxy-based)
  - [In-App Registration](#in-app-registration)
  - [Side car](#side-car)
  - [Consul implementation](#consul-implementation)
- [Message bus based registration](#message-bus-based-registration)

# Zookeeper based implementation
* It is becoming popular because it is the default registration center for Dubbo framework.
* Idea: Use Zookeeper ephemeral node to work as service registry and watch mechanism for notifications.
* Cons:
  * When there are too many directories or too many clients connecting to Zookeeper, the performance will have a natural degradation because Zookeeper enforces strong consistency.

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

## Characteristics

* Zookeeper keeps data in memory as a tree structure.

## Limitations

### CP model

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

### Scalability

* All writes will come to master node, then it will be replicated to the majority of slave nodes synchronously \(two phase commit is used for replication\). 
  * Possible solution: Split Zookeeper cluster according to the business functionalities. Different business units will not talk to each other. 
  * Limitation: Due to the reorg and evolution, different business units will need to talk to each other. 

### Storage

* Since Zookeeper is based on ZAB protocol. ZAB will maintain a commit log on each node which records every write request. On a regular basis the in-memory records will be dumped to disk. This means that the entire history of write change will be recorded. 
* From the perspective of registration center, it does not need to access history of node changes
  * It only needs to access information such as epoch number, weight of different nodes

## Industrial usage

* HBase: Use Zookeeper for leader election
* Solr: Use Zookeeper for cluster management, configuration management, leader election
* Dubbo: Service discovery [http://dubbo.apache.org/en-us/docs/user/references/registry/zookeeper.html](http://dubbo.apache.org/en-us/docs/user/references/registry/zookeeper.html)
* Mycat: Cluster management, configuration management
* Sharding-sphere: Cluster management, configuration management

### Pros and Cons

* Reliable
* Need to create ephemeral nodes which are not as efficient

# DNS based implementation

* Benefits: Low intrusion to the business logic when compared with SDK-based solution.

## Put service providers under a domain

* Idea: Put all service providers under a domain.
* Cons:
  * If an IP address goes offline, then the service provider could not easily remove the node because DNS has many layers of cache.
  * If scaling up, then newly scaled nodes will not receive enough traffic.

```
┌───────────┐       ┌───────────┐      ┌───────────────┐       ┌───────────┐       ┌─────────────┐      ┌───────────┐
│user browse│       │           │      │Local DNS cache│       │ local DNS │       │Regional DNS │      │ authority │
│ a domain  ├──────▶│ JVM cache │─────▶│  (Host file)  │──────▶│  server   │──────▶│(with cache) │─────▶│    DNS    │
│           │       │           │      │               │       │           │       │             │      │           │
└───────────┘       └───────────┘      └───────────────┘       └───────────┘       └─────────────┘      └───────────┘
```

## DNS service points to a load balancer address

* Idea: Consumers connect to the virtual ip address of a load balancer, not DNS servers.
* Cons:
  * All traffic needs to go through an additional hop, causing performance degradation.
  * Usually for load balancers, if you want to add or remove nodes, it needs to be done manually.
  * When it comes to service governance, usually a more flexible load balancing algorithm will be needed.

```
                                           ┌─────────────────────────┐                                              
                                           │                         │                                              
             ┌──────Discover───────────────│           DNS           │                                              
             │                             │                         │                                              
             │                             └─────────────────────────┘                                              
             │                                                                                                      
             │                                                                                                      
             │                                                                                                      
             ▼                                                                                                      
┌─────────────────────────┐                ┌─────────────────────────┐                   ┌─────────────────────────┐
│                         │                │                         │       Load        │                         │
│    Service consumer     │ ──Invoke──────▶│      Load balancer      │─────balancing ───▶│    Service Provider     │
│                         │                │                         │    and revoke     │                         │
└─────────────────────────┘                └─────────────────────────┘                   └─────────────────────────┘
```

## Ali DNS implementation

* Independent DNS server
  * Pros:
    * Centralized DNS server. Easy for maintenance.
  * Cons:
    * High requirement on DNS server performance.
    * SPOF

![](../.gitbook/assets/registryCenter\_independentDNS.png)

* Filter based on DNS server: Embed a DNS server in local server. All DNS queries will first be parsed by the local DNS.
  * Pros:
    * Avoid the SPOF
  * Cons:
    * Higher maintenance cost because DNS is embedded within each service.

![](../.gitbook/assets/registryCenter\_filterDNS.png)

* Ali's implementation derive from filter based DNS server.
  1. Service A query service B's IP address
  2. DNS-F intercept service A's request, and see whether VIPServer has the data.
  3. Otherwise, DNS-F will query the actual DNS server.

![](../.gitbook/assets/registryCenter\_DnsF.png)

* Reference in Chinese: [https://developer.aliyun.com/article/598792](https://developer.aliyun.com/article/598792)

# Proxy based

## In-App Registration

* Def: Each app embed a proxy (e.g. Alibaba Dubbo / Netflix karyon / Twitter Finagle)
* Pros:
  * No single point of failure
  * High performant
* Cons:
  * Language compatibility. Requires multiple language support.
* Use cases:
  * In mid/large sized company where language stack is consistent and unified.

![Comparison](../.gitbook/assets/discoveryCenter\_clientEmbed.png)

## Side car

* Def: Run two separate applications on the same machine. One for service registration, and the other for service discovery.

![Comparison](../.gitbook/assets/discoveryCenter\_clientProcess.png)

## Consul implementation

* Consul: Registry center's server end, will store registration information and provide registration and discovery service.
* Registrator: An open-source third party service management project. It will listen to services' docker instances and provide registration and unregistration.
* Consul template: Regularly pull information from registry center and update load balancer configuration (such as Nginx stream module). Then service consumers could get latest info by querying Nginx.

![](../.gitbook/assets/registryCenter\_consul.png)

# Message bus based registration

* Idea:
  * When service providers come online, consumers could tolerate some latency in discovering these service providers. A strong consistency model such as what Zookeeper provides is not needed.
  * Could have multiple registry center connectecd by message bus. Registry center could have a full cached copy of service providers and multiple registry centers could sync the data via a message bus.
* How to solve the above idea's update latency problem
  * Client retry when the service provider is no longer there.

```
  ┌─────────────────────────────┐                                                          
  │      Service provider       │                                                          
  │                             │                                                          
  └─────────────────────────────┘                                                          
                 │                                                                         
                 │                                                                         
          Step 1. Create a                                                                 
            registration                                                                   
               message                                                                     
                 │                                     ┌──────────────────────────────────┐
                 │                                     │           Message bus            │
                 ▼                                     │                                  │
  ┌────────────────────────────┐    Step 3. Publish    │  ┌───────────────────────────┐   │
  │                            │     registration      │  │Service: Checkout          │   │
  │                            │  ────message to ───▶  │  │Address: 192.168.1.9:9080  │   │
  │                            │      message bus      │  │Version: 2019113589        │   │
  │                            │                       │  └───────────────────────────┘   │
  │                            │                       │                                  │
  │                            │                       │  ┌───────────────────────────┐   │
  │                            │                       │  │Service: addToCart         │   │
  │                            │                       │  │Address: 192.168.1.2:9080  │   │
  │                            │                       │  │Version: 2019103243        │   │
  │    Registration center     │                       │  └───────────────────────────┘   │
  │                            │                       │                                  │
  │                            │        Step 4.        │  ┌───────────────────────────┐   │
  │                            │   Pull/receive push   │  │          ......           │   │
  │                            │◀──notification from── │  │                           │   │
  │                            │      message bus      │  │                           │   │
  │                            │                       │  └───────────────────────────┘   │
  │                            │                       │                                  │
  │                            │                       │  ┌───────────────────────────┐   │
  │                            │                       │  │Service: Checkout          │   │
  └────────────────────────────┘                       │  │Address: 192.168.1.9:9080  │   │
         ▲                │                            │  │Version: 2019113590        │   │
         │                │                            │  └───────────────────────────┘   │
         │                │                            └──────────────────────────────────┘
Step 2. Consumer          │                                                                
  subscribe to            │                                                                
  registration            Step 5. Receive                                                  
 center change       notification for service                                              
         │                │provider list                                                   
         │                │                                                                
         │                ▼                                                                

  ┌─────────────────────────────┐                                                          
  │      Service consumer       │                                                          
  │                             │                                                          
  └─────────────────────────────┘
```
