
<!-- MarkdownTOC -->

- [Design a read-intensive key value store](#design-a-read-intensive-key-value-store)
- [Design a write-intensive key value store](#design-a-write-intensive-key-value-store)
	- [Features](#features)
		- [Functional features](#functional-features)
	- [P2P approach](#p2p-approach)
		- [Data distribution - consistent hashing](#data-distribution---consistent-hashing)
			- [Design thoughts](#design-thoughts)
			- [Complexity analysis](#complexity-analysis)
		- [Consensus](#consensus)
		- [Conflict resolution - Vector clock](#conflict-resolution---vector-clock)
		- [Failure recover](#failure-recover)
			- [Temporary failure - hinted hand-off](#temporary-failure---hinted-hand-off)
			- [Permanent failure - Merkle tree](#permanent-failure---merkle-tree)
		- [Membership and failure detection - gossip protocol](#membership-and-failure-detection---gossip-protocol)
	- [Centralized approach](#centralized-approach)
		- [Standalone solution](#standalone-solution)
			- [Design thoughts](#design-thoughts-1)
			- [Initial design flow chart](#initial-design-flow-chart)
				- [Read process](#read-process)
				- [Write process](#write-process)
				- [Pros](#pros)
				- [Cons](#cons)
			- [LSM tree](#lsm-tree)
				- [Read-optimization: Compaction](#read-optimization-compaction)
					- [Minor compaction](#minor-compaction)
					- [Major compaction](#major-compaction)
				- [Read steps](#read-steps)
		- [Multi-machine](#multi-machine)
			- [Design thoughts](#design-thoughts-2)
			- [Flow chart](#flow-chart)
				- [Read process](#read-process-1)
				- [Write process](#write-process-1)
			- [Data migration ???](#data-migration-)
- [Reference:](#reference)

<!-- /MarkdownTOC -->

# Design a read-intensive key value store

# Design a write-intensive key value store
## Features
### Functional features
* API
	- value get(Key)
	- set(key, value)
		+ Modify existing entry (key, value)
		+ Create new entry (key, value)
* Index support

## P2P approach 
### Data distribution - consistent hashing
#### Design thoughts
1. Normal hashing algorithm
	- Cons: Too many items to migrate during resharding
2. Consistent hashing 
	- Cons: Uneven load during scale up/down
3. Consistent hashing with virtual nodes:

#### Complexity analysis
* Assume the total number of data M, the total number of nodes N
* Read/write complexity increases from O(1）to O(lgn) When compared with traditional hashing because consistent hashing read/write steps are as follow: 
	1. Convert hashkey into 32 bit int number. O(1)
	2. Use binary search to find the corresponding node. O(lgn)
* Data migration complexity decreases from O(m) to O(m/N). 

* References: 
	- Data distributed in multiDC: https://www.onsip.com/voip-resources/voip-fundamentals/intro-to-cassandra-and-networktopologystrategy
	- Consistent hashing in Cassandra documentation: https://cassandra.apache.org/doc/latest/architecture/dynamo.html

### Consensus 
* Overview of consensuse protocol
	- Strong consistency
		- Paxos
		- Raft (the most popular consensus algorithm)
		- ZAB
	- Weak consistency
		- Gossip
	- Tunable consistency - Quorum NWR
		* Definition:
			- N: 
			- W: 
			- R: 
		* If W+R > N, could guarantee strong consistency
		* If W+R <=N,  could only guarantee eventual consistency
* Dynamo DB adopted tunable consistency

### Conflict resolution - Vector clock
* Pros of vector clock:
	- Not requiring clock synchronization across all nodes, and helps us identify transactions that might be in conflict.
* References:
	- http://guyharrison.squarespace.com/blog/2015/10/12/vector-clocks.html
	- UIUC week 4's series: https://www.coursera.org/learn/cloud-computing/lecture/7zWzq/2-1-introduction-and-basics

### Failure recover
#### Temporary failure - hinted hand-off
* Hinted handoff
	* https://cassandra.apache.org/doc/latest/operating/hints.html
	* https://docs.scylladb.com/architecture/anti-entropy/hinted-handoff/
* References: 
	* Read repair - https://docs.scylladb.com/architecture/anti-entropy/read-repair/
	* Others
		- Incremental repair: https://www.datastax.com/blog/2014/02/more-efficient-repairs-21
		- Advanced repair: https://www.datastax.com/blog/2013/07/advanced-repair-techniques

#### Permanent failure - Merkle tree
* https://www.codementor.io/blog/merkle-trees-5h9arzd3n8

### Membership and failure detection - gossip protocol
* Overview of dissemination protocol
	- Multicast/Broadcast: 
		- Usually disabled in cloud network env
	- Gossip
		- Pros: No single point of failure
		- Cons: High transmission cost
	- SWIM protocol https://www.brianstorti.com/swim/
* References: 
	- Gossip protocol data structure https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49
	- https://www.coursera.org/lecture/cloud-computing/1-1-multicast-problem-G75ld
	- https://www.coursera.org/lecture/cloud-computing/1-2-the-gossip-protocol-5AOex
	- UIUC disemination protocols: https://www.coursera.org/lecture/cloud-computing/2-6-dissemination-and-suspicion-OQF73

## Centralized approach
### Standalone solution
#### Design thoughts
1. Sorted file with (Key, Value) entries
	- Disk-based binary search based read O(lgn)
	- Linear read operations write O(n)
2. Unsorted file with (Key, Value) entries. Then build index on top of it. 
	- Linear read operations O(n)
	- Constant time write O(1)
3. Combine append-only write and binary search read
	- Process: 
		- Break the large table into a list of smaller tables 0 to N
			+ 0 to N-1 th tables are all stored in disk in sorted order as File 0 to File N-1.
			+ Nth table is stored in disk unsorted as File N.
		- Have a in-memory table mapping mapping tables/files to its address.
	- Write: O(1)
		- Write directly goes to the Nth table/file.
		- If the Nth table is full, sort it and write it to disk. And then create a new table/file.
	- Read: O(n)
		- Linearly scan through the Nth table.  
		- If cannot find, perform binary search on N-1, N-2, ..., 0th. 
4. Store the Nth table/file in memory
	* Disk-based approach vs in-memory approach
		- Disk-based approach: All data Once disk reading + disk writing + in-memory sorting
		- In-memory approach: All data Once disk writing + in-memory sorting
	* What if memory is lost?
		- Problem: Nth in memory table is lost. 
		- Write ahead log / WAL: The WAL is the lifeline that is needed when disaster strikes. Similar to a BIN log in MySQL it records all changes to the data. This is important in case something happens to the primary storage. So if the server crashes it can effectively replay that log to get everything up to where the server should have been just before the crash. It also means that if writing the record to the WAL fails the whole operation must be considered a failure. Have a balance between between latency and durability.

5. Further optimization
	- Write: How to Save disk space. Consume too much disk space due to repetitive entries (Key, Value)
		+ Have a background process doing K-way merge for the sorted tables regularly
	- Read: 
		+ Optimize read with index
			* Each sorted table should have an index inside memory. 
				- The index is a sketch of key value pairs
			* More advanced way to build index with B tree. 
		+ Optimize read with Bloom filter
			* Each sorted table should have a bloomfilter inside memory. 
			* Accuracy of bloom filter
				- Number of hash functions
				- Length of bit vector
				- Number of stored entries

#### Initial design flow chart

```
      ┌─────────────────────────────┐              ┌─────────────────────────┐         
      │Read tries to find the entry │              │                         │         
      │in the following order:      │              │Write directly happens to│         
      │1. in-memory sorted list     │              │  in-memory sorted list  │         
      │2. If not found, then search │              │                         │         
      │the in-disk sorted list in   │              │                         │         
      │reverse chronological order -│              └──────┬─────────▲────────┘         
      │newer ones first, older ones │                     │         │                  
      │later                        │                     │         │                  
      └─────────────────────────────┘                     │         │                  
                                                          │         │                  
                                                          │         │                  
┌─────────────────────────────────────────────────────────┼─────────┼─────────────────┐
│                                     Data Server         │         │                 │
│                                                         │         │                 │
│   ┌─────────────────────────────────────────────────────┼─────────┼──────────┐      │
│   │                          In-Memory sorted list      │         │          │      │
│   │                                                     │         │          │      │
│   │                               key1, value1          ▼         │          │      │
│   │                               key2, value2                               │      │
│   │                                   ...                                    │      │
│   │                               keyN, valueN                               │      │
│   └──────────────────────────────────────────────────────────────────────────┘      │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│   ┌────────────┐   ┌────────────┐  ┌────────────┐   ┌────────────┐  ┌────────────┐  │
│   │bloom filter│   │bloom filter│  │            │   │bloom filter│  │bloom filter│  │
│   │and index 1 │   │and index 2 │  │   ......   │   │ and index  │  │and index N │  │
│   │            │   │            │  │            │   │    N-1     │  │            │  │
│   └────────────┘   └────────────┘  └────────────┘   └────────────┘  └────────────┘  │
│                                                                                     │
│   ┌────────────┐   ┌────────────┐  ┌────────────┐   ┌────────────┐  ┌────────────┐  │
│   │            │   │            │  │            │   │            │  │            │  │
│   │  In-disk   │   │  In-disk   │  │            │   │  In-disk   │  │  In-disk   │  │
│   │sorted list │   │sorted list │  │   ......   │   │sorted list │  │sorted list │  │
│   │     1      │   │     2      │  │            │   │    N-1     │  │     N      │  │
│   │            │   │            │  │            │   │            │  │            │  │
│   │            │   │            │  │            │   │            │  │            │  │
│   └────────────┘   └────────────┘  └────────────┘   └────────────┘  └────────────┘  │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                                                                       
                                                                                       
    ┌─────┐                                                               ┌─────┐      
   ─┤older├─────────────────────Chronological order───────────────────────┤newer├─────▶
    └─────┘                                                               └─────┘      
```

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

##### Pros
* Optimized for write: Write only happens to in-memory sorted list

##### Cons
* In the worst case, read needs to go through a chain of units (in-memory, in-disk N, ..., in-disk 1)
	- Compaction could help reduce the problem

#### LSM tree
* Motivation: Optimize read further
* Take LevelDB's LSM tree implementation as an example here
	* Having a Log-Structured Merge-Tree architecture, LevelDB does not store data directly in SST files. It stores new key/value pairs mutations (either additions or deletions) in a log file. This log file is the file with the .log suffix in your leveldb directory. The data stored in the log file is also stored in a memory structure called the memtable.

	* When the log file reaches a certain size (around 4 MB), its content is transferred to a new SST file and a new log file and memtable are initiated, the previous memtable is discarded. Those fresh SST files are collectively called the level 0 files. Files at level 0 are special because their keys can overlap since they are simply copies of the various log files.

	* When the number of files at level 0 reaches a threshold (around 10 files), a compaction is triggered. Compaction will chose a set of overlapping files at level 0 and a set of files they overlap at the next level (level 1) and will merge the data in those files to create a new set of SST files at level 1. The chosen SST files are then discarded.

	* LevelDB continuously inspects files at each level and triggers compactions when the number of files or the total size at a given level goes beyond a set threshold. LevelDB manages 7 levels of files. The list of current SST files is kept in a MANIFEST file. The id of the current MANIFEST file is stored in the CURRENT file. 

	* When reading data, the set of SST files to access is retrieved from the data in the MANIFEST and the required files are opened and the data to read is reconciled from those files and the current memtable, managing overwrites and deletions.

![levelDB architecture](./images/leveldb_architecture.jpg)

##### Read-optimization: Compaction
###### Minor compaction
* Definition: The process of turning immutable memtable dump into sstable. During this process SSTable will be pushed as further down as possible if
	- No overlap with current level
	- Overlapping with no more than 10 SSTs in the next level

* Trigger for minor compaction:
	- When write new data into level DB, if the current memtable >= default buffer size (4M)

* Steps: 
	1. Convert memtable into sstable format
	2. Determine the level of the new sstable
	3. Put sstable into the selected level

![levelDB minor compaction](./images/leveldb_compaction_minor.jpg)

![levelDB sstable level](./images/leveldb_compaction_sstable_level.jpg)

###### Major compaction
* Definition: Merge SSTable in different layers
* Categories:
	- Manual compaction
	- Size compaction: There is a threshold on the size of each level
	- Seek compaction: Each 

##### Read steps
1. Gets the value from memtable
2. If not found within memtable, tries to find it within immutable memtable. 
3. Look inside sstable
	- On level L0, search through each SStable
	- On L1 and up, all sstable is non-overlapped. 

![levelDB read process](./images/leveldb_readoperation.jpg)

### Multi-machine 
#### Design thoughts
1. Master slave model
	* Master has the hashmap [Key, server address]
	* Slave is responsible for storing data
	* Read process
		1. Client sends request of reading Key K to master server. 
		2. Master returns the server index by checking its consistent hashmap.
		3. Client sends request of Key to slave server. 
			1. First check the Key pair inside memory.
			2. Check the bloom filter for each file and decide which file might have this key.
			3. Use the index to find the value for the key. 
			4. Read and return key, value pair
	* Write process
		1. Clients send request of writing pair K,V to master server.
		2. Master returns the server index
		3. Clients send request of writing pair K,V to slave server. 
			1. Slave records the write operation inside write ahead log.
			2. Slave writes directly go to the in-memory skip list.
			3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
			4. Then create a new table/file.

2. How to handle race condition
	* Master server also has a distributed lock (such as Chubby/Zookeeper)
	* Distributed lock 
		- Consistent hashmap is stored inside the lock server

3. (Optional) Too much data to store on slave local disk
	* Replace local disk with distributed file system (e.g. GFS) for
		- Disk size
		- Replica 
		- Failure and recovery
	* Write ahead log and SsTable are all stored inside GFS.
		- How to write SsTable to GFS
			+ Divide SsTable into multiple chunks (64MB) and store each chunk inside GFS.

4. Config server will easily become single point of failure
	* Client could cache the routing table

#### Flow chart
* The dashboard lines means these network calls could be avoided if the routing table is cached on client. 

```
                                         ┌─────────────────────────────────┐      
                                         │          Config server          │      
 ┌────────────────────┐ ─ ─ ─step5─ ─ ─▶ │   (where routing table stays)   │      
 │       Client       │                  │                                 │      
 │  ┌──────────────┐  │                  │ ┌───────────┐     ┌───────────┐ │      
 │  │cache of      │  │                  │ │           │     │           │ │      
 │  │routing table │  │  ─ ─Step1─ ─ ─ ▶ │ │  Master   │     │   Slave   │ │      
 │  └──────────────┘  │                  │ │           │     │           │ │      
 └────────────────────┘ ◀─ ─ ─ Step3 ─ ─ │ └───────────┘     └───────────┘ │      
            │                            └─────────────────────────────────┘      
            │                                           │      │                  
            │                                                                     
            │                                          Step2  step 6              
            │                                                                     
            │                                           ▼      ▼                  
            └─────────Step4─────────────────────┐  ┌──────────────┐               
                                                │  │ Distributed  │               
                                                │  │     lock     │       ─       
                                                │  └──────────────┘               
                                                │                                 
                                                │                                 
                                                ▼                                 
┌───────────────┐     ┌───────────────┐    ┌───────────────┐     ┌───────────────┐
│ Data server 1 │     │ Data server 2 │    │               │     │ Data server N │
│               │     │               │    │               │     │               │
│┌────────────┐ │     │┌────────────┐ │    │    ......     │     │┌────────────┐ │
││in-memory   │ │     ││in-memory   │ │    │               │     ││in-memory   │ │
││sorted list │ │     ││sorted list │ │    │               │     ││sorted list │ │
│└────────────┘ │     │└────────────┘ │    └───────────────┘     │└────────────┘ │
│┌────────────┐ │     │┌────────────┐ │                          │┌────────────┐ │
││in-disk     │ │     ││in-disk     │ │                          ││in-disk     │ │
││sorted list │ │     ││sorted list │ │                          ││sorted list │ │
││1 and bloom │ │     ││1 and bloom │ │                          ││1 and bloom │ │
││filter/index│ │     ││filter/index│ │                          ││filter/index│ │
│└────────────┘ │     │└────────────┘ │                          │└────────────┘ │
│┌────────────┐ │     │┌────────────┐ │                          │┌────────────┐ │
││......      │ │     ││......      │ │                          ││......      │ │
│└────────────┘ │     │└────────────┘ │                          │└────────────┘ │
│┌────────────┐ │     │┌────────────┐ │                          │┌────────────┐ │
││in-disk     │ │     ││in-disk     │ │                          ││in-disk     │ │
││sorted list │ │     ││sorted list │ │                          ││sorted list │ │
││N and bloom │ │     ││N and bloom │ │                          ││N and bloom │ │
││filter/index│ │     ││filter/index│ │                          ││filter/index│ │
│└────────────┘ │     │└────────────┘ │                          │└────────────┘ │
└───────────────┘     └───────────────┘                          └───────────────┘
```

##### Read process
1. Step1: Client sends request of reading Key K to master server. 
2. Step2/3: Master server locks the key. Returns the server index by checking its consistent hashmap.
3. Step4: Client sends request of Key to slave server. 
	1. First check the Key pair inside memory.
	2. Check the bloom filter for each file and decide which file might have this key.
	3. Use the index to find the value for the key. 
	4. Read and return key, value pair
	5. Read process finishes. Slave notifies the client. 
4. Step5: The client notifies the master server to unlock the key. 
5. Step6: Master unlocks the key

##### Write process
1. step1: Clients send request of writing pair K,V to master server.
2. step2/3: Master server locks the key. Returns the server index. 
3. Step4: Clients send request of writing pair K,V to slave server. 
	1. Slave records the write operation inside write ahead log.
	2. Slave writes directly go to the in-memory skip list.
	3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
	4. Then create a new table/file.
	5. Write process finishes. Slave notifies the client.
4. Step5: The client notifies the master server to unlock the key. 
5. Step6: Master unlocks the key

#### Data migration ??? 

# Reference: 
1. using level DB and Rocks DB as an example - https://soulmachine.gitbooks.io/system-design/content/cn/key-value-store.html
2. Meituan build on top of tair and redis - https://tech.meituan.com/2020/07/01/kv-squirrel-cellar.html
3. TairDB
	- hot key problem: https://zhuanlan.zhihu.com/p/32743904
	- Tair db in detail: https://www.cnblogs.com/chenny7/p/4875396.html
4. LevelDB: 
	- https://leveldb-handbook.readthedocs.io/zh/latest/basic.html
	- https://zhuanlan.zhihu.com/p/51360281
5. Disk IO
  * https://medium.com/databasss/on-disk-io-part-1-flavours-of-io-8e1ace1de017
