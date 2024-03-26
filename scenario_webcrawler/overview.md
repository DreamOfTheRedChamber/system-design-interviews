- [Estimation](#estimation)
  - [Needs to be distributed?](#needs-to-be-distributed)
  - [Storage](#storage)
  - [Throughput](#throughput)
- [Scenarios](#scenarios)
- [Functional requirements](#functional-requirements)
- [Non-functional requirements](#non-functional-requirements)
  - [Efficiency](#efficiency)
  - [Robust](#robust)
  - [Scalability](#scalability)
- [Real world applications](#real-world-applications)
  - [Python scrapy](#python-scrapy)
  - [TODO](#todo)

# Estimation
## Needs to be distributed?
* Suppose that a single machine could use 16 threads for web crawling and crawling each web page takes 0.1s. And there are 2 billion new pages per month. 
* A single machine could crawl up to 160 pages per second. Then it takes 2 * 10^9 / (160 * 86400) = 154 days. 
* So the cralwer needs to be distributed.

## Storage
* Suppose each webpage is 500KB and each webpage needs to be stored for 20 years. 
* Then it takes in total 20 * 12 * 500KB * 2*10^9 = 

## Throughput
* 2 * 10^9 / (30 * 24 * 60 * 60) = 800

# Scenarios
# Functional requirements

* Crawl a specific website? Or entire internet for usage of a search engine
* Want to crawl dynamic pages containing Ajax pages? Or static pages will be enough?
* Want to handle verification code?
* Store HTML pages only? Or need other types of media such as images and videos. Need to store historical webpages or only the latest webpages?
* What protocols we support: HTTP/HTTPS/FTP

# Non-functional requirements
## Efficiency
* Prioritization: Crawl high-importance webpages first. Given that a significant fraction of all web pages are of poor utility for serving user query needs, the crawler should be biased towards fetching “useful” pages first.
* Avoid duplication: Crawling webpages which have same or extremely similar web content.

## Robust
* Avoid deadlocks: The Web contains servers that create spider traps, which are generators of web pages that mislead crawlers into getting stuck fetching an infinite number of pages in a particular domain. Crawlers must be designed to be resilient to such traps. Not all such traps are malicious; some are the inadvertent side-effect of faulty website development.

## Scalability
* Scalability: Could crawl more content by simply adding machines


# Real world applications

## Python scrapy
* [How does Google store petabytes of data](https://www.8bitmen.com/google-database-how-do-google-services-store-petabyte-exabyte-scale-data/)
* Language comparison for crawler:
  * Java: Too heavy, not easy to refactor while crawler change might need to change regularly
  * PHP: Not good support for asynchronous, multi-threading, 
  * C/C++: High effort in development
  * Python: Winner. Rich in html parser and httprequest. Have modules such as Scrapy, Redis-Scrapy

* Scrapy cluster: [https://scrapy-cluster.readthedocs.io/en/latest/topics/introduction/overview.html](https://scrapy-cluster.readthedocs.io/en/latest/topics/introduction/overview.html)

![Scrapy cluster](../.gitbook/assets/webcrawler_scrapycluster.png)

* Scrapy: [https://docs.scrapy.org/en/latest/topics/architecture.html](https://docs.scrapy.org/en/latest/topics/architecture.html)
* Middleware:
  * Download middleware: [https://docs.scrapy.org/en/latest/topics/downloader-middleware.html#topics-downloader-middleware](https://docs.scrapy.org/en/latest/topics/downloader-middleware.html#topics-downloader-middleware)
  * Extractor middleware: [https://docs.scrapy.org/en/latest/topics/spider-middleware.html#topics-spider-middleware](https://docs.scrapy.org/en/latest/topics/spider-middleware.html#topics-spider-middleware)

## TODO
* https://leetcode.com/discuss/interview-question/124657/Design-a-distributed-web-crawler-that-will-crawl-all-the-pages-of-wikipedia/263401