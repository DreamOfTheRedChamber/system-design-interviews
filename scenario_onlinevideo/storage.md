- [Storage](#storage)
  - [HBase schema](#hbase-schema)
  - [Thumbnails](#thumbnails)

# Storage
## HBase schema

```
# HDFS ID => (HDFS Path, Offset, Size)
# For example
123 => (/data/videos/clust0/p0/000000001, 0,          99000000)
456 => (/data/videos/clust0/p0/000000001, 99000000,  100000000)
789 => (/data/videos/clust0/p0/000000001, 199000000, 880000000)
...

```

## Thumbnails

* SeaweedFS
* FastFS

![](../.gitbook/assets/online_video_distributedfilesystems.png)
