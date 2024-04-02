- [Target](#target)
  - [Throughput](#throughput)
  - [Bandwidth](#bandwidth)
  - [Storage space](#storage-space)

# Target
* There are 1 billion new Urls per month. 
* Need to retain data for 2 years.

## Throughput 

```
# write throughput
1 * 10^9 / (30 * 24 * 60 * 60) = 10 ^ 9 / 86400 = 10^4

# read throughput: Suppose read write ratio 100:1
10^4 * 100 = 10 ^ 6

* Usually peak time traffic is twice the average traffic
2 * 10^6
```

## Bandwidth

```
# Peak time traffic * space
2 * 10^6 * 1KB = 2000 Mb

``` 

## Storage space
* Tiny url encoded length? 
  * 6
* Each url record takes 1KB

```
# Total number of records:
1 * 10^9 * 12 * 2 = 24 * 10^9

# Total storage space
24 * 10^9 * 10^3 = 24 * 10^12 = 24TB
``` 
