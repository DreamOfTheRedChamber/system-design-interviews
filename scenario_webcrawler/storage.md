- [Storage](#storage)
  - [Scheduler output to downloader input DB schema](#scheduler-output-to-downloader-input-db-schema)
  - [Crawled webpage schema](#crawled-webpage-schema)
    - [DB selection](#db-selection)

# Storage
## Scheduler output to downloader input DB schema
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


## Crawled webpage schema

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

### DB selection

* Wide-column preferred because snapshot of the same page could be stored - support 3-dimensional query

```
(row, column family, timestamp)
```
