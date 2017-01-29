# Crawler

<!-- MarkdownTOC -->

- [Scenario](#scenario)
- [Initial design](#initial-design)
	- [A simplistic news crawler](#a-simplistic-news-crawler)
	- [A single threaded web crawler](#a-single-threaded-web-crawler)
	- [A multi-threaded web crawler](#a-multi-threaded-web-crawler)
	- [A distributed web crawler](#a-distributed-web-crawler)
- [Service](#service)
- [Scale](#scale)
	- [Shard task table](#shard-task-table)
	- [How to handle update for failure \(Content update, crawl failure\)](#how-to-handle-update-for-failure-content-update-crawl-failure)

<!-- /MarkdownTOC -->


## Scenario
* Given seeds, crawl the web
	- How many web pages?
		+ 1 trillion web pages
	- How long? 
		+ Crawl all of them every week
	- How large?
		+ Average size of a web page: 10k
		+ 10p web page storage

## Initial design
### A simplistic news crawler
* Given the URL of news list page
	1. Send an HTTP request and grab the content of the news list page
	2. Extract all the news titles from the news list page. (Regular expressions)

```python
import urllib2
url = 'http://tech.163.com/it'
// get html
request = urllib2.Request(url)
response = urllib2.urlopen(request)
page = response.read()

// extract info using regular expressions
```

### A single threaded web crawler
* Input: Url seeds
* Output: List of urls
* [Producer-consumer implementation in Python](http://agiliq.com/blog/2013/10/producer-consumer-problem-in-python/)

```
// breath first search, single-threaded crawler
function run
	while ( url_queue not empty )
		url = url_queue.dequeue()
		html = web_page_loader.load( url ) // consume
		url_list = url_extractor.extract( html ) // produce
		url_queue.enqueue_all( url_list )
	end
```

### A multi-threaded web crawler
* How different threads work together
	- sleep: Stop a random interval and come back to see whether the resource is available to use. 
	- condition variable: As soon as the resource is released by other threads, you could get it immediately.
	- semaphore: Allowing multiple number of threads to occupy a resource simultaneously. Number of semaphore set to 1. 
* However, more threads doesn't necessarily mean more performance. The number of threads on a single machine is limited because:
	- Context switch cost ( CPU number limitation )
	- Thread number limitation
		+ TCP/IP limitation on number of threads
	- Network bottleneck for single machine

### A distributed web crawler
* URL queue is inside memory. Queue is too big to completely fit into memory. Use a MySQL DB task table
	- state (working/idle): Whether it is being crawling.
	- priority (1/0): 
	- available time: frequency. When to fetch the next time.

| id | url                     | state     | priority | available_time        | 
|----|-------------------------|-----------|----------|-----------------------| 
| 1  | “http://www.sina.com/”  | “idle”    | 1        | “2016-03-04 11:00 am” | 
| 2  | “http://www.sina1.com/” | “working” | 1        | “2016-03-04 12:00 am” | 
| 3  | “http://www.sina2.com/” | “idle”    | 0        | “2016-03-14 02:00 pm” | 
| 4  | “http://www.sina3.com/” | “idle”    | 2        | “2016-03-12 04:25 am” | 


## Service
* Crawler service
* Task service
* Storage service

## Scale
### Shard task table
* Horizontal sharding

### How to handle update for failure (Content update, crawl failure)
* Exponential back-off
	- 
* 