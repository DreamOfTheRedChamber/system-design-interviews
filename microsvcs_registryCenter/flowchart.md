- [Functionalities registry center should support](#functionalities-registry-center-should-support)
  - [Service provider register and unregister service](#service-provider-register-and-unregister-service)
  - [Consumers look up service provider info](#consumers-look-up-service-provider-info)
  - [Healthcheck information](#healthcheck-information)
  - [Event subscription](#event-subscription)
  - [Administration](#administration)


# Functionalities registry center should support

## Service provider register and unregister service

* Storage structure is as the following:
  * Service layer
  * Cluster layer
  * Info entries as KV

![](../.gitbook/assets/registryCenter\_directory.png)

* Flowchart for registering service

![](../.gitbook/assets/registryCenter\_registerFlowchart.png)

* Flowchart for unregistering service

![](../.gitbook/assets/registryCenter\_unregisterFlowChart.png)

* Multiple registration center deployed
  * Different gateways/business logic units are connected to different registration center
* What if a pushdown instruction arrives at the wrong registration center
  * Gossip protocol to discover between different registration centers
    * Cons: Long message delay / Forward redundancy
    * Pros: Suitable for large number of clusters. Used in P2P, Redis cluster, Consul
  * An open source implementation for Gossip [https://github.com/scalecube/scalecube-cluster](https://github.com/scalecube/scalecube-cluster)

## Consumers look up service provider info

* Local cache: Need local cache to improve performance.
* Snapshot: This snapshot in disk is needed because the network between consumers and registry center are not always good. If consumers restart and connection is not good, then consumers could still read from snapshot (there is no cache so far).

![](../.gitbook/assets/registryCenter\_lookup.png)

## Healthcheck information

* Service providers report heartbeat messages to registry center.
* Take the example of Zookeeper:
  1. Upon connection establishment, a long connection will be established between service providers and Zookeeper with a unique SESSION\_ID and SESSION\_TIMEOUT period.
  2. Clients will regularly report heartbeat messages to the Zookeeper.
  3. If Zookeeper does not receive heartbeat messages within SESSION\_TIMEOUT period, it will consider that the service provider is not healthy and remove it fromm the pool.

**Design heartbeat messages**

**Proactive vs reactive heartbeat messages**

* Proactive: Registry center proactively calls service providers.
  * Cons: Registry center needs to loop through all service providers regularly. There will be some delay.
* Reactive: Service providers reports heartbeat messages to service registry.

**Report frequency**

* Usually the health ping frequency is set to 30s. This will avoid too much pressure on the server, and at the same time avoid too much delay in catching a node's health states.

**Subhealth criteria**

* A State transition between death, health and subhealth. An interesting question is how to decide the threshold for a node to transit from health to subhealth?
  * Both application layer and service layer health info needs to be gathered. For application layer, the RPS/response time of each API will be different, so simply setting threshold for total failure or TPS. Use the percentage of success / total as standards.

**Resilient to network latency**

* Deploy detectors across different locations.
* But set up a threshold (like 40%) to avoid remove all nodes due to network problems.

## Event subscription

* RPC client subscribes to certain services
* Take the example of Zookeeper: Use watch mechanism

![](../.gitbook/assets/registryCenter\_subscribe.png)

**How to avoid notification storm**

* Problem: Suppose a service provider has 100 nodes and each node has 100 consumers. Then when there is an update in the service provider, there will be 100\*100 notifications generated.
* Solution:
  * Capacity planning for registry center.
  * Scale up registry center.
  * Only transmit incremental information.

## Administration

* Administrative functionalities:
  * Update a service provider's information
* Blacklist and whitelist service providers
  * e.g. Service providers in production environments should not register inside register center of test environments.
