- [Functional requirements](#functional-requirements)
  - [Crawler scope](#crawler-scope)
  - [Supported protocol](#supported-protocol)
  - [Ajax page crawling](#ajax-page-crawling)
  - [Auth manager](#auth-manager)
  - [Verification code](#verification-code)
  - [Anti crawler functionalities](#anti-crawler-functionalities)
- [Non-functional requirements](#non-functional-requirements)
  - [Efficiency](#efficiency)
  - [Robust](#robust)
  - [Scalability](#scalability)
  - [Performance](#performance)

# Functional requirements
## Crawler scope
* Crawl a specific website? Or entire internet for usage of a search engine

## Supported protocol
* What protocols we support: HTTP/HTTPS/FTP
* Store HTML pages only? Or need other types of media such as images and videos. Need to store historical webpages or only the latest webpages?

## Ajax page crawling
* Want to crawl dynamic pages containing Ajax pages? Or static pages will be enough?
* For content loaded by Ajax, you will not be able to see it when viewing the source page, but you will be able to see it by browser element inspector. As a result, crawler will need to have customized way to access these contents. 
  1. Manually mimic a request by copying relevant fields (cookie, user-agent, origin, etc.).
     * Error prone and requires lots of expertise
  2. Use selenium to click the load button and crawl pages

## Auth manager
* If the target website requires logging in, then customized accounts are needed for crawling. When there is a large number of content to be crawled, hundreds or thousands of accounts need to be managed because there will be rate limiting on a single account. 
* Design a cookie pool, typical problems include:
  * When to detect that the pool size is not big enough?
  * How to manage different types of cookie for different websites
  * How to know cookie gets expired？
  * Unified way for configuration

## Verification code
* Want to handle verification code?
  * Optical character recognition mechanism
  * How to handle sliding verification code

## Anti crawler functionalities
* [TODO in Chinese:如何搭建一个爬虫代理服务](https://mp.weixin.qq.com/s/Kpw8OIQ-eMexOD7_Oc9_Gw)
* [TODO in Chinese:如何构建一个通用的垂直爬虫平台](https://mp.weixin.qq.com/s/AhWYLjC4nHBpsoUDKtlk7Q)

# Non-functional requirements
## Efficiency
* Prioritization: Crawl high-importance webpages first. Given that a significant fraction of all web pages are of poor utility for serving user query needs, the crawler should be biased towards fetching “useful” pages first.
* Avoid duplication: Crawling webpages which have same or extremely similar web content.

## Robust
* Avoid deadlocks: The Web contains servers that create spider traps, which are generators of web pages that mislead crawlers into getting stuck fetching an infinite number of pages in a particular domain. Crawlers must be designed to be resilient to such traps. Not all such traps are malicious; some are the inadvertent side-effect of faulty website development.

## Scalability
* Could crawl more content by simply adding machines

## Performance

