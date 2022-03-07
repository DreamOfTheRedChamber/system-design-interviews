- [History](#history)
  - [MapReduce](#mapreduce)
  - [FlumeJava and Millwheel](#flumejava-and-millwheel)
  - [Dataflow and Cloud Dataflow](#dataflow-and-cloud-dataflow)
  - [Apache Beam (Batch + Streaming)](#apache-beam-batch--streaming)
- [](#)

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

# 
