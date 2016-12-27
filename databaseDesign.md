# Database system

<!-- MarkdownTOC -->

- [Scenario](#scenario)
- [Storage](#storage)
	- [Initial design](#initial-design)
	- [Combine append-only write and binary search read](#combine-append-only-write-and-binary-search-read)
	- [Optimize read with Index](#optimize-read-with-index)
	- [Optimize read with Bloom filter](#optimize-read-with-bloom-filter)
	- [Final read process on a single machine](#final-read-process-on-a-single-machine)
- [Scale](#scale)
	- [Sharding with master slave model](#sharding-with-master-slave-model)
	- [Too much data to store on local disk](#too-much-data-to-store-on-local-disk)
	- [Race condition](#race-condition)

<!-- /MarkdownTOC -->


## Scenario
* value get(Key)
* set(key, value)
	- Modify existing entry (key, value)
	- Create new entry (key, value)

## Storage
### Initial design
* Sorted file with (Key, Value) entries
	- Binary search based read O(lgn)
	- Linear read operations write O(n)
* Unsorted file with (Key, Value) entries
	- Linear read operations O(n)
	- Constant time write O(1)

### Combine append-only write and binary search read
* Break the large table into a list of smaller tables 0~N
	- 0~N-1 th tables are all stored in disk in sorted order
	- Nth table is stored in memory in unsorted order
* Write
	- Write directly to the Nth table
	- If the Nth table is full, sort it and write it to disk. Create a new table for future writing. 
* Read
	- Linearly scan through the Nth table in memory first. 
	- If cannot find, perform binary search on N-1, N-2, ..., 0th
* Consume too much disk space due to repetitive entries (Key, Value)
	- Have a K-way merge for the sorted tables regularly
* What if memory is lost?
	- Write ahead log

### Optimize read with Index
* Each sorted table should have an index inside memory. The index is a sketch of key value pairs
* More advanced way to build index with B tree. 

### Optimize read with Bloom filter
* Accuracy of bloom filter
	- Number of hash functions
	- Length of bit vector
	- Number of stored entries

### Final read process on a single machine
1. First check the entry inside memory
2. Check the bloom filter for each file
3. Use the index to find the value for the key
4. Return key, value pair

## Scale
### Sharding with master slave model
* Master has the hashmap [Key, server address]
* Slave is responsible for storing data

### Too much data to store on local disk
* Add 
* Replace local disk with GFS
	- Disk size
	- Replica 
	- Failure and recovery
* How to write SSTable to GFS
	- Divide SSTable into multiple chunks and store each chunk inside GFS.
	
### Race condition
* Distributed lock 
	- Consistent hashmap is stored inside the lock server


