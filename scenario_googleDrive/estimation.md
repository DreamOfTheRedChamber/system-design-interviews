- [Target](#target)
  - [Throughput](#throughput)
  - [Bandwidth](#bandwidth)
  - [Storage](#storage)

# Target
* 1 billion registered user. Each free user has 1TB storage space. 
* Daily active user represents 20% of the entire user, namely 0.2 billion.
* Each DAU uploads and downloads 4 file per day.

## Throughput
* Average and peak QPS. 

```
# Average QPS
0.2 * 10^9 * 4 files / (24 * 60 * 60) = 10^4

# Peak QPS
2 * 10^4
```

## Bandwidth
* Suppose that each file uploaded/downloaded is 1MB. 

```
# Average bandwidth
10^4 * 1MB = 10 GB/s = 80Gb/s

# Peak bandwidth
160 Gb/s
```

## Storage
* Lots of files are duplicated (Videos, books, software installation packages). And most users won't use the entire space. Instead they typically only use 10% of the entire space. 
* The total space needed is

```
1 * 10^9 * 1TB * 10% = 10^8 TB
```

