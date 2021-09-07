- [Error recover](#error-recover)
	- [Temporary failure - hinted hand-off](#temporary-failure---hinted-hand-off)
	- [Permanent failure - Merkle tree](#permanent-failure---merkle-tree)
	- [BloomFilter](#bloomfilter)

# Error recover
## Temporary failure - hinted hand-off
* Hinted handoff
	* https://cassandra.apache.org/doc/latest/operating/hints.html
	* https://docs.scylladb.com/architecture/anti-entropy/hinted-handoff/
* References: 
	* Read repair - https://docs.scylladb.com/architecture/anti-entropy/read-repair/
	* Others
		- Incremental repair: https://www.datastax.com/blog/2014/02/more-efficient-repairs-21
		- Advanced repair: https://www.datastax.com/blog/2013/07/advanced-repair-techniques

## Permanent failure - Merkle tree
* https://www.codementor.io/blog/merkle-trees-5h9arzd3n8

## BloomFilter
* [Bloom Filter: A simple but interesting data structure](https://medium.datadriveninvestor.com/bloom-filter-a-simple-but-interesting-data-structure-37fd53b11606)