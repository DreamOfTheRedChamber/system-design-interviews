# TopK

<!-- MarkdownTOC -->

- [Offline TopK](#offline-topk)
	- [Algorithm level](#algorithm-level)
	- [System level](#system-level)
		- [All data is kept in memory](#all-data-is-kept-in-memory)
		- [Too slow for large amounts of data](#too-slow-for-large-amounts-of-data)
- [Online TopK](#online-topk)
	- [Algorithm level](#algorithm-level-1)
		- [TreeMap](#treemap)
		- [HashMap + TreeMap](#hashmap--treemap)
		- [Approximate algorithms with LFU cache](#approximate-algorithms-with-lfu-cache)
	- [System level](#system-level-1)
		- [All data is kept in memory](#all-data-is-kept-in-memory-1)
		- [Too slow for large amounts of data](#too-slow-for-large-amounts-of-data-1)
		- [Thundering herd problem](#thundering-herd-problem)
		- [Low frequency words take up so much space](#low-frequency-words-take-up-so-much-space)
		- [Write load too high](#write-load-too-high)
		- [How to calculate topk recent X minutes](#how-to-calculate-topk-recent-x-minutes)
			- [Bucket](#bucket)
			- [Cache](#cache)

<!-- /MarkdownTOC -->


# Offline TopK
## Algorithm level
* HashMap + PriorityQueue
* Parameters
	- n: number of records
	- m: number of distinct entries
	- K: target k
* TC: O(n + mlgk) = O(n)
	- Count frequency: O(n)
	- Calculate top K: O(mlgk)
* SC: O(n + k)
	- HashMap: O(n)
	- PriorityQueue: O(k)

## System level
### All data is kept in memory
* Potential issues
	- Out of memory because all data is kept inside memory.
	- Data loss when the node has failure and powers off.
* Solution: Replace hashmap with database
	- Store data in database
	- Update counter in database

### Too slow for large amounts of data
* Scenarios
	- Given a 10T word file, how to process (Need hash)
	- Each machine store word files, how to process (Need rehash)
* Divide entries by hash value (SHA1, MD5) and dispatch the workload to different machines.
* Get list of topK: {topK1, topK2, topK3, ...} from each machine
* Merge results from the returned topK list to get final TopK.

# Online TopK
## Algorithm level
### TreeMap
* TC: O(nlgm)
* SC: O(m)

### HashMap + TreeMap
* TC: O(nlgk)
	- Update hashMap O(n)
	- Update treeMap O(nlgk)
* SC: O(n + k)
	- HashMap: O(n)
	- TreeMap: O(k)

### Approximate algorithms with LFU cache 
* Data structure: DLL + HashMap
* Algorithm complexity: 
	- TC: O(n + k)
	- SC: O(n)

## System level
### All data is kept in memory
* Problems and solutions are same with offline

### Too slow for large amounts of data
* Distribute the input stream among multiple machines 1, ..., N
* Get a list of TopK from machines 1, ..., N
* Merge results from the returned topK list to get final TopK.

### Thundering herd problem
* Problem: What if one key is too hot, writing frequency is very heavy on one node?
* Solution: Add a cache layer to have a tradeoff between accuracy and latency. More speicifically, count how many times an item appears in a distributed way. 
	- For each slave, maintain a local counter inside memory. Every 5 seconds, these slaves report to the master node. Namely, each slave will aggregate the statistics of 5 seconds and report to master. Then the master will update the database. Although the cache layer adds a five seconds latency, it does not have any central point of failure anymore.
	- What if the master node fails?
		+ Use another machine to monitor the master, if the master dies, issue a command to restart the machine.

### Low frequency words take up so much space
* Solution: Approximate topK. Sacrifice accuracy for space
	- Flexible space
	- O(logk) time complexity
* Disadvantage:
	- All low frequency will be hashed to same value, which will result in incorrect result (low possibility)
	- Some low frequency words will come later, which will have a great count, then replace other high frequency words (bloom filter)
		+ HashMap will have 3 different hash functions
		+ Choose the lowest count from hashmap

### Write load too high
* Solution: Probabilistic logging. TopK items must occur a lot of times. 

### How to calculate topk recent X minutes
* How to calculate the records in last 5 minutes, 1 hour and 24 hours

#### Bucket
#### Cache