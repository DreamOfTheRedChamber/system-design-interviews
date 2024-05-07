- [Use cases](#use-cases)
  - [Last resort](#last-resort)
  - [Triggers](#triggers)
  - [Capacity planning](#capacity-planning)

# Use cases

## Last resort

* Sharding should be used as a last resort after you exhausted the following:
  * Add cache
  * Add read-write separation
  * Consider table partition

## Triggers

* Only use in OLTP cases (OLAP is more likely to have complex changing SQL queries)
* A single table's capacity reaches 2GB. 
* A database should not contain more than 1,000 tables.
* Each individual table should not exceed 1 GB in size or 20 million rows;
* The total size of all the tables in a database should not exceed 2 GB.

## Capacity planning

* For fast growing data (e.g. order data in ecommerce website), use 2X planned capacity to avoid resharding
* For slow growing data (e.g. user identity data in ecommerce website), use 3-year estimated capacity to avoid resharding. 
