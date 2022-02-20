- [Requirements](#requirements)
- [Asynchronous design](#asynchronous-design)
- [Page view](#page-view)
- [Storm architecture](#storm-architecture)
  - [History](#history)
  - [Model](#model)

# Requirements
* Random reads—A realtime view should support fast random reads to answer
queries quickly. This means the data it contains must be indexed.
* Random writes—To support incremental algorithms, it must also be possible to
modify a realtime view with low latency.
* Scalability—As with the serving layer views, the realtime views should scale with
the amount of data they store and the read/write rates required by the application. Typically this implies that realtime views can be distributed across many
machines.
* Fault tolerance—If a disk or a machine crashes, a realtime view should continue
to function normally. Fault tolerance is accomplished by replicating data across
machines so there are backups should a single machine fail.

# Asynchronous design

![](../.gitbook/assets/lambda_speed_asynchronous.png)

# Page view 

![](../.gitbook/assets/lambda_speed_pageviews.png)

# Storm architecture
## History
* Storm is an improvements on Yahoo S4. It solved the following pain points:
  * Yahoo S4 will create a huge number of PE, consuming huge number of memory and GC cost. 
  * Yahoo S4 needs to embed data distribution logic into business logic layer. 

## Model
* Spout: Data source. 
* Tuple: The minimum unit for data transmission. A key, value pair. 
* Streams: A stream contain huge number of tuples. 
* Bolts: The place where business logic is calculated. 