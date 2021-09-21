# Algorithm\_ConsistentHashing

* [Consistent hashing](algorithm_consistenthashing.md#consistent-hashing)
  * [Design thoughts](algorithm_consistenthashing.md#design-thoughts)
  * [Complexity analysis](algorithm_consistenthashing.md#complexity-analysis)
  * [References](algorithm_consistenthashing.md#references)

## Consistent hashing

### Design thoughts

1. Normal hashing algorithm
   * Cons: Too many items to migrate during resharding
2. Consistent hashing 
   * Cons: Uneven load during scale up/down
3. Consistent hashing with virtual nodes:

### Complexity analysis

* Assume the total number of data M, the total number of nodes N
* Read/write complexity increases from O\(1）to O\(lgn\) When compared with traditional hashing because consistent hashing read/write steps are as follow: 
  1. Convert hashkey into 32 bit int number. O\(1\)
  2. Use binary search to find the corresponding node. O\(lgn\)
* Data migration complexity decreases from O\(m\) to O\(m/N\). 

### References

* Data distributed in multiDC: [https://www.onsip.com/voip-resources/voip-fundamentals/intro-to-cassandra-and-networktopologystrategy](https://www.onsip.com/voip-resources/voip-fundamentals/intro-to-cassandra-and-networktopologystrategy)
* Consistent hashing in Cassandra documentation: [https://cassandra.apache.org/doc/latest/architecture/dynamo.html](https://cassandra.apache.org/doc/latest/architecture/dynamo.html)

