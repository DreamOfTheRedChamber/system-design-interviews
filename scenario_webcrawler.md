# Scenario_Crawler

* [Crawler](scenario_webcrawler.md#crawler)
  * [Requirements](scenario_webcrawler.md#requirements)
    * [Functional](scenario_webcrawler.md#functional)
    * [Non-functional](scenario_webcrawler.md#non-functional)
  * [High level design](scenario_webcrawler.md#high-level-design)
    * [Standalone crawler](scenario_webcrawler.md#standalone-crawler)
      * [Naive implementation](scenario_webcrawler.md#naive-implementation)
      * [A more extensible architecture according to scrapy](scenario_webcrawler.md#a-more-extensible-architecture-according-to-scrapy)
    * [Distributed web crawler](scenario_webcrawler.md#distributed-web-crawler)
      * [Benefits](scenario_webcrawler.md#benefits)
      * [Straightforward impl](scenario_webcrawler.md#straightforward-impl)
      * [A more extensible impl based on scrapy cluster](scenario_webcrawler.md#a-more-extensible-impl-based-on-scrapy-cluster)
  * [Detailed component design](scenario_webcrawler.md#detailed-component-design)
    * [Standalone web crawler](scenario_webcrawler.md#standalone-web-crawler)
      * [Downloader](scenario_webcrawler.md#downloader)
        * [Components](scenario_webcrawler.md#components)
        * [DNS resolver](scenario_webcrawler.md#dns-resolver)
        * [Ajax handler](scenario_webcrawler.md#ajax-handler)
        * [Auth manager](scenario_webcrawler.md#auth-manager)
        * [Verification code](scenario_webcrawler.md#verification-code)
      * [Scheduler](scenario_webcrawler.md#scheduler)
        * [Components](scenario_webcrawler.md#components-1)
        * [Size of queue](scenario_webcrawler.md#size-of-queue)
        * [Url filter](scenario_webcrawler.md#url-filter)
        * [Duplication remover](scenario_webcrawler.md#duplication-remover)
        * [Prioritization strategies](scenario_webcrawler.md#prioritization-strategies)
      * [Scheduler and downloader combined](scenario_webcrawler.md#scheduler-and-downloader-combined)
      * [Storage](scenario_webcrawler.md#storage)
        * [Component](scenario_webcrawler.md#component)
        * [DB selection](scenario_webcrawler.md#db-selection)
    * [Distributed crawler](scenario_webcrawler.md#distributed-crawler)
      * [Multi-region](scenario_webcrawler.md#multi-region)
      * [Scale by functional partitioning](scenario_webcrawler.md#scale-by-functional-partitioning)
  * [Appendix](scenario_webcrawler.md#appendix)
    * [Multithread programs for standalone crawler](scenario_webcrawler.md#multithread-programs-for-standalone-crawler)
      * [Problematic impl with lock](scenario_webcrawler.md#problematic-impl-with-lock)
      * [First workable solution with Condition](scenario_webcrawler.md#first-workable-solution-with-condition)
      * [Threadsafe queue](scenario_webcrawler.md#threadsafe-queue)
  * [Real world applications](scenario_webcrawler.md#real-world-applications)
  * [Reference](scenario_webcrawler.md#reference)

## Requirements

### Functional

* Crawl a specific website? Or entire internet for usage of a search engine
* Want to crawl dynamic pages containing Ajax pages? Or static pages will be enough?
* Want to handle verification code?
* Store HTML pages only? Or need other types of media such as images and videos. Need to store historical webpages or only the latest webpages?
* What protocols we support: HTTP/HTTPS/FTP

### Non-functional

* Efficiency
  * Prioritization: Crawl high-importance webpages first. Given that a significant fraction of all web pages are of poor utility for serving user query needs, the crawler should be biased towards fetching “useful” pages first.
  * Avoid duplication: Crawling webpages which have same or extremely similar web content.
* Availability
  * Avoid deadlocks: The Web contains servers that create spider traps, which are generators of web pages that mislead crawlers into getting stuck fetching an infinite number of pages in a particular domain. Crawlers must be designed to be resilient to such traps. Not all such traps are malicious; some are the inadvertent side-effect of faulty website development.
* Scalability: Could crawl more content by simply adding machines
* Politeness: Web servers have both implicit and explicit policies regulating the rate at which a crawler can visit them. These politeness policies must be respected.
  * robots.txt: 
    * Def: The Robots Exclusion Standards specifies which areas of a website should be crawled and which should not.
    * Example: Wikipedia's robots.txt - [https://en.wikipedia.org/robots.txt](https://en.wikipedia.org/robots.txt) 
  * sitemap.xml: 
    * Def: A webmaster specifies how often to crawl, which url to prioritize, etc. 
    * Example: [https://www.sitemaps.org/protocol.html](https://www.sitemaps.org/protocol.html)

## High level design

### Standalone crawler

#### Naive implementation

```
// pseudo code
function run
    while ( UrlPrioritizerQueue not empty )
        url = UrlPrioritizerQueue.dequeue()
        html = DownloadWorker.download(url)
        extractedNewUrls, extractedItems = urlExtractor.extract(html)
        UrlPrioritizerQueue.enqueue_all(extracted_new_urls)
    end
```

```
                                                                       ┌────────────────┐     
                                                                       │                │     
                                                                       │    Storage     │     
                                                                       │                │     
                                                                       │                │     
                                                                       └────────────────┘     
                                                                                ▲             
                                                                                │             
                                                                                │             
                                                                        Save extracted items  
                                                                                │             
                                                                                │             
┌───────────────────┐           ┌───────────────────┐                 ┌───────────────────┐   
│                   │           │                   │                 │                   │   
│  Url prioritizer  │           │ Downloader worker │                 │ Extractor worker  │   
│       queue       │──────────▶│                   │────────────────▶│                   │   
│                   │           │                   │                 │                   │   
└───────────────────┘           └───────────────────┘                 └───────────────────┘   
          ▲                                                                     │             
          │                                                                     │             
          └────────────────────────Pass extracted new urls──────────────────────┘
```

#### A more extensible architecture according to scrapy

* Scrapy: [https://docs.scrapy.org/en/latest/topics/architecture.html](https://docs.scrapy.org/en/latest/topics/architecture.html)
* Middleware:
  * Download middleware: [https://docs.scrapy.org/en/latest/topics/downloader-middleware.html#topics-downloader-middleware](https://docs.scrapy.org/en/latest/topics/downloader-middleware.html#topics-downloader-middleware)
  * Extractor middleware: [https://docs.scrapy.org/en/latest/topics/spider-middleware.html#topics-spider-middleware](https://docs.scrapy.org/en/latest/topics/spider-middleware.html#topics-spider-middleware)

```
                                                    ┌──────────────────────┐                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │      Extractor       │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    └────┬────────────▲────┘                                                                         
                                                         │            │                                                                              
                                                         │            │                                                                              
                                               Step5. Extract       Step4. Send html response to                                                     
                                                  1) items            │       extractor                                                              
                                                 2) new Urls          │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                 ┌ ─ ─ ─ ┴ ─ ─ ─ ─ ─ ─│─ ─ ─ ─                                                                       
                                                      Extractor Middleware    │                                                                      
                                                 │  (aka spider middleware)                                                                          
                                                                              │                                                                      
                                                 └ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─│─ ─ ─ ─                                                                       
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
┌──────────────────────┐                             ┌───▼────────────┴─────┐  ┌ ─ ─ ─ ─ ─                                   ┌──────────────────────┐
│                      │                             │                      │             │                                  │                      │
│                      │        Step6. Save items to │                      │  │             Step3. Download the entire      │                      │
│                      ◀───────────────storage───────┤                      ◀───          ├──────html page response──────────┤                      │
│       Storage        │                             │        Engine        │  │Downloader                                   │      Downloader      │
│                      │                             │                      │   Middleware│                                  │                      │
│                      │                             │                      │  │                                             │                      │
│                      │                             │                      ├───          ├─────Step2. Send Url to ──────────▶                      │
│                      │                             │                      │  │                    downloader               │                      │
└──────────────────────┘                             └────┬──────────▲──────┘             │                                  └──────────────────────┘
                                                          │          │         └ ─ ─ ─ ─ ─                                                           
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                               Step7. Pass new       │                                                                               
                                              Urls to scheduler     Step1. Get url from scheduler                                                    
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                      ┌───▼──────────┴──────┐                                                                        
                                                      │                     │                                                                        
                                                      │      Scheduler      │                                                                        
                                                      │                     │                                                                        
                                                      │                     │                                                                        
                                                      └─────────────────────┘
```

### Distributed web crawler

#### Benefits

* Utilize multiple machines' bandwidth to speed up crawling
* Utilize multiple machines' ip address to speed up crawling

#### Straightforward impl

* The scheduler in the standalone impl needs to be make distributed because
  * For centralize all urls
  * For dedupe purpose

```
                                                    ┌──────────────────────┐                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │      Extractor       │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    │                      │                                                                         
                                                    └────┬────────────▲────┘                                                                         
                                                         │            │                                                                              
                                                         │            │                                                                              
                                               Step5. Extract       Step4. Send html response to                                                     
                                                  1) items            │       extractor                                                              
                                                 2) new Urls          │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                 ┌ ─ ─ ─ ┴ ─ ─ ─ ─ ─ ─│─ ─ ─ ─                                                                       
                                                      Extractor Middleware    │                                                                      
                                                 │  (aka spider middleware)                                                                          
                                                                              │                                                                      
                                                 └ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─│─ ─ ─ ─                                                                       
                                                         │            │                                                                              
                                                         │            │                                                                              
                                                         │            │                                                                              
┌──────────────────────┐                             ┌───▼────────────┴─────┐  ┌ ─ ─ ─ ─ ─                                   ┌──────────────────────┐
│                      │                             │                      │             │                                  │                      │
│                      │        Step6. Save items to │                      │  │             Step3. Download the entire      │                      │
│                      ◀───────────────storage───────┤                      ◀───          ├──────html page response──────────┤                      │
│       Storage        │                             │        Engine        │  │Downloader                                   │      Downloader      │
│                      │                             │                      │   Middleware│                                  │                      │
│                      │                             │                      │  │                                             │                      │
│                      │                             │                      ├───          ├─────Step2. Send Url to ──────────▶                      │
│                      │                             │                      │  │                    downloader               │                      │
└──────────────────────┘                             └────┬──────────▲──────┘             │                                  └──────────────────────┘
                                                          │          │         └ ─ ─ ─ ─ ─                                                           
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                               Step7. Pass new       │                                                                               
                                              Urls to scheduler     Step1. Get url from scheduler                                                    
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                          │          │                                                                               
                                                      ┌───▼──────────┴──────┐                                                                        
                                                      │                     │                                                                        
                                                      │  Replace Scheduler  │                                                                        
                                                      │     with Redis      │                                                                        
                                                      │                     │                                                                        
                                                      └─────────────────────┘
```

#### A more extensible impl based on scrapy cluster

* Scrapy cluster: [https://scrapy-cluster.readthedocs.io/en/latest/topics/introduction/overview.html](https://scrapy-cluster.readthedocs.io/en/latest/topics/introduction/overview.html)

![Scrapy cluster](images/webcrawler_scrapycluster.png)

## Detailed component design

### Standalone web crawler

#### Downloader

**Components**

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
                                             Downloader                                              
│                                                                                                   │
                                                   ┌────────────────┐     ┌────────────────┐         
│    ┌────────────────┐     ┌────────────────┐     │  Verification  │     │  Ajax handler  │        │
     │  DNS Resolver  │     │  Auth manager  │     │  code handler  │     │                │         
│    └────────────────┘     └────────────────┘     └────────────────┘     └────────────────┘        │

│                                                                                                   │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
```

**DNS resolver**

* DNS resolution is a well-known bottleneck in web crawling. Due to the distributed nature of the Domain Name Service, DNS resolution may entail multiple requests and round-trips across the internet, requiring seconds and sometimes even longer. Right away, this puts in jeopardy our goal of fetching several hundred documents a second. 
  * A standard remedy is to introduce caching: URLs for which we have recently performed DNS lookups are likely to be found in the DNS cache, avoiding the need to go to the DNS servers on the internet. However, obeying politeness constraints limits the of cache hit rate.
  * [https://nlp.stanford.edu/IR-book/pdf/20crawl.pdf](https://nlp.stanford.edu/IR-book/pdf/20crawl.pdf) for more details.

**Ajax handler**

* For content loaded by Ajax, you will not be able to see it when viewing the source page, but you will be able to see it by browser element inspector. As a result, crawler will need to have customized way to access these contents. 
  1. Manually mimic a request by copying relevant fields (cookie, user-agent, origin, etc.).
     * Error prone and requires lots of expertise
  2. Use selenium to click the load button and crawl pages

**Auth manager**

* If the target website requires logging in, then customized accounts are needed for crawling. When there is a large number of content to be crawled, hundreds or thousands of accounts need to be managed because there will be rate limiting on a single account. 
* Design a cookie pool, typical problems include:
  * When to detect that the pool size is not big enough?
  * How to manage different types of cookie for different websites
  * How to know cookie gets expired？
  * Unified way for configuration

**Verification code**

* Optical character recognition mechanism
* How to handle sliding verification code

#### Scheduler

**Components**

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
                                                         Scheduler                                                         
│                                                                                                                         │

│  ┌────────────────┐   ┌────────────────┐  ┌────────────────┐   ┌────────────────┐   ┌───────────────────────────────┐   │
   │                │   │  Url filter:   │  │  Duplication   │   │   Similarity   │   │         Prioritizer:          │    
│  │Url Normalizer: │   │                │  │    remover:    │   │  calculator:   │   │                               │   │
   │                │   │    Removed     │  │                │   │                │   │    e.g. Based on timestamp    │    
│  │ Relative Urls  │   │   disallowed   │  │ Some Urls have │   │  e.g. Minhash  │   │ A heap with (lastCrawlTime +  │   │
   │      ...       │   │  domains ...   │  │  been crawled  │   │   algorithm    │   │expectedIntervalBeforeTwoCrawl)│    
│  │                │   │                │  │     before     │   │                │   │                               │   │
   └────────────────┘   └────────────────┘  └────────────────┘   └────────────────┘   └───────────────────────────────┘    
└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
```

**Size of queue**

* In standalone case, scheduler is essentially a priority queue inside memory. 
* Could use a MySQL DB task table if scheduler queue grows too big
  * state (working/idle): Whether it is being crawling.
  * priority (1/0): 
  * available time: frequency. When to fetch the next time.

| id | url                                                       | state     | priority | available_time        |
| -- | --------------------------------------------------------- | --------- | -------- | --------------------- |
| 1  | “[http://www.sina.com/”](http://www.sina.com/%E2%80%9D)   | “idle”    | 1        | “2016-03-04 11:00 am” |
| 2  | “[http://www.sina1.com/”](http://www.sina1.com/%E2%80%9D) | “working” | 1        | “2016-03-04 12:00 am” |
| 3  | “[http://www.sina2.com/”](http://www.sina2.com/%E2%80%9D) | “idle”    | 0        | “2016-03-14 02:00 pm” |
| 4  | “[http://www.sina3.com/”](http://www.sina3.com/%E2%80%9D) | “idle”    | 2        | “2016-03-12 04:25 am” |

**Url filter**

* A URL filter is used to determine whether the extracted URL should be excluded from the frontier based on one of several tests. For instance, the crawl may seek to exclude certain domains (say, all .com URLs) – in this case the test would simply filter out the URL if it were from the .com domain.
  * Many hosts on the Web place certain portions of their websites off-limits to crawling, under a standard known as the Robots Exclusion Protocol. This is done by placing a file with the name robots.txt at the root of the URL hierarchy at the site. Here is an example robots.txt file that specifies that no robot should visit any URL whose position in the file hierarchy starts with /yoursite/temp/, except for the robot called “searchengine”.

**Duplication remover**

* The simplest implementation for this would use a simple fingerprint such as a checksum. 
* A more sophisticated test would use shingles instead of fingerprints. (What is Shingles ???)
* Bloom filter. A Bloom filter is a probabilistic data structure and is used for answering set-existential questions (eg: has this URL been crawled before?). Due its probabilistic nature, it can give erroneous results in the form of false positives. You can however tweak the error rate, allowing for only a small number of false positives. The great benefit is the large amount of memory you can save (much more memory efficient than Redis Hashes). If we start crawling pages in the hundreds of millions, we definitely would have to switch to this data structure. As for the false positives, well, there ain’t no harm in occasionally crawling the same page twice.        

**Prioritization strategies**

* Use quota to balance different domains: Too many web pages in sina.com, the crawler keeps crawling sina.com and don't crawl other websites
* How to handle update for failure
  * Exponential back-off
    * Success: crawl after 1 week
    * no.1 failure: crawl after 2 weeks
    * no.2 failure: crawl after 4 weeks
    * no.3 failure: crawl after 8 weeks

#### Scheduler and downloader combined

![Crawler url frontier](.gitbook/assets/crawler_UrlFrontier.png)

* A set of scheduler queues: Prioritization
  * A prioritizer first assigns to the URL an integer priority i between 1 and F based on its fetch history (taking into account the rate at which the web page at this URL has changed between previous crawls). 
    * Frequency of change: For instance, a document that has exhibited frequent change would be assigned a higher priority. 
    * Other heuristics (application-dependent and explicit) – for instance, URLs from news services may always be assigned the highest priority. 
  * Now that it has been assigned priority i, the URL is now appended to the ith of the front queues
  * Two important considerations govern the order in which URLs are returned by the frontier. 
    * First, high-quality pages that change frequently should be prioritized for frequent crawling. Thus, the priority of a page should be a function of both its change rate and its quality (using some reasonable quality estimate). The combination is necessary because a large number of spam pages change completely on every fetch.
    * We must avoid repeated fetch requests to a host within a short time span. The likelihood of this is exacerbated because of a form of locality of reference: many URLs link to other URLs at the same host. A common heuristic is to insert a gap between successive fetch requests to a host that is an order of magnitude larger than the time taken for the most recent fetch from that host.
  * An importance score will be assigned to each URL which we discover and then crawl them accordingly. We use Redis sorted sets to store the priority associated with each URL and hashes to store the visited status of the discovered URLs. This, of course, comes with a large memory footprint.
* A set of downloader queues: Politeness
  * Each of the downloader queue maintains the following invariants: 
    * (i) it is non- empty while the crawl is in progress 
    * (ii) it only contains URLs from a single host
  * An auxiliary table T is used to maintain the mapping from hosts to download queues. Whenever a downloader queue is empty and is being re-filled from a front-queue, table T must be updated accordingly.
  * Process
    1. A crawler thread requesting a URL from the frontier extracts the root of this heap and (if necessary) waits until the corresponding time entry te. 
    2. It then takes the URL u at the head of the downloader queue j corresponding to the extracted heap root, and proceeds to fetch the URL u. 
    3. After fetching u, the calling thread checks whether j is empty. 
    4. If so, it picks a front queue and extracts from its head a URL v. The choice of front queue is biased (usually by a random process) towards queues of higher priority, downloader that URLs of high priority flow more quickly into the back queues. We examine v to check whether there is already a back queue holding URLs from its host. 
    5. If so, v is added to that queue and we reach back to the front queues to find another candidate URL for insertion into the now-empty queue j. 
    6. This process continues until j is non-empty again. In any case, the thread inserts a heap entry for j with a new earliest time te based on the properties of the URL in j that was last fetched (such as when its host was last contacted as well as the time taken for the last fetch), then continues with its processing. For instance, the new entry te could be the current time plus ten times the last fetch time.

![Crawler host to back queue mapping](images/crawler_hostToBackQueueMapping.png)

#### Storage

**Component**

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
                                               Storage                                               
│  ┌───────────────────────────────┐                                                                │
   │Webpage crawl history          │                                                                 
│  │                               │                                                                │
   │Url: string                    │                                                                 
│  │Domain: string (sharding key)  │                                                                │
   │Expected frequency: date       │                                                                 
│  │Last crawl timestamp: date     │                                                                │
   │Content signature: string      │                                                                 
│  │(calculate similarity)         │                                                                │
   │                               │                                                                 
│  │                               │                                                                │
   └───────────────────────────────┘                                                                 
│                                                                                                   │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
```

**DB selection**

* Wide-column preferred because snapshot of the same page could be stored - support 3-dimensional query

```
(row, column family, timestamp)
```

### Distributed crawler

#### Multi-region

* When Google's webpage crawls China's webpages, it will be really really slow. Deploy crawler servers in multiple regions.

#### Scale by functional partitioning

* Crawler service
* Task service
* Storage service

## Appendix

### Multithread programs for standalone crawler

* [Producer-consumer implementation in Python](http://agiliq.com/blog/2013/10/producer-consumer-problem-in-python/)
* Different coordination mechanisms in multithreads:
  * sleep: Stop a random interval and come back to see whether the resource is available to use. 
  * condition variable: As soon as the resource is released by other threads, you could get it immediately.
  * semaphore: Allowing multiple number of threads to occupy a resource simultaneously. Number of semaphore set to 1. 
* Note: More threads doesn't necessarily mean more performance. The number of threads on a single machine is limited because:
  * Context switch cost ( CPU number limitation )
  * Thread number limitation
    * TCP/IP limitation on number of threads
  * Network bottleneck for single machine

#### Problematic impl with lock

* Problems of this implementation: 
  * Consumers could not identify queue empty state and continue running. 
* Correct behavior: 
  * When there was nothing in the queue, consumer should have stopped running and waited instead of continuing consuming from the queue. 
  * And once producer adds something to the queue, there should be a way for it to notify the consumer telling it has added something to queue. 

```python
from threading import Thread, Lock
import time
import random

queue = []
lock = Lock()

# Producer keeps on adding to the queue 
class ProducerThread(Thread):
    def run(self):
        nums = range(5) #Will create the list [0, 1, 2, 3, 4]
        global queue
        while True:
            num = random.choice(nums) #Selects a random number from list [0, 1, 2, 3, 4]

            # queue is kept inside lock to avoid race condition
            lock.acquire()
            queue.append(num)
            print "Produced", num 
            lock.release()

            time.sleep(random.random())

# Consumer keeps on removing from the queue
class ConsumerThread(Thread):
    def run(self):
        global queue
        while True:

            # queue is kept inside lock to avoid race condition
            lock.acquire()
            if not queue:
                print "Nothing in queue, but consumer will try to consume"
            num = queue.pop(0)
            print "Consumed", num 
            lock.release()

            time.sleep(random.random())

# start one producer thread and one consumer thread
ProducerThread().start()
ConsumerThread().start()
```

#### First workable solution with Condition

* Use case of condition: Condition object allows one or more threads to wait until notified by another thread. 
  * Consumer should wait when the queue is empty and resume only when it gets notified by the producer. 
  * Producer should notify only after it adds something to the queue. 
* Internal mechanism of condition: Condition uses a lock internally
  * A condition has acquire() and release() methods that call the corresponding methods of the associated lock internally. 
  * Consumer needs to wait using a condition instance and producer needs to notify the consumer using the same condition instance.

```python
from threading import Condition

condition = Condition()
queue = []

class ConsumerThread(Thread):
    def run(self):
        global queue
        while True:
            condition.acquire()

            # Check if the queue is empty before consuming. If yes then call wait() on condition instance. 
            # wait() blocks the consumer and also releases the lock associated with the condition. This lock was held by consumer, so basically consumer loses hold of the lock.
            # Now unless consumer is notified, it will not run.
            if not queue:
                print "Nothing in queue, consumer is waiting"
                condition.wait()
                print "Producer added something to queue and notified the consumer"
            num = queue.pop(0)
            print "Consumed", num 
            condition.release()
            time.sleep(random.random())

class ProducerThread(Thread):
    def run(self):
        nums = range(5)
        global queue
        while True:
            # Producer can acquire the lock because lock was released by consumer
            condition.acquire()

            # Producer puts data in queue and calls notify() on the condition instance.
            num = random.choice(nums)
            queue.append(num)
            print "Produced", num 

            # Once notify() call is made on condition, consumer wakes up. But waking up doesn't mean it starts executing. notify() does not release the lock. Even after notify(), lock is still held by producer.
            condition.notify()

            # Producer explicitly releases the lock by using condition.release().
            condition.release()

            # And consumer starts running again. Now it will find data in queue and no IndexError will be raised.
            time.sleep(random.random())
```

#### Threadsafe queue

* Queue encapsulates the behaviour of Condition, wait(), notify(), acquire() etc.

```python
from threading import Thread
import time
import random
from Queue import Queue

queue = Queue(10)

class ProducerThread(Thread):
    def run(self):
        nums = range(5)
        global queue
        while True:
            num = random.choice(nums)
            # Producer uses put available on queue to insert data in the queue.
            # put() has the logic to acquire the lock before inserting data in queue.
            # Also put() checks whether the queue is full. If yes, then it calls wait() internally and so producer starts waiting.
            queue.put(num)
            print "Produced", num
            time.sleep(random.random())


class ConsumerThread(Thread):
    def run(self):
        global queue
        while True:
            # Consumer uses get.
            # get() acquires the lock before removing data from queue.
            # get() checks if the queue is empty. If yes, it puts consumer in waiting state.
            # get() and put() has proper logic for notify() too. Why don't you check the source code for Queue now?
            num = queue.get()
            queue.task_done()
            print "Consumed", num
            time.sleep(random.random())


ProducerThread().start()
ConsumerThread().start()
```

## Real world applications

* [How does Google store petabytes of data](https://www.8bitmen.com/google-database-how-do-google-services-store-petabyte-exabyte-scale-data/)
* Language comparison for crawler:
  * Java: Too heavy, not easy to refactor while crawler change might need to change regularly
  * PHP: Not good support for asynchronous, multi-threading, 
  * C/C++: High effort in development
  * Python: Winner. Rich in html parser and httprequest. Have modules such as Scrapy, Redis-Scrapy

## Reference

* [blog post](http://agiliq.com/blog/2013/10/producer-consumer-problem-in-python/)
