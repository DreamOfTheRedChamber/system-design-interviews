- [Target](#target)
  - [Throughput](#throughput)
  - [Bandwidth](#bandwidth)
  - [Storage space](#storage-space)

# Target
* There are 2 billion total users. DAU is 1 billion. 
* Each user will browse 10 videos per day. 

## Throughput

```
# Daily video playing number:
1 * 10^9 * 10 = 10 ^ 10

# Video play QPS 
10 ^ 10 / 86400 = 10 ^ 5

# Assume each user watches 5 minutes, simulatenously watched videos
10^5 * 5 * 60 = 3 * 10^7

# Upload QPS
# Assume each video will be played 200 times, then the upload QPS should be
10^5 / 200 = 500 
```

## Bandwidth

```
# The upload bandwidth
10^5 * 100MB * 8bit = 80 TB
```

## Storage space

```
# Suppose each video is 100 MB
# Then each second, the amount of video uploaded is 
100 MB * 500 = 50 GB 

# The space needed per year
50GB * 86400 * 365 = 50 * 10^9 * 32 * 10^6 = 1600 PB

# And each video needs 3 backups to avoid loss
1600PB * 3 = 5200PB
```