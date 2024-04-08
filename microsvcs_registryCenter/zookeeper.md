- [Storage structure](#storage-structure)
- [Cluster](#cluster)

# Storage structure
* Storage structure is as the following:
  * Service layer
  * Cluster layer
  * Info entries as KV

![](../.gitbook/assets/registryCenter\_directory.png)

# Cluster
* Take the example of Zookeeper
  1. Upon Zookeeper start, a leader will be elected according to Paxos protocol.
  2. Leader will be responsible for update operations according to ZAB protocol.
  3. An update operation is considered successful only if majority servers have finished update.

![](../.gitbook/assets/registerCenter\_zookeeperCluster.png)

