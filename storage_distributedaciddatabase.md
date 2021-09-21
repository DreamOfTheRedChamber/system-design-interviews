# Storage\_DistributedAcidDatabase

* [Relational distributed database](storage_distributedaciddatabase.md#relational-distributed-database)
  * [OLTP](storage_distributedaciddatabase.md#oltp)
  * [ACID - Consistency](storage_distributedaciddatabase.md#acid---consistency)
  * [ACID - Durability](storage_distributedaciddatabase.md#acid---durability)
  * [ACID - Atomicity](storage_distributedaciddatabase.md#acid---atomicity)
    * [Def](storage_distributedaciddatabase.md#def)
    * [2PC and TCC](storage_distributedaciddatabase.md#2pc-and-tcc)
  * [ACID - Isolation](storage_distributedaciddatabase.md#acid---isolation)
    * [ANSI SQL-92](storage_distributedaciddatabase.md#ansi-sql-92)
    * [Critique](storage_distributedaciddatabase.md#critique)
      * [Def](storage_distributedaciddatabase.md#def-1)
      * [Snapshot isolation](storage_distributedaciddatabase.md#snapshot-isolation)
        * [Phantom read vs unrepeatable read](storage_distributedaciddatabase.md#phantom-read-vs-unrepeatable-read)
        * [Write skew problem](storage_distributedaciddatabase.md#write-skew-problem)
        * [Why snapshot isolation is left from SQL-92](storage_distributedaciddatabase.md#why-snapshot-isolation-is-left-from-sql-92)
    * [Effort to support linearizable](storage_distributedaciddatabase.md#effort-to-support-linearizable)
  * [Components](storage_distributedaciddatabase.md#components)
    * [Traditional DB architecture](storage_distributedaciddatabase.md#traditional-db-architecture)
    * [PGXC](storage_distributedaciddatabase.md#pgxc)
      * [Proxy layer only](storage_distributedaciddatabase.md#proxy-layer-only)
      * [Proxy layer + transaction management](storage_distributedaciddatabase.md#proxy-layer--transaction-management)
      * [Proxy layer + Global clock](storage_distributedaciddatabase.md#proxy-layer--global-clock)
    * [NewSQL](storage_distributedaciddatabase.md#newsql)
  * [Sharding](storage_distributedaciddatabase.md#sharding)
    * [Benefits of range based dynamic sharding](storage_distributedaciddatabase.md#benefits-of-range-based-dynamic-sharding)
    * [Differentiators between NewSQL and PGXC sharding](storage_distributedaciddatabase.md#differentiators-between-newsql-and-pgxc-sharding)
    * [Sharding metadata](storage_distributedaciddatabase.md#sharding-metadata)
      * [Static sharding with hash based arrangement](storage_distributedaciddatabase.md#static-sharding-with-hash-based-arrangement)
      * [TiDB: Dedicated cluster for metadata with Paxos replication](storage_distributedaciddatabase.md#tidb-dedicated-cluster-for-metadata-with-paxos-replication)
      * [CoackroachDB: Gossip protocol](storage_distributedaciddatabase.md#coackroachdb-gossip-protocol)
  * [References](storage_distributedaciddatabase.md#references)
  * [TODO](storage_distributedaciddatabase.md#todo)

## Relational distributed database

### OLTP

* OLTP scenarios characteristics:
  * Write intensive. Read operation has low complexity and usually does not involve large amount of data. 
  * Low latency. Usually within 500ms. 
  * High concurrency. 

### ACID - Consistency

* Reference: [https://jepsen.io/consistency](https://jepsen.io/consistency)

![](.gitbook/assets/relational_distributedDb_consistencyModel.png)

|  | `Linearizability` | `Casual consistency` |
| :--- | :--- | :--- |
| `Serializable(SSI)` | Spanner | CockroadDB |
| `Snap Isolation(SI)` | TiDB | YugabyteDB |
| `Repeatable Read(RR)` | GoldenDB |  |
| `Read Committed(RC)` | OceanBase 2.0+ |  |

### ACID - Durability

* Category 1: Hardware is not damaged and could recover. 
  * Rely on write ahead log to recover
* Category 2: Hardware is damaged and could not recover. 
  * Rely on synchronous or semi-synchronous replication typically shipped together with database.
  * Rely on shared storage such as Amazon Aurora.
  * Rely on consensus protocol such as Paxos/Raft. 

### ACID - Atomicity

#### Def

* Either all the changes from the transaction occur \(writes, and messages sent\), or none occur. 
* It requires transaction to be in two states.

#### 2PC and TCC

* When compared with 2PC, TCC does not always need lock so could be a bit more performant

### ACID - Isolation

* There are multiple isolation levels

#### ANSI SQL-92

* The earliest definition on ANSI SQL-92: Defines four isolation levels and three types of unexpected behaviors. 

#### Critique

**Def**

* Based on top of ANSI SQL-92, Critique defines six isolation levels and eight types of unexpected behaviors. 

![](.gitbook/assets/relational_distributedDb_Critique.png)

![](.gitbook/assets/relational_distributedDb_Critique2.png)

**Snapshot isolation**

* Snapshot isolation makes the most differences because
  * Within SQL-92, the biggest differences between Repeatable Read \(RR\) and Serializable is how to handle phantom read. 
  * Critique points out that even Snapshot isolation could solve phantom read, it is still not serializable because it could not handle write skew problem. 

**Phantom read vs unrepeatable read**

* Similaririties: Within a transaction, query with the same condition twice but the results of the two times are different. 
* Differences: 
  * Unrepeatable read: Some results for the second time are updated or deleted. 
  * Phantom read: Some results for the second time are inserted. Within MySQL, the lock to prevent phantom read is called Gap Lock. 

**Write skew problem**

* Process: 
  * Both Alice and Bob select the Post and the PostDetails entities.
  * Bob modifies the Post title, but, since the PostDetails is already marked as updated by Bob, the dirty checking mechanism will skip updating the PostDetails entity, therefore preventing a redundant UPDATE statement.
  * Alice wants to update the Post entity, but the entity already has the same value as the one she wants to apply so only the PostDetails record will mark that the latest change is the one proposed by Alice.

![](.gitbook/assets/relational_distributedDb_critique_writeSkew.png)

* References: [https://vladmihalcea.com/a-beginners-guide-to-read-and-write-skew-phenomena/](https://vladmihalcea.com/a-beginners-guide-to-read-and-write-skew-phenomena/)

**Why snapshot isolation is left from SQL-92**

* SQL-92 is built on top of lock-based concurrency control, and snapshot isolation is built on top of MVCC. 

#### Effort to support linearizable

* Redis/VoltDB: Use single thread to implement serialization.
* CockroachDB: 

### Components

#### Traditional DB architecture

* Client communications manager: Developers use JDBC or ODBC to conenct to database. 
* Process manager: After the connection is established, database system will allocate a process for handling all follow-up operations. More precisely,
  * Oracle and PostgreSQL uses a process. 
  * MySQL uses a thread. 
* Relational query processor: 
  * Query parsing and authorization
  * Query rewrite
  * Query optimizer
  * Plan executor
* Transactional storage manager: 
  * Access methods
  * Lock manager
  * Log manager
  * Buffer manager
* Shared components and utilities: 

![](.gitbook/assets/relational_distributedDb_Rdbms.png)

#### PGXC

* Products: GoldenDB / TBase / GaussDB 300 / AntDB

**Proxy layer only**

```text
┌────────────────────────────────────────────────────────────────────┐     ┌─────────────────┐
│                             Proxy Node                             │     │                 │
│                                                                    │     │                 │
│ ┌────────────┐     ┌────────────┐    ┌───────────────┐             │     │                 │
│ │  Process   │     │   Query    │    │    Client     │             │     │                 │
│ │ management │     │ processor  │    │communications │             │     │                 │
│ │            │     │            │    │    manager    │             │     │                 │
│ └────────────┘     └────────────┘    └───────────────┘             │     │                 │
└────────────────────────────────────────────────────────────────────┘     │                 │
                                                                           │                 │
                                                                           │    Sharding     │
                                                                           │   information   │
                                                                           │                 │
┌─────────────────────────────────────────────────────────────────────┐    │                 │
│                              Data Node                              │    │                 │
│ ┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐│    │                 │
│ │  Process   │    │   Query    │    │Transaction │    │   Shared   ││    │                 │
│ │ management │    │ processor  │    │  storage   │    │ components ││    │                 │
│ │            │    │            │    │  manager   │    │            ││    │                 │
│ └────────────┘    └────────────┘    └────────────┘    └────────────┘│    │                 │
│                                                                     │    │                 │
└─────────────────────────────────────────────────────────────────────┘    └─────────────────┘
```

**Proxy layer + transaction management**

* Sample implementation: MyCat

```text
┌────────────────────────────────────────────────────────────────────┐     ┌─────────────────┐
│                             Proxy Node                             │     │                 │
│                                                   ┏━━━━━━━━━━━━━━┓ │     │                 │
│ ┌────────────┐  ┌────────────┐  ┌───────────────┐ ┃██████████████┃ │     │                 │
│ │  Process   │  │   Query    │  │    Client     │ ┃█Distributed █┃ │     │                 │
│ │ management │  │ processor  │  │communications │ ┃█transaction █┃ │     │                 │
│ │            │  │            │  │    manager    │ ┃███manager████┃ │     │                 │
│ └────────────┘  └────────────┘  └───────────────┘ ┃██████████████┃ │     │                 │
└───────────────────────────────────────────────────┻━━━━━━━━━━━━━━┻─┘     │                 │
                                                                           │                 │
                                                                           │    Sharding     │
                                                                           │   information   │
                                                                           │                 │
┌─────────────────────────────────────────────────────────────────────┐    │                 │
│                              Data Node                              │    │                 │
│ ┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐│    │                 │
│ │  Process   │    │   Query    │    │Transaction │    │   Shared   ││    │                 │
│ │ management │    │ processor  │    │  storage   │    │ components ││    │                 │
│ │            │    │            │    │  manager   │    │            ││    │                 │
│ └────────────┘    └────────────┘    └────────────┘    └────────────┘│    │                 │
│                                                                     │    │                 │
└─────────────────────────────────────────────────────────────────────┘    └─────────────────┘
```

**Proxy layer + Global clock**

```text
┌────────────────────────────────────────────────────────────────────┐     ┌─────────────────┐ 
│                             Proxy Node                             │     │                 │ 
│                                                                    │     │                 │ 
│ ┌────────────┐  ┌────────────┐  ┌───────────────┐ ┌──────────────┐ │     │    Sharding     │ 
│ │  Process   │  │   Query    │  │    Client     │ │ Distributed  │ │     │   information   │ 
│ │ management │  │ processor  │  │communications │ │ transaction  │ │     │                 │ 
│ │            │  │            │  │    manager    │ │   manager    │ │     │                 │ 
│ └────────────┘  └────────────┘  └───────────────┘ └──────────────┘ │     │                 │ 
└────────────────────────────────────────────────────────────────────┘     └─────────────────┘ 




┌─────────────────────────────────────────────────────────────────────┐     ┌─────────────────┐
│                              Data Node                              │     │█████████████████│
│ ┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐│     │█████████████████│
│ │  Process   │    │   Query    │    │Transaction │    │   Shared   ││     │█████████████████│
│ │ management │    │ processor  │    │  storage   │    │ components ││     │██Global clock███│
│ │            │    │            │    │  manager   │    │            ││     │█████████████████│
│ └────────────┘    └────────────┘    └────────────┘    └────────────┘│     │█████████████████│
│                                                                     │     │█████████████████│
└─────────────────────────────────────────────────────────────────────┘     └─────────────────┘
```

#### NewSQL

* Products: Spanner / CockroachDB / TiDB / YugabyteDB / OceanBase
* Differences from PGXC:
  * Based on K/V store BigTable instead of  traditional relational DB
  * Based on Paxos/Raft protocol instead of master slave replication
  * Based on LSM-Tree instead of B+ Tree

```text
┌────────────────────────────────────────────────────────────────────┐     ┌─────────────────┐ 
│                             Proxy Node                             │     │█████████████████│ 
│                                                                    │     │█████████████████│ 
│ ┌────────────┐  ┌────────────┐  ┌───────────────┐ ┌──────────────┐ │     │████Metadata ████│ 
│ │  Process   │  │   Query    │  │    Client     │ │ Distributed  │ │     │███management████│ 
│ │ management │  │ processor  │  │communications │ │ transaction  │ │     │█████████████████│ 
│ │            │  │            │  │    manager    │ │   manager    │ │     │█████████████████│ 
│ └────────────┘  └────────────┘  └───────────────┘ └──────────────┘ │     │█████████████████│ 
└────────────────────────────────────────────────────────────────────┘     └─────────────────┘ 




┌─────────────────────────────────────────────────────────────────────┐     ┌─────────────────┐
│██████████████████████████Key value system███████████████████████████│     │                 │
│█┌────────────┐████┌────────────┐████┌────────────┐████┌────────────┐│     │                 │
│█│  Process   │████│  Storage   │████│Computation │████│   Shared   ││     │                 │
│█│ management │████│ management │████│  pushdown  │████│ components ││     │  Global clock   │
│█│            │████│            │████│            │████│            ││     │                 │
│█└────────────┘████└────────────┘████└────────────┘████└────────────┘│     │                 │
│█████████████████████████████████████████████████████████████████████│     │                 │
└─────────────────────────────────────────────────────────────────────┘     └─────────────────┘
```

### Sharding

|  | `Static` | `Dynamic` |
| :--- | :--- | :--- |
| `Hash` | PGXC / NewSQL | NA |
| `Range` | PGXC | NewSQL |

* There are two sharding approaches: Hash based vs range base. 
* Range based sharding is typically used more widely when compared with hash based because:
  * Range based sharding are more suitable for search through the data. 
  * It could include the business related logic. For example, if a service has more users in Beijing and Shanghai but much less users overseas, then both of Beijing and Shanghai could have their separate shard. And overseas users could have a separate shard. 

#### Benefits of range based dynamic sharding

* Shard could automatically split and merge according to the data volume or traffic volume. 
* Reduce distributed transactions. 
  * For example, Spanner adds the concept of directory under tablet. And the scheduling of directory could span across tablet. 
  * By directory scheling, it could move directories participating a transaction under the same tablet. 
* Reduce service latency. 
  * Spanner could move a directory to a position closer to users. 

#### Differentiators between NewSQL and PGXC sharding

* Sharding is the smallest reliable unit for NewSQL; For PGXC, sharding's reliability relies on the node it resides in. 
* NewSQL relies on the concept of replication group. Within the replication group, multiple copies work with each other via Raft or Paxos protocol. 
* PGXC relies on the concepts of set. Within the set, multiple copies work with each other via semi-synchronous replication. 

#### Sharding metadata

**Static sharding with hash based arrangement**

* If static sharding strategy is used, then metadata never needs to change and only replication is needed. And the metadata could use hash based sharding.  
* Cons:
  * If the metadata needs to be updated, then this is inappropriate because too many nodes need to update. 

**TiDB: Dedicated cluster for metadata with Paxos replication**

* Roles:
  * TiKV node: Store sharding data 
  * Placement driver: Store sharding metadata
* Interaction:
  * TiKV reports heartbeat message to PD on a regular basis. And metadata for sharding is also reported. 
  * PD will rely with sharding scheduling instructions. Then the next time TiKVs report, PD will know how the scheduling is going. 
* Benefits of the design:
  * TiKV always reports the full sharding info so that PD could be stateless. And the new PD master does not need to sync status with the old PD master. 

![](.gitbook/assets/relational_distributedDb_TiKV.png)

**CoackroachDB: Gossip protocol**

* Gossip vs Paxos
  * Paxos is a broadcasting protocol in essence. When there is a large number of nodes, the protocol will become much less efficient. 
  * Each spreader inside Gossip protocol only multicast the message in a small range. 
* Process:
  1. Node A receives the SQL request from client and want to query for T1. According to primary key range, T1 exists on shard R1, and local metadata shows that R1 exists on node B. 
  2. Node A sends requests to node B. Unfortunately, node A's metadata has already expired and R1 has already been reallocated to node C. 
  3. Node B relies an important information to node A. Shard R1 exists on node C. 
  4. Node A sends requests to Node C. Luckily, shard R1 do exists in Node C. 
  5. Node A receives node C's returned shard R1. 
  6. Node A returns records on R1 and update local metadata. 

```text
                            ┌────────────────────┐                              
        Step1.              │                    │                              
       Send SQL ────────────│       Client       │                              
       request              │                    │                              
           │                └────────────────────┘                              
           │                           ▲                                        
           │                           │                                        
           │      Step6. Return shard  │                                        
           │    ┌──R1 corresponding ───┘                                        
           │    │       record                                                  
           │    │                                                               
           │    │                                         ┌────────────────────┐
           ▼    │                                         │       Node C       │
┌────────────────────┐                                    │      ┌──────┐      │
│       Node A       │                                    │      │ Data │      │
│                    │                                    │      │  R1  │      │
│   ┌────────────┐   │                                    │      └──────┘      │
│   │  Metadata  │   │◀──────Step5. Return shard R1───────│                    │
│   │R1 -> Node B│   │                                    │ ┌────────────────┐ │
│   │            │   │───────Step4. Look for shard───────▶│ │    Metadata    │ │
│   └────────────┘   │          R1 from Node C            │ │  R1 -> Node C  │ │
└────────────────────┘                                    │ └────────────────┘ │
           │    ▲                                         │                    │
           │    │                                         └────────────────────┘
           │    │      Step3. Return                                            
           │    └───────sharding R1 ──────┐                                     
           │             position         │                                     
           │                              │                                     
      Step2. Look                         │                                     
     for shard R1                         │                                     
      from Node B              ┌────────────────────┐                           
           │                   │       Node B       │                           
           │                   │                    │                           
           │                   │   ┌────────────┐   │                           
           └──────────────────▶│   │  Metadata  │   │                           
                               │   │R1 -> Node C│   │                           
                               │   │            │   │                           
                               │   └────────────┘   │                           
                               └────────────────────┘
```

### References

* [极客时间-分布式数据库](https://time.geekbang.org/column/article/271373)

### TODO

* Distributed clock
  1. [https://medium.com/geekculture/all-things-clock-time-and-order-in-distributed-systems-physical-time-in-depth-3c0a4389a838](https://medium.com/geekculture/all-things-clock-time-and-order-in-distributed-systems-physical-time-in-depth-3c0a4389a838) 

