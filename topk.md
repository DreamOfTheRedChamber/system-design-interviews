# TopK

<!-- MarkdownTOC -->

- [TopK on single nodes](#topk-on-single-nodes)
- [TopK on multiple nodes](#topk-on-multiple-nodes)
- [Realtime topK with low frequency](#realtime-topk-with-low-frequency)
- [Realtime topK with high frequency](#realtime-topk-with-high-frequency)
- [Approximate topK](#approximate-topk)

<!-- /MarkdownTOC -->


## TopK on single nodes
* HashMap + PriorityQueue
	- Time complexity: O(n + nlgk) = O(nlgk)
	- Space complexity: O(n + k)

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