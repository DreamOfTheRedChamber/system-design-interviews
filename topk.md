# TopK

<!-- MarkdownTOC -->

- [TopK on a single node](#topk-on-a-single-node)
	- [Parameters](#parameters)
	- [Batch mode](#batch-mode)
		- [HashMap + PriorityQueue](#hashmap--priorityqueue)
	- [Streaming mode](#streaming-mode)
		- [Accurate algorithms](#accurate-algorithms)
			- [TreeMap](#treemap)
			- [HashMap + TreeMap](#hashmap--treemap)
		- [Approximate algorithms](#approximate-algorithms)
			- [LFU cache \(DLL + HashMap\)](#lfu-cache-dll--hashmap)
- [TopK on multiple nodes](#topk-on-multiple-nodes)
- [Realtime topK with low frequency](#realtime-topk-with-low-frequency)
- [Realtime topK with high frequency](#realtime-topk-with-high-frequency)
- [Approximate topK](#approximate-topk)

<!-- /MarkdownTOC -->


## TopK on a single node
### Parameters
* n: number of records
* m: number of distinct entries
* K: target k

### Batch mode
#### HashMap + PriorityQueue
* TC: O(n + mlgk) = O(n)
	- Count frequency: O(n)
	- Calculate top K: O(mlgk)
* SC: O(n + k)
	- HashMap: O(n)
	- PriorityQueue: O(k)

### Streaming mode
#### Accurate algorithms
##### TreeMap
* TC: O(nlgm)
* SC: O(m)

##### HashMap + TreeMap
* TC: O(nlgk)
	- Update hashMap O(n)
	- Update treeMap O(nlgk)
* SC: O(n + k)
	- HashMap: O(n)
	- TreeMap: O(k)

#### Approximate algorithms
##### LFU cache (DLL + HashMap)
* TC: O(n + k)
* SC: O(n)

## TopK on multiple nodes
* Divide by hash value (SHA1, MD5)
* Get list of topK: {topK1, topK2, topK3, ...}
* Get final topK

## Realtime topK with low frequency
* Approach
	- When new data comes in, write it to disk file
	- When server request for topK, run the algorithm on disk file
	- Get topK 
* Disadvantage:
	- Out of memory because data is continously increasing
	- Data losss when node failure happens or is powered off
* Solution: Replace hashmap with database
	- Store data in database
	- Update counter in database
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