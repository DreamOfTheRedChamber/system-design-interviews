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

## Basic Web Load Balancing

### Use cases

#### Decoupling

* Hidden server maintenance. You can take a web server out of the load balancer pool, wait for all active connections to drain, and then safely shutdown the web server without affecting even a single client. You can use this method to perform rolling updates and deploy new software across the cluster without any downtime. 
* Seamlessly increase capacity. You can add more web servers at any time without your client ever realizing it. As soon as you add a new server, it can start receiving connections. 
* Automated scaling. If you are on cloud-based hosting with the ability to configure auto-scaling (like Amazon, Open Stack, or Rackspace), you can add and remove web servers throughout the day to best adapt to the traffic. 

#### Security

* SSL termination: By making load balancer the termination point, the load balancers can inspect the contents of the HTTPS packets. This allows enhanced firewalling and means that you can balance requests based on teh contents of the packets. 
* Filter out unwanted requests or limit them to authenticated users only because all requests to back-end servers must first go past the balancer. 
* Protect against SYN floods (DoS attacks) because they pass traffic only on to a back-end server after a full TCP connection has been set up with the client. 

```
┌────────────────┐        ┌────────────────┐       ┌────────────────┐
│     Client     │        │     Client     │       │     Client     │
└────────────────┘        └────────────────┘       └────────────────┘
         │                         │                        │        
         │                     TLS/SSL                      │        
     TLS/SSL                       │                      TCP        
         │                         │                        │        
         ▼                         ▼                        ▼        

┌────────────────┐        ┌────────────────┐       ┌────────────────┐
│  Layer 4 load  │        │  Layer 4 load  │       │  Layer 4 load  │
│    balancer    │        │    balancer    │       │    balancer    │
└────────────────┘        └────────────────┘       └────────────────┘
         │                         │                        │        
         │                       TCP                        │        
     TLS/SSL                       │                    TLS/SSL      
         │                         │                        │        
         │                         │                        │        
         ▼                         ▼                        ▼        
┌────────────────┐        ┌────────────────┐       ┌────────────────┐
│    Upstream    │        │    Upstream    │       │    Upstream    │
│    services    │        │    services    │       │    services    │
└────────────────┘        └────────────────┘       └────────────────┘
```

### Load balancing algorithms

#### Round-robin

* Def: Cycles through a list of servers and sends each new request to the next server. When it reaches the end of the list, it starts over at the beginning. 
  * How to support sticky sessions: Hashing based on network address might help but is not a reliable option. Or the load balancer could maintain a lookup table mapping session ID to server. 

#### Weighted round robin

* Problems of round robin:  
  * Not all requests have an equal performance cost on the server. But a request for a static resource will be several orders of magnitude less resource-intensive than a requst for a dynamic resource. 
  * Not all servers have identical processing power. Need to query back-end server to discover memory and CPU usage, server load, and perhaps even network latency. 

#### Least load first (from server perspective)

* Problems of weighted round robin:  
  * Server might be under different status even given the same type of requests
* How to define least load: 
  * For a layer 4 load balancing option such as LVS, it could load balance based on the number of connections.
  * For a layer 7 load balancing option such as Nginx, it could load balance based on the number of Http requests.  
  * More customized criteria such as CPU load, I/O load. 

#### Best performance first (from client perspective)

* Response time

#### Source hashing

* Hash of IP address
* Hash of session id

### Categorization

#### Http redirect based load balancer (rarely used)

* Steps:
  1. Client's requests first reach a load balancing server which translate original target IP address A to a new target IP address B with a 302 HTTP response code
  2. Client issues another request to the new target IP address B
* Pros:
  * Easy to implement
* Cons: 
  * Client needs to have two http requests to data center.
  * Internal web/application servers' IP address will be exposed to external world and cause potential security risks.
    * Compared with internal servers, load balancing servers will have stricter firewall policies and security configurations. 
* Due to the security risks and performance cost, Http redirect based load balancing is rarely used in practice. 
* There is a flow chart [Caption in Chinese to be translated](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/loadBalancing-Redirect.png)

#### DNS based load balancer

* Steps:
  1. Client's requests first reach DNS authority server to get the IP address.
  2. Client issues another requests to the parsed IP address.
* Pros: 
  * Low latency: DNS will be resolved for the first time. The second time when a request come it will use the cached version. 
  * It is a basic config service provided by DNS providers so no development will be required. Many DNS service provider offers geographical DNS service. 
* Cons: 
  * Low accuracy: DNS will be only resolved one time and cached for sometime. The cached value will be used for a relatively long time. 
  * High update time: DNS has multi-layer caches. Even after scaling up, the old DNS record might still point to the old IP address.
  * Not customizable: DNS load balancer is controlled by DNS service providers. 
  * Simple load balancing algorithm: DNS load balancing algorithms are relatively simple. For example, it could not make decisions based on the differences of servers. 
* There is a flow chart [Caption in Chinese to be translated](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/loadBalancing-DnsBased.png)
*   Example DNS record for using round robin load balancing on top of DNS

    ```
    ┌────────────────────────────────┐
    │     Domain     IP addresses    │
    │   example.com     66.66.66.1   │
    │   example.com     66.66.66.2   │
    │                                │
    └────────────────────────────────┘
    ```

**HTTP-DNS based load balancer**

* Due to the limitation of traditional DNS, many companies will develop HTTPDNS. For more details for how HttpDNS work, please see [HTTPDNS](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/dns.md#httpdns)
* It is mainly used in App scenarios. For web scenarios, since url parsing is done by browser, it will be more compliacted. 
* Pros:
  * Shorter update time. It won't be impacted by the multi-layer cache. 
  * More customized load balancing algorithm.
* Cons:
  * Needs customized development and has high cost. 

#### Application layer (e.g. Nginx, HAProxy, Apache)

* Pros: 
  * Could make load balancing decisions based on detailed info such as application Url.
  * Only applicable to limited scenarios such as HTTP / Email which sit in level 7. 
* Cons: 
  * Low Performance (A single server could support roughly 50K QPS) when compared with LVS (Layer 4 - Transport layer software, A single server could support 800K QPS) or F5 (Layer 4 hardware, a single device supports 2000K - 8000K QPS)
  * Security: Usually don't have security features built-in place

**Reverse proxy (e.g. Nginx)**

* Steps: 
  1. Client's requests first reach reverse proxy. 
  2. The reverse proxy forwards requests to internal servers and gets the response. 
  3. The reverse proxy forwards the response to clients.
* Pros: 
  * Integrated together with reverse proxy. No additional deployment. 
* Cons:
  * Reverse proxy operates on the HTTP layer so not high performance. It is usually used on a small scale when there are fewer than 100 servers. 
* There is a flow chart [Caption in Chinese to be translated](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/loadBalancing-ReverseProxy.png)

#### Network/Transport/DataLink layer (e.g. Nginx Plus, F5/A10, LVS, HAProxy)

**Software based**

**LVS**

* LVS supports three modes of operation: VS/NAT, VS/TUN, VS/DR. It supports the following type of load balancing algorithms:
  * Round robin
  * Weighted round robin
  * Least connections
  * Weighted least connections
  * Locality-based least connections
  * Locality-based least connections with replication
  * Destination hashing
  * Source hashing

**VS/NAT mode**

* Steps: 
  1. Client's requests first reach IP load balancer.
  2. IP based load balancer changes the target IP address to internal servers' IP address, and change source IP to be load balancer's IP address (SNAT)
  3. Internal servers return response to IP based load balancer. 
  4. IP based load balancer changes the target IP address.
* Pros:
  * Operates on network layer and has much more efficiency compared with reverse proxy
* Cons: 
  * All requests/Responses will require IP based load balancer to replace the target IP address. It will become the bottleneck really easy. 
* There is a flow chart [Caption in Chinese to be translated](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/loadBalancing-IpBased.png)

**VS/DR mode**

* How it works:
  * A same virtual IP address is bond to both director server and real server. 
  * When ARP for a IP address, director server's address will be returned and real server's address will be hidden. 
* Steps: 
  1. Client's requests first reach director server. For illustration, let's say within the request, the source IP is CIP, the dest IP is VIP; the source MAC is CMAC, the dest MAC is DMAC. 
  2. Director server will keep the source and dest IP unchanged. Director server will change the target MAC address DMAC to internal servers' MAC address RMAC. 
  3. Internal servers return response to client. 
* Pros:
  * High performance and could be used broadly
    1. Since there are no changes to IP address, internal servers could directly return the response to clients.
    2. Operates on the data link layer (only need to change the MAC address) 
* Cons: 
  * Relies on client to retry because the response does not pass through director server. Even when a real server is down, there might still be gap until director server could remove the problematic real server from the pool. 
  * A bit more complicated when scaling/upgrading real servers because needs to coordinate changes with director server because retry are done from client. 

![VS DR mode](images/loadBalancing-changeMacAddress.png)

**VS/TUN mode - TODO**

**Hardware based**

* F5 / A10:
  * Pros: 
    * High performance. Could support 1000K concurrent connections
    * Stability: Tested thoroughly by producers
    * Security features such as DDoS protection, firewalls
  * Cons: 
    * Low customization options

### Typical architecture and metrics

#### Multi layer

* Geo level - Use DNS load balancing.
* Cluster level - hardware load balancing such as F5 among cluster level (a single device supports 2000K - 8000K QPS)
* Within a cluster
  * (Optional) LVS - A single server could support 800K QPS. No need to introduce if QPS is lower than 100K. 
  * Nginx - A single server could support roughly 50K QPS

#### Keepalived for high availability

* Virtual IP: A floating IP will be shared between a active and many backup load balancers. 
* Use cases: 
  * Popular in stateless scenarios such as load balancing (LVS, nginx).
  * Many modes such as master-slave, master-master mode. 
* Mechanism: Run a monitor scripts against target. 
  * If run on network layer, then use ICMP to send a package to check whether it is responding. 
  * If run on transport layer, then check whether specific TCP port is responding.
  * If run on application layer, then could run FTP/TELNET/HTTP/DNS protocols to customize some checking. 
* VRRP protocol will be used for failover and master election
* Please refer to [Keepalived and haproxy](https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/7/html/load_balancer_administration/ch-keepalived-overview-vsa#s1-lvs-basic-VSA) for more details. 

![Keepalived deployment](.gitbook/assets/loadBalancingKeepAlivedDeployment.png)

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
