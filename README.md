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
		+ [Cassandra](#workflow-storage-cassandra)
	- [Scale](#workflow-scale)
		+ [Cache](#workflow-storage-cache)
		+ [Scale MySQL](#workflow-scale-mysql)
		+ [Scale NoSQL](#workflow-scale-nosql)
		+ [Replication](#replication)
		+ [Sharding](#sharding)
		+ [Consistent hashing](#consistent-hashing)
		+ [Eventual consistency](#eventual-consistency)
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
|    Consistency        | Strong consistency  |  Trade consistency for availability or partition tolerance.  |
|    Scalability      | elational database use ACID transactions to handle consistency across the whole database. This inherently clashes with a cluster environment |  Aggregate structure helps greatly with running on a cluster. It we are running on a cluster, we need to minize how many nodes we need to query when we are gathering data. By using aggregates, we give the database important information about which bits of data (an aggregate) will be manipulated together, and thus should live on the same node. | 
|    Performance        | MySQL/PosgreSQL ~ 1k QPS  |  MongoDB/Cassandra ~ 10k QPS. Redis/Memcached ~ 100k ~ 1M QPS |
|    Maturity           | Over 20 years | Usually less than 10 years. Not great support for serialization and secondary index |

### Schema design <a id="schema-design"></a>

### Cassandra <a id="workflow-storage-cassandra"></a>
#### Features
	- All nodes are functionally equal but each node is responsible for different data set. 
		+ Does not have a single point of failure. 
		+ Automatic data partitioning.
	- Data model
		+ Level1: row_key
			* Namely hashkey
			* Could not perform range query
		+ Level2: column_key
			* Already sorted, could perform range query
			* Could be compound value (e.g. timestamp + user_id)
		+ Level3: value
			* In general it is String
			* Could use custom serialization or avaible ones such as Protobuff/Thrift.
	- Tunable consistency

	- Little administration

#### Read/Write process
	+ Clients can connect to any server no matter what data they intend to read or write. 
	+ Clients then issue queries to the coordinator node they chose without any knowledge about the topology or state of the cluster.
	+ Each of the Cassandra nodes knows the status of all other nodes and what data they are responsible for. They can delegate queries to the correct servers.	

## Scale <a id="workflow-scale"></a>
### Cache <a id="workflow-storage-cache"></a>
* Hot spot / Thundering herd
* Cache topoloy
	- Cache aside
	- Cache through
* DNS



### Replication <a id="replication"></a>
#### Use case <a id="replication-use-case"></a>
* Suitable for:
	- Scaling read-heavy applications. Namely scale the number of concurrent reading clients and the number of read queries per second.
	- Increase availability by reducing the time needed to replace the broken database.
* Not suitable for:
	- Scaling write-heavy applications because all of your writes need to go through a single machine.
	- Scale the overall data set size because all of the data must be present on each of the machines. The master and each of its slave need to have all of the data. 
		+ When active data set is small, the database can buffer most of it in memory.
		+ As your active data set grows, database needs to load more disk block. 

#### Consistency <a id="replication-consistency"></a>
* Source: Replication is asynchronous. Master server is decoupled from its slaves.
* Behavior: Slaves could return stale data because of the replication log, the delay between requests and the speed of each server. 
* Solution:
	- Send critical read requests to the master so that they would always return the most up-to-date data.
	- Cache the data that has been written on the client side so that you would not need to read the data you have just written. 
	- Minize the replication lag to reduce the chance of stale data being read from stale slaves.

#### Deployment topology <a id="replication-deployment-topology"></a>
* Master slave replication
	- Responsibilities:
		+ Master: All data-modifying commands like updates, inserts, deletes or create table statements.
		+ Slave: All read statements.
	- Failure recovery
		+ Slave failure: Take it out of rotation, rebuild it and put it back.
		+ Master failure: If simply restart does not work, first find out which of your slaves is most up to date. Then reconfigure it to become a master. Finally reconfigure all remaining slaves to replicate from the new master.
* Master master
	- Responsibilities:
		+ Any statement that is sent to a master is replicated to other masters.
	- Benefits: Useful in increasing availability.
		+ Failure recovery: In case of master A failure, quickly fail over to use master B instead.
	- Downsides: Not a viable scalability technique.
		+ Both masters have to perform all the writes. Each of the master needs to execute every single write statement either coming from your application or via the replication. To make it worse, each master will need to perform additional I/O to write replicated statements into the relay log. 
		+ Both masters have the same data set size. Since both masters have the exact same data set, both of them will need more memory to hold ever-growing indexes and to keep enough of the data set in cache.
* Ring (Chain master to create a ring): Really bad idea
	- Downsides:
		+ All masters need to execute all the write statements. Does not help scale writes.
		+ Reduced availability and more difficult failure recovery: Ring topology makes it more difficult to replace servers and recover from failures correctly.
		+ Increase the replication lag because each write needs to jump from master to master until it makes a full circle. 

* It is a common practice to have two or more slaves for each master server. Having more than one slave machine have the following benefits:
	- Distribute read-only statements among more servers, thus sharding the load among more servers
	- Use different slaves for different types of queries. E.g. Use one slave for regular application queries and another slave for slow, long-running reports.
	- Losing a slave is a nonevent, as slaves do not have any information that would not be available via the master or other slaves.

### Sharding <a id="sharding"></a>
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

#### Benefits <a id="sharding-benefits"></a>
* Servers are independent from each other because they shared nothing. Each server can make authoritative decisions about data modifications 
* There is no overhead of communication between servers and no need for cluster-wide synchronization or blocking.
* Can implement in the application layer and then apply it to any data store, regardless of whether it supports sharding out of the box or not.

#### Challenges <a id="sharding-challenges"></a>
* Cannot execute queries spanning multiple shards. Any time you want to run such a query, you need to execute parts of it on each shard and then somehow merge the results in the application layer.
	- It is pretty common that running the same query on each of your servers and picking the highest of the values will not guarantee a correct result.
* Lose the ACID properties of your database as a whole.
	- Maintaining ACID properties across shards requires you to use distributed transactions, which are complex and expensive to execute. 
* Depending on how you map from sharding key to the server number, it might be difficult to add more servers. 
	- Solution1: Keep all of the mappings in a separate database. 
	- Solution2: Map to logical database rather than physical database.

### Consistent hashing <a id="consistent-hashing"></a>
* Hash input to a large range with hash function

### Eventual consistency <a id="workflow-scale-replica"></a>


# User system <a id="user-system"></id>
## Register/Login/Lookup/Modify profile
## Authentication service

# Newsfeed <a id="newsfeed"></a>
