# TopK

<!-- MarkdownTOC -->

- [Offline TopK](#offline-topk)
	- [Algorithm level](#algorithm-level)
	- [System level](#system-level)
		- [All data is kept in memory](#all-data-is-kept-in-memory)
		- [Too slow for large amounts of data](#too-slow-for-large-amounts-of-data)
			- [MapReduce](#mapreduce)
				- [Word count](#word-count)
				- [TopK](#topk)
- [Online TopK](#online-topk)
	- [Algorithm level](#algorithm-level-1)
		- [TreeMap](#treemap)
		- [HashMap + TreeMap](#hashmap--treemap)
		- [Approximate algorithms with LFU cache](#approximate-algorithms-with-lfu-cache)
	- [System level](#system-level-1)
		- [All data is kept in memory](#all-data-is-kept-in-memory-1)
		- [Too slow for large amounts of data because of locking](#too-slow-for-large-amounts-of-data-because-of-locking)
		- [Thundering herd problem](#thundering-herd-problem)
		- [Low frequency words take up so much space](#low-frequency-words-take-up-so-much-space)
		- [How to calculate topk recent X minutes](#how-to-calculate-topk-recent-x-minutes)
			- [Storage](#storage)
			- [Multi-level bucket](#multi-level-bucket)
			- [Final data structure](#final-data-structure)

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

#### MapReduce
* Divide entries by hash value (SHA1, MD5) and dispatch the workload to different machines.
* Get list of topK: {topK1, topK2, topK3, ...} from each machine
* Merge results from the returned topK list to get final TopK.

##### Word count

```java
public class WordCount 
{

    public static class Map 
    {
    	// Key is the file location
        public void map( String key, String value, OutputCollector<String, Integer> output ) 
        {
            String[] tokens = value.split(" ");

            for( String word : tokens ) 
            {
            	// the collector will batch operations writing to disk
                output.collect( word, 1 );
            }
        }
    }

    public static class Reduce 
    {
        public void reduce( String key, Iterator<Integer> values, OutputCollector<String, Integer> output )
        {
            int sum = 0;
            while ( values.hasNext() ) 
            {
                    sum += values.next();
            }
            output.collect( key, sum );
        }
    }
}

```

##### TopK

```java
class Pair
{
	String key;
	int value;

	Pair(String key, int value) {
		this.key = key;
		this.value = value;
	}
}

public class TopKFrequentWords
{

	public static class Map
	{
		public void map( String kkey, Document value, OutputCollector<String, Integer> output )
		{
			int id = value.id;
			StringBuffer temp = new StringBuffer( "" );
			String content = value.content;
			String[] words = content.split( " " );
			for ( String word : words )
			{
				if ( word.length() > 0 )
				{
					output.collect( word, 1 );
				}
			}
		}
	}

	public static class Reduce
	{
		private PriorityQueue<Pair> maxQueue = null;
		private int k;

		public void setup( int k )
		{
			// initialize your data structure here
			this.k = k;
			maxQueue = new PriorityQueue<>( k, ( o1, o2 ) -> o2.value - o1.value );
		}

		public void reduce( String key, Iterator<Integer> values )
		{
			// Write your code here
			int sum = 0;
			while ( values.hasNext() )
			{
				sum += values.next();
			}

			Pair pair = new Pair( key, sum );
			if ( maxQueue.size() < k )
			{
				maxQueue.add( pair );
			}
			else
			{
				if ( maxQueue.peek().value < pair.value )
				{
					maxQueue.poll();
					maxQueue.add( pair );
				}
			}
		}

		public void cleanup( OutputCollector<String, Integer> output )
		{

			List<Pair> pairs = new ArrayList<>();
			while ( !maxQueue.isEmpty() )
			{
				Pair qHead = maxQueue.poll();
				output.collect( qHead.key, qHead.value );				
			}
		}
	}
}```

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

### Too slow for large amounts of data because of locking
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

### How to calculate topk recent X minutes

#### Storage
* Write intensive like 20K QPS. NoSQL database suited for this purpose.
* Do not need data persistence. Use in-memory data store. 
	- Redis
	- Memcached
* Redis supports more complex data structures
	- Use a key to sorted time mapping. 
		+ The keys are 
		+ The values are sorted set. The sorted set member is the Key and score is the count. 

#### Multi-level bucket
* One bucket maps to one key inside Redis.
* How to calculate the records in last 5 minutes, 1 hour and 24 hours
	- 6 1-minute bucket
	- 13 5-min bucket
	- 25 1-hour bucket
* Retention: 
	- Every one minute, a background job will put the oldest 1-min bucket into 5-min bucket and reset the clear up the bucket. 
	- Every five minutes, a background job will put the oldest 5-min bucket into 1-hour bucket and reset the clear up the bucket. 
	- Every one hour, a background job will put the oldest 1-hour bucket into 1 hour bucket and reset the clear up the bucket. 
* How to get the latest 5 minutes: Merge the five key spaces

#### Final data structure
* Multi-level bucket structure + TreeMap

