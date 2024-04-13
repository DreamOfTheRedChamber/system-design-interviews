- [Big key](#big-key)
  - [Scenarios](#scenarios)
  - [Diagnose](#diagnose)
  - [Solutions: Delete big keys in the background\*\*](#solutions-delete-big-keys-in-the-background)
  - [Solutions: Compression](#solutions-compression)
  - [Solutions: Split key](#solutions-split-key)
  - [TODO](#todo)

# Big key

## Scenarios

* Star's follower list
* Comments under hot topic
* Value stores too many items(e.g. redis Hash/List/Set/SortedSet)
  * The upper limit size is 2^32
  * As long as number of items inside collection >= 1 million, the latency is roughly 1s. 

## Diagnose

* Using redis as example

```
>= redis 4.0, memory usage command
< redis 4.0
    1. bgsave, redis-rdb-tool: export rdb file and analyse
    2. redis-cli --bigkeys: find big keys
    3. debug object key: look for the length of serialized key
```

## Solutions: Delete big keys in the background**

* Using redis as example

```
// Redis introduced Lazyfreeze commands "unlink"/"flushallasync"/"flushdbasync" commands to delete the item in the 
// background. When deleting an object, only logical deletion is made and then the object is thrown to the background. 

Slve-lazy-flush: Clear data options after slave receives RDB files
Lazy free-lazy-eviction: full memory ejection option
Lazy free-lazy-expire: expired key deletion option
lazyfree-lazy-server-del: Internal deletion options, such as rename oldkey new key, need to be deleted if new key exists
```

## Solutions: Compression

* When cache value is bigger than a certain size, use compression. 

## Solutions: Split key

* Under the same key, limit the size of buckets. 

## TODO
* https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=404202261&idx=1&sn=1b8254ba5013952923bdc21e0579108e&scene=21#wechat_redirect
* Extension read: Facebook lease get problem "Scaling Memcache at Facebook"
