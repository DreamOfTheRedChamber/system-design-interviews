
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



## Lookup service
### Features
* How big is the data
	- Key ( Latitude 37.40, Longtitude -122.09 )
		+ Each key size < 20B
		+ Total key size = 200GB
	- Value ( pic and all the building name on this pic )
		+ Each value size = 100KB
		+ Total value size = 1PB

### Services
* App client + Web servers + Storage service

### Storage
#### Initial solution
* Hashmap
	- Only in memory
* Database (SQL, noSQL)
	- Good but no perfect
	- Usually optimized for writing
* GFS
	- Cannot support key, value lookup

#### How to support lookup for files in one disk
##### Architecture
* Only a single file sorted by key stored in GFS
* Memory: index and file address.
* Chunk index table (Key, Chunk index)
	- Given a key How do we know which chunk we should read
	- 20B * 10 billion = 200G. Can be stored inside memory.

##### Read optimization
1. Cache

##### Read process
1. Check index for the given key
2. Binary search within the file

#### Distributed lookup
##### Master slave
* Master has consistent hashmap
	- Shard the key according to latitude/longtitude
	- Actual do not need the master because consistent hashmap could be stored directly in the web server.
* Slave

##### Final read process
1. Client sends lookup request key K to web server. 
2. Web server checks its local consistent hashmap and finds the slave server Id.
3. Web server sends the request key K to the slave server. 
4. Slave server looks up its chunk table (Key, Chunk) table by with Key K and get the chunk index. 
5. Slave server checks the cache to see whether the specific chunk is already inside the cache. 
6. If not inside the cache, the slave server asks the specific chunk from GFS by chunk index.   



