# Fight for 100 commits
* [Typical system design workflow](#workflow)
	- [Scenarios](#workflow-scenario)
		+ [What features to design](#what-features-to-design)
		+ [How strong services are](#how-strong-services-are)
	- [Service](#workflow-service)
		+ [HTTP status code](#workflow-service-http-status-code)
		+ [Restful principles](#workflow-service-restful-principles)
	- [Storage](#workflow-storage)
		+ [NoSQL features](#workflow-storage-nosql-features)
		+ [Consistency](#workflow-storage-consistency)
		+ [Schema design](#schema-design)
	- [Scale](#workflow-scale)
		+ [Consistency](#workflow-scale-consistency)
			* [Update consistency](#workflow-scale-update-consistency)
			* [Read consistency](#workflow-scale-read-consistency)
		+ [Tradeoffs between availability and consistency](#workflow-scale-tradeoff-availability-consistency)
		+ [Tradeoffs between latency and durability](#workflow-scale-tradeoff-latency-durability)
		+ [Cache](#workflow-storage-cache)
		+ [Functional partition](#workflow-scale-functional-partition)
		+ [Replication](#replication)
			* [When to use](#replication-when-to-use)
			* [When not to use](#replication-when-not-to-use)
			* [Consistency](#replication-consistency)
			* [Master-slave vs peer-to-peer](#replication-types)
			* [MySQL master-slave replication](#replication-mysql-master-slave)
				- [Number of slaves](#replication-mysql-number-of-slaves)
				- [Master-slave consistency](#replication-mysql-master-slave-consistency)
				- [Failure recovery for master-slave model](#replication-mysql-master-slave-failure-recovery)
			* [Deployment topology](#replication-deployment-topology)
		+ [Data partition - Sharding](#sharding)
			* [Use case](#sharding-benefits)
			* [Types](#sharding-types)
			* [Challenges](#sharding-challenges)
* [Cassandra](#workflow-storage-cassandra)
 	- [Data model](#workflow-storage-cassandra-data-model)
	- [Features](#workflow-storage-cassandra-features)
	- [Read-write prcess](#workflow-storage-cassandra-read-write-process)
* [User system](#user-system)
* [Newsfeed](#newsfeed)
	- [Push vs pull](#newsfeed-push-vs-pull)
	- [Follow and unfollow](#follow-and-unfollow)
* [Web system](#web-system-tinyurl)
* [Map reduce](#map-reduce)
	- [Word count](#word-count)
	- [Anagram](#anagram)
	- [Inverted index](#inverted-index)
* [Lookup service](#lookup-service)
* [Distributed File System](#file-system)
	- [Architecture](#file-system-architecture)
	- [Write a file](#file-system-write-a-file)
	- [Read a file](#file-system-read-a-file)
	- [Data integrity](#file-system-data-integrity)
	- [Data loss](#file-system-data-loss)
	- [Chunk server failure](#file-system-chunk-server-failure)
	- [Client bottleneck](#file-system-client-bottleneck)
* [Key-value data store - Bigtable](#key-value-store)
	- [Server management](#server-management)
	- [Read](#key-value-store-read)
	- [Write](#key-value-store-write)
	- [Race condition](#key-value-store-race-condition)
	- [Write ahead log: SSTable](#key-value-store-write-ahead-log-sstable)
	- [Index](#key-value-store-index)
* [Message system](#message-system)
	- [Real time](#real-time)
	- [Large group chat](#large-group-chat)
	- [Online status](#online-status)
* [Rate limiter](#rate-limiter)
* [Location based service](#location-based-service)
* [Crawler](#crawler)
* [Type ahead](#type-ahead)

# Typical system design workflow <a id="workflow"></a>
## Scenarios <a id="workflow-scenario"></a>
### What features to design <a id="what-features-to-design"></a>
#### Common features
* User system
	- Register/Login
	- User profile display/Edit
* Search

#### Specialized features
* Newsfeed
	- Post/Share a tweet
	- News feed
	- Follow/Unfollow a user
	- Timeline
	- Friendship
 
### How strong services are <a id="how-strong-services-are"></a>
#### Metrics:
* Monthly active user
* Daily active user
* QPS
	- Average QPS
	- Peak QPS
	- Future QPS
	- Read QPS
	- Write QPS	

## Service <a id="workflow-service"></a>
* Split application into small modules

## Read HTTP response
### HTTP status code <a id="workflow-service-http-status-code"></a>
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
* The response headers
	- Content-type/Media type: Tells HTTP client how to understand the entity-body.
		+ e.g. text/html, application/json, image/jpeg
* The entity body

* HTTP verbs
	- CRUD verbs.
	- Patch: Small modifications
	- Head: A lightweight version of GET
	- Options: Discovery mechanism for HTTP
	- Link/Unlink: Removes the link between a story and its author

| Verb | URI or template | common response code | Use | 
| ---- |:----------------:|:-------------------:|:-------------:| 
| POST | /order           | Created(201) / 202(Accepted) | Post-to-append: Create a new order, and upon success, receive a Location header specifying the new order's URI / Overloaded-post: | 
| GET  | /order/{orderId} | OK(200) / Moved permanently(301) / Not found (404) | Ask for a representation of a resource |  
| PUT  | /order/{orderId} | OK(200) / 204(No content)| Modify resource state | 
| DELETE | /order/{orderId} | OK(200) / 202(Accepted, will delete later) / 204 (has already been deleted, nothing more to say about it) | Wants to destroy a resource | 

### Restful principles <a id="workflow-service-restful-principles"></a>
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

## Storage <a id="workflow-storage"></a>

### NoSQL features<a id="workflow-storage-nosql-features"></a>
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

### Schema design <a id="schema-design"></a>


## Scale <a id="workflow-scale"></a>
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

### Tradeoffs between availability and consistency <a id="workflow-scale-tradeoff-availability-consistency"></a>
* CAP theorem: if you get a network partition, you have to trade off consistency versus availability. 
	- Consistency: Every read would get the most recent write. 
	- Availability: Every request received by the nonfailing node in the system must result in a response. 
	- Partition tolerance: The cluster can survive communication breakages in the cluster that separate the cluster into multiple partitions unable to communicate with each other. 

### Tradeoffs between latency and durability <a id="workflow-scale-tradeoff-latency-durability"></a>

### Cache <a id="workflow-storage-cache"></a>
* Hot spot / Thundering herd
* Cache topoloy
	- Cache aside
	- Cache through
* DNS

### Functional partition <a id="functional-partition"></a>

### Replication <a id="replication"></a>
#### When to use <a id="replication-when-to-use"></a>
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

#### Replication consistency <a id="replication-slave-consistency"></a>
* Def: Slaves could return stale data. 
* Reason: 
	- Replication is usually asynchronous, and any change made on the master needs some time to replicate to its slaves. Depending on the replication lag, the delay between requests, and the speed of each server, you may get the freshest data or you may get stale data. 
* Solution:
	- Send critical read requests to the master so that they would always return the most up-to-date data.
	- Cache the data that has been written on the client side so that you would not need to read the data you have just written. 
	- Minize the replication lag to reduce the chance of stale data being read from stale slaves.

#### Master-slave vs peer-to-peer <a id="replication-types"></a>

|     Types    |    Strengths     |      Weakness       | 
| ------------ |:----------------:|:-------------------:|
| Master-slave | <ul><li>Helpful for scaling when you have a read-intensive dataset. Can scale horizontally to handle more read requests by adding more slave nodes and ensuring that all read requests are routed to the slaves.</li><li>Helpful for read resilience. Should the master fail, the slaves can still handle read requests.</li><li>Increase availability by reducing the time needed to replace the broken database. Having slaves as replicas of the master does speed up recovery after a failure of the master since a slave can be appointed a new master very quickly. </li></ul> | <ul><li>Not a good scheme for datasets with heavy write traffic, although offloading the read traffic will help a little bit with handling the write load. All of your writes need to go through a single machine </li><li>The failure of the master does eliminate the ability to handle writes until either the master is restored or a new master is appointed.</li><li>Inconsistency. Different clients reading different slaves will see different values because the changes haven't all propagated to the slaves. In the worst case, that can mean that a client cannot read a write it just made. </li></ul> | 
| p2p: Master-master |  <ul><li> Faster master failover. In case of master A failure, or anytime you need to perform long-lasting maintainence, your application can be quickly reconfigured to direct all writes to master B.</li><li>More transparent maintainance. Switch between groups with minimal downtime.</li></ul>| 	Not a viable scalability technique. <ul><li>Need to use auto-increment and UUID() in a specific way to make sure you never end up with the same sequence number being generated on both masters at the same time.</li><li>Data inconsistency. For example, updating the same row on both masters at the same time is a classic race condition leading to data becoming inconsistent between masters.</li><li>Both masters have to perform all the writes. Each of the master needs to execute every single write statement either coming from your application or via the replication. To make it worse, each master will need to perform additional I/O to write replicated statements into the relay log.</li><li> Both masters have the same data set size. Since both masters have the exact same data set, both of them will need more memory to hold ever-growing indexes and to keep enough of the data set in cache.</li></ul> | 
| p2p: Ring-based    | Chain three or more masters together to create a ring. | <ul><li> All masters need to execute all the write statements. Does not help scale writes.</li><li> Reduced availability and more difficult failure recovery: Ring topology makes it more difficult to replace servers and recover from failures correctly. </li><li>Increase the replication lag because each write needs to jump from master to master until it makes a full circle.</li></ul> | 

#### MySQL master-slave replication <a id="replication-mysql-master-slave"></a>
* Responsibility: 
	- Master is reponsible for all data-modifying commands like updates, inserts, deletes or create table statements. The master server records all of these statements in a log file called a binlog, together with a timestamp, and a sequence number to each statement. Once a statement is written to a binlog, it can then be sent to slave servers. 
	- Slave is responsible for all read statements.
* Replication process: The master server writes commands to its own binlog, regardless if any slave servers are connected or not. The slave server knows where it left off and makes sure to get the right updates. This asynchronous process decouples the master from its slaves - you can always connect a new slave or disconnect slaves at any point in time without affecting the master.
	1. First the client connects to the master server and executes a data modification statement. The statement is executed and written to a binlog file. At this stage the master server returns a response to the client and continues processing other transactions. 
	2. At any point in time the slave server can connect to the master server and ask for an incremental update of the master' binlog file. In its request, the slave server provides the sequence number of the last command that it saw. 
	3. Since all of the commands stored in the binlog file are sorted by sequence number, the master server can quickly locate the right place and begin streaming the binlog file back to the slave server.
	4. The slave server then writes all of these statements to its own copy of the master's binlog file, called a relay log.
	5. Once a statement is written to the relay log, it is executed on the slave data set, and the offset of the most recently seen command is increased.  

##### Number of slaves <a id="replication-mysql-number-of-slaves"></a>
* It is a common practice to have two or more slaves for each master server. Having more than one slave machine have the following benefits:
	- Distribute read-only statements among more servers, thus sharding the load among more servers
	- Use different slaves for different types of queries. E.g. Use one slave for regular application queries and another slave for slow, long-running reports.
	- Losing a slave is a nonevent, as slaves do not have any information that would not be available via the master or other slaves.

##### Failure recovery <a id="replication-mysql-master-slave-failure-recovery"></a>
* Failure recovery
	- Slave failure: Take it out of rotation, rebuild it and put it back.
	- Master failure: If simply restart does not work, 
		+ First find out which of your slaves is most up to date. 
		+ Then reconfigure it to become a master. 
		+ Finally reconfigure all remaining slaves to replicate from the new master.

### Data partition - Sharding <a id="sharding"></a>
#### Benefits <a id="sharding-benefits"></a>
* Scale horizontally to any size. Without sharding, sooner or later, your data set size will be too large for a single server to manage or you will get too many concurrent connections for a single server to handle. You are also likely to reach your I/O throughput capacity as you keep reading and writing more data. By using application-level sharing, none of the servers need to have all of the data. This allows you to have multiple MySQL servers, each with a reasonable amount of RAM, hard drives, and CPUs and each of them being responsible for a small subset of the overall data, queries, and read/write throughput.
* Since sharding splits data into disjoint subsets, you end up with a share-nothing architecture. There is no overhead of communication between servers, and there is no cluster-wide synchronization or blocking. Servers are independent from each other because they shared nothing. Each server can make authoritative decisions about data modifications 
* You can implement in the application layer and then apply it to any data store, regardless of whether it supports sharding out of the box or not. You can apply sharding to object caches, message queues, nonstructured data stores, or even file systems. 

#### Types <a id="sharding-types"></a>
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

#### Challenges <a id="sharding-challenges"></a>
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

# Cassandra <a id="workflow-storage-cassandra"></a>
* Cassandra is a data store that was originally built at Facebook and could be seen as a merger of design patterns borrowed from BigTable and Dynamo. Cassandra is one of the clear leaders when it comes to ease of management, scalability, and self-healing, but it is important to remember that everything has its price. The main challenges that come with operating Cassandra are that it is heavily specialized, and it has a very particular data model, and it is an eventually consistent data store. 
* You can work around eventual conisstency by using quorum reads and writes, but the data model and tradeoffs made by the designers can often come as a surprise. Anything that you might have learned about relational databases is pretty much invalid when you work with NoSQL data stores like Cassandra. It is easy to get started with most NoSQL data stores, but to be able to operate them at scale takes much more experience and understanding of their internal structure than you might expect. 
	- For example, even though you can read in the open-source community that "Cassandra loves writes", deletes are the most expensive type of operation you can perform in Cassandra, which can come as a big suprise. Most people would not expect that deletes would be expensive type of operation you can perform in Cassandra. Cassandra uses append-only data structures, which allows it to write inserts with astonishing efficiency. Data is never overwritten in place and hard disks never have to perform random write operations, greatly increasing write throughput. But that feature, together with the fact that Cassandra is an eventually consistent datastore , forces deletes and updates to be internally persisted as inserts as well. As a result, some use cases that add and delete a lot of data can become inefficient because deletes increase the data set size rather than reducing it ( until the compaction process cleans them up ). 
	- A great example of how that can come as a surprise is a common Cassandra anti-pattern of a queue. You could model a simple first-in-first-out queue in Cassandra by using its dynamic columns. You add new entries to the queue by appending new columns, and you remove jobs from the queue by deleting columns. With a small scale and low volume of writes, this solution seems to work perfectly, but as you keep adding and deleting columns, your performance will begin to degrade dramatically. Although both inserts and deletes are perfectly fine and Cassandra purges old deleted data using its background compaction mechanism, it does not particularly like workloads with a such high rate of deletes (in this case, 50 percent of the operations are deletes).

## Data model <a id="workflow-storage-cassandra-data-model"></a>
* Level1: row_key
	- Namely hashkey
	- Could not perform range query
* Level2: column_key
	- Already sorted, could perform range query
	- Could be compound value (e.g. timestamp + user_id)
* Level3: value
	- In general it is String
	- Could use custom serialization or avaible ones such as Protobuff/Thrift.

## Features <a id="workflow-storage-cassandra-features"></a>
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

## Read/Write process <a id="workflow-storage-cassandra-read-write-process"></a> 
* Clients can connect to any server no matter what data they intend to read or write. 
* Clients then issue queries to the coordinator node they chose without any knowledge about the topology or state of the cluster.
* Each of the Cassandra nodes knows the status of all other nodes and what data they are responsible for. They can delegate queries to the correct servers.	

