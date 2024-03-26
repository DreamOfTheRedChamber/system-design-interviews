- [Storage](#storage)
  - [Database layer](#database-layer)
  - [SQL](#sql)
  - [NoSQL](#nosql)


# Storage
## Database layer

**Database schema**

```text
Create table tiny_url (
    Id bigserial primary key,
    short_url text not null, 
    original_url text not null,
    expired_datetime timestamp without timezone)
```

**Database index**

* For querying 400 million records
  * With index, around 0.3ms
  * Without indexes, about 1 minute
* Should create index for what we query

**Solution1: Short url =&gt; original url mapping**

**A single database**

* Before insert random url into database. Check whether it exists within database, if not then insert. 
* Cons:
  * Easy to timeout 
  * Heavy load on database when size is big

```text
public string longToShort(string url)
{
    while(true)
    {
        string newShortUrl = randomShorturl()
        if (!database.filter(shortUrl=newShortUrl).exists())
        {
            database.create(shortUrl=newShortUrl, longUrl=url);
            return shortUrl;
        }
    }
}
```

**Adding offline key generators**

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

**Solution 2: id =&gt; shorturl mapping**

**Initial**

* Use relational database incremental id to avoid duplication
  * Relies on a single node to generate UUID. Single point of failure and performance is low.

**Database trigger - instagram distributed unique id ???**



## SQL

**Schema design**

* Two maps
  * longURL -&gt; shortURL
  * shortURL -&gt; longURL
* In order to store less data \( Given shortURL, its corresponding sequential ID can be calculated \)
  * longURL -&gt; Sequential ID
  * Sequential ID -&gt; longURL
* Create index on longURL column, only needs to store one table
  * Sequential ID -&gt; longURL

## NoSQL
