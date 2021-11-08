
- [Storage](#storage)
  - [Single Redis instance](#single-redis-instance)
  - [Single MySQL instance](#single-mysql-instance)
  - [Wechat 2016 World Record for MySQL clusters](#wechat-2016-world-record-for-mysql-clusters)

# Storage
## Single Redis instance
* Read: 50k (20K \~ 100K)
* Write: 50K (20K \~ 100K)
* Capacity: 32 GB

## Single MySQL instance

* Single row size: 1KB
* Physical upper limit of concurrent connections: 16K
* Single table rows: 20M. Single table size: 1GB. Exceeding this number will result in fast degradation in terms of performance. 
* A single MySQL 5.6 benchmark on cloud (Aliyun). Use the following for ease of memorization:
  * TPS: 1k TPS
  * QPS: 25k QPS
  * Connection num: 10K
  * Response time: 10ms (Like a lower bound)

![](../.gitbook/assets/mysql_scalability_singleMachinePerf.png)

## Wechat 2016 World Record for MySQL clusters

* TPS (payment transaction for yearly red envelope): 200K
* RPS (number of yearly red envelope): 760K