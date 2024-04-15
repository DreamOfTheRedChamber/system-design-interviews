- [High concurrent writes and large volume across tables: MySQL DB Sharding](#high-concurrent-writes-and-large-volume-across-tables-mysql-db-sharding)
  - [Use cases](#use-cases)
    - [Last resort](#last-resort)
    - [Triggers](#triggers)
    - [Capacity planning](#capacity-planning)
  - [Introduced problems](#introduced-problems)
    - [How to choose sharding key](#how-to-choose-sharding-key)
    - [Choose the number of shards](#choose-the-number-of-shards)
    - [Limited SQL queries](#limited-sql-queries)
    - [Sharding stratgies](#sharding-stratgies)
      - [Lookup strategy](#lookup-strategy)
      - [Range strategy](#range-strategy)
      - [By customer or tenant](#by-customer-or-tenant)
      - [By geography](#by-geography)
      - [By time](#by-time)
      - [Hash strategy](#hash-strategy)
        - [By entity id](#by-entity-id)
    - [How to store unsharded table](#how-to-store-unsharded-table)
    - [How to deploy shards on nodes](#how-to-deploy-shards-on-nodes)
    - [Cross shard join](#cross-shard-join)
    - [Distributed transactions (write across shards)](#distributed-transactions-write-across-shards)
    - [Unique global ID](#unique-global-id)
    - [Challenges in Graph DB sharding](#challenges-in-graph-db-sharding)
  - [ShardingSphere](#shardingsphere)
  - [Sharding JDBC](#sharding-jdbc)
  - [Sharding Proxy\*\*](#sharding-proxy)
    - [Sharding example (In Chinese)](#sharding-example-in-chinese)


# High concurrent writes and large volume across tables: MySQL DB Sharding

## Use cases

### Last resort

* Sharding should be used as a last resort after you exhausted the following:
  * Add cache
  * Add read-write separation
  * Consider table partition

### Triggers

* Only use in OLTP cases (OLAP is more likely to have complex changing SQL queries)
* A single table's capacity reaches 2GB. 
* A database should not contain more than 1,000 tables.
* Each individual table should not exceed 1 GB in size or 20 million rows;
* The total size of all the tables in a database should not exceed 2 GB.

### Capacity planning

* For fast growing data (e.g. order data in ecommerce website), use 2X planned capacity to avoid resharding
* For slow growing data (e.g. user identity data in ecommerce website), use 3-year estimated capacity to avoid resharding. 

## Introduced problems

### How to choose sharding key

* Avoid cross shard joins
* Make data distribution even across shards

### Choose the number of shards

* If has a cap on storage:
  * Each shard could contain at most 1TB data.
  * number of shards = total storage / 1TB
* If has a cap on number of records:
  * Suppose the size of row is 100 bytes
    * User table: uid (long 8 bytes), name (fixed char 16 bytes), city (int 4 bytes), timestamp (long 8 bytes), sex (int 4 bytes), age (int 4 bytes) = total 40 bytes
  * Total size of the rows: 100 bytes \* Number_of_records
  * number of shards = total size of rows / 1TB

### Limited SQL queries

* Not all single node SQL will be supported. 
* See this for [a detailed example of ShardingSphere](https://shardingsphere.apache.org/document/current/en/features/sharding/use-norms/sql/)

### Sharding stratgies

#### Lookup strategy

* Pros:
  * Easy to migrate data
* Cons: 
  * Need an additional hop when query
  * If the lookup table is really big, it could also need to be sharded

![lookup](../.gitbook/assets/mysql_sharding_lookupstrategy.png)

#### Range strategy

* Pros:
  * Easy to add a new shard. No need to move the original data. For example, each month could have a new shard.
* Cons:
  * Uneven distribution. For example, July is the hot season but December is the cold season. 

![range](../.gitbook/assets/mysql_sharding_rangestrategy.png)

#### By customer or tenant

* If it is a SaaS business, it is often true that data from one customer doesn't interact with data from any of your other customers. These apps are usually called multi-tenant apps. 
  * Multi-tenant apps usually require strong consistency where transaction is in place and data loss is not possible. 
  * Multi-tenant data usually evolves over time to provide more and more functionality. Unlike consumer apps which benefit from network effects to grow, B2B applications grows by adding new features for customers. 

#### By geography

* Apps such as postmate, lyft or instacart.
* You’re not going to live in Alabama and order grocery delivery from California. And if you were to order a Lyft pick-up from California to Alabama you’ll be waiting a good little while for your pickup.

#### By time

* Time sharding is incredibly common when looking at some form of event data. Event data may include clicks/impressions of ads, it could be network event data, or data from a systems monitoring perspective.
* This approach should be used when
  * You generate your reporting/alerts by doing analysis on the data with time as one axis.
  * You’re regularly rolling off data so that you have a limited retention of it.

#### Hash strategy

![range](../.gitbook/assets/mysql_sharding_hashstrategy.png)

##### By entity id

* Shard based on hashing value of a field. 
* Pros:
  * Evenly distributed data
* Cons:
  * Hard to add a new shard. Lots of data migration need to happen. 

### How to store unsharded table

* Store a copy within each shard
  * Cons: Write across to guarantee consistency
* Use a centralized node to store
  * Cons: Need to have cross shard joins

### How to deploy shards on nodes

* Each shard gets stored in a separate database, and each database gets stored in a separate node. 
* Each shard gets stored in a separate database, and multiple database gets stored in a single node. 

### Cross shard join

* Query types:
  * Join queries: 
  * count queries:
  * order by queries:
* Solutions:
  * Aggregate query result for different shard within application code.
  * Usually use two sets of data to solve the problem
    * One data is based on unique sharding key.
    * The other one is data replicated asynchronously to Elasticsearch or Solr.

### Distributed transactions (write across shards)

* Original transaction needs to be conducted within a distributed transaction.
  * e.g. ecommerce example (order table and inventory table)
* There are wwo ways in general to implement distributed transactions:
  * 2PC 
  * TCC
* For example, some software has built-in implementations such as:
  * MySQL XA
  * Spring JTA

### Unique global ID

* Please see [ID generator](Scenario_IDGenerator.md)

### Challenges in Graph DB sharding

* Graph model is most common in B2C apps like Facebook and Instagram. 
* With this model, data is often replicated in a few different forms. Then it is the responsibility of the application to map to the form that is most useful to acquire the data. The result is you have multiple copies for your data sharded in different ways, eventual consistency of data typically, and then have some application logic you have to map to your sharding strategy. For apps like Facebook and Reddit there is little choice but to take this approach, but it does come at some price.

## ShardingSphere

* ShardingSphere has three solutions: ShardingJDBC / ShardingProxy / ShardingSphere

## Sharding JDBC

![](../.gitbook/assets/mysql-sharding-jdbc.png)

## Sharding Proxy**

![](../.gitbook/assets/mysql-sharding-proxy.png)

### Sharding example (In Chinese)

* Original table

![](../.gitbook/assets/mysql-sharding-ecommerce-example.png)

* Sharded result
  * Vertical sharding: Store, product and Sku should be stored in three different databases.
  * Fast growing table: Among all three database, Sku table will grow much faster than product and store. 
  * Binding table: Sku and SkuInfo always appear together. Product and productType usually appear together. They should be sharded according to the same column. 

![](../.gitbook/assets/mysql-sharding-ecommerce-example-result.png)
