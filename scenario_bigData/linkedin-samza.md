- [Samza Use cases](#samza-use-cases)
- [Samza Example - Enrich tracking events](#samza-example---enrich-tracking-events)
  - [Enrich feature in detail: Who viewed your profile](#enrich-feature-in-detail-who-viewed-your-profile)
    - [Problem](#problem)
    - [Naive solution](#naive-solution)
      - [Cache](#cache)
      - [Stream processing impacts critical storage](#stream-processing-impacts-critical-storage)
    - [Solution: A second event type with profile edit](#solution-a-second-event-type-with-profile-edit)
- [Samze example - Data standardization](#samze-example---data-standardization)
  - [When standardization rule change](#when-standardization-rule-change)
    - [Lambda architecture](#lambda-architecture)
    - [Kappa architecture](#kappa-architecture)
- [References](#references)

# Samza Use cases
* Filtering, aggregation & joining of streams

![](../.gitbook/assets/samza_overview.png)

# Samza Example - Enrich tracking events
* Once you have the tracking events, a number of use cases are possible. 

![](../.gitbook/assets/samza_event.png)

![](../.gitbook/assets/samza_pageview_usecases.png)

* Feature: People also viewed

![](../.gitbook/assets/samza_people_also_viewed.png)

## Enrich feature in detail: Who viewed your profile

![](../.gitbook/assets/samza_who_viewed_profile.png)

![](../.gitbook/assets/samza_who_viewed_profile_statistics.png)

![](../.gitbook/assets/samza_who_viewed_profile_example.png)

### Problem

![](../.gitbook/assets/samza_enrich_and_index.png)

### Naive solution
* Too slow because connecting to database could be slow

![](../.gitbook/assets/samza_enrich_naive_solution.png)

#### Cache
* If expiration time is too short, not much benefit.
* If expiration time is too long, outdated profiles. 

![](../.gitbook/assets/samza_enrich_naive_solution_cache.png)

#### Stream processing impacts critical storage
* After a stream processing is shutdown for half an hour (pretty common in production) and restarted, it will hit the database really quick because it is designed to be realtime.

![](../.gitbook/assets/samza_enrich_critical_storage.png)

### Solution: A second event type with profile edit

![](../.gitbook/assets/samza_enrich_second_event.png)

* Every time get a profile edit event, write to a profile edit database. Each time who edits an event, write to a profiles database. 

![](../.gitbook/assets/samza_enrich_pageViewEventWithProfile.png) 

* Samza has an embedded database which could hold profiles. 

![](../.gitbook/assets/samza_enrich_pageViewEventWithProfile_embedded.png)

* Scale by partitioning

![](../.gitbook/assets/samza_enrich_copartitioning.png)

# Samze example - Data standardization
* Job terms get standardized to basic categories.

![](../.gitbook/assets/samza_standardize_example.png)

![](../.gitbook/assets/samza_standardize_example_developer.png)

* The overall flowchart for standardization

![](../.gitbook/assets/samza_standardize_search_index.png)

## When standardization rule change
* Expect search index update happening in real time

![](../.gitbook/assets/samza_standardize_profileEdit.png)

### Lambda architecture
* Cons: Implement the same job twice

![](../.gitbook/assets/samza_standardize_profileEdit_overview)

### Kappa architecture
* Process realtime data and reprocess historical data in the same framework

![](../.gitbook/assets/samza_standardize_entirehistory.png)

* There could be two jobs. Each job writes result to a different location. 
  * One consuming the latest entries in the stream
  * The other one starting from the beginning

![](../.gitbook/assets/samza_kappa.png)

* The client could switch over when the new standardization rule when it is ready

![](../.gitbook/assets/samza_standardize_switch.png)

# References
* [Building real-time data products at LinkedIn with Apache Samza](https://www.youtube.com/watch?v=yO3SBU6vVKA&list=PLeKd45zvjcDHJxge6VtYUAbYnvd_VNQCx&index=7)