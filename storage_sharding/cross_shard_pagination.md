- [Problem](#problem)
- [Global query](#global-query)
  - [Steps](#steps)
  - [Cons - Low performance](#cons---low-performance)
    - [High network volume](#high-network-volume)
    - [High memory footprint](#high-memory-footprint)
- [Average pagination](#average-pagination)
  - [Steps](#steps-1)
  - [Cons - Accuracy](#cons---accuracy)
- [Forbid pagination](#forbid-pagination)
- [Intermediate table](#intermediate-table)

# Problem
* In sharding cases, how will the following pagination query be executed

```sql
SELECT * FROM order_tab ORDER BY id LIMIT 4 OFFSET 2
```

# Global query
## Steps
1. "LIMIT x OFFSET y" gets transformed to "LIMIT x+y OFFSET 0".

```sql
SELECT * FROM order_tab ORDER BY id LIMIT 6 OFFSET 0
SELECT * FROM order_tab ORDER BY id LIMIT 6 OFFSET 0
```

2. Suppose that there are N tables hit. After getting results from N tables, it could be merged together using "merge sort". 

## Cons - Low performance
### High network volume
* For a query "LIMIT 10 OFFSET 1000"
* In cross-shard query scenarios, it will be tranformed to "LIMIT 1010 OFFSET 1000". 
* If there are N table hit, in total N * 1010 rows need to be transmitted. 

### High memory footprint
* Since query results for N tables need to sit inside memory and then merged, the memory footprint will be high. 

# Average pagination
## Steps
* Divide the LIMIT and OFFSET by 2

```sql
SELECT * FROM order_tab ORDER BY id LIMIT 4 OFFSET 2

--Transformed to the following:
SELECT * FROM order_tab ORDER BY id LIMIT 2 OFFSET 1
SELECT * FROM order_tab ORDER BY id LIMIT 2 OFFSET 1
```

## Cons - Accuracy
* It will get an approximate answer in most cases. 
* The improved version could be an weighted average pagination. 

# Forbid pagination

# Intermediate table