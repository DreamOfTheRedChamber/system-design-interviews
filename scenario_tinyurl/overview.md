
- [Scenario](#scenario)
  - [Main purpose](#main-purpose)
  - [Functional requirements](#functional-requirements)
    - [Optional](#optional)
  - [Non functional](#non-functional)
    - [Latency](#latency)
    - [Consistency vs Availability](#consistency-vs-availability)
    - [URL as short as possible](#url-as-short-as-possible)
  - [Estimation](#estimation)
- [Flowchart](#flowchart)
  - [Status code](#status-code)
  - [Service interface](#service-interface)
- [Service](#service)
  - [shortURL insert( longURL )](#shorturl-insert-longurl-)
  - [longURL lookup( shortURL )](#longurl-lookup-shorturl-)
- [Follow-up](#follow-up)
  - [Deal with expired records](#deal-with-expired-records)
  - [Track clicks](#track-clicks)
  - [Handle hort entries](#handle-hort-entries)


# Scenario

## Main purpose

* Data analysis like click events, user sources
* Short url length to fit social media content limit \(140 characters\)
* Avoid the website is blacklisted by domain name

## Functional requirements

* Shortening: Take a url and return a much shorter url. 
  * Ex: [http://www.interviewbit.com/courses/programming/topics/time-complexity/](http://www.interviewbit.com/courses/programming/topics/time-complexity/) =&gt; [http://goo.gl/GUKA8w/](http://goo.gl/GUKA8w/)
  * Gotcha: What if two people try to shorten the same URL?

### Optional

* Redirection: Take a short url and redirect to the original url. 
  * Ex: [http://goo.gl/GUKA8w](http://goo.gl/GUKA8w) =&gt; [http://www.interviewbit.com/courses/programming/topics/time-complexity/](http://www.interviewbit.com/courses/programming/topics/time-complexity/)
* Custom url: Allow the users to pick custom shortened url. 
  * Ex: [http://www.interviewbit.com/courses/programming/topics/time-complexity/](http://www.interviewbit.com/courses/programming/topics/time-complexity/) =&gt; [http://goo.gl/ib-time](http://goo.gl/ib-time)
* Analytics: Usage statistics for site owner. 
  * Ex: How many people clicked the shortened url in the last day? 
* Each url can have multiple tiny urls? 
  * Yes 
* Tiny url encoded length? 
  * 6
* QPS
  * 500M new records per month
  * 10:1 read write ratio
* URL is not guessable? 
  * Yes
* Needs original url validation
  * No
* Automatic link expiration
* Manual link removal
* UI vs API

## Non functional

### Latency
* Our system is similar to DNS resolution, higher latency on URL shortener is as good as a failure to resolve.

### Consistency vs Availability
* Both are extremenly important. However, CAP theorem dictates that we choose one. Do we want a system that always answers correctly but is not available sometimes? Or else, do we want a system which is always available but can sometime say that a URL does not exists even if it does? This tradeoff is a product decision around what we are trying to optimize. Let's say, we go with consistency here.

### URL as short as possible
* URL shortener by definition needs to be as short as possible. Shorter the shortened URL, better it compares to competition.

## Estimation

* QPS: 500M per month
  * 200 per second write
  * If read write ratio 10:1, then read 2000
* Performance:
  * Query with index should be around 1ms ~ 2ms
  * One write should be around 5ms for SSD disk
* Capacity:
  * 1 CPU core can handle 200 operation
  * Usually database server: 56 CPU cores -&gt; 60 CPU cores or more
  * 5-10 CPU cores should be enough without cache
  * One database should be good enough to handle the load


# Flowchart

* ShortUrl =&gt; API Gateway =&gt; TinyUrl Service =&gt; Database =&gt; TinyUrlService =&gt; API Gateway =&gt; 301 redirect =&gt; Original Url
* API Gateway: Can be REST API or GraphQL

## Status code

**Normal**

* 200: 2XX OK -&gt; Successful
* 302: temporary redirect
* 301: permanent redirect

**Error codes**

* 400: bad request
* 402 / 403: forbidden or unauthorized
* 413: payload too large
* 500-5XX: service error or internal error

## Service interface

**RPC**

**Initial design**

```text
public void generateTinyUrl(TinyUrlRequest request) throw ServiceException;

class TinyUrlRequest
{
    String originalUrl;
    Date expiredDate;
}

public string getOriginalUrl(String tinyUrl) throw ServiceException;
```

**If API needs to be provided to other vendors**

* If we need to provide service \(API\) to other vendors, we need api key and rate limiting

```text
public void generateTinyUrl(String APIKey, TinyUrlRequest request) throw ServiceException;
public string getOriginalUrl(String APIKey, String tinyUrl) throw ServiceException;
```

**Restful**

* Post /tinyurl
* Get /tinyurl?url=xxxxx
* Put data into post body

```text
data: 
{
    original_url: xxxx,
    expired_date: xxxx, 
    ip: xxxx
}
```


# Service

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

## shortURL insert\( longURL \)

**Encode**

**Long to short with Base62**

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

## longURL lookup\( shortURL \)

**Short to long with Base62**

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



# Follow-up

## Deal with expired records

* Solution1: Check data in the service level, if expired, return null
  * Pros: Simple and keep historical recordds
  * Cons: Waste disks
* Solution2: Remove expired data in the database and cache using daemon job
  * Pros: Reduce storage and save cost, improve query performance
  * Cons: 
    * Lost historical records
    * Complicated structure
    * Easy to fail

```text
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

## Track clicks

* Question: How would we store statistics such as 
  * How many times a short URL has been used
  * What were user locations

**Batch processing**

* If the Kafka producer produces a lot of events, the broker has a lot of backlog
  * Solution1: Add Kafka brokers and partition
  * Solution2: Add memory buffer in the Kafka producer and batch send to Kafka broker

**Stream processing**

* 
## Handle hort entries

* If it is part of a DB row that gets updated on each view, what will happen when a popular URL is slammed with a large number of concurrent requests

