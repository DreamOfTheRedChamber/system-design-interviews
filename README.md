# Fight for 100 commits
* [Typical system design workflow](#system-design-workflow)
	- [Scenarios](#system-design-workflow-scenario)
	- [Service](#system-design-workflow-service)
		+ [HTTP status code](#system-design-workflow-service-http-status-code)
		+ [Restful principles](#system-design-workflow-service-restful-principles)
	- [Storage](#system-design-workflow-storage)
		+ [Key considerations](#system-design-workflow-storage-key-considerations)
		+ [Database and QPS](#system-design-workflow-storage-database-qps)
		+ [SQL vs NoSQL](#system-design-workflow-storage-sql-vs-nosql)
		+ [Cassandra](#system-design-workflow-storage-cassandra)
		+ [Cache](#system-design-workflow-storage-cache)
	- [Scale](#system-design-workflow-scale)
		+ [Replication](#replication)
		+ [Sharding](#sharding)
		+ [Consistent hashing](#consistent-hashing)
		+ [Eventual consistency](#eventual-consistency)
* [Twitter](#twitter)
	- [Scenarios](#twitter-scenario)
		* [Features](#twitter-scenario-features)
		* [QPS](#twitter-scenario-qps)
	- [Service](#twitter-service)
	- [Storage](#twitter-storage)
	- [Scale](#twitter-scale)
* [User system](#user-system)
	- [Scenarios](#user-system-scenario)
		* [Features](#user-system-scenario-features)
		* [QPS](#user-system-scenario-qps)
	- [Service](#user-system-service)
	- [Storage](#user-system-storage)
	- [Scale](#user-system-scale)
* [Web system & TinyURL](#web-system-tinyurl)
* [Distributed File System](#distributed-file-system)
* [Distributed database](#distributed-database)

### Typical system design workflow <a id="system-design-workflow"></a>
#### Scenarios <a id="system-design-workflow-scenario"></a>
* What feature to design
* QPS/DAU/Interfaces

#### Service <a id="system-design-workflow-service"></a>
* Split application into small modules

#### Read HTTP response
##### HTTP status code <a id="system-design-workflow-service-http-status-code"></a>
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

##### Restful principles <a id="system-design-workflow-service-restful-principles"></a>
* Consistency:
	- Naming conventions:	
		+ Nouns: You should use nouns, not verbs
> GET /orders not /getAllOrders
		+ Plurals: You should use plural nouns, not singular nouns, to manage two different types of resources. 
> Collection resource: /users
> Instance resource: /users/007
		+ Versioning: You should make versioning mandatory in the URL at the highest scope (major versions). You may support at most two versions at the same time (Native apps need a longer cycle). GET /v1/orders
		+ The use cases HTTP verbs including POST/PUT/PATCH/DELETE
	- CRUD-like operations: Use HTTP verbs for CRUD operations (Create/Read/Update/Delete).
		+ POST is used to Create an instance of a collection. The ID isn’t provided, and the new resource location is returned in the “Location” Header.
			* 
		+ PUT is used for Updates to perform a full replacement. But remember that, if the ID is specified by the client, PUT is used to Create the resource.
		+ GET is used to Read a collection.
		+ PATCH is commonly used for partial Update.
		+ GET is used to Read an instance.
* Be careful about HTTP headers
	- Accept: If a client requires you to return application/xml and the server could only return application/json, then you'd better return status code 406. 
	- If-Modified-Since/If-None-Match: If a client specifies a codnition and ask the server to return data only when this condition satifies, otherwise return 304 unmodified. For example, if a client has cached the response of a request and only wants to know whether they are latest.
	- If-Match, 
* CURL
	- You should use CURL to share examples, which you can copy/paste easily.
* Granularity: medium graied resources
	- You should use medium grained, not  ne nor coarse. Resources shouldn’t be nested more than two levels deep.
* Security: OAuth2/OIDC & HTTPS
	- You should use OAuth2 to manage Authorization. OAuth2 matches 99% of requirements and client typologies, don’t reinvent the wheel, you’ll fail. You should use HTTPS for every API/OAuth2 request. You may use OpenID Connect to handle Authentication.
* API Domain names: You may consider the following five subdomains:
	- Production: https://api.fakecompany.com
	- Test: https://api.sandbox.fakecompany.com
	- Developer portal: https://developers.fakecompany.com
	- Production: https://oauth2.fakecompany.com
	- Test: https://oauth2.sandbox.fakecompany.com
* URLs
	- Hierachical structure: You should leverage the hierarchical nature of the URL to imply structure (aggregation or composition). Ex: an order contains products. GET /orders/1234/products/1
	- Consistent case: You may choose between snake_case or camelCase for attributes and parameters, but you should remain consistent. 

* Query strings
	- Search: You should use /search keyword to perform a search on a speci c resource. GET /restaurants/search?type=thai. You may use the “Google way” to perform a global search on multiple resources. GET /search?q=running+paid
	- Filter: You ought to use ‘?’ to  lter resources. GET /orders?state=payed&id_user=007 or (multiple URIs may refer to the same resource) GET /users/007/orders?state=paied
	- Pagination: You may use a range query parameter. Pagination is mandatory: a default pagination has to be de ned, for example: range=0-25. The response should contain the following headers: Link, Content-Range, Accept-Range. Note that pagination may cause some unexpected behavior if many resources are added.
	- Partial responses: You should use partial responses so developers can select which information they need, to optimize bandwidth (crucial for mobile development).
	- Sort: Use ?sort =atribute1,atributeN to sort resources. By default resources are sorted in ascending order. Use ?desc=atribute1,atributeN to sort resources in descending order
	- URL RESERVED WORDS : FIRST, LAST, COUNT

#### Storage <a id="system-design-workflow-storage"></a>
##### Key considerations <a id="system-design-workflow-storage-key-considerations"></a>
* How to store data, SQL, NoSQL or File System
* What is the schema

##### Database and QPS <a id="system-design-workflow-storage-database-qps"></a>
* MySQL/PosgreSQL ~ 1k QPS
* MongoDB/Cassandra ~ 10k QPS
* Redis/Memcached ~ 100k ~ 1M QPS

##### SQL vs NoSQL <a id="system-design-workflow-storage-sql-vs-nosql"></a>
* Both SQL and NoSQL works in most scenarios.
* To support transaction, needs SQL.
* NoSQL does not support features such as secondary index and serialization natively. Need to build them by yourself if needed. 
* NoSQL usually have 10X performance improvements on SQL.

##### Cassandra <a id="system-design-workflow-storage-cassandra"></a>
* Features
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

* Read/Write process
	+ Clients can connect to any server no matter what data they intend to read or write. 
	+ Clients then issue queries to the coordinator node they chose without any knowledge about the topology or state of the cluster.
	+ Each of the Cassandra nodes knows the status of all other nodes and what data they are responsible for. They can delegate queries to the correct servers.

	
##### Cache <a id="system-design-workflow-storage-cache"></a>
* 

#### Scale <a id="system-design-workflow-scale"></a>
##### Replication <a id="replication"></a>
* Use case
	- Suitable for:
		+ Scaling read-heavy applications. Namely scale the number of concurrent reading clients and the number of read queries per second.
		+ Increase availability by reducing the time needed to replace the broken database.
	- Not suitable for:
		+ Scaling write-heavy applications because all of your writes need to go through a single machine.
		+ Scale the overall data set size because all of the data must be present on each of the machines. The master and each of its slave need to have all of the data. 
			* When active data set is small, the database can buffer most of it in memory.
			* As your active data set grows, database needs to load more disk block. 
* Consistency
	- Source: Replication is asynchronous. Master server is decoupled from its slaves.
	- Behavior: Slaves could return stale data because of the replication log, the delay between requests and the speed of each server. 
	- Solution:
		+ Send critical read requests to the master so that they would always return the most up-to-date data.
		+ Cache the data that has been written on the client side so that you would not need to read the data you have just written. 
		+ Minize the replication lag to reduce the chance of stale data being read from stale slaves.
* Deployment topology
	- Master slave replication
		+ Responsibilities:
			* Master: All data-modifying commands like updates, inserts, deletes or create table statements.
			* Slave: All read statements.
		+ Failure recovery
			* Slave failure: Take it out of rotation, rebuild it and put it back.
			* Master failure: If simply restart does not work, first find out which of your slaves is most up to date. Then reconfigure it to become a master. Finally reconfigure all remaining slaves to replicate from the new master.
	- Master master
		+ Responsibilities:
			* Any statement that is sent to a master is replicated to other masters.
		+ Benefits: Useful in increasing availability.
			* Failure recovery: In case of master A failure, quickly fail over to use master B instead.
		+ Downsides: Not a viable scalability technique.
			* Both masters have to perform all the writes. Each of the master needs to execute every single write statement either coming from your application or via the replication. To make it worse, each master will need to perform additional I/O to write replicated statements into the relay log. 
			* Both masters have the same data set size. Since both masters have the exact same data set, both of them will need more memory to hold ever-growing indexes and to keep enough of the data set in cache.
	- Ring (Chain master to create a ring): Really bad idea
		+ Downsides:
			* All masters need to execute all the write statements. Does not help scale writes.
			* Reduced availability and more difficult failure recovery: Ring topology makes it more difficult to replace servers and recover from failures correctly.
			* Increase the replication lag because each write needs to jump from master to master until it makes a full circle. 

* It is a common practice to have two or more slaves for each master server. Having more than one slave machine have the following benefits:
	- Distribute read-only statements among more servers, thus sharding the load among more servers
	- Use different slaves for different types of queries. E.g. Use one slave for regular application queries and another slave for slow, long-running reports.
	- Losing a slave is a nonevent, as slaves do not have any information that would not be available via the master or other slaves.

##### Sharding <a id="sharding"></a>
* Types
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
* Benefits
	- Servers are independent from each other because they shared nothing. Each server can make authoritative decisions about data modifications 
	- There is no overhead of communication between servers and no need for cluster-wide synchronization or blocking.
	- Can implement in the application layer and then apply it to any data store, regardless of whether it supports sharding out of the box or not.
* Challenges
	- Cannot execute queries spanning multiple shards. Any time you want to run such a query, you need to execute parts of it on each shard and then somehow merge the results in the application layer.
		+ It is pretty common that running the same query on each of your servers and picking the highest of the values will not guarantee a correct result.
	- Lose the ACID properties of your database as a whole.
		+ Maintaining ACID properties across shards requires you to use distributed transactions, which are complex and expensive to execute. 
	- Depending on how you map from sharding key to the server number, it might be difficult to add more servers. 
		+ Solution1: Keep all of the mappings in a separate database. 
		+ Solution2: Map to logical database rather than physical database.

##### Consistent hashing <a id="consistent-hashing"></a>
* Hash input to a large range with hash function
* 

##### Eventual consistency <a id="system-design-workflow-scale-replica"></a>


### Twitter <a id="twitter"></a>
#### Scenario <a id="twitter-scenario"></a>
##### Features <a id="twitter-scenario-features"></a>
* Enumerate all features
	- Register/Login
	- User profile display
	- Edit
	- Upload image/video
	- Search
	- Post/Share a tweet
	- Timeline/Newsfeed
	- Follow/Unfollow a user
* Pick core functionality
	- Post a tweet
	- Timeline
	- News feed
	- Follow/Unfollow a user

##### QPS <a id="twitter-scenario-qps"></a>
* Daily active user (DAU): ~ 150M+, Monthly active user (MAU): ~320M
* Concurrent user
	- DAU * request per day and user / seconds in a day = 150M * 60 / 86400 ~ 100K
	- Peak: Average concurrent user * 3 ~ 300K
	- Future oriented/Fast growing products: Max peak user in 3 months = Peak users * 2
* Read QPS
	- 300K
* Write QPS
	- 5K

### User system <a id="user-system"></id>
#### Scenario <a id="user-system-scenario"></a>
##### Features <a id="user-system-scenario-features"></a>
* Register/Login/Lookup/Modify profile

##### QPS <a id="user-system-scenario-qps"></a>
* Daily active user (DAU): ~ 100M+
* Register/Login/Modify profile: 
	- DAU * request per day and user / seconds in a day = 100M * 0.1 / 86400 ~ 100
	- Peak: Average concurrent user * 3 ~ 300K
* Lookup:
	- DAU * request per day and user / seconds in a day = 100M * 100 / 86400 ~ 100K
	- Peak: 100K * 3 = 300K

