- [Naive short url web service](#naive-short-url-web-service)
    - [Database schema](#database-schema)
    - [Query](#query)
    - [Cons](#cons)
      - [Race condition](#race-condition)
- [Improved approach](#improved-approach)
  - [Flowchart](#flowchart)
  - [HDFS](#hdfs)
  - [Short url preload service](#short-url-preload-service)
    - [Steps](#steps)
    - [Linkedlists vs circular arrays](#linkedlists-vs-circular-arrays)
    - [Benefits](#benefits)

# Naive short url web service
* Offline job generates keys \(Daemon process\) and stores into database.
  * Offline job can tolerate longer query time. 

### Database schema
* create table keys\(Key text primary key, status integer\)
* status
  * 0: available
  * 1: occupied

### Query
* Keys database query 
  * 1. Select key from keys where status = 0 limit 1 
  * 2. Update keys set status = 1 where key = a\_key

### Cons
#### Race condition
* Multiple users can get same keys if qps is high
  * Select / Update is not atomic operation
* Database is not suitable for queuing
* Solution: 
  1. Database lock: Select key from keys where status = 0 limit 1 for update skip locked
  2. Distributed lock: bad performance. 
  3. Redis -&gt; LPush and LPop: Redis list data structure is actually queue and it is thread-safe and production ready

# Improved approach

## Flowchart

![Flowchart](../.gitbook/assets/tinyurl_flowchart.png)

## HDFS 
* After short url is generated, it is stored inside HDFS files consecutively. 
* 1 billion urls per month means 24 billion records in two years and in total 24 * 10 ^ 9 * 6 = 144 GB.
* It is stored as ASC code consecutively inside the file.

## Short url preload service
### Steps
1. When preload service starts, it needs to preload 10K urls from HDFS. 
2. Then it just need to read 60K bytes data from HDFS, and store the offset value of 60K. 
   * Typically this process will take 20-50ms.  
3. Next time it loads another 60K bytes data, starting from the offset value. 
4. The loaded 10K short urls will be stored as linkedlist inside Url short
5. Upon request, the url shortener service will get a new entry from the preload service. 
   * This step has lock and it could be resource exclusive.  
6. When there is fewer than 2000 new urls inside url preload service, it will load another 10K short urls from HDFS. 

### Linkedlists vs circular arrays
* Using linkedlist has the following cons:
  * Much additional GC cost
  * Additional space cost, shortUrl only takes 6 bytes. 

### Benefits
* Since opening the file and reading from offset is a mutally exclusive operation, it will prevent multiple short url services load simultaneously from HDFS. 