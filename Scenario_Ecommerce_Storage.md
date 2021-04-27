
- [Ecommerce Storage](#ecommerce-storage)
	- [Schema design](#schema-design)
	- [SQL statements optimization](#sql-statements-optimization)
		- [Performance factors](#performance-factors)
		- [Optimize on Query level](#optimize-on-query-level)
		- [Reduce join](#reduce-join)
	- [Table partition](#table-partition)
		- [Vertical partition](#vertical-partition)
		- [Horizontal partition](#horizontal-partition)
	- [Sharding](#sharding)
		- [Pros](#pros)
		- [Cons](#cons)
			- [Write across shards](#write-across-shards)
			- [Query Cross shard](#query-cross-shard)
			- [Challenges in Graph DB sharding](#challenges-in-graph-db-sharding)
		- [Choose the number of shards](#choose-the-number-of-shards)
		- [Sharding stratgies](#sharding-stratgies)
			- [Lookup strategy](#lookup-strategy)
			- [Range strategy](#range-strategy)
				- [By customer or tenant](#by-customer-or-tenant)
				- [By geography](#by-geography)
				- [By time](#by-time)
			- [Hash strategy](#hash-strategy)
				- [By entity id](#by-entity-id)
		- [Best practices](#best-practices)
	- [Replication](#replication)
		- [Replication delay](#replication-delay)
			- [Defition](#defition)
			- [Delay sources](#delay-sources)
			- [Inconsistency](#inconsistency)
				- [When binlog format = raw](#when-binlog-format--raw)
				- [When binlog format = mixed](#when-binlog-format--mixed)
			- [Replication strategies](#replication-strategies)
				- [Paralelle approaches](#paralelle-approaches)
			- [Configure master-slave replication](#configure-master-slave-replication)
			- [How to find sync points](#how-to-find-sync-points)
			- [Solutions for master slave delay](#solutions-for-master-slave-delay)
			- [Failover strategies](#failover-strategies)
				- [Reliability first](#reliability-first)
				- [Availability first](#availability-first)
	- [Architecture patterns](#architecture-patterns)
		- [MySQL + Sharding proxy](#mysql--sharding-proxy)
			- [Sharding proxy (using MyCat)](#sharding-proxy-using-mycat)
			- [PXC cluster](#pxc-cluster)
			- [Replication cluster](#replication-cluster)
		- [MySQL + Archiving](#mysql--archiving)
			- [Use case](#use-case)
			- [Implementation](#implementation)
			- [Flowchart](#flowchart)
		- [MySQL + Redis](#mysql--redis)
			- [Use case](#use-case-1)
			- [Use case study - Prevent oversell](#use-case-study---prevent-oversell)
				- [V1: Serializable DB isolation](#v1-serializable-db-isolation)
				- [V2: Optimistic lock](#v2-optimistic-lock)
				- [V3: Put inventory number inside Redis](#v3-put-inventory-number-inside-redis)
		- [MySQL + Blob storage](#mysql--blob-storage)
		- [MySQL + Inforbright](#mysql--inforbright)
	- [Real world](#real-world)
		- [Wechat Red pocket](#wechat-red-pocket)
		- [WePay MySQL high availability](#wepay-mysql-high-availability)
		- [High availability at Github](#high-availability-at-github)
	- [Appendix of MySQL tools](#appendix-of-mysql-tools)
		- [MMM (Multi-master replication manager)](#mmm-multi-master-replication-manager)
		- [MHA (Master high availability)](#mha-master-high-availability)
		- [ecommerce SQL schema design](#ecommerce-sql-schema-design)
			- [Product group tables](#product-group-tables)
			- [Parameters table](#parameters-table)
			- [Brand table](#brand-table)
			- [Category table](#category-table)
			- [Spu table](#spu-table)
			- [Category and brand association table](#category-and-brand-association-table)
			- [Sku table](#sku-table)
			- [Location table](#location-table)
			- [Warehouse table](#warehouse-table)
			- [Warehouse and sku association table](#warehouse-and-sku-association-table)
			- [Retail shop table](#retail-shop-table)
			- [Retail shop and sku association table](#retail-shop-and-sku-association-table)
			- [Membership table](#membership-table)
			- [Customer table](#customer-table)
			- [Customer address table](#customer-address-table)
			- [Voucher](#voucher)
				- [Voucher table](#voucher-table)
				- [Voucher customer association table](#voucher-customer-association-table)
			- [Order](#order)
				- [Order table](#order-table)
				- [Order detail table](#order-detail-table)
			- [Dept](#dept)
			- [Job](#job)
			- [employee](#employee)
			- [user](#user)
			- [Delivery table](#delivery-table)
			- [Return table](#return-table)
			- [Rating table](#rating-table)
			- [Supplier table](#supplier-table)
			- [Supplier sku table](#supplier-sku-table)
			- [Purchase table](#purchase-table)
			- [Warehouse keeper table](#warehouse-keeper-table)
			- [WarehouseKeeper product table](#warehousekeeper-product-table)

# Ecommerce Storage


## Schema design


## SQL statements optimization
1. Don't use "SELECT * from abc": Will return a large number of data. Database will also need to retrieve table structure before executing the request. 
2. Be careful when using fuzzy matching - Don't use % in the beginning: Use % in the beginning will cause the database for a whole table scanning. "SELECT name from abc where name like %xyz"
3. Use "Order by" on field which has index: 
4. Don't use "IS NOT NULL" or "IS NULL": Index (binary tree) could not be created on Null values. 
5. Don't use != : Index could not be used. Could use < and > combined together.
   * Select name from abc where id != 20
   * Optimized version: Select name from abc where id > 20 or id < 20
6. Don't use OR: The expression before OR will use index. The expression 
   * Select name from abc where id == 20 or id == 30
   * Optimized version: Select name from abc where id == 20 UNION ALL select name from abc where id == 30
7. Don't use IN, NOT IN: Similar to OR 
8. Avoid type conversion inside expression: Select name from abc where id == '20'
9. Avoid use operators or function on the left side of an expression: 
	* Select name from abc where salary * 12 > 100000
	* Optimized version: Select name from abc where salary > 100000 / 12

### Performance factors
* Unpractical needs

```
Select count(*) from infoTable
```

* Deep paging

### Optimize on Query level
* Solution 1

```
SELECT id, subject, url FROM photo WHERE user_id = 1 LIMIT 10
SELECT COUNT(*) FROM photo_comment WHERE photo_id = ?
```

* Solution 2

```
SELECT id, subject, url FROM photo WHERE user_id = 1 LIMIT 10
SELECT photo_id, count(*) FROM photo_comment WHERE photo_id IN() GROUP BY photo_id
```

### Reduce join
* Have redundancy
* Merge in business level


## Table partition
* [Example for vertial and horizontal partition](https://www.sqlshack.com/database-table-partitioning-sql-server/#:~:text=What%20is%20a%20database%20table,is%20less%20data%20to%20scan)
* Use case: Single table too big. There are too many lines in a single table. Each query scans too many rows and the efficiency is really low.

### Vertical partition
* Operations:
	+ Put different **fields of a table** into different tables
	+ Segmented tables usually share the primary key for correlating data

![Table Vertical sharding](./images/shard_verticalTable.png)

### Horizontal partition
* Operations:
	+ Based on certain fields, put **rows of a table** into different tables. 

![Table horizontal sharding](./images/shard_horizontalTable.png)


## Sharding
### Pros
* Disk IO: There are too many hot data to fit into database memory. Each time a query is executed, there are a lot of IO operations being generated which reduce performance. 
* Network IO: Too many concurrent requests. 

![database Vertical sharding](./images/shard_verticalDatabase.png)

### Cons
#### Write across shards
* Original transaction needs to be conducted within a distributed transaction.
	- e.g. ecommerce example (order table and inventory table)
* There are wwo ways in general to implement distributed transactions:
	- 2PC 
	- TCC
* For example, some software has built-in implementations such as:
	- MySQL XA
	- Spring JTA

#### Query Cross shard
* Query types:
	- Join queries: 
	- count queries:
	- order by queries:

* Solutions:
	* Aggregate query result for different shard within application code.
	* Usually use two sets of data to solve the problem
		- One data is based on unique sharding key.
		- The other one is data replicated asynchronously to Elasticsearch or Solr.

#### Challenges in Graph DB sharding
* Graph model is most common in B2C apps like Facebook and Instagram. 
* With this model, data is often replicated in a few different forms. Then it is the responsibility of the application to map to the form that is most useful to acquire the data. The result is you have multiple copies for your data sharded in different ways, eventual consistency of data typically, and then have some application logic you have to map to your sharding strategy. For apps like Facebook and Reddit there is little choice but to take this approach, but it does come at some price.

### Choose the number of shards
* If has a cap on storage:
	- Each shard could contain at most 1TB data.
	- number of shards = total storage / 1TB
* If has a cap on number of records:
	- Suppose the size of row is 100 bytes
		- User table: uid (long 8 bytes), name (fixed char 16 bytes), city (int 4 bytes), timestamp (long 8 bytes), sex (int 4 bytes), age (int 4 bytes) = total 40 bytes
	- Total size of the rows: 100 bytes * Number_of_records
	- number of shards = total size of rows / 1TB

### Sharding stratgies
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

### Best practices
1. Single database single table
2. Single database multiple table
	* Table vertical sharding: If within a single table, some fields have a different usage pattern and consume large amount of space
		- Take user profile as an example (name, age, sex, nickname, description). Nickname and description are usually only used in display instead of query and description is really long. They could be put into a different table.  
	* Table horizontal sharding: If data volume is big, could consider table horizontal sharding single database multiple table. 
		- Could use 50M rows as the standard size for a single table. 
		- The MyISAM storage engine supports 2^32 rows per table.The InnoDB storage engine doesn't seem to have a limit on the number of rows, but it has a limit on table size of 64 terabytes. How many rows fits into this depends on the size of each row.
3. If concurrent volume is high, then could consider using multiple database multiple table. 
	- For example, test MySQL 5.7 on a 4 Core 8 GB cloud server
		- Write performance: 500 TPS 
		- Also note down here the read performance for reference: 10000 QPS

## Replication
### Replication delay
#### Defition
* The master-slave latency is defined as the difference between T3 and T1. 
	1. Master DB executes a transaction, writes into binlog and finishes at timestamp T1.
	2. The statement is replicated to binlog, Slave DB received it from the binlog T2.
	3. Slave DB executes the transaction and finishes at timestamp T3. 

#### Delay sources
* Inferior slave machines: Slave machine is insuperior to master
* Too much load for slave
	* Causes: Many analytical queries run on top of slave. 
	* Solutions:
		- Multiple slaves
		- Output telemetry to external statistical systems such as Hadoop through binlog 
* Big transactions
	* If a transaction needs to run for as long as 10 minutes on the master database, then it must wait for the transaction to finish before running it on slave. Slave will be behind master for 10 minutes. 
		- e.g. Use del to delete too many records within DB
		- e.g. mySQL DDL within big tables. 
* Slow slave thread replay

![Master slave replication process](./images/mysql_ha_masterSlaveReplication.png)

![Master slave replication process](./images/mysql_ha_masterSlave_multiThreads.png)

#### Inconsistency
##### When binlog format = raw
* [TODO]: understand deeper

![Inconsistency row format binlog](./images/mysql_ha_availabilityfirstRow.png)

##### When binlog format = mixed

![Inconsistency row format mixed](./images/mysql_ha_availabilityfirstMixed.png)

#### Replication strategies
* Synchronous replication: 
* Asynchronous replication: 
* Semi-Synchronous replication: 

##### Paralelle approaches
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

#### Configure master-slave replication
* Configure the following inside slave machine

```
SQL > STOP SLAVE;
SQL > Change master to 
master_host = '192.168.99.102',
master_port = 3306,
master_user = 'xxx',
master_password = 'yyy';
SQL > START SLAVE;
SQL > SHOW SLAVE STATUS;

// Watch the Slave_IO_Running and Slave_SQL_running field from the output

```

#### How to find sync points
* sql_slave_skip_counter
* slave_skip_errors
* GTID

#### Solutions for master slave delay
* Solution1: After write to master, write to cache as well. 
	- What if write to cache fails
		+ If read from master, slave useless
		+ If read from slave, still replication delay
* Solution2: If cannot read from slave, then read from master. 
	+ It works for DB add operation
	+ It doesn't work for DB update operation
* Solution3: If master and slave are located within the same location, synchronous replication
* Solution4: Shard the data

#### Failover strategies
##### Reliability first
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

##### Availability first
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


## Architecture patterns
### MySQL + Sharding proxy
#### Sharding proxy (using MyCat)

#### PXC cluster
* PXC is a type of strong consistency MySQL cluster built on top of Galera. It could store data requring high consistency. 

#### Replication cluster
* Replication is a type of weak consistency MySQL cluster shipped with MySQL based on binlog replication. It could be used to store data only requiring low consistency. 

```
                                                               │                                                             
                                                               │                                                             
                                                               ▼                                                             
                                                ┌─────────────────────────────┐                                              
                                                │DB Proxy such as MyCat for   │                                              
                                                │1. Sharding                  │                                              
                                                │2. Load balancing            │                                              
                                                │3. Routing such as read write│                                              
                                                │separation                   │                                              
                                                └─────────────────────────────┘                                              
                                                               │                                                             
                               Query for strong                │                                                             
                               consistency data                │              Query for weak                                 
                      ┌────────────────────────────────────────┼─────────────consistency data──────────┐                     
                      │             Shard A                    │                                       │                     
                      │                                        │                  Shard A              │                     
                      ▼                                        │                                       ▼                     
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                    │                 ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
               PXC Cluster A               │                   │                            Replication Cluster A           │
│                                                              │                 │                                           
                                           │                   │                                                            │
│┌──────────────┐          ┌──────────────┐                    │                 │┌──────────────┐          ┌──────────────┐ 
 │              │          │              ││                   │                  │              │          │              ││
││   PXC node   │◀────────▶│   PXC node   │                    │                 ││ Master node  │─────────▶│  Slave node  │ 
 │              │          │              ││                   │                  │              │          │              ││
│└──────────────┘          └──────────────┘                    │                 │└──────────────┘          └──────────────┘ 
         ▲                         ▲       │                   │                          │                                 │
│        │                         │                           │                 │        │                                  
         │     ┌──────────────┐    │       │                   │                          │    ┌──────────────┐             │
│        │     │              │    │                           │                 │        │    │              │              
         └────▶│   PXC node   │◀───┘       │                   │                          └───▶│  Slave node  │             │
│              │              │                                │                 │             │              │              
               └──────────────┘            │                   │                               └──────────────┘             │
└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                    │                 └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
                                                               │                                                             
                                                               │                                                             
                                                               │                                                             
                                                               │                                                             
                                                               │                                                             
 ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                   │                 ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
                PXC Cluster B               │                  │                            Replication Cluster B           │
 │                                                             │                 │                                           
                                            │                  │                                                            │
 │┌──────────────┐          ┌──────────────┐                   │                 │┌──────────────┐          ┌──────────────┐ 
  │              │          │              ││     Query for    │    Query for     │              │          │              ││
 ││   PXC node   │◀────────▶│   PXC node   │       strong      │      weak       ││ Master node  │─────────▶│  Slave node  │ 
  │              │          │              ││    consistency   │   consistency    │              │          │              ││
 │└──────────────┘          └──────────────┘ ◀──────data───────┴──────data──────▶│└──────────────┘          └──────────────┘ 
          ▲                         ▲       │                                             │                                 │
 │        │                         │              Shard B           Shard B     │        │                                  
          │     ┌──────────────┐    │       │                                             │    ┌──────────────┐             │
 │        │     │              │    │                                            │        │    │              │              
          └────▶│   PXC node   │◀───┘       │                                             └───▶│  Slave node  │             │
 │              │              │                                                 │             │              │              
                └──────────────┘            │                                                  └──────────────┘             │
 └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                                     └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
```

### MySQL + Archiving
#### Use case
* If a single SQL table's size exceeds 20M rows, then its performance will be slow. Partitioning and Sharding have already been applied. Due to the use cases, there are some hot and cold data, e.g. ecommerce website orders.

#### Implementation
* MySQL engine for archiving
  * InnoDB is based on B+ tree, each time a write happens, the clustered index will need to modified again. The high traffic during archiving process decides that InnoDB will not be a great fit. 
  * TukoDB has higher write performance
* Create archiving tables

```
CREATE Table t_orders_2021_03 {
	...
} Engine = TokuDB;
```

* Pt-archiver: One of the utils of Percona-toolkit and used to archive rows from a MySQL table into another table or a file. https://www.percona.com/doc/percona-toolkit/LATEST/pt-archiver.html

#### Flowchart

```
┌──────────────────────────┐                                                                                        
│         Shard A          │                              ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                       
│                          │                                                                                        
│   ┌─────────────────┐    │                              │                                 │                       
│   │                 │    │                                                                                        
│   │    Cold data    │────┼──┐                           │                                 │                       
│   │                 │    │  │                               ┌──────────────────────────┐          ┌──────────────┐
│   └─────────────────┘    │  │                           │   │         HA Proxy         │  │       │              │
│                          │  │                               │                          │          │              │
│   ┌─────────────────┐    │  │                           │   │  ┌───────────────────┐   │  │       │  Archive DB  │
│   │                 │    │  │                               │  │    Keepalived     │   │      ┌──▶│              │
│   │    Hot data     │    │  │                           │   │  └───────────────────┘   │  │   │   │              │
│   │                 │    │  │                               │                          │      │   │              │
│   └─────────────────┘    │  │       ┌──────────────┐    │   └──────────────────────────┘  │   │   └──────────────┘
│                          │  │       │              │                                          │                   
│                          │  │       │              │    │                                 │   │                   
└──────────────────────────┘  │       │  Virtual IP  │                                          │                   
                              ├──────▶│   address    │───▶│                                 │───┤                   
┌──────────────────────────┐  │       │              │                                          │                   
│         Shard Z          │  │       │              │    │   ┌──────────────────────────┐  │   │   ┌──────────────┐
│                          │  │       └──────────────┘        │         HA Proxy         │      │   │              │
│   ┌─────────────────┐    │  │                           │   │                          │  │   │   │              │
│   │                 │    │  │                               │   ┌───────────────────┐  │      │   │  Archive DB  │
│   │    Cold data    │────┼──┘                           │   │   │    Keepalived     │  │  │   └──▶│              │
│   │                 │    │                                  │   └───────────────────┘  │          │              │
│   └─────────────────┘    │                              │   │                          │  │       │              │
│                          │                                  └──────────────────────────┘          └──────────────┘
│   ┌─────────────────┐    │                              │                                 │                       
│   │                 │    │                                                                                        
│   │    Hot data     │    │                              │                                 │                       
│   │                 │    │                                                                                        
│   └─────────────────┘    │                              │                                 │                       
│                          │                               ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                        
│                          │                                                                                        
└──────────────────────────┘                                                                                        
```

### MySQL + Redis
#### Use case
* Deal with intensive read conditions

#### Use case study - Prevent oversell
* Question: How to prevent overselling for limited inventory products?

##### V1: Serializable DB isolation
* Solution1: Set serializable isolation level in DB


##### V2: Optimistic lock
* Set optimistic lock on the table where multiple writes to a single table happens often. 

```
             Step1.                                                                                 
       ┌─────Query ───────────────────────────┐                                                     
       │    version                           │                                                     
       │     number                           ▼                                                     
       │                                ┌──────────┐                                                
       │                                │  Lookup  │                                                
       │                  ┌─────────────│ request  │                                                
       │                  │             │          │                                                
       │               Step2.           └──────────┘                                                
       │               Return                                                                       
       │               version                                                                      
┌────────────┐         number                                                                       
│            │            │                                                                         
│   Start    │◀───────────┘                                                                         
│            │                                                                ┌────────────────────┐
└────────────┘                          ┌──────────┐       ┌──────────┐   ┌──▶│  If match, write   │
       │           Step3.               │  Write   │       │If version│   │   └────────────────────┘
       └───────────Write ──────────────▶│ request  │──────▶│  match   │───┤                         
                  request               │          │       │          │   │                         
                                        └──────────┘       └──────────┘   │   ┌────────────────────┐
                                                                          └──▶│    If not, fail    │
                                                                              └────────────────────┘
```

##### V3: Put inventory number inside Redis
* Redis transaction mechanism: 
  * Different from DB transaction, an atomic batch processing mechanism for Redis
  * Similar to put optimistic mechanism inside Redis

* Flowchart

```
    ┌────────────────┐          ┌────────────────┐
    │ Redis client A │          │ Redis client B │
    └────────────────┘          └────────────────┘
             │                          │         
             │                          │         
             ▼                          │         
      ┌─────────────┐                   │         
      │ Watch data  │                   │         
      └─────────────┘                   │         
             │                          │         
             │                          │         
             ▼                          │         
┌─────────────────────────┐             │         
│Execute batch of commands│             │         
└─────────────────────────┘             │         
             │                          │         
             │                          │         
             │                          │         
             ▼                          ▼         
  ┌──────────────────────────────────────────────┐
  │                    Redis                     │
  └──────────────────────────────────────────────┘
```

* Implementation:
  
```
// Redis watch data
Redis > Watch inventory_number, userlist

// Start a transaction (execute batch of commands)
Redis > Multi
Redis > DECR inventory_number // reduce number of inventory because it is sold
Redis > RPUSH userlist 1234 // add 1234 user id to userlist who buys the product
Redis > EXEC
```

### MySQL + Blob storage
### MySQL + Inforbright

## Real world
### Wechat Red pocket
- https://www.infoq.cn/article/2017hongbao-weixin
- http://www.52im.net/thread-2548-1-1.html

### WePay MySQL high availability
* [Used at Wepay](https://wecode.wepay.com/posts/highly-available-mysql-clusters-at-wepay)

### High availability at Github
* [Used at Github](
https://github.blog/2018-06-20-mysql-high-availability-at-github/)

![MySQL HA github](./images/mysql_ha_github.png)

* Master discovery series
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

## Appendix of MySQL tools
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


### ecommerce SQL schema design
* SPU: Standard product unit. A specific product such as an iphone 10
* SKU: Stock keeping unit. 

```
┌───────────────────────┐                     ┌───────────────────────┐ 
│                       │                     │                       │ 
│                       │                     │                       │ 
│                       │                     │                       │ 
│    Category table     │◀─────────1:n───────▶│    Parameter table    │ 
│                       │                     │              *        │ 
│                       │                     │                       │ 
│                       │                     │                       │ 
└───────────────────────┘                     └───────────────────────┘ 
            ▲                                                           
            │                                                           
            │                                                           
           1:1                                                          
            │                                                           
            │                                                           
            ▼                                                           
┌───────────────────────┐                      ┌───────────────────────┐
│                       │                      │                       │
│                       │                      │                       │
│                       │                      │  Stock keeping unit   │
│     Product table     │◀────────1:n─────────▶│         table         │
│                       │                      │                       │
│                       │                      │                       │
│                       │                      │                       │
└───────────────────────┘                      └───────────────────────┘


┌───────────────────────┐                     ┌───────────────────────┐
│                       │                     │                       │
│                       │                     │                       │
│                       │                     │                       │
│     Retail store      │◀─────────m:n───────▶│       Warehouse       │
│                       │                     │              *        │
│                       │                     │                       │
│                       │                     │                       │
└───────────────────────┘                     └───────────────────────┘
            ▲                                             ▲            
            │                                             │            
            │                                             │            
           m:n                                            │            
            │                                             │            
            │                                             │            
            ▼                                             │            
┌───────────────────────┐                                 │            
│                       │                                 │            
│                       │                                 │            
│                       │                                 │            
│       Products        │◀─────────m:n────────────────────┘            
│                       │                                              
│                       │                                              
│                       │                                              
└───────────────────────┘                                              
```


#### Product group tables
* t_spec_group table
  * id -- primary key
  * spg_id -- this category id will help identify categories much faster. 
    - e.g. 0-1000 fruit / 1000-2000 funiture. 
    - Index and unique constraint will be added for this column. 
  * name -- category name

```
CREATE TABLE t_spec_group(
id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT "automatically generated primary key",
spg_id INT UNSIGNED NOT NULL COMMENT "category id",
`name` VARCHAR(200) NOT NULL COMMENT "category name",
UNIQUE INDEX unq_spg_id(spg_id),
UNIQUE INDEX unq_name(`name`),
INDEX idx_spg_id(spg_id)
)COMMENT="product group table";
```

#### Parameters table

```
DROP TABLE IF EXISTS `t_spec_param`;
CREATE TABLE `t_spec_param`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `spg_id` int(10) UNSIGNED NOT NULL COMMENT 'category id',
  `spp_id` int(10) UNSIGNED NOT NULL COMMENT 'parameter id inside a category',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'paramter name',
  `numeric` tinyint(1) NOT NULL COMMENT 'whether the parameter is a digit',
  `unit` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'unit of the parameter',
  `generic` tinyint(1) NOT NULL COMMENT 'whether it is a generic parameter, whether this parameter needs to be displayed',
  `searching` tinyint(1) NOT NULL COMMENT 'whether this parameter will be used for search',
  `segements` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'enumeration of parameter values',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_spg_id`(`spg_id`) USING BTREE,
  INDEX `idx_spp_id`(`spp_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'parameter table' ROW_FORMAT = Dynamic;
```

#### Brand table

```
CREATE TABLE `t_brand`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'name ',
  `image` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'image url for the website',
  `letter` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'first letter of a brand',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_name`(`name`) USING BTREE,
  INDEX `idx_letter`(`letter`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'brand table' ROW_FORMAT = Dynamic;
```

#### Category table

```
CREATE TABLE `t_category`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '',
  `parent_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'previous categorization level',
  `if_parent` tinyint(1) NOT NULL COMMENT 'whether it has child categorization level',
  `sort` int(10) UNSIGNED NOT NULL COMMENT 'ranking weight',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'category table' ROW_FORMAT = Dynamic;
```

#### Spu table

```
CREATE TABLE `t_spu`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'title',
  `sub_title` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'sub title',
  `category_id` int(10) UNSIGNED NOT NULL COMMENT 'category id',
  `brand_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'brand id',
  `spg_id` int(10) UNSIGNED NOT NULL COMMENT '品类ID',
  `saleable` tinyint(1) NOT NULL COMMENT 'whether online',
  `valid` tinyint(1) NOT NULL COMMENT 'whether the product is still on the market',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create timestamp',
  `last_update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last modified timestamp',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_brand_id`(`brand_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_spg_id`(`spg_id`) USING BTREE,
  INDEX `idx_saleable`(`saleable`) USING BTREE,
  INDEX `idx_valid`(`valid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'product table' ROW_FORMAT = Dynamic;

```

#### Category and brand association table

```
CREATE TABLE `t_category_brand`  (
  `category_id` int(10) UNSIGNED NOT NULL COMMENT 'category id',
  `brand_id` int(10) UNSIGNED NOT NULL COMMENT 'brand id',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`category_id`, `brand_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'association table for brand and category' ROW_FORMAT = Dynamic;
```

#### Sku table

```
REATE TABLE `t_sku`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `spu_id` int(10) UNSIGNED NOT NULL COMMENT 'product ID',
  `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'title',
  `images` json NULL COMMENT 'a list of images',
  `price` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'price', // for discount on price based on memberships, the price does not need to be stored separately. 
  `param` json NOT NULL COMMENT 'parameters',
  `saleable` tinyint(1) NOT NULL COMMENT 'whether this item is still on sale',
  `valid` tinyint(1) NOT NULL COMMENT 'whether this item is still valid',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `last_update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last modified time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_spu_id`(`spu_id`) USING BTREE,
  INDEX `idx_saleable`(`saleable`) USING BTREE,
  INDEX `idx_valid`(`valid`) USING BTREE,
  FULLTEXT INDEX `title`(`title`)
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

```

#### Location table
* Province/City tables are created so that it no longer are strings

```
CREATE TABLE `t_province`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `province` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'province name',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_province`(`province`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'province table' ROW_FORMAT = Dynamic;

CREATE TABLE `t_city`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `city` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'city',
  `province_id` int(10) UNSIGNED NOT NULL COMMENT 'province ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'city table' ROW_FORMAT = Dynamic;
```

#### Warehouse table

```
CREATE TABLE `t_warehouse`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `city_id` int(10) UNSIGNED NOT NULL COMMENT 'city ID',
  `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'address',
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'telephone',
  `lng` decimal(15, 10) NULL DEFAULT NULL COMMENT 'longtitude',
  `lat` decimal(15, 10) NULL DEFAULT NULL COMMENT 'altitude',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_city_id`(`city_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'warehouse table' ROW_FORMAT = Dynamic;
```

#### Warehouse and sku association table

```
CREATE TABLE `t_warehouse_sku`  (
  `warehouse_id` int(10) UNSIGNED NOT NULL COMMENT 'warehouse ID',
  `sku_id` int(10) UNSIGNED NOT NULL COMMENT 'product ID',
  `num` int(10) UNSIGNED NOT NULL COMMENT 'number inside warehouse',
  `unit` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'sku unit',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`warehouse_id`, `sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'warehouse and sku association table' ROW_FORMAT = Dynamic;
 
```

#### Retail shop table

```
CREATE TABLE `t_shop`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `city_id` int(10) UNSIGNED NOT NULL COMMENT 'city id',
  `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'address',
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'telephone',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_city_id`(`city_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'retail shop table' ROW_FORMAT = Dynamic;
```

#### Retail shop and sku association table

```
CREATE TABLE `t_shop_sku`  (
  `shop_id` int(10) UNSIGNED NOT NULL COMMENT '零售店ID',
  `sku_id` int(10) UNSIGNED NOT NULL COMMENT '商品ID',
  `num` int(10) UNSIGNED NOT NULL COMMENT '库存数量',
  `unit` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '库存单位',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`shop_id`, `sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '零售店商品库存表' ROW_FORMAT = Dynamic;
```

#### Membership table

```
CREATE TABLE `t_level`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `level` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'level',
  `discount` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'discount',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'membership' ROW_FORMAT = Dynamic;
```

#### Customer table

```
CREATE TABLE `t_customer`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'username',
  `password` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'password encrypted by AES',
  `wechat` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'wechat id',
  `tel` char(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'cellphone id',
  `level_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'membership level',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `last_update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last modification time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_username`(`username`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'Customer table' ROW_FORMAT = Dynamic;
```

#### Customer address table

```
CREATE TABLE `t_customer_address`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `customer_id` int(10) UNSIGNED NOT NULL COMMENT 'customer id',
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'name',
  `tel` char(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'telephone',
  `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'address',
  `prime` tinyint(1) NOT NULL COMMENT 'whether to use address as default address to receive the package',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_customer_id`(`customer_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '?????' ROW_FORMAT = Dynamic;
```

#### Voucher
* Voucher table needs to have two tables. One single table solution is 

##### Voucher table

```
CREATE TABLE `t_voucher`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `deno` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'amount in the vouncher',
  `condition` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'when this voucher could be applied',
  `start_date` date NULL DEFAULT NULL COMMENT 'start date, could be null meaning always effective',
  `end_date` date NULL DEFAULT NULL COMMENT 'end date, could be null meaning always effective',
  `max_num` int(11) NULL DEFAULT NULL COMMENT 'the max number of voucher',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'voucher table' ROW_FORMAT = Dynamic;

```

##### Voucher customer association table

```
CREATE TABLE `t_voucher_customer`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `voucher_id` int(10) UNSIGNED NOT NULL COMMENT 'voucher id',
  `customer_id` int(10) UNSIGNED NOT NULL COMMENT 'customer id',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'customer voucher association table' ROW_FORMAT = Dynamic;
```

#### Order
* What about use JSON to store multiple ordered items inside a single table?
  * Easy to store, but hard to query. e.g. search for historical orders
  * Needs to split into two table 
  
##### Order table

```
CREATE TABLE `t_order`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `code` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'order number, defined as string, which could contain more info about order such as time, category, ...',
  `type` tinyint(3) UNSIGNED NOT NULL COMMENT 'type of order: physical store vs online store',
  `shop_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'physical store id',
  `customer_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'member ID',
  `amount` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'total amount',
  `payment_type` tinyint(3) UNSIGNED NOT NULL COMMENT 'payment method: 1 debit card, 2 credit card, 3 cash',
  `status` tinyint(3) UNSIGNED NOT NULL COMMENT 'order status：1未付款,2已付款,3已发货,4已签收',
  `postage` decimal(10, 2) UNSIGNED NULL DEFAULT NULL COMMENT 'shipping fees',
  `weight` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'weight(gram)',
  `voucher_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'voucher ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical deletion',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_code`(`code`) USING BTREE,
  INDEX `idx_code`(`code`) USING BTREE,
  INDEX `idx_customer_id`(`customer_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'order table' ROW_FORMAT = Dynamic;
```

##### Order detail table

```
CREATE TABLE `t_order_detail`  (
  `order_id` int(10) UNSIGNED NOT NULL COMMENT 'orderID',
  `old_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'old SKU_OLD table ID',
  `sku_id` int(10) UNSIGNED NOT NULL COMMENT 'sku ID',
  `price` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'original price',
  `actual_price` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'purchase price',
  `num` int(10) UNSIGNED NOT NULL COMMENT 'purchase number',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`order_id`, `sku_id`) USING BTREE,
  INDEX `idx_old_id`(`old_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'order details table' ROW_FORMAT = Dynamic;
```

#### Dept

```
CREATE TABLE `t_dept`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `dname` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'department name',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_dname`(`dname`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'department table' ROW_FORMAT = Dynamic;
```

#### Job

```
CREATE TABLE `t_job`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `job` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'job title',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_job`(`job`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'job table' ROW_FORMAT = Dynamic;
```

#### employee

```
CREATE TABLE `t_emp`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `wid` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'work id',
  `ename` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `married` tinyint(1) NOT NULL,
  `education` tinyint(4) NOT NULL,
  `tel` char(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `email` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `job_id` int(10) UNSIGNED NOT NULL COMMENT 'job ID',
  `dept_id` int(10) UNSIGNED NOT NULL COMMENT 'department ID',
  `mgr_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT 'manager ID',
  `hiredate` date NOT NULL COMMENT 'employee date',
  `termdate` date NULL DEFAULT NULL COMMENT 'leave date',
  `status` tinyint(3) UNSIGNED NOT NULL COMMENT 'status：1 work,2 vacation,3 leave, 4 death',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_wid`(`wid`) USING BTREE,
  INDEX `idx_job_id`(`job_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_mgr_id`(`mgr_id`) USING BTREE,
  INDEX `idx_wid`(`wid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'employee table' ROW_FORMAT = Dynamic;
```

#### user

```
CREATE TABLE `t_user`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `username` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'username',
  `password` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'password',
  `emp_id` int(10) UNSIGNED NOT NULL COMMENT 'employee ID',
  `role_id` int(10) UNSIGNED NOT NULL COMMENT 'role ID',
  `status` tinyint(3) UNSIGNED NOT NULL COMMENT 'status：1 allow, 2 forbidden',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `last_update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last modified time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_username`(`username`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE,
  INDEX `idx_emp_id`(`emp_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'user table' ROW_FORMAT = Dynamic;
```

#### Delivery table

```
CREATE TABLE `t_shipment`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `order_id` int(10) UNSIGNED NOT NULL COMMENT 'shipment ID',
  `sku` json NOT NULL COMMENT '商品',
  `qa_id` int(10) UNSIGNED NOT NULL COMMENT 'quality checker ID',
  `de_id` int(10) UNSIGNED NOT NULL COMMENT 'sender ID',
  `postid` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'shipment order',
  `price` decimal(10, 0) UNSIGNED NOT NULL COMMENT 'delivery fee',
  `ecp` tinyint(3) UNSIGNED NOT NULL COMMENT 'id of shipment company',
  `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'delivery address',
  `warehouse_id` int(10) UNSIGNED NOT NULL COMMENT 'warehouse ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_qa_id`(`qa_id`) USING BTREE,
  INDEX `idx_de_id`(`de_id`) USING BTREE,
  INDEX `idx_postid`(`postid`) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id`) USING BTREE,
  INDEX `idx_address_id`(`address`) USING BTREE,
  INDEX `idx_ecp`(`ecp`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'shipment table' ROW_FORMAT = Dynamic;
```

#### Return table

```
CREATE TABLE `t_return`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `order_id` int(10) UNSIGNED NOT NULL COMMENT 'order ID',
  `sku` json NOT NULL COMMENT 'return product',
  `reason` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'return reason',
  `qa_id` int(10) UNSIGNED NOT NULL COMMENT '质检员ID',
  `payment` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'return order amount',
  `payment_type` tinyint(3) UNSIGNED NOT NULL COMMENT 'refund way：1 debit card，2 credit card，3 check',
  `status` tinyint(3) UNSIGNED NOT NULL COMMENT 'status: 1 successful returned. 2 failed',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical deletion',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_qa_id`(`qa_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'return table' ROW_FORMAT = Dynamic;

```

#### Rating table

```
CREATE TABLE `t_rating`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'comment ID',
  `order_id` int(10) UNSIGNED NOT NULL COMMENT 'order ID',
  `sku_id` int(10) UNSIGNED NOT NULL COMMENT 'product ID',
  `img` json NULL COMMENT 'buyers' images',
  `rating` tinyint(3) UNSIGNED NOT NULL COMMENT '评分',
  `comment` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'buyers' comments',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_sku_id`(`sku_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'comment table' ROW_FORMAT = Dynamic;

```

#### Supplier table

```
CREATE TABLE `t_supplier`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `code` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'supplier id',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'supplier name',
  `type` tinyint(3) UNSIGNED NOT NULL COMMENT 'type of supplier：1 factory, 2 agent,3 individual',
  `link_man` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'contact person',
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'contact number',
  `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'contact address',
  `bank_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'bank name',
  `bank_account` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'bank account',
  `status` tinyint(3) UNSIGNED NOT NULL COMMENT 'status:1 allow, 2 forbidden',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unq_code`(`code`) USING BTREE,
  INDEX `idx_code`(`code`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'supplier table' ROW_FORMAT = Dynamic;
```

#### Supplier sku table

```
CREATE TABLE `t_supplier_sku`  (
  `supplier_id` int(10) UNSIGNED NOT NULL COMMENT 'supplier ID',
  `sku_id` int(10) UNSIGNED NOT NULL COMMENT 'sku ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`supplier_id`, `sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'supplier sku association table' ROW_FORMAT = Dynamic;
```

#### Purchase table

```
CREATE TABLE `t_purchase`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `sku_id` int(10) UNSIGNED NOT NULL COMMENT 'sku ID',
  `num` int(10) UNSIGNED NOT NULL COMMENT 'number',
  `warehouse_id` int(10) UNSIGNED NOT NULL COMMENT 'warehouse ID',
  `in_price` decimal(10, 2) UNSIGNED NOT NULL COMMENT 'purchase price',
  `out_price` decimal(10, 2) UNSIGNED NULL DEFAULT NULL COMMENT 'suggest price',
  `buyer_id` int(10) UNSIGNED NOT NULL COMMENT 'buyer ID',
  `status` tinyint(3) UNSIGNED NOT NULL COMMENT 'status: 1 finished 2 unknown',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'modified time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'logical delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sku_id`(`sku_id`) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id`) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '采购表' ROW_FORMAT = Dynamic;
```

#### Warehouse keeper table

```
CREATE TABLE `t_productin`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `storekeeper_id` int(10) UNSIGNED NOT NULL COMMENT '保管员ID',
  `amount` decimal(15, 2) UNSIGNED NOT NULL COMMENT '总金额',
  `supplier_id` int(10) UNSIGNED NOT NULL COMMENT '供应商ID',
  `payment` decimal(15, 2) UNSIGNED NOT NULL COMMENT '实付金额',
  `payment_type` tinyint(3) UNSIGNED NOT NULL COMMENT '支付方式',
  `invoice` tinyint(1) NOT NULL COMMENT '是否开票',
  `remark` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_storekeeper_id`(`storekeeper_id`) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id`) USING BTREE,
  INDEX `idx_payment_type`(`payment_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '入库信息表' ROW_FORMAT = Dynamic;

```

#### WarehouseKeeper product table

```
CREATE TABLE `t_productin_purchase`  (
  `productin_id` int(10) UNSIGNED NOT NULL COMMENT '入库ID',
  `purchase_id` int(10) UNSIGNED NOT NULL COMMENT '采购ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`productin_id`, `purchase_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '入库商品表' ROW_FORMAT = Dynamic;
```