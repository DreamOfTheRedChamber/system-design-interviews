# Roadmap

## August
### 01-02
* ~~CDN~~
* ~~Load balancing~~
	- Detect failure

## Planned in August
### 03-09
* DNS
* Cookie
* ~~SSL/TLS~~
* Keep-alive/Web-Socket
* Web-server
	- Tomcat/Nginx/OpenResty
* ~~HTTP protocol~~
	- long connection
* Api gateway vs Reverse proxy (Nginx)
	- https://time.geekbang.org/course/detail/100031401-109715?utm_source=related_read&utm_medium=article&utm_term=related_read
	- https://www.cnblogs.com/huojg-21442/p/7514848.html
	- https://developer.aliyun.com/article/175294
	- https://github.com/javagrowing/JGrowing/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%BC%80%E5%8F%91/%E6%B5%85%E6%9E%90%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E4%BA%BF%E7%BA%A7%E7%BD%91%E5%85%B3.md
	- https://juejin.im/post/6844903989637562382
	- https://gitbook.cn/books/5bbb3d2a61d11c2d996be26b/index.html
	- https://freecontent.manning.com/the-api-gateway-pattern/
	- 
	- Basic functionality for API gateway https://time.geekbang.org/course/detail/100003901-2270
	- Zuul architecture: https://time.geekbang.org/course/detail/100003901-2271
* Multi DC

### 10-16
* ~~Cache~~
	* Cache real life applications:
		- seckill
		- social
		- feed
* MySQL
	* index and schema design
	* ~~high availability~~
	* read-write separation
	* ~~sharding~~
	* Database lock
		- https://github.com/javagrowing/JGrowing/blob/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/%E6%95%B0%E6%8D%AE%E5%BA%93/mysql/%E4%B8%BA%E4%BB%80%E4%B9%88%E5%BC%80%E5%8F%91%E4%BA%BA%E5%91%98%E5%BF%85%E9%A1%BB%E8%A6%81%E4%BA%86%E8%A7%A3%E6%95%B0%E6%8D%AE%E5%BA%93%E9%94%81%EF%BC%9F.md
* Storage
	* ElasticSearch
	* NoSQL
		- ~~key value store~~
	* NewSQL
	* File based storage
* ~~Unique id generator~~

### 17-23
* ~~Distributed protocols~~
* Service discovery
	- https://time.geekbang.org/course/detail/100003901-2269
	- Netflix's microservice architecture: https://time.geekbang.org/course/detail/100003901-2272
* Configuration center / Apollo
	- Appolo architecture: https://time.geekbang.org/course/detail/100003901-2273
* RPC
* Monitoring
	- What are the dimensions of monitoring https://time.geekbang.org/course/detail/100003901-2276
	- Calling chain monitoring https://time.geekbang.org/course/detail/100003901-2277
* Resiliency platform
	- Circuit breaker / rate limiting
	- 

### 24-30
* Multi-threaded programming
* Message queue
	- 
* Counter service design
* Platform management
	* Tracing
	* Resiliency patterns
* Recode IM message system
	* 

### Security:
* Auth history: https://time.geekbang.org/course/detail/100031401-111473
	- Cookie / Sticky session / auth service + token
	- JWT based security
	- Staffjoy JWT cookie
* OpenID Connect: Authentication layer
	- id_token: 
* OAuth2: Authorization layer
	- Simlplest guide: https://medium.com/@darutk/the-simplest-guide-to-oauth-2-0-8c71bd9a15bb
	- Definition: https://time.geekbang.org/course/detail/100007001-6936
	- Typical modes: https://time.geekbang.org/course/detail/100007001-6937
		- Ruanyifeng overview: http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html
		- Authorization code based approach
		- implicit approach： 
		- resource owner credentials approach:
		- client credentials approach:
	- How to refresh token: 
	- Categorize OAuth2 mode
		- based on channel / client type / 
* Type of access token:
	- By reference token
	- By value token: JWT token
* Next generation of security architecture:
	- Typical architecture: https://time.geekbang.org/course/detail/100007001-7511
		+ Approach 1:
		+ Approach 2: 
		+ Approach 3: 	