
<!-- MarkdownTOC -->

- [Amazon Dynamo](#amazon-dynamo)
	- [Data distribution - consistent hashing](#data-distribution---consistent-hashing)
	- [Replication protocol - Replicated write protocol](#replication-protocol---replicated-write-protocol)
	- [Data consistency - Vector clock](#data-consistency---vector-clock)
	- [Temporary failure - hinted handoff](#temporary-failure---hinted-handoff)
	- [permanent failure - merkle tree](#permanent-failure---merkle-tree)
	- [Membership and failure detection - gossip protocol](#membership-and-failure-detection---gossip-protocol)
- [LevelDB](#leveldb)
	- [Architecture](#architecture)
		- [Memory](#memory)
		- [Storage](#storage)
			- [SSTable \(Sorted String Table\)](#sstable-sorted-string-table)
			- [Write ahead log](#write-ahead-log)
		- [Compact thread](#compact-thread)
			- [Conditions for compaction](#conditions-for-compaction)
			- [Minor compaction](#minor-compaction)
			- [Major compaction](#major-compaction)
	- [Write process](#write-process)
		- [Downsides of B+ tree](#downsides-of-b-tree)
		- [LSM tree](#lsm-tree)
		- [LSM tree write process](#lsm-tree-write-process)
		- [Memtable format](#memtable-format)
	- [Read process](#read-process)
		- [SStable format](#sstable-format)
	- [Cache](#cache)
	- [MVCC](#mvcc)
		- [Recover process](#recover-process)
		- [Repair process](#repair-process)
- [Big table](#big-table)
		- [Features](#features)
		- [Services](#services)
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
				- [Write process](#write-process-1)
		- [Scale](#scale)
			- [Master slave model](#master-slave-model)
				- [Read process](#read-process-2)
				- [Write process](#write-process-2)
			- [Too much data to store on slave local disk](#too-much-data-to-store-on-slave-local-disk)
				- [Read/Write process](#readwrite-process)
			- [Race condition](#race-condition)
				- [Read process](#read-process-3)
				- [Write process](#write-process-3)
- [Reference:](#reference)

<!-- /MarkdownTOC -->

# Amazon Dynamo
## Data distribution - consistent hashing
## Replication protocol - Replicated write protocol
## Data consistency - Vector clock
## Temporary failure - hinted handoff
## permanent failure - merkle tree
## Membership and failure detection - gossip protocol


# LevelDB
## Architecture
![levelDB architecture](./images/leveldb_architecture.png)

### Memory

### Storage
#### SSTable (Sorted String Table)
#### Write ahead log
* log format
	- WAL log
	- block
	- record

![levelDB log format](./images/leveldb_logFormat.png)

![levelDB log format 2](./images/leveldb_logFormat2.png)

* log record payload
	- writebatch
	- put_op
	- delete_op

![levelDB log format](./images/leveldblogrecordformat.jpg)

### Compact thread
#### Conditions for compaction


#### Minor compaction
1. turn immutable memtable dump into sstable

![levelDB minor compaction](./images/leveldb_compaction_minor.jpg)

2. Determine the level of sstable

![levelDB sstable level](./images/leveldb_compaction_sstable_level.jpg)

#### Major compaction

## Write process

### Downsides of B+ tree
![levelDB BPlus tree](./images/leveldb_BPlusTree.jpg)

### LSM tree

![levelDB lsm tree](./images/leveldb_lsmtree.jpg)

### LSM tree write process
* Write steps:

### Memtable format

![levelDB memtable format](./images/leveldb_memtableformat.jpg)

## Read process

![levelDB read operation](./images/leveldb_readoperation.jpg)

### SStable format

![levelDB sstable format](./images/leveldb_sstableformat.jpg)

![levelDB sstable format v2](./images/leveldb_sstableformatv2.png)

## Cache

![levelDB cache](./images/leveldb_lrucache.jpg)

## MVCC

![levelDB manifest format](./images/leveldb_manifestformat.jpg)

![levelDB manifest format V2](./images/leveldb_manifestformatv2.jpg)

### Recover process

![levelDB manifest recover process](./images/leveldb_mvcc_recoverprocess.jpg)

### Repair process

![levelDB manifest repair process](./images/leveldb_mvcc_repairprocess.jpg)


# Big table
### Features
* Read or write intensive
	- Whether to optimize read operations
* Large amounts of data
	- Whether needs sharding

### Services
* value get(Key)
* set(key, value)
	- Modify existing entry (key, value)
	- Create new entry (key, value)

### Storage
#### Initial design
* Sorted file with (Key, Value) entries
	- Disk-based binary search based read O(lgn)
	- Linear read operations write O(n)
* Unsorted file with (Key, Value) entries
	- Linear read operations O(n)
	- Constant time write O(1)

#### Balance read/write complexity
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

#### Store the Nth table/file in memory
* Disk-based approach vs in-memory approach
	- Disk-based approach: All data Once disk reading + disk writing + in-memory sorting
	- In-memory approach: All data Once disk writing + in-memory sorting
* What if memory is lost?
	- Problem: Nth in memory table is lost. 
	- Write ahead log / WAL: The WAL is the lifeline that is needed when disaster strikes. Similar to a BIN log in MySQL it records all changes to the data. This is important in case something happens to the primary storage. So if the server crashes it can effectively replay that log to get everything up to where the server should have been just before the crash. It also means that if writing the record to the WAL fails the whole operation must be considered a failure. Have a balance between between latency and durability.

#### Save disk space
* Consume too much disk space due to repetitive entries (Key, Value)
	- Have a background process doing K-way merge for the sorted tables regularly

#### Optimize read
##### Optimize read with index
* Each sorted table should have an index inside memory. 
	- The index is a sketch of key value pairs
* More advanced way to build index with B tree. 

##### Optimize read with Bloom filter
* Each sorted table should have a bloomfilter inside memory. 
* Accuracy of bloom filter
	- Number of hash functions
	- Length of bit vector
	- Number of stored entries

#### Standalone final solution
##### Terminologies
* In-memory table: In-memory skip list
* 1~N-1th disk-based tables: Sstable
* Tablet server: Slave server


##### Read process
1. First check the Key inside in-memory skip list.
2. Check the bloom filter for each file and decide which file might have this key.
3. Use the index to find the value for the key.
4. Read and return key, value pair.

##### Write process
1. Record the write operation inside write ahead log.
2. Write directly goes to the in-memory skip list.
3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
4. Then create a new table/file.

### Scale
#### Master slave model
* Master has the hashmap [Key, server address]
* Slave is responsible for storing data

##### Read process
1. Client sends request of reading Key K to master server. 
2. Master returns the server index by checking its consistent hashmap.
3. Client sends request of Key to slave server. 
	1. First check the Key pair inside memory.
	2. Check the bloom filter for each file and decide which file might have this key.
	3. Use the index to find the value for the key. 
	4. Read and return key, value pair

##### Write process
1. Clients send request of writing pair K,V to master server.
2. Master returns the server index
3. Clients send request of writing pair K,V to slave server. 
	1. Slave records the write operation inside write ahead log.
	2. Slave writes directly go to the in-memory skip list.
	3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
	4. Then create a new table/file.

#### Too much data to store on slave local disk
* Replace local disk with GFS for
	- Disk size
	- Replica 
	- Failure and recovery
* Write ahead log and SsTable are all stored inside GFS.
	- How to write SsTable to GFS
		+ Divide SsTable into multiple chunks (64MB) and store each chunk inside GFS.

##### Read/Write process
* GFS is added as an additional layer

#### Race condition
* Master server also has a distributed lock (such as Chubby/Zookeeper)
* Distributed lock 
	- Consistent hashmap is stored inside the lock server

##### Read process
1. Client sends request of reading Key K to master server. 
2. Master server locks the key. Returns the server index by checking its consistent hashmap.
3. Client sends request of Key to slave server. 
	1. First check the Key pair inside memory.
	2. Check the bloom filter for each file and decide which file might have this key.
	3. Use the index to find the value for the key. 
	4. Read and return key, value pair
4. Read process finishes. Slave notifies the client. 
5. The client notifies the master server to unlock the key. 

##### Write process
1. Clients send request of writing pair K,V to master server.
2. Master server locks the key. Returns the server index. 
3. Clients send request of writing pair K,V to slave server. 
	1. Slave records the write operation inside write ahead log.
	2. Slave writes directly go to the in-memory skip list.
	3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
	4. Then create a new table/file.
4. Write process finishes. Slave notifies the client.
5. The client notifies the master server to unlock the key. 

# Reference: 
1. using level DB and Rocks DB as an example - https://soulmachine.gitbooks.io/system-design/content/cn/key-value-store.html
2. Meituan build on top of tair and redis - https://tech.meituan.com/2020/07/01/kv-squirrel-cellar.html
3. Series of blog on key value store - http://codecapsule.com/2012/11/07/ikvs-implementing-a-key-value-store-table-of-contents/
4. MIT spring 2018. Final course on KV store - http://nil.csail.mit.edu/6.824/2018/projects.html
5. Raft-based implementation 极客时间：https://time.geekbang.org/column/article/217049
6. Taobao tair: https://time.geekbang.org/column/article/217049
7. LevelDB: 
  * Basic: https://zhuanlan.zhihu.com/p/51358869
  * Read write process: https://zhuanlan.zhihu.com/p/51360281
  * Cache mechanism: https://zhuanlan.zhihu.com/p/51573464
  * Compaction design: https://zhuanlan.zhihu.com/p/51573929
  * MVCC: https://zhuanlan.zhihu.com/p/51858206
  * https://soulmachine.gitbooks.io/system-design/content/cn/key-value-store.html
  * https://zhuanlan.zhihu.com/p/80684560
  * https://developer.aliyun.com/article/618109
8. Disk IO
  * https://medium.com/databasss/on-disk-io-part-1-flavours-of-io-8e1ace1de017
