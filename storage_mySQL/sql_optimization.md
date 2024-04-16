- [InnoDB buffer improvement](#innodb-buffer-improvement)
- [Slow query](#slow-query)
- [Do](#do)
  - [Always define a primary key for each table](#always-define-a-primary-key-for-each-table)
  - [Use auto-increment int column when possible](#use-auto-increment-int-column-when-possible)
  - [Push range query conditions to last](#push-range-query-conditions-to-last)
  - [Order/Group By](#ordergroup-by)
  - [Use IN for low radix attributes if leftmost prefix index could not be used](#use-in-for-low-radix-attributes-if-leftmost-prefix-index-could-not-be-used)
  - [Use efficient pagination](#use-efficient-pagination)
  - [Use covering index to avoid low](#use-covering-index-to-avoid-low)
  - [Join](#join)
  - [NOT NULL constraint on column](#not-null-constraint-on-column)
- [Don't](#dont)
  - [IN operator](#in-operator)
  - [Unequal filter when possible](#unequal-filter-when-possible)
  - [Filtering based on Nullable match conditions](#filtering-based-on-nullable-match-conditions)
  - [Prefix based fuzzy matching](#prefix-based-fuzzy-matching)
  - [Type conversion in the filtering condition](#type-conversion-in-the-filtering-condition)
  - [Functions on index](#functions-on-index)
  - [Computation expression on index](#computation-expression-on-index)
  - [Or condition](#or-condition)

# InnoDB buffer improvement
*  InnoDB tries to minimise disk I/O operation by using a buffer. Following is the representation:

![](../.gitbook/assets/mysql_datastructure_innodb_buffer.png)

* InnoDB buffer inserts, deletes, and updates if the needed leaf node is not in memory. The buffer is flushed when it is full or the corresponding leaf nodes come into memory. This way InnoDB defers the disk I/O operation. But still, database write operation can be made much much faster by leveraging the available disk bandwidth which existing relational databases fail to do. Also relational database systems are very complex inside as they use locking, concurrency, ACID transaction semantics etc which makes read write operation more complex.

# Slow query

* In most cases, please use EXPLAIN to understand the execution plan before optimizing. But there are some patterns practices which are known to have bad performance. 
* [https://coding.imooc.com/lesson/49.html\#mid=513](https://coding.imooc.com/lesson/49.html#mid=513)
* [https://study.163.com/course/courseLearn.htm?courseId=1209773843\#/learn/video?lessonId=1280437152&courseId=1209773843](https://study.163.com/course/courseLearn.htm?courseId=1209773843#/learn/video?lessonId=1280437152&courseId=1209773843)

# Do
## Always define a primary key for each table

1. When PRIMARY KEY is defined, InnoDB uses primary key index as the clustered index. 
2. When PRIMARY KEY is not defined, InnoDB will use the first UNIQUE index where all the key columns are NOT NULL and InnoDB uses it as the clustered index.
3. When PRIMRARY KEY is not defined and there is no logical unique and non-null column or set of columns, InnoDB internally generates a hidden clustered index named GEN\_CLUST\_INDEX on a synthetic column containing ROWID values. The rows are ordered by the ID that InnoDB assigns to the rows in such a table. The ROWID is a 6-byte field that increases monotonically as new rows are inserted. Thus, the rows ordered by the row ID are physically in insertion order.

## Use auto-increment int column when possible

* Why prefer auto-increment over random \(e.g. UUID\)? 
  * In most cases, primary index uses B+ tree index. 
  * For B+ tree index, if a new record has an auto-increment primary key, then it could be directly appended in the leaf node layer. Otherwise, B+ tree node split and rebalance would need to be performed. 
* Why int versus other types \(string, composite primary key\)?
  * Smaller footprint: Primary key will be stored within each B tree index node, making indexes sparser. Things like composite index or string based primary key will result in less index data being stored in every node. 

## Push range query conditions to last

* For range query candidate, please push it to the last in composite index because usually the column after range query won't really be sorted. 

## Order/Group By

* When using EXPLAIN, the ext column means whether the Order/Group By uses file sort or index sort
* If the combination of WHERE and ORDER/GROUP BY satisfies the leftmost prefix index, then 

## Use IN for low radix attributes if leftmost prefix index could not be used

```SQL
-- using dating website as an example
-- 1. Composite index: city, sex, age
select * from users_table where city == XX and sex == YY and age <= ZZ

-- 2. There will be cases where some users don't filter based on sex
select * from users_table where city == XX and age <= ZZ

-- 3. Could use IN to make WHERE clause satisfy leftmost prefix condition
select * from users_table where city == XX and Sex in ('male', 'female') and age <= ZZ
```

## Use efficient pagination

* Pagination starts from a large offset index.

```SQL
-- Original query
select * from myshop.ecs_order_info order by myshop.ecs_order_info.order_id limit 4000000, 100

-- Optimization option 1 if order_id is continuous, 
select * from myshop.ecs_order_info order where myshop.ecs_order_info.order_id between 4000000 and 4000100

-- Optimization option 2 if order_id is not continuous,
-- Compared the original query, the child query "select order_id ..." uses covering index and will be faster.
select * from myshop.ecs_order_info where 
(myshop.ecs_order_info.order_id >= (select order_id from myshop.ecs_order_info order by order_id limit 4000000,1) limit 100)
```

```SQL
-- Original query
select * from myshop.ecs_users u where u.last_login_time >= 1590076800 order by u.last_login_time, u.user_id limit 200000, 10

-- Optimization with join query
select * from myshop.ecs_users u (select user_id from myshop.ecs_users where u.last_login_time >= 1590076800) u1 where u1.user_id = u.user_id order by u.user_id
```

## Use covering index to avoid low

* Def: A special kind of composite index where all the columns specified in the query exist in the index. So the query optimizer does not need to hit the database to get the data — rather it gets the result from the index itself. 
* Special benefits: Avoid second-time query on Innodb primary key
* Limitations:
  * Only a limited number of indexes should be set up on each table. So could not rely on covered index. 
  * There are some db engine which does not support covered index

```SQL
-- original query
select * from orders where order = 1

-- Optimized by specifying the columns to return
-- order_id column has index
-- queried columns already contain filter columns
select order_id from orders where order_id = 1
```

## Join
* When joining two tables, assume table A has num1 returned according to the JOIN condition, table B has num2 returned according to the JOIN condition. And Assume num1 &gt; num2. 
* Make sure:
  * Query against table B \(smaller\) will be executed first.
  * filters on table A \(bigger\) will be based on indexed column. 
* Avoid using more than three joins in any case. 
  * For join, handle that inside application code when the join is big. Business applications are easier to scale. 
* Two algorithms:
  * Block nested join
  * Nested loop join

## NOT NULL constraint on column

# Don't

## IN operator
* When there are too few or many operators inside IN, it might not go through index. 

## Unequal filter when possible
* Don't use "IS NOT NULL" or "IS NULL": Index \(binary tree\) could not be created on Null values. 
* Don't use != : Index could not be used. Could use &lt; and &gt; combined together.
  * Select name from abc where id != 20
  * Optimized version: Select name from abc where id &gt; 20 or id &lt; 20

## Filtering based on Nullable match conditions
* There are only two values for a null filter \(is null or is not null\). In most cases it will do a whole table scanning. 

## Prefix based fuzzy matching
* Use % in the beginning will cause the database for a whole table scanning. 

```SQL 
SELECT name from abc where name like %xyz
```

## Type conversion in the filtering condition

## Functions on index

* [https://coding.imooc.com/lesson/49.html\#mid=439](https://coding.imooc.com/lesson/49.html#mid=439)
* Don't use function or expression on index column

```SQL
-- Original query:
select ... from product
where to_days(out_date) - to_days(current_date) <= 30

-- Improved query:
select ... from product
where out_date <= date_add(current_date, interval 30 day)
```

## Computation expression on index

```SQL
SELECT comment_id, user_id, comment_text FROM product_comment WHERE comment_id+1 = 900001
```

## Or condition
* If only one condition inside OR has index. 

```SQL
SELECT comment_id, user_id, comment_text FROM product_comment WHERE comment_id = 900001 OR comment_text = '462eed7ac6e791292a79'
```
