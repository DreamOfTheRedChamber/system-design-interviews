# Database system

<!-- MarkdownTOC -->

- [Lookup service](#lookup-service)
	- [Features](#features)
	- [Services](#services)
	- [Storage](#storage)
		- [Initial solution](#initial-solution)
		- [How to support lookup for files in one disk](#how-to-support-lookup-for-files-in-one-disk)
			- [Architecture](#architecture)
			- [Read optimization](#read-optimization)
			- [Read process](#read-process)
		- [Distributed lookup](#distributed-lookup)
			- [Master slave](#master-slave)
			- [Final read process](#final-read-process)
- [Big table](#big-table)
	- [Features](#features-1)
	- [Services](#services-1)
	- [Storage](#storage-1)
		- [Initial design](#initial-design)
		- [Balance read/write complexity](#balance-readwrite-complexity)
		- [Store the Nth table/file in memory](#store-the-nth-tablefile-in-memory)
		- [Save disk space](#save-disk-space)
		- [Optimize read](#optimize-read)
			- [Optimize read with index](#optimize-read-with-index)
			- [Optimize read with Bloom filter](#optimize-read-with-bloom-filter)
		- [Standalone final solution](#standalone-final-solution)
			- [Terminologies](#terminologies)
			- [Read process](#read-process-1)
			- [Write process](#write-process)
	- [Scale](#scale)
		- [Master slave model](#master-slave-model)
			- [Read process](#read-process-2)
			- [Write process](#write-process-1)
		- [Too much data to store on slave local disk](#too-much-data-to-store-on-slave-local-disk)
			- [Read/Write process](#readwrite-process)
		- [Race condition](#race-condition)
			- [Read process](#read-process-3)
			- [Write process](#write-process-2)

<!-- /MarkdownTOC -->

# Lookup service
## Features
* How big is the data
	- Key ( Latitude 37.40, Longtitude -122.09 )
		+ Each key size < 20B
		+ Total key size = 200GB
	- Value ( pic and all the building name on this pic )
		+ Each value size = 100KB
		+ Total value size = 1PB

## Services
* App client + Web servers + Storage service

## Storage
### Initial solution
* Hashmap
	- Only in memory
* Database (SQL, noSQL)
	- Good but no perfect
	- Usually optimized for writing
* GFS
	- Cannot support key, value lookup

### How to support lookup for files in one disk
#### Architecture
* Only a single file sorted by key stored in GFS
* Memory: index and file address.
* Chunk index table (Key, Chunk index)
	- Given a key How do we know which chunk we should read
	- 20B * 10 billion = 200G. Can be stored inside memory.

#### Read optimization
1. Cache

#### Read process
1. Check index for the given key
2. Binary search within the file

### Distributed lookup
#### Master slave
* Master has consistent hashmap
	- Shard the key according to latitude/longtitude
	- Actual do not need the master because consistent hashmap could be stored directly in the web server.
* Slave

#### Final read process
1. Client sends lookup request key K to web server. 
2. Web server checks its local consistent hashmap and finds the slave server Id.
3. Web server sends the request key K to the slave server. 
4. Slave server looks up its chunk table (Key, Chunk) table by with Key K and get the chunk index. 
5. Slave server checks the cache to see whether the specific chunk is already inside the cache. 
6. If not inside the cache, the slave server asks the specific chunk from GFS by chunk index.   

# Big table
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
#### Terminologies
* In-memory table: In-memory skip list
* 1~N-1th disk-based tables: Sstable
* Tablet server: Slave server

#### Read process
1. First check the Key inside in-memory skip list.
2. Check the bloom filter for each file and decide which file might have this key.
3. Use the index to find the value for the key.
4. Read and return key, value pair.

#### Write process
1. Record the write operation inside write ahead log.
2. Write directly goes to the in-memory skip list.
3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
4. Then create a new table/file.

## Scale
### Master slave model
* Master has the hashmap [Key, server address]
* Slave is responsible for storing data

#### Read process
1. Client sends request of reading Key K to master server. 
2. Master returns the server index by checking its consistent hashmap.
3. Client sends request of Key to slave server. 
	1. First check the Key pair inside memory.
	2. Check the bloom filter for each file and decide which file might have this key.
	3. Use the index to find the value for the key. 
	4. Read and return key, value pair

#### Write process
1. Clients send request of writing pair K,V to master server.
2. Master returns the server index
3. Clients send request of writing pair K,V to slave server. 
	1. Slave records the write operation inside write ahead log.
	2. Slave writes directly go to the in-memory skip list.
	3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
	4. Then create a new table/file.

### Too much data to store on slave local disk
* Replace local disk with GFS for
	- Disk size
	- Replica 
	- Failure and recovery
* Write ahead log and SsTable are all stored inside GFS.
	- How to write SsTable to GFS
		+ Divide SsTable into multiple chunks (64MB) and store each chunk inside GFS.

#### Read/Write process
* GFS is added as an additional layer

### Race condition
* Master server also has a distributed lock (such as Chubby/Zookeeper)
* Distributed lock 
	- Consistent hashmap is stored inside the lock server

#### Read process
1. Client sends request of reading Key K to master server. 
2. Master server locks the key. Returns the server index by checking its consistent hashmap.
3. Client sends request of Key to slave server. 
	1. First check the Key pair inside memory.
	2. Check the bloom filter for each file and decide which file might have this key.
	3. Use the index to find the value for the key. 
	4. Read and return key, value pair
4. Read process finishes. Slave notifies the client. 
5. The client notifies the master server to unlock the key. 

#### Write process
1. Clients send request of writing pair K,V to master server.
2. Master server locks the key. Returns the server index. 
3. Clients send request of writing pair K,V to slave server. 
	1. Slave records the write operation inside write ahead log.
	2. Slave writes directly go to the in-memory skip list.
	3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
	4. Then create a new table/file.
4. Write process finishes. Slave notifies the client.
5. The client notifies the master server to unlock the key. 


