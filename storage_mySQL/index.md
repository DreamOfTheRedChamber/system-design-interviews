- [Factors impacting DB performance](#factors-impacting-db-performance)
- [Primary key](#primary-key)
- [Index](#index)
  - [Use case](#use-case)
  - [Clustered/Primary](#clusteredprimary)
    - [Def](#def)
    - [Structure](#structure)
    - [Comparison with unclustered index](#comparison-with-unclustered-index)
  - [Unclustered / Secondary index](#unclustered--secondary-index)
    - [Def](#def-1)
    - [Structure](#structure-1)
  - [Unique index](#unique-index)
  - [B+ tree vs hash index](#b-tree-vs-hash-index)
  - [Adaptive hash index](#adaptive-hash-index)
  - [Composite index](#composite-index)
    - [Def](#def-2)
    - [Why not multiple secondary index](#why-not-multiple-secondary-index)
    - [Covering index](#covering-index)
  - [Partial index](#partial-index)
- [Choose index columns](#choose-index-columns)
  - [References](#references)

# Factors impacting DB performance

* Hardware 
* Operating system
* DB engine selection
* DB configuration parameters
* DB schema design
  * Slow queries

# Primary key
* A primary key should be part of many vital queries in your application.
* Primary key is a constraint that uniquely identifies each row in a table. If multiple columns are part of the primary key, that combination should be unique for each row.
* Primary key should be Non-null. Never make null-able fields your primary key. By ANSI SQL standards, primary keys should be comparable to each other, and you should definitely be able to tell whether the primary key column value for a particular row is greater, smaller or equal to the same from other row. Since NULL means an undefined value in SQL standards, you can’t deterministically compare NULL with any other value, so logically NULL is not allowed.
* The ideal primary key type should be a number like INT or BIGINT because integer comparisons are faster, so traversing through the index will be very fast.

# Index
## Use case
* Pros:
  * Change random to sequential IO
  * Reduce the amount of data to scan
  * Sort data to avoid using temporary table
* Cons: 
  * Slow down writing speed
  * Increase query optimizer process time

## Clustered/Primary
### Def
* If within the index, the leaf node stores the entire data record, then it is a clustered index. "Clustered" literrally means whether the index and data are clustered together. 
* Overview
  * The yellow coloured big rectangle represents a disk block / data block
  * The blue coloured rectangles represent data stored as rows inside that block
  * The footer area represents the index of the block where red coloured small rectangles reside in sorted order of a particular key. These small blocks are nothing but sort of pointers pointing to offsets of the records.

![](../.gitbook/assets/mysql_clusteredIndex_composition.png)

### Structure

![](../.gitbook/assets/mysql_internal_clusteredIndex.png)

![](../.gitbook/assets/mysql_internal_unclusteredindex.png)

### Comparison with unclustered index

|   | `Clustered index`  | `Unclustered index`  |
|---|---|---|
| `Number`  | Since a clustered index impacts the physical organization of the data, there can be only one clustered index per table.  | Since an unclustered index only points to data location, at least two operations need to be performed for accessing data.   |
| `Example`  | mySQL innoDB primary key index is a clustered index (both index and data inside \*.idb file)  | mySQL myISAM index is an unclustered index (index inside _.myi file and data inside \\_.myd file)  |
| `Perf`  | For read queries: Clustered index will typically perform a bit faster because only needs to read disk once (data and index stored together)  | For write updates/deletes: Unclustered index will typically perform a bit faster because for unclustered index approach, the data part could be written in an append-only fashion and index part could be inserted.   |
| `Range query` | Primary/Clustered index based range queries are very efficient. There might be a possibility that the disk block that the database has read from the disk contains all the data belonging to the query, since the primary index is clustered & records are ordered physically. So the locality of data can be provided by the primary index | Since the primary index contains a direct reference to the data block address through the virtual address space & disk blocks are physically organized in the order of the index key, every time the OS does some disk page split due to DML operations like INSERT / UPDATE / DELETE, the primary index also needs to be updated. So DML operations puts some pressure on the performance of the primary index.
 |

## Unclustered / Secondary index
### Def
* Def: Primary index points to data and secondary index points to primary key. Primary index will be a clustered index and secondary index will be an unclustered index.  

### Structure
* Why secondary index only points to primary key: 
  * Save space: Avoid storing copies of data. 
  * Keep data consistency: When there are updates on primary key, all other secondary indexes need to be updated at the same time. 

![Index B tree secondary index](../.gitbook/assets/mysql_index_secondaryIndex.png)

## Unique index
* Like primary keys, unique keys can also identify records uniquely with one difference — the unique key column can contain null values.
* Unlike other database servers, in MySQL a unique key column can have as many null values as possible. In SQL standard, null means an undefined value. So if MySQL has to contain only one null value in a unique key column, it has to assume that all null values are the same.

```SQL
CREATE UNIQUE INDEX unique_idx_1 ON index_demo (pan_no);
```

## B+ tree vs hash index

* B+ tree index
  * Use case: Used in 99.99% case because it supports different types of queries
* Hash index
  * Use case: Only applicable for == and IN type of query, does not support range/order by query, could not be used in left-prefix composite index

## Adaptive hash index

![Index B tree secondary index](../.gitbook/assets/mysql_index_adaptiveHashIndex.png)

## Composite index
### Def
* Multiple column builds a single index. MySQL lets you define indices on multiple columns, up to 16 columns. This index is called a Multi-column / Composite / Compound index. If certain fields are appearing together regularly in queries, please consider creating a composite index.
* Let’s say we have an index defined on 4 columns — col1, col2, col3, col4. With a composite index, we have search capability on col1, (col1, col2) , (col1, col2, col3) , (col1, col2, col3, col4). So we can use any left side prefix of the indexed columns, but we can’t omit a column from the middle & use that like — (col1, col3) or (col1, col2, col4) or col3 or col4 etc. These are invalid combinations.

### Why not multiple secondary index
* MySQL uses only one index per table per query except for UNION. (In a UNION, each logical query is run separately, and the results are merged.) So defining multiple indices on multiple columns does not guarantee those indices will be used even if they are part of the query.

### Covering index
* A covering index is a special kind of composite index where all the columns specified in the query somewhere exist in the index. So the query optimizer does not need to hit the database to get the data — rather it gets the result from the index itself. Example: we have already defined a composite index on (pan_no, name, age) , so now consider the following query:
* The columns mentioned in the SELECT & WHERE clauses are part of the composite index. So in this case, we can actually get the value of the age column from the composite index itself. Let’s see what the EXPLAIN command shows for this query:

```SQL
SELECT age FROM index_demo WHERE pan_no = 'HJKXS9086W' AND name = 'kousik'
```

## Partial index
* We already know that Indices speed up our queries at the cost of space. The more indices you have, the more the storage requirement. We have already created an index called secondary_idx_1 on the column name. The column name can contain large values of any length. Also in the index, the row locators’ or row pointers’ metadata have their own size. So overall, an index can have a high storage & memory load.
* In MySQL, it’s possible to create an index on the first few bytes of data as well. Example: the following command creates an index on the first 4 bytes of name. Though this method reduces memory overhead by a certain amount, the index can’t eliminate many rows, since in this example the first 4 bytes may be common across many names. Usually this kind of prefix indexing is supported on CHAR ,VARCHAR, BINARY, VARBINARY type of columns.


# Choose index columns

* General rules
  * On columns not changing often
  * On columns which have high cardinality
  * On columns whose sizes are smaller. If the column's size is big, could consider build index on its prefix. 
* Create indexes on columns frequently used in Where / Order By / Group By / Distinct condition
* Avoid create indexes when
  * There are too few records

```SQL
-- create index on prefix of a column
CREAT INDEX on index_name ON table(col_name(n))
```

## References
* https://medium.com/free-code-camp/database-indexing-at-a-glance-bb50809d48bd