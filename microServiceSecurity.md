
<!-- MarkdownTOC -->

- [Security](#security)
	- [Auth architecture revolution](#auth-architecture-revolution)
		- [Single server cookie based auth](#single-server-cookie-based-auth)
		- [Multi server sticky session based auth](#multi-server-sticky-session-based-auth)
		- [Auth Service and token](#auth-service-and-token)
		- [Gateway and token](#gateway-and-token)
		- [Gateway and JWT](#gateway-and-jwt)
			- [JWT](#jwt)
				- [Motivation](#motivation)
				- [Types of JWT](#types-of-jwt)
	- [OAuth2](#oauth2)
		- [Simple guide](#simple-guide)
		- [Roles](#roles)
		- [Types of token](#types-of-token)
			- [Access token](#access-token)
			- [Refresh token](#refresh-token)
			- [Authorization code token](#authorization-code-token)
			- [Bearer token](#bearer-token)
			- [Proof of possession token](#proof-of-possession-token)
		- [OAuth 2 modes](#oauth-2-modes)
			- [Authorization code mode](#authorization-code-mode)
			- [Simplified mode](#simplified-mode)
			- [Password mode](#password-mode)
			- [Refresh token mode](#refresh-token-mode)
	- [OpenID Connect](#openid-connect)
	- [Microservices security architecture](#microservices-security-architecture)
		- [External access token internal JWT token](#external-access-token-internal-jwt-token)
		- [Encrypted JWT token](#encrypted-jwt-token)
		- [External access token internal JWT token with token cache](#external-access-token-internal-jwt-token-with-token-cache)

<!-- /MarkdownTOC -->


# Security
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

### Auth Service and token
* Pros:
	- Encapsulate everything related with token issuing
	- Introduce the concept of token, which could be passed around between services
* Cons:
	- Services need to implement the logic to validate the token. 
	- All services need to talk to authSvc, which might become a performance bottleneck. 	
	- All requests need to be verified via auth service. 

![Auth service and token](./images/security_authservice_token.png)

### Gateway and token
* Pros:
	- Gateway centralizes the logic of parsing userInfo. Only gateway need to validate the token with auth service. 
* Cons:
	- All requests need to be verified via auth service. Auth service needs to be maintained and scaled in a manageable way. 

![Gateway and token](./images/security_gateway_token.png)

### Gateway and JWT
* Pros:
	- Compact and lightweight
	- Low pressure on Auth server
	- Simplify the implementation of auth server
* Cons:
	- Could not invalidate a JWT token if it has been leaked
	- JWT might become big

![Gateway and token](./images/security_gateway_jwt.png)

#### JWT
##### Motivation
* JWT is a type of by value token which does not need to be verified at the authorization server. 
	- Def for by reference token: Randomly generated string value. Upon receiving the token, resource server needs to verify it against OAuth authorization server to obtain information such as claims/scopes. 
	- Def for by value token: A token which contains key value pair of (issuer, audience, scope, claims). It could be verified locally and does not need to be verified against authorization server. 

##### Types of JWT

* HMAC JWT

![Gateway and token](./images/security_gateway_jwt_hmac.png)

* RSA JWT

![Gateway and token](./images/security_gateway_jwt_rsa.png)


## OAuth2
### Simple guide
* Simlplest guide: https://medium.com/@darutk/the-simplest-guide-to-oauth-2-0-8c71bd9a15bb

### Roles
* Third party client app: it needs to access users' protected resource
* Resource server: a web server which expose users' protected resource to outside users
* Authorization server: issue access token to client app after resource owner grant the permission
* Resource owner: the owner of a resource who wants to share it with third party apps.

### Types of token
#### Access token
* Used to access target resource.

#### Refresh token
* Used to exchange for a refreshed token

#### Authorization code token
* Only used for authorization token, used for obtaining access token and refresh token. 

#### Bearer token
* It is like cash. Whoever has the token could access target resource. 

#### Proof of possession token
* Could verify whether client has ownership for the token.

### OAuth 2 modes
* Good reference: Ruanyifeng overview - http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html
* Choose between OAuth modes: 

![Choose between](./images/security_oauth_chooseMode.svg) 

#### Authorization code mode

![Authorization code mode](./images/security_oauth_authorizationcodemode.png) 

#### Simplified mode

![simplified mode](./images/security_oauth_simplifiedmode.png) 

#### Password mode

![password mode](./images/security_oauth_clientmode.png) 

#### Refresh token mode

![refresh token mode](./images/security_oauth_refreshtokenmode.png) 

## OpenID Connect
* The authentication layer on top of OAuth2

## Microservices security architecture
### External access token internal JWT token
* Cons: Still rely on gateway to switch access token with JWT token. 

![first arch](./images/security_futureMicroservice_firstArch.png) 

### Encrypted JWT token
* Pros: Stateless token

![second arch](./images/security_futureMicroservice_secArch.png) 

### External access token internal JWT token with token cache
* Most widely used in practice

![third arch](./images/security_futureMicroservice_thirdArch.png) 




