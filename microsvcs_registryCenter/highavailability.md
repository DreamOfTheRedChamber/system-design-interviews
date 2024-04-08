- [Healthcheck information](#healthcheck-information)
  - [Design heartbeat messages](#design-heartbeat-messages)
- [Event subscription](#event-subscription)

## Healthcheck information

* Service providers report heartbeat messages to registry center.
* Take the example of Zookeeper:
  1. Upon connection establishment, a long connection will be established between service providers and Zookeeper with a unique SESSION\_ID and SESSION\_TIMEOUT period.
  2. Clients will regularly report heartbeat messages to the Zookeeper.
  3. If Zookeeper does not receive heartbeat messages within SESSION\_TIMEOUT period, it will consider that the service provider is not healthy and remove it fromm the pool.

### Design heartbeat messages

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
