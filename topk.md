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
		- [Database + TreeMap](#database--treemap)
		- [Cache](#cache)
	- [Realtime topK with low frequency](#realtime-topk-with-low-frequency)
	- [Realtime topK with high frequency](#realtime-topk-with-high-frequency)
	- [Approximate topK](#approximate-topk)

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

### Database + TreeMap

### Cache

## Realtime topK with low frequency
* Approach
	- When new data comes in, write it to disk file
	- When server request for topK, run the algorithm on disk file
	- Get topK 
* Disadvantage:
	- Out of memory because data is continously increasing
	- Data losss when node failure happens or is powered off
* Use TreeMap to replace PQ
	- To support find and delete by key

## Realtime topK with high frequency
* Problem: QPS is too high. Database could not respond immediately, resulting in high latency. 
* What if one key is too hot, writing frequency is very heavy on one node?
	- Add cache to have a tradeoff between accuracy and latency
* Store all words on disk
	- Low frequency words take up so much space

## Approximate topK 
* Sacrifice accuracy for space
	- Flexible space
	- O(logk) time complexity
* Disadvantage:
	- All low frequency will be hashed to same value, which will result in incorrect result (low possibility)
	- Some low frequency words will come later, which will have a great count, then replace other high frequency words (bloom filter)
		+ HashMap will have 3 different hash functions
		+ Choose the lowest count from hashmap