# Storage\_MySQL\_Scalability

* [MySQL Scalability](storage_mysql_scalability.md#mysql-scalability)
  * [Replication](storage_mysql_scalability.md#replication)
    * [Category](storage_mysql_scalability.md#category)
    * [Process](storage_mysql_scalability.md#process)
      * [Flowchart](storage_mysql_scalability.md#flowchart)
      * [binlog](storage_mysql_scalability.md#binlog)
        * [Format](storage_mysql_scalability.md#format)
          * [Statement](storage_mysql_scalability.md#statement)
          * [Row](storage_mysql_scalability.md#row)
          * [Mixed](storage_mysql_scalability.md#mixed)
        * [Why MySQL 5.7 default to Row instead of Mixed](storage_mysql_scalability.md#why-mysql-57-default-to-row-instead-of-mixed)
    * [Replication delay](storage_mysql_scalability.md#replication-delay)
      * [Def](storage_mysql_scalability.md#def)
      * [Delay sources](storage_mysql_scalability.md#delay-sources)
      * [How to reduce replication delay](storage_mysql_scalability.md#how-to-reduce-replication-delay)
    * [Use cases](storage_mysql_scalability.md#use-cases)
      * [Handle old data - Archive](storage_mysql_scalability.md#handle-old-data---archive)
        * [Use case](storage_mysql_scalability.md#use-case)
        * [Implementation](storage_mysql_scalability.md#implementation)
        * [Flowchart](storage_mysql_scalability.md#flowchart-1)
      * [Backup](storage_mysql_scalability.md#backup)
      * [High availability with failover](storage_mysql_scalability.md#high-availability-with-failover)
        * [Two servers](storage_mysql_scalability.md#two-servers)
          * [Reliability first failover](storage_mysql_scalability.md#reliability-first-failover)
        * [Approaches](storage_mysql_scalability.md#approaches)
          * [Availability first failover](storage_mysql_scalability.md#availability-first-failover)
        * [Multiple servers](storage_mysql_scalability.md#multiple-servers)
          * [New challenges - find sync point between multiple servers](storage_mysql_scalability.md#new-challenges---find-sync-point-between-multiple-servers)
          * [Problem with binlog position](storage_mysql_scalability.md#problem-with-binlog-position)
          * [GTID to rescue](storage_mysql_scalability.md#gtid-to-rescue)
  * [High concurrent writes conflicts](storage_mysql_scalability.md#high-concurrent-writes-conflicts)
    * [V1: Serializable DB isolation](storage_mysql_scalability.md#v1-serializable-db-isolation)
    * [V2: Optimistic lock](storage_mysql_scalability.md#v2-optimistic-lock)
    * [V3: Put inventory number inside Redis](storage_mysql_scalability.md#v3-put-inventory-number-inside-redis)
  * [High concurrent read but low concurrent writes - Read/Write separation](storage_mysql_scalability.md#high-concurrent-read-but-low-concurrent-writes---readwrite-separation)
  * [High concurrent writes and large volume in a single table: MySQL table partitioning](storage_mysql_scalability.md#high-concurrent-writes-and-large-volume-in-a-single-table-mysql-table-partitioning)
    * [Def](storage_mysql_scalability.md#def-1)
    * [Benefits](storage_mysql_scalability.md#benefits)
    * [MySQL only supports horizontal partition](storage_mysql_scalability.md#mysql-only-supports-horizontal-partition)
    * [Limitations: Partition column and unique indexes](storage_mysql_scalability.md#limitations-partition-column-and-unique-indexes)
    * [Use cases](storage_mysql_scalability.md#use-cases-1)
    * [Types](storage_mysql_scalability.md#types)
      * [RANGE Partitioning](storage_mysql_scalability.md#range-partitioning)
      * [List partitioning](storage_mysql_scalability.md#list-partitioning)
      * [Hash partitioning](storage_mysql_scalability.md#hash-partitioning)
    * [References](storage_mysql_scalability.md#references)
  * [High concurrent writes and large volume across tables: MySQL DB Sharding](storage_mysql_scalability.md#high-concurrent-writes-and-large-volume-across-tables-mysql-db-sharding)
    * [Use cases](storage_mysql_scalability.md#use-cases-2)
      * [Last resort](storage_mysql_scalability.md#last-resort)
      * [Triggers](storage_mysql_scalability.md#triggers)
      * [Capacity planning](storage_mysql_scalability.md#capacity-planning)
    * [Introduced problems](storage_mysql_scalability.md#introduced-problems)
      * [How to choose sharding key](storage_mysql_scalability.md#how-to-choose-sharding-key)
      * [Choose the number of shards](storage_mysql_scalability.md#choose-the-number-of-shards)
      * [Limited SQL queries](storage_mysql_scalability.md#limited-sql-queries)
      * [Sharding stratgies](storage_mysql_scalability.md#sharding-stratgies)
        * [Lookup strategy](storage_mysql_scalability.md#lookup-strategy)
        * [Range strategy](storage_mysql_scalability.md#range-strategy)
          * [By customer or tenant](storage_mysql_scalability.md#by-customer-or-tenant)
          * [By geography](storage_mysql_scalability.md#by-geography)
          * [By time](storage_mysql_scalability.md#by-time)
        * [Hash strategy](storage_mysql_scalability.md#hash-strategy)
          * [By entity id](storage_mysql_scalability.md#by-entity-id)
      * [How to store unsharded table](storage_mysql_scalability.md#how-to-store-unsharded-table)
      * [How to deploy shards on nodes](storage_mysql_scalability.md#how-to-deploy-shards-on-nodes)
      * [Cross shard join](storage_mysql_scalability.md#cross-shard-join)
      * [Distributed transactions \(write across shards\)](storage_mysql_scalability.md#distributed-transactions-write-across-shards)
      * [Unique global ID](storage_mysql_scalability.md#unique-global-id)
      * [Challenges in Graph DB sharding](storage_mysql_scalability.md#challenges-in-graph-db-sharding)
    * [ShardingSphere](storage_mysql_scalability.md#shardingsphere)
      * [Sharding JDBC](storage_mysql_scalability.md#sharding-jdbc)
      * [Sharding Proxy](storage_mysql_scalability.md#sharding-proxy)
    * [Sharding example \(In Chinese\)](storage_mysql_scalability.md#sharding-example-in-chinese)
  * [Architecture example - Replication + PXC + Sharding proxy](storage_mysql_scalability.md#architecture-example---replication--pxc--sharding-proxy)
  * [Architecture example - Disaster recovery](storage_mysql_scalability.md#architecture-example---disaster-recovery)
    * [One active and the other cold backup machine](storage_mysql_scalability.md#one-active-and-the-other-cold-backup-machine)
    * [Two active DCs with full copy of data](storage_mysql_scalability.md#two-active-dcs-with-full-copy-of-data)
      * [Same city vs different city](storage_mysql_scalability.md#same-city-vs-different-city)
      * [Two active DCs within a city](storage_mysql_scalability.md#two-active-dcs-within-a-city)
      * [Two active DCs in different cities](storage_mysql_scalability.md#two-active-dcs-in-different-cities)
    * [Multi active DCs with sharded data](storage_mysql_scalability.md#multi-active-dcs-with-sharded-data)
      * [Synchronization mechanisms](storage_mysql_scalability.md#synchronization-mechanisms)
        * [Message queue based](storage_mysql_scalability.md#message-queue-based)
        * [RPC based](storage_mysql_scalability.md#rpc-based)
    * [Distributed database \(Two cities / three DCs and five copies\)](storage_mysql_scalability.md#distributed-database-two-cities--three-dcs-and-five-copies)
      * [Pros](storage_mysql_scalability.md#pros)
      * [Cons](storage_mysql_scalability.md#cons)
  * [Parameters to monitor](storage_mysql_scalability.md#parameters-to-monitor)
  * [Real world](storage_mysql_scalability.md#real-world)
    * [Past utility: MMM \(Multi-master replication manager\)](storage_mysql_scalability.md#past-utility-mmm-multi-master-replication-manager)
    * [Past utility MHA \(Master high availability\)](storage_mysql_scalability.md#past-utility-mha-master-high-availability)
    * [Wechat Red pocket](storage_mysql_scalability.md#wechat-red-pocket)
    * [WePay MySQL high availability](storage_mysql_scalability.md#wepay-mysql-high-availability)
    * [High availability at Github](storage_mysql_scalability.md#high-availability-at-github)
    * [Multi DC for disaster recovery](storage_mysql_scalability.md#multi-dc-for-disaster-recovery)

## MySQL Scalability

### Replication

#### Category

* Synchronous replication: 
* Asynchronous replication: 
* Semi-Synchronous replication:
* Replication topology
  * [https://coding.imooc.com/lesson/49.html\#mid=491](https://coding.imooc.com/lesson/49.html#mid=491)

```text
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

#### Process

**Flowchart**

![Master slave replication process](.gitbook/assets/mysql_ha_masterSlaveReplication.png)

**binlog**

**Format**

* Please see [MySQL official documents](https://dev.mysql.com/doc/refman/8.0/en/replication-sbr-rbr.html)

**Statement**

* Statement: not always safe, but may save storage place and faster
  * Pros: 
    1. Low number of logs generated, save I/O bandwidth
    2. Does not require consistency between master and slave table schema 
  * Cons: 
    1. Inapplicable for many SQL statements. For non-deterministic functions, could not gaurantee the consistency between master and slave server
       1. Database locks: Needs to lock a bigger chunk of data

**Row**

* Binlog: always safe, possibly very slow and inefficient in time and space
  * Pros:
    1. Database locks: Only need to lock a specific row
    2. Applicable to any SQL statement. 
  * Cons: 
    1. High number of logs generated
    2. Does not require consistency between master and slave table schema

**Mixed**

* Mixed: best of both worlds in theory. Most queries are replicated by statement. But transactions MySQL knows are non-deterministic are replicated by row. Mixed Mode uses row-based replication for any transaction that:
  * Uses user defined functions
  * Uses the UUID\(\), USER\(\), or CURRENT\_USER\(\) functions
  * Uses LOAD\_FILE \(which otherwise assumes every slave has the exact same file on the local file system and doesn't replicate the data\)
  * Updates two tables with auto\_increment columns \(the binlog format only carries one auto\_increment value per statement\)

**Why MySQL 5.7 default to Row instead of Mixed**

* Main reason is for backup and data recovery
* For example
  * For delete SQL commands, Row based format will record the entire original record. 
  * For insert SQL commands, Row based format will record the inserted record. 
  * For update SQL commands, Row based format will record the before and after record. 

#### Replication delay

**Def**

* The master-slave latency is defined as the difference between T3 and T1. 
  1. Master DB executes a transaction, writes into binlog and finishes at timestamp T1.
  2. The statement is replicated to binlog, Slave DB received it from the binlog T2.
  3. Slave DB executes the transaction and finishes at timestamp T3. 

**Delay sources**

* Inferior slave machines: Slave machine is insuperior to master
* Too much load for slave
  * Causes: Many analytical queries run on top of slave. 
  * Solutions:
    * Multiple slaves
    * Output telemetry to external statistical systems such as Hadoop through binlog 
* Big transactions
  * If a transaction needs to run for as long as 10 minutes on the master database, then it must wait for the transaction to finish before running it on slave. Slave will be behind master for 10 minutes. 
    * e.g. Use del to delete too many records within DB
    * e.g. mySQL DDL within big tables. 
* Before MySQL 5.6, there isn't much built-in support for parallel relay log processing. If there is a steady difference between the write speed on master server and relay speed on slave server, then the replication delay between master and slave could become several hours. For more details, please check this file in [geektime in Chinese](https://time.geekbang.org/column/article/77083)

![Master slave replication process](.gitbook/assets/mysql_ha_masterSlave_multiThreads.png)

**How to reduce replication delay**

* Solution1: After write to master, write to cache as well. 
  * What if write to cache fails
    * If read from master, slave useless
    * If read from slave, still replication delay
* Solution2: If cannot read from slave, then read from master. 
  * It works for DB add operation
  * It doesn't work for DB update operation
* Solution3: If master and slave are located within the same location, synchronous replication

#### Use cases

**Handle old data - Archive**

* Archive old data in time and save disk space

**Use case**

* If a single SQL table's size exceeds 20M rows, then its performance will be slow. Partitioning and Sharding have already been applied. Due to the use cases, there are some hot and cold data, e.g. ecommerce website orders.

**Implementation**

* MySQL engine for archiving
  * InnoDB is based on B+ tree, each time a write happens, the clustered index will need to modified again. The high traffic during archiving process decides that InnoDB will not be a great fit. 
  * TukoDB has higher write performance
* Create archiving tables

```text
CREATE Table t_orders_2021_03 {
    ...
} Engine = TokuDB;
```

* Pt-archiver: One of the utils of Percona-toolkit and used to archive rows from a MySQL table into another table or a file. [https://www.percona.com/doc/percona-toolkit/LATEST/pt-archiver.html](https://www.percona.com/doc/percona-toolkit/LATEST/pt-archiver.html)

**Flowchart**

```text
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

**Backup**

* Recovery test on the backup data

**High availability with failover**

* Usually reliability first is preferred to availability first to avoid data consistency problems. 
* For reliability first failover, the duration for downtime depends on the replication delay between master and slave. 
* Problems to be solved:
  1. After switch, how to notify applications the new address of master
  2. How to determine the master is available
  3. After switch, how to decide on the master-slave replication relationship 

**Two servers**

**Reliability first failover**

* There will be a brief period when both server A and server B are readonly between step \(2-4\)
  1. Check the value of slave server's seconds\_behind\_master, if it is smaller than certain threshold \(e.g. 5s\), continue to next step; Else repeat this step
  2. Change master server A's state to readonly \(set readonly flag to true\)
  3. Check the value of slave server's seconds\_behind\_master until its value becomes 0. 
  4. Change slave server B's state to read-write \(set readonly flag to false\)
  5. Switch the traffic to slave server B

**Approaches**

1. Reliability first - After step 2 and before step4 below, both master and slave will be in readonly state. 

```text
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

![](.gitbook/assets/mysql-replication-failover-reliability.png)

**Availability first failover**

* There is no blank period for availability. The switch always happens immediately. 
* When the binlog format is set to row based, it will be easier to discover the inconsistency between master and slave. 

```text
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

* Example setup: Server A is master and B is slave. 
  * In mixed format, only the statement is sent along. 
  * In row format, the entire record is sent along so it will be easier to detect the conflict \(duplicate key error shown below\)

```text
// table structure and already setup record
mysql> CREATE TABLE `t` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `c` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

insert into t(c) values(1),(2),(3);

// The next examples discuss what happens for an availability first failover
insert into t(c) values(4); // Send to server A
insert into t(c) values(5); // Send to server B
```

![Inconsistency row format mixed](.gitbook/assets/mysql_ha_availabilityfirstMixed.png)

![Inconsistency row format binlog](.gitbook/assets/mysql_ha_availabilityfirstRow.png)

**Multiple servers**

**New challenges - find sync point between multiple servers**

* It has the following setup

![](.gitbook/assets/mysql-replication-failover-multi-machine.png)

* After failing over, A' becomes the new master. The new challenges 

![](.gitbook/assets/mysql-replication-failover-multi-machine-result.png)

**Problem with binlog position**

* Hard to be accurate with MASTER\_LOG\_POS

```text
// The command to execute when set B as A's slave
CHANGE MASTER TO 
MASTER_HOST=$host_name 
MASTER_PORT=$port 
MASTER_USER=$user_name 
MASTER_PASSWORD=$password 
MASTER_LOG_FILE=$master_log_name 
MASTER_LOG_POS=$master_log_pos  // master binlog offset position, hard to be accurate !!!
```

* Reason for the inaccuracy. The sync point is usually found by locating a timestamp in original master server. Then according to this problematic timestamp, find transaction id in new master server. It is hard to guarantee that slave servers are behind new master server \(e.g. might execute the same command twice\)

**GTID to rescue**

* Traditional MySQL replication is based on relative coordinates — each replica keeps track of its position with respect to its current primary’s binary log files. GTID enhances this setup by assigning a unique identifier to every transaction, and each MySQL server keeps track of which transactions it has already executed. This permits “auto-positioning,” the ability for a replica to be pointed at a primary instance without needing to specify a binlog filename or position in the CHANGE PRIMARY statement.
* Auto-positioning makes failover simpler, faster, and less error-prone. It becomes trivial to get replicas in sync after a primary failure, without requiring an external tool such as Master High Availability \(MHA\). Planned primary promotions also become easier, as it is no longer necessary to stop all replicas at the same position first. Database administrators need not worry about manually specifying incorrect positions; even in the case of human error, the server is now smart enough to ignore transactions it has already executed.

```text
CHANGE MASTER TO 
MASTER_HOST=$host_name 
MASTER_PORT=$port 
MASTER_USER=$user_name 
MASTER_PASSWORD=$password 
master_auto_position=1  // This means the replication method is GTID
```

* More detailed explanation available at [Geektime MySQL](https://time.geekbang.org/column/article/77427)

### High concurrent writes conflicts

* Problem: How to prevent overselling for limited inventory products?

#### V1: Serializable DB isolation

* Solution1: Set serializable isolation level in DB

#### V2: Optimistic lock

* Set optimistic lock on the table where multiple writes to a single table happens often. 

```text
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

#### V3: Put inventory number inside Redis

* Redis transaction mechanism:
  * Different from DB transaction, an atomic batch processing mechanism for Redis
  * Similar to put optimistic mechanism inside Redis
* Flowchart

```text
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

```text
// Redis watch data
Redis > Watch inventory_number, userlist

// Start a transaction (execute batch of commands)
Redis > Multi
Redis > DECR inventory_number // reduce number of inventory because it is sold
Redis > RPUSH userlist 1234 // add 1234 user id to userlist who buys the product
Redis > EXEC
```

### High concurrent read but low concurrent writes - Read/Write separation

### High concurrent writes and large volume in a single table: MySQL table partitioning

#### Def

* MySQL table partitioning means to divide one table into multiple partitions and each partition resides on a single disk. 
* Horizontal partitioning means that all rows matching the partitioning function will be assigned to different physical partitions. 
  * When a single table's number of rows exceed 20M, the performance will degrade quickly.
  * Based on certain fields, put **rows of a table** into different tables. 
* Vertical partitioning allows different table columns to be split into different physical partitions. 
  * Put different **fields of a table** into different tables
  * Segmented tables usually share the primary key for correlating data

![Table vertical partition](.gitbook/assets/mysql-vertical-partitioning.png)

![Table horizontal partition](.gitbook/assets/mysql-horizontal-partitioning.png)

#### Benefits

* Storage: It is possible to store more data in one table than can be held on a single disk or file system partition. As known, the upper limit number of rows in a single MySQL is 20M due to the B+ tree depth. MySQL table partitioning enables more rows in any single table because these different partitions are stored in different disks.
* Deletion: Dropping a useless partition is almost instantaneous \(partition level lock\), but a classical DELETE query run in a very large table could lock the entire table \(table level lock\). 
* Partition Pruning: This is the ability to exclude non-matching partitions and their data from a search; it makes querying faster. Also, MySQL 5.7 supports explicit partition selection in queries, which greatly increases the search speed. \(Obviously, this only works if you know in advance which partitions you want to use.\) This also applies for DELETE, INSERT, REPLACE, and UPDATE statements as well as LOAD DATA and LOAD XML.
* A much cheaper option than sharding: Does not need cluster

![](.gitbook/assets/mysql-db-sharding.png)

#### MySQL only supports horizontal partition

* Currently, MySQL supports horizontal partitioning but not vertical. The engine’s documentation clearly states it won’t support vertical partitions any time soon: ”There are no plans at this time to introduce vertical partitioning into MySQL.”
* [https://dev.mysql.com/doc/mysql-partitioning-excerpt/8.0/en/partitioning-overview.html](https://dev.mysql.com/doc/mysql-partitioning-excerpt/8.0/en/partitioning-overview.html)

#### Limitations: Partition column and unique indexes

* Partition Columns: The rule of thumb here is that all columns used in the partitioning expression must be part of every unique key in the partitioned table. This apparently simple statement imposes certain important limitations. 

![](.gitbook/assets/mysql-partitionkey-uniqueindexes.png)

* Parition key could not be used in child query

#### Use cases

* Time Series Data

#### Types

**RANGE Partitioning**

* This type of partition assigns rows to partitions based on column values that fall within a stated range. The values should be contiguous, but they should not overlap each other. The VALUES LESS THAN operator will be used to define such ranges in order from lowest to highest \(a requirement for this partition type\). Also, the partition expression – in the following example, it is YEAR\(created\) – must yield an integer or NULL value.
* Use cases:
  * Deleting Old Data: In the above example, if logs from 2013 need to be deleted, you can simply use ALTER TABLE userslogs DROP PARTITION from\_2013\_or\_less; to delete all rows. This process will take almost no time, whereas running DELETE FROM userslogs WHERE YEAR\(created\) &lt;= 2013; could take minutes if there are lots of rows to delete.
  * Series Data: Working with a range of data expressions comes naturally when you’re dealing with date or time data \(as in the example\) or other types of “series” data.
  * Frequent Queries on the Partition Expression Column: If you frequently perform queries directly involving the column used in the partition expression \(where the engine can determine which partition\(s\) it needs to scan based directly on the WHERE clause\), RANGE is quite efficient. 
* Example

```text
CREATE TABLE userslogs (
    username VARCHAR(20) NOT NULL,
    logdata BLOB NOT NULL,
    created DATETIME NOT NULL,
    PRIMARY KEY(username, created)
)
PARTITION BY RANGE( YEAR(created) )(
    PARTITION from_2013_or_less VALUES LESS THAN (2014),
    PARTITION from_2014 VALUES LESS THAN (2015),
    PARTITION from_2015 VALUES LESS THAN (2016),
    PARTITION from_2016_and_up VALUES LESS THAN MAXVALUE;

// An alternative to RANGE is RANGE COLUMNS, which allows the expression to include more than one column involving STRING, INT, DATE, and TIME type columns (but not functions). In such a case, the VALUES LESS THAN operator must include as many values as there are columns listed in the partition expression. For example:
CREATE TABLE rc1 (
    a INT,
    b INT
)
PARTITION BY RANGE COLUMNS(a, b) (
    PARTITION p0 VALUES LESS THAN (5, 12),
    PARTITION p3 VALUES LESS THAN (MAXVALUE, MAXVALUE)
);
```

**List partitioning**

* LIST partitioning is similar to RANGE, except that the partition is selected based on columns matching one of a set of discrete values. In this case, the VALUES IN statement will be used to define matching criteria.
* Example

```text
CREATE TABLE serverlogs (
    serverid INT NOT NULL, 
    logdata BLOB NOT NULL,
    created DATETIME NOT NULL
)
PARTITION BY LIST (serverid)(
    PARTITION server_east VALUES IN(1,43,65,12,56,73),
    PARTITION server_west VALUES IN(534,6422,196,956,22)
);

// LIST comes with an alternative – LIST COLUMNS. Like RANGE COLUMNS, this statement can include multiple columns and non-INT data types, such as STRING, DATE, and TIME. The general syntax would look like this

CREATE TABLE lc (
    a INT NULL,
    b INT NULL
)
PARTITION BY LIST COLUMNS(a,b) (
    PARTITION p0 VALUES IN( (0,0), (NULL,NULL) ),
    PARTITION p1 VALUES IN( (0,1), (0,2), (0,3), (1,1), (1,2) ),
    PARTITION p2 VALUES IN( (1,0), (2,0), (2,1), (3,0), (3,1) ),
    PARTITION p3 VALUES IN( (1,3), (2,2), (2,3), (3,2), (3,3) )
);
```

**Hash partitioning**

* In HASH partitioning, a partition is selected based on the value returned by a user-defined expression. This expression operates on column values in rows that will be inserted into the table. A HASH partition expression can consist of any valid MySQL expression that yields a nonnegative integer value. HASH is used mainly to evenly distribute data among the number of partitions the user has chosen.
* Example

```text
CREATE TABLE serverlogs2 (
    serverid INT NOT NULL, 
    logdata BLOB NOT NULL,
    created DATETIME NOT NULL
)
PARTITION BY HASH (serverid)
PARTITIONS 10;
```

#### References

* [https://www.vertabelo.com/blog/everything-you-need-to-know-about-mysql-partitions/](https://www.vertabelo.com/blog/everything-you-need-to-know-about-mysql-partitions/)

### High concurrent writes and large volume across tables: MySQL DB Sharding

#### Use cases

**Last resort**

* Sharding should be used as a last resort after you exhausted the following:
  * Add cache
  * Add read-write separation
  * Consider table partition

**Triggers**

* Only use in OLTP cases \(OLAP is more likely to have complex changing SQL queries\)
* A single table's capacity reaches 2GB. 
* A database should not contain more than 1,000 tables.
* Each individual table should not exceed 1 GB in size or 20 million rows;
* The total size of all the tables in a database should not exceed 2 GB.

**Capacity planning**

* For fast growing data \(e.g. order data in ecommerce website\), use 2X planned capacity to avoid resharding
* For slow growing data \(e.g. user identity data in ecommerce website\), use 3-year estimated capacity to avoid resharding. 

#### Introduced problems

**How to choose sharding key**

* Avoid cross shard joins
* Make data distribution even across shards

**Choose the number of shards**

* If has a cap on storage:
  * Each shard could contain at most 1TB data.
  * number of shards = total storage / 1TB
* If has a cap on number of records:
  * Suppose the size of row is 100 bytes
    * User table: uid \(long 8 bytes\), name \(fixed char 16 bytes\), city \(int 4 bytes\), timestamp \(long 8 bytes\), sex \(int 4 bytes\), age \(int 4 bytes\) = total 40 bytes
  * Total size of the rows: 100 bytes \* Number\_of\_records
  * number of shards = total size of rows / 1TB

**Limited SQL queries**

* Not all single node SQL will be supported. 
* See this for [a detailed example of ShardingSphere](https://shardingsphere.apache.org/document/current/en/features/sharding/use-norms/sql/)

**Sharding stratgies**

**Lookup strategy**

* Pros:
  * Easy to migrate data
* Cons: 
  * Need an additional hop when query
  * If the lookup table is really big, it could also need to be sharded

![lookup](.gitbook/assets/mysql_sharding_lookupstrategy.png)

**Range strategy**

* Pros:
  * Easy to add a new shard. No need to move the original data. For example, each month could have a new shard.
* Cons:
  * Uneven distribution. For example, July is the hot season but December is the cold season. 

![range](.gitbook/assets/mysql_sharding_rangestrategy.png)

**By customer or tenant**

* If it is a SaaS business, it is often true that data from one customer doesn't interact with data from any of your other customers. These apps are usually called multi-tenant apps. 
  * Multi-tenant apps usually require strong consistency where transaction is in place and data loss is not possible. 
  * Multi-tenant data usually evolves over time to provide more and more functionality. Unlike consumer apps which benefit from network effects to grow, B2B applications grows by adding new features for customers. 

**By geography**

* Apps such as postmate, lyft or instacart.
* You’re not going to live in Alabama and order grocery delivery from California. And if you were to order a Lyft pick-up from California to Alabama you’ll be waiting a good little while for your pickup.

**By time**

* Time sharding is incredibly common when looking at some form of event data. Event data may include clicks/impressions of ads, it could be network event data, or data from a systems monitoring perspective.
* This approach should be used when
  * You generate your reporting/alerts by doing analysis on the data with time as one axis.
  * You’re regularly rolling off data so that you have a limited retention of it.

**Hash strategy**

![range](.gitbook/assets/mysql_sharding_hashstrategy.png)

**By entity id**

* Shard based on hashing value of a field. 
* Pros:
  * Evenly distributed data
* Cons:
  * Hard to add a new shard. Lots of data migration need to happen. 

**How to store unsharded table**

* Store a copy within each shard
  * Cons: Write across to guarantee consistency
* Use a centralized node to store
  * Cons: Need to have cross shard joins

**How to deploy shards on nodes**

* Each shard gets stored in a separate database, and each database gets stored in a separate node. 
* Each shard gets stored in a separate database, and multiple database gets stored in a single node. 

**Cross shard join**

* Query types:
  * Join queries: 
  * count queries:
  * order by queries:
* Solutions:
  * Aggregate query result for different shard within application code.
  * Usually use two sets of data to solve the problem
    * One data is based on unique sharding key.
    * The other one is data replicated asynchronously to Elasticsearch or Solr.

**Distributed transactions \(write across shards\)**

* Original transaction needs to be conducted within a distributed transaction.
  * e.g. ecommerce example \(order table and inventory table\)
* There are wwo ways in general to implement distributed transactions:
  * 2PC 
  * TCC
* For example, some software has built-in implementations such as:
  * MySQL XA
  * Spring JTA

**Unique global ID**

* Please see [ID generator](https://github.com/DreamOfTheRedChamber/system-design-interviews/blob/master/Scenario_IDGenerator.md)

**Challenges in Graph DB sharding**

* Graph model is most common in B2C apps like Facebook and Instagram. 
* With this model, data is often replicated in a few different forms. Then it is the responsibility of the application to map to the form that is most useful to acquire the data. The result is you have multiple copies for your data sharded in different ways, eventual consistency of data typically, and then have some application logic you have to map to your sharding strategy. For apps like Facebook and Reddit there is little choice but to take this approach, but it does come at some price.

#### ShardingSphere

* ShardingSphere has three solutions: ShardingJDBC / ShardingProxy / ShardingSphere

**Sharding JDBC**

![](.gitbook/assets/mysql-sharding-jdbc.png)

**Sharding Proxy**

![](.gitbook/assets/mysql-sharding-proxy.png)

#### Sharding example \(In Chinese\)

* Original table

![](.gitbook/assets/mysql-sharding-ecommerce-example.png)

* Sharded result
  * Vertical sharding: Store, product and Sku should be stored in three different databases.
  * Fast growing table: Among all three database, Sku table will grow much faster than product and store. 
  * Binding table: Sku and SkuInfo always appear together. Product and productType usually appear together. They should be sharded according to the same column. 

![](.gitbook/assets/mysql-sharding-ecommerce-example-result.png)

### Architecture example - Replication + PXC + Sharding proxy

* PXC is a type of strong consistency MySQL cluster built on top of Galera. It could store data requring high consistency. 
* Replication is a type of weak consistency MySQL cluster shipped with MySQL based on binlog replication. It could be used to store data only requiring low consistency. 

```text
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

* [Storage disaster recovery](storage_mysql_scalability.md#storage-disaster-recovery)
  * [Architecture revolution](storage_mysql_scalability.md#architecture-revolution)
    * [Disaster recovery with backup DCs](storage_mysql_scalability.md#disaster-recovery-with-backup-dcs)
    * [Two active DCs with full copy of data](storage_mysql_scalability.md#two-active-dcs-with-full-copy-of-data)
      * [Latency metrics for geographical distance](storage_mysql_scalability.md#latency-metrics-for-geographical-distance)
      * [Architecture for two DCs within a city](storage_mysql_scalability.md#architecture-for-two-dcs-within-a-city)
      * [Architecture for two DCs in different cities](storage_mysql_scalability.md#architecture-for-two-dcs-in-different-cities)
    * [Architecture for multi active DCs with sharded data](storage_mysql_scalability.md#architecture-for-multi-active-dcs-with-sharded-data)
    * [Architecture for three DCs in two cities](storage_mysql_scalability.md#architecture-for-three-dcs-in-two-cities)
    * [Architecture for five DCs in three cities](storage_mysql_scalability.md#architecture-for-five-dcs-in-three-cities)
  * [Synchronization architecture](storage_mysql_scalability.md#synchronization-architecture)
    * [Message queue based](storage_mysql_scalability.md#message-queue-based)
    * [RPC based](storage_mysql_scalability.md#rpc-based)
  * [Database](storage_mysql_scalability.md#database)
    * [MySQL data replication](storage_mysql_scalability.md#mysql-data-replication)
    * [NoSQL data replication](storage_mysql_scalability.md#nosql-data-replication)
    * [NewSQL data replication](storage_mysql_scalability.md#newsql-data-replication)
* [References:](storage_mysql_scalability.md#references)

### Architecture example - Disaster recovery

#### One active and the other cold backup machine

* Definition:
  * Master databases in City A serves all traffic. Backup databases are only for backup purpose. 
  * To be tolerant against failure in DC1. Deploy another backup DC 2 within the same city. 
  * To be tolerant against failure for entire city A. Deploy another backup DC 3 in city B. 
* Failover: 
  * If master database goes down, fail over to backup database. 
* Pros:
  * Improve availability. 
* Cons: 
  * Backup DC capacity is not used 100%.
  * No confidence that after failing over to backup DC, it could still serve traffic.
  * Could not serve larger traffic volume. 

```text
┌─────────────────────────────────┐  ┌──────────────────────────────────┐
│             City A              │  │              City B              │
│                                 │  │                                  │
│     ┌───────────────────┐       │  │       ┌───────────────────┐      │
│     │                   │       │  │       │                   │      │
│     │   Load balancer   │       │  │       │   Load balancer   │      │
│     │                   │       │  │       │                   │      │
│     └───────────────────┘       │  │       └───────────────────┘      │
│               │                 │  │                 │                │
│               │                 │  │                 │                │
│       ┌───────┴────────┐        │  │         ┌───────┴────────┐       │
│       │                │        │  │         │                │       │
│       │                │        │  │         │                │       │
│       ▼                ▼        │  │         ▼                ▼       │
│ ┌───────────┐    ┌───────────┐  │  │   ┌───────────┐    ┌───────────┐ │
│ │           │    │           │  │  │   │           │    │           │ │
│ │App system │    │App system │  │  │   │App system │    │App system │ │
│ │           │    │           │  │  │   │           │    │           │ │
│ └───────────┘    └───────────┘  │  │   └───────────┘    └───────────┘ │
│       │                │        │  │         │                │       │
│       │                │        │  │         │                │       │
│       │                │        │  │         │                │       │
│       └───────┬────────┴────────┼──┼─────────┴────────────────┘       │
│               │                 │  │                                  │
│               │                 │  │                                  │
│               │                 │  │                                  │
│               ▼                 │  │                                  │
│    ┌─────────────────────┐      │  │     ┌─────────────────────┐      │
│    │                     │      │  │     │                     │      │
│    │  Master database 1  │────backup────▶│  Backup database 2  │      │
│    │                     │      │  │     │                     │      │
│    └─────────────────────┘      │  │     └─────────────────────┘      │
│                                 │  │                                  │
└─────────────────────────────────┘  └──────────────────────────────────┘
```

#### Two active DCs with full copy of data

* Definition:
  * Master DCs serve read/write traffic. Slave DCs only serve read traffic. All master DCs have full copy of data. 
  * Slave DCs redirect write traffic to master DCs. 
* Failover: 
  * If DC 1 goes down, fail over to DC 2. 
  * If entire city A goes down, fail over to DC 3. 
* Pros:
  * Can be horizontally scaled to multiple DCs. 
* Cons:
  * Each DC needs to have full copy of data to be fault tolerant. 
  * To avoid write conflicts, two masters could not process the same copy of data. 

**Same city vs different city**

* The following table summarizes the differences of these two pattern

| Dimensions | two data centers within the same city | two data centers in different cities |
| :--- | :--- | :--- |
| Definition | Two DCs are located close to each other geographically. For example, the two DCs are within the same city | Two DCs are located distant from each other geographically. For example, the two DCs are cross region \(e.g. New York and Log Angeles\), or even cross continent \(e.g. USA and Australia\) |
| Cost | high \(DC itself and dedicated line with same city\) | extremely high \(DC itself and dedicated line across same region/continent\) |
| Complexity | Low. Fine to call across DCs due to low latency | High. Need to rearchitect due to high latency |
| Service quality | Increase latency a bit / increase availability | Decrease latency  / increase availability |

**Two active DCs within a city**

* Since the latency within the same city will be low, it is fine to have one centralized database layer and have cross DC remote calls. 

```text
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                              City                                               │
│                                                                                                 │
│                                                                                                 │
│ ┌───────────────────────────────────────────────┐     ┌────────────────────────────────────┐    │
│ │                      DC1                      │     │                DC2                 │    │
│ │                                               │     │                                    │    │
│ │      ┌─────────────────────────────────────┐  │     │    ┌─────────────────────────────┐ │    │
│ │      │                                     │  │     │    │                             │ │    │
│ │      │         Application Servers         │  │     │    │     Application Servers     │ │    │
│ │      │                                     │  │     │    │                             │ │    │
│ │      └─────────────────────────────────────┘  │     │    └─────────────────────────────┘ │    │
│ │                        │                      │     │                    │      │        │    │
│ │                        │                      │     │                    │      │        │    │
│ │                        │                      │     │                    │      │        │    │
│ │                        │                      │     │                    │      │        │    │
│ │                        │                      │     │                    │      │        │    │
│ │            ┌───read────┘           ┌──────────┼─────┼─────write──────────┘      │        │    │
│ │            │                       │          │     │                         read       │    │
│ │            │                       │          │     │                           │        │    │
│ │            │                       │          │     │                           │        │    │
│ │            │                       │          │     │                           │        │    │
│ │            │                       │          │     │                           │        │    │
│ │            ▼                       ▼          │     │                           ▼        │    │
│ │  ┌──────────────────┐    ┌──────────────────┐ │     │              ┌──────────────────┐  │    │
│ │  │    read slave    │    │   write master   │ │     │              │    read slave    │  │    │
│ │  │    components    │    │    components    │ │     │              │    components    │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │┌───────────┐     │    │  ┌───────────┐   │ │     │              │    ┌───────────┐ │  │    │
│ │  ││   Slave   │     │    │  │  Master   │   │ │     │              │    │   Slave   │ │  │    │
│ │  ││  service  ◀─synchronize─┤  service  │ ──┼─┼─────synchronize────┼──▶ │  service  │ │  │    │
│ │  ││ discovery │     │    │  │ discovery │   │ │     │              │    │ discovery │ │  │    │
│ │  │└───────────┘     │    │  └───────────┘   │ │     │              │    └───────────┘ │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  │┌───────────┐     │    │  ┌───────────┐   │ │     │              │    ┌───────────┐ │  │    │
│ │  ││   Slave   │     │    │  │  Master   │   │ │     │              │    │           │ │  │    │
│ │  ││ Database ◀┼synchronize──┼─  Cache   │───┼─┼───synchronize──────┼──▶ │Slave Cache│ │  │    │
│ │  ││           │     │    │  │           │   │ │     │              │    │           │ │  │    │
│ │  │└───────────┘     │    │  └───────────┘   │ │     │              │    └───────────┘ │  │    │
│ │  │                  │    │         │        │ │     │              │                  │  │    │
│ │  │                  │    │         │        │ │     │              │                  │  │    │
│ │  │                  │    │         │        │ │     │              │                  │  │    │
│ │  │                  │    │         │        │ │     │              │                  │  │    │
│ │  │                  │    │         │        │ │     │              │                  │  │    │
│ │  │                  │    │         ▼        │ │     │              │                  │  │    │
│ │  │┌───────────┐     │    │   ┌───────────┐  │ │     │              │    ┌───────────┐ │  │    │
│ │  ││   Slave   │     │    │   │  Master   │  │ │     │              │    │   Slave   │ │  │    │
│ │  ││ Database  ◀─synchronize──│ Database  │──┼─┼───synchronize──────┼───▶│ Database  │ │  │    │
│ │  ││           │     │    │   │           │  │ │     │              │    │           │ │  │    │
│ │  │└───────────┘     │    │   └───────────┘  │ │     │              │    └───────────┘ │  │    │
│ │  │                  │    │                  │ │     │              │                  │  │    │
│ │  └──────────────────┘    └──────────────────┘ │     │              └──────────────────┘  │    │
│ │                                               │     │                                    │    │
│ │                                               │     │                                    │    │
│ └───────────────────────────────────────────────┘     └────────────────────────────────────┘    │
│                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

**Two active DCs in different cities**

* Since the latency between two DCs across region/continent will be high, it is only possible to sync the data asynchronously. 

```text
┌──────────────────────────────────────────────────┐    ┌──────────────────────────────────────────────────┐
│                      City A                      │    │                      City B                      │
│                                                  │    │                                                  │
│  ┌──────────────────────────────────────────────┐│    │┌──────────────────────────────────────────────┐  │
│  │                     DC1                      ││    ││                     DC3                      │  │
│  │                                              ││    ││                                              │  │
│  │  ┌─────────────────────────────────────────┐ ││    ││ ┌─────────────────────────────────────────┐  │  │
│  │  │                                         │ ││    ││ │                                         │  │  │
│  │  │           Application Servers           │ ││    ││ │           Application Servers           │  │  │
│  │  │                                         │ ││    ││ │                                         │  │  │
│  │  └─────────────────────────────────────────┘ ││    ││ └─────────────────────────────────────────┘  │  │
│  │           │                     │            ││    ││                                │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │         read                 write           ││    ││             │                  │             │  │
│  │           │                     │            ││    ││          write               read            │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           │                     │            ││    ││             │                  │             │  │
│  │           ▼                     ▼            ││    ││             ▼                  ▼             │  │
│  │  ┌────────────────┐  ┌────────────────┐      ││    ││     ┌───────────────┐   ┌──────────────────┐ │  │
│  │  │   read slave   │  │  write master  │      ││    ││     │ write master  │   │    read slave    │ │  │
│  │  │   components   │  │   components   │      ││    ││     │  components   │   │    components    │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │  ┌───┴┴────┴┴─┐   │               │   │                  │ │  │
│  │  │┌───────────┐   │  │ ┌───────────┐  │  │            │   │ ┌───────────┐ │   │  ┌───────────┐   │ │  │
│  │  ││   Slave   │   │  │ │  Master   │  │  │  message   │   │ │  Master   │ │   │  │   Slave   │   │ │  │
│  │  ││  service  ◀──sync┼─┼─ service  │◀─┼──┤ queue for ─┼───┼▶│  service ─┼─sync┼──▶  service  │   │ │  │
│  │  ││ discovery │   │  │ │ discovery │  │  │    sync    │   │ │ discovery │ │   │  │ discovery │   │ │  │
│  │  │└───────────┘   │  │ └───────────┘  │  │            │   │ └───────────┘ │   │  └───────────┘   │ │  │
│  │  │                │  │                │  │            │   │               │   │                  │ │  │
│  │  │                │  │                │  └───┬┬────┬┬─┘   │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │┌───────────┐   │  │ ┌───────────┐  │      ││    ││     │ ┌───────────┐ │   │  ┌───────────┐   │ │  │
│  │  ││   Slave   │   │  │ │  Master   │  │      direct db    │ │  Master   │ │   │  │           │   │ │  │
│  │  ││ Database ◀┼─sync─┼─┤   Cache   │  │◀─────┼┼sync┼┼────▶│ │ Database  ├─sync┼──▶Slave Cache│   │ │  │
│  │  ││           │   │  │ │           │  │      ││    ││     │ │           │ │   │  │           │   │ │  │
│  │  │└───────────┘   │  │ └───────────┘  │      ││    ││     │ └───────────┘ │   │  └───────────┘   │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  │┌───────────┐   │  │ ┌───────────┐  │      ││    ││     │ ┌───────────┐ │   │   ┌───────────┐  │ │  │
│  │  ││   Slave   │   │  │ │  Master   │  │     direct db     │ │  Master   │ │   │   │   Slave   │  │ │  │
│  │  ││ Database  ◀─sync─┼─│ Database  │  ◀──────┼sync─┼┼────▶│ │ Database  │─┼sync──▶│ Database  │  │ │  │
│  │  ││           │   │  │ │           │  │      ││    ││     │ │           │ │   │   │           │  │ │  │
│  │  │└───────────┘   │  │ └───────────┘  │      ││    ││     │ └───────────┘ │   │   └───────────┘  │ │  │
│  │  │                │  │                │      ││    ││     │               │   │                  │ │  │
│  │  └────────────────┘  └────────────────┘      ││    ││     └───────────────┘   └──────────────────┘ │  │
│  │                                              ││    ││                                              │  │
│  │                                              ││    ││                                              │  │
│  └──────────────────────────────────────────────┘│    │└──────────────────────────────────────────────┘  │
│                                                  │    │                                                  │
│ ┌───────────────────────────────────────────────┐│    │┌───────────────────────────────────────────────┐ │
│ │                      DC2                      ││    ││                      DC4                      │ │
│ │                    ......                     ││    ││                    ......                     │ │
│ │                                               ││    ││                                               │ │
│ │                                               ││    ││                                               │ │
│ │                                               ││    ││                                               │ │
│ │                                               ││    ││                                               │ │
│ └───────────────────────────────────────────────┘│    │└───────────────────────────────────────────────┘ │
│                                                  │    │                                                  │
└──────────────────────────────────────────────────┘    └──────────────────────────────────────────────────┘
```

#### Multi active DCs with sharded data

* Definition:
  * Request routing:
    * API Router Layer: Route external API calls to the correct DC.
    * Internal DC call Router: Within a sharded DC, route cross DC calls. 
    * Global Coordinator Service: Maintains the mapping from shard key -&gt; shard id -&gt; DC
      * Shard key varies with each request.
      * Shard Id -&gt; DC mapping does not change much.
  * Data:
    * Sharded DC: Contains eventual consistent sharded data. For example, in case of ecommerce system, each buyer has their own orders, comments, user behaviors. 
    * Global zone DC: Contains strong consistent global data. For example, in case of ecommerce system, all users will see the same inventory.
* Typical flow: 
  * Step1. A request comes to API router layer with sharding keys \(geographical location, user Id, order Id\)
  * Step2. The API router layer component will determine the DC which contains the shard
  * Step3. 
  * Step4. \(Optional\) It will call "Inter DC Call Router" in case it needs to use data in another sharded DC \(e.g. Suppose the sharded DC is based on geographical location, a buyer on an ecommerce website wants to look at a seller's product who is in another city.\)
  * Step5. \(Optional\) It will call "Global zone" in case it needs to access the global strong consistent data \(e.g. \)

```text
    ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                                                       
     Step 1. Requests come with logical sharding keys                                                         
    │such as geographical location, user ID, order ID │                                                       

    └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘                                                       
                             │                                                                                
                             │                                                                                
                             ▼                                                                                
 ┌──────────────────────────────────────────────────────┐                    ┌─────────────────────────────┐  
 │                   API Router Layer                   │                    │Global Coordinator Service   │  
 │  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │                    │                             │  
 │   Step2. Calculate DC Id by                          │                    │Maintains a mapping from     │  
 │  │                                                 │ │                    │shard key to shard id to     │  
 │   a. Calculate shard Id from logical sharding key    │─────subscribe────▶ │ezone                        │  
 │  │(geographical location, user ID, Order ID)       │ │                    │                             │  
 │   b. Calculate DC Id based on shard Id               │  ┌──subscribe────▶ │                             │  
 │  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘ │  │                 │                             │  
 └──────────────────────────────────────────────────────┘  │                 │                             │  
                             │                             │                 └─────────────────────────────┘  
                             └──┬──────────────────────────┼──────────────────────────┐                       
                                │                          │                          │                       
                                ▼                          │                          ▼                       
        ┌───────────────────────────────────────────────┐  │  ┌──────────────────────────────────────────────┐
        │                  Sharded DC1                  │  │  │                 Sharded DC2                  │
        │                                               │  │  │                                              │
        │  ┌─────────────────────────────────────────┐  │  │  │  ┌─────────────────────────────────────────┐ │
        │  │                                         │  │  │  │  │                                         │ │
        │  │           Application Servers           │  │  │  │  │           Application Servers           │ │
        │  │                                         │  │  │  │  │                                         │ │
        │  └────────┬─────────────────────┬──────────┘  │  │  │  └──────────┬──────────────────┬───────────┘ │
        │           │                     │             │  │  │             │                  │             │
        │           │                     │             │  │  │             │                  │             │
        │           │                     │             │  │  │             │                  │             │
        │           │                     │    ┌────────┴─────┴─────┐       │                  │             │
        │           │                     │    │Inter DC Call Router│       │                  │             │
        │           │                     │    │                    │       │                  │             │
        │           │                     │    │┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │       │                  │             │
┌ ─ ─ ─ ┴ ─ ─       │                     │    │ Step3. DC router   │       │                  │             │
             │      │                     │    ││for routing cross│ │       │                  │             │
│Step4. read        │                     │    │     DC calls       │       │                  │             │
  from slave │      │                     │    │└ ─ ─ ─ ─ ─ ─ ─ ─ ┘ │       │                  │             │
│and write to       │                     │    └────────┬─────┬─────┘       │                  │             │
    master   │    read                 write            │     │          write               read            │
│                   │                     │             │     │             │                  │             │
 ─ ─ ─ ─│─ ─ ┘      │                     │             │     │             │                  │             │
        │           │                     │             │     │             │                  │             │
        │           ▼                     ▼             │     │             ▼                  ▼             │
        │  ┌────────────────┐  ┌────────────────┐       │     │     ┌───────────────┐   ┌──────────────────┐ │
        │  │   read slave   │  │  write master  │       │     │     │ write master  │   │    read slave    │ │
        │  │   components   │  │   components   │       │     │     │  components   │   │    components    │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │  ┌────┴─────┴─┐   │               │   │                  │ │
        │  │┌───────────┐   │  │ ┌───────────┐  │  │            │   │ ┌───────────┐ │   │  ┌───────────┐   │ │
        │  ││   Slave   │   │  │ │  Master   │  │  │  message   │   │ │  Master   │ │   │  │   Slave   │   │ │
        │  ││  service  ◀──sync┼─┼─ service  │◀─┼──┤ queue for ─┼───┼▶│  service ─┼─sync┼──▶  service  │   │ │
        │  ││ discovery │   │  │ │ discovery │  │  │    sync    │   │ │ discovery │ │   │  │ discovery │   │ │
        │  │└───────────┘   │  │ └───────────┘  │  │            │   │ └───────────┘ │   │  └───────────┘   │ │
        │  │                │  │                │  │            │   │               │   │                  │ │
        │  │                │  │                │  └────┬─────┬─┘   │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │┌───────────┐   │  │ ┌───────────┐  │       │     │     │ ┌───────────┐ │   │  ┌───────────┐   │ │
        │  ││   Slave   │   │  │ │  Master   │  │       │     │     │ │  Master   │ │   │  │           │   │ │
        │  ││ Database ◀┼─sync─┼─┤   Cache   │  │       │     │     │ │   Cache   ├─sync┼──▶Slave Cache│   │ │
        │  ││           │   │  │ │           │  │       │     │     │ │           │ │   │  │           │   │ │
        │  │└───────────┘   │  │ └───────────┘  │       │     │     │ └───────────┘ │   │  └───────────┘   │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  │┌───────────┐   │  │ ┌───────────┐  │       │     │     │ ┌───────────┐ │   │   ┌───────────┐  │ │
        │  ││   Slave   │   │  │ │  Master   │  │     direct db     │ │  Master   │ │   │   │   Slave   │  │ │
        │  ││ Database  ◀─sync─┼─│ Database  │  ◀───────sync──┼────▶│ │ Database  │─┼sync──▶│ Database  │  │ │
        │  ││           │   │  │ │           │  │       │     │     │ │           │ │   │   │           │  │ │
        │  │└───────────┘   │  │ └───────────┘  │       │     │     │ └───────────┘ │   │   └───────────┘  │ │
        │  │                │  │                │       │     │     │               │   │                  │ │
        │  └────────────────┘  └────────────────┘       │     │     └───────────────┘   └──────────────────┘ │
        └───────────────────────────────────────────────┘     └──────────────────────────────────────────────┘
                                │                                                     │                       
 ┌ ─ ─ ─ ─ ─ ─                  │                                                     │                       
              │                 │                                                     │                       
 │   Step5.           ┌─────────┴───────────────┬─────────────────────────────────────┤                       
   Read/write │     read                     write                                  read                      
 │ to global  ────────┼─────────────────────────┼─────────────────────────────────────┼──────────────────┐    
      zone    │       │                         │                                     │                  │    
 │                    ▼                         ▼                                     ▼                  │    
  ─ ─ ─ ┬ ─ ┬─┴────────────────┐      ┌──────────────────┐                      ┌──────────────────┐     │    
        │   │    read slave    │      │   write master   │                      │    read slave    │     │    
        │   │    components    │      │    components    │                      │    components    │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │ ┌───────────┐    │      │ ┌───────────┐    │                      │   ┌───────────┐  │     │    
        │   │ │   Slave   │    │      │ │  Master   │    │                      │   │   Slave   │  │     │    
        │   │ │  service  ◀─synchronize─┤  service  │ ───┼──────synchronize─────┼─▶ │  service  │  │     │    
        │   │ │ discovery │    │      │ │ discovery │    │                      │   │ discovery │  │     │    
        │   │ └───────────┘    │      │ └───────────┘    │                      │   └───────────┘  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │ ┌───────────┐    │      │ ┌───────────┐    │                      │   ┌───────────┐  │     │    
        │   │ │   Slave   │    │      │ │  Master   │    │                      │   │           │  │     │    
        │   │ │ Database ◀┼synchronize┼─┼─  Cache   │────┼────synchronize───────┼─▶ │Slave Cache│  │     │    
        │   │ │           │    │      │ │           │    │                      │   │           │  │     │    
        │   │ └───────────┘    │      │ └───────────┘    │                      │   └───────────┘  │     │    
        │   │                  │      │        │         │                      │                  │     │    
        │   │                  │      │        │         │                      │                  │     │    
        │   │                  │      │        │         │                      │                  │     │    
        │   │                  │      │        │         │                      │                  │     │    
        │   │                  │      │        │         │                      │                  │     │    
        │   │                  │      │        ▼         │                      │                  │     │    
        │   │ ┌───────────┐    │      │  ┌───────────┐   │                      │   ┌───────────┐  │     │    
        │   │ │   Slave   │    │      │  │  Master   │   │                      │   │   Slave   │  │     │    
        │   │ │ Database  ◀─synchronize──│ Database  │───┼────synchronize───────┼──▶│ Database  │  │     │    
        │   │ │           │    │      │  │           │   │                      │   │           │  │     │    
        │   │ └───────────┘    │      │  └───────────┘   │                      │   └───────────┘  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   │                  │      │                  │                      │                  │     │    
        │   └──────────────────┘      └──────────────────┘                      └──────────────────┘     │    
        │                                                                                                │    
        │                            Global Zone for strong consistency data                             │    
        └────────────────────────────────────────────────────────────────────────────────────────────────┘
```

**Synchronization mechanisms**

* Reship component: Forward the write requests coming in local DC to remote DCs.
* Collector component: Read write requests from remote DCs and write to local DC. 
* Elastic search component: Update to DC requests are all written to elastic search to guarantee strong consistency. 

**Message queue based**

```text
┌─────────────────────────────────────────────┐                ┌────────────────────────────────────────────────┐
│                    DC 1                     │                │                      DC 2                      │
│                                             │                │                                                │
│              ┌──────────────┐               │                │               ┌──────────────┐                 │
│              │              │               │                │               │              │                 │
│              │    Client    │               │                │               │    Client    │                 │
│              │              │               │                │               │              │                 │
│              └──────────────┘               │                │               └───────┬──────┘                 │
│                      │                      │                │                       │                        │
│                      │                      │                │                       └──────────────┐         │
│         ┌─Step 1─────┘                      │                │                                      │         │
│         │                ┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐│                │┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐                   │         │
│         ▼                  ┌──────────────┐ │                │  ┌──────────────┐                    ▼         │
│ ┌──────────────┐         │ │              │││                ││ │              ││           ┌──────────────┐  │
│ │              │           │Message parser│ │                │  │Message parser│            │              │  │
│ │   Cache/DB   │◀────────│ │              │││                ││ │              ││──Step 6──▶│   Cache/DB   │  │
│ │              │           └──────────────┘ │                │  └──────────────┘            │              │  │
│ └──────────────┘         │Collector        ││                ││Collector        │           └──────────────┘  │
│         │                 component         │                │ component                            │         │
│         │                └ ─ ─ ─ ─ ─ ─ ─ ─ ┘│                │└ ─ ─ ─ ─ ─ ─ ─ ─ ┘                   │         │
│      Step 2                       ▲         │                │         ▲                            ▼         │
│         │                         │         │                │      Step 5                 ┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐│
│┌ ─ ─ ─ ─▼─ ─ ─ ─ ┐                │         │                │         │                                      │
│ ┌──────────────┐          ┌──────────────┐  │                │ ┌──────────────┐            │ ┌──────────────┐││
│││  Listen to   │ │        │              │  │                │ │              │              │  Listen to   │ │
│ │cache keyspace│──Step 3─▶│Message Queue │──┼──────Step 4────┼▶│Message Queue │◀───────────│ │cache keyspace│││
│││ notification │ │        │              │◀─┼────────────────┼─┤              │              │ notification │ │
│ └──────────────┘          └──────────────┘  │                │ └──────────────┘            │ └──────────────┘││
││                 │                │         │                │         │                    Reship component  │
│ Reship component                  │         │                │         │                   └ ─ ─ ─ ─ ─ ─ ─ ─ ┘│
│└ ─ ─ ─ ─ ─ ─ ─ ─ ┘                │         │                │         │                                      │
└───────────────────────────────────┼─────────┘                └─────────┼──────────────────────────────────────┘
                                    └──────────────────┬─────────────────┘                                       
                                                       │                                                         
                                                       │                                                         
                                                       │                                                         
                                                       ▼                                                         
                                    ┌────────────────────────────────────┐                                       
                                    │ElasticSearch Cluster for detecting │                                       
                                    │   failures and instructing retry   │                                       
                                    │                                    │                                       
                                    │   Request could be identified by   │                                       
                                    │           DC + RequestId           │                                       
                                    └────────────────────────────────────┘
```

**RPC based**

```text
┌─────────────────────────────┐                ┌─────────────────────────────┐
│            DC 1             │                │            DC 2             │
│                             │                │                             │
│                             │                │                             │
│      ┌──────────────┐       │                │       ┌──────────────┐      │
│      │              │       │                │       │              │      │
│      │    Client    │       │                │       │    Client    │      │
│      │              │       │                │       │              │      │
│      └──────────────┘       │                │       └──────────────┘      │
│                             │                │                             │
│                             │                │                             │
│   ┌─────────────────────┐   │                │    ┌─────────────────────┐  │
│   │         RPC         │   │                │    │         RPC         │  │
│   │                     │   │                │    │                     │  │
│   │ ┌ ─ ─ ─ ─ ─ ─ ─ ─   │   │                │    │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │  │
│   │  Reship component│  │   │                │    │      Collector      │  │
│   │ │                 ──┼───┼────────────────┼────┼▶│    component    │ │  │
│   │  ─ ─ ─ ─ ─ ─ ─ ─ ┘  │   │                │    │  ─ ─ ─ ─ ─ ─ ─ ─ ─  │  │
│   │                     │   │                │    │                     │  │
│   │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │   │                │    │ ┌ ─ ─ ─ ─ ─ ─ ─ ─   │  │
│   │      Collector      │   │                │    │  Reship component│  │  │
│   │ │    component    │◀┼───┼────────────────┼────┼─│                   │  │
│   │  ─ ─ ─ ─ ─ ─ ─ ─ ─  │   │                │    │  ─ ─ ─ ─ ─ ─ ─ ─ ┘  │  │
│   │                     │   │                │    │                     │  │
│   │ ┌ ─ ─ ─ ─ ─ ─ ─ ─   │   │                │    │ ┌ ─ ─ ─ ─ ─ ─ ─ ─   │  │
│   │     Processor    │  │   │                │    │     Processor    │  │  │
│   │ │   component       │   │                │    │ │   component       │  │
│   │  ─ ─ ─ ─ ─ ─ ─ ─ ┘  │   │                │    │  ─ ─ ─ ─ ─ ─ ─ ─ ┘  │  │
│   └──────────┬──────────┘   │                │    └──────────┬──────────┘  │
│              │              │                │               │             │
│              │              │                │               │             │
└──────────────┼──────────────┘                └───────────────┼─────────────┘
               │                                               │              
               │                                               │              
               │                                               │              
               │     ┌────────────────────────────────────┐    │              
               │     │ElasticSearch Cluster for detecting │    │              
               │     │   failures and instructing retry   │    │              
               └────▶│                                    │◀───┘              
                     │   Request could be identified by   │                   
                     │           DC + RequestId           │                   
                     └────────────────────────────────────┘
```

#### Distributed database \(Two cities / three DCs and five copies\)

* For distributed ACID database, the basic unit is sharding. And the data consensus is achieved by raft protocol. 

**Pros**

* Disaster recovery support:
  * If any server room within city A is not available, then city B server room's vote could still form majority with the remaining server room in city A. 

```text
┌──────────────────────────────────────────────────────────────────────────────────────────────┐  ┌────────────────────────────┐
│                                            City A                                            │  │           City B           │
│  ┌───────────────────────────────────────────┐ ┌───────────────────────────────────────────┐ │  │ ┌─────────────────────────┐│
│  │               Server Room A               │ │               Server Room B               │ │  │ │      Server Room C      ││
│  │                                           │ │                                           │ │  │ │                         ││
│  │ ┌──────────────────┐ ┌──────────────────┐ │ │ ┌──────────────────┐ ┌──────────────────┐ │ │  │ │  ┌──────────────────┐   ││
│  │ │    Server A1     │ │    Server A2     │ │ │ │    Server B1     │ │    Server B2     │ │ │  │ │  │    Server C1     │   ││
│  │ │                  │ │                  │ │ │ │                  │ │                  │ │ │  │ │  │ ┌──────────────┐ │   ││
│  │ │┌──────────────┐  │ │ ┌──────────────┐ │ │ │ │┌──────────────┐  │ │ ┌──────────────┐ │ │ │  │ │  │ │              │ │   ││
│  │ ││██████████████│  │ │ │              │ │ │ │ ││              │  │ │ │              │ │ │ │  │ │  │ │   Range R1   │ │   ││
│  │ ││███Range R1 ██│  │ │ │   Range R1   │ │ │ │ ││   Range R1   │  │ │ │   Range R1   │ │ │ │  │ │  │ │   Follower   │ │   ││
│  │ ││████Leader████│  │ │ │   Follower   │ │ │ │ ││   Follower   │  │ │ │   Follower   │ │ │ │  │ │  │ │              │ │   ││
│  │ ││██████████████│  │ │ │              │ │ │ │ ││              │  │ │ │              │ │ │ │  │ │  │ └──────────────┘ │   ││
│  │ │└──────────────┘  │ │ └──────────────┘ │ │ │ │└──────────────┘  │ │ └──────────────┘ │ │ │  │ │  │                  │   ││
│  │ │                  │ │                  │ │ │ │                  │ │                  │ │ │  │ │  │ ┌──────────────┐ │   ││
│  │ │ ┌──────────────┐ │ │ ┌──────────────┐ │ │ │ │ ┌──────────────┐ │ │ ┌──────────────┐ │ │ │  │ │  │ │              │ │   ││
│  │ │ │              │ │ │ │██████████████│ │ │ │ │ │              │ │ │ │              │ │ │ │  │ │  │ │   Range R2   │ │   ││
│  │ │ │   Range R2   │ │ │ │███Range R2 ██│ │ │ │ │ │   Range R2   │ │ │ │   Range R2   │ │ │ │  │ │  │ │   Follower   │ │   ││
│  │ │ │   Follower   │ │ │ │████Leader████│ │ │ │ │ │   Follower   │ │ │ │   Follower   │ │ │ │  │ │  │ │              │ │   ││
│  │ │ │              │ │ │ │██████████████│ │ │ │ │ │              │ │ │ │              │ │ │ │  │ │  │ └──────────────┘ │   ││
│  │ │ └──────────────┘ │ │ └──────────────┘ │ │ │ │ └──────────────┘ │ │ └──────────────┘ │ │ │  │ │  │                  │   ││
│  │ │                  │ │                  │ │ │ │                  │ │                  │ │ │  │ │  │                  │   ││
│  │ └──────────────────┘ └──────────────────┘ │ │ └──────────────────┘ └──────────────────┘ │ │  │ │  └──────────────────┘   ││
│  └───────────────────────────────────────────┘ └───────────────────────────────────────────┘ │  │ └─────────────────────────┘│
│                                                                                              │  │                            │
└──────────────────────────────────────────────────────────────────────────────────────────────┘  └────────────────────────────┘
```

**Cons**

* If it is single server providing timing, then Raft leaders for the shard will need to stay close to the timing. It is recommended to have multiple servers which could assign time. 
* Otherwise, exception will happen. For example
  1. C1 talks to timing server in server room A for getting the time. And absolute time \(AT\) is 500 and global time \(Ct\) is 500. 
  2. A1 node talks to timing server to get time. A1's request is later than C1, so the AT is 510 and Ct is also 510. 
  3. A1 wants to write data to R2. At is 512 and Ct is 510. 
  4. C1 wants to write data to R2. Since C2 is in another city and will have longer latency, C1 will be behind A1 to write data to R2. 
* As a result of the above steps, although C1's global time is before A1, its abosolute time is after A1. 

```text
┌─────────────────────────────────────────────────────┐           ┌────────────────────────────┐
│                       City A                        │           │           City B           │
│  ┌───────────────────────────────────────────┐      │           │ ┌─────────────────────────┐│
│  │               Server Room A               │      │           │ │      Server Room C      ││
│  │                                           │      │           │ │                         ││
│  │ ┌──────────────────┐ ┌──────────────────┐ │      │           │ │  ┌──────────────────┐   ││
│  │ │    Server A1     │ │    Server A2     │ │      │           │ │  │    Server C1     │   ││
│  │ │                  │ │                  │ │      │           │ │  │ ┌──────────────┐ │   ││
│  │ │┌──────────────┐  │ │                  │ │      │           │ │  │ │              │ │   ││
│  │ ││              │  │ │                  │ │      │    Step4  │ │  │ │ Compute node │ │   ││
│  │ ││   Range R1   │  │ │   Step3 At       │ │      │   At 550,─┼─┼──┼─│              │ │   ││
│  │ ││    Leader    │──┼─┼──512, Ct 510     │ │      │   Ct 500  │ │  │ │              │ │   ││
│  │ ││              │  │ │         │        │ │      │   │       │ │  │ └──────────────┘ │   ││
│  │ │└──────────────┘  │ │         │        │ │      │   │    ┌──┼─┼──┼───────────       │   ││
│  │ │        │         │ │         ▼        │ │      │   │    │  │ │  │                  │   ││
│  │ │        │         │ │ ┌──────────────┐ │ │      │   │    │  │ │  │                  │   ││
│  │ │        │         │ │ │              │ │ │      │   │    │  │ │  │                  │   ││
│  │ │        │         │ │ │   Range R2   │ │ │      │   │    │  │ │  │                  │   ││
│  │ │        │         │ │ │    Leader    │◀┼─┼──────┼───┘    │  │ │  │                  │   ││
│  │ │        │         │ │ │              │ │ │      │        │  │ │  │                  │   ││
│  │ │        │         │ │ └──────────────┘ │ │      │        │  │ │  └──────────────────┘   ││
│  │ │        │         │ │                  │ │      │        │  │ │                         ││
│  │ └────────┼─────────┘ └──────────────────┘ │      │        │  │ │                         ││
│  └──────────┼────────────────────────────────┘      │        │  │ └─────────────────────────┘│
│             │                                       │        │  │                            │
│           Step2                                     │        │  └────────────────────────────┘
│          At 510,                                    │        │                                
│          Ct 510                                     │      Step1                              
│             │  ┌─────────────────────────┐          │     At 500,                             
│             │  │                         │          │     Ct 500                              
│             │  │       Time server       │          │        │                                
│             └─▶│                         │◀─────────┼────────┘                                
│                │                         │          │                                         
│                └─────────────────────────┘          │                                         
│                                                     │                                         
└─────────────────────────────────────────────────────┘
```

### Parameters to monitor

* Availability
  * Connectability
  * Number of available connections
* Performance \(Using mySQL built-in variables to calculate\) 
  * QPS / TPS 
  * Deadlock
* Master-slave replication delay \(Using the diff of binlogs\)
* Disk space 

### Real world

#### Past utility: MMM \(Multi-master replication manager\)

* [MMM](https://mysql-mmm.org/downloads.html) is a set of scripts written in perl providing the following capabilities:
  * Load balancing among read slaves
  * Master failover
  * Monitor mySQL states
* Pros:
  * Easy config
* Cons:
  * Not suitable for scenarios having high requirements on data consistency
* Deployment: Although dual master, only allows writing to a single master at a time.
  * mmm\_mond: Coordinator scripts. Run on top of a monitoring machine
    * Create a set of virtual IPs. One write IP binds to the master and multiple read IPs bind to slave. 
    * When a mySQL is down, it will migrate the VIP to another mySQL machine. 
  * mmm\_agentd: Run on the same machine as the mysql server
  * mmm\_control: Provides administrative commands for mmm\_mond
* [Video tutorial in Mooc in Chinese](https://coding.imooc.com/lesson/49.html#mid=495)

#### Past utility MHA \(Master high availability\)

* [MHA](https://github.com/yoshinorim/mha4mysql-manager/wiki/Architecture)
  * Fast failover: Complete the failover within 0-30 seconds
  * Max effort consistency: When a master goes down, it will try to save binlog in the failed master. It uses this way to keep the maximum data consistency. However, this isn't reliable way. For example, some hardware failures may result in failure of saving binlogs. 
  * Compared with MMM, 
    * Supports devops work like health check, suspend nodes
    * Supports semi-synchronous, GTID 
* Deployment: 
  * MHA manager could be deployed in a separate machine for managing several master-slave clusters. It could also be deployed on a single slave. 
  * MHA node runs on each mysql server. 
* Cons:
  * Needs at minimum 3 machines
  * Brain split
  * Not suitable for scenarios having high requirements on data consistency
* [Video tutorial in Mooc in Chinese](https://coding.imooc.com/lesson/49.html#mid=499)

#### Wechat Red pocket

* [https://www.infoq.cn/article/2017hongbao-weixin](https://www.infoq.cn/article/2017hongbao-weixin)
* [http://www.52im.net/thread-2548-1-1.html](http://www.52im.net/thread-2548-1-1.html)

#### WePay MySQL high availability

* [Used at Wepay](https://wecode.wepay.com/posts/highly-available-mysql-clusters-at-wepay)

#### High availability at Github

* \[Used at Github\]\(

  [https://github.blog/2018-06-20-mysql-high-availability-at-github/](https://github.blog/2018-06-20-mysql-high-availability-at-github/)\)

![MySQL HA github](.gitbook/assets/mysql_ha_github.png)

* Master discovery series
* DNS [http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-1-dns](http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-1-dns)
* VPN and DNS

  [http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-2-vip-dns](http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-2-vip-dns)

* app and service discovery

  [http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-3-app-service-discovery](http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-3-app-service-discovery)

* Proxy heuristics

  [http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-4-proxy-heuristics](http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-4-proxy-heuristics)

* Service discovery and Proxy

  [http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-5-service-discovery-proxy](http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-5-service-discovery-proxy)

* [http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-6-other-methods](http://code.openark.org/blog/mysql/mysql-master-discovery-methods-part-6-other-methods)

#### Multi DC for disaster recovery

* 饿了吗：[https://zhuanlan.zhihu.com/p/32009822](https://zhuanlan.zhihu.com/p/32009822)
* 异地多活架构： [https://www.infoq.cn/video/PSpYkO6ygNb4tdmFGs0G](https://www.infoq.cn/video/PSpYkO6ygNb4tdmFGs0G)
* 微博异地多活：[https://mp.weixin.qq.com/s?\_\_biz=MzAwMDU1MTE1OQ==&mid=402920548&idx=1&sn=45cd62b84705fdd853bdd108b9301a17&3rd=MzA3MDU4NTYzMw==&scene=6\#rd](https://mp.weixin.qq.com/s?__biz=MzAwMDU1MTE1OQ==&mid=402920548&idx=1&sn=45cd62b84705fdd853bdd108b9301a17&3rd=MzA3MDU4NTYzMw==&scene=6#rd)
* Overview: [https://www.modb.pro/db/12798](https://www.modb.pro/db/12798)
* golden ant: 
  * [https://www.infoq.cn/article/xYEWLWBSc1L9H4XvzGl0](https://www.infoq.cn/article/xYEWLWBSc1L9H4XvzGl0)
  * [https://static001.geekbang.org/con/33/pdf/1703863438/file/%E7%BB%88%E7%A8%BF-%E6%97%B6%E6%99%96-%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB%E5%8D%95%E5%85%83%E5%8C%96%E6%9E%B6%E6%9E%84%E4%B8%8B%E7%9A%84%E5%BE%AE%E6%9C%8D%E5%8A%A1%E4%BD%93%E7%B3%BB.pdf](https://static001.geekbang.org/con/33/pdf/1703863438/file/%E7%BB%88%E7%A8%BF-%E6%97%B6%E6%99%96-%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB%E5%8D%95%E5%85%83%E5%8C%96%E6%9E%B6%E6%9E%84%E4%B8%8B%E7%9A%84%E5%BE%AE%E6%9C%8D%E5%8A%A1%E4%BD%93%E7%B3%BB.pdf)
* 甜橙： [https://mp.weixin.qq.com/s?\_\_biz=MzIzNjUxMzk2NQ==&mid=2247489336&idx=1&sn=0a078591dbacda3e892d21ac0525de67&chksm=e8d7e8fadfa061eca5ff5b0c8f0035f7eec9abc6a6e8336a07cc2ea95ed0e9de1a8e3f19e508&scene=27\#wechat\_redirect](https://mp.weixin.qq.com/s?__biz=MzIzNjUxMzk2NQ==&mid=2247489336&idx=1&sn=0a078591dbacda3e892d21ac0525de67&chksm=e8d7e8fadfa061eca5ff5b0c8f0035f7eec9abc6a6e8336a07cc2ea95ed0e9de1a8e3f19e508&scene=27#wechat_redirect)
* More: [https://www.infoq.cn/article/kihSqp\_twV16tiiPa1LO](https://www.infoq.cn/article/kihSqp_twV16tiiPa1LO)
* [https://s.geekbang.org/search/c=0/k=%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB/t=](https://s.geekbang.org/search/c=0/k=%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB/t=)
* 魅族：[http://www.ttlsa.com/linux/meizu-mutil-loaction-soul/](http://www.ttlsa.com/linux/meizu-mutil-loaction-soul/)
* 迁移角度：[https://melonshell.github.io/2020/01/24/tech3\_multi\_room\_living/](https://melonshell.github.io/2020/01/24/tech3_multi_room_living/)
* 李运华：[https://time.geekbang.org/column/article/12408](https://time.geekbang.org/column/article/12408)
* 唐杨：[https://time.geekbang.org/column/article/171115](https://time.geekbang.org/column/article/171115)
* 微服务多机房：[https://time.geekbang.org/column/article/64301](https://time.geekbang.org/column/article/64301)
* 缓存多机房：[https://time.geekbang.org/course/detail/100051101-253459](https://time.geekbang.org/course/detail/100051101-253459)
* Google Ads 异地多活的高可用架构：[https://zhuanlan.zhihu.com/p/103391944](https://zhuanlan.zhihu.com/p/103391944)
* TiDB: [https://docs.pingcap.com/zh/tidb/dev/multi-data-centers-in-one-city-deployment](https://docs.pingcap.com/zh/tidb/dev/multi-data-centers-in-one-city-deployment)
* 支付宝架构：[https://www.hi-linux.com/posts/39305.html\#1-%E8%83%8C%E6%99%AF](https://www.hi-linux.com/posts/39305.html#1-%E8%83%8C%E6%99%AF)
* 三地五中心：[https://www.jianshu.com/p/aff048130bed](https://www.jianshu.com/p/aff048130bed)

