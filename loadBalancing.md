<!-- MarkdownTOC -->

- [Load balancing](#load-balancing)
	- [Use cases](#use-cases)
		- [Decoupling](#decoupling)
		- [Security](#security)
	- [Load balancing algorithms](#load-balancing-algorithms)
		- [Round-robin](#round-robin)
		- [Weighted round robin](#weighted-round-robin)
		- [Least connections](#least-connections)
		- [Source hashing](#source-hashing)
	- [Categorize based on internal mechanism](#categorize-based-on-internal-mechanism)
		- [Http redirect based load balancer](#http-redirect-based-load-balancer)
		- [DNS based load balancer](#dns-based-load-balancer)
		- [Reverse proxy based load balancer](#reverse-proxy-based-load-balancer)
		- ["SNAT" based load balancer](#snat-based-load-balancer)
			- [Only Change IP address](#only-change-ip-address)
			- [Only change MAC address](#only-change-mac-address)
	- [Categorize based on hardware/software](#categorize-based-on-hardwaresoftware)
		- [Hardware based load balancer](#hardware-based-load-balancer)
			- [F5](#f5)
		- [Software based load balancer](#software-based-load-balancer)
			- [LVS](#lvs)
			- [Nginx](#nginx)
			- [HAProxy](#haproxy)

<!-- /MarkdownTOC -->

# Load balancing
## Use cases
### Decoupling
* Hidden server maintenance. You can take a web server out of the load balancer pool, wait for all active connections to drain, and then safely shutdown the web server without affecting even a single client. You can use this method to perform rolling updates and deploy new software across the cluster without any downtime. 
* Seamlessly increase capacity. You can add more web servers at any time without your client ever realizing it. As soon as you add a new server, it can start receiving connections. 
* Automated scaling. If you are on cloud-based hosting with the ability to configure auto-scaling (like Amazon, Open Stack, or Rackspace), you can add and remove web servers throughout the day to best adapt to the traffic. 

### Security
* SSL termination: By making load balancer the termination point, the load balancers can inspect the contents of the HTTPS packets. This allows enhanced firewalling and means that you can balance requests based on teh contents of the packets. 
* Filter out unwanted requests or limit them to authenticated users only because all requests to back-end servers must first go past the balancer. 
* Protect against SYN floods (DoS attacks) because they pass traffic only on to a back-end server after a full TCP connection has been set up with the client. 

## Load balancing algorithms
### Round-robin
* Def: Cycles through a list of servers and sends each new request to the next server. When it reaches the end of the list, it starts over at the beginning. 
* Problems: 
	- Not all requests have an equal performance cost on the server. But a request for a static resource will be several orders of magnitude less resource-intensive than a requst for a dynamic resource. 
	- Not all servers have identical processing power. Need to query back-end server to discover memory and CPU usage, server load, and perhaps even network latency. 
	- How to support sticky sessions: Hashing based on network address might help but is not a reliable option. Or the load balancer could maintain a lookup table mapping session ID to server. 

### Weighted round robin

### Least connections


### Source hashing


## Categorize based on internal mechanism
### Http redirect based load balancer
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

### DNS based load balancer
* Steps:
	1. Client's requests first reach DNS authority server to get the IP address.
	2. Client issues another requests to the parsed IP address.
* Pros: 
	- DNS has caching, after the first-time parse, there won't need another time for a long time. As a result, it won't have too much performance downsides.
	- It is a basic config service provided by DNS providers so no development will be required. Many DNS service provider offers geographical DNS service. 
* Cons: 
	- DNS has multi-layer caches. Even after scaling up, the old DNS record might still point to the old IP address.
	- DNS load balancer is controlled by DNS service providers instead service owners. 
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-DnsBased.png)

### Reverse proxy based load balancer
* Steps: 
	1. Client's requests first reach reverse proxy. 
	2. The reverse proxy forwards requests to internal servers and gets the response. 
	3. The reverse proxy forwards the response to clients.
* Pros: 
	- Integrated together with reverse proxy. No additional deployment. 
* Cons:
	- Reverse proxy operates on the HTTP layer so not high performance. It is usually used on a small scale when there are fewer than 100 servers. 
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-ReverseProxy.png)

### "SNAT" based load balancer
#### Only Change IP address
* Steps: 
	1. Client's requests first reach IP load balancer.
	2. IP based load balancer changes the target IP address to internal servers' IP address, and change source IP to be load balancer's IP address (SNAT)
	3. Internal servers return response to IP based load balancer. 
	4. IP based load balancer changes the target IP address.
* Pros:
	- Operates on network layer and has much more efficiency compared with reverse proxy
* Cons: 
	- All requests/Responses will require IP based load balancer to replace the target IP address. It will become the bottleneck really easy. 
* There is a flow chart [Caption in Chinese to be translated](./images/loadBalancing-IpBased.png)

#### Only change MAC address
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

## Categorize based on hardware/software
### Hardware based load balancer
* Higher throughput and lower latency. High purchase cost. Hardware load balancer prices start from a few thousand dollars and go as high as over 100,000 dollars per device. Specialized training and harder to find people with the work experience necessary to operate them.  

#### F5

### Software based load balancer
* More intelligent because can talk HTTP (can perform the compression of resources passing through and routing-based on the presence of cookies) and more flexible for hacking in new features or changes

#### LVS

#### Nginx

#### HAProxy
* Extra functionalities of HAProxy. It can be configured as either a layer 4 or layer 7 load balancer. 
	- When HAProxy is set up to be a layer 4 proxy, it does not inspect higher-level protocols and it depends solely on TCP/IP headers to distribute the traffic. This, in turn, allows HAProxy to be a load balancer for any protocol, not just HTTP/HTTPS. You can use HAProxy to distribute traffic for services like cache servers, message queues, or databases. 
	- HAProxy can also be configured as a layer 7 proxy, in which case it supports sticky sessions and SSL termination, but needs more resources to be able to inspect and track HTTP-specific information. The fact that HAProxy is simpler in design makes it perform sligthly better than Nginx, especially when configured as a layer 4 load balancer. Finally, HAProxy has built-in high-availability support.




