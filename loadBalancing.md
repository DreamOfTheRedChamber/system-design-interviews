<!-- MarkdownTOC -->

- [Load balancing](#load-balancing)
	- [Definition](#definition)

<!-- /MarkdownTOC -->


# Load balancing
## Definition
* Client to gateway: 
	- Implementation: DNS resolves to different ip address. 
* Gateway to web server: 
	- Implementation: Nginx reverse proxy
		+ Proxy hides itself
		+ Reverse proxy hides the server pool. 
* Web server to application server:
	- Implementation: Connection pool
* Application server to database: 
	- Implementation: Partition / Sharding