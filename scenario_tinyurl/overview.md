
- [Use cases](#use-cases)
- [Functional requirements](#functional-requirements)
  - [Core](#core)
  - [Optional](#optional)
  - [Non functional](#non-functional)
    - [High performance](#high-performance)
    - [High availability](#high-availability)
    - [Not predictable](#not-predictable)
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


# Use cases
* Data analysis like click events, user sources.
* Short url length to fit social media content limit \(140 characters\).
* Avoid the website is blacklisted by domain name.

# Functional requirements
## Core
* Shortening: Take a url and return a much shorter url. 
* Redirection: Take a short url and redirect to the original url. 

## Optional
* Custom url: Allow the users to pick custom shortened url. 
* Analytics: Usage statistics for site owner. 
  * Ex: How many people clicked the shortened url in the last day.
* What if two people try to shorten the same URL?
  * Each url can have multiple tiny urls 
* URL is not guessable? 
  * Yes
* Needs original url validation
  * No
* Automatic link expiration
* Manual link removal
* UI vs API

## Non functional

### High performance
* 80% latency smaller than 5ms, 99% latency smaller than 20ms, average latency smaller than 10ms.
* Our system is similar to DNS resolution, higher latency on URL shortener is similar to a failure to resolve. 

### High availability
* It should be high available. 

### Not predictable
* Short url should be not predictable to avoid hacking and leaking important information. 

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

