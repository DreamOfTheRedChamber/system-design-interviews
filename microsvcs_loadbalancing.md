# MicroSvcs_LoadBalancing

* [Basic Web Load Balancing](microsvcs_loadbalancing.md#basic-web-load-balancing)
  * [Use cases](microsvcs_loadbalancing.md#use-cases)
    * [Decoupling](microsvcs_loadbalancing.md#decoupling)
    * [Security](microsvcs_loadbalancing.md#security)
  * [Load balancing algorithms](microsvcs_loadbalancing.md#load-balancing-algorithms)
    * [Round-robin](microsvcs_loadbalancing.md#round-robin)
    * [Weighted round robin](microsvcs_loadbalancing.md#weighted-round-robin)
    * [Least load first (from server perspective)](microsvcs_loadbalancing.md#least-load-first-from-server-perspective)
    * [Best performance first (from client perspective)](microsvcs_loadbalancing.md#best-performance-first-from-client-perspective)
    * [Source hashing](microsvcs_loadbalancing.md#source-hashing)
  * [Categorization](microsvcs_loadbalancing.md#categorization)
    * [Http redirect based load balancer (rarely used)](microsvcs_loadbalancing.md#http-redirect-based-load-balancer-rarely-used)
    * [DNS based load balancer](microsvcs_loadbalancing.md#dns-based-load-balancer)
      * [HTTP-DNS based load balancer](microsvcs_loadbalancing.md#http-dns-based-load-balancer)
    * [Application layer (e.g. Nginx, HAProxy, Apache)](microsvcs_loadbalancing.md#application-layer-eg-nginx-haproxy-apache)
      * [Reverse proxy (e.g. Nginx)](microsvcs_loadbalancing.md#reverse-proxy-eg-nginx)
    * [Network/Transport/DataLink layer (e.g. Nginx Plus, F5/A10, LVS, HAProxy)](microsvcs_loadbalancing.md#networktransportdatalink-layer-eg-nginx-plus-f5a10-lvs-haproxy)
      * [Software based](microsvcs_loadbalancing.md#software-based)
        * [LVS](microsvcs_loadbalancing.md#lvs)
          * [VS/NAT mode](microsvcs_loadbalancing.md#vsnat-mode)
          * [VS/DR mode](microsvcs_loadbalancing.md#vsdr-mode)
          * [VS/TUN mode - TODO](microsvcs_loadbalancing.md#vstun-mode---todo)
      * [Hardware based](microsvcs_loadbalancing.md#hardware-based)
  * [Typical architecture and metrics](microsvcs_loadbalancing.md#typical-architecture-and-metrics)
    * [Multi layer](microsvcs_loadbalancing.md#multi-layer)
    * [Keepalived for high availability](microsvcs_loadbalancing.md#keepalived-for-high-availability)
* [Microservices Load Balancing](microsvcs_loadbalancing.md#microservices-load-balancing)
  * [Overall flowchart](microsvcs_loadbalancing.md#overall-flowchart)
  * [Gateway architecture](microsvcs_loadbalancing.md#gateway-architecture)
    * [Revolution history](microsvcs_loadbalancing.md#revolution-history)
      * [Initial architecture](microsvcs_loadbalancing.md#initial-architecture)
      * [BFF (Backend for frontEnd) layer](microsvcs_loadbalancing.md#bff-backend-for-frontend-layer)
      * [Gateway layer and Cluster BFF Layer](microsvcs_loadbalancing.md#gateway-layer-and-cluster-bff-layer)
      * [Clustered BFF and Gateway layer](microsvcs_loadbalancing.md#clustered-bff-and-gateway-layer)
    * [Gateway vs reverse proxy](microsvcs_loadbalancing.md#gateway-vs-reverse-proxy)
      * [Reverse Proxy (Nginx)](microsvcs_loadbalancing.md#reverse-proxy-nginx)
        * [Use cases](microsvcs_loadbalancing.md#use-cases-1)
    * [Gateway internals](microsvcs_loadbalancing.md#gateway-internals)
    * [Gateway comparison](microsvcs_loadbalancing.md#gateway-comparison)
  * [Service discovery](microsvcs_loadbalancing.md#service-discovery)
    * [Approach - Hardcode service provider addresses](microsvcs_loadbalancing.md#approach---hardcode-service-provider-addresses)
    * [Approach - Service registration center](microsvcs_loadbalancing.md#approach---service-registration-center)
  * [How to detect failure](microsvcs_loadbalancing.md#how-to-detect-failure)
    * [Detect failure](microsvcs_loadbalancing.md#detect-failure)
  * [How to gracefully shutdown](microsvcs_loadbalancing.md#how-to-gracefully-shutdown)
  * [How to gracefully start](microsvcs_loadbalancing.md#how-to-gracefully-start)
* [Future readings](microsvcs_loadbalancing.md#future-readings)


## Microservices Load Balancing

### Overall flowchart

```
                                                                   ┌──────────────────┐                 
                                                                   │      Client      │                 
                                                                   └──────────────────┘                 
                                                                             │                          
                                                                             ▼                          
                           Step 2.                                 ┌──────────────────┐                 
       ┌────────────────────Watch ─────────────────────────────────│     Gateway      │                 
       │                   changes                                 └──────────────────┘                 
       │                                                                     │                          
       │        ┌────────────┐                                               │                          
       │        │Control     │      Step 5. Command to restart               │                          
       │        │center      │◀──────────business logic 1────────────────────┤                          
       │        │service     │                                               │                          
       │        └────────────┘                                               │                          
       ▼               │                                        Step3.       ├──────────────────┐       
┌─────────────┐        │                        ┌─────────────Establish ─────┤                  │       
│   Service   │        │                        │                Long        │                  │       
│Registration │        │                        │                            │                  │       
└─────────────┘        │                        │                            │                  │       
       ▲               │                        │                            │                  │       
       │        Step 6: Restart                 │                            │                  │       
       │        business logic                  │                            ▼                  ▼       
       │            unit 1   ┌──────────────────┼────────────────┐  ┌─────────────────┐  ┌─────────────┐
       │               │     │                  ▼                │  │                 │  │             │
 Step 1. register      │     │   ┌────────────────────────────┐  │  │                 │  │             │
    IP:Port and        │     │   │Thread for business logic   │  │  │                 │  │             │
    establish a        │     │   │                            │  │  │                 │  │             │
  connection for       │     │   │   Step 4. Agent/Process    │  │  │                 │  │             │
     heartbeat         │     │   │  for business logic dies   │  │  │                 │  │             │
       │               │     │   │      for some reason       │  │  │                 │  │             │
       │               │     │   └────────────────────────────┘  │  │                 │  │             │
       │               │     │                                   │  │                 │  │             │
       │               │     │   ┌────────────────────────────┐  │  │                 │  │             │
       └───────────────┼─────┼───│Agent for heartbeat         │  │  │                 │  │             │
                       │     │   └────────────────────────────┘  │  │                 │  │             │
                       │     │                                   │  │                 │  │             │
                       │     │   ┌────────────────────────────┐  │  │ Business logic  │  │  Business   │
                       │     │   │Agent for restart           │  │  │    unit ...     │  │logic unit n │
                       │     │   │a). Kill agent for heartbeat│  │  └─────────────────┘  └─────────────┘
                       │     │   │b). Sleep long enough to    │  │           │                          
                       └─────┼──▶│wait removal of the entry   │  │           │                          
                             │   │within service registration │  │           ▼                          
                             │   │c). Restart the unit        │  │  ┌─────────────────┐                 
                             │   └────────────────────────────┘  │  │Data access layer│                 
                             │                                   │  │                 │                 
                             │                                   │  └─────────────────┘                 
                             │                                   │           │                          
                             │       Business logic unit 1       │           │                          
                             │                                   │           ▼                          
                             │                                   │  ┌─────────────────┐                 
                             └───────────────────────────────────┘  │    Database     │                 
                                                                    │                 │                 
                                                                    └─────────────────┘
```

### Gateway architecture

#### Revolution history

**Initial architecture**

* Only need to support web browser

![Keepalived deployment](.gitbook/assets/loadBalancingGatewayWebApp.png)

**BFF (Backend for frontEnd) layer**

* BFF layer exists to perform the following:
  * Security logic: If internal services are directly exposed on the web, there will be security risks. BFF layer could hide these internal services
  * Aggregation/Filter logic: Wireless service will typically need to perform filter (e.g. Cutting images due to the device size) / fit (client's customized requirements). BFF layer could perform these operations
* However, BFF contains both business and cross-cutting logic over time. 

![Keepalived deployment](.gitbook/assets/loadBalancingGatewayWirelessBFF.png)

**Gateway layer and Cluster BFF Layer**

* BFF contains too many cross-cutting logic such as
  * Rate limiting
  * Auth
  * Monitor
* Gateway is introduced to deal with these cross cutting concerns.

![Keepalived deployment](.gitbook/assets/loadBalancingGatewayWirelessBFFWithGateway.png)

**Clustered BFF and Gateway layer**

* Cluster implementation is introduced to remove single point of failure. 

![Keepalived deployment](images/loadBalancingGatewayClusteredBFF.png)

#### Gateway vs reverse proxy

1. Web Age: Reverse proxy (e.g. HA Proxy/Nginx) has existed since the web age
   * However, in microservice age, quick iteration requires dynamic configuration
2. MicroService Age: Gateway is introduce to support dynamic configuration
   * However, in cloud native age, gateway also needs to support dynamic programming such as green-blue deployment
3. Cloud native Age: Service mesh and envoy are proposed because of this. 

![Keepalived deployment](.gitbook/assets/loadBalancing_reverseProxyVsGateway.png)

**Reverse Proxy (Nginx)**

**Use cases**

* Use distributed cache while skipping application servers: Use Lua scripts on top of Nginx so Redis could be directly served from Nginx instead of from web app (Java service applications whose optimization will be complicated such as JVM/multithreading)
* Provides high availability for backend services
  * Failover config: proxy_next_upstream. Failure type could be customized, such as Http status code 5XX, 4XX, ...
  * Avoid failover avalanche config: proxy_next_upstream_tries limit number. Number of times to fail over

#### Gateway internals

* API Gateway has become a pattern: [https://freecontent.manning.com/the-api-gateway-pattern/](https://freecontent.manning.com/the-api-gateway-pattern/)
* Please see this [comparison](https://github.com/javagrowing/JGrowing/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%BC%80%E5%8F%91/%E6%B5%85%E6%9E%90%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E4%BA%BF%E7%BA%A7%E7%BD%91%E5%85%B3.md) (in Chinese)

![Keepalived deployment](.gitbook/assets/loadBalancing_gatewayInternals.png)

#### Gateway comparison

![Keepalived deployment](images/loadBalancing_gatewayComparison.png)

### Service discovery

#### Approach - Hardcode service provider addresses

* Pros:
  * Update will be much faster
* Cons:
  * Load balancer is easy to become the single point of failure
  * Load balancing strategy is inflexible in microservice scenarios. TODO: Details to be added.
  * All traffic volume needs to pass through load balancer, results in some performance cost. 

```
                                   ┌────────────────┐                                    
                                   │   DNS Server   │                                    
         ┌────────────────────────▶│                │              ┌────────────────────┐
         │                         └────────────────┘              │ Service provider 1 │
         │                                                 ┌──────▶│                    │
         │                                                 │       └────────────────────┘
         │                                                 │                             
         │             ┌────────────────────────────┐      │                             
┌────────────────┐     │Load balancer               │      │       ┌────────────────────┐
│Service consumer│     │                            │      │       │Service provider ...│
│                │────▶│service provider 1 address  │──────┼──────▶│                    │
└────────────────┘     │service provider ... address│      │       └────────────────────┘
                       │service provider N address  │      │                             
                       └────────────────────────────┘      │                             
                                                           │       ┌────────────────────┐
                                                           │       │ Service provider N │
                                                           └──────▶│                    │
                                                                   └────────────────────┘
```

#### Approach - Service registration center

* Pros:
  * No single point of failure. 
  * No additional hop for load balancing
* For details on service registration implementation, please refer to \[Service registration center]\(([https://github.com/DreamOfTheRedChamber/system-design/blob/master/serviceRegistry.md](https://github.com/DreamOfTheRedChamber/system-design/blob/master/serviceRegistry.md)))

### How to detect failure

* Heatbeat messages: Tcp connect, HTTP, HTTPS
* Detecting failure should not only rely on the heartbeat msg, but also include the application's health. There is a chance that the node is still sending heartbeat msg but application is not responding for some reason. (Psedo-dead)

#### Detect failure

* centralized and decentralized failure detecting: [https://time.geekbang.org/column/article/165314](https://time.geekbang.org/column/article/165314)
* heartbeat mechanism: [https://time.geekbang.org/column/article/175545](https://time.geekbang.org/column/article/175545)
* [https://iswade.github.io/database/db_internals_ch09\_failure_detection/#-accrual](https://iswade.github.io/database/db_internals_ch09\_failure_detection/#-accrual)

### How to gracefully shutdown

* Problem: Two RPC calls are involved in the process
  1. Service provider notifies registration center about offline plan for certain nodes
  2. Registration center notifies clients to remove certain nodes clients' copy of service registration list

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                               Within the Shutdown Hook                               │
│                     (e.g. Java's Runtime.addShutdownHook method)                     │
│                              ┌──────────────────┐                                    │
│                              │For requests which│                                    │
│                        ┌───▶ │happens before    │───────┐                            │
│                        │     │flag is turned on,│       │        ┌──────────────────┐│
│ ┌──────────────────┐   │     └──────────────────┘       │        │Close the machine ││
│ │Turn on the       │   │                                ├───────▶│                  ││
│ │shutdown flag upon│───┴┐                               │        │                  ││
│ │hook is triggered │    │                               │        └──────────────────┘│
│ └──────────────────┘    │    ┌──────────────────┐       │                            │
│                         │    │For new request,  │       │                            │
│                         └───▶│notify the caller │───────┘                            │
│                              │about the closure │                                    │
│                              └──────────────────┘                                    │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### How to gracefully start

* Problem: If a service provider node receives large volume of traffic without prewarm, it is easy to cause failures. How to make sure a newly started node won't receive large volume of traffic? 

```
┌───────────────────────────────────────────────────────────────────────────────────────────────┐
│                                     Within the start Hook                                     │
│                                                                                               │
│                 ┌───────────────────────────┐    ┌───────────────────────────┐    ┌─────────┐ │
│  ┌─────────┐    │Register the node info and │    │                           │    │         │ │
│  │ Service │    │     start time within     │    │  Adaptive load balancer   │    │         │ │
│  │provider │    │    registration center    │    │  based on the start time  │    │Finished │ │
│  │  node   │───▶│                           │───▶│                           │───▶│pre-warm │ │
│  │ starts  │    │    Service: addToCart     │    │ +10% weight every certain │    │         │ │
│  │         │    │ Address: 192.168.1.2:9080 │    │          period           │    │         │ │
│  └─────────┘    │StartTime: 02172020-11:34pm│    │                           │    │         │ │
│                 └───────────────────────────┘    └───────────────────────────┘    └─────────┘ │
│                                                                                               │
└───────────────────────────────────────────────────────────────────────────────────────────────┘
```

## Future readings

* [https://blog.51cto.com/cloumn/detail/6](https://blog.51cto.com/cloumn/detail/6)
