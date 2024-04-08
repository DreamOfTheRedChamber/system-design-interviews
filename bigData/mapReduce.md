* [MapReduce example](scenario_searchengine-todo.md#mapreduce-example)
  * [Interface structures](scenario_searchengine-todo.md#interface-structures)
  * [MapReduce steps](scenario_searchengine-todo.md#mapreduce-steps)
* [References](scenario_searchengine-todo.md#references)

## MapReduce example

* Problems
  * Need to replace in-memory wordCount with a disk-based hashmap
  * Need to scale reduce:
    * Need to partition the intermediate data \(wordCount\) from first phase.
    * Shuffle the partitions to the appropriate machines in second phase.  
* Partition: Partition sorted output of map phase according to hash value. Write output to local disk. 
  * Why local disk, not GFS \(final input/output all inside GFS\): 
    * GFS can be too slow. 
    * Do not require replication. Just recompute if needed. 
* External sorting: Sort each partition with external sorting.
* Send: Send sorted partitioned data to corresponding reduce machines.
* Merge sort: Merge sorted partitioned data from different machines by merge sort.

```java
// first phase
// define wordCount as Multiset;
for each document in documentSet 
{
    T = tokenize( document )
    for each token in T
    {
        wordCount[token]++;
    }
}
display( wordCount )

// second phase
// define totalWordCount as Multiset;
for each wordCount received from first phase
{
    multisetAdd( totalWordCount, wordCount )
}
```

### Interface structures

* In order for mapping, reducing, partitioning, and shuffling to seamlessly work together, we need to agree on a common structure for the data being processed. 

| Phase | Input | Output |
| :--- | :--- | :--- |
| map | &lt;K1,V1&gt; | List\(&lt;K2, V2&gt;\) |
| reduce | &lt;K2,list\(V2\)&gt; | List\(&lt;K3, V3&gt;\) |

* Examples
  * Split: The input to your application must be structured as a list of \(key/value\) pairs, list \(&lt;k1,v1&gt;\). The input format for processing multiple files is usually list \(&lt;String filename, String file\_content &gt;\). The input format for processing one large file, such as a log file, is list \(&lt;Integer line\_number, String log\_event &gt;\).
  * Map: The list of \(key/value\) pairs is broken up and each individual \(key/value\) pair, &lt;k1, v1&gt; is processed by calling the map function of the mapper. In practice, the key k1 is often ignored by the mapper. The mapper transforms each &lt; k1,v1 &gt; pair into a list of &lt; k2, v2 &gt; pairs. For word counting, the mapper takes &lt; String filename, String file\_content ;&gt; and promptly ignores filename. It can output a list of &lt; String word, Integer count &gt;. The counts will be output as a list of &lt; String word, Integer 1&gt; with repeated entries. 
  * Reduce: The output of all the mappers are aggregated into one giant list of &lt; k2, v2 &gt; pairs. All pairs sharing the same k2 are grouped together into a new \(key/value\) pair, &lt; k2, list\(v2\) &gt; The framework asks teh reducer to process each one of these aggregated \(key/value\) pairs individually. 

### MapReduce steps

1. Input: The system reads the file from GFS
2. Split: Splits up the data across different machines, such as by hash value \(SHA1, MD5\)
3. Map: Each map task works on a split of data. The mapper outputs intermediate data.
4. Transmission: The system-provided shuffle process reorganizes the data so that all {Key, Value} pairs associated with a given key go to the same machine, to be processed by Reduce.
5. Reduce: Intermediate data of the same key goes to the same reducer. 
6. Output: Reducer output is stored. 

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

## References

* [Instagram search](https://instagram-engineering.com/search-architecture-eeb34a936d3a)

