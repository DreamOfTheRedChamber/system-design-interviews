
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

