

# Static
## Round robin
* Weighted round robin
  * Cons: It may result same node being picked during several consecutive requests. 
* Smooth weighted round robin
  1. For each node, currentWeight = currentWeight + weight
  2. Pick the max currentWeight node as target node
  3. Change target node currentWeight = currentWeight - sum(weight)

## Random
* When compared with round robin, the later could be more smooth. Because random-number based could make several consecutive requests landing in one node.

### Consistent hashing
* Normal hashing algorithm
   * Cons: Too many items to migrate during resharding
* Consistent hashing 
   * Cons: Uneven load during scale up/down
* Consistent hashing with virtual nodes
  * The interval between nodes could be uneven, but the load will be even. 

#### Pros
* If local cache is enabled on servers, then consistent hashing could reduce the inconsistency. 
  * Using UserID as key, if request 1 for user ID comes to server 1, then request for this user will always land on this node. It will result in better consistency when compared with round robin, etc.
  * However, it could not completely resolve the inconsistency. For example, when cluster scales up, the old request could land on old node and new request could land on new node. 

#### Complexity analysis

* Assume the total number of data M, the total number of nodes N
* Read/write complexity increases from O\(1）to O\(lgn\) When compared with traditional hashing because consistent hashing read/write steps are as follow: 
  1. Convert hashkey into 32 bit int number. O\(1\)
  2. Use binary search to find the corresponding node. O\(lgn\)
* Data migration complexity decreases from O\(m\) to O\(m/N\). 

#### References

* Data distributed in multiDC: [https://www.onsip.com/voip-resources/voip-fundamentals/intro-to-cassandra-and-networktopologystrategy](https://www.onsip.com/voip-resources/voip-fundamentals/intro-to-cassandra-and-networktopologystrategy)
* Consistent hashing in Cassandra documentation: [https://cassandra.apache.org/doc/latest/architecture/dynamo.html](https://cassandra.apache.org/doc/latest/architecture/dynamo.html)


## Based on metric numbers
* Least num of connection
* Least num of ongoing requests
* Fastest response time

### How to collect metrics
* Client report to metric collector

### Cons
* These approaches don't take the request itself into consideration. 
* For example, using the lease num of connection. There might not be that many requests but each request could be pretty big. 

# Dynamic