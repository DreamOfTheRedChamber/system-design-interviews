
<!-- MarkdownTOC -->

- [Security:](#security)
	- [Auth architecture revolution](#auth-architecture-revolution)
		- [Single server cookie based auth](#single-server-cookie-based-auth)
		- [Multi server sticky session based auth](#multi-server-sticky-session-based-auth)
		- [Microservice security](#microservice-security)

<!-- /MarkdownTOC -->


# Security:
## Auth architecture revolution
### Single server cookie based auth

![Auth version 1](./images/security_singlemachine-cookiebased.png)

### Multi server sticky session based auth
* Cons:
	- Sticky session binds a session to a server. If the server goes down or needs to be maintained. 
	- Sticky session needs to store session data in load balancer. 
* Possible solutions:
	1. Session synchronization by replicating across web servers
	2. Store session data completely inside users' browser
		- Cons: Limited size of cookie
	3. Store session data in a shared storage

![Sticky session](./images/security_multimachine-stickysession.png)

### Microservice security


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