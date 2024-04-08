

# Motivation: Use Nginx as an example

* Nginx is a reverse proxy. It knows the application servers' address because it is inside configuration file. This approach has some limitations:
  * When application servers scale up, needs to update Nginx's config file and restart.
  * When application servers have problems, also need to update Nginx's config file and restart.

# References

* Three ways for service discovery: [https://time.geekbang.org/course/detail/100003901-2269](https://time.geekbang.org/course/detail/100003901-2269)
* Discovery and internals:
  * Theory: [https://time.geekbang.org/column/article/14603](https://time.geekbang.org/column/article/14603)
  * Practical: [https://time.geekbang.org/column/article/39783](https://time.geekbang.org/column/article/39783)
* TODO: Registry center:
  * [https://time.geekbang.org/column/article/39792](https://time.geekbang.org/column/article/39792)
* TODO: Select among registry centers
  * [https://time.geekbang.org/column/article/39797](https://time.geekbang.org/column/article/39797)
* TODO: Whether a node is alive:
  * [https://time.geekbang.org/column/article/40684](https://time.geekbang.org/column/article/40684)
* Notification storm problem 高并发设计40问
