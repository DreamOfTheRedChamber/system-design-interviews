- [UUID](#uuid)
  - [Pros](#pros)
  - [Cons](#cons)

# UUID

* UUIDs are 128-bit hexadecimal numbers that are globally unique. The chances of the same UUID getting generated twice is negligible.

## Pros

* Self-generation uniqueness: They can be generated in isolation and still guarantee uniqueness in a distributed environment. 
* Minimize points of failure: Each application thread generates IDs independently, minimizing points of failure and contention for ID generation. 

## Cons

* Generally requires more storage space (96 bits for MongoDB Object ID / 128 bits for UUID). It takes too much space as primary key of database. 
* UUID could be computed by using hash of the machine's MAC address. There is the security risk of leaking MAC address. 
