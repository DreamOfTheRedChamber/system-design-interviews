# File system

<!-- MarkdownTOC -->

- [File system](#file-system)
	- [Scenario](#scenario)
	- [Storage](#storage)
		- [How to save a file in one machine](#how-to-save-a-file-in-one-machine)
		- [How to save a much larger file in one machine](#how-to-save-a-much-larger-file-in-one-machine)
	- [Scale](#scale)
		- [Architecture style](#architecture-style)
		- [How to save an extra large file on several machines](#how-to-save-an-extra-large-file-on-several-machines)
			- [Move chunk offset from master to slaves](#move-chunk-offset-from-master-to-slaves)
		- [Write process](#write-process)
		- [Do not support modification](#do-not-support-modification)
		- [Read process](#read-process)
		- [Master task](#master-task)
		- [Failure and recovery](#failure-and-recovery)
			- [Single master](#single-master)
			- [What if a chunk is broken](#what-if-a-chunk-is-broken)
			- [Avoid loss of data when chunk server is down](#avoid-loss-of-data-when-chunk-server-is-down)
			- [How to recover when a chunk is broken](#how-to-recover-when-a-chunk-is-broken)
			- [How to find whether a chunk server is down](#how-to-find-whether-a-chunk-server-is-down)
			- [How to solve client bottleneck](#how-to-solve-client-bottleneck)
			- [How to solve chunk server failure](#how-to-solve-chunk-server-failure)
- [Lookup service](#lookup-service)
	- [Features](#features)
	- [Services](#services)
	- [Storage](#storage-1)
		- [Initial solution](#initial-solution)
		- [How to support lookup for files in one disk](#how-to-support-lookup-for-files-in-one-disk)
			- [Architecture](#architecture)
			- [Read optimization](#read-optimization)
			- [Read process](#read-process-1)
		- [Distributed lookup](#distributed-lookup)
			- [Master slave](#master-slave)
			- [Final read process](#final-read-process)

<!-- /MarkdownTOC -->


## Scenario
* Write a file
* Read a file
* Use multiple machines to store these files

## Storage
### How to save a file in one machine
* Metadata
	- FileInfo
		+ Name = dengchao.mp4
		+ CreatedTime = 201505031232
		+ Size = 2044323
	- Index
		+ Block 11 -> diskOffset1
		+ Block 12 -> diskOffset2
		+ Block 13 -> diskOffset3
* Block
	- 1 block = 1024 Byte
	- Advantages
		+ Error checking
		+ Fragmenting the data for storage

### How to save a much larger file in one machine 
* Change chunk size
	- 1 chunk = 64M = 64 * 1024K
	- Advantages
		+ Reduce size of metadata
	- Disadvantages
		+ Waste space for small files

## Scale
### Architecture style
* Peer 2 Peer (BitComet, Cassandra)
	- Advantage: No single point of failure
	- Disadvantage: Multiple machines need to negotiate with each other
* Master slave
	- Advantage: Simple design. Easy to keep data consistent
	- Disadvantage: Master is a single point of failure
* Final decision
	- Master + slave
	- Restart the single master

### How to save an extra large file on several machines
* One master + many chunk servers

#### Move chunk offset from master to slaves
* Master don't record the disk offset of a chunk
	- Advantage: Reduce the size of metadata in master; Reduce the traffic between master and chunk server

### Write process
1. The client divides the file into chunks. Create a chunk index for each chunk
2. Send (FileName, chunk index) to master and master replies with assigned chunk servers
3. The client transfer data with the assigned chunk server.

### Do not support modification

### Read process
1. The client sends (FileName) to master and receives a chunk list (chunk index, chunk server) from the master 
2. The client connects with different server for reading files

### Master task
* Store metadata for different files
* Store Map (file name + chunk index -> chunk server)
	- Find corresponding server when reading in data
	- Write to more available chunk server

### Failure and recovery
#### Single master
* Double master (Apache Hadoop Goes Realtime at Facebook)
* Multi master (Paxos algorithm)

#### What if a chunk is broken
* Check sum 4bytes = 32 bit
* Each chunk has a checksum 
* Write checksum when writing out a chunk
* Check checsum when reading in a chunk

#### Avoid loss of data when chunk server is down
* Replica: 3 copies
	- Two copies in the same data center but on different racks
	- Third copy in a different data center
* How to choose chunk servers
	- Find servers which are not busy
	- Find servers with lots of available disk space

#### How to recover when a chunk is broken
* Ask master for help

#### How to find whether a chunk server is down
* Heart beat message

#### How to solve client bottleneck
* Client only writes to a leader chunk server. The leader chunk server is responsible for communicating with other chunk servers. 
* How to select leading slaves

#### How to solve chunk server failure
* Ask the client to retry


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



