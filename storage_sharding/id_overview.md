- [Requirements on unique IDs](#requirements-on-unique-ids)
- [Requirements on unique IDs generators](#requirements-on-unique-ids-generators)
- [References](#references)
  - [Wechat seqsvr](#wechat-seqsvr)
- [ID in messaging systems](#id-in-messaging-systems)

# Requirements on unique IDs
* Uniqueness: ID is the unique identifier
* Monotonically increasing: 
  * The IDs inserted later will have a bigger value than one inserted earlier. When used as primary key in indexes, the monotonically increasing primary key will make sure that data are written sequentially, not randomly on disk.
  * For range-based query, it will also be efficient when close IDs are nearby.   
* Security: ID is not guessable otherwise attackers could easily crack it. 
* Space efficient: Not too long because it will be used in many cases. 

# Requirements on unique IDs generators
* Low latency 
* High availability 99.999%
* High QPS

# References
## Wechat seqsvr

* [https://www.infoq.cn/article/wechat-serial-number-generator-architecture/](https://www.infoq.cn/article/wechat-serial-number-generator-architecture/)

# ID in messaging systems
* http://www.52im.net/thread-1998-1-1.html