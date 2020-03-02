# TinyURL
<!-- MarkdownTOC -->

- [TinyURL](#tinyurl)
	- [Scenario](#scenario)
		- [Main purpose](#main-purpose)
		- [Requirements](#requirements)
			- [Core](#core)
			- [Optional](#optional)
		- [Design goals](#design-goals)
		- [Estimation](#estimation)
	- [Algorithm](#algorithm)
		- [UUID](#uuid)
		- [Hash - MD5 / SHA-256 / Murmur / Jenkins](#hash---md5--sha-256--murmur--jenkins)
		- [Encoding - Base10 / Base62 / Base64](#encoding---base10--base62--base64)
		- [Escape invalid characters](#escape-invalid-characters)
	- [Architecture](#architecture)
		- [Status code](#status-code)
			- [Normal](#normal)
			- [Error codes](#error-codes)
		- [Service interface](#service-interface)
			- [RPC](#rpc)
				- [Initial design](#initial-design)
				- [If API needs to be provided to other vendors](#if-api-needs-to-be-provided-to-other-vendors)
			- [Restful](#restful)
		- [Database layer](#database-layer)
			- [Database schema](#database-schema)
			- [Database index](#database-index)
			- [Solution1: Short url => original url mapping](#solution1-short-url--original-url-mapping)
				- [A single database](#a-single-database)
				- [Adding offline key generators](#adding-offline-key-generators)
			- [Solution 2: id => shorturl mapping](#solution-2-id--shorturl-mapping)
				- [Initial](#initial)
				- [Database trigger - instagram distributed unique id ???](#database-trigger---instagram-distributed-unique-id-)
	- [Service](#service)
		- [shortURL insert\( longURL \)](#shorturl-insert-longurl-)
			- [Encode](#encode)
			- [Long to short with Base62](#long-to-short-with-base62)
		- [longURL lookup\( shortURL \)](#longurl-lookup-shorturl-)
			- [Short to long with Base62](#short-to-long-with-base62)
	- [Storage](#storage)
		- [SQL](#sql)
			- [Schema design](#schema-design)
		- [NoSQL](#nosql)
	- [Scale](#scale)
		- [How to scale read](#how-to-scale-read)
			- [Cache](#cache)
			- [One master multiple slaves](#one-master-multiple-slaves)
			- [Optimize based on geographical info](#optimize-based-on-geographical-info)
		- [How to scale write](#how-to-scale-write)
			- [Sharding](#sharding)
				- [Problematic scenarios](#problematic-scenarios)
				- [How to locate physical machine](#how-to-locate-physical-machine)
				- [Sharding with multiple MySQL instances](#sharding-with-multiple-mysql-instances)
				- [How to get global unique ID?](#how-to-get-global-unique-id)
	- [Follow-up](#follow-up)
		- [Deal with expired records](#deal-with-expired-records)
		- [Track clicks](#track-clicks)
			- [Batch processing](#batch-processing)
			- [Stream processing](#stream-processing)
		- [Handle hort entries](#handle-hort-entries)

<!-- /MarkdownTOC -->

# TinyURL 
## Scenario 
### Main purpose
* Data analysis like click events, user sources
* Short url length to fit social media content limit (140 characters)
* Avoid the website is blacklisted by domain name

### Requirements
#### Core
* Shortening: Take a url and return a much shorter url. 
	- Ex: http://www.interviewbit.com/courses/programming/topics/time-complexity/ => http://goo.gl/GUKA8w/
	- Gotcha: What if two people try to shorten the same URL?

#### Optional
* Redirection: Take a short url and redirect to the original url. 
	- Ex: http://goo.gl/GUKA8w => http://www.interviewbit.com/courses/programming/topics/time-complexity/
* Custom url: Allow the users to pick custom shortened url. 
	- Ex: http://www.interviewbit.com/courses/programming/topics/time-complexity/ => http://goo.gl/ib-time
* Analytics: Usage statistics for site owner. 
	- Ex: How many people clicked the shortened url in the last day? 
* Each url can have multiple tiny urls? 
	- Yes 
* Tiny url encoded length? 
	- 6
* QPS
	- 500M new records per month
	- 10:1 read write ratio
* URL is not guessable? 
	- Yes
* Needs original url validation
	- No
* Automatic link expiration
* Manual link removal
* UI vs API

### Design goals 
* Latency
	- Our system is similar to DNS resolution, higher latency on URL shortener is as good as a failure to resolve.
* Consistency vs Availability
	-  Both are extremenly important. However, CAP theorem dictates that we choose one. Do we want a system that always answers correctly but is not available sometimes? Or else, do we want a system which is always available but can sometime say that a URL does not exists even if it does? This tradeoff is a product decision around what we are trying to optimize. Let's say, we go with consistency here.
* URL as short as possible
	- URL shortener by definition needs to be as short as possible. Shorter the shortened URL, better it compares to competition.

### Estimation 
* QPS: 500M per month
	- 200 per second write
	- If read write ratio 10:1, then read 2000
* Performance:
	- Query with index should be around 1ms ~ 2ms
	- One write should be around 5ms for SSD disk
* Capacity:
	- 1 CPU core can handle 200 operation
	- Usually database server: 56 CPU cores -> 60 CPU cores or more
	- 5-10 CPU cores should be enough without cache
	- One database should be good enough to handle the load

## Algorithm
### UUID
* Hash based on MAC address + Current datetime 
	- 36 characters: 32 alphanumeric characters and 4 hyphens
	- The granularity of current datetime: microseconds. Lots of collision
* Pros
	- Pure random
	- Not guessable and reversable
* Cons
	- Too long
	- Key duplication if only use 6 prefix characters

### Hash - MD5 / SHA-256 / Murmur / Jenkins
* Traditional Crypto hash function: MD5 and SHA-1
		+ Secure but slow
* Fast hash function: Murmur and Jenkins
		+ Performance
		+ Have 32-, 64-, and 128-bit variants available
* Pros
	- Not guessable and reverserable
* Cons
	- Too long
	- Key duplication if only use 6 prefix characters
	- Not random -> hash (current time or UUID + url)

* Pros
	- No need to write additional hash function, easy to implement
	- Are randomly distributed
	- Support URL clean


| Problem                             | Possible solution                                                                                                                                              | 
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| Not short enough (At least 4 bytes) | Only retrieve first 4-5 digit of the hash result                                                                                                               | 
| Collision cannot be avoided         | Use long_url + timestamp as hash argument, if conflict happens, try again (timestamp changes) -> multiple attempts, highly possible conflicts when data is big | 
| Slow                                |                                                                                                                                                                | 

### Encoding - Base10 / Base62 / Base64
* Retention period varies with base radix. For example, assume 500M new records per month
	- If length == 8, 62^8 ~ 200 trillion ~ 33333 years
	- If length == 7, 62^7 ~ 3 trillion ~ 600 years
	- If length == 6, 62^6 ~ 57 B ~ 10 years

| Encoding           | Base10     | Base62      | Base64 | 
|--------------------|------------|-------------|--------| 
| Year               | 36,500,000 | 36,500,000  |        |
| Usable characters  | [0-9]      | [0-9a-zA-Z] | [0-9a-zA-Z+/=]       |
| Encoding length    | 8          | 5           |        |

* Pros:
	- Shorter URL compared with hash
	- No collision
	- Simple computation
	- Easy to generate url without duplication (increment id)
	- Advantages of Base64 vs Base62

* Cons:
	- Too long, reversable if directly apply base62 to URL


### Escape invalid characters
* For example, Base64 = 

## Architecture
* ShortUrl => API Gateway => TinyUrl Service => Database => TinyUrlService => API Gateway => 301 redirect => Original Url
* API Gateway: Can be REST API or GraphQL

### Status code
#### Normal
* 200: 2XX OK -> Successful
* 302: temporary redirect
* 301: permanent redirect

#### Error codes
* 400: bad request
* 402 / 403: forbidden or unauthorized
* 413: payload too large
* 500-5XX: service error or internal error

### Service interface
#### RPC
##### Initial design

```
public void generateTinyUrl(TinyUrlRequest request) throw ServiceException;

class TinyUrlRequest
{
	String originalUrl;
	Date expiredDate;
}

public string getOriginalUrl(String tinyUrl) throw ServiceException;

```

##### If API needs to be provided to other vendors
* If we need to provide service (API) to other vendors, we need api key and rate limiting

```
public void generateTinyUrl(String APIKey, TinyUrlRequest request) throw ServiceException;
public string getOriginalUrl(String APIKey, String tinyUrl) throw ServiceException;
```

#### Restful
* Post /tinyurl
* Get /tinyurl?url=xxxxx
* Put data into post body

```
data: 
{
	original_url: xxxx,
	expired_date: xxxx, 
	ip: xxxx
}
```

### Database layer
#### Database schema

```
Create table tiny_url (
	Id bigserial primary key,
	short_url text not null, 
	original_url text not null,
	expired_datetime timestamp without timezone)
```

#### Database index
* For querying 400 million records
	- With index, around 0.3ms
	- Without indexes, about 1 minute
* Should create index for what we query

#### Solution1: Short url => original url mapping
##### A single database
* Before insert random url into database. Check whether it exists within database, if not then insert. 
* Cons:
	- Easy to timeout 
	- Heavy load on database when size is big

```
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

##### Adding offline key generators
* Offline job generates keys (Daemon process) and stores into database. 
	- Offline job can tolerate longer query time. 

* Keys database schema
	- create table keys(Key text primary key, status integer)
	- status
		+ 0: available
		+ 1: occupied

* Keys database query
	1. Select key from keys where status = 0 limit 1
	2. Update keys set status = 1 where key = a_key

* Cons: Race condition
	- Multiple users can get same keys if qps is high
		+ Select / Update is not atomic operation
	- Database is not suitable for queuing
	- Solution: 
		1. Database lock: Select key from keys where status = 0 limit 1 for update skip locked
		2. Distributed lock: bad performance. 
		3. Redis -> LPush and LPop: Redis list data structure is actually queue and it is thread-safe and production ready

#### Solution 2: id => shorturl mapping
##### Initial 
* Use relational database incremental id to avoid duplication
	+ Relies on a single node to generate UUID. Single point of failure and performance is low.

##### Database trigger - instagram distributed unique id ???


## Service 
```java
class TinyURL
{
	map<longURL, shortURL> longToShortMap;
	map<shortURL, longURL> shortToLongMap;
	
	shortURL insert( longURL )
	{
		if longToShortMap not containsKey longURL
			generate shortURL;
			put<longURL, shortURL> into longToShortMap;
			put<shortURL, longURL> into shortToLongMap;
		return longToShortMap.get(longURL);
	}

	longURL lookup( shortURL )
	{
		return shortToLongMap.get( shortURL );
	}
}
```

### shortURL insert( longURL )
#### Encode 


#### Long to short with Base62 
```java
    public String longToShort( String url ) 
    {
        if ( url2id.containsKey( url ) ) 
        {
            return "http://tiny.url/" + idToShortKey( url2id.get( url ) );
        }
        GLOBAL_ID++;
        url2id.put( url, GLOBAL_ID );
        id2url.put( GLOBAL_ID, url );
        return "http://tiny.url/" + idToShortKey( GLOBAL_ID );
    }

    private String idToShortKey( int id )
    {    	
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String short_url = "";
        while ( id > 0 ) 
        {
            short_url = chars.charAt( id % 62 ) + short_url;
            id = id / 62;
        }
        while ( short_url.length() < 6 ) 
        {
            short_url = "0" + short_url;
        }
        return short_url;
    }
```

### longURL lookup( shortURL ) 
#### Short to long with Base62
```java
    public String shortToLong( String url ) 
    {
        String short_key = getShortKey( url );
        int id = shortKeytoID( short_key );
        return id2url.get( id );
    }

    private String getShortKey( String url ) 
    {
        return url.substring( "http://tiny.url/".length() );
    }

    private int shortKeytoID( String short_key ) 
    {
        int id = 0;
        for ( int i = 0; i < short_key.length(); ++i ) 
        {
            id = id * 62 + toBase62( short_key.charAt( i ) );
        }
        return id;
    }
```

## Storage 
### SQL 
#### Schema design 
* Two maps
	- longURL -> shortURL
	- shortURL -> longURL
* In order to store less data ( Given shortURL, its corresponding sequential ID can be calculated )
	- longURL -> Sequential ID
	- Sequential ID -> longURL
* Create index on longURL column, only needs to store one table
	- Sequential ID -> longURL

### NoSQL 

## Scale 
### How to scale read
#### Cache
* Memcache or Rediss usually are cluster, and usually one operation takes 0.1ms or less.
	- 1 CPU cores => 5000-10000 requests
	- 20-40 CPU cores => 200K requests (one machine)

```
string originalUrl = getUrlFromMemcache(tinyUrl)
if (originalUrl == null)
{
	originalUrl = readFromDatabase(tinyUrl);
	putUrlIntoCache(tinyUrl, originalUrl);
}
return originalUrl;
```

#### One master multiple slaves
* Write to master, streamly replicated to slaves, usually less than one second Read from slaves.
* Pros
	- Increase availability to handle single point of failure
	- Reduce read pressure on a single node
* Cons
	- Replication lag
		+ Solution1: We can write to memcache when creating a new tiny url. Service can get tiny url from memcache instead of database. 
		+ Solution2: We can implement Raft protocol against relational database or use opensource or commercial realted database. 

#### Optimize based on geographical info
* Web server
	- Different web servers deployed in different geographical locations
	- Use DNS to parse different web servers to different geographical locations
* Database 
	- Centralized MySQL + Distributed memcached server
	- Cache server deployed in different geographical locations

### How to scale write
#### Sharding
##### Problematic scenarios
* Too many write operations
 * The limit of 62^6 is approaching

##### How to locate physical machine
* Problem: For "31bJF4", how do we find which physical machine the code locate at? 
* Our query is 
	- Select original_url from tiny_url where short_url = XXXXX;
* The simple one
	- We query all nodes. But there will be n queries
	- This will be pretty expensive, even if we fan out database calls. 
	- But the overall QPS to database will be n times QPS 
* Solution
	- Add virtual node number to prefix "31bJF4" => "131bJF4"
	- When querying, we can find virtual nodes from prefix (the tinyUrl length is 6). Then we can directly query node1.

```
// Assign virtual nodes to physical nodes
// Store within database
{
	0: "db0.tiny_url.com",
	1: "db1.tiny_url.com",
	2: "db2.tiny_url.com",
	3: "db3.tiny_url.com"
}
```

##### Sharding with multiple MySQL instances
* Vertical sharing
	- Only one table
	- Even with Custom URL, two tables in total
* Horizontal sharding: Choose sharding key?
	- Use Long Url as sharding key
		+ Short to long operation will require lots of cross-shard joins
	- Use ID as sharding key
		+ Short to long url: First convert shourt url to ID; Find database according to ID; Find long url in the corresponding database
		+ Long to short url: Broadcast to N databases to see whether the link exist before. If not, get the next ID and insert into database. 
	- Combine short Url and long Url together
		+ Hash(longUrl)%62 + shortkey
		+ Given shortURL, we can get the sharding machine by the first bit of shortened url.
		+ Given longURL, get the sharding machine according to Hash(longURL) % 62. Then take the first bit.
	- Sharding according to the geographical info. 
		+ First know which websites are more popular in which region. Put all websites popular in US in US DB.

##### How to get global unique ID?
* Zookeeper
* Use a specialized database for managing IDs

## Follow-up
### Deal with expired records
* Solution1: Check data in the service level, if expired, return null
	- Pros: Simple and keep historical recordds
	- Cons: Waste disks
* Solution2: Remove expired data in the database and cache using daemon job
	- Pros: Reduce storage and save cost, improve query performance
	- Cons: 
		+ Lost historical records
		+ Complicated structure
		+ Easy to fail

```
while(true)
{
	List<TinyUrlRecord> expiredRecords = getExpiredRecords();
	For (TinyUrlRecord r: expiredRecords)
	{
		deleteFromDb(r);
		removeFromCache(r);
	}
}
```

### Track clicks
* Question: How would we store statistics such as 
	- How many times a short URL has been used
	- What were user locations

#### Batch processing
* If the Kafka producer produces a lot of events, the broker has a lot of backlog
	- Solution1: Add Kafka brokers and partition
	- Solution2: Add memory buffer in the Kafka producer and batch send to Kafka broker

#### Stream processing
* 


### Handle hort entries
* If it is part of a DB row that gets updated on each view, what will happen when a popular URL is slammed with a large number of concurrent requests











