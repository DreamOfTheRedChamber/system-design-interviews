- [SQL database](#sql-database)
  - [Schema design](#schema-design)
  - [Database schema](#database-schema)
  - [A single database](#a-single-database)
  - [Adding offline key generators](#adding-offline-key-generators)


# SQL database
## Schema design
## Database schema

```SQL
Create table tiny_url (
    Id bigserial primary key,
    short_url text not null, 
    original_url text not null,
    expired_datetime timestamp without timezone)
```

## A single database
* Before insert random url into database. Check whether it exists within database, if not then insert. 
* Cons:
  * Easy to timeout 
  * Heavy load on database when size is big

## Adding offline key generators
* Offline job generates keys \(Daemon process\) and stores into database.
  * Offline job can tolerate longer query time. 
* Keys database schema
  * create table keys\(Key text primary key, status integer\)
  * status
    * 0: available
    * 1: occupied
* Keys database query 1. Select key from keys where status = 0 limit 1 2. Update keys set status = 1 where key = a\_key
* Cons: Race condition
  * Multiple users can get same keys if qps is high
    * Select / Update is not atomic operation
  * Database is not suitable for queuing
  * Solution: 
    1. Database lock: Select key from keys where status = 0 limit 1 for update skip locked
    2. Distributed lock: bad performance. 
    3. Redis -&gt; LPush and LPop: Redis list data structure is actually queue and it is thread-safe and production ready

