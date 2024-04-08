- [DNS based implementation](#dns-based-implementation)
  - [Put service providers under a domain](#put-service-providers-under-a-domain)
  - [DNS service points to a load balancer address](#dns-service-points-to-a-load-balancer-address)
  - [Ali DNS implementation](#ali-dns-implementation)
- [Proxy based](#proxy-based)
  - [In-App Registration](#in-app-registration)
  - [Side car](#side-car)
  - [Consul implementation](#consul-implementation)
- [Zookeeper based implementation](#zookeeper-based-implementation)
  - [Consensus](#consensus)
- [Message bus based registration](#message-bus-based-registration)

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

## Consensus
* Take the example of Zookeeper
  1. Upon Zookeeper start, a leader will be elected according to Paxos protocol.
  2. Leader will be responsible for update operations according to ZAB protocol.
  3. An update operation is considered successful only if majority servers have finished update.

![](../.gitbook/assets/registerCenter\_zookeeperCluster.png)



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
