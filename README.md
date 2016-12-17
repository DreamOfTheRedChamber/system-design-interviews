# Fight for 100 commits
* [Components](#Components)
	- [HTTP](#http)
		+ [Status code](#http-status-code)
		+ [Headers](#http-headers)
		+ [Verbs](#http-verbs)
	- [Web services](#web-services)
		+ [REST best practices](#web-services-rest-best-practices)
	- [Frontend](#frontend)
		+ [Minification](#frontend-minification)
		+ [DNS](#frontend-dns)
		+ [Load balancers](#frontend-load-balancers)
			* [Benefits](#frontend-load-balancers-benefits)
			* [Types](#frontend-load-balancers-types)
		+ [Web servers](#frontend-web-servers)
	- [Backend](#backend)
		+ [Message queue](#backend-message-queue)
			* [Benefits](#backend-message-queue-benefits)
			* [Components](#backend-message-queue-components)
			* [Routing methods](#backend-message-queue-routing-methods)
			* [Protocols](#backend-message-queue-protocols)
			* [Metrics](#backend-message-queue-metrics)
			* [Challenges](#backend-message-queue-challenges)
		+ [MySQL](#backend-MySQL)
			* [Replication](#backend-MySQL-replication)
				- [When to use](#backend-MySQL-replication-when-to-use)
				- [When not to use](#backend-MySQL-replication-when-not-to-use)
				- [Consistency](#backend-MySQL-replication-consistency)
				- [Master-slave vs peer-to-peer](#backend-MySQL-replication-types)
				- [Master-slave](#backend-MySQL-replication-master-slave)
					+ [Number of slaves](#backend-MySQL-replication-master-slave-number-of-slaves)
					+ [Consistency](#backend-MySQL-replication-master-slave-consistency)
					+ [Failure recovery](#backend-MySQL-replication-master-slave-failure-recovery)
				- [Deployment topology](#backend-MySQL-replication-deployment-topology)
			* [Sharding](#backend-MySQL-sharding)
				- [Benefits](#backend-MySQL-sharding-benefits)
				- [Types](#backend-MySQL-sharding-types)
				- [Challenges](#backend-MySQL-sharding-challenges)
		+ [NoSQL](#backend-NoSQL)
			* [Features](#backend-NoSQL-features)
* [Typical system design workflow](#workflow)
	- [Scenarios](#workflow-scenario)
		+ [Features](#workflow-scenario-features)
			* [Common features](#workflow-scenario-common-features)
		+ [Design goals](#workflow-scenario-design-goals)
		+ [Metrics](#workflow-scenario-metrics)
	- [Service](#workflow-service)
	- [Storage](#workflow-storage)
		+ [Manage HTTP sessions](#manage-http-sessions)
		+ [MySQL index](#workflow-storage-mysql-index)
		+ [NoSQL features](#workflow-storage-nosql-features)
		+ [Consistency](#workflow-storage-consistency)
	- [Scale](#workflow-scale)
		+ [Cache](#workflow-scale-cache)
			* [Cache hit ratio](#workflow-scale-cache-hit-ratio)
			* [Cache based on HTTP](#workflow-scale-cache-based-on-HTTP)
				- [HTTP Caching headers](#workflow-scale-cache-HTTP-caching-headers)
				- [Types of HTTP cache technologies](#workflow-scale-cache-types-of-HTTP-cache-technologies)
				- [A few common caching scenarios](#workflow-scale-cache-scenarios)
				- [Scaling HTTP caches](#workflow-scale-cache-scale-http-caches)
			* [Cache for application objects](#workflow-scale-cache-application-objects)
			* [Caching rules of thumb](#caching-rules-of-thumb)
		+ [Consistency](#workflow-scale-consistency)
			* [Update consistency](#workflow-scale-update-consistency)
			* [Read consistency](#workflow-scale-read-consistency)
			* [Tradeoffs between availability and consistency](#workflow-scale-tradeoff-availability-consistency)
			* [Tradeoffs between latency and durability](#workflow-scale-tradeoff-latency-durability)
* [Popular technologies](#popular-technologies)
	- [Cassandra](#cassandra)
 		+ [Data model](#cassandra-data-model)
		+ [Features](#cassandra-features)
		+ [Read-write prcess](#cassandra-read-write-process)
	- [Kafka](#kafka) 
	- [Zookeeper](#zookeeper)
	- [Spark](#spark)
	- [Redis](#redis)
	- [Nodejs](#nodejs)

# Components <a id="components"></a>
## HTTP <a id="http"></a>
### HTTP status code <a id="http-status-code"></a>
* 2XX: Success
	- 200 OK: The request has succeeded. Especially used on successful first GET requests or PUT/PATCH updated content.
	- 201 Created: Indicates that a resource was created. Typically responding to PUT and POST requests.
	- 202 Accepted: Indicates that the request has been accepted for processing. Typically responding to an asynchronous processing call (for a better UX and good performances).
	- 204 No Content: The request succeeded but there is nothing to show. Usually sent after a successful DELETE.
	- 206 Partial Content: The returned resource is incomplete. Typically used with paginated resources.
* 3XX: Redirection
	- 301: Moved permanently
	- 307: Temporary redirect
* 4XX: Client Error
	- 400 Bad request: The client sends a malformed request to the service.
	- 401 Unauthorized: I do not know you, tell me who you are and I will check your permissions.
	- 403 Forbidden: Your rights are not sufficient to access this resource.
	- 404 Not found: The resource you are requesting does not exist.
	- 405 Method not allowed: Either the method is not supported or relevant on this resource or the user does not have the permission.
	- 406 Not acceptable: There is nothing to send that matches the Accept-* headers. For example, you requested a resource in XML but it is only available in JSON.
* 5XX: Server Error
	- 500 Internal server error: For those rare cases where the server faults and cannot recover internally.
	- 502 Bad gateway: 
		+ Usually due to improperly configured proxy servers. 
		+ Also possible when a server is overloaded or a firewall is configured improperly.
	- 503 Service unavailable:
		+ Server is unavailable to handle requests due to a temporary overload or due to the server being temporarily closed for maintainence. The error only indicates that the server will only temporarily be down.  
	- 504 Gateway timeout: 
		+ When a server somewhere along the chain does not receive a timely response from a server further up the chain. The problem is caused entirely by slow communication between upstream computers.

### Headers <a id="http-headers"></a>
* Content-type/Media type: Tells HTTP client how to understand the entity-body.
	- e.g. text/html, application/json, image/jpeg
* Accept-Encoding/Content-Encoding: 
	- Condition: Content compression occurs only when a client advertises, wants to use it and a server indicates its willingness to enable it. 
		+ Clients indicate they want to use it by sending the Accept-Encoding header when making requests. The value of this header is a comma-separated list of compression methods that the client will accept. For example, Accept-Encoding: gzip, deflate.
		+ If the server supports any of the compression methods that the client has advertised, it may deliver a compressed version of the resource. It indicates that the content has been compressed with the Content-Encoding header in the response. For example, Content-Encoding: gzip. Content-Length header in the response indicates the size of the compressed content. 
	- Methods 
		+ identity: no compression. 
		+ compress: UNIX compress method, which is based on the Lempel-Ziv Welch (LZW) aglorithm
		+ gzip: the most popular format. 
		+ deflate: just gzip without the checksum header. 
	- What to compress:
		+ Usually applied to text-based content such as HTML, XML, CSS, and Javascript.
		+ Not applied to binary data
			* Many of binary formats such as GIF, PNG, and JPEG already use compression. 
	- Disadvantages:
		+ There is additional CPU usage at both the server side and client side. 
		+ There will always be a small percentage of clients that simply can't accept compressed content.

### Verbs <a id="http-verbs"></a>
* CRUD verbs.
* Patch: Small modifications
* Head: A lightweight version of GET
* Options: Discovery mechanism for HTTP
	- Link/Unlink: Removes the link between a story and its author

| Verb | URI or template | common response code | Use | 
| ---- |:----------------:|:-------------------:|:-------------:| 
| POST | /order           | Created(201) / 202(Accepted) | Post-to-append: Create a new order, and upon success, receive a Location header specifying the new order's URI / Overloaded-post: | 
| GET  | /order/{orderId} | OK(200) / Moved permanently(301) / Not found (404) | Ask for a representation of a resource |  
| PUT  | /order/{orderId} | OK(200) / 204(No content)| Modify resource state | 
| DELETE | /order/{orderId} | OK(200) / 202(Accepted, will delete later) / 204 (has already been deleted, nothing more to say about it) | Wants to destroy a resource | 

## Web services <a id="web-services"></a>
### REST best practices <a id="web-services-rest-best-practices"></a>
* Resources
	- Use nouns but no verbs for resources. Use subresource for relations
		+ GET /cars/711/drivers/ Returns a list of drivers for car 711
		+ GET /cars/711/drivers/4 Returns driver #4 for car 711
	- Plurals nouns: You should use plural nouns for all resources
		+ Collection resource: /users
		+ Instance resource: /users/007
	- Consistent casing
	- Average granularity
		+ "One resource = one URL" theory tends to increase the number of resources. It is important to keep a reasonable limit.
		+ Group only resources that are almost always accessed together. 
		+ Having at most 2 levels of nested objects (e.g. /v1/users/addresses/countries)
* Use HTTP verbs for CRUD operations (Create/Read/Update/Delete).
	- POST is used to Create an instance of a collection. The ID isn’t provided, and the new resource location is returned in the “Location” Header.
		+ POST /orders {"state":"running", "id_user":"007"}
	- PUT is used for Updates to perform a full replacement. But remember that, if the ID is specified by the client, PUT is used to Create the resource.
	- GET is used to Read a collection/an instance
		+ GET /orders
		+ GET /orders/1234
	- PATCH is commonly used for partial Update.
		+ PATCH /orders/1234 {"state":"paid"}
	- Updates & creation should return a resource representation
		+ A PUT, POST or PATCH call may make modifications to fields of the underlying resource that weren't part of the provided parameters (for example: created_at or updated_at timestamps). To prevent an API consumer from having to hit the API again for an updated representation, have the API return the updated (or created) representation as part of the response.
		+ In case of a POST that resulted in a creation, use a HTTP 201 status code and include a Location header that points to the URL of the new resource.
* What about actions that don't fit into the world of CRUD operations?
	- Restructure the action to appear like a field of a resource. This works if the action doesn't take parameters. For example an activate action could be mapped to a boolean activated field and updated via a PATCH to the resource.
	- Treat it like a sub-resource with RESTful principles. For example, GitHub's API lets you star a gist with PUT /gists/:id/star and unstar with DELETE /gists/:id/star.
	- Sometimes you really have no way to map the action to a sensible RESTful structure. For example, a multi-resource search doesn't really make sense to be applied to a specific resource's endpoint. In this case, /search would make the most sense even though it isn't a resource. This is OK - just do what's right from the perspective of the API consumer and make sure it's documented clearly to avoid confusion.
* Cache
	- ETag:
		+  When generating a request, include a HTTP header ETag containing a hash or checksum of the representation. This value should change whenever the output representation changes. Now, if an inbound HTTP requests contains a If-None-Match header with a matching ETag value, the API should return a 304 Not Modified status code instead of the output representation of the resource.
	- Last-Modified:
		+ This basically works like to ETag, except that it uses timestamps. The response header Last-Modified contains a timestamp in RFC 1123 format which is validated against If-Modified-Since. Note that the HTTP spec has had 3 different acceptable date formats and the server should be prepared to accept any one of them.
	- If-Modified-Since/If-None-Match: A client sends a conditional request. Then the server should return data only when this condition satifies. Otherwise the server should return 304 unmodified. For example, if a client has cached the response of a request and only wants to know whether they are latest.
	- If-Match, 
* Error handling: It is hard to work with an API that ignores error handling. Pure returning of a HTTP 500 with a stacktrace is not very helpful.
	- Use HTTP status codes:
		+ Error codes starting with 4XX means client side error.
		+ Error codes starting with 5XX means server side error.
	- Use error payloads:
		+ All exceptions should be mapped in an error payload.
* Always use OAuth and HTTPS for security.
	- OAuth: OAuth2 allows you to manage authentication and resource authorization for any type of application (native mobile app, native tablet app, JavaScript app, server side web app, batch processing…) with or without the resource owner’s consent.
	- HTTPS: 
* Provide filtering, sorting, field selection and paging for collections
	- Filtering: Use a unique query parameter for all fields or a query language for filtering.
		+ GET /cars?color=red Returns a list of red cars
		+ GET /cars?seats<=2 Returns a list of cars with a maximum of 2 seats
	- Sorting: Allow ascending and descending sorting over multiple fields
		+ GET /cars?sort=-manufactorer,+model. This returns a list of cars sorted by descending manufacturers and ascending models.
	- Field selection: Mobile clients display just a few attributes in a list. They don’t need all attributes of a resource. Give the API consumer the ability to choose returned fields. This will also reduce the network traffic and speed up the usage of the API.
	 	+ GET /cars?fields=manufacturer,model,id,color
	- Paging: 
	 	+ Use limit and offset. It is flexible for the user and common in leading databases. The default should be limit=20 and offset=0. GET /cars?offset=10&limit=5.
	 	+ To send the total entries back to the user use the custom HTTP header: X-Total-Count.
	 	+ Links to the next or previous page should be provided in the HTTP header link as well. It is important to follow this link header values instead of constructing your own URLs.
* Content negotiation
	- Content-type defines the request format. 
	- Accept defines a list of acceptable response formats. If a client requires you to return application/xml and the server could only return application/json, then you'd better return status code 406.
	- We recommend handling several content distribution formats. We can use the HTTP Header dedicated to this purpose: “Accept”.
	- By default, the API will share resources in the JSON format, but if the request begins with “Accept: application/xml”, resources should be sent in the XML format.
	- It is recommended to manage at least 2 formats: JSON and XML. The order of the formats queried by the header “Accept” must be observed to define the response format.
	- In cases where it is not possible to supply the required format, a 406 HTTP Error Code is sent (cf. Errors — Status Codes).
* Pretty print by default and ensure gzip is supported
	- An API that provides white-space compressed output isn't very fun to look at from a browser. Although some sort of query parameter (like ?pretty=true) could be provided to enable pretty printing, an API that pretty prints by default is much more approachable.
* Cross-domain
* Rate limiting
	- To prevent abuse, it is standard practice to add some sort of rate limiting to an API. RFC 6585 introduced a HTTP status code 429 Too Many Requests to accommodate this.
	- However, it can be very useful to notify the consumer of their limits before they actually hit it. This is an area that currently lacks standards but has a number of popular conventions using HTTP response headers.
	- At a minimum, include the following headers (using newsfeed's naming conventions as headers typically don't have mid-word capitalization):
		+ X-Rate-Limit-Limit - The number of allowed requests in the current period
		+ X-Rate-Limit-Remaining - The number of remaining requests in the current period
		+ X-Rate-Limit-Reset - The number of seconds left in the current period
* Versioning: Make the API Version mandatory and do not release an unversioned API. An API version should be included in the URL to ensure browser explorability. 
* Docs
	- The docs should be easy to find and publically accessible. Most developers will check out the docs before attempting any integration effort. When the docs are hidden inside a PDF file or require signing in, they're not only difficult to find but also not easy to search.
	- The docs should show examples of complete request/response cycles. Preferably, the requests should be pastable examples - either links that can be pasted into a browser or curl examples that can be pasted into a terminal. GitHub and Stripe do a great job with this.
		+ CURL: always illustrating your API call documentation by cURL examples. Readers can simply cut-and-paste them, and they remove any ambiguity regarding call details.
	- Once you release a public API, you've committed to not breaking things without notice. The documentation must include any deprecation schedules and details surrounding externally visible API updates. Updates should be delivered via a blog (i.e. a changelog) or a mailing list (preferably both!).
* HATEOAS: Hypertext As The Engine of Application State
	- There should be a single endpoint for the resource, and all of the other actions you’d need to undertake should be able to be discovered by inspecting that resource.
	- People are not doing this because the tooling just isn't there.
* Hooks/Event propogation


## Front end <a id="frontend"></a>
### Minification <a id="frontend-minification"></a>
* Javascript and CSS:
	- Tools: YUI compressor, Google closure
* HTML:
	- Whereas CSS and Javascript are usually static files, HTML is often generated on-the-fly by assembling fragments of markup using a back-end scripting language. Although the percentage decrease in size may be lower for HTML documents, the cumulative effect over the length of the user's session is often greater thann that for CSS and Javascript. 
* Images: 
	- Using the JPEG format for photography and GIF and PNG for everything else. 
	- SVG uses XML to describe an image in terms of geometrical shapes. The fact that they must be parsed and rendered by the browser raises its own performance considerations. 

### DNS <a id="frontend-dns"></a>
* Resolve domain name to IP address		
* The process: When a user enters a URL into the browser's address bar, the first step is for the browser to resolve the hostname to an IP address, a task that it delegates to the operating system. At this stage, the operating system has a couple of choices. 
	- It can either resolve the address using a static hosts file (such as /etc/hosts on Linux) 
	- It can query a DNS resolver. Most commonly, the DNS server is hosted by the client's ISP. In larger corporate environments, it's common for a resolving DNS server to run inside the LAN.
	- If the queried resolver does not have the answer, it attempts to establish which name servers are authoritative for the hostname the clients want to resolve. It then queries one of them and relays the answer back to the client that issued the request.  
* Caching: In practice, queries can be much faster than this because of the effects of caching. 
	- Types:
	    + Whenever the client issues a request to an ISP's resolver, the resolver caches the response for a short period (TTL, set by the authoritative name server), and subsequent queries for this hostname can be answered directly from the cache. 
        + All major browsers also implement their own DNS cache, which removes the need for the browser to ask the operating system to resolve. Because this isn't particularly faster than quuerying the operating system's cache, the primary motivation here is better control over what is cached and for how long.
    - Performance:
    	+ DNS look-up times can vary dramatically - anything from a few milliseconds to perhaps one-half a second if a remote name server must be queried. This manifests itself mostly as a slight delay when the user first loads the site. On subsequent views, the DNS query is answered from a cache. 
* DNS prefeching:
	- Involves performing DNS lookups on URLs linked to in the HTML document, in anticipation that the user may eventually click one of these links. 
		+ Prefetchinng is slightly reminiscent of those annoying browsers and plug-ins that were popular a decade or so ago. They would prefetch all the links in an HTML document to improve responsiveness. The difference with DNS prefetching is that the amount of data sent over network is much lower. Typically, a single UDP packet can carry the question, and a second UDP packet can carry the answer. 

### Load balancers <a id="frontend-load-balancers"></a>
* All the traffic between web servers and clients is routed through the load balancer. 

#### Benefits <a id="frontend-load-balancers-benefits"></a>
* Hidden server maintenance. 
	- You can take a web server out of the load balancer pool, wait for all active connections to drain, and then safely shutdown the web server without affecting even a single client. You can use this method to perform rolling updates and deploy new software across the cluster without any downtime. 
* Seamlessly increase capacity. 
	- You can add more web servers at any time without your client ever realizing it. As soon as you add a new server, it can start receiving connections. 
* Automated scaling. 
	- If you are on cloud-based hosting with the ability to configure auto-scaling (like Amazon, Open Stack, or Rackspace), you can add and remove web servers throughout the day to best adapt to the traffic. 
* Effective resource management. 
	- You can use Secure Socket Layer (SSL) offloading to reduce the resources your web servers need. 

#### Types <a id="frontend-load-balancers-types"></a>
* Self-managed software-based load balancer (Nginx/HAProxy)
	- Nginx: The main advantage of Nginx is that it is also a reverse HTTP proxy, so it can cache HTTP responses from your servers. This quality makes it a great candidate for internal web service load balancer. 
	- HAProxy: Simpler in design than Nginx, just a load balancer. It can be configured as either a layer 4 or layer 7 load balancer. 
		+ When HAProxy is set up to be a layer 4 proxy, it does not inspect higher-level protocols and it depends solely on TCP/IP headers to distribute the traffic. This, in turn, allows HAProxy to be a load balancer for any protocol, not just HTTP/HTTPS. You can use HAProxy to distribute traffic for services like cache servers, message queues, or databases. 
		+ HAProxy can also be configured as a layer 7 proxy, in which case it supports sticky sessions and SSL termination, but needs more resources to be able to inspect and track HTTP-specific information. The fact that HAProxy is simpler in design makes it perform sligthly better than Nginx, especially when configured as a layer 4 load balancer. Finally, HAProxy has built-in high-availability support. 
	- In both cases, whether you use Nginx or HAProxy, you will need to scale the load balancer yourself. You are most likely going to reach the capacity limit by having too many concurrent connections or by having too many requests per second being sent to the load balancer. When you do reach the limits of your load balancer capacity, you can scale out by deploying multiple load balancers under distinct public IP addresses and distributing traffic among them via a round-robin DNS. 
* Hardware load balancer
	- Advantages: High throughput, extremely low latencies, and consistent performance. 
	- Downsides: 
		+ High purchase cost. Hardware load balancer prices start from a few thousand dollars and go as high as over 100,000 dollars per device. 
		+ Specialized training and harder to find people with the work experience necessary to operate them. 

### Web servers <a id="frontend-web-servers"></a>
* Technologies selection: Front end is mainly about handling user interactions, rendering views and processing user input, it makes sense to use technologies that are good at these tasks. I would recommend dynamic languages like PHP, Python, Groovy, Ruby or even Node.js for the front-end web application development. You want to make common front-end problems like SEO, AJAX, internationalization easy to solve. 

## Back end <a id="backend"></a>
### Message queue <a id="backend-message-queue"></a>
#### Benefits <a id="backend-message-queue-benefits"></a>
* **Enabling asynchronous processing**: 
	- Defer processing of time-consuming tasks without blocking our clients. Anything that is slow or unpredictable is a candidate for asynchronous processing. Example include
		+ Interact with remote servers
		+ Low-value processing in the critical path
		+ Resource intensive work
		+ Independent processing of high- and low- priority jobs
	- Message queues enable your application to operate in an asynchronous way, but it only adds value if your application is not built in an asynchronous way to begin with. If you developed in an environment like Node.js, which is built with asynchronous processing at its core, you will not benefit from a message broker that much. What is good about message brokers is that they allow you to easily introduce asynchronous processing to other platforms, like those that are synchronous by nature (C, Java, Ruby)
* **Easier scalability**: 
	- Producers and consumers can be scaled separately. We can add more producers at any time without overloading the system. Messages that cannot be consumed fast enough will just begin to line up in the message queue. We can also scale consumers separately, as now they can be hosted on separate machines and the number of consumers can grow independently of producers.
* **Decoupling**: 
	- All that publishers need to know is the format of the message and where to publish it. Consumers can become oblivious as to who publishes messages and why. Consumers can focus solely on processing messages from the queue. Such a high level decoupling enables consumers and producers to be developed indepdently. They can even be developed by different teams using different technologies. 
* **Evening out traffic spikes**:
	- You should be able to keep accepting requests at high rates even at times of icnreased traffic. Even if your publishing generates messages much faster than consumers can keep up with, you can keep enqueueing messages, and publishers do not have to be affected by a temporary capacity problem on the consumer side. 
* **Isolating failures and self-healing**:
	- The fact that consumers' availability does not affect producers allows us to stop message processing at any time. This means that we can perform maintainance and deployments on back-end servers at any time. We can simply restart, remove, or add servers without affecting producer's availability, which simplifies deployments and server management. Instead of breaking the entire application whenever a back-end server goes offline, all that we experience is reduced throughput, but there is no reduction of availability. Reduced throughput of asynchronous tasks is usually invisible to the user, so there is no consumer impact. 

#### Components <a id="backend-message-queue-components"></a>
* Message producer 
	- Locate the message queue and send a valid message to it
* Message broker - where messages are sent and buffered for consumers. 
	- Be available at all times for producers and to accept their messages. 
	- Buffering messages and allowing consumers to consume related messages.
* Message consumer
	- Receive and process message from the message queue. 
	- The two most common ways of implement consumers are a "cron-like" and a "daemon-like" approach. 
		+ Connects periodically to the queue and checks the status of the queue. If there are messages, it consumes them and stops when the queue is empty or after consuming a certain amount of messages. This model is common in scripting languages where you do not have a persistenly running application container, such as PHP, Ruby, or Perl. Cron-like is also referred to as a pull model because the consumers pulls messages from the queue. It can also be used if messages are added to the queue rarely or if network connectivity is unreliable. For example, a mobile application may try to pull the queue from time to time, assuming that connection may be lost at any point in time.
		+ A daemon-like consumer runs constantly in an infinite loop, and it usually has a permanent connection to the message broker. Instead of checking the status of the queue periodically, it simply blocks on the socket read operation. This means that the consumer is waiting idly until messages are pushed by the message broker in the connection. This model is more common in languages with persistent application containers, such as Java, C#, and Node.js. This is also referred to as a push model because messages are pushed by the message broker onto the consumer as fast as the consumer can keep processing them. 

#### Routing methods <a id="backend-message-queue-routing-methods"></a>
* Direct worker queue method
	- Consumers and producers only have to know the name of the queue. 
	- Well suited for the distribution of time-consuming tasks such as sending out e-mails, processing videos, resizing images, or uploading content to third-party web services.
* Publish/Subscribe method
	- Producers publish message to a topic, not a queue. Messages arriving to a topic are then cloned for each consumer that has a declared subscription to that topic. 
* Custom routing rules
	- A consumer can decide in a more flexible way what messages should be routed to its queue. 
	- Logging and alerting are good examples of custom routing based on pattern matching. 

#### Protocols <a id="backend-message-queue-protocols"></a>
* AMQP: A standardized protocol accepted by OASIS. Aims at enterprise integration and interoperability. 
* STOMP: A minimalist protocol. 
	- Simplicity is one of its main advantages. It supports fewer than a dozen operations, so implementation and debugging of libraries are much easier. It also means that the protocol layer does not add much performance overhead. 
	- But interoperability can be limited because there is no standard way of doing certain things. A good example of impaired is message prefetch count. Prefetch is a great way of increasing throughput because messages are received in batches instead of one message at a time. Although both RabbitMQ and ActiveMQ support this feature, they both implement it using different custom STOMP headers. 
* JMS
	- A good feature set and is popular
	- Your ability to integrate with non-JVM-based languages will be very limited. 

#### Metrics to decide which message broker to use <a id="backend-message-queue-metrics"></a>
* Number of messages published per second
* Average message size
* Number of messages consumed per second (this can be much higher than publishing rate, as multiple consumers may be subscribed to receive copies of the same message)
* Number of concurrent publishers
* Number of concurrent consumers
* If message persistence is needed (no message loss during message broker crash)
* If message acknowledgement is need (no message loss during consumer crash)

#### Challenges <a id="backend-message-queue-challenges"></a>
* No message ordering: Messages are processed in parallel and there is no synchronization between consumers. Each consumer works on a single message at a time and has no knowledge of other consumers running in parallel to it. Since your consumers are running in parallel and any of them can become slow or even crash at any point in time, it is difficult to prevent messages from being occasionally delivered out of order. 
	- Solutions:
		+ Limit the number of consumers to a single thread per queue
		+ Build the system to assume that messages can arrive in random order
		+ Use a messaging broker that supports partial message ordering guarantee. 
	- It is best to depend on the message broker to deliver messages in the right order by using partial message guarantee (ActiveMQ) or topic partitioning (Kafka). If your broker does not support such functionality, you will need to ensure that your application can handle messages being processed in an unpredictable order.
		+ Partial message ordering is a clever mechanism provided by ActiveMQ called message groups. Messages can be published with a special label called a message group ID. The group ID is defined by the application developer. Then all messages belonging to the same group are guaranteed to be consumed in the same order they were produced. Whenever a message with a new group ID gets published, the message broker maps the new group Id to one of the existing consumers. From then on, all the messages belonging to the same group are delivered to the same consumer. This may cause other consumers to wait idly without messages as the message broker routes messages based on the mapping rather than random distribution. 
	- Message ordering is a serious issue to consider when architecting a message-based application, and RabbitMQ, ActiveMQ and Amazon SQS messaging platform cannot guarantee global message ordering with parallel workers. In fact, Amazon SQS is known for unpredictable ordering messages because their infrastructure is heavily distributed and ordering of messages is not supported. 
* Message requeueing
	- By allowing messages to be delivered to your consumers more than once, you make your system more robust and reduce constraints put on the message queue and its workers. For this approach to work, you need to make all of your consumers idempotent. 
		+ But it is not an easy thing to do. Sending emails is, by nature, not an idempotent operation. Adding an extra layer of tracking and persistence could help, but it would add a lot of complexity and may not be able to handle all of the faiulres. 
		+ Idempotent consumers may be more sensitive to messages being processed out of order. If we have two messages, one to set the product's price to $55 and another one to set the price of the same product to $60, we could end up with different results based on their processing order. 
* Race conditions become more likely
* Risk of increased complexity
	- When integrating applications using a message broker, you must be very diligent in documenting dependencies and the overarching message flow. Without good documentation of the message routes and visibility of how the message flow through the system, you may increase the complexity and make it much harder for developers to understand how the system works. 

### MySQL <a id="backend-MySQL"></a>
#### Replication <a id="backend-MySQL-replication"></a>
#### When to use <a id="backend-MySQL-replication-when-to-use"></a>
* Scale reads: Instead of a single server having to respond to all the queries, you can have many clones sharing the load. You can keep scaling read capacity by simply adding more slaves. And if you ever hit the limit of how many slaves your master can handle, you can use multilevel replication to further distribute the load and keep adding even more slaves. By adding multiple levels of replication, your replication lag increases, as changes need to propogate through more servers, but you can increase read capacity. 
* Scale the number of concurrently reading clients and the number of queries per second: If you want to scale your database to support 5,000 concurrent read connections, then adding more slaves or caching more aggressively can be a great way to go.

#### When not to use <a id="replication-when-not-to-use"></a>
* Scale writes: No matter what topology you use, all of your writes need to go through a single machine. 
* Not a good way to scale the overall data set size: If you want to scale your active data set to 5TB, replication would not help you get there. The reason why replication does not help in scaling the data set size is that all of the data must be present on each of the machines. The master and each of its slave need to have all of the data. 
	- Def of active data set: All of the data that must be accessed frequently by your application. (all of the data your database needs to read from or write to disk within a time window, like an hour, a day, or a week.)
	- Size of active data set: When the active data set is small, the database can buffer most of it in memory. As your active data set grows, your database needs to load more disk blocks because in-memory buffers are not large enough to contain enough of the active disk blocks. 
	- Access pattern of data set
		+ Like a time-window: In an e-commerce website, you use tables to store information about each purchase. This type of data is usually accessed right after the purchase and then it becomes less and less relevant as time goes by. Sometimes you may still access older transactions after a few days or weeks to update shipping details or to perform a refund, but after that, the data is pretty much dead except for an occasional report query accessing it.
		+ Unlimited data set growth: A website that allowed users to listen to music online, your users would likely come back every day or every week to listen to their music. In such case, no matter how old an account is, the user is still likely to log in and request her playlists on a weekly or daily basis. 

#### Replication consistency <a id="backend-MySQL-replication-consistency"></a>
* Def: Slaves could return stale data. 
* Reason: 
	- Replication is usually asynchronous, and any change made on the master needs some time to replicate to its slaves. Depending on the replication lag, the delay between requests, and the speed of each server, you may get the freshest data or you may get stale data. 
* Solution:
	- Send critical read requests to the master so that they would always return the most up-to-date data.
	- Cache the data that has been written on the client side so that you would not need to read the data you have just written. 
	- Minize the replication lag to reduce the chance of stale data being read from stale slaves.

#### Master-slave vs peer-to-peer <a id="backend-MySQL-replication-types"></a>

|     Types    |    Strengths     |      Weakness       | 
| ------------ |:----------------:|:-------------------:|
| Master-slave | <ul><li>Helpful for scaling when you have a read-intensive dataset. Can scale horizontally to handle more read requests by adding more slave nodes and ensuring that all read requests are routed to the slaves.</li><li>Helpful for read resilience. Should the master fail, the slaves can still handle read requests.</li><li>Increase availability by reducing the time needed to replace the broken database. Having slaves as replicas of the master does speed up recovery after a failure of the master since a slave can be appointed a new master very quickly. </li></ul> | <ul><li>Not a good scheme for datasets with heavy write traffic, although offloading the read traffic will help a little bit with handling the write load. All of your writes need to go through a single machine </li><li>The failure of the master does eliminate the ability to handle writes until either the master is restored or a new master is appointed.</li><li>Inconsistency. Different clients reading different slaves will see different values because the changes haven't all propagated to the slaves. In the worst case, that can mean that a client cannot read a write it just made. </li></ul> | 
| p2p: Master-master |  <ul><li> Faster master failover. In case of master A failure, or anytime you need to perform long-lasting maintainence, your application can be quickly reconfigured to direct all writes to master B.</li><li>More transparent maintainance. Switch between groups with minimal downtime.</li></ul>| 	Not a viable scalability technique. <ul><li>Need to use auto-increment and UUID() in a specific way to make sure you never end up with the same sequence number being generated on both masters at the same time.</li><li>Data inconsistency. For example, updating the same row on both masters at the same time is a classic race condition leading to data becoming inconsistent between masters.</li><li>Both masters have to perform all the writes. Each of the master needs to execute every single write statement either coming from your application or via the replication. To make it worse, each master will need to perform additional I/O to write replicated statements into the relay log.</li><li> Both masters have the same data set size. Since both masters have the exact same data set, both of them will need more memory to hold ever-growing indexes and to keep enough of the data set in cache.</li></ul> | 
| p2p: Ring-based    | Chain three or more masters together to create a ring. | <ul><li> All masters need to execute all the write statements. Does not help scale writes.</li><li> Reduced availability and more difficult failure recovery: Ring topology makes it more difficult to replace servers and recover from failures correctly. </li><li>Increase the replication lag because each write needs to jump from master to master until it makes a full circle.</li></ul> | 

#### Master-slave replication <a id="replication-mysql-master-slave"></a>
* Responsibility: 
	- Master is reponsible for all data-modifying commands like updates, inserts, deletes or create table statements. The master server records all of these statements in a log file called a binlog, together with a timestamp, and a sequence number to each statement. Once a statement is written to a binlog, it can then be sent to slave servers. 
	- Slave is responsible for all read statements.
* Replication process: The master server writes commands to its own binlog, regardless if any slave servers are connected or not. The slave server knows where it left off and makes sure to get the right updates. This asynchronous process decouples the master from its slaves - you can always connect a new slave or disconnect slaves at any point in time without affecting the master.
	1. First the client connects to the master server and executes a data modification statement. The statement is executed and written to a binlog file. At this stage the master server returns a response to the client and continues processing other transactions. 
	2. At any point in time the slave server can connect to the master server and ask for an incremental update of the master' binlog file. In its request, the slave server provides the sequence number of the last command that it saw. 
	3. Since all of the commands stored in the binlog file are sorted by sequence number, the master server can quickly locate the right place and begin streaming the binlog file back to the slave server.
	4. The slave server then writes all of these statements to its own copy of the master's binlog file, called a relay log.
	5. Once a statement is written to the relay log, it is executed on the slave data set, and the offset of the most recently seen command is increased.  

##### Number of slaves <a id="backend-MySQL-replication-master-slave-number-of-slaves"></a>
* It is a common practice to have two or more slaves for each master server. Having more than one slave machine have the following benefits:
	- Distribute read-only statements among more servers, thus sharding the load among more servers
	- Use different slaves for different types of queries. E.g. Use one slave for regular application queries and another slave for slow, long-running reports.
	- Losing a slave is a nonevent, as slaves do not have any information that would not be available via the master or other slaves.

##### Failure recovery <a id="backend-MySQL-replication-master-slave-failure-recovery"></a>
* Failure recovery
	- Slave failure: Take it out of rotation, rebuild it and put it back.
	- Master failure: If simply restart does not work, 
		+ First find out which of your slaves is most up to date. 
		+ Then reconfigure it to become a master. 
		+ Finally reconfigure all remaining slaves to replicate from the new master.

### Sharding <a id="backend-MySQL-sharding"></a>
#### Benefits <a id="backend-MySQL-sharding-benefits"></a>
* Scale horizontally to any size. Without sharding, sooner or later, your data set size will be too large for a single server to manage or you will get too many concurrent connections for a single server to handle. You are also likely to reach your I/O throughput capacity as you keep reading and writing more data. By using application-level sharing, none of the servers need to have all of the data. This allows you to have multiple MySQL servers, each with a reasonable amount of RAM, hard drives, and CPUs and each of them being responsible for a small subset of the overall data, queries, and read/write throughput.
* Since sharding splits data into disjoint subsets, you end up with a share-nothing architecture. There is no overhead of communication between servers, and there is no cluster-wide synchronization or blocking. Servers are independent from each other because they shared nothing. Each server can make authoritative decisions about data modifications 
* You can implement in the application layer and then apply it to any data store, regardless of whether it supports sharding out of the box or not. You can apply sharding to object caches, message queues, nonstructured data stores, or even file systems. 

#### Types <a id="backend-MySQL-sharding-types"></a>
- Vertical sharding
- Horizontal sharding
	+ Range partitioning (used in HBase)
		* Easy to define but hard to predict
	+ Hash partitioning
		* Evenly distributed but need large amount of data migration when the number of server changes and rehashing
	+ Consistent hashing (murmur3 -2^128, 2^128)
		* Less data migration but hard to balance node 
		* Unbalanced scenario 1: Machines with different processing power/speed.
		* Unbalanced scenario 2: Ring is not evenly partitioned. 
		* Unbalanced scenario 3: Same range length has different amount of data.
	+ Virtual nodes (Used in Dynamo and Cassandra)
		* Solution: Each physical node associated with a different number of virtual nodes.
		* Problems: Data should not be replicated in different virtual nodes but the same physical nodes.

#### Challenges <a id="backend-MySQL-sharding-challenges"></a>
* Cannot execute queries spanning multiple shards. Any time you want to run such a query, you need to execute parts of it on each shard and then somehow merge the results in the application layer.
	- It is pretty common that running the same query on each of your servers and picking the highest of the values will not guarantee a correct result.
* Lose the ACID properties of your database as a whole.
	- Maintaining ACID properties across shards requires you to use distributed transactions, which are complex and expensive to execute (most open-source database engines like MySQL do not even support distributed transactions).
* Depending on how you map from sharding key to the server number, it might be difficult to add more servers.
	- Solution0: Modulo-based mapping. As the total number of servers change, most of the user-server mappings change.
	- Solution1: Keep all of the mappings in a separate database. Rather than computing server number based on an algorithm, we could look up the server number based on the sharding key value. 
		+ The benefit of keeping mapping data in a database is that you can migrate users between shards much more easily. You do not need to migrate all of the data in one shot, but you can do it incrementally, one account at a time. To migrate a user, you need to lock its account, migrate the data, and then unlock it. You could usually do these migrations at night to reduce the impact on the system, and you could also migrate multiple accounts at the same time.
		+ Additionaly flexibility, as you can cherry-pick users and migrate them to the shards of your choice. Depending on the application requirements, you could migrate your largest or busiest clients to separate dedicated database instances to give them more capacity. 
	- Solution2: Map to logical database rather than physical database.
* Challenge:
	- It may be harder to generate an identifier that would be unique across all of the shards. Some data stores allow you to generate globally unique IDs, but since MySQL does not natively support sharding, your application may need to enforce these rules as well. 
		+ If you do not care how your unique identifiers look, you can use MySQL auto-increment with an offset to ensure that each shard generates different numbers. To do that on a system with two shards, you would set auto_increment_increment = 2 and auto_increment_offset = 1 on one of them and auto_increment_increment = 2 and auto_increment_offset = 2 on the other. This way, each time auto-increment is used to generate a new value, it would generate even numbers on one server and odd numbers on the other. By using that trick, you would not be able to ensure that IDs are always increasing across shards, since each server could have a different number of rows, but usually that is not be a serious issue.
		+ Use atomic counters provided by some data stores. For example, if you already use Redis, you could create a counter for each unique identifier. You would then use Redis' INCR command to increase the value of a selected counter and return it with a different value. 

## NoSQL <a id="backend-NoSQL"></a>
### Features<a id="backend-NoSQL-features"></a>
* There is no generally accepted definition. All we can do is discuss some common characteristics of the databases that tend to be called "NoSQL".

|       Database        |        SQL    |     NoSQL    |  
| --------------------- |:-------------:| ------------:| 
|     Data uniformness  | Uniform data. Best visualized as a set of tables. Each table has rows, with each row representing an entity of interest. Each row is described through columns. One row cannot be nested inside another. | Non-uniform data. NoSQL databases recognize that often, it is common to operate on data in units that have a more complex structure than a set of rows. This is particularly useful in dealing with nonuniform data and custom fields. NoSQL data model can be put into four categories: key-value, document, column-family and graph. |
|     Schema change     | Define what table exists, what column exists, what data types are. Although actually relational schemas can be changed at any time with standard SQL commands, it is of hight cost. | Changing schema is casual and of low cost. Essentially, a schemaless database shifts the schema into the application code. |
|    Query flexibility  | Low cost on changing query. It allows you to easily look at the data in different ways. Standard SQL supports things like joins and subqueries. | High cost in changing query. It does not allow you to easily look at the data in different ways. NoSQL databases do not have the flexibility of joins or subqueries. |
|    Transactions       | SQL has ACID transactions (Atomic, Consistent, Isolated, and Durable). It allows you to manipulate any combination of rows from any tables in a single transaction. This operation either succeeds or fails entirely, and concurrent operations are isolated from each other so they cannot see a partial update. | Graph database supports ACID transactions. Aggregate-oriented databases do not have ACID transactions that span multiple aggregates. Instead, they support atomic manipulation of a single aggregate at a time. If we need to manipulate multiple aggregates in an atomic way, we have to manage that ourselves in application code. An aggregate structure may help with some data interactions but be an obstacle for others.  |
|    Consistency        | Strong consistency  |  Trade consistency for availability or partition tolerance. Eventual consistency |
|    Scalability      | elational database use ACID transactions to handle consistency across the whole database. This inherently clashes with a cluster environment |  Aggregate structure helps greatly with running on a cluster. It we are running on a cluster, we need to minize how many nodes we need to query when we are gathering data. By using aggregates, we give the database important information about which bits of data (an aggregate) will be manipulated together, and thus should live on the same node. | 
|    Performance        | MySQL/PosgreSQL ~ 1k QPS  |  MongoDB/Cassandra ~ 10k QPS. Redis/Memcached ~ 100k ~ 1M QPS |
|    Maturity           | Over 20 years. Integrate naturally with most web frameworks. For example, Active Record inside Ruby on Rails | Usually less than 10 years. Not great support for serialization and secondary index |


# Typical system design workflow <a id="workflow"></a>
## Scenarios <a id="workflow-scenario"></a>
### Features <a id="workflow-scenario-features"></a>
#### Common features <a id="workflow-scenario-common-features"></a>
* User system
	- Register/Login
	- User profile display/Edit
* Search

### Design goals <a id="workflow-scenario-design-goals"></a>
 
### Metrics <a id="workflow-scenario-metrics"></a>
* Monthly active user
* Daily active user
* QPS
	- Average QPS
	- Peak QPS
	- Future QPS
	- Read QPS
	- Write QPS	

## Service <a id="workflow-service"></a>

## Storage <a id="workflow-storage"></a>
### MySQL index <a id="workflow-storage-mysql-index"></a>



## Scale <a id="workflow-scale"></a>

### Front-end layer <a id="front-end-layer"></a>
#### Manage HTTP sessions <a id="manage-http-sessions"></a>
* Since the HTTP protocol is stateless itself, web applications developed techniques to create a concept of a session on top of HTTP so that servers could recognize multiple requests from the same user as parts of a more complex and longer lasting sequence. 
* Any data you put in the session should be stored outside of the web server itself to be available from any web server. 
	- Store session state in cookies
		+ Advantage: You do not have to store the sesion state anywhere in your data center. The entire session state is being handed to your web server with every web request, thus making your application stateless in the context of the HTTP session. 
		+ Disadvantage: Session storage can becomes expensive. Cookies are sent by the browser with every single request, regardless of the type of resource being requested. As a result, all requests within the same cookie domain will have session storage appended as part of the request. 
		+ Use case: When you can keep your data minimal. If all you need to keep in session scope is userID or some security token, you will benefit from the simplicity and speed of this solution. Unfortunately, if you are not careful, adding more data to the session scope can quickly grow into kilobytes, making web requests much slower, especially on mobile devices. The coxt of cookie-based session storage is also amplified by the fact that encrypting serialized data and then Based64 encoding increases the overall byte count by one third, so that 1KB of session scope data becomes 1.3KB of additional data transferred with each web request and web response. 
	- Delegate the session storage to an external data store: Your web application would take the session identifier from the web request and then load session data from an external data store. At the end of the web request life cycle, just before a response is sent back to the user, the application would serialize the session data and save it back in the data store. In this model, the web server does not hold any of the session data between web requests, which makes it stateless in the context of an HTTP session. 
		+ Many data stores are suitable for this use case, for example, Memcached, Redis, DynamoDB, or Cassandra. The only requirement here is to have very low latency on get-by-key and put-by-key operations. It is best if your data store provides automatic scalability, but even if you had to do data partitioning yourself in the application layer, it is not a problem, as sessions can be partitioned by the session ID itself. 
	- Use a load balancer that supports sticky sessions: The load balancer needs to be able to inspect the headers of the request to make sure that requests with the same session cookie always go to the server that initially the cookie.
		+ Sticky sessions break the fundamental principle of statelessness, and I recommend avoiding them. Once you allow your web servers to be unique, by storing any local state, you lose flexibility. You will not be able to restart, decommission, or safely auto-scale web servers without braking user's session because their session data will be bound to a single physical machine. 

### Cache <a id="workflow-scale-cache"></a>
#### Cache hit ratio <a id="workflow-scale-cache-hit-ratio"></a>
* Size of cache key space
    - The more unique cache keys your application generates, the less chance you have to reuse any one of them. Always consider ways to reduce the number of possible cache keys. 
* The number of items you can store in cache
	- The more objects you can physically fit into your cache, the better your cache hit ratio.
* Longevity
	- How long each object can be stored in cache before expiring or being invalidated. 

#### Cache based on HTTP <a id="workflow-scale-cache-based-on-HTTP"></a>
* All of the caching technologies working in the HTTP layer work as read-through caches
	- Procedures
		+ First Client 1 connects to the cache and request a particular web resource.
	    + Then the cache has a change to intercept the request and respond to it using a cached object.
	    + Only if the cache does not have a valid cached response, will it connect to the origin server itself and forward the client's request. 
	- Advantages: Read-through caches are especially attractive because they are transparent to the client. This pluggable architecture gives a lot of flexibility, allowing you to add layers of caching to the HTTP stack without needing to modify any of the clients.

##### HTTP Caching headers <a id="workflow-scale-cache-HTTP-caching-headers"></a>
* Conditional gets: If-Modified-Since header in the get request
	- If the server determines that the resource has been modified since the date given in this header, the resource is returned as normal. Otherwise, a 304 Not Modified status is returned. 
	- Use case: Rather than spending time downloading the resource again, the browser can use its locally cached copy. When downloading of the resource only forms only a small fraction of the request time, it doesn't have much benefit.
* max-age inside Expires and Cache-Contrl: The resource expires on such-and-such a date. Until then, you can just use your locally cached copy. 
	- The main difference is that Expires was defined in HTTP 1.0, whereas the Cache-Control family is new to HTTP 1.1. So, in theory, Expires is safer because you occasionally still encounter clients that support only HTTP 1.0. Although if both are presents, preferences are given to Cache-Control: max-age. 
	- Choosing expiration policies:
		+ Images, CSS, Javascript, HTML, Flash movies are primary candidates. The only type of resources you don't usually want to cache is dynamically generated content created by server-side scripting languages such as PHP, Perl and Ruby. Usually one or two months seem like a good figure.
	- Coping with stale content: There are a few tricks to make the client re-request the resource, all of which revolved around changing the URL to trick the browser into thinking the resource is not cached. 
		+ Use a version/revision number or date in the filename
		+ Use a version/revision number or date in the path
		+ Append a dummy query string
* Other headers inside Cache-Control:
	- private: The result is specific to the user who requested it and the response cannot be served to any other user. This means that only browsers will be able to cache this response because intermediate caches would not have the knowledge of what identifies a user.
	- public: Indicates the response can be shared between users as long as it has not expired. Note that you cannot specify private and public options together; the response is either public or private.
	- no-store: Indicates the response should not be stored on disks by any of the intermediate caches. In other words, the response can be cached in memory, but it will not be persisted to disk.
	- no-cache: The response should not be cache. To be accurate, it states that the cache needs to ask the server whether this response is still valid every time users request the same resource.
	- max-age: Indicates how many seconds this response can be served from the cache before becoming stale. (TTL of the response)
	- s-maxage: Indicates how many seconds this response can be served from the cache before becoming stale on shared caches. 
	- no-transformation: Indicates the response should be served without any modifications. For example, a CDN provider might transcode images to reduce their size, lowering the quality or changing the compression algorithm. 
	- must-revalidate: Once the response becomes stale, it cannot be returned to clients without revalidation. Although it may seem odd, caches may return stale objects under certain conditions. For example, if the client explicitly allows it or if the cache loses connection to the original server. 
* Expires:
	- Allows you to specify an absolute point in time when the object becomes stale. 
	- Some of the functionality controlled by the Cache-Control header overlaps that of other HTTP headers. Expiration time of the web response can be defined either by Cache-Control: max-age=600 or by setting an absolute expiration time using the Expires header. Including both of these headers in the response is redundant and leads to confusion and potentially inconsistent behavior. 
* Vary:
	- Tell caches that you may need to generate multiple variations of the response based on some HTTP request headers. For example: Vary:Accept-Encoding is the most common Vary header indicating that you may return responses encoded in different ways depending on the Accept-Encoding header that the client sends to your web server. Some clients who accept gzip encoding will get a compressed response, where others who cannot support gzip will get an uncompressed response. 
* How not to cache: 
	- It's common to see meta tags used in the HTML of pages to control caching. This is a poor man's cache control technique, which isn't terribly effective. Although most browsers honor these meta tags when caching locally, most intermediate proxies do not. 

##### Types of HTTP cache technologies <a id="workflow-scale-cache-types-of-HTTP-cache-technologies"></a>
* Browser cache
	- Browsers have built-in caching capabilities to reduce the number of request sent out. These usually uses a combination of memory and local files.
	- There are several problems with browser cache
		+ The size of the cache tends to be quite small by default. Usually around 1GB. Given that web pages have become increasingly heavy, browsers would probably be more effective if they defaulted to much larger caches.
		+ When the cache becomes full, the algorithm to decide what to remove is crude. Commonly, the LRU algorithm is used to purge old items. It fails to take into account the relative "cost" to request different types of resources. For example, the loading of Javascript resources typically blocks loading of the rest of the page. It makes more sense for these to be given preference in the cache over, say, images. 
		+ Many browsers offer an easy way for the user to remove temporary data for the sake of privacy. Users often feel that cleaning the browser cache is an important step in somehow stopping their PC from running slow.
* Caching proxies
	- A caching proxy is a server, usually installed in a local corporate network or by the Internet service provider (ISP). It is a read-through cache used to reduce the amount of traffic generated by the users of the network by reusing responses between users of the network. The larger the network, the larger the potential savings - that is why it was quite common among ISPs to install transparent caching proxies and route all of the HTTP traffic through them to cache as many requests as possible. 
	- In recent years, the practice of installing local proxy servers has become less popular as bandwidth has become cheaper and as it becomes more popular for websiste to serve their resources soley over the Secure Socket Layer. 
* Reverse proxy
	- A reverse proxy works in the exactly same way as a regular caching proxy, but the intent is to place a reverse proxy in your own data center to reduce the load put on your web servers. 
	- Purpose: 
		+ For caching, they can be used to lighten load on the back-end server by serving up cached versions of dynamically generated pages (thus cuttping CPU usage). Using reverse proxies can also give you more flexibility because you can override HTTP headers and better control which requests are being cached and for how long. 
		+ For load balancing, they can be used for load-balancing multiple back-end web servers. 
* Content delivery networks
	- A CDN is a distributed network of cache servers that work in similar way as caching proxies. They depend on the same HTTP headers, but they are controlled by the CDN service provider. 
	- Advantage: 
		+ Reduce the load put on your servers
		+ Save network bandwidth
		+ Improve the user experience because by pushing content closer to your users. 
	- Procedures: Web applications would typically use CDN to cache their static files like images, CSS, JavaScript, videos or PDF. 
		+ You can imlement it easily by creating a static subdomain and generate URLs for all of your static files using this domain
		+ Then you configure the CDN provider to accept these requests on your behalf and point DNS for s.example.org to the CDN provider. 
		+ Any time CDN fails to serve a piece of content from its cache, it forwards the request to your web servers and caches the response for subsequent users. 

##### A few common caching scenarios <a id="workflow-scale-cache-scenarios"></a>	
* The first and best scenario is allowing your clients to cache a response forever. This is a very important technique and you want to apply it for all of your static content (like image, CSS, or Javascript files). Static content files should be considered immutable, and whenever you need to make a change to the contents of such a file, you should publish it under a new URL. Want you want to deploy a new version of your web application, you can bundle and minify all of your CSS files and include a timestamp or a hash of the contents of the file in the URL. Even though you could cache static files forever, you should not set the Expires header more than one year into the future. 
* The second most common scenario is the worst case - when you want to make sure that the HTTP response is never stored, cached, or reused for any users. 
* A last use case is for situations where you want the same user to reuse a piece of content, but at the same time you do not want other users to share the cached response. 

##### Scaling HTTP caches <a id="workflow-scale-cache-scale-http-caches"></a>
* Do not worry about the scalability of browser caches or third-party proxy servers. 
* This usually leaves you to manage reverse proxy servers. For most young startups, a single reverse proxy should be able to handle the incoming traffic, as both hardware reverse proxies and leading open-source ones can handle more than 10,000 requests per second from a single machine. 
	- First step: To be able to scale the reverse proxy layer efficiently, you need to first focus on your cache hit ratio first. 
	    + Cache key space: Describe how many distinct URLs your reverse proxies will observe in a period of time. The more distinct URLs are served, the more memory or storage you need on each reverse proxy to be able to serve a significant portion of traffic from cache. Avoid caching responses that depend on the user (for example, that contain the user ID in the URL). These types of response can easily pollute your cache with objects that cannot be reused.
	    + Average response TTL: Describe how long each response can be cached. The longer you cache objects, the more chance you have to reuse them. Always try to cache objects permanently. If you cannot cache objects forever, try to negotiate the longest acceptable cache TTL with your business stakeholders. 
     	+ Average size of cached object: Affects how much memory or storage your reverse proxies will need to store the most commonly accessed objects. Average size of cached object is the most difficult to control, but you should still keep in mind because there are some techniques that help you "shrink" your objects. 
    - Second step: Deploying multiple reverse proxies in parallel and distributing traffic among them. You can also scale reverse proxies vertically by giving them more memory or switching their persistent storage to solid-state drive. 

#### Cache for application objects <a id="workflow-scale-cache-application-objects"></a>
* Application object caches are mostly cache-aside caches. The application needs to be aware of the existence of the object cache, and it actively uses it to store and retrieve objects rather than the cache being transparently positioned between the application and its data sources.
* All of the object cache types discussed in this section can be imagined as key-value stores with support of object expiration. 

##### Types of application objects cache <a id="workflow-scale-types-of-application-objects"></a>
* Client-side caches
	- Web storage allows a web application to use a limited amount (usually up to 5MB to 25MB of data). 
	- Web storage works as a key-value store. 
* Caches co-located with code: One located directly on your web servers. 
	- Objects are cached directly in the application's memory
	- Objects are stored in shared memory segments so that multiple processes running on the same machine could access them. 
	- A caching server is deployed on each web server as a separate application. 
* Distributed object caches 
	- Interacting with a distributed object cache usually requires a network round trip to the cache server. On the plus side, distributed object caches usually work as simple key-value stores, allowing clients to store data in the cache. You can scale simply by adding more servers to the cache cluster. By adding servers, you can scale both the throughput and overall memory pool of your cache. 

##### Scaling object caches <a id="scaling-object-caches"></a>
* Client-side caches like web browser storage cannot be scaled. 
* The web server local caches are usually scaled by falling back to the file system. 
* Distributed caches are usually scaled by data partitioning. Adding read-only slaves to sharded node. 

#### Caching rules of thumb <a id="caching-rules-of-thumb"></a>
##### Cache priority <a id="workflow-scale-cache-priority"></a>
* The higher up the call stack you can cache, the more resources you can save. 

##### Cache reuse <a id="workflow-scale-cache-reuse"></a>
* Always try to reuse the same cached object for as many requests/users as you can.

##### Where to start caching <a id="workflow-scale-cache-where-to-start"></a>
* Aggregated time spent = time spent per request * number of requests

##### Cache invalidation <a id="workflow-scale-cache-invalidation"></a>
* LRU
* TTL

### Consistency <a id="workflow-scale-consistency"></a>
#### Update consistency <a id="workflow-scale-update-consistency"></a>
* Def: Write-write conflicts occur when two clients try to write the same data at the same time. Result is a lost update. 
* Solutions: 
	- Pessimistic approach: Preventing conflicts from occuring.
		+ The most common way: Write locks. In order to change a value you need to acquire a lock, and the system ensures that only once client can get a lock at a time. 
	- Optimistic approach: Let conflicts occur, but detects them and take actions to sort them out.
		+ The most common way: Conditional update. Any client that does an update tests the value just before updating it to see if it is changed since his last read. 
		+ Save both updates and record that they are in conflict. This approach usually used in version control systems. 
* Problems of the solution: Both pessimistic and optimistic approach rely on a consistent serialization of the updates. Within a single server, this is obvious. But if it is more than one server, such as with peer-to-peer replication, then two nodes might apply the update in a different order.
* Often, when people first encounter these issues, their reaction is to prefer pessimistic concurrency because they are determined to avoid conflicts. Concurrent programming involves a fundamental tradeoff between safety (avoiding errors such as update conflicts) and liveness (responding quickly to clients). Pessimistic approaches often severly degrade the responsiveness of a system to the degree that it becomes unfit for its purpose. This problem is made worse by the danger of errors such as deadlocks. 

#### Read consistency <a id="workflow-scale-read-consistency"></a>
* Def: Read-write conflicts occur when one client reads inconsistent data in the middle of another client's write.
* Types:
	- Logical consistency: Ensuring that different data items make sense together. 
		+ Example: 
			* Martin begins update by modifying a line item
			* Pramod reads both records
			* Martin completes update by modifying shipping charge
	- Replication consistency: Ensuring that the same data item has the same value when read from different replicas. 
		+ Example: 
			* There is one last hotel room for a desirable event. The reservation system runs onmany nodes. 
			* Martin and Cindy are a couple considering this room, but they are discussing this on the phone because Martin is in London and Cindy is in Boston. 
			* Meanwhile Pramod, who is in Mumbai, goes and books that last room. 
			* That updates the replicated room availability, but the update gets to Boston quicker than it gets to London. 
			* When Martin and Cindy fire up their browsers to see if the room is available, Cindy sees it booked and Martin sees it free. 
	- Read-your-write consistency (Session consistency): Once you have made an update, you're guaranteed to continue seeing that update. This can be difficult if the read and write happen on different nodes. 
		+ Solution1: A sticky session. a session that's tied to one node. A sticky session allows you to ensure that as long as you keep read-your-writes consistency on a node, you'll get it for sessions too. The downsides is that sticky sessions reduce the ability of the load balancer to do its job. 
		+ Solution2: Version stamps and ensure every interaction with the data store includes the latest version stamp seen by a session. 

#### Tradeoffs between availability and consistency <a id="workflow-scale-tradeoff-availability-consistency"></a>
* CAP theorem: if you get a network partition, you have to trade off consistency versus availability. 
	- Consistency: Every read would get the most recent write. 
	- Availability: Every request received by the nonfailing node in the system must result in a response. 
	- Partition tolerance: The cluster can survive communication breakages in the cluster that separate the cluster into multiple partitions unable to communicate with each other. 

#### Tradeoffs between latency and durability <a id="workflow-scale-tradeoff-latency-durability"></a>

# Cassandra <a id="cassandra"></a>
* Cassandra is a data store that was originally built at Facebook and could be seen as a merger of design patterns borrowed from BigTable and Dynamo. Cassandra is one of the clear leaders when it comes to ease of management, scalability, and self-healing, but it is important to remember that everything has its price. The main challenges that come with operating Cassandra are that it is heavily specialized, and it has a very particular data model, and it is an eventually consistent data store. 
* You can work around eventual conisstency by using quorum reads and writes, but the data model and tradeoffs made by the designers can often come as a surprise. Anything that you might have learned about relational databases is pretty much invalid when you work with NoSQL data stores like Cassandra. It is easy to get started with most NoSQL data stores, but to be able to operate them at scale takes much more experience and understanding of their internal structure than you might expect. 
	- For example, even though you can read in the open-source community that "Cassandra loves writes", deletes are the most expensive type of operation you can perform in Cassandra, which can come as a big suprise. Most people would not expect that deletes would be expensive type of operation you can perform in Cassandra. Cassandra uses append-only data structures, which allows it to write inserts with astonishing efficiency. Data is never overwritten in place and hard disks never have to perform random write operations, greatly increasing write throughput. But that feature, together with the fact that Cassandra is an eventually consistent datastore , forces deletes and updates to be internally persisted as inserts as well. As a result, some use cases that add and delete a lot of data can become inefficient because deletes increase the data set size rather than reducing it ( until the compaction process cleans them up ). 
	- A great example of how that can come as a surprise is a common Cassandra anti-pattern of a queue. You could model a simple first-in-first-out queue in Cassandra by using its dynamic columns. You add new entries to the queue by appending new columns, and you remove jobs from the queue by deleting columns. With a small scale and low volume of writes, this solution seems to work perfectly, but as you keep adding and deleting columns, your performance will begin to degrade dramatically. Although both inserts and deletes are perfectly fine and Cassandra purges old deleted data using its background compaction mechanism, it does not particularly like workloads with a such high rate of deletes (in this case, 50 percent of the operations are deletes).

## Data model <a id="cassandra-data-model"></a>
* Level1: row_key
	- Namely hashkey
	- Could not perform range query
* Level2: column_key
	- Already sorted, could perform range query
	- Could be compound value (e.g. timestamp + user_id)
* Level3: value
	- In general it is String
	- Could use custom serialization or avaible ones such as Protobuff/Thrift.

## Features <a id="cassandra-features"></a>
* Horizontal scalability / No single point of failure (peer to peer)
	- The more servers you add, the more read and write capacity you get. And you can easily scale in and out depending on your needs. In addition, since all of the topology is hidden from the clients, Cassandra is free to move data around. As a result, adding new servers is as easy as starting up a new node and telling it to join the cluster. 
	- All of its nodes perform the exact same functions. Nodes are functionally equal but each node is responsible for different data set. 
		+ Does not have a single point of failure.
		+ Automatic data partitioning.

* Write optimization: When a write is received by Cassandra, the data is first recorded in a commit log (append-only log), then written to an in-memory structure known as memtable. A write operation is only considered successful once it's written to the commit log and the memtable. Writes are batched in memory and periodically written out to structures know as SSTable. SSTable are not written to again after they are flushed; if there are changes to the data, a new SSTable is written. Unused SSTables are reclaimed by compaction.
	- Commit log: 
		+ Write to it before into db
	- Memtable:
		+ In memory
		+ Value will be added to memtable after commit log
	- SSTable
		+ Write after memtable is full
		+ Append only / Sequential write
	- Compaction 

* Tunable consistency
	- Consistency can be increased by reducing the availability level of request. 
	- Formula: R + W > N. Tune the availability by changing the R and W values for a fixed value of N.
		+ R: Minimum number of nodes that must respond successfully to a read
		+ W: Minimum number of nodes that must respond successfully to a write
		+ N: The number of nodes participating in the replication of data. Configured during keyspace creation.
	- Levels:
		+ ONE
			* Write: Write to one node's commit log and return a response to the client. Good when have high write requirements and do not mind if some writes are lost.
			* READ: Return data from the first replica. If the data is stale, subsequent reads will get the latest data; this process is called read repair. Good when you have high read requirements and do not mind if you get stale data.
		+ QUORUM: Similar to one, but the majority of the nodes need to respond to read/write requests.
	    + ALL: All nodes have to respond to reads or writes, which will make the cluster not tolerant to faults - even when one node is down, the write or read is blocked and reported as a failure. 

* Highly automated and little administration 
	- Replacing a failed node does not require complex backup recovery and replication offset tweaking, as often happens in MySQL. All you need to do to replace a broken server is add a new one and tell Cassandra which IP address this new node is replacing. Since each piece of data is stored on multiple servers, the cluster is fully operational throughout the server replacement procedure. Clients can read and write any data they wish even when one server is broken or being replaced. 

## Read/Write process <a id="cassandra-read-write-process"></a> 
* Clients can connect to any server no matter what data they intend to read or write. 
* Clients then issue queries to the coordinator node they chose without any knowledge about the topology or state of the cluster.
* Each of the Cassandra nodes knows the status of all other nodes and what data they are responsible for. They can delegate queries to the correct servers.	

# Kafka <a id="kafka"></a>
* Kafka is a high throughput message broker.
* Compared with traditional message brokers such as ActiveMQ/RabbitMQ, it has fewer constraints on explicit message ordering to improve throughput. In addition, how Kafka deals with message consumers is different. Messages are not removed from the system. Instead, they rely on consumers to keep track of the last message consumed.
* Compared with distributed data collection system like Flume, Kafka is a more generic message broker and Flume is a more complete Hadoop ingestion solution. Flume has good supports for writing data to Hadoop such as HDFS, HBase and Solr. If the requirements involve fault-tolerant message delivery, message reply and a large number of consumers, you should consider Kafka.

# Spark <a id="spark"></a>
* Spark is a cluster computing system.
* Spark is typically much faster than MapReduce due to in-memory processing, Especially for iterative algorithms. In more detail, MapReduce does not leverage the memory of Hadoop cluster to the maximum, spark has the concept of RDD and caching which lets you save data or partial results in memory.
* With Spark it is possible to perform batch processing, streaming, interactive analysis altogether while MapReduce is only suitable for batch processing.

# Redis <a id="redis"></a>
* Redis is an in-memory key-value store.
* Compared with other key-value stores, Redis is much more faster. Redis can only serve data that fits in main memory. Although Redis has some replication features, it does not support features like eventual consistency. Even though Redis has been in the works for sometime, sharding and consistent hashing are provided by external services.
* Compared with other caching systems, Redis supports high-level data structures with atomic update capability. It also includes the capability to expire keys and publish-subscribe mechanism which can be used as messaging bus.

# Nodejs <a id="nodejs"></a>
