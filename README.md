# README

* [System design Interview](./#system-design-interview)
  * [Goal](./#goal)
    * [Intermediate tutorial](./#intermediate-tutorial)
    * [Graph heavy tutorial](./#graph-heavy-tutorial)
  * [References](./#references)
  * [Remaining items in prioritized order](./#remaining-items-in-prioritized-order)
  * [Backlog](./#backlog)

## System design Interview

### Goal

#### In depth discussion

* There are so many great system design tutorials for beginners in the market: 
  * [donnemartin/system-design-primer](https://github.com/donnemartin/system-design-primer)
  * [checkcheckzz/system-design-interview](https://github.com/checkcheckzz/system-design-interview)
  * [puncsky/**system-design-and-architecture**](https://github.com/puncsky/system-design-and-architecture)\*\*\*\*
  * Book: [System Design Interview](https://www.amazon.com/System-Design-Interview-insiders-Second/dp/B08CMF2CQF/ref=sr_1_1?dchild=1&keywords=system+design&qid=1619578081&sr=8-1)
  * Course: [Grokking the system design interview](https://www.educative.io/courses/grokking-the-system-design-interview)
* Learning them does help in interviews because most interviews are super busy people. They typically don't have much time thinking about new interview questions. 
* Personally, I also want to use this opportunity to learn software architecture. In-depth discussions is the most effective way for me to understand and remember things. Write this tutorial to record my journey in learning software architecture. 
  * This following blogs really demonstrate what type of in-depth discussions I want to have:
    * [https://kousiknath.medium.com/](https://kousiknath.medium.com/)
    * [https://netflixtechblog.com/](https://netflixtechblog.com/)
    * 美团技术团队：[https://tech.meituan.com/](https://tech.meituan.com/)

#### Graph intensive tutorial

* I am a visual learner and graph is always easier for me to understand things with graph. I especially like tech books published by [Manning](https://www.manning.com/) because it is so good at illustrating with pictures. 
* Earlier I was using [Monodraw](https://monodraw.helftone.com/) for pictures. Recently I started using [Excalidraw](https://excalidraw.com/) because it was easier to use and maintain. 

### References

* The summary inside this repo are the result of learning from the following materials:
  * Blogs: 
    * [AWS architecture doc](https://aws.amazon.com/architecture/well-architected/?wa-lens-whitepapers.sort-by=item.additionalFields.sortDate&wa-lens-whitepapers.sort-order=desc)
    * [Company engineering blogs](https://github.com/aaronwinter/engineering-blogs)
    * [Cool wizard zines explaining basic concepts](https://wizardzines.com/)
  * Books: 
    * [DDIA](https://www.amazon.com/Designing-Data-Intensive-Applications-Reliable-Maintainable/dp/1449373321/ref=sr_1_1?crid=38CARLM3E1P07&dchild=1&keywords=designing+data-intensive+applications&qid=1619579153&sprefix=intensive+data+app%2Caps%2C208&sr=8-1)
  * Videos courses:
    * [Cloud Academy](https://cloudacademy.com/)
    * [InfoQ](https://www.infoq.com/?variant=homepage_collections)
  * In Chinese only
    * [极客时间](https://time.geekbang.org/)
    * [网易云架构师课程](https://mooc.study.163.com/smartSpec/detail/1202858603.htm)

### TODO

* There are two types of TODOs in the repo
  * The first is a topic on the left hand rail directly marked as TODO. It is a topic that I am interested in but haven't got time to learn systematically about it. 
  * The second is a TODO listed inside the bottom of a discussion topic. It typically links to a blog / resource that I want to read further but haven't got time to. 
* Open topics in prioritized order:
  1. Distributed database
  2. KV database
  3. Nginx \(3 days\)
  4. etcd \(3 days\)
  5. Kubernetes
  6. JVM
  7. Typeahead lookback
  8. Newsfeed lookback
  9. Instant messenger lookback
  10. ElasticSearch \(2 days\)
  11. Twitter search
  12. Online coordination
  13. Google drive
  14. NoSQL
  15. newSQL

### Backlog

* Database migration
  * [https://time.geekbang.org/column/article/221658](https://time.geekbang.org/column/article/221658)
  * [https://time.geekbang.org/column/article/155138](https://time.geekbang.org/column/article/155138)
* Sharding middleware diffs
* Distributed rate limiting - imooc
* API Idempotent - imooc
* Distributed file system - imooc
* Distributed transactions - imooc
* Multithreading
* Keep-alive/Web-Socket
* Web-server
  * Tomcat/Nginx/OpenResty
* Api gateway vs Reverse proxy \(Nginx\)
  * [https://time.geekbang.org/course/detail/100031401-109715?utm\_source=related\_read&utm\_medium=article&utm\_term=related\_read](https://time.geekbang.org/course/detail/100031401-109715?utm_source=related_read&utm_medium=article&utm_term=related_read)
  * [https://www.cnblogs.com/huojg-21442/p/7514848.html](https://www.cnblogs.com/huojg-21442/p/7514848.html)
  * [https://developer.aliyun.com/article/175294](https://developer.aliyun.com/article/175294)
  * [https://github.com/javagrowing/JGrowing/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%BC%80%E5%8F%91/%E6%B5%85%E6%9E%90%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E4%BA%BF%E7%BA%A7%E7%BD%91%E5%85%B3.md](https://github.com/javagrowing/JGrowing/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%BC%80%E5%8F%91/%E6%B5%85%E6%9E%90%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E4%BA%BF%E7%BA%A7%E7%BD%91%E5%85%B3.md)
  * [https://juejin.im/post/6844903989637562382](https://juejin.im/post/6844903989637562382)
  * [https://gitbook.cn/books/5bbb3d2a61d11c2d996be26b/index.html](https://gitbook.cn/books/5bbb3d2a61d11c2d996be26b/index.html)
  * [https://freecontent.manning.com/the-api-gateway-pattern/](https://freecontent.manning.com/the-api-gateway-pattern/)
  * Basic functionality for API gateway [https://time.geekbang.org/course/detail/100003901-2270](https://time.geekbang.org/course/detail/100003901-2270)
  * Zuul architecture: [https://time.geekbang.org/course/detail/100003901-2271](https://time.geekbang.org/course/detail/100003901-2271)
* MySQL
  * index and schema design
    * 分析磁盘I/O时间 [https://blog.csdn.net/mysteryhaohao/article/details/51719871](https://blog.csdn.net/mysteryhaohao/article/details/51719871)
  * Problems of mySQL: [https://time.geekbang.org/column/article/267741](https://time.geekbang.org/column/article/267741)
  * Isolation level: [https://time.geekbang.org/column/article/12288](https://time.geekbang.org/column/article/12288)
    * Database lock
    * [https://github.com/javagrowing/JGrowing/blob/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/%E6%95%B0%E6%8D%AE%E5%BA%93/mysql/%E4%B8%BA%E4%BB%80%E4%B9%88%E5%BC%80%E5%8F%91%E4%BA%BA%E5%91%98%E5%BF%85%E9%A1%BB%E8%A6%81%E4%BA%86%E8%A7%A3%E6%95%B0%E6%8D%AE%E5%BA%93%E9%94%81%EF%BC%9F.md](https://github.com/javagrowing/JGrowing/blob/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/%E6%95%B0%E6%8D%AE%E5%BA%93/mysql/%E4%B8%BA%E4%BB%80%E4%B9%88%E5%BC%80%E5%8F%91%E4%BA%BA%E5%91%98%E5%BF%85%E9%A1%BB%E8%A6%81%E4%BA%86%E8%A7%A3%E6%95%B0%E6%8D%AE%E5%BA%93%E9%94%81%EF%BC%9F.md)
* Service discovery
  * [https://time.geekbang.org/course/detail/100003901-2269](https://time.geekbang.org/course/detail/100003901-2269)
  * Netflix's microservice architecture: [https://time.geekbang.org/course/detail/100003901-2272](https://time.geekbang.org/course/detail/100003901-2272)
* Configuration center / Apollo
  * Appolo architecture: [https://time.geekbang.org/course/detail/100003901-2273](https://time.geekbang.org/course/detail/100003901-2273)
* RPC
* Multi-threaded programming
* Platform management
  * Tracing
  * Resiliency patterns
* Database middleware: 3h
  * requirement: 
  * mycat:
  * sharding jdbc: 
* Distributed database 2h: [https://course.study.163.com/480000006749023/lecture-480000036843500](https://course.study.163.com/480000006749023/lecture-480000036843500)
* [https://systeminterview.com/download.php](https://systeminterview.com/download.php)

