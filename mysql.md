
<!-- MarkdownTOC -->

- [MySQL](#mysql)
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
- [Future readings](#future-readings)

<!-- /MarkdownTOC -->


# MySQL

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

![MySQL MMM normal flow](./images/mysql_ha_mmm_normalflow.png)

![MySQL MMM failover flow](./images/mysql_ha_mmm_failoverflow.png)

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

# Future readings
* MySQL DDL as big transaction