- [Cluster and unclustered index](#cluster-and-unclustered-index)
  - [Clustered index structure](#clustered-index-structure)
  - [Unclustered / Secondary index structure](#unclustered--secondary-index-structure)
  - [Capacity estimation](#capacity-estimation)
    - [Clustered index capacity - 5M](#clustered-index-capacity---5m)
    - [Unclustered index capacity - 1G](#unclustered-index-capacity---1g)
- [Unique index](#unique-index)
- [Covering index](#covering-index)
  - [Example](#example)
- [Composite index](#composite-index)
  - [Def](#def)
  - [Why not multiple secondary index](#why-not-multiple-secondary-index)
  - [Left-prefix index rule](#left-prefix-index-rule)
- [Hash index for == and IN](#hash-index-for--and-in)
  - [Adaptive hash index](#adaptive-hash-index)
- [Queries not using index](#queries-not-using-index)
  - [References](#references)

# Cluster and unclustered index

|   | `Clustered index`  | `Unclustered index`  |
|---|---|---|
| `Number`  | Since a clustered index impacts the physical organization of the data, there can be only one clustered index per table.  | Since an unclustered index only points to data location, at least two operations need to be performed for accessing data.   |
| `Example`  | mySQL innoDB primary key index is a clustered index (both index and data inside \*.idb file)  | mySQL myISAM index is an unclustered index (index inside _.myi file and data inside \\_.myd file)  |
| `Perf`  | For read queries: Clustered index will typically perform a bit faster because only needs to read disk once (data and index stored together)  | For write updates/deletes: Unclustered index will typically perform a bit faster because for unclustered index approach, the data part could be written in an append-only fashion and index part could be inserted.   |
| `Range query` | Primary/Clustered index based range queries are very efficient. There might be a possibility that the disk block that the database has read from the disk contains all the data belonging to the query, since the primary index is clustered & records are ordered physically. So the locality of data can be provided by the primary index | Since the primary index contains a direct reference to the data block address through the virtual address space & disk blocks are physically organized in the order of the index key, every time the OS does some disk page split due to DML operations like INSERT / UPDATE / DELETE, the primary index also needs to be updated. So DML operations puts some pressure on the performance of the primary index.
 |

## Clustered index structure
* If leaf nodes store actual data, it is a clustered index. 

![Clustered index](../.gitbook/assets/mysql_internal_clusteredIndex.png)

## Unclustered / Secondary index structure
* Primary index points to data and secondary index points to primary key. Primary index will be a clustered index and secondary index will be an unclustered index.  

* Secondary index only points to primary key because
  * Save space: Avoid storing copies of data. 
  * Keep data consistency: When there are updates on primary key, all other secondary indexes need to be updated at the same time. 

![Index B tree secondary index](../.gitbook/assets/mysql_index_secondaryIndex.png)

## Capacity estimation
### Clustered index capacity - 5M
* First/Root layer (Store indexes only): 
  * For non-leaf node, suppose that the primary key is an integer (8 Byte / 64 bits) and the address pointer to next level is also 8 bytes / 64 bits. 
  * The MySQL InnoDB database engine has block size of 16 KB. It means every time you read or write data to the database, a block of disk pages of size 16 KB will be fetched from the disk into RAM, it will get manipulated and then written back to disk again if required.
  * The first layer has in total 16 KB / 16 Byte = 1K children
* Second layer (Store indexes only): 
  * 1K node with 1K \* 1K = 1M children 
* Third layer (Store indexes and record): 
  * For leaf node, suppose that record size is 1KB. 
  * Each node could store 16KB / 1KB = 16 records. 
  * In total, there could be
    * 1M \* 16 = 16M records stored in an InnoDB table. 
    * Store 1,048,576 \* 16 =  16,777,216
  * In practice, each InnoDB usage not bigger than 5 million

### Unclustered index capacity - 1G
* The first two layers will be the same as above.
* Unclustered index approach could store more data because all three layers of tree are indexes. 
  * 1024 * 1024 * 1024 = 1G records

# Unique index
* Like primary keys, unique keys can also identify records uniquely with one difference — the unique key column can contain null values.
* Unlike other database servers, in MySQL a unique key column can have as many null values as possible. In SQL standard, null means an undefined value. So if MySQL has to contain only one null value in a unique key column, it has to assume that all null values are the same.

```SQL
CREATE UNIQUE INDEX unique_idx_1 ON index_demo (pan_no);
```

# Covering index
* A covering index is a special kind of composite index where all the columns specified in the query somewhere exist in the index. 

## Example 
* The columns mentioned in the SELECT & WHERE clauses are part of the composite index. So in this case, we can actually get the value of the age column from the composite index itself. Let’s see what the EXPLAIN command shows for this query:

```SQL
-- For a composite index on (pan_no, name, age)  and the following query
SELECT age FROM index_demo WHERE pan_no = 'HJKXS9086W' AND name = 'kousik'
```

# Composite index
## Def
* Multiple column builds a single index. MySQL lets you define indices on multiple columns, up to 16 columns. This index is called a Multi-column / Composite / Compound index. If certain fields are appearing together regularly in queries, please consider creating a composite index.
* Let’s say we have an index defined on 4 columns — col1, col2, col3, col4. With a composite index, we have search capability on col1, (col1, col2) , (col1, col2, col3) , (col1, col2, col3, col4). So we can use any left side prefix of the indexed columns, but we can’t omit a column from the middle & use that like — (col1, col3) or (col1, col2, col4) or col3 or col4 etc. These are invalid combinations.

## Why not multiple secondary index
* MySQL uses only one index per table per query except for UNION. (In a UNION, each logical query is run separately, and the results are merged.) So defining multiple indices on multiple columns does not guarantee those indices will be used even if they are part of the query.

## Left-prefix index rule
1. Left-to-right, no skipping: MySQL can only access the index in order, starting from the leftmost column and moving to the right. It can't skip columns in the index.
2. Stops at the first range: MySQL stops using the index after the first range condition encountered.

# Hash index for == and IN

* B+ tree index
  * Use case: Used in 99.99% case because it supports different types of queries
* Hash index
  * Use case: Only applicable for == and IN type of query, does not support range/order by query, could not be used in left-prefix composite index

## Adaptive hash index

![Index B tree secondary index](../.gitbook/assets/mysql_index_adaptiveHashIndex.png)

# Queries not using index
* Used SQL query including !=, LIKE
* Had special expressions such as math and function call. 
* When dataset size is too small. 

## References
* https://medium.com/free-code-camp/database-indexing-at-a-glance-bb50809d48bd