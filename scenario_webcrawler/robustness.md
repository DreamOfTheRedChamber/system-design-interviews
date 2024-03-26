- [Robustness](#robustness)
  - [No need to be highly available](#no-need-to-be-highly-available)
    - [Output the crawling status to disk for recovery](#output-the-crawling-status-to-disk-for-recovery)
  - [Num of machines](#num-of-machines)

# Robustness
## No need to be highly available
* The scheduler and downloader is an offline system and could be restarted if needed. 
* However, they need to recover from failure such as timeout or parsing failures. Different types of exceptions need to be taken care of. 

### Output the crawling status to disk for recovery
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



## Num of machines
* From the previous analysis, the write throughput is around 800 RPS. A single scheduler machine will be enough. 

