<!-- MarkdownTOC -->

- [Load balancing](#load-balancing)
	- [Categories](#categories)
		- [Http redirect based](#http-redirect-based)
		- [DNS based](#dns-based)
		- [Reverse proxy based](#reverse-proxy-based)
		- [IP based](#ip-based)
		- [Link layer based](#link-layer-based)
	- [Definition](#definition)
	- [Benefits](#benefits)
	- [Round-robin algorithm](#round-robin-algorithm)
	- [Hardware vs software](#hardware-vs-software)
		- [HAProxy vs Nginx](#haproxy-vs-nginx)

<!-- /MarkdownTOC -->


# Load balancing
## Categories
### Http redirect based
* Steps:
	1. Client's requests first reach a load balancing server which translate original target IP address A to a new target IP address B with a 302 HTTP response code
	2. Client issues another request to the new target IP address B
* Pros:
	- Easy to implement
* Cons: 
	- Client needs to have two http requests to data center.
	- Internal web/application servers' IP address will be exposed to external world and cause potential security risks.
		+ Compared with internal servers, load balancing servers will have stricter firewall policies and security configurations. 
* Due to the security risks, Http redirect based load balancing is rarely used in practice. 
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-Redirect.png)

### DNS based
* Steps:
	1. Client's requests first reach DNS authority server to get the IP address.
	2. Client issues another requests to the parsed IP address.
* Pros: 
	- DNS has caching, after the first-time parse, there won't need another time for a long time. As a result, it won't have any performance sacrifice.
	- It is a basic config service provided by DNS providers so no development will be required. 
* Cons: 
	- The config is controlled by DNS service providers.
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-DnsBased.png)

### Reverse proxy based
* Steps: 
	1. Client's requests first reach reverse proxy. 
	2. The reverse proxy forwards requests to internal servers and gets the response. 
	3. The reverse proxy forwards the response to clients.
* Pros: 
	- ??? 
* Cons:
	- Reverse proxy operates on the HTTP layer so not high performance. It is usually used on a small scale when there are fewer than 100 servers. 
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-ReverseProxy.png)

### IP based
* Steps: 
	1. Client's requests first reach IP load balancer.
	2. IP based load balancer changes the target IP address to internal servers' IP address.
	3. Internal servers return response to IP based load balancer.
	4. IP based load balancer changes the target IP address.
* Pros:
	- Operates on network layer and has much more efficiency compared with reverse proxy
* Cons: 
	- All requests/Responses will require IP based load balancer to replace the target IP address. It will become the bottleneck really easy. 
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-IpBased.png)

### Link layer based
* Steps: 
	1. Client's requests first reach link layer load balancer.
	2. IP based load balancer changes the target MAC address to internal servers' IP address.
	3. Internal servers return response to link layer based load balancer.
	4. IP based load balancer changes the target MAC address.
* Pros:
	- Since there are no changes to IP address, internal servers could directly return the response to clients. 
	- Operates on link layer so could be more performant than IP based load balancing. 
* Cons: 
	- All requests/Responses will require IP based load balancer to replace the target IP address. It will become the bottleneck really easy. 

* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-LinkLayer.png)


## Definition
* Client to gateway: 
	- Implementation: DNS resolves to different ip address. 
* Gateway to web server: 
	- Implementation: Nginx reverse proxy
		+ Proxy hides itself
		+ Reverse proxy hides the server pool. 
* Web server to application server:
	- Implementation: Connection pool
* Application server to database: 
	- Implementation: Partition / Sharding

## Benefits 
* Decoupling
	- Hidden server maintenance. You can take a web server out of the load balancer pool, wait for all active connections to drain, and then safely shutdown the web server without affecting even a single client. You can use this method to perform rolling updates and deploy new software across the cluster without any downtime. 
	- Seamlessly increase capacity. You can add more web servers at any time without your client ever realizing it. As soon as you add a new server, it can start receiving connections. 
	- Automated scaling. If you are on cloud-based hosting with the ability to configure auto-scaling (like Amazon, Open Stack, or Rackspace), you can add and remove web servers throughout the day to best adapt to the traffic. 
* Security
	- SSL termination: By making load balancer the termination point, the load balancers can inspect the contents of the HTTPS packets. This allows enhanced firewalling and means that you can balance requests based on teh contents of the packets. 
	- Filter out unwanted requests or limit them to authenticated users only because all requests to back-end servers must first go past the balancer. 
	- Protect against SYN floods (DoS attacks) because they pass traffic only on to a back-end server after a full TCP connection has been set up with the client. 

## Round-robin algorithm 
* Def: Cycles through a list of servers and sends each new request to the next server. When it reaches the end of the list, it starts over at the beginning. 
* Problems: 
	- Not all requests have an equal performance cost on the server. But a request for a static resource will be several orders of magnitude less resource-intensive than a requst for a dynamic resource. 
	- Not all servers have identical processing power. Need to query back-end server to discover memory and CPU usage, server load, and perhaps even network latency. 
	- How to support sticky sessions: Hashing based on network address might help but is not a reliable option. Or the load balancer could maintain a lookup table mapping session ID to server. 

## Hardware vs software 
| Category | Software                                                                                                                                                                                                | Hardware                                                                                                                                                | 
|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------| 
| Def      | Run on standard PC hardware, using applications like Nginx and HAProxy                                                                                                                                  | Run on special hardware and contain any software pre-installed and configured by the vendor.                                                            | 
| Model    | Operate on Application Layer                                                                                                                                                                            | Operate on network and transport layer and work with TCP/IP packets. Route traffic to backend servers and possibly handling network address translation | 
| Strength/Weakness | More intelligent because can talk HTTP (can perform the compression of resources passing through and routing-based on the presence of cookies) and more flexible for hacking in new features or changes | Higher throughput and lower latency. High purchase cost. Hardware load balancer prices start from a few thousand dollars and go as high as over 100,000 dollars per device. Specialized training and harder to find people with the work experience necessary to operate them.                                                                                                                      | 

### HAProxy vs Nginx 

| Category  | Nginx                                       | HAProxy                                                                                                                       | 
|-----------|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------| 
| Strengths | Can cache HTTP responses from your servers. | A little faster than Nginx and a wealth of extra features. It can be configured as either a layer 4 or layer 7 load balancer. | 

* Extra functionalities of HAProxy. It can be configured as either a layer 4 or layer 7 load balancer. 
	- When HAProxy is set up to be a layer 4 proxy, it does not inspect higher-level protocols and it depends solely on TCP/IP headers to distribute the traffic. This, in turn, allows HAProxy to be a load balancer for any protocol, not just HTTP/HTTPS. You can use HAProxy to distribute traffic for services like cache servers, message queues, or databases. 
	- HAProxy can also be configured as a layer 7 proxy, in which case it supports sticky sessions and SSL termination, but needs more resources to be able to inspect and track HTTP-specific information. The fact that HAProxy is simpler in design makes it perform sligthly better than Nginx, especially when configured as a layer 4 load balancer. Finally, HAProxy has built-in high-availability support.
