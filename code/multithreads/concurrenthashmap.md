# ConcurrentHashmap

* [ConcurrentHashmap](concurrenthashmap.md#concurrenthashmap)
  * [Cache with read write lock](concurrenthashmap.md#cache-with-read-write-lock)

## ConcurrentHashmap

### Cache with read write lock

```java
class Cache<K,V> 
{
  final Map<K, V> m = new HashMap<>();
  final ReadWriteLock rwl = new ReentrantReadWriteLock();
  final Lock r = rwl.readLock();
  final Lock w = rwl.writeLock();

  V get(K key) 
  {
    V v = null;
    // read cache
    r.lock();      //   ①
    try 
    {
      v = m.get(key); // ②
    }
    finally
    {
      r.unlock();//     ③
    }

    // if cache hits, then return
    if(v != null) 
    {  // ④
      return v;
    }  
    // Read database if not inside cache
    w.lock(); // ⑤
    try 
    {
      // validate again
      // other threads might have already checked database
      v = m.get(key); // ⑥
      if(v == null)
      { // ⑦
        // Although in step ④, already checked it once before
        // Here needs to check again because read lock is shared, 
        // other threads might have already been waiting at ⑤
        // And will continue execution from there. 

        // check database
        v=... // operations to read database
        m.put(key, v);
      }
    } 
    finally
    {
      w.unlock();
    }
    return v; 
  }
}
```

