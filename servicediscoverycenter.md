
# Registration center

<!-- MarkdownTOC -->

- [Responsibility](#responsibility)
	- [Service registration](#service-registration)
	- [Service discovery](#service-discovery)
	- [Heartbeat detection](#heartbeat-detection)
- [CP or AP model?](#cp-or-ap-model)
	- [Case study](#case-study)
- [Implementation](#implementation)
	- [Comparison](#comparison)
	- [Zookeeper?](#zookeeper)
		- [Definition](#definition)
		- [Functionality](#functionality)
		- [Limitations](#limitations)
			- [CP model](#cp-model)
			- [Scalability](#scalability)
			- [Storage](#storage)
			- [Service health check](#service-health-check)
			- [Nonfault tolerant native client](#nonfault-tolerant-native-client)
		- [Applicable scenarios](#applicable-scenarios)

<!-- /MarkdownTOC -->

## Responsibility
### Service registration
### Service discovery
### Heartbeat detection 

## CP or AP model?
* Service registration: Si = F(ServiceName)
	- ServiceName is a lookup parameter
	- Si is the corresponding service' IP:Port list

### Case study 
* Setup:
	- Service A is calling Service B
	- Service A has two deployments A1 and A2
	- Service B has one deployment with IP port from IP1 -> IP10
	- A1 deployment only gets IP1 -> IP9
	- A2 deployment only gets IP2 -> IP10
* If using CP model
	- Then A1 and A2 are both not available
* If using AP model
	- Then the only impact is that the traffic distribution on IP1 -> IP10 is not even. 
* Should use AP model

## Implementation

### Comparison

### Zookeeper?
* It is becoming popular because it is the default registration center for Dubbo framework. 

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