- [API Design](#api-design)
- [REST best practices](#rest-best-practices)
	- [Consistency](#consistency)
		- [Endpoint naming conventions](#endpoint-naming-conventions)
		- [HTTP verbs and CRUD consistency](#http-verbs-and-crud-consistency)
		- [Versioning](#versioning)
		- [Data transfer format](#data-transfer-format)
		- [HTTP status codes and error handling](#http-status-codes-and-error-handling)
		- [Paging](#paging)
			- [Scaling REST web services](#scaling-rest-web-services)
				- [Keeping service machine stateless](#keeping-service-machine-stateless)
					- [Benefits](#benefits)
					- [Common use cases needing share state](#common-use-cases-needing-share-state)
				- [Caching service responses](#caching-service-responses)
					- [Cache-Control header](#cache-control-header)
					- [Expires](#expires)
					- [Last-Modified/If-Modified-Since/Max-age](#last-modifiedif-modified-sincemax-age)
					- [ETag](#etag)
					- [Vary: Authorization](#vary-authorization)
				- [Functional partitioning](#functional-partitioning)
		- [Security](#security)
			- [Throttling](#throttling)
			- [Use OAuth2 with HTTPS for authorization, authentication and confidentiality.](#use-oauth2-with-https-for-authorization-authentication-and-confidentiality)
				- [??? API authentication](#-api-authentication)
		- [Documentation](#documentation)
		- [Others](#others)
- [RPC](#rpc)
	- [Communication protocol](#communication-protocol)
	- [Goal](#goal)
	- [RPC vs REST](#rpc-vs-rest)
	- [Components](#components)
		- [Overview](#overview)
		- [Interface definition language](#interface-definition-language)
		- [Marshal/Unmarshal](#marshalunmarshal)
		- [Server processing model](#server-processing-model)
		- [Binding](#binding)
		- [Transport protocol](#transport-protocol)
		- [Serialization protocol](#serialization-protocol)
		- [Semantics of RPC](#semantics-of-rpc)
			- [At least once](#at-least-once)
			- [Exactly once](#exactly-once)
			- [At most once](#at-most-once)
				- [Designs](#designs)
			- [Last of many](#last-of-many)
	- [Implementations](#implementations)
		- [History](#history)
		- [gRPC](#grpc)
			- [History](#history-1)
			- [Features](#features)
				- [Multi-language, multi-platform framework](#multi-language-multi-platform-framework)
				- [Transport over HTTP/2 + TLS](#transport-over-http2--tls)
				- [C/C++ implementation goals](#cc-implementation-goals)
			- [Components](#components-1)
		- [Comparison](#comparison)
			- [Cross language RPC: gRPC vs Thrift](#cross-language-rpc-grpc-vs-thrift)
			- [Same language RPC: Tars vs Dubbo vs Motan vs Spring Cloud](#same-language-rpc-tars-vs-dubbo-vs-motan-vs-spring-cloud)
- [API Design](#api-design-1)
	- [Trends](#trends)
	- [Real world](#real-world)
		- [Netflix](#netflix)
	- [Real world](#real-world-1)
		- [Netflix](#netflix-1)

# API Design

# REST best practices 
* Could look at industrial level api design example by [Github](https://developer.github.com/v3/)

## Consistency
### Endpoint naming conventions
* Use all lowercase, hyphenated endpoints such as /api/verification-tokens. This increases URL "hackability", which is the ability to manually go in and modify the URL by hand. You can pick any naming scheme you like, as long as you're consistent about it. 
* Use a noun or two to describe the resource, such as users, products, or verification-tokens.
* Always describe resources in plural: /api/users rather than /api/user. This makes the API more semantic. 
	- Collection resource: /users
	- Instance resource: /users/007

### HTTP verbs and CRUD consistency
* Use HTTP verbs for CRUD operations (Create/Read/Update/Delete).
	- Updates & creation should return a resource representation
		+ A PUT, POST or PATCH call may make modifications to fields of the underlying resource that weren't part of the provided parameters (for example: created_at or updated_at timestamps). To prevent an API consumer from having to hit the API again for an updated representation, have the API return the updated (or created) representation as part of the response.
		+ In case of a POST that resulted in a creation, use a HTTP 201 status code and include a Location header that points to the URL of the new resource.

| Verb   | Endpoint              | Description                                                            | 
|--------|-----------------------|------------------------------------------------------------------------| 
| GET    | /products             | Gets a list of products                                                | 
| GET    | /products/:id         | Gets a single product by ID                                            | 
| GET    | /products/:id/parts   | Gets a list of parts in a single product                               | 
| PUT    | /products/:id/parts   | Inserts a new part for a particular product                            | 
| DELETE | /products/:id         | Deletes a single product by ID                                         | 
| POST   | /products             | Inserts a new product                                                  | 
| HEAD   | /products/:id         | Returns whether the product exists through a status code of 200 or 404 | 
| PATCH  | /products/:id         | Edits an existing product by ID                                        | 
| POST   | /authentication/login | Most other API methods should use POST requests                        | 


### Versioning
* **What is versioning?** In traditional API scenarios, versioning is useful because it allows you to commit breaking changes to your service without demolishing the interaction with existing consumers.
* **Whether you need versioning?** Unless your team and your application are small enough that both live in the same repository and developers touch on both indistinctly, go for the safe bet and use versions in your API. 
	- Is the API public facing as well? In this case, versioning is necessary, baking a bit more predictability into your service's behavior. 
	- Is teh API used by several applications？ Are the API and the front end developed by separated teams? Is there a drawn-out process to change an API point? If any of these cases apply, you're probably better off versioning your API.   
* **How to implement versioning?** There are two popular ways to do it:
	- The API version should be set in HTTP headers, and that if a version isn't specified in the request, you should get a response from the latest version of the API. But it can lead to breaking changes inadvertently. 
	- The API version should be embedded into the URL. This identifies right away which version of the API your application wants by looking at the requested endpoint. An API version should be included in the URL to ensure browser explorability. 

### Data transfer format
* **Request**: You should decide on a consistent data-transfer strategy to upload the data to the server when making PUT, PATCH, or POST requests that modify a resource in the server. Nowadays, JSON is used almost ubiquitously as the data transport of choice due to its simplicity, the fact that it's native to browsers, and the high availability of JSON parsing libraries across server-side languages. 
* **Response**: 
	- Responses should conform to a consistent data-transfer format, so you have no surprises when parsing the response. Even when an error occurs on the server side, the response is still expected to be valid according to the chosen transport; For example, if your API is built using JSON, then all the responses produced by our API should be valid JSON. 
	- You should figure out the envelope in which you'll wrap your responses. An envelope, or message wrapper, is crucial for providing  a consistent experience across all your API endpoints, allowing consumers to make certain assumptions about the responses the API provides. A useful starting point may be an object with a single field, named data, that contains the body of your response. 

```json
{
	"data" : {}	 // actual response
}
```

### HTTP status codes and error handling
* Choose the right status codes for the problems your server is encountering so that the client knows what to do, but even more important is to make sure the error messages that are coming back are clear. 
	- An authentication error can happen because the wrong keys are used, because the signature is generated incorrectly, or because it's passed to the server in the wrong way. The more information you can give to developers about how and why the command failed, the more likely they'll be able to figure out how to solve the problem. 
* When you respond with status codes in the 2XX Success class, the response body should contain all of the relevant data that was requested. Here's an example showing the response to a request on a product that could be found, alongside with the HTTP version and status code:

```json
HTTP/1.1 200 OK
{
	"data": {
		"id" : "baeb-b001",
		"name" : "Angry Pirate Plush Toy",
		"description" : "Batteries not included",
		"price" : "$39.99",
		"categories": ["plushies", "kids"]
	}
}
```

* If the request is most likely failed due to an error made by the client side (the user wasn't properly authenticated, for instance), you should use 4XX Client Error codes. If the request is most likely failed due to a server side error, then you should use 5XX error codes. In these cases, you should use the error field to describe why the request was faulty. 

```json
// if input validation fails on a form while attempting to create a product, you could return a response using a 400 bad request status code, as shown in the following listing.
HTTP/1.1 400 Bad Request
{
	"error": {
		"code": "bf-400",
		"message": "Some required fields were invalid.",
		"context": {
			"validation": [
				"The product name must be 6-20 alphanumeric characters",
				"The price cann't be negative",
				"At least one product category should be selected. "
			]
		}
	}
}

// server side error
{
	"error": {
		"code": "bf-500",
		"message": "An unexpected error occurred while accessing the database",
		"context": {
			"id": "baeb-b001"
		}
	}
}
```

### Paging
* Suppose a user makes a query to your API for /api/products. How many products should that end point return? You could set a default pagination limit across the API and have the ability to override that default for each individual endpoint. Within a reasonable range, the consumer should have the ability to pass in a query string parameter and choose a different limit. 
	- Using Github paging API as an example, requests that return multiple items will be paginated to 30 items by default. You can specify further pages with the ?page parameter. For some resources, you can also set a custom page size up to 100 with the ?per_page parameter. Note that for technical reasons not all endpoints respect the ?per_page parameter, see events for example. Note that page numbering is 1-based and that omitting the ?page parameter will return the first page.

```bash
 curl 'https://api.github.com/user/repos?page=2&per_page=100'
```

* Common parameters
	- page and per_page. Intuitive for many use cases. Links to "page 2" may not always contain the same data.
	- offset and limit. This standard comes from the SQL database world, and is a good option when you need stable permalinks to result sets.
	- since and limit. Get everything "since" some ID or timestamp. Useful when it's a priority to let clients efficiently stay "in sync" with data. Generally requires result set order to be very stable.

* Metadata
	- Include enough metadata so that clients can calculate how much data there is, and how and whether to fetch the next set of results. Examples of how that might be implemented:

```json
{
  "results": [ ... actual results ... ],
  "pagination": {
    "count": 2340,
    "page": 4,
    "per_page": 20
  }
}
```

* Link header
	- The pagination info is included in the Link header. It is important to follow these Link header values instead of constructing your own URLs. In some instances, such as in the Commits API, pagination is based on SHA1 and not on page number.

```bash
 Link: <https://api.github.com/user/repos?page=3&per_page=100>; rel="next",
   <https://api.github.com/user/repos?page=50&per_page=100>; rel="last"
```

* Rel attribute
	- describes the relationship between the requested page and the linked page

| Name  | Description                                                   | 
|-------|---------------------------------------------------------------| 
| next  | The link relation for the immediate next page of results.     | 
| last  | The link relation for the last page of results.               | 
| first | The link relation for the first page of results.              | 
| prev  | The link relation for the immediate previous page of results. | 

* Cases exist where data flows too rapidly for traditional paging methods to behave as expected. For instance, if a few records make their way into the database between requests for the first page and the second one, the second page results in duplicates of items that were on page one but were pushed to the second page as a result of the inserts. This issue has two solutions:
	- The first is to use identifiers instead of page numbers. This allows the API to figure out where you left off, and even if new records get inserted, you'll still get the next page in the context of the last range of identifiers that the API gave you.
	- The second is to give tokens to the consumer that allow the API to track the position they arrived at after the last request and what the next page should look like. 


#### Scaling REST web services
##### Keeping service machine stateless
###### Benefits
* You can distribute traffic among your web service machines on a per-request basis. You can deploy a load balancer between your web services and their clients, and each request can be sent to any of the available web service machines. Being able to distribute requests in a round-robin fashion allows for better load distributionn and more flexibility.
* Since each web service request can be served by any of the web service machines, you can take service machines out of the load balancer pool as soon as they crash. Most of the modern load balancers support heartbeat checks to make sure that web services machines serving the traffic are available. As soon as a machine crashes or experiences some other type of failure, the load balancer will remove that host from the load-balancing pool, reducing the capacity of the cluster, but preventing clients from timing out or failing to get responses. 
* You can restart and decommission servers at any point in time without worrying about affecting your clients. For example, if you want to shut down a server for maintenance, you need to take that machine out of the load balancer pool. Most load balancers support graceful removal of hosts, so new connections from clients are not sent to that server any more, but existing connections are not terminated to prevent client-side errors. After removing the host from the pool, you need to wait for all of your open connections to be closed by your clients, which can take a minute or two, and then you can safely shut down the machine without affecting even a single web service request. 
* You will be able to perform zero-downtime updates of your web services. You can roll out your changes to one server at a time by taking it out of rotation, upgrading, and then putting it back into rotation. If your software does not allow you to run two different versions at the same time, you can deploy to an alternative stack and switch all of the traffic at once on the load balancer level. 
* By removing all of the application state from your web services, you will be able to scale your web services layer by simply adding more clones. All you need to do is adding more machines to the load balancer pool to be able to support more concurrent connections, perform more network I/O, and compute more responses. 

###### Common use cases needing share state
* The first use case is related to security, as your web service is likely going to require clients to pass some authentication token with each web service request. The token will have to be validated on the web service side, and client permissions will have to be evaluated in some way to make sure that the user has access to the operation they are attempting to perform. You could cache authentication and authorization details directly on your web service machines, but that could cause problems when changing permissions or blocking accounts, as these objects would need to expire before new permissions could take effect. A better approach is to use a shared in-memory object cache and have each web service machine reach out for the data needed at request time. If not present, data could be fetched from the original data store and placed in the object cache. By having a single central copy of each cached object, you will be able to easily invalidate it when users' permissions change. 
* Another common problem when dealing with stateless web services is how to support resource locking. You can use distributed lock systems like Zookeeper or even build your own simple lock service using a data store of your choice. To make sure your web services scale well, you should avoid resource locks for as long as possible and look for alternative ways to synchronize parallel processes. 
	- Distributed locking creates an opportunity for your service to stall or fail. This, in turn, increases your latency and reduces the number of parallel clients that your web service can serve. Instead of resource locks, you can sometimes use optimistic concurrency control where you check the state before the final update rather than acquiring locks. You can also consider message queues as a way to decouple components and remove the need for resource locking in the first place. 
	- If none of the above techniques work for you and you need to use resource locks, it is important to strike a balance between having to acquire a lot of fine-grained locks and having coarse locks that block access to large sets of data. By having too many fine-grained locks, you increase risk for deadlocks. If you use few coarse locks, you can increase concurrency because multiple web services can be blocked waiting on the same resource lock. 
* The last challenge is application-level transactions. A distributed transaction is a set of internal service steps and external web service calls that either complete together or fail entirely. It is very difficult to scale and coordinate without sacrificing high availability. The most common method of implementing distributed transactions is the 2 Phase Commit algorithm. An example of a distributed transaction would be a web service that creates an order within an online shop. 
	- The first alternative to distributed transactions is to not support them at all. As long as the core of your system functionality is not compromised, your company may be fine with such a minor inconsistencies in return for the time saved developing it.
	- The second alternative to distributed transactions is to provide a mechanism of compensating transactions. A compensating transactins can be used to revert the result of an operation that was issued as part of a larger logical transaction that has failed. The benefit of this approach is that web services do not need to wait for one another; they do not need to maintain any state or resources for the duration of the overarching transaction either. 


##### Caching service responses
* From a caching perspective, the GET method is the most important one, as GET responses can be cached. 
* To be able to scale using cache, you would usually deploy reverse proxies between your clients and your web service. As your web services layer grow, you may end up with a more complex deployment where each of your web services has a reverse proxy dedicated to serve its results. Depending on the reverse proxy used, you may also have load balancers deployed between reverse proxies and web services to distribute the underlying network traffic and provide quick failure recovery. 

###### Cache-Control header
* Setting the Cache-Control header to private bypasses intermediaries (such as nginx, other caching layers like Varnish, and all kinds of hardware in between) and only allows the end client to cache the response. 
* Setting it to public allows intermediaries to store a copy of the response in their cache. 

###### Expires
* Tells the browser that a resource should be cached and not requested again until the expiration date has elapsed. 
* It's hard to define future Expires headers in API responses because if the data in the server changes, it could mean that the cleint's cache becomes stale, but it doesn't have any way of knowing that until the expiration date. A conservative alternative to Expires header in responses is using a pattern callled "conditional requests"

###### Last-Modified/If-Modified-Since/Max-age
* Specifying a Last-Modified header in your response. It's best to specify a max-age in the Cache-Control header, to let the browser invalidate the cache after a certain period of time even if the modification date doesn't change

> Cache-Control: private, max-age=86400
> Last-Modified: Thu, 3 Jul 2014 18:31:12 GMT

* The next time the browser requests this resource, it will only ask for the contents of the resource if they're unchanged since this date, using the If-Modified-Since request header. If the resource hasn't changed since Thu, 3 Jul 2014 18:31:12 GMT, the server will return with an empty body with the 304 Not Modified status code.

> If-Modified-Since: Thu, 3 Jul 2014 18:31:12 GMT

###### ETag
* ETag header is usually a hash that represents the source in its current state. This allows the server to identify if the cached contents of the resource are different than the most recent versions:

> Cache-Control: private, max-age=86400
> ETag: "d5jiodjiojiojo"

* On subsequent requests, the If-None-Match request header is sent with the ETag value of the last requested version for the same resource. If the current version has the same ETag value, your current version is what the client has cached and a 304 Not Modified response will be returned. 

> If-None-Match: "d5jiodjiojiojo"	

###### Vary: Authorization
* You could implement caching of authenticated REST resources by using headers like Vary: Authorization in your web service responses. Responses with such headers instruct HTTP caches to store a separate response for each value of the Authorization header. 

##### Functional partitioning
* By functional partitioning, you group closely related functionality together. The resulting web services are loosely coupled and they can now be scaled independently. 

### Security

#### Throttling
* This kind of safeguarding is usually unnecessary when dealing with an internal API, or an API meant only for your front end, but it's a crucial measure to make when exposing the API publicly. 
* Suppose you define a rate limit of 2,000 requests per hour for unauthenticated users; the API should include the following headers in its responses, with every request shaving off a point from the remainder. The X-RateLimit-Reset header should contain a UNIX timestamp describing the moment when the limit will be reset

> X-RateLimit-Limit: 2000
> X-RateLimit-Remaining: 1999
> X-RateLimit-Reset: 1404429213925


* Once the request quota is drained, the API should return a 429 Too Many Request response, with a helpful error message wrapped in the usual error envelope: 

```
X-RateLimit-Limit: 2000
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1404429213925
{
	"error": {
		"code": "bf-429",
		"message": "Request quota exceeded. Wait 3 minutes and try again.",
		"context": {
			"renewal": 1404429213925
		}
	}
}
```

* However, it can be very useful to notify the consumer of their limits before they actually hit it. This is an area that currently lacks standards but has a number of popular conventions using HTTP response headers.

#### Use OAuth2 with HTTPS for authorization, authentication and confidentiality. 
##### ??? API authentication
* https://jobandtalent.engineering/api-authentication-strategies-in-a-microservices-architecture-dc84cc61c5cc

### Documentation
* Good documentation should
	- Explain how the response envelope works
	- Demonstrate how error reporting works
	- Show how authentication, paging, throttling, and caching work on a high level
	- Detail every single endpoint, explain the HTTP verbs used to query those endpoints, and describe each piece of data that should be in the request and the fields that may appear in the response
* Test cases can sometimes help as documentation by providing up-to-date working examples that also indicate best practices in accessing an API. The docs should show examples of complete request/response cycles. Preferably, the requests should be pastable examples - either links that can be pasted into a browser or curl examples that can be pasted into a terminal. GitHub and Stripe do a great job with this.
	- CURL: always illustrating your API call documentation by cURL examples. Readers can simply cut-and-paste them, and they remove any ambiguity regarding call details.
* Another desired component in API documentation is a changelog that briefly details the changes that occur from one version to the next. The documentation must include any deprecation schedules and details surrounding externally visible API updates. Updates should be delivered via a blog (i.e. a changelog) or a mailing list (preferably both!).

### Others 
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
* HATEOAS: Hypertext As The Engine of Application State
	- There should be a single endpoint for the resource, and all of the other actions you’d need to undertake should be able to be discovered by inspecting that resource.
	- People are not doing this because the tooling just isn't there.

# RPC 
## Communication protocol
* [TODO: REST vs XML vs IDL](https://time.geekbang.org/column/article/14425)
* TODO: Network establish & NIO/BIO/AIO vs serialization
  * https://time.geekbang.org/column/article/15092
* TODO: Choose RPC framework:
  * https://time.geekbang.org/column/article/39809

## Goal
* Make the process of executing code on a remote machine as simple and straight-forward as calling a local functions. 

## RPC vs REST

|   |  REST |  RPC |
|---|---|---|
| Definition  | REST is an [architecture style](https://www.restapitutorial.com/lessons/whatisrest.html). It exposes data as resources and CRUD operations could be used to access resources. HTTP is an implement conforming to REST styles |  Exposes  action-based API methods |
| HTTP verbs | REST will use HTTP methods such as GET, POST, PUT, DELETE, OPTIONS and, hopefully, PATCH to provide semantic meaning for the intention of the action being taken.  | RPC uses only GET and POST, with GET being used to fetch information and POST being used for everything else.  |
| Serilization protocol | readablee text(XML, JSon) | ProtoBuf, Thrift |
| Transmission protocol | HTTP/HTTP2 | Usually UDP/TCP (There are also exception. e.g. gRPC uses HTTP2) |
| Examples  | Dubbo, Motan, Tars, gRPC, Thrift  | SpringMVC/Boot, Jax-rs, drop wizard |
| User friendly | Easy to debug because request/response are readable | Hard to debug because request/response are not readable |
| Design challenges  | 1. Fetching multiple resources in a single request 2. The challenge of mapping operations to HTTP verbs  |  Hard to discover because there is limited standardization. Without a nice documentation, you won’t know how to start neither what to call. |

## Components
### Overview
* The steps are as follows:
	1. Programmer writes an interface description in the IDL (Mechanism to pass procedure parameters and return values in a machine-independent way)
	2. Programmer then runs an IDL compiler which generates
		+ Code to marshal native data types into machien independent byte streams
		+ Client/server stub: Forwards local procedure call as request to server / Dispatches RPC to its implementation

### Interface definition language

```
// An example of an interface for generating stubs
service FacebookService {
  // Returns a descriptive name of the service
  string getName(),

  // Returns the version of the service
  string getVersion(),
    
  // Gets an option
  string getOption(1: string key),

  // Gets all options
  map<string, string> getOptions()
}
```

### Marshal/Unmarshal
* Challenges: For a remote procedure call, a remote machine may:
	- Different sizes of integers and other types
	- Different byte ordering
	- Different floating point representations
	- Different character sets
	- Different alignment requirements

* Challenges: Reference variables
	- What we pass is the value of the pointer, instead of the pointer itself. A local pointer, pointing to this value is created on the server side (Copy-in). When the server procedure returns, the modified 'value' is returned, and is copied back to the address from where it was taken (Copy-out).

	- This approach is not foolproof. The procedure 'myfunction()' resides on the server machine. If the program executes on a single machine then we must expect the output to be '4'. But when run in the client-server model we get '3'. Why ? Because 'x, and 'y' point to different memory locations with the same value. Each then increments its own copy and the incremented value is returned. Thus '3' is passed back and not '4'.

```
#include <studio.h>

void myfunction(int *x, int *y)
{
	*x += 1;
	*y += 1;
}

```

### Server processing model
* BIO: Server creates a new thread to handle to handle each new coming request. 
	- Applicable for scenarios where there are not too many concurrent connections
* NIO: The server uses IO multiplexing to process new coming request.
	- Applicable for scenarios where there are many concurrent connections and the request processing is lightweight. 
* AIO: The client initiates an IO operation and immediately returns. The server will notify the client when the processing is done. 
	- Applicable for scenarios where there are many concurrent connections and the request processing is heavyweight. 
* Reactor model: A main thread is responsible for all request connection operation. Then working threads will process further jobs.

### Binding
* Binding: How does the client know who to call, and where the service resides?
	- The most flexible solution is to use dynamic binding and find the server at run time when the RPC is first made. The first time the client stub is invoked, it contacts a name server to determine the transport address at which the server resides.

* Where to locate host and correct server process
	- Solution1: Maintain a centralized DB that can locate a host that provides a particular service
		1. Challenge: Who administers this
		2. Challenge: What is the scope of administration
		3. Challenge: What if the same services run on different machines 
	- Solution2: A server on each host maintains a DB of locally provided services
	- Please see [Service discovery](./servicediscoverycenter.md)

### Transport protocol
* If performance is preferred, then UDP protocol should be adopted. 
* If reliability is needed, then TCP protocol should be adopted. 
	- If the connection is a service to service, then long connection is preferred than short connection. 

### Serialization protocol
* Factors to consider:
	- Support data types: Some serialization framework such as Hessian 2.0 even support complicated data structures such as Map and List. 
	- Cross-language support
	- Performance: The compression rate and the speed for serialization. 

### Semantics of RPC
#### At least once
* Def: For every request message that the client sends, at least one copy of that message is delivered to the server. The client stub keeps retrying until it gets an ack. This is applicable for idempotent operations.

#### Exactly once
* Def: For every request message that the client sends, exactly one copy of that message is delivered to the server.
* But this goal is extremely hard to build. For example, in case of a server crash, the server stub call and server business logic could happen not in an atomic manner. 

#### At most once
* Def: For every request message that the client sends, at most one copy of that message is delivered to the server.

##### Designs
1. How to detect a duplicate request?
	- Client includes unique transaction ID with each one of its RPC requests
	- Client uses the same xid for retransmitted requests
2. How to avoid false detection?
	- One of the recurrent challenges in RPC is dealing with unexpected responses, and we see this with message IDs. For example, consider the following pathological (but realistic) situation. A client machine sends a request message with a message ID of 0, then crashes and reboots, and then sends an unrelated request message, also with a message ID of 0. The server may not have been aware that the client crashed and rebooted and, upon seeing a request message with a message ID of 0, acknowledges it and discards it as a duplicate. The client never gets a response to the request.
	- One way to eliminate this problem is to use a boot ID. A machine’s boot ID is a number that is incremented each time the machine reboots; this number is read from nonvolatile storage (e.g., a disk or flash drive), incremented, and written back to the storage device during the machine’s start-up procedure. This number is then put in every message sent by that host. If a message is received with an old message ID but a new boot ID, it is recognized as a new message. In effect, the message ID and boot ID combine to form a unique ID for each transaction.
3. How to ensure that the xid is unique?
	- Combine a unique client ID (e.g. IP address) with the current time of the day
	- Combine a unique client ID with a sequence number
	- Combine a unique client ID with a boot ID
	- Big random number
4. seen and old arrays will grow without bound
	- Client could tell server "I'm done with xid x - delete it".
	- Client includes "seen all replies <= X" with every RPC
5. Server may crash and restart
	- If old[], seen[] tables are only in meory, then the user needs to retry


```
if seen[xid]:
	retval = old[xid]
else:
	retval = handler()
	old[xid] = retval
	seen[xid] = true
return retval
```

#### Last of many
* Last of many : This a version of 'At least once', where the client stub uses a different transaction identifier in each retransmission. Now the result returned is guaranteed to be the result of the final operation, not the earlier ones. So it will be possible for the client stub to tell which reply belongs to which request and thus filter out all but the last one.

## Implementations
### History
* SunRPC is the basis for Network File System. 
* DCE-RPC is the basis of Microsoft's DCOM and ActiveX. 
* RMI
	- RMI uses Java Remote Messaging Protocol for communication. It has limitation that both the sender and receiver need to be Java programs. It could not be used in cross-language scenarios
	- RMI uses Java's native approach for serialization and deserialization. The generated binary format is not efficient. 

### gRPC
#### History
* The biggest differences between gRPC and SunRPC/DCE-RPC/RMI is that gRPC is designed for cloud services rather than the simpler client/server paradigm. In the client/server world, one server process is presumed to be enough to serve calls from all the client processes that might call it. With cloud services, the client invokes a method on a service, which in order to support calls from arbitrarily many clients at the same time, is implemented by a scalable number of server processes, each potentially running on a different server machine.
* The caller identifies the service it wants to invoke, and a load balancer directs that invocation to one of the many available server processes (containers) that implement that service

![gRPC history](./images/grpc_history.png)

#### Features
##### Multi-language, multi-platform framework
* Native implementations in C, Java, and Go
* Platforms supported: Linux, Android, iOS, MacOS, Windows

##### Transport over HTTP/2 + TLS
* First, gRPC runs on top of TCP instead of UDP, which means it outsources the problems of connection management and reliably transmitting request and reply messages of arbitrary size. 
* Second, gRPC actually runs on top of a secured version of TCP called Transport Layer Security (TLS)—a thin layer that sits above TCP in the protocol stack—which means it outsources responsibility for securing the communication channel so adversaries can’t eavesdrop or hijack the message exchange. 
* Third, gRPC actually, actually runs on top of HTTP/2 (which is itself layered on top of TCP and TLS), meaning gRPC outsources yet two other problems: 
	- Binary framing and compression: Efficiently encoding/compressing binary data into a message.
	- Multiplexing: Requests by introducing concept of streams.
		- HTTP: The client could send a single request message and the server responds with a single reply message.
		- HTTP 1.1: The client could send multiple requests without waiting for the response. However, the server is still required to send the responses in the order of incoming requests. So Http 1.1 remained a FIFO queue and suffered from requests getting blocked on high latency requests in the front [Head-of-line blocking](https://en.wikipedia.org/wiki/Head-of-line_blocking)
		- HTTP2 introduces fully asynchronous, multiplexing of requests by introducing concept of streams. lient and servers can both initiate multiple streams on a single underlying TCP connection. Yes, even the server can initiate a stream for transferring data which it anticipates will be required by the client. For e.g. when client request a web page, in addition to sending theHTML content the server can initiate a separate stream to transfer images or videos, that it knows will be required to render the full page. 

##### C/C++ implementation goals
* High throughput and scalability, low latency
* Minimal external dependencies

#### Components

![gRPC components](./images/grpc_components.png)

### Comparison
#### Cross language RPC: gRPC vs Thrift
* gRPC uses HTTP/2, serialization uses ProtoBuf
* Thrift support multiple modes:
	- Serialization: Binary, compact, Json, multiplexed
	- Transmission: Socket, Framed, File, Memory
	- Server processing model: Simple, Thread Pool, Non-blocking

#### Same language RPC: Tars vs Dubbo vs Motan vs Spring Cloud
* C++: Tars
* Java: 
	+ Spring cloud provides many other functionalities such as service registration, load balancing, circuit breaker. 
		- HTTP protocol
	+ Motan/Dubbo is only RPC protocol


# API Design
## Trends
* Count
	1. countViewEvent(videoId)
	2. countEvent(videoId, eventType) 
		+ eventType: view/like/share
	3. processEvent(video, eventType, func)
		+ func: count/sum/avg
	4. processEvents(listOfEvents)
* Query
	1. getViewsCount(videoId, startTime, endTime)
	2. getCount(videoId, eventType, startTime, endTime)
	3. getStats(videoId, eventType, func, startTime, endTime) 

## Real world 
### Netflix
* GraphQL at Netflix: 
  * https://netflixtechblog.com/beyond-rest-1b76f7c20ef6
  * https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-2-bbe71aaec44a
  * https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-1-ae3557c187e2
  * https://netflixtechblog.com/our-learnings-from-adopting-graphql-f099de39ae5f
* API migration at Netflix:
  * https://netflixtechblog.com/seamlessly-swapping-the-api-backend-of-the-netflix-android-app-3d4317155187


## Real world 
### Netflix
* GraphQL at Netflix: 
  * https://netflixtechblog.com/beyond-rest-1b76f7c20ef6
  * https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-2-bbe71aaec44a
  * https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-1-ae3557c187e2
  * https://netflixtechblog.com/our-learnings-from-adopting-graphql-f099de39ae5f
* API migration at Netflix:
  * https://netflixtechblog.com/seamlessly-swapping-the-api-backend-of-the-netflix-android-app-3d4317155187