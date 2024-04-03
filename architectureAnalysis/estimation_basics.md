## Performance
* Query with index should be around 1ms ~ 2ms
* One write should be around 5ms for SSD disk

## CPU capability
* 1 CPU core can handle 200 operation
* Usually database server: 56 CPU cores -&gt; 60 CPU cores or more
* 5-10 CPU cores should be enough without cache
* One database should be good enough to handle the load

## Cache performance
* Memcache or Rediss usually are cluster, and usually one operation takes 0.1ms or less.
  * 1 CPU cores =&gt; 5000-10000 requests
  * 20-40 CPU cores =&gt; 200K requests \(one machine\)

## QPS and concurrency
* Defined inside Little's law
* For example, if a server has a QPS of 20K and 10ms average response time. 
* Then the concurrency number will be 200. 