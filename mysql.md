
<!-- MarkdownTOC -->

- [MySQL](#mysql)
	- [Architecture](#architecture)
	- [Index](#index)
		- [Data structure](#data-structure)
			- [Balanced binary tree](#balanced-binary-tree)
			- [B Tree](#b-tree)
			- [B+ tree](#b-tree-1)
		- [InnoDB index](#innodb-index)
			- [Clustered index](#clustered-index)
				- [Def](#def)
				- [Pros and cons](#pros-and-cons)
			- [Secondary index](#secondary-index)
				- [Pros and cons](#pros-and-cons-1)
			- [Adaptive hash index](#adaptive-hash-index)
			- [Composite index](#composite-index)
			- [Covered index](#covered-index)
	- [Lock](#lock)
	- [High availability basics](#high-availability-basics)
		- [Master slave delay](#master-slave-delay)
			- [Sources](#sources)
				- [Inferior slave machines](#inferior-slave-machines)
				- [Too much load for slave](#too-much-load-for-slave)
				- [Big transactions](#big-transactions)
				- [Slow slave thread replay](#slow-slave-thread-replay)
					- [Paralelle approaches](#paralelle-approaches)
			- [How to find sync points](#how-to-find-sync-points)
				- [sql_slave_skip_counter](#sql_slave_skip_counter)
				- [slave_skip_errors](#slave_skip_errors)
				- [GTID](#gtid)
		- [Failover strategy](#failover-strategy)
			- [Reliability first](#reliability-first)
			- [Availability first](#availability-first)
				- [Inconsistency when binlog format = raw](#inconsistency-when-binlog-format--raw)
				- [Inconsistency when binlog format = mixed](#inconsistency-when-binlog-format--mixed)
	- [Typical architectures](#typical-architectures)
		- [Dual master](#dual-master)
			- [Asynchronous replication](#asynchronous-replication)
			- [Semi-Synchronous replication](#semi-synchronous-replication)
			- [Group replication](#group-replication)
		- [MMM \(Multi-master replication manager\)](#mmm-multi-master-replication-manager)
		- [MHA \(Master high availability\)](#mha-master-high-availability)
		- [Github/WePay MySQL high availability](#githubwepay-mysql-high-availability)
			- [Master discovery series](#master-discovery-series)
		- [Other solutions](#other-solutions)
			- [MySQL cluster](#mysql-cluster)
			- [Galera](#galera)
			- [PAXOS](#paxos)
			- [Shared storage such as Amazon Aurora](#shared-storage-such-as-amazon-aurora)
	- [Sharding](#sharding)
		- [Choose the number of shards](#choose-the-number-of-shards)
		- [Shard routing strategy](#shard-routing-strategy)
			- [Lookup strategy](#lookup-strategy)
			- [Range strategy](#range-strategy)
				- [By customer or tenant](#by-customer-or-tenant)
				- [By geography](#by-geography)
				- [By time](#by-time)
			- [Hash strategy](#hash-strategy)
				- [By entity id](#by-entity-id)
			- [Shard a graph ???](#shard-a-graph-)
		- [Sharding preferences](#sharding-preferences)
		- [Table sharding](#table-sharding)
			- [Vertical sharding](#vertical-sharding)
			- [Horizontal sharding](#horizontal-sharding)
		- [Database sharding](#database-sharding)
			- [Limitations](#limitations)
				- [Write across shards](#write-across-shards)
				- [Query Cross shard](#query-cross-shard)
				- [Unique global key](#unique-global-key)
				- [Reshard](#reshard)
- [Future readings](#future-readings)

<!-- /MarkdownTOC -->


# MySQL

## Architecture

![Index B Plus tree](./images/mysql_architecture.png)

## Index
* Where to set up index
	* On columns not changing often
	* On columns which have high cardinality
	* Automatically increase id is a good candidate to set up B tree. 
* References: https://www.freecodecamp.org/news/database-indexing-at-a-glance-bb50809d48bd/

### Data structure
#### Balanced binary tree
* Why not balanced binary tree
	- Tree too high which results in large number of IO operations
	- Operating system load items from disk in page size (4k). 

#### B Tree
* How does B Tree solve the above problem
	- Control the height of tree. The number of children is the number of key word - 1. 
	- B tree stores data inside 

![Index B tree](./images/mysql_index_btree.png)

#### B+ tree 
* Pros compared with B tree
	- There is no data field inside non-leaf nodes. So have better IO capability
	- Only needs to look at leaf nodes. So have better range query capability (Does not need to move up and down in a tree

![Index B Plus tree](./images/mysql_index_bPlusTree.png)

### InnoDB index
#### Clustered index
##### Def
* A clustered index is collocated with the data in the same table space or same disk file. You can consider that a clustered index is a B-Tree index whose leaf nodes are the actual data blocks on disk, since the index & data reside together. This kind of index physically organizes the data on disk as per the logical order of the index key.
* Within innoDB, the MySQL InnoDB engine actually manages the primary index as clustered index for improving performance, so the primary key & the actual record on disk are clustered together.
	- When you define a PRIMARY KEY on your table, InnoDB uses it as the clustered index. Define a primary key for each table that you create. If there is no logical unique and non-null column or set of columns, add a new auto-increment column, whose values are filled in automatically.	
	- If you do not define a PRIMARY KEY for your table, MySQL locates the first UNIQUE index where all the key columns are NOT NULL and InnoDB uses it as the clustered index.
	- If the table has no PRIMARY KEY or suitable UNIQUE index, InnoDB internally generates a hidden clustered index named GEN_CLUST_INDEX on a synthetic column containing row ID values. The rows are ordered by the ID that InnoDB assigns to the rows in such a table. The row ID is a 6-byte field that increases monotonically as new rows are inserted. Thus, the rows ordered by the row ID are physically in insertion order.

##### Pros and cons
* Pros: 
	- This ordering or co-location of related data actually makes a clustered index faster. When data is fetched from disk, the complete block containing the data is read by the system since our disk IO system writes & reads data in blocks. So in case of range queries, it’s quite possible that the collocated data is buffered in memory. This is especially true for range queries. 
- Cons: 
	- Since a clustered index impacts the physical organization of the data, there can be only one clustered index per table.

#### Secondary index
* Why secondary key does not directly point to data, instead it needs to point to primary key
	- when there are updates on primary key, it will need to modify all other secondary index if that's the case. 

![Index B tree secondary index](./images/mysql_index_secondaryIndex.png)

##### Pros and cons
* Pros: Logically you can create as many secondary indices as you want. But in reality how many indices actually required needs a serious thought process since each index has its own penalty.
* Cons: 
	- With DML operations like DELETE / INSERT , the secondary index also needs to be updated so that the copy of the primary key column can be deleted / inserted. In such cases, the existence of lots of secondary indexes can create issues.
	- Also, if a primary key is very large like a URL, since secondary indexes contain a copy of the primary key column value, it can be inefficient in terms of storage. More secondary keys means a greater number of duplicate copies of the primary key column value, so more storage in case of a large primary key. Also the primary key itself stores the keys, so the combined effect on storage will be very high.


#### Adaptive hash index

![Index B tree secondary index](./images/mysql_index_adaptiveHashIndex.png)

#### Composite index
* Def: Multiple column builds a single index. MySQL lets you define indices on multiple columns, up to 16 columns. This index is called a Multi-column / Composite / Compound index.
* When you need a composite index
	- Analyze your queries first according to your use cases. If you see certain fields are appearing together in many queries, you may consider creating a composite index.
	- If you are creating an index in col1 & a composite index in (col1, col2), then only the composite index should be fine. col1 alone can be served by the composite index itself since it’s a left side prefix of the index.
	- Consider cardinality. If columns used in the composite index end up having high cardinality together, they are good candidate for the composite index.

#### Covered index
* A covering index is a special kind of composite index where all the columns specified in the query somewhere exist in the index. So the query optimizer does not need to hit the database to get the data — rather it gets the result from the index itself. 


## Lock


## High availability basics
### Master slave delay
#### Sources
* The master-slave latency is defined as the difference between T3 and T1. 
	1. Master DB executes a transaction, writes into binlog and finishes at timestamp T1.
	2. The statement is replicated to binlog, Slave DB received it from the binlog T2.
	3. Slave DB executes the transaction and finishes at timestamp T3. 

##### Inferior slave machines
* Slave machine is insuperior to master

##### Too much load for slave
* Causes: Many analytical queries run on top of slave. 
* Solutions:
	- Multiple slaves
	- Output telemetry to external statistical systems such as Hadoop through binlog 

##### Big transactions
* If a transaction needs to run for as long as 10 minutes on the master database, then it must wait for the transaction to finish before running it on slave. Slave will be behind master for 10 minutes. 
	- e.g. Use del to delete too many records within DB
	- e.g. mySQL DDL within big tables. 

##### Slow slave thread replay

![Master slave replication process](./images/mysql_ha_masterSlaveReplication.png)

![Master slave replication process](./images/mysql_ha_masterSlave_multiThreads.png)

###### Paralelle approaches
* DB based parallel
* Table/Row based parallel
* History
	- MySQL 5.5
	- MySQL 5.6
	- MySQL 5.7
	- MySQL 5.7.22

![Master slave replication process](./images/mysql_ha_masterSlave_multiThreads_distributeTable.png)

![Master slave replication process](./images/mysql_ha_masterSlave_multiThreads_distributeGroupCommit.png)

![Master slave replication process](./images/mysql_ha_masterSlave_multiThreads_distributeMariaDB.png)

#### How to find sync points
##### sql_slave_skip_counter
##### slave_skip_errors
##### GTID

### Failover strategy
#### Reliability first
* After step 2 and before step4 below, both master and slave will be in readonly state. 

```
                  │     │         ┌──────────────────────┐                          
                  │     │         │Step5. Switch traffic │                          
                  │     │         │     from A to B      │                          
                  │     │         └──────────────────────┘                          
                 Requests                                                           
                  │     │                                                           
                  │     │                                                           
                  │     │                                                           
                  ▼     ▼                                                           
                                                                                    
┌────────────────────────────┐                         ┌───────────────────────────┐
│          Master A          │                         │         Master B          │
│ ┌───────────────────────┐  │                         │ ┌───────────────────────┐ │
│ │step2. Change master to│  │                         │ │step1. check           │ │
│ │readonly state         │  │                         │ │seconds_behind_master  │ │
│ └───────────────────────┘  │                         │ │until it is smaller    │ │
│                            │                         │ │than 5 seconds         │ │
│                            │                         │ └───────────────────────┘ │
│                            │                         │ ┌───────────────────────┐ │
│                            │                         │ │step3. wait until      │ │
│                            │                         │ │seconds_behind_master  │ │
│                            │                         │ │to become 0            │ │
└────────────────────────────┘                         │ │                       │ │
                                                       │ └───────────────────────┘ │
                                                       │ ┌───────────────────────┐ │
                                                       │ │step4. change to       │ │
                                                       │ │read/write state       │ │
                                                       │ │instead of readonly    │ │
                                                       │ │                       │ │
                                                       │ └───────────────────────┘ │
                                                       │                           │
                                                       │                           │
                                                       └───────────────────────────┘
```

#### Availability first
* It may result in data inconsistency. Using row format binlog will makes identify data inconsistency problems much easier than mixed or statement based binlog. 

```
                  │     │         ┌──────────────────────┐                          
                  │     │         │Step3. Switch traffic │                          
                  │     │         │     from A to B      │                          
                  │     │         └──────────────────────┘                          
                 Requests                                                           
                  │     │                                                           
                  │     │                                                           
                  │     │                                                           
                  ▼     ▼                                                           
                                                                                    
┌────────────────────────────┐                         ┌───────────────────────────┐
│          Master A          │                         │         Master B          │
│ ┌───────────────────────┐  │                         │                           │
│ │step2. Change master to│  │                         │ ┌───────────────────────┐ │
│ │readonly state         │  │                         │ │step1. change to       │ │
│ └───────────────────────┘  │                         │ │read/write state       │ │
│                            │                         │ │instead of readonly    │ │
│                            │                         │ │                       │ │
│                            │                         │ └───────────────────────┘ │
│                            │                         │                           │
│                            │                         │                           │
│                            │                         │                           │
└────────────────────────────┘                         └───────────────────────────┘
```

##### Inconsistency when binlog format = raw
* [TODO]: understand deeper

![Inconsistency row format binlog](./images/mysql_ha_availabilityfirstRow.png)

##### Inconsistency when binlog format = mixed

![Inconsistency row format mixed](./images/mysql_ha_availabilityfirstMixed.png)

## Typical architectures
* [Standards to select mysql ha solutions](https://www.mysqlha.com/)

```
1. Local Failure handling (Local HA) – Can you recover quickly and automatically from DBMS and host failures?
2. Global Failure handling (Disaster Recovery) – Can you recover quickly and in an automated fashion on full site failures?
3. Zero-down Time Maintenance – Can you upgrade hardware, software, and data without taking applications offline?
4. Load Balancing – Can you effectively split reads to slaves and writes to master, and can you automatically load balance the reads to multiple slaves?
5. Performance – Does the HA/DR solution also improve the overall database performance?
6. Transparency – Can you deploy the HA solution without making changes to applications or migrating data?
7. Multi-Site Operations – Can you handle data spread over multiple data centers using both multi-master as well as primary/backup models?
8. Cloud Readiness – Can you run easily in cloud environments like Amazon Web Services, Google Cloud, or OpenStack?
9. Hardware Utilization – Does the HA/DR solution increase application throughput and hardware utilization through active replicas?
10. Readiness for Business-Critical Use– Does the HA/DR solution have management and monitoring tools, QA, and 24/7 support with very fast response times for business-critical use? What about the HA/DR solutions maturity and number of production deployments?
```

### Dual master
#### Asynchronous replication
#### Semi-Synchronous replication
#### Group replication


### MMM (Multi-master replication manager)
* [MMM](https://mysql-mmm.org/downloads.html) is a set of scripts written in perl providing the following capabilities:
	- Load balancing among read slaves
	- Master failover
	- Monitor mySQL states
* Pros:
	- Easy config
* Cons:
	- Not suitable for scenarios having high requirements on data consistency
* Deployment: Although dual master, only allows writing to a single master at a time.
	- mmm_mond: Coordinator scripts. Run on top of a monitoring machine
		+ Create a set of virtual IPs. One write IP binds to the master and multiple read IPs bind to slave. 
		+ When a mySQL is down, it will migrate the VIP to another mySQL machine. 
	- mmm_agentd: Run on the same machine as the mysql server
	- mmm_control: Provides administrative commands for mmm_mond

### MHA (Master high availability)
* [MHA](https://github.com/yoshinorim/mha4mysql-manager/wiki/Architecture)
	- Fast failover: Complete the failover within 0-30 seconds
	- Max effort consistency: When a master goes down, it will try to save binlog in the failed master. It uses this way to keep the maximum data consistency. However, this isn't reliable way. For example, some hardware failures may result in failure of saving binlogs. 
	- Compared with MMM, 
		+ Supports devops work like health check, suspend nodes
		+ Supports semi-synchronous, GTID 
* Deployment: 
	- MHA manager could be deployed in a separate machine for managing several master-slave clusters. It could also be deployed on a single slave. 
	- MHA node runs on each mysql server. 
* Cons:
	- Needs at minimum 3 machines
	- Brain split
	- Not suitable for scenarios having high requirements on data consistency

### Github/WePay MySQL high availability
* [Used at Wepay](https://wecode.wepay.com/posts/highly-available-mysql-clusters-at-wepay)
* [Used at Github](
https://github.blog/2018-06-20-mysql-high-availability-at-github/)

![MySQL HA github](./images/mysql_ha_github.png)

#### Master discovery series
1. DNS http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-1-dns
2. VPN and DNS
http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-2-vip-dns
3. app and service discovery
http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-3-app-service-discovery
4. Proxy heuristics
http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-4-proxy-heuristics
5. Service discovery and Proxy
http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-5-service-discovery-proxy
6. http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-6-other-methods

### Other solutions
#### MySQL cluster
#### Galera
#### PAXOS
#### Shared storage such as Amazon Aurora

## Sharding
### Choose the number of shards
* If has a cap on storage:
	- Each shard could contain at most 1TB data.
	- number of shards = total storage / 1TB
* If has a cap on number of records:
	- Suppose the size of row is 100 bytes
		- User table: uid (long 8 bytes), name (fixed char 16 bytes), city (int 4 bytes), timestamp (long 8 bytes), sex (int 4 bytes), age (int 4 bytes) = total 40 bytes
	- Total size of the rows: 100 bytes * Number_of_records
	- number of shards = total size of rows / 1TB

### Shard routing strategy
#### Lookup strategy
* Pros:
	- Easy to migrate data
* Cons: 
	- Need an additional hop when query
	- If the lookup table is really big, it could also need to be sharded

![lookup](./images/mysql_sharding_lookupstrategy.png)

#### Range strategy
* Pros:
	- Easy to add a new shard. No need to move the original data. For example, each month could have a new shard.
* Cons:
	- Uneven distribution. For example, July is the hot season but December is the cold season. 

![range](./images/mysql_sharding_rangestrategy.png)

##### By customer or tenant
* If it is a SaaS business, it is often true that data from one customer doesn't interact with data from any of your other customers. These apps are usually called multi-tenant apps. 
	- Multi-tenant apps usually require strong consistency where transaction is in place and data loss is not possible. 
	- Multi-tenant data usually evolves over time to provide more and more functionality. Unlike consumer apps which benefit from network effects to grow, B2B applications grows by adding new features for customers. 

##### By geography
* Apps such as postmate, lyft or instacart.
* You’re not going to live in Alabama and order grocery delivery from California. And if you were to order a Lyft pick-up from California to Alabama you’ll be waiting a good little while for your pickup.

##### By time
* Time sharding is incredibly common when looking at some form of event data. Event data may include clicks/impressions of ads, it could be network event data, or data from a systems monitoring perspective.
* This approach should be used when
	- You generate your reporting/alerts by doing analysis on the data with time as one axis.
	- You’re regularly rolling off data so that you have a limited retention of it.

#### Hash strategy

![range](./images/mysql_sharding_hashstrategy.png)

##### By entity id
* Shard based on hashing value of a field. 
* Pros:
	- Evenly distributed data
* Cons:
	- Hard to add a new shard. Lots of data migration need to happen. 

#### Shard a graph ???
* Graph model is most common in B2C apps like Facebook and Instagram. 
* With this model, data is often replicated in a few different forms. Then it is the responsibility of the application to map to the form that is most useful to acquire the data. The result is you have multiple copies for your data sharded in different ways, eventual consistency of data typically, and then have some application logic you have to map to your sharding strategy. For apps like Facebook and Reddit there is little choice but to take this approach, but it does come at some price.

### Sharding preferences
* We could classify the layout within database into the following categories:
	- Single database single table
	- Single database multiple table
	- Multiple database multiple table
* The preference order is as follows:
	1. Single database single table
	2. Single database multiple table
		* Table vertical sharding: If within a single table, some fields have a different usage pattern and consume large amount of space
			- Take user profile as an example (name, age, sex, nickname, description). Nickname and description are usually only used in display instead of query and description is really long. They could be put into a different table.  
		* Table horizontal sharding: If data volume is big, could consider table horizontal sharding single database multiple table. 
			- Could use 50M rows as the standard size for a single table. 
			- The MyISAM storage engine supports 2^32 rows per table.The InnoDB storage engine doesn't seem to have a limit on the number of rows, but it has a limit on table size of 64 terabytes. How many rows fits into this depends on the size of each row.
	4. If concurrent volume is high, then could consider using multiple database multiple table. 
		- For example, test MySQL 5.7 on a 4 Core 8 GB cloud server
			- Write performance: 500 TPS 
			- Also note down here the read performance for reference: 10000 QPS

### Table sharding
* Use case: Single table too big: There are too many lines in a single table. Each query scans too many rows and the efficiency is really low.

#### Vertical sharding
* Operations:
	+ Put different **fields of a table** into different tables
	+ Segmented tables usually share the primary key for correlating data

![Table Vertical sharding](./images/shard_verticalTable.png)

#### Horizontal sharding
* Operations:
	+ Based on certain fields, put **rows of a table** into different tables. 

![Table horizontal sharding](./images/shard_horizontalTable.png)

### Database sharding
* Use case:
	* Disk IO: There are too many hot data to fit into database memory. Each time a query is executed, there are a lot of IO operations being generated which reduce performance. 
	* Network IO: Too many concurrent requests. 

![database Vertical sharding](./images/shard_verticalDatabase.png)

#### Limitations
##### Write across shards
* Original transaction needs to be conducted within a distributed transaction.
	- e.g. ecommerce example (order table and inventory table)
* There are wwo ways in general to implement distributed transactions:
	- 2PC 
	- TCC
* For example, some software has built-in implementations such as:
	- MySQL XA
	- Spring JTA

##### Query Cross shard
* Query types:
	- Join queries: 
	- count queries:
	- order by queries:

* Solutions:
	* Aggregate query result for different shard within application code.
	* Usually use two sets of data to solve the problem
		- One data is based on unique sharding key.
		- The other one is data replicated asynchronously to Elasticsearch or Solr.

##### Unique global key
* Please see [this](https://github.com/DreamOfTheRedChamber/system-design/blob/master/uniqueIDGenerator.md) for details

##### Reshard
* Use 2's multiply number as table's number or database's number.

# Future readings
* MySQL DDL as big transaction
* Sharding Proxy - 百度DB Proxy ??? 
