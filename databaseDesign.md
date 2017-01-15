# Database system

<!-- MarkdownTOC -->

- [Features](#features)
- [Services](#services)
- [Storage](#storage)
	- [Initial design](#initial-design)
	- [Balance read/write complexity](#balance-readwrite-complexity)
	- [Store the Nth table/file in memory](#store-the-nth-tablefile-in-memory)
	- [Save disk space](#save-disk-space)
	- [Optimize read](#optimize-read)
		- [Optimize read with index](#optimize-read-with-index)
		- [Optimize read with Bloom filter](#optimize-read-with-bloom-filter)
	- [Standalone final solution](#standalone-final-solution)
		- [Read process](#read-process)
		- [Write process](#write-process)
- [Scale](#scale)
	- [Sharding with master slave model](#sharding-with-master-slave-model)
	- [Too much data to store on local disk](#too-much-data-to-store-on-local-disk)
	- [Race condition](#race-condition)

<!-- /MarkdownTOC -->

## Features
* Read or write intensive
	- Whether to optimize read operations
* Large amounts of data
	- Whether needs sharding

## Services
* value get(Key)
* set(key, value)
	- Modify existing entry (key, value)
	- Create new entry (key, value)

## Storage
### Initial design
* Sorted file with (Key, Value) entries
	- Disk-based binary search based read O(lgn)
	- Linear read operations write O(n)
* Unsorted file with (Key, Value) entries
	- Linear read operations O(n)
	- Constant time write O(1)

### Balance read/write complexity
* Combine append-only write and binary search read
	- Break the large table into a list of smaller tables 0~N
		+ 0~N-1 th tables are all stored in disk in sorted order as File 0 ~ File N-1.
		+ Nth table is stored in disk unsorted as File N.
	- Have a in-memory table mapping mapping tables/files to its address.
* Write: O(1)
	- Write directly goes to the Nth table/file.
	- If the Nth table is full, sort it and write it to disk. And then create a new table/file.
* Read: O(n)
	- Linearly scan through the Nth table.  
	- If cannot find, perform binary search on N-1, N-2, ..., 0th. 

### Store the Nth table/file in memory
* Disk-based approach vs in-memory approach
	- Disk-based approach: All data Once disk reading + disk writing + in-memory sorting
	- In-memory approach: All data Once disk writing + in-memory sorting
* What if memory is lost?
	- Problem: Nth in memory table is lost. 
	- Write ahead log / WAL: The WAL is the lifeline that is needed when disaster strikes. Similar to a BIN log in MySQL it records all changes to the data. This is important in case something happens to the primary storage. So if the server crashes it can effectively replay that log to get everything up to where the server should have been just before the crash. It also means that if writing the record to the WAL fails the whole operation must be considered a failure. Have a balance between between latency and durability.

### Save disk space
* Consume too much disk space due to repetitive entries (Key, Value)
	- Have a background process doing K-way merge for the sorted tables regularly

### Optimize read
#### Optimize read with index
* Each sorted table should have an index inside memory. 
	- The index is a sketch of key value pairs
* More advanced way to build index with B tree. 

#### Optimize read with Bloom filter
* Each sorted table should have a bloomfilter inside memory. 
* Accuracy of bloom filter
	- Number of hash functions
	- Length of bit vector
	- Number of stored entries


### Standalone final solution
#### Read process
1. First check the (Key, Value) pair inside memory.
2. Check the bloom filter for each file and decide which file might have this key.
3. Use the index to find the value for the key. 
4. Read and return key, value pair

#### Write process
1. Write directly goes to the last table in memory
2. If the Nth table is full, sort it and write it to disk. At the same time create index and bloom filter for it. 
3. Then create a new table/file.

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


