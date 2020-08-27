<!-- MarkdownTOC -->

- [Http Protocol](#http-protocol)
	- [TCP vs UDP](#tcp-vs-udp)
	- [Cache control](#cache-control)
		- [Expire headers](#expire-headers)
		- [Cache control headers](#cache-control-headers)
			- [Categorize by functionality](#categorize-by-functionality)
				- [Whether a response is cacheable](#whether-a-response-is-cacheable)
				- [Set the expiration time of the header](#set-the-expiration-time-of-the-header)
				- [When a response must be revalidated](#when-a-response-must-be-revalidated)
			- [Categorize by where the header appear](#categorize-by-where-the-header-appear)
				- [Request headers:](#request-headers)
				- [Response headers](#response-headers)
				- [Extension headers](#extension-headers)
		- [Flow chart](#flow-chart)
	- [Conditional Get](#conditional-get)
		- [Validators](#validators)
		- [Conditional get headers](#conditional-get-headers)
		- [Flow chart](#flow-chart-1)
	- [Proxy related headers](#proxy-related-headers)
	- [HTTP session](#http-session)
		- [Stateless applications](#stateless-applications)
		- [Structure of a session](#structure-of-a-session)
		- [Server-side session vs client-side cookie](#server-side-session-vs-client-side-cookie)
			- [Store session state in client-side cookies](#store-session-state-in-client-side-cookies)
				- [Cookie Def](#cookie-def)
				- [Cookie typical workflow](#cookie-typical-workflow)
				- [Cookie Pros and cons](#cookie-pros-and-cons)
			- [Store session state in server-side](#store-session-state-in-server-side)
				- [Typical server-side session workflow](#typical-server-side-session-workflow)
				- [Use a load balancer that supports sticky sessions:](#use-a-load-balancer-that-supports-sticky-sessions)
	- [Long connection](#long-connection)
	- [Security](#security)
		- [SSL](#ssl)
			- [Definition](#definition)
			- [How does HTTPS work](#how-does-https-work)
			- [How to avoid public key being modified?](#how-to-avoid-public-key-being-modified)
			- [How to avoid computation consumption from PKI](#how-to-avoid-computation-consumption-from-pki)
	- [Web server](#web-server)
		- [Apache and Nginx](#apache-and-nginx)
		- [Apache vs Nginx](#apache-vs-nginx)

<!-- /MarkdownTOC -->


# Http Protocol
## TCP vs UDP

| TCP                                                                                                                                                                                                                                        | UDP                                                                                                                                                                                                                              | 
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| Reliable: TCP is connection-oriented protocol. When a file or message send it will get delivered unless connections fails. If connection lost, the server will request the lost part. There is no corruption while transferring a message. | Not Reliable: UDP is connectionless protocol. When you a send a data or message, you don’t know if it’ll get there, it could get lost on the way. There may be corruption while transferring a message.                          | 
| Ordered: If you send two messages along a connection, one after the other, you know the first message will get there first. You don’t have to worry about data arriving in the wrong order.                                                | Not Ordered: If you send two messages out, you don’t know what order they’ll arrive in i.e. no ordered                                                                                                                           | 
| Heavyweight: – when the low level parts of the TCP “stream” arrive in the wrong order, resend requests have to be sent, and all the out of sequence parts have to be put back together, so requires a bit of work to piece together.       | Lightweight: No ordering of messages, no tracking connections, etc. It’s just fire and forget! This means it’s a lot quicker, and the network card / OS have to do very little work to translate the data back from the packets. | 
| Streaming: Data is read as a “stream,” with nothing distinguishing where one packet ends and another begins. There may be multiple packets per read call.                                                                                  | Datagrams: Packets are sent individually and are guaranteed to be whole if they arrive. One packet per one read call.                                                                                                            | 
| Examples: World Wide Web (Apache TCP port 80), e-mail (SMTP TCP port 25 Postfix MTA), File Transfer Protocol (FTP port 21) and Secure Shell (OpenSSH port 22) etc.                                                                         | Examples: Domain Name System (DNS UDP port 53), streaming media applications such as IPTV or movies, Voice over IP (VoIP), Trivial File Transfer Protocol (TFTP) and online multiplayer games                                    | 

## Cache control

### Expire headers
* The Expires header tells the cache whether they should serve a request using a cached copy of the response, or whether they should retrieve an updated response from the origin server. This header is specified as an absolute expiry time for a cached representation. Beyond that time, the cached representation is considered stale and must be revalidated by the origin server.

### Cache control headers
The Cache-Control header determines whether a response is cacheable, by whom, and for how long. 

#### Categorize by functionality
##### Whether a response is cacheable
* public: indicates that the response may be cached, even if it would normally be non-cacheable.
* private: indicates that the response may be cached by local (typically browser) caches only.
* no-cache: indicates that caches must revalidate responses with the origin server on every request.
* no-store: indicates that content is uncacheable by all caches.

##### Set the expiration time of the header
* max-age: gives a time to live of the resource in seconds, after which any local and shared caches must revalidate the response.
* s-maxage: gives a time to live of the resource in seconds, after which any shared caches must revalidate the response.

##### When a response must be revalidated
* must-revalidate: indicates a normally uncacheable response is cacheable, but requires a cache to revalidate stale responses before using a cached response. This forces revalidation requests to travel all the way to the origin server, but an efficient validation mechanism on the server will prevent complex service logic from being invoked on each request.
* proxy-revalidate: similar to must-revalidate, but applied only to shared caches.
* stale-while-revalidate: allows a cache to serve a stale response while a revalidation happens in the background. This directive favors reduced latency over consistency of data by allowing a cache to serve stale data while a non-blocking request to revalidate happens in the background.
* stale-if-error: allows a cache to serve a stale response if there is an error during revalidation. This directive favors availability over consistency by allowing a cache to return stale data during origin server failure.

#### Categorize by where the header appear
##### Request headers:

```
Cache-Control: max-age=<seconds>
Cache-Control: max-stale[=<seconds>]
Cache-Control: min-fresh=<seconds>
Cache-Control: no-cache 
Cache-Control: no-store
Cache-Control: no-transform
Cache-Control: only-if-cached
```

##### Response headers

```
Cache-Control: must-revalidate
Cache-Control: no-cache
Cache-Control: no-store
Cache-Control: no-transform
Cache-Control: public
Cache-Control: private
Cache-Control: proxy-revalidate
Cache-Control: max-age=<seconds>
Cache-Control: s-maxage=<seconds>
```

##### Extension headers

```
Cache-Control: immutable 
Cache-Control: stale-while-revalidate=<seconds>
Cache-Control: stale-if-error=<seconds>
```

### Flow chart
* [Flow chart](https://github.com/NeilMadden/cache-control-flowchart) for determining what Cache-Control header to use. 

![Cache-Control headers](./images/cacheControl-headers.png)

* There are some other charts useful for what should be done related with cache control headers. (In Chinese so not attach inline here)
	- [What proxy/server should do about caching when get a response](./images/cacheControlHeaders-server.png)
	- [What client should do about a request](./images/cacheControlHeaders-client.png)

## Conditional Get
* To revalidate a response with the origin server, a cache uses the value in the Validator headers (Etag or Last-Modified) to do a conditional GET.

### Validators
* ETag: An Etag, or entity tag, is an opaque token that the server associates with a particular state of a resource. Whenever the resource state changes, the entity tag should be changed accordingly.
* Last-modified: The Last-Modified header indicates the last point in time when the resource was changed.

### Conditional get headers

* If-Match: Succeeds if the ETag of the distant resource is equal to one listed in this header. By default, unless the etag is prefixed with 'W/', it performs a strong validation.
* If-None-Match: Succeeds if the ETag of the distant resource is different to each listed in this header. By default, unless the etag is prefixed with 'W/', it performs a strong validation.
* If-Modified-Since: Succeeds if the Last-Modified date of the distant resource is more recent than the one given in this header.
* If-Unmodified-Since: Succeeds if the Last-Modified date of the distant resource is older or the same than the one given in this header.
* If-Range: Similar to If-Match, or If-Unmodified-Since, but can have only one single etag, or one date. If it fails, the range request fails, and instead of a 206 Partial Content response, a 200 OK is sent with the complete resource.

### Flow chart
![Resource changed](./images/conditionalGetResourceChanged.png)

![Resource unchanged](./images/conditionalGetResourceUnchanged.png)

## Proxy related headers
* s-maxage
* proxy-revalidate
* no-transform
* X-forwarded-for / X-real-ip
* via

## HTTP session
### Stateless applications
* Web application servers are generally "stateless":
	- Each HTTP request is independent; server can't tell if 2 requests came from the same browser or user.
	- Web server applications maintain no information in memory from request to request (only information on disk survives from one request to another).
* Statelessness not always convenient for application developers: need to tie together a series of requests from the same user. Since the HTTP protocol is stateless itself, web applications developed techniques to create a concept of a session on top of HTTP so that servers could recognize multiple requests from the same user as parts of a more complex and longer lasting sequence. 

### Structure of a session
* The session is a key-value pair data structure. Think of it as a hashtable where each user gets a hashkey to put their data in. This hashkey would be the “session id”.
 
### Server-side session vs client-side cookie

| Category       | Session                                                                               | Cookie                                            | 
|----------------|---------------------------------------------------------------------------------------|---------------------------------------------------| 
| Location       | User ID on server                                                                     | User ID on web browser                            | 
| Safeness       | Safer because data cannot be viewed or edited by the client                           | A hacker could manipulate cookie data and attack  | 
| Amount of data | Big                                                                                   | Limited                                           | 
| Efficiency     | Save bandwidth by passing only a reference to the session (sessionID) each pageload.  | Must pass all data to the webserver each pageload | 
| Scalability    | Need efforts to scale because requests depend on server state                         | Easier to implement                               | 


#### Store session state in client-side cookies
##### Cookie Def
* Cookies are key/value pairs used by websites to store state informations on the browser. Say you have a website (example.com), when the browser requests a webpage the website can send cookies to store informations on the browser.

##### Cookie typical workflow

```
// Browser request example:

GET /index.html HTTP/1.1
Host: www.example.com

// Example answer from the server:


HTTP/1.1 200 OK
Content-type: text/html
Set-Cookie: foo=10
Set-Cookie: bar=20; Expires=Fri, 30 Sep 2011 11:48:00 GMT
... rest  of the response

// Here two cookies foo=10 and bar=20 are stored on the browser. The second one will expire on 30 September. In each subsequent request the browser will send the cookies back to the server.


GET /spec.html HTTP/1.1
Host: www.example.com
Cookie: foo=10; bar=20
Accept: */*
```

##### Cookie Pros and cons
* Advantage: You do not have to store the sesion state anywhere in your data center. The entire session state is being handed to your web server with every web request, thus making your application stateless in the context of the HTTP session. 
* Disadvantage: Session storage can becomes expensive. Cookies are sent by the browser with every single request, regardless of the type of resource being requested. As a result, all requests within the same cookie domain will have session storage appended as part of the request. 
* Use case: When you can keep your data minimal. If all you need to keep in session scope is userID or some security token, you will benefit from the simplicity and speed of this solution. Unfortunately, if you are not careful, adding more data to the session scope can quickly grow into kilobytes, making web requests much slower, especially on mobile devices. The coxt of cookie-based session storage is also amplified by the fact that encrypting serialized data and then Based64 encoding increases the overall byte count by one third, so that 1KB of session scope data becomes 1.3KB of additional data transferred with each web request and web response. 

#### Store session state in server-side 
* Approaches:
	- Keep state in main memory
	- Store session state in files on disk
	- Store session state in a database
		+ Delegate the session storage to an external data store: Your web application would take the session identifier from the web request and then load session data from an external data store. At the end of the web request life cycle, just before a response is sent back to the user, the application would serialize the session data and save it back in the data store. In this model, the web server does not hold any of the session data between web requests, which makes it stateless in the context of an HTTP session. 
		+ Many data stores are suitable for this use case, for example, Memcached, Redis, DynamoDB, or Cassandra. The only requirement here is to have very low latency on get-by-key and put-by-key operations. It is best if your data store provides automatic scalability, but even if you had to do data partitioning yourself in the application layer, it is not a problem, as sessions can be partitioned by the session ID itself. 

##### Typical server-side session workflow
1. Every time an internet user visits a specific website, a new session ID (a unique number that a web site's server assigns a specific user for the duration of that user's visit) is generated. And an entry is created inside server's session table

| Columns    | Type        | Meaning                       | 
|------------|-------------|-------------------------------| 
| sessionID | string      | a global unique hash value    | 
| userId     | Foreign key | pointing to user table        | 
| expireAt   | timestamp   | when does the session expires | 

2. Server returns the sessionID as a cookie header to client
3. Browser sets its cookie with the sessionID
4. Each time the user sends a request to the server. The cookie for that domain will be automatically attached.
5. The server validates the sessionID inside the request. If it is valid, then the user has logged in before. 

##### Use a load balancer that supports sticky sessions: 
* The load balancer needs to be able to inspect the headers of the request to make sure that requests with the same session cookie always go to the server that initially the cookie.
* But sticky sessions break the fundamental principle of statelessness, and I recommend avoiding them. Once you allow your web servers to be unique, by storing any local state, you lose flexibility. You will not be able to restart, decommission, or safely auto-scale web servers without braking user's session because their session data will be bound to a single physical machine. 

## Long connection
* https://juejin.im/post/6844903682467856392

## Security
### SSL 
#### Definition 
* Hyper Text Transfer Protocol Secure (HTTPS) is the secure version of HTTP, the protocol over which data is sent between your browser and the website that you are connected to. The 'S' at the end of HTTPS stands for 'Secure'. It means all communications between your browser and the website are encrypted. HTTPS is often used to protect highly confidential online transactions like online banking and online shopping order forms.

#### How does HTTPS work 
* HTTPS pages typically use one of two secure protocols to encrypt communications - SSL (Secure Sockets Layer) or TLS (Transport Layer Security). Both the TLS and SSL protocols use what is known as an 'asymmetric' Public Key Infrastructure (PKI) system. An asymmetric system uses two 'keys' to encrypt communications, a 'public' key and a 'private' key. Anything encrypted with the public key can only be decrypted by the private key and vice-versa.
* As the names suggest, the 'private' key should be kept strictly protected and should only be accessible the owner of the private key. In the case of a website, the private key remains securely ensconced on the web server. Conversely, the public key is intended to be distributed to anybody and everybody that needs to be able to decrypt information that was encrypted with the private key.

#### How to avoid public key being modified? 
* Put public key inside digital certificate.
	- When you request a HTTPS connection to a webpage, the website will initially send its SSL certificate to your browser. This certificate contains the public key needed to begin the secure session. Based on this initial exchange, your browser and the website then initiate the 'SSL handshake'. The SSL handshake involves the generation of shared secrets to establish a uniquely secure connection between yourself and the website.
	- When a trusted SSL Digital Certificate is used during a HTTPS connection, users will see a padlock icon in the browser address bar. When an Extended Validation Certificate is installed on a web site, the address bar will turn green.

#### How to avoid computation consumption from PKI 
* Only use PKI to generate session key and use the session key for further communications. 


## Web server
### Apache and Nginx 
* Apache and Nginx could always be used together. 
	- NGINX provides all of the core features of a web server, without sacrificing the lightweight and high‑performance qualities that have made it successful, and can also serve as a proxy that forwards HTTP requests to upstream web servers (such as an Apache backend) and FastCGI, memcached, SCGI, and uWSGI servers. NGINX does not seek to implement the huge range of functionality necessary to run an application, instead relying on specialized third‑party servers such as PHP‑FPM, Node.js, and even Apache.
	- A very common use pattern is to deploy NGINX software as a proxy in front of an Apache-based web application. Can use Nginx's proxying abilities to forward requests for dynamic resources to Apache backend server. NGINX serves static resources and Apache serves dynamic content such as PHP or Perl CGI scripts. 

### Apache vs Nginx 

| Category           | Apache   | Nginx         |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| History            | Invented around 1990s when web traffic is low and web pages are really simple. Apache's heavyweight, monolithic model has its limit. Tunning Apache to cope with real-world traffic efficiently is a complex art. | Heavy traffic and web pages. Designed for high concurrency. Provides 12 features including which make them appropriate for microservices. |
| Architecture       | One process/threads per connection. Each requests to be handled as a separate child/thread. | Asynchronous event-driven model. There is a single master process with one or more worker processes. |
| Performance        | To decrease page-rendering time, web browsers routinely open six or more TCP connections to a web server for each user session so that resources can download in parallel. Browsers hold these connections open for a period of time to reduce delay for future requests the user might make during the session. Each open connection exclusively reserves an httpd process, meaning that at busy times, Apache needs to create a large number of processes. Each additional process consumes an extra 4MB or 5MB of memory. Not to mention the overhead involved in creating and destroying child processes. | Can handle a huge number of concurrent requests | 
| Easier development | Very easy to insert additional code at any point in Apache's web-serving logic. Developers could add code securely in the knowledge that if newly added code is blocked, ran slowly, leaked resources, or even crashed, only the worker process running the code would be affected. Processing of all other connections would continue undisturbed | Developing modules for it isn't as simple and easy as with Apache. Nginx module developers need to be very careful to create efficient and accurate code, without any resource leakage, and to interact appropriately with the complex event-driven kernel to avoid blocking operations. | 



