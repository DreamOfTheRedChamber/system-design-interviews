- [NoSQL](#nosql)
  - [Motivations](#motivations)
  - [NoSQL vs SQL](#nosql-vs-sql)
  - [NoSQL flavors](#nosql-flavors)
    - [Key-value](#key-value)
    - [Document](#document)
    - [Column-Family](#column-family)
    - [Graph](#graph)
  - [TODO](#todo)

# NoSQL

## Motivations
* RDBMS are first choice of people because of the following reasons:
  * Good ACID support.
  * Easy SQL queries to access the data.
  * Operational confidence in case something goes wrong.
  * Reliability, maturity, community support & high performance on individual storage node.
  * Support for secondary index.
* RDBMS becomes bottleneck because
  * Too Many Index — in order to make db operations faster, people create suitable index ( singular or composite index ) on different columns. So every time we update something, db has to update the index, maintain it. If there are millions of rows in a table and say you add a new column in a table, the operation may take several countable minutes to perform that operation as it takes a lock on the whole table in order to guarantee consistency. When we delete some column, it’s typical that the corresponding index stays & hogs up memory. So maintaining index becomes a real operational challenge.
  * Sharding / Partitioning data: RDBMS systems struggle to scale with millions of data. No built in sharding mechanism. Developers create sharding mechanism which might or might not serve at scale well without solid failover mechanism or clustering per shard.
  * Deadlock: Chances of deadlock even when tens of transactions are waiting or locked for resources.
  * Table Joining: Lots of joining may create a lot of temp tables thus hogging up memory, disk, slowing up the query execution, timing out connections. Although, breaking a big query into multiple small queries can help here.
  * Write Speed: RDBMS provide consistent write, they don’t write on the disk sequentially, even their B-Tree backed index is also random read/write data structure. So write speed at scale suffers.
  * No Queue Support: Not all use cases require relational queries, some require FIFO ordering. Consider, your new feed on Facebook, if you want to store the feeds in a mysql table ( just a naive example ), you can do that ordering by timestamp, but internally, you are actually using the B-Tree based index which takes O(logN) time ( considering balanced tree) to operate on a data. This just does not scale well for simple FIFO dominated operations.
  * No TTL support: RDBMS does not support out of the box TTL. So in such use cases where business does not need to store data forever, extra worker / cron jobs are to be written which will archive & delete the row, it adds up to the extra pressure when the traffic is in millions.

## NoSQL vs SQL

* There is no generally accepted definition. All we can do is discuss some common characteristics of the databases that tend to be called "NoSQL".

| Database          |                                                                                                                                                        SQL                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                                                                           NoSQL |
| ----------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| Data uniformity   |                                                      Uniform data. Best visualized as a set of tables. Each table has rows, with each row representing an entity of interest. Each row is described through columns. One row cannot be nested inside another.                                                     |                                                                                          Non-uniform data. NoSQL databases recognize that often, it is common to operate on data in units that have a more complex structure than a set of rows. This is particularly useful in dealing with nonuniform data and custom fields. NoSQL data model can be put into four categories: key-value, document, column-family and graph. |
| Schema change     |                                                                 Define what table exists, what column exists, what data types are. Although actually relational schemas can be changed at any time with standard SQL commands, it is of high cost.                                                                |                                                                                                                                                                                                                                                                                                      Changing schema is casual and of low cost. Essentially, a schemaless database shifts the schema into the application code. |
| Query flexibility |                                                                                  Low cost on changing query. It allows you to easily look at the data in different ways. Standard SQL supports things like joins and subqueries.                                                                                  |                                                                                                                                                                                                                                                            High cost in changing query. It does not allow you to easily look at the data in different ways. NoSQL databases do not have the flexibility of joins or subqueries. |
| Transactions      | SQL has ACID transactions (Atomic, Consistent, Isolated, and Durable). It allows you to manipulate any combination of rows from any tables in a single transaction. This operation either succeeds or fails entirely, and concurrent operations are isolated from each other so they cannot see a partial update. | Graph database supports ACID transactions. Aggregate-oriented databases do not have ACID transactions that span multiple aggregates. Instead, they support atomic manipulation of a single aggregate at a time. If we need to manipulate multiple aggregates in an atomic way, we have to manage that ourselves in application code. An aggregate structure may help with some data interactions but be an obstacle for others. |
| Consistency       |                                                                                                                                                 Strong consistency                                                                                                                                                |                                                                                                                                                                                                                                                                                                                                                 Trade consistency for availability or partition tolerance. Eventual consistency |
| Scalability       |                                                                                   relational database use ACID transactions to handle consistency across the whole database. This inherently clashes with a cluster environment                                                                                   |                                                                         Aggregate structure helps greatly with running on a cluster. It we are running on a cluster, we need to minize how many nodes we need to query when we are gathering data. By using aggregates, we give the database important information about which bits of data (an aggregate) will be manipulated together, and thus should live on the same node. |
| Performance       |                                                                                                                                             MySQL/PosgreSQL \~ 1k QPS                                                                                                                                             |                                                                                                                                                                                                                                                                                                                                                                 MongoDB/Cassandra \~ 10k QPS. Redis/Memcached \~ 100k \~ 1M QPS |
| Maturity          |                                                                                                    Over 20 years. Integrate naturally with most web frameworks. For example, Active Record inside Ruby on Rails                                                                                                   |                                                                                                                                                                                                                                                                                                                                             Usually less than 10 years. Not great support for serialization and secondary index |

* Scenarios where MySQL does not work so well [Pinterest experience](https://medium.com/pinterest-engineering/learn-to-stop-using-shiny-new-things-and-love-mysql-3e1613c2ce14)
  * Cartesian Distance: If you need to search for nearby points in two dimensions, storing coordinates as Geohashes in MySQL would work well (here’s an XKCD comic to help). Three dimensions would probably also work well. But if you need large N-dimensional search spaces, I don’t know of a good way to store and retrieve them in MySQL efficiently. You might find yourself needing to store N-dimensional points if, for instance, you have created a model that produces feature vectors for some input and you want to see if two inputs are similar. Classic examples include determining if two images are similar but not exactly the same. For these sorts of situations, consider building a distributed RP/KD tree (would love to collaborate! Email me!).
  * Speed of writes: MySQL delivers full write consistency. If you’re willing to trade off “full” for “eventual” consistency, your writes can be much faster. HBase, Cassandra and other similar technologies write to an update log incredibly fast, at the expense of making reads slower (reads must now read the stored info and walk the update log). This is a nice inversion, because it’s easier to cache reads and make them fast.
  * FIFOs, such as feeds: My biggest complaint about MySQL is that it’s still living in 1994 (with baggy pants and the.. erk.. Macarena). Many uses of databases back then needed relational queries. There were no social networks. MySpace wouldn’t come around for another nine years! And so MySQL is built out of trees and has no good notion of queues. To insert into a B-tree is an O(lg(N)) operation (assuming happy balance). But today, social networks are a major force on the internet, and they depend heavily on queues. We want uber fast O(1) enqueuing! My suggestion is to not use MySQL for feeds. It’s too much overhead. Instead, consider Redis, especially if you’re still a small team. Redis is super fast and has lists with fast insertion and retrieval. If you’re a larger company and can hire the folks to maintain it, consider HBase. It’s working well for our feeds.
  * Logs: For the love of everything holy, don’t store logs in MySQL. As mentioned in the previous paragraph, MySQL stores things in trees. Logs should not live in trees (that’s a weird thing to say…). Send your logs to Kafka, then use Secor to read from Kafka and ship up to S3. Then go nuts with EMR, Qubole or your map-reduce platform du jour.
  * Scale beyond one box: If you’re in this position and you’ve optimized all queries (no joins, foreign keys, distinct, etc.), and you’re now considering using read slaves, you need to scale to beyond one MySQL server. MySQL won’t do this for you out of the box, but it’s not hard. And we have a solution, which you’ll be able to read about and learn how we did this once I finish writing the blog post

## NoSQL flavors

### Key-value

* Suitable use cases
  * **Storing session information**: Generally, every web session is unique and is assigned a unique sessionid value. Applications that store the sessionid on disk or in a RDBMS will greatly benefit from moving to a key-value store, since everything about the session can be stored by a single PUT request or retrieved using GET. This single-request operation makes it very fast, as everything about the session is stored in a single object. Solutions such as Memcached are used by many web applications, and Riak can be used when availability is important
  * **User profiles, Preferences**: Almost every user has a unique userId, username, or some other attributes, as well as preferences such as language, color, timezone, which products the user has access to, and so on. This can all be put into an object, so getting preferences of a user takes a single GET operation. Similarly, product profiles can be stored.
  * **Shopping Cart Data**: E-commerce websites have shopping carts tied to the user. As we want the shopping carts to be available all the time, across browsers, machines, and sessions, all the shopping information can be put into value where the key is the userid. A riak cluster would be best suited for these kinds of applications.
* When not to use
  * **Relationships among Data**: If you need to have relationships between different sets of data, or correlate teh data between different sets of key, key-value stores are not the best solution to use, even though some key-value stores provide link-walking features.
  * **Multioperation transactions**: If you're saving multiple keys and there is a failure to save any of them, and you want to revert or roll back the rest of the operations, key-value stores are not the best solution to be used.
  * **Query by data**: If you need to search the keys based on something found in the value part of the key-value pairs, then key-value stores are not going to perform well for you. This is no way to inspect the value on the database side, with the exception of some products like Riak Search or indexing engines like Lucene.
  * **Operations by sets**: Since operations are limited to one key at a time, there is no way to operate upon multiple keys at the same time. If you need to operate upon multiple keys, you have to handle this from the client side.

### Document

* Suitable use cases
  * **Event logging**: Applications have different event logging needs; within the enterprise, there are many different applications that want to log events. Document databases can store all these different types of events and can act as a central data store for event storage. This is especially true when the type of data being captured by the events keeps changing. Events can be sharded by the name of the application where the event originated or by the type of event such as order_processed or customer_logged.
  * **Content Management Systems, Blogging Platforms**: Since document databases have no predefined schemas and usually uderstand JSON documents, they work well in content management systems or applications for publishing websites, managing user comments, user registrations, profiles, web-facing documents.
  * **Web Analytics or Real-Time Analytics**: Document databases can store data for real-time analytics; since parts of the document can be updated, it's very easy to store page views or unique visitors, and new metrics can be easily added without schema changes.
  * **E-Commerce Applications**: E-commerce applications often need to have flexible schema for products and orders, as well as the ability to evolve their data models without expensive database refactoring or data migration.
* When not to use
  * **Complex Transactions Spanning Different Operations**: If you need to have atomic cross-document operations, then document databases may not be for you. However, there are some document databases that do support these kinds of operations, such as RavenDB.
  * **Queries against Varying Aggregate Structure**: Flexible schema means that the database does not enforce any restrictions on the schema. Data is saved in the form of application entities. If you need to query these entities ad hoc, your queries will be changing (in RDBMS terms, this would mean that as you join criteria between tables, the tables to join keep changing). Since the data is saved as an aggregate, if the design of the aggregate is constantly changing, you need to save the aggregates at the lowest level of granularity-basically, you need to normalize the data. In this scenario, document databases may not work.

### Column-Family

* Suitable use cases
  * **Event Logging**: Column-family databases with their ability to store any data structures are a great choice to store event information, such as application state or errors encountered by the application. Within the enterprise, all applications can write their events to Cassandra with their own columns and the row key of the form appname:timestamp. Since we can scale writes, Cassandra would work ideally for an event logging system.
  * **Content Management Systems, Blogging Platforms**: Using column-families, you can store blog entries with tags, categories, links, and trackbacks in different columns. Comments can be either stored in the same row or moved to a different keyspace; similarly, blog users and the actual blogs can be put into different column families.
  * **Counters**: Often, in web applications you need to count and categorize visitors of a page to calculate analytics, you can use the CounterColumnType during creation of a column family.
  * **Expiring usage**: You may provide demo to users, or may want to show ad banners on a website for a specific time. You can do this by using expiring columns: Cassandra allows you to have columns which, after a given time, are deleted automatically. This time is known as TTL and is defined in seconds. The column is deleted after the TTL has elapsed; when the column does not exist, the access can be revoked or the banner can be removed.

```sql
CREATE COLUMN FAMILY visit_counter
WITH default_validation_class=CounterColumnType
AND key_validation_class=UTF8Type AND comparator=UTF8Type

// Once a column family is created, you can have arbitrary columns for each page visited within the web application for every user. 
INCR visit_counter['mfowler'][home] BY 1;
INCR visit_counter['mfowler'][products] BY 1;
INCR visit_counter['mfowler'][contactus] BY 1;

// expiring columns
SET Customer['mfowler']['demo_access'] = 'allowed' WITH ttl=2592000;
```

* When not to use
  * **ACID transactions for writes and reads**
  * **Database to aggregate the data using queries (such as SUM or AVG)**: you have to do this on the client side using data retrieved by the client from all the rows.
  * **Early prototypes or initial tech spikes**: During the early stages, we are not sure how the query patterns may change, and as the query patterns change, we have to change the column family design. This causes friction for the product innovation team and slows down developer productivity. RDBMS impose high cost on schema change, which is traded off for a low cost of query change; in Cassandra, the cost may be higher for query change as compared to schema change.

### Graph

* Suitable use cases
  * **Connected data**:
    * Social networks are where graph databases can be deployed and used very effectively. These social graphs don't have to be only of the friend kind; for example, they can represent employees, their knowledge, and where they worked with other employees on different projects. Any link-rich domain is well-suited for graph databases.
    * If you have relationships between domain entities from different domains (such as social, spatial, commerce) in a single database, you can make these relationships more valuable by providing the ability to traverse across domains.
  * **Routing, Dispatch, and Location-Based Services**: Every location or address that has a delivery is node, and all the nodes where the delivery has to be made by the delivery person can be modeled as a graph nodes. Relationships between nodes can have the property of distance, thus allowing you to deliver the goods in an efficient manner. Distance and location properties can also be used in graphs of places of interest, so that your application can provide recommendations of good restaurants or entertainment options nearby. You can also create nodes for your points of sales, such as bookstores or restaurants, and notify the users when they are close to any of the nodes to provide location-based services.
  * **Recommendation Engines**:
    * As nodes and relationships are created in the system, they can be used to make recommendations like "your friends also bought this product" or "when invoicing this item, these other items are usually invoiced." Or, it can be used to make recommendations to travelers mentioning that when other visitors come to Barcelona they usually visit Antonio Gaudi's creations.
    * An interesting side effect of using the graph databases for recommendations is that as the data size grows, the number of nodes and relationships available to make the recommendations quickly increases. The same data can also be used to mine information-for example, which products are always bought together, or which items are always invoiced together; alerts can be raised when these conditions are not met. Like other recommendation engines, graph databases can be used to search for patterns in relationships to detect fraud in transactions.
* When not to use
  * When you want to update all or a subset of entities - for example, in an analytics solution where all entities may need to be updated with a changed property - graph databases may not be optimal since changing a peroperty on all the nodes is not a straight-forward operation. Even if the data model works for the problem domain, some databases may be unable to handle lots of data, especially in global graph operations.

## TODO

* [TODO: NoSQL overview slides](https://www.slideshare.net/quipo/nosql-databases-why-what-and-when/116-Google_BigTable_PaperSparse_distributed_persistent)
* TODO: DynamoDB series
  * [DynamoDB: An Inside Look Into NoSQL – Part 1](https://cloudacademy.com/blog/dynamodb-an-inside-look-into-nosql-part-1/)
  * [DynamoDB: An Inside Look Into NoSQL – Part 2](https://cloudacademy.com/blog/dynamodb-an-inside-look-into-nosql-part-2/)
  * [DynamoDB: An Inside Look Into NoSQL – Part 3](https://cloudacademy.com/blog/dynamodb-an-inside-look-into-nosql-part-3/)
  * [DynamoDB: Replication and Partitioning – Part 4](https://cloudacademy.com/blog/dynamodb-replication-and-partitioning-part-4/)
  * [Data Versioning With DynamoDB – NoSQL, Part 5](https://cloudacademy.com/blog/data-versioning-with-dynamodb-an-inside-look-into-nosql-part-5/)
  * [How to Handle Failures in DynamoDB – An Inside Look Into NoSQL, Part 6](https://cloudacademy.com/blog/how-to-handle-failures-in-dynamodb-an-inside-look-into-nosql-part-6/)
  * [Membership and Failure Detection in DynamoDB: An Inside Look Into NoSQL, Part 7](https://cloudacademy.com/blog/membership-and-failure-detection-in-dynamodb-an-inside-look-into-nosql-part-7/)
