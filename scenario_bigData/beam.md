- [History](#history)
  - [MapReduce](#mapreduce)
  - [FlumeJava and Millwheel](#flumejava-and-millwheel)
  - [Dataflow and Cloud Dataflow](#dataflow-and-cloud-dataflow)
  - [Apache Beam (Batch + Streaming)](#apache-beam-batch--streaming)
- [Example arch with Amazon best sellers](#example-arch-with-amazon-best-sellers)
  - [Using beam API](#using-beam-api)
  - [Serving strategy for best sellers](#serving-strategy-for-best-sellers)
    - [Dedicated database](#dedicated-database)
    - [Save back to original database with products](#save-back-to-original-database-with-products)
  - [Count products selling frequency](#count-products-selling-frequency)

# History
## MapReduce
* Initial effort for a fault tolerant system for large data processing such as Google URL visiting, inverted index
* Cons: 
  * All intermediate results of Map and Reduce need to be persisted on disk and are time-consuming. 
  * Whether the problem could be solved in memory in a much more efficient way. 

## FlumeJava and Millwheel
* Improvements: 
  * Abstract all data into structure such as PCollection.
  * Abstract four primitive operations:
    * parallelDo / groupByKey / combineValues and flatten
  * Uses deferred evaluation to form a DAG and optimize the planning. 
* Cons: 
  * FlumeJava only supports batch processing
  * Millwheel only supports stream processing

## Dataflow and Cloud Dataflow
* Improvements:
  * A unifid model for batch and stream processing
  * Use a set of standardized API to process data
* Cons:
  * Only run on top of Google cloud

## Apache Beam (Batch + Streaming)
* Improvements: 
  * Become a full open source platform
  * Apache beam support different runners such as Spark/Flink/etc.

# Example arch with Amazon best sellers

## Using beam API

```java
// Count frequency of selling
salesCount = salesRecords.apply(Count.perElement())

// Count the top K elements
PCollection<KV<String, Long>> topK =
      salesCount.apply(Top.of(K, new Comparator<KV<String, Long>>() {
          @Override
          public int compare(KV<String, Long> a, KV<String, Long> b) {
            return b.getValue.compareTo(a.getValue());
          }
      }));
```

## Serving strategy for best sellers
### Dedicated database
* Save topK hot selling data in a separate database. 
* Cons:
  * When serving queries, need to join with primary database table. 

### Save back to original database with products
* Have a separate column for hot selling products
* Cons:
  * Need to update large amounts of databse records after each update. 

## Count products selling frequency
