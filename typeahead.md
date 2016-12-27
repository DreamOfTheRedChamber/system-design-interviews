# Typeahead

<!-- MarkdownTOC -->

- [Scenario](#scenario)
- [Services](#services)
- [Storage](#storage)
	- [Initial solution](#initial-solution)
	- [A better way: Trie](#a-better-way-trie)
	- [Final storage scheme](#final-storage-scheme)
- [Scale](#scale)
	- [How to reduce response time](#how-to-reduce-response-time)
	- [What if the trie too large for one machine](#what-if-the-trie-too-large-for-one-machine)
	- [How to reduce the size of log file](#how-to-reduce-the-size-of-log-file)

<!-- /MarkdownTOC -->


## Scenario
* Google suggestion

## Services
* Query service
* Data collection service

## Storage
### Initial solution
* Schema
	- Keyword: String
	- hitCount: integer
* How to query on the db
	- Query SQL: Select * from hit_stats where keyword like ${key}% order by hitCount DESC Limit 10
* Problem: like operation is really expensive
* Solution: Add cache

### A better way: Trie
* Store search count at node, but it's slow
* Instead, we can store the top n hot key words, search becomes O(len)
* How do we add a new record {abd: 3b} to the trie

### Final storage scheme
* QueryService: in-memory trie along with disk serialization
* DataCollectionService: BigTable


## Scale
### How to reduce response time
* Cache result
* Pre-fetch

### What if the trie too large for one machine
* Use consistent hashing to decide which machine a particular string belongs to. 

### How to reduce the size of log file
* Probablistic logging. Log with 1/10,000 probability