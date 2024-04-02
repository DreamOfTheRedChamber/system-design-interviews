- [Target](#target)
  - [Throughput estimation](#throughput-estimation)
  - [Storage estimation](#storage-estimation)
  - [Needs to be distributed?](#needs-to-be-distributed)

# Target
* There are 1 billion new pages per month. 
* Each website has 100 links.
* Need to retain data for 5 years.

## Throughput estimation

```
// How many web pages to fetch per second
10^9 website * 100 links per website / (4 weeks * 7 days * 86400 sec) 
~= 10^11 / (10 * 10^5) 
~= 10^5 webpages /sec
```

## Storage estimation
* Suppose each webpage needs to be stored for 5 years. 
* Page sizes vary a lot, but if we will be dealing with HTML text only, let’s assume an average page size of 100KB.
  * Total copy of data to store: 5 year retention
  * Needed storage = Full capacity: 0.7

```
10^9 website * 100 links per page * 100KB * 5 year retention period / capacity ratio
~= 10^9 * 10^2 * 10^5 * 5 / 0.7
~= 10^16 * 0.5
~= 5 Petabytes
```

## Needs to be distributed?
* Suppose that a single machine could use 16 threads for web crawling and crawling each web page takes 0.1s. 
* A single machine could crawl up to 160 pages per second. Then it takes 2 * 10^9 / (160 * 86400) = 154 days. 
* So the cralwer needs to be distributed.

