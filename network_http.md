# Network\_HTTP

* [Http Protocol](network_http.md#http-protocol)
  * [TCP vs UDP](network_http.md#tcp-vs-udp)
  * [Proxy related headers](network_http.md#proxy-related-headers)
  * [HTTP session](network_http.md#http-session)
    * [Stateless applications](network_http.md#stateless-applications)
    * [Structure of a session](network_http.md#structure-of-a-session)
    * [Server-side session vs client-side cookie](network_http.md#server-side-session-vs-client-side-cookie)
      * [Store session state in client-side cookies](network_http.md#store-session-state-in-client-side-cookies)
        * [Cookie Def](network_http.md#cookie-def)
        * [Cookie typical workflow](network_http.md#cookie-typical-workflow)
        * [Cookie Pros and cons](network_http.md#cookie-pros-and-cons)
      * [Store session state in server-side](network_http.md#store-session-state-in-server-side)
        * [Typical server-side session workflow](network_http.md#typical-server-side-session-workflow)
        * [Use a load balancer that supports sticky sessions:](network_http.md#use-a-load-balancer-that-supports-sticky-sessions)
  * [Long connection](network_http.md#long-connection)
  * [Security](network_http.md#security)
    * [SSL](network_http.md#ssl)
      * [Definition](network_http.md#definition)
      * [How does HTTPS work](network_http.md#how-does-https-work)
      * [How to avoid public key being modified?](network_http.md#how-to-avoid-public-key-being-modified)
      * [How to avoid computation consumption from PKI](network_http.md#how-to-avoid-computation-consumption-from-pki)

## Http Protocol

### TCP vs UDP

| TCP | UDP |
| :--- | :--- |
| Reliable: TCP is connection-oriented protocol. When a file or message send it will get delivered unless connections fails. If connection lost, the server will request the lost part. There is no corruption while transferring a message. | Not Reliable: UDP is connectionless protocol. When you a send a data or message, you don’t know if it’ll get there, it could get lost on the way. There may be corruption while transferring a message. |
| Ordered: If you send two messages along a connection, one after the other, you know the first message will get there first. You don’t have to worry about data arriving in the wrong order. | Not Ordered: If you send two messages out, you don’t know what order they’ll arrive in i.e. no ordered |
| Heavyweight: – when the low level parts of the TCP “stream” arrive in the wrong order, resend requests have to be sent, and all the out of sequence parts have to be put back together, so requires a bit of work to piece together. | Lightweight: No ordering of messages, no tracking connections, etc. It’s just fire and forget! This means it’s a lot quicker, and the network card / OS have to do very little work to translate the data back from the packets. |
| Streaming: Data is read as a “stream,” with nothing distinguishing where one packet ends and another begins. There may be multiple packets per read call. | Datagrams: Packets are sent individually and are guaranteed to be whole if they arrive. One packet per one read call. |
| Examples: World Wide Web \(Apache TCP port 80\), e-mail \(SMTP TCP port 25 Postfix MTA\), File Transfer Protocol \(FTP port 21\) and Secure Shell \(OpenSSH port 22\) etc. | Examples: Domain Name System \(DNS UDP port 53\), streaming media applications such as IPTV or movies, Voice over IP \(VoIP\), Trivial File Transfer Protocol \(TFTP\) and online multiplayer games |

### Proxy related headers

* s-maxage
* proxy-revalidate
* no-transform
* X-forwarded-for / X-real-ip
* via

### HTTP session

#### Stateless applications

* Web application servers are generally "stateless":
  * Each HTTP request is independent; server can't tell if 2 requests came from the same browser or user.
  * Web server applications maintain no information in memory from request to request \(only information on disk survives from one request to another\).
* Statelessness not always convenient for application developers: need to tie together a series of requests from the same user. Since the HTTP protocol is stateless itself, web applications developed techniques to create a concept of a session on top of HTTP so that servers could recognize multiple requests from the same user as parts of a more complex and longer lasting sequence. 

#### Structure of a session

* The session is a key-value pair data structure. Think of it as a hashtable where each user gets a hashkey to put their data in. This hashkey would be the “session id”.

#### Server-side session vs client-side cookie

| Category | Session | Cookie |
| :--- | :--- | :--- |
| Location | User ID on server | User ID on web browser |
| Safeness | Safer because data cannot be viewed or edited by the client | A hacker could manipulate cookie data and attack |
| Amount of data | Big | Limited |
| Efficiency | Save bandwidth by passing only a reference to the session \(sessionID\) each pageload. | Must pass all data to the webserver each pageload |
| Scalability | Need efforts to scale because requests depend on server state | Easier to implement |

**Store session state in client-side cookies**

**Cookie Def**

* Cookies are key/value pairs used by websites to store state informations on the browser. Say you have a website \(example.com\), when the browser requests a webpage the website can send cookies to store informations on the browser.

**Cookie typical workflow**

```text
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

**Cookie Pros and cons**

* Advantage: You do not have to store the sesion state anywhere in your data center. The entire session state is being handed to your web server with every web request, thus making your application stateless in the context of the HTTP session. 
* Disadvantage: Session storage can becomes expensive. Cookies are sent by the browser with every single request, regardless of the type of resource being requested. As a result, all requests within the same cookie domain will have session storage appended as part of the request. 
* Use case: When you can keep your data minimal. If all you need to keep in session scope is userID or some security token, you will benefit from the simplicity and speed of this solution. Unfortunately, if you are not careful, adding more data to the session scope can quickly grow into kilobytes, making web requests much slower, especially on mobile devices. The coxt of cookie-based session storage is also amplified by the fact that encrypting serialized data and then Based64 encoding increases the overall byte count by one third, so that 1KB of session scope data becomes 1.3KB of additional data transferred with each web request and web response. 

**Store session state in server-side**

* Approaches:
  * Keep state in main memory
  * Store session state in files on disk
  * Store session state in a database
    * Delegate the session storage to an external data store: Your web application would take the session identifier from the web request and then load session data from an external data store. At the end of the web request life cycle, just before a response is sent back to the user, the application would serialize the session data and save it back in the data store. In this model, the web server does not hold any of the session data between web requests, which makes it stateless in the context of an HTTP session. 
    * Many data stores are suitable for this use case, for example, Memcached, Redis, DynamoDB, or Cassandra. The only requirement here is to have very low latency on get-by-key and put-by-key operations. It is best if your data store provides automatic scalability, but even if you had to do data partitioning yourself in the application layer, it is not a problem, as sessions can be partitioned by the session ID itself. 

**Typical server-side session workflow**

1. Every time an internet user visits a specific website, a new session ID \(a unique number that a web site's server assigns a specific user for the duration of that user's visit\) is generated. And an entry is created inside server's session table

| Columns | Type | Meaning |
| :--- | :--- | :--- |
| sessionID | string | a global unique hash value |
| userId | Foreign key | pointing to user table |
| expireAt | timestamp | when does the session expires |

1. Server returns the sessionID as a cookie header to client
2. Browser sets its cookie with the sessionID
3. Each time the user sends a request to the server. The cookie for that domain will be automatically attached.
4. The server validates the sessionID inside the request. If it is valid, then the user has logged in before. 

**Use a load balancer that supports sticky sessions:**

* The load balancer needs to be able to inspect the headers of the request to make sure that requests with the same session cookie always go to the server that initially the cookie.
* But sticky sessions break the fundamental principle of statelessness, and I recommend avoiding them. Once you allow your web servers to be unique, by storing any local state, you lose flexibility. You will not be able to restart, decommission, or safely auto-scale web servers without braking user's session because their session data will be bound to a single physical machine. 

### Long connection

* [https://juejin.im/post/6844903682467856392](https://juejin.im/post/6844903682467856392)
* screenshot for comparison http1.0/http1.1
  * [https://blog.insightdatascience.com/learning-about-the-http-connection-keep-alive-header-7ebe0efa209d](https://blog.insightdatascience.com/learning-about-the-http-connection-keep-alive-header-7ebe0efa209d)
* TCP vs HTTP keep-alive
  * [https://www.colabug.com/2019/0310/5661258/](https://www.colabug.com/2019/0310/5661258/)
* How to open keep-alive on webserver
  * Use Nginx as an example [https://blog.csdn.net/qq\_34556414/article/details/106116889](https://blog.csdn.net/qq_34556414/article/details/106116889)
* different categories of keepalive 
  * [https://zhuanlan.zhihu.com/p/73484447](https://zhuanlan.zhihu.com/p/73484447)

### Security

#### SSL

**Definition**

* Hyper Text Transfer Protocol Secure \(HTTPS\) is the secure version of HTTP, the protocol over which data is sent between your browser and the website that you are connected to. The 'S' at the end of HTTPS stands for 'Secure'. It means all communications between your browser and the website are encrypted. HTTPS is often used to protect highly confidential online transactions like online banking and online shopping order forms.

**How does HTTPS work**

* HTTPS pages typically use one of two secure protocols to encrypt communications - SSL \(Secure Sockets Layer\) or TLS \(Transport Layer Security\). Both the TLS and SSL protocols use what is known as an 'asymmetric' Public Key Infrastructure \(PKI\) system. An asymmetric system uses two 'keys' to encrypt communications, a 'public' key and a 'private' key. Anything encrypted with the public key can only be decrypted by the private key and vice-versa.
* As the names suggest, the 'private' key should be kept strictly protected and should only be accessible the owner of the private key. In the case of a website, the private key remains securely ensconced on the web server. Conversely, the public key is intended to be distributed to anybody and everybody that needs to be able to decrypt information that was encrypted with the private key.

**How to avoid public key being modified?**

* Put public key inside digital certificate.
  * When you request a HTTPS connection to a webpage, the website will initially send its SSL certificate to your browser. This certificate contains the public key needed to begin the secure session. Based on this initial exchange, your browser and the website then initiate the 'SSL handshake'. The SSL handshake involves the generation of shared secrets to establish a uniquely secure connection between yourself and the website.
  * When a trusted SSL Digital Certificate is used during a HTTPS connection, users will see a padlock icon in the browser address bar. When an Extended Validation Certificate is installed on a web site, the address bar will turn green.

**How to avoid computation consumption from PKI**

* Only use PKI to generate session key and use the session key for further communications. 

