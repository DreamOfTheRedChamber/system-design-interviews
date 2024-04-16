- [UUID](#uuid)
  - [Pros](#pros)
  - [Cons](#cons)

# UUID

* UUIDs are 128-bit hexadecimal numbers that are globally unique. Its format is in 8-4-4-4-12 and in total 36 characters.  

```
550e8400-e29b-41d4-a716-446655440000
```

## Pros
* High performance: Could be generated locally. 

## Cons
* Generally requires more storage space (96 bits for MongoDB Object ID / 128 bits for UUID). It takes too much space as primary key of database. 
* UUID could be computed by using hash of the machine's MAC address. There is the security risk of leaking MAC address. 
* UUID not suitable for primary key due to its length and unorderness. 
  * All indexes other than the clustered index are known as secondary indexes. In InnoDB, each record in a secondary index contains the primary key columns for the row, as well as the columns specified for the secondary index. InnoDB uses this primary key value to search for the row in the clustered index.*** If the primary key is long, the secondary indexes use more space, so it is advantageous to have a short primary key.
  * Not efficient when used as primary key.
