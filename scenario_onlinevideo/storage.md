- [Storage](#storage)
  - [HBase](#hbase)
    - [HDFS schema](#hdfs-schema)
    - [Thumbnails](#thumbnails)

# Storage
## HBase
### HDFS schema
* Each HDFS file consists of several videos and each video consists of several blocks. 
  * Each HDFS big file takes about 55GB. 
  * Each block only has 64MB.

```
# HDFS ID => (HDFS Path, Offset, Size)
# For example
123 => (/data/videos/clust0/p0/000000001, 0,          99 000 000)
456 => (/data/videos/clust0/p0/000000001, 99000000,  100 000 000)
789 => (/data/videos/clust0/p0/000000001, 199000000, 880 000 000)
...

```

### Thumbnails
* key -> value pairs
* Thumbnails typically have a several KB size. 