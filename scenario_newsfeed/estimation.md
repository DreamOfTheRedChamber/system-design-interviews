- [Target](#target)
  - [Throughput](#throughput)
  - [Bandwidth](#bandwidth)
  - [Storage](#storage)

# Target
* Assuem there is 1 billion user. Then 20% DAU is 0.2 billion. 
* Assume that each DAU publishes 1 tweet per day. And each user has 500 followers. 

## Throughput

```
# Each DAU enters 10 personal pages per day and 20 tweets are displayed per time. 
0.2 billion * 10 * 20 = 4 billion

# The daily average query per second is 
4 billion / 86400 = 40,000 / s

# The daily peak query per second is
4000 * 2 = 100,000 / s
```

## Bandwidth

```
# The peak QPS is 100K. 
# The total number of tweets is 
100 K  * 20 tweets = 2 Million tweets per second

# the total bandwidth will be
(2 Million / 5 * 500 KB + 2 Million / 10 * 2MB) * 8 bit = 5 Tb/s
```

## Storage

```
# Each tweet is 140 characters. Assume using UTF8 encoding, each tweet takes in total
140 * 3 bytes = 420 bytes

# In addition to the tweet content, there is also metadata including tweet ID, user ID etc.
# Assume the metadata is 80 bytes, in total:
420 + 80 = 500 bytes

# The amount of text generated per day is:
0.5KB * 0.2 * 10^9 = 100 GB 

# Suppose there will be 1 image in every 5 tweet, and 1 video in every 10 tweet. 
# Each image is 500KB and each video is 2MB. 
# The amount of multi-media generated per day is:
0.2 * 10 ^ 9 / 5 * 500 KB + 0.2 * 10 ^ 9 / 10 * 2 MB = 60 TB
```