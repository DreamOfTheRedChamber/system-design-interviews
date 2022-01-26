- [Metrics](#metrics)
  - [Properties](#properties)
  - [Use cases](#use-cases)

# Metrics
* Use case: Time series data such as counters aggregation, latency measurement
* Storage by InfluxDB and display by Grafana 

## Properties

* Metrics are numeric measurements. Metrics can include:
  * A numeric status at a moment in time (like CPU % used)
  * Aggregated measurements (like a count of events over a one-minute time, or a rate of events-per-minute)
* The types of metric aggregation are diverse (for example, average, total, minimum, maximum, sum-of-squares), but all metrics generally share the following traits:
  * A name
  * A timestamp
  * One or more numeric values

![](../.gitbook/assets/MicroSvcs-observability-metrics.jpeg)

## Use cases

* Metrics work well for large bodies of data or data collected at regular intervals when you know what you want to ask ahead of time, but they are less granular than event data.

