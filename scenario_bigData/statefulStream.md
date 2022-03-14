- [Problem - Stateful stream processing](#problem---stateful-stream-processing)
- [Traditional approaches](#traditional-approaches)
  - [In-memory state with checkpointing](#in-memory-state-with-checkpointing)
  - [Use an external store](#use-an-external-store)
- [Samza approaches](#samza-approaches)
  - [Window aggregation](#window-aggregation)
  - [Table-table join (Data standardization)](#table-table-join-data-standardization)
    - [Overall flowchart for standardization](#overall-flowchart-for-standardization)
    - [When standardization rule change](#when-standardization-rule-change)
      - [Lambda architecture](#lambda-architecture)
      - [Kappa architecture](#kappa-architecture)
  - [Stream-table join （Enrich tracking events）](#stream-table-join-enrich-tracking-events)
    - [Enrich feature in detail: Who viewed your profile](#enrich-feature-in-detail-who-viewed-your-profile)
      - [Problem](#problem)
      - [Naive solution](#naive-solution)
        - [Cache](#cache)
        - [Stream processing impacts critical storage](#stream-processing-impacts-critical-storage)
      - [Solution: A second event type with profile edit](#solution-a-second-event-type-with-profile-edit)
        - [Embedded database - Lose data if we lose the machine](#embedded-database---lose-data-if-we-lose-the-machine)
        - [Scale by partitioning](#scale-by-partitioning)
- [Example - Streaming search](#example---streaming-search)
  - [Solution](#solution)
- [Samza usage at Uber](#samza-usage-at-uber)
- [References](#references)

# Problem - Stateful stream processing 
* By analogy to SQL, the select and where clauses of a query are usually stateless, but join, group by and aggregation functions like sum and count require state.

# Traditional approaches
## In-memory state with checkpointing
* Maintain in-memory stream and periodically save the task's entire in-memory data to durable storage. 
* Cons:Too much storage if task state keeps growing. 
  * Many non-trivival use cases for joins and aggregation have large amounts of state - often gigabytes. It makes full dumps of the state impractical. 
  * Some academic system produce diffs in addition to full checkpoints, which are smaller if only some of the state has changed since the last checkpoint. However, this optimization only helps if most of the state remains unchanged.

## Use an external store
* Store the state in an external database or key-value store. 
* Cons:
  * Performance: 
    * 
  * Isolation: 
    * 
  * Query capabilities: 
    * 
  * Correctness: 
    * 
  * Reprocessing: 
    * 

![](../.gitbook/assets/statefulstream_externalstore.png)

# Samza approaches

![](../.gitbook/assets/samza_overview.png)

## Window aggregation
* Example: Counting the number of page views for each user per hour
* The simplest implementation keeps this state in-memory (a hashmap in the task instances)
* Cons:
  * What happens when a container fails and your in-memory state is lost. You might be able to restore it by processing all the messages in the current window again, but that might take a long time if the window covers a long period of time.

## Table-table join (Data standardization)
* Job terms get standardized to basic categories.

![](../.gitbook/assets/samza_standardize_example.png)

![](../.gitbook/assets/samza_standardize_example_developer.png)

### Overall flowchart for standardization

![](../.gitbook/assets/samza_standardize_search_index.png)

### When standardization rule change
* Expect search index update happening in real time

![](../.gitbook/assets/samza_standardize_profileEdit.png)

#### Lambda architecture
* Cons: Implement the same job twice

![](../.gitbook/assets/samza_standardize_profileEdit_overview)

#### Kappa architecture
* Process realtime data and reprocess historical data in the same framework

![](../.gitbook/assets/samza_standardize_entirehistory.png)

* There could be two jobs. Each job writes result to a different location. 
  * One consuming the latest entries in the stream
  * The other one starting from the beginning

![](../.gitbook/assets/samza_kappa.png)

* The client could switch over when the new standardization rule when it is ready

![](../.gitbook/assets/samza_standardize_switch.png)

## Stream-table join （Enrich tracking events）
* Once you have the tracking events, a number of use cases are possible. 

![](../.gitbook/assets/samza_event.png)

![](../.gitbook/assets/samza_pageview_usecases.png)

* Feature: People also viewed

![](../.gitbook/assets/samza_people_also_viewed.png)

### Enrich feature in detail: Who viewed your profile

![](../.gitbook/assets/samza_who_viewed_profile.png)

![](../.gitbook/assets/samza_who_viewed_profile_statistics.png)

![](../.gitbook/assets/samza_who_viewed_profile_example.png)

#### Problem

![](../.gitbook/assets/samza_enrich_and_index.png)

#### Naive solution
* Too slow because connecting to database could be slow

![](../.gitbook/assets/samza_enrich_naive_solution.png)

##### Cache
* If expiration time is too short, not much benefit.
* If expiration time is too long, outdated profiles. 

![](../.gitbook/assets/samza_enrich_naive_solution_cache.png)

##### Stream processing impacts critical storage
* After a stream processing is shutdown for half an hour (pretty common in production) and restarted, it will hit the database really quick because it is designed to be realtime.

![](../.gitbook/assets/samza_enrich_critical_storage.png)

#### Solution: A second event type with profile edit

![](../.gitbook/assets/samza_enrich_second_event.png)

* Every time get a profile edit event, write to a profile edit database. Each time who edits an event, write to a profiles database. 

![](../.gitbook/assets/samza_enrich_pageViewEventWithProfile.png) 

* Samza has an embedded database which could hold profiles. 

![](../.gitbook/assets/samza_enrich_pageViewEventWithProfile_embedded.png)

##### Embedded database - Lose data if we lose the machine
* Solution: Reprocess the data stream from the beginning

![](../.gitbook/assets/samza_enrich_embedded_losedata.png)

* Speed up: Kafka data compaction could help speed up the process. 

![](../.gitbook/assets/samza_kafka_compaction.png)

* Samza maintains an in-memory key value store, and it has multiple incoming Kafka sources. For each Samza store, there could also be a kafka changelog stream output defined. In cases where a Samza instance is dead, its corresponding Kafka changelog could be replayed back to rebuild Samza instance. 

![](../.gitbook/assets/samza_kafka_changelog.png)

##### Scale by partitioning

![](../.gitbook/assets/samza_enrich_copartitioning.png)
# Example - Streaming search

![](../.gitbook/assets/samza_twitter_datamodel.png)

* Following the search result for some time

![](../.gitbook/assets/samza_twitter_newsearchresult.png)

* Reverse patttern of traditional search

![](../.gitbook/assets/samza_stream_search.png)

## Solution
* Mark each query with ID
  * Delete query:
  * Update query: 

![](../.gitbook/assets/samza_stream_solution.png)

# Samza usage at Uber
* https://www.youtube.com/watch?v=i4QxJIHrfOY

# References
* [Building real-time data products at LinkedIn with Apache Samza](https://www.youtube.com/watch?v=yO3SBU6vVKA&list=PLeKd45zvjcDHJxge6VtYUAbYnvd_VNQCx&index=7)
* [Scalable real-time data processing with Apache Samza](https://www.youtube.com/watch?v=uRmYJGRPfKU&list=PLeKd45zvjcDHJxge6VtYUAbYnvd_VNQCx&index=17)
* [Samza: Stateful stream processing](https://samza.apache.org/learn/documentation/0.9/container/state-management.html)