- [High concurrent writes conflicts](#high-concurrent-writes-conflicts)
  - [V1: Serializable DB isolation](#v1-serializable-db-isolation)
  - [V2: Optimistic lock](#v2-optimistic-lock)
  - [V3: Put inventory number inside Redis](#v3-put-inventory-number-inside-redis)
- [High concurrent read but low concurrent writes - Read/Write separation](#high-concurrent-read-but-low-concurrent-writes---readwrite-separation)
- [Architecture example - Replication + PXC + Sharding proxy](#architecture-example---replication--pxc--sharding-proxy)
- [Architecture example - Disaster recovery](#architecture-example---disaster-recovery)
  - [One active and the other cold backup machine](#one-active-and-the-other-cold-backup-machine)
  - [Two active DCs with full copy of data](#two-active-dcs-with-full-copy-of-data)
    - [Same city vs different city](#same-city-vs-different-city)
    - [Two active DCs within a city](#two-active-dcs-within-a-city)
    - [Two active DCs in different cities](#two-active-dcs-in-different-cities)
  - [Multi active DCs with sharded data](#multi-active-dcs-with-sharded-data)
    - [Synchronization mechanisms](#synchronization-mechanisms)
    - [Message queue based](#message-queue-based)
    - [RPC based](#rpc-based)
  - [Distributed database (Two cities / three DCs and five copies)](#distributed-database-two-cities--three-dcs-and-five-copies)
    - [Pros](#pros)
    - [Cons](#cons)
- [Parameters to monitor](#parameters-to-monitor)
- [Real world](#real-world)
  - [Past utility: MMM (Multi-master replication manager)](#past-utility-mmm-multi-master-replication-manager)
  - [Past utility MHA (Master high availability)](#past-utility-mha-master-high-availability)
  - [Wechat Red pocket](#wechat-red-pocket)
  - [WePay MySQL high availability](#wepay-mysql-high-availability)
  - [High availability at Github](#high-availability-at-github)
  - [Multi DC for disaster recovery](#multi-dc-for-disaster-recovery)


# High concurrent writes conflicts

* Problem: How to prevent overselling for limited inventory products?

## V1: Serializable DB isolation

* Solution1: Set serializable isolation level in DB

## V2: Optimistic lock

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

## V3: Put inventory number inside Redis

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

# High concurrent read but low concurrent writes - Read/Write separation


# Architecture example - Replication + PXC + Sharding proxy

* PXC is a type of strong consistency MySQL cluster built on top of Galera. It could store data requring high consistency. 
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

# Architecture example - Disaster recovery

## One active and the other cold backup machine

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

```
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

## Two active DCs with full copy of data

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

### Same city vs different city

* The following table summarizes the differences of these two pattern

| Dimensions      | two data centers within the same city                                                                     | two data centers in different cities                                                                                                                                                    |
| --------------- | --------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Definition      | Two DCs are located close to each other geographically. For example, the two DCs are within the same city | Two DCs are located distant from each other geographically. For example, the two DCs are cross region (e.g. New York and Log Angeles), or even cross continent (e.g. USA and Australia) |
| Cost            | high (DC itself and dedicated line with same city)                                                        | extremely high (DC itself and dedicated line across same region/continent)                                                                                                              |
| Complexity      | Low. Fine to call across DCs due to low latency                                                           | High. Need to rearchitect due to high latency                                                                                                                                           |
| Service quality | Increase latency a bit / increase availability                                                            | Decrease latency  / increase availability                                                                                                                                               |

### Two active DCs within a city

* Since the latency within the same city will be low, it is fine to have one centralized database layer and have cross DC remote calls. 

```
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

### Two active DCs in different cities

* Since the latency between two DCs across region/continent will be high, it is only possible to sync the data asynchronously. 

```
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

## Multi active DCs with sharded data

* Definition:
  * Request routing:
    * API Router Layer: Route external API calls to the correct DC.
    * Internal DC call Router: Within a sharded DC, route cross DC calls. 
    * Global Coordinator Service: Maintains the mapping from shard key -> shard id -> DC
      * Shard key varies with each request.
      * Shard Id -> DC mapping does not change much.
  * Data:
    * Sharded DC: Contains eventual consistent sharded data. For example, in case of ecommerce system, each buyer has their own orders, comments, user behaviors. 
    * Global zone DC: Contains strong consistent global data. For example, in case of ecommerce system, all users will see the same inventory.
* Typical flow: 
  * Step1. A request comes to API router layer with sharding keys (geographical location, user Id, order Id)
  * Step2. The API router layer component will determine the DC which contains the shard
  * Step3. 
  * Step4. (Optional) It will call "Inter DC Call Router" in case it needs to use data in another sharded DC (e.g. Suppose the sharded DC is based on geographical location, a buyer on an ecommerce website wants to look at a seller's product who is in another city.)
  * Step5. (Optional) It will call "Global zone" in case it needs to access the global strong consistent data (e.g. )

```
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

### Synchronization mechanisms

* Reship component: Forward the write requests coming in local DC to remote DCs.
* Collector component: Read write requests from remote DCs and write to local DC. 
* Elastic search component: Update to DC requests are all written to elastic search to guarantee strong consistency. 

### Message queue based

```
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

### RPC based

```
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

## Distributed database (Two cities / three DCs and five copies)

* For distributed ACID database, the basic unit is sharding. And the data consensus is achieved by raft protocol. 

### Pros

* Disaster recovery support:
  * If any server room within city A is not available, then city B server room's vote could still form majority with the remaining server room in city A. 

```
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

### Cons

* If it is single server providing timing, then Raft leaders for the shard will need to stay close to the timing. It is recommended to have multiple servers which could assign time. 
* Otherwise, exception will happen. For example
  1. C1 talks to timing server in server room A for getting the time. And absolute time (AT) is 500 and global time (Ct) is 500. 
  2. A1 node talks to timing server to get time. A1's request is later than C1, so the AT is 510 and Ct is also 510. 
  3. A1 wants to write data to R2. At is 512 and Ct is 510. 
  4. C1 wants to write data to R2. Since C2 is in another city and will have longer latency, C1 will be behind A1 to write data to R2. 
* As a result of the above steps, although C1's global time is before A1, its abosolute time is after A1. 

```
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

# Parameters to monitor

* Availability
  * Connectability
  * Number of available connections
* Performance (Using mySQL built-in variables to calculate) 
  * QPS / TPS 
  * Deadlock
* Master-slave replication delay (Using the diff of binlogs)
* Disk space 

# Real world

## Past utility: MMM (Multi-master replication manager)

* [MMM](https://mysql-mmm.org/downloads.html) is a set of scripts written in perl providing the following capabilities:
  * Load balancing among read slaves
  * Master failover
  * Monitor mySQL states
* Pros:
  * Easy config
* Cons:
  * Not suitable for scenarios having high requirements on data consistency
* Deployment: Although dual master, only allows writing to a single master at a time.
  * mmm_mond: Coordinator scripts. Run on top of a monitoring machine
    * Create a set of virtual IPs. One write IP binds to the master and multiple read IPs bind to slave. 
    * When a mySQL is down, it will migrate the VIP to another mySQL machine. 
  * mmm_agentd: Run on the same machine as the mysql server
  * mmm_control: Provides administrative commands for mmm_mond
* [Video tutorial in Mooc in Chinese](https://coding.imooc.com/lesson/49.html#mid=495)

## Past utility MHA (Master high availability)

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

## Wechat Red pocket

* [https://www.infoq.cn/article/2017hongbao-weixin](https://www.infoq.cn/article/2017hongbao-weixin)
* [http://www.52im.net/thread-2548-1-1.html](http://www.52im.net/thread-2548-1-1.html)

## WePay MySQL high availability

* [Used at Wepay](https://wecode.wepay.com/posts/highly-available-mysql-clusters-at-wepay)

## High availability at Github

* \[Used at Github]\(

    [https://github.blog/2018-06-20-mysql-high-availability-at-github/](https://github.blog/2018-06-20-mysql-high-availability-at-github/))

![MySQL HA github](images/mysql_ha_github.png)

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

## Multi DC for disaster recovery

* 饿了吗：[https://zhuanlan.zhihu.com/p/32009822](https://zhuanlan.zhihu.com/p/32009822)
* 异地多活架构： [https://www.infoq.cn/video/PSpYkO6ygNb4tdmFGs0G](https://www.infoq.cn/video/PSpYkO6ygNb4tdmFGs0G)
* 微博异地多活：[https://mp.weixin.qq.com/s?\__biz=MzAwMDU1MTE1OQ==\&mid=402920548\&idx=1\&sn=45cd62b84705fdd853bdd108b9301a17&3rd=MzA3MDU4NTYzMw==\&scene=6#rd](https://mp.weixin.qq.com/s?\__biz=MzAwMDU1MTE1OQ==\&mid=402920548\&idx=1\&sn=45cd62b84705fdd853bdd108b9301a17&3rd=MzA3MDU4NTYzMw==\&scene=6#rd)
* Overview: [https://www.modb.pro/db/12798](https://www.modb.pro/db/12798)
* golden ant: 
  * [https://www.infoq.cn/article/xYEWLWBSc1L9H4XvzGl0](https://www.infoq.cn/article/xYEWLWBSc1L9H4XvzGl0)
  * [https://static001.geekbang.org/con/33/pdf/1703863438/file/%E7%BB%88%E7%A8%BF-%E6%97%B6%E6%99%96-%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB%E5%8D%95%E5%85%83%E5%8C%96%E6%9E%B6%E6%9E%84%E4%B8%8B%E7%9A%84%E5%BE%AE%E6%9C%8D%E5%8A%A1%E4%BD%93%E7%B3%BB.pdf](https://static001.geekbang.org/con/33/pdf/1703863438/file/%E7%BB%88%E7%A8%BF-%E6%97%B6%E6%99%96-%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB%E5%8D%95%E5%85%83%E5%8C%96%E6%9E%B6%E6%9E%84%E4%B8%8B%E7%9A%84%E5%BE%AE%E6%9C%8D%E5%8A%A1%E4%BD%93%E7%B3%BB.pdf)
* 甜橙： [https://mp.weixin.qq.com/s?\__biz=MzIzNjUxMzk2NQ==\&mid=2247489336\&idx=1\&sn=0a078591dbacda3e892d21ac0525de67\&chksm=e8d7e8fadfa061eca5ff5b0c8f0035f7eec9abc6a6e8336a07cc2ea95ed0e9de1a8e3f19e508\&scene=27#wechat_redirect](https://mp.weixin.qq.com/s?\__biz=MzIzNjUxMzk2NQ==\&mid=2247489336\&idx=1\&sn=0a078591dbacda3e892d21ac0525de67\&chksm=e8d7e8fadfa061eca5ff5b0c8f0035f7eec9abc6a6e8336a07cc2ea95ed0e9de1a8e3f19e508\&scene=27#wechat_redirect)
* More: [https://www.infoq.cn/article/kihSqp_twV16tiiPa1LO](https://www.infoq.cn/article/kihSqp_twV16tiiPa1LO)
* [https://s.geekbang.org/search/c=0/k=%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB/t=](https://s.geekbang.org/search/c=0/k=%E5%BC%82%E5%9C%B0%E5%A4%9A%E6%B4%BB/t=)
* 魅族：[http://www.ttlsa.com/linux/meizu-mutil-loaction-soul/](http://www.ttlsa.com/linux/meizu-mutil-loaction-soul/)
* 迁移角度：[https://melonshell.github.io/2020/01/24/tech3\_multi_room_living/](https://melonshell.github.io/2020/01/24/tech3\_multi_room_living/)
* 李运华：[https://time.geekbang.org/column/article/12408](https://time.geekbang.org/column/article/12408)
* 唐杨：[https://time.geekbang.org/column/article/171115](https://time.geekbang.org/column/article/171115)
* 微服务多机房：[https://time.geekbang.org/column/article/64301](https://time.geekbang.org/column/article/64301)
* 缓存多机房：[https://time.geekbang.org/course/detail/100051101-253459](https://time.geekbang.org/course/detail/100051101-253459)
* Google Ads 异地多活的高可用架构：[https://zhuanlan.zhihu.com/p/103391944](https://zhuanlan.zhihu.com/p/103391944)
* TiDB: [https://docs.pingcap.com/zh/tidb/dev/multi-data-centers-in-one-city-deployment](https://docs.pingcap.com/zh/tidb/dev/multi-data-centers-in-one-city-deployment)
* 支付宝架构：[https://www.hi-linux.com/posts/39305.html#1-%E8%83%8C%E6%99%AF](https://www.hi-linux.com/posts/39305.html#1-%E8%83%8C%E6%99%AF)
* 三地五中心：[https://www.jianshu.com/p/aff048130bed](https://www.jianshu.com/p/aff048130bed)
