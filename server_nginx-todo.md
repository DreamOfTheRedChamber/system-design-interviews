# Server\_Nginx-\[TODO\]

## \[TODO:::\]

* [https://coding.imooc.com/class/405.html](https://coding.imooc.com/class/405.html)

## Web server

### Apache and Nginx

* Apache and Nginx could always be used together. 
  * NGINX provides all of the core features of a web server, without sacrificing the lightweight and high‑performance qualities that have made it successful, and can also serve as a proxy that forwards HTTP requests to upstream web servers \(such as an Apache backend\) and FastCGI, memcached, SCGI, and uWSGI servers. NGINX does not seek to implement the huge range of functionality necessary to run an application, instead relying on specialized third‑party servers such as PHP‑FPM, Node.js, and even Apache.
  * A very common use pattern is to deploy NGINX software as a proxy in front of an Apache-based web application. Can use Nginx's proxying abilities to forward requests for dynamic resources to Apache backend server. NGINX serves static resources and Apache serves dynamic content such as PHP or Perl CGI scripts. 

### Apache vs Nginx

| Category | Apache | Nginx |
| :--- | :--- | :--- |
| History | Invented around 1990s when web traffic is low and web pages are really simple. Apache's heavyweight, monolithic model has its limit. Tunning Apache to cope with real-world traffic efficiently is a complex art. | Heavy traffic and web pages. Designed for high concurrency. Provides 12 features including which make them appropriate for microservices. |
| Architecture | One process/threads per connection. Each requests to be handled as a separate child/thread. | Asynchronous event-driven model. There is a single master process with one or more worker processes. |
| Performance | To decrease page-rendering time, web browsers routinely open six or more TCP connections to a web server for each user session so that resources can download in parallel. Browsers hold these connections open for a period of time to reduce delay for future requests the user might make during the session. Each open connection exclusively reserves an httpd process, meaning that at busy times, Apache needs to create a large number of processes. Each additional process consumes an extra 4MB or 5MB of memory. Not to mention the overhead involved in creating and destroying child processes. | Can handle a huge number of concurrent requests |
| Easier development | Very easy to insert additional code at any point in Apache's web-serving logic. Developers could add code securely in the knowledge that if newly added code is blocked, ran slowly, leaked resources, or even crashed, only the worker process running the code would be affected. Processing of all other connections would continue undisturbed | Developing modules for it isn't as simple and easy as with Apache. Nginx module developers need to be very careful to create efficient and accurate code, without any resource leakage, and to interact appropriately with the complex event-driven kernel to avoid blocking operations. |



* Api gateway vs Reverse proxy \(Nginx\)
* * [https://time.geekbang.org/course/detail/100031401-109715?utm\_source=related\_read&utm\_medium=article&utm\_term=related\_read](https://time.geekbang.org/course/detail/100031401-109715?utm_source=related_read&utm_medium=article&utm_term=related_read)
  * [https://www.cnblogs.com/huojg-21442/p/7514848.html](https://www.cnblogs.com/huojg-21442/p/7514848.html)
  * [https://developer.aliyun.com/article/175294](https://developer.aliyun.com/article/175294)
  * [https://github.com/javagrowing/JGrowing/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%BC%80%E5%8F%91/%E6%B5%85%E6%9E%90%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E4%BA%BF%E7%BA%A7%E7%BD%91%E5%85%B3.md](https://github.com/javagrowing/JGrowing/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%BC%80%E5%8F%91/%E6%B5%85%E6%9E%90%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E4%BA%BF%E7%BA%A7%E7%BD%91%E5%85%B3.md)
  * [https://juejin.im/post/6844903989637562382](https://juejin.im/post/6844903989637562382)
  * [https://gitbook.cn/books/5bbb3d2a61d11c2d996be26b/index.html](https://gitbook.cn/books/5bbb3d2a61d11c2d996be26b/index.html)
  * [https://freecontent.manning.com/the-api-gateway-pattern/](https://freecontent.manning.com/the-api-gateway-pattern/)
  * Basic functionality for API gateway [https://time.geekbang.org/course/detail/100003901-2270](https://time.geekbang.org/course/detail/100003901-2270)
  * Zuul architecture: [https://time.geekbang.org/course/detail/100003901-2271](https://time.geekbang.org/course/detail/100003901-2271)
* 
