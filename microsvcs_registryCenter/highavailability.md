- [High availability](#high-availability)
  - [Cluster](#cluster)
  - [Multi-DC](#multi-dc)

# High availability

## Cluster

* Take the example of Zookeeper
  1. Upon Zookeeper start, a leader will be elected according to Paxos protocol.
  2. Leader will be responsible for update operations according to ZAB protocol.
  3. An update operation is considered successful only if majority servers have finished update.

![](../.gitbook/assets/registerCenter\_zookeeperCluster.png)

## Multi-DC

* Please see the following chart for Consul

![](../.gitbook/assets/registryCenter\_consul\_multiDC.png)
