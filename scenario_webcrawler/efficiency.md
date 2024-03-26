- [Challenges](#challenges)
- [Flowchart](#flowchart)
- [Scheduler](#scheduler)
  - [Steps](#steps)
  - [Url priority factors](#url-priority-factors)
    - [Page content quality](#page-content-quality)
    - [Frequency of change](#frequency-of-change)
    - [Politeness](#politeness)
    - [Other heuristics](#other-heuristics)
- [Connection between scheduler and downloader](#connection-between-scheduler-and-downloader)
- [Downloader](#downloader)
  - [Steps](#steps-1)
  - [Url filter](#url-filter)
    - [Robots.txt](#robotstxt)
  - [Duplication remover](#duplication-remover)
    - [Content similarity](#content-similarity)
    - [Url similarity](#url-similarity)

# Challenges
* How to avoid the same page being crawled twice? e.g. Same url, very similar content
* What if a webpage gets updated? How long does it take to be crawled again? 

# Flowchart

![Flowchart](../.gitbook/assets/urlSchedulerDownloader.png)

# Scheduler
## Steps
* A prioritizer first assigns to the URL an integer priority i between 1 and F based on priority factors.    
* Now that it has been assigned priority i, the URL is now appended to the ith of the front queues

![Crawler url frontier](../.gitbook/assets/crawler_UrlFrontier.png)

## Url priority factors
### Page content quality
* High-quality pages according to algorithms such as PageRank.

### Frequency of change
* Frequency of change: For instance, a document that has exhibited frequent change would be assigned a higher priority. 

### Politeness
* We must avoid repeated fetch requests to a host within a short time span. The likelihood of this is exacerbated because of a form of locality of reference: many URLs link to other URLs at the same host. A common heuristic is to insert a gap between successive fetch requests to a host that is an order of magnitude larger than the time taken for the most recent fetch from that host.

### Other heuristics
* application-dependent and explicit – for instance, URLs from news services may always be assigned the highest priority. 

# Connection between scheduler and downloader

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

# Downloader
* A set of downloader queues: Politeness
  * Each of the downloader queue maintains the following invariants: 
    * (i) it is non- empty while the crawl is in progress 
    * (ii) it only contains URLs from a single host
  * An auxiliary table T is used to maintain the mapping from hosts to download queues. Whenever a downloader queue is empty and is being re-filled from a front-queue, table T must be updated accordingly.

## Steps
1. A crawler thread requesting a URL from the frontier extracts the root of this heap and (if necessary) waits until the corresponding time entry te. 
2. It then takes the URL u at the head of the downloader queue j corresponding to the extracted heap root, and proceeds to fetch the URL u. 
3. After fetching u, the calling thread checks whether j is empty. 
4. If so, it picks a front queue and extracts from its head a URL v. The choice of front queue is biased (usually by a random process) towards queues of higher priority, downloader that URLs of high priority flow more quickly into the back queues. We examine v to check whether there is already a back queue holding URLs from its host. 
5. If so, v is added to that queue and we reach back to the front queues to find another candidate URL for insertion into the now-empty queue j. 
6. This process continues until j is non-empty again. In any case, the thread inserts a heap entry for j with a new earliest time te based on the properties of the URL in j that was last fetched (such as when its host was last contacted as well as the time taken for the last fetch), then continues with its processing. For instance, the new entry te could be the current time plus ten times the last fetch time.

![Crawler host to back queue mapping](../.gitbook/assets/crawler_hostToBackQueueMapping.png)

## Url filter

* A URL filter is used to determine whether the extracted URL should be excluded from the frontier based on one of several tests. For instance, the crawl may seek to exclude certain domains (say, all .com URLs) – in this case the test would simply filter out the URL if it were from the .com domain.
* The most popular example is robots.txt file. Many hosts on the Web place certain portions of their websites off-limits to crawling, under a standard known as the Robots Exclusion Protocol. This is done by placing a file with the name robots.txt at the root of the URL hierarchy at the site. Here is an example robots.txt file that specifies that no robot should visit any URL whose position in the file hierarchy starts with /yoursite/temp/, except for the robot called “searchengine”.

### Robots.txt
* Def: The Robots Exclusion Standards specifies which areas of a website should be crawled and which should not.
* Example: Wikipedia's robots.txt - [https://en.wikipedia.org/robots.txt](https://en.wikipedia.org/robots.txt) 
* sitemap.xml: 
  * Def: A webmaster specifies how often to crawl, which url to prioritize, etc. 
  * Example: [https://www.sitemaps.org/protocol.html](https://www.sitemaps.org/protocol.html)

## Duplication remover
### Content similarity
### Url similarity
* The simplest implementation for this would use a simple fingerprint such as a checksum. 
* A more sophisticated test would use shingles instead of fingerprints. (What is Shingles ???)
* Bloom filter. A Bloom filter is a probabilistic data structure and is used for answering set-existential questions (eg: has this URL been crawled before?). Due its probabilistic nature, it can give erroneous results in the form of false positives. You can however tweak the error rate, allowing for only a small number of false positives. The great benefit is the large amount of memory you can save (much more memory efficient than Redis Hashes). If we start crawling pages in the hundreds of millions, we definitely would have to switch to this data structure. As for the false positives, well, there ain’t no harm in occasionally crawling the same page twice.        

