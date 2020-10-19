
<!-- MarkdownTOC -->

- [Session Server](#session-server)
	- [Where to store session data](#where-to-store-session-data)
		- [Sticky session](#sticky-session)
		- [Pure client](#pure-client)
		- [Shared session](#shared-session)
		- [Centralized session](#centralized-session)
	- [Architecture](#architecture)
		- [Sesssion server internal design](#sesssion-server-internal-design)
		- [How to design a high performant memory based LRU cache](#how-to-design-a-high-performant-memory-based-lru-cache)
		- [How to design a high-performant large volume persistent ConcurrentHashmap](#how-to-design-a-high-performant-large-volume-persistent-concurrenthashmap)

<!-- /MarkdownTOC -->


# Session Server
## Where to store session data
### Sticky session
* Cons:
	- Coupling between application and load balancing
	- Single point failure
	- Hard to scale horizontally
	- Uneven traffic

### Pure client
* Store everything on client
* Cons: 4K limit

### Shared session
* Data copy across machine

### Centralized session
* Redis
* Microsoft implementation:  ASP.NET State Server Protocol

## Architecture
* Problems
	* where to store SessionId and SessionServer mapping?
	* What if session server dies
	* Upgrade and scale
	* Service discovery

### Sesssion server internal design
### How to design a high performant memory based LRU cache
* Thread unsafe LRU Cache

* Thread safe LRU Cache

* Thread safe and high performant LRU cache
	- segment lock - Determine the number of segment numbers 
	- OkCache: An implementation already on github

* Industrial implementation
	- Guava Cache
	- Caffenine
		+ Window TinyLFU
		+ High concurrent RingBuffer

### How to design a high-performant large volume persistent ConcurrentHashmap
* BigCache
* Store all sessionId to memory. Session value could be persistent to disk
	- SessioId: Fixed length 8B
	- SessionValue: On average 10KB
* Industrial implementation
	- Yahoo HaloDB