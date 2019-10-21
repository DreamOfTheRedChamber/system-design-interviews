# Messenger

<!-- MarkdownTOC -->

- [Scenario](#scenario)
	- [Core features](#core-features)
		- [One to one chatting](#one-to-one-chatting)
		- [Group chatting](#group-chatting)
		- [User online status](#user-online-status)
	- [Common features](#common-features)
		- [History info](#history-info)
		- [Log in from multiple devices](#log-in-from-multiple-devices)
		- [Friendship / Contact book](#friendship--contact-book)
	- [Initial solution](#initial-solution)
	- [Charateristics](#charateristics)
		- [Real-time](#real-time)
			- [Approaches](#approaches)
			- [Long Poll](#long-poll)
		- [Reliability](#reliability)
			- [Scenario](#scenario-1)
			- [App layer acknowledgement](#app-layer-acknowledgement)
		- [Consistency](#consistency)
			- [How to find a global time order](#how-to-find-a-global-time-order)
			- [Rerange message](#rerange-message)
			- [Total unread message and unread message against a specific person](#total-unread-message-and-unread-message-against-a-specific-person)
		- [Security](#security)
			- [Transmission security](#transmission-security)
			- [Storage security](#storage-security)
			- [Content security](#content-security)
- [Estimation](#estimation)
	- [System components](#system-components)
		- [Client](#client)
	- [Service](#service)
		- [Connection service](#connection-service)
			- [Why separating connection service](#why-separating-connection-service)
			- [Core functionalities](#core-functionalities)
		- [Business logic service](#business-logic-service)
		- [Third party service](#third-party-service)
	- [Storage](#storage)
		- [Message table](#message-table)
		- [Thread table](#thread-table)
- [Scale](#scale)
	- [Sharding](#sharding)
	- [Speed up with Push service](#speed-up-with-push-service)
		- [Socket](#socket)
		- [Push service](#push-service)
			- [Initialization and termination](#initialization-and-termination)
			- [Number of push servers](#number-of-push-servers)
			- [Steps](#steps)
	- [Channel service](#channel-service)
	- [How to check / update online status](#how-to-check--update-online-status)
		- [Online status pull](#online-status-pull)
		- [Online status push](#online-status-push)

<!-- /MarkdownTOC -->

# Scenario
## Core features
### One to one chatting
### Group chatting
### User online status

## Common features
### History info
### Log in from multiple devices
### Friendship / Contact book

## Initial solution
* Sender sends message and message receiverId to server
* Server creates a thread for each receiver and message sender
* Server creates a new message (with thread_id)
* How does user receives information
	- Pull server every 10 second

## Charateristics
### Real-time
#### Approaches
* Periodical short poll: The initial solution relies on a short periodical polling process. 
	- Cons: Periodic polling is usually done on a high frequency. It wastes client devices' electricity because most polling are useless. It puts high pressure on server resources and implies a high QPS. 
* Periodical long poll: The difference with short poll is that the client request does not return immediately after the request reaches the server. Instead, it hangs on the connection for a certain period of time. If there is any incoming messages during the hanging period, it could be returned immediately. 
	- Cons: Hanging on the server for a period reduces the QPS but does not really reduce the pressure on server resources such as thread pool. (If there are 1000 connections, server side still needs to have 1000 threads handling the connection.) 
* Websocket: Client and server need one-time handshake for bi-directional data transfer. TODO: HOW DOES WEBSOCKET WORK INTERNALLy
	- Pros: 
		- Support bidirectional communication
		- Reduce the setup time 
		- Support natively by the web
* Many other protocols based on TCP long connection such as XMPP/MQTT. 
	- XMPP is mature and easy to extend. But the XML based transfer schema consumes a lot of network bandwidth. 
	- MQTT is based on pub/sub mode, reserve network bandwidth, easy to extend. But it is not a protocol for IM so does not support many IM features such as group chatting, offline messages. 

#### Long Poll	
* HOW DOES LONG TCP CONNECTION FIND USER's CONNECTION WHEN THE USER IS ON. 
	- There will be a user sign-in process
		- A TCP connection is set after three time hand shake. 
		- Client sends a request based on the connection. 
		- Server interprets the connection. If valid, it will save the mapping between uid and tcp connection socket descriptor. 
		- This descriptor will be saved on local cache or distributed cache. 

### Reliability
#### Scenario
* Among the IM forwarding model, the process of User A send a message to User B consists of the following steps:
	1. User A sends a msg to IM server (possible failure: the request failed midway)
	2. IM server stores the msg (possible failure: fails to store the message)
	3. IM server sends User A an acknowledge (possible failure: the server does not return within timeout period)
	4. IM server forwards the msg to User B (possible failure: after writing the msg to kernel send space, the server gets suspended because of power outage / User B receives the message but there is an exception happening resulting in message not put into queue.)
		1. When IM server forwards a msg to User B, it will carry a unique SID. This unique SID will be put inside an acknowledgement list (possible failure: the message never reaches user b's device because network is down).
		2. When User B receives the msg successfully, it will reply with an ACK package (possible failure: the acknowledgement package gets lost in the midway / User B's device gets corrupted before it could send an acknowledgement package).
		3. IM server will delete the msg with unique SID from the acknowledgement list (possible failure: ). 
* For possible failures within 1/2/3) step above, the failure could be mitigated by 
	- Client retries after timeout
	- Server implements the de-duplication mechanism 
* For failure within 4) step above: Business layer acknowledgement
	- IM server will maintain an acknowledgement list with a timeout. If it does not get an acknowledgement package from user B, it will retry the message from the acknowledgement list. 

#### App layer acknowledgement
* What if the IM gets corrupted when it is resending the msg: Unique msg sequence id for guaranteeing the completeness 
	1. IM server forwards a msg MESSAGE to User B, it carries a unique sequence id SEQ1. 
	2. When user B gets the msg MESSAGE, it update its local msg sequence id to SEQ1. 
	3. IM server forwards another msg MESSAGE to User B, it  carries another unique sequence id SEQ2. 
	4. msg MESSAGE gets lost because User B is getting disconnected from the network. 
	5. User B reconnects online, carrying the latest local msg sequence id SEQ1 to IM server. 
	6. IM server detects that User B needs more msgs, so it delivers all of msgs with sequence id between SEQ1 and SEQ2. 
	7. User B receives the msg and update the local sequence id to SEQ2. 
* Why needs an acknowledgement even if TCP layer already acknowledges msg:
	* These acknowledgement are at different layers. TCP acknowledgement is at network layer. App layer acknowledgement happens at acknowledge layer. There could be some error happening during the process from network layer to app layer. 

### Consistency
* Multi sender, receiver, multi-thread

#### How to find a global time order
* Sender's timestamp or sequence number? No because
	- Senders' timestamp are not synced
	- Sender's sequence number could be cleared after a reset
* IM server's timestamp? No because
	- Usually IM server will be a cluster and the clock is synced using NTP. 
	- When the cluster size is really big, it is challenging to maintain uniqueness.
* IM server's sequence number? 
	- Could be implemented using Redis' incr instruction, DB's auto increment id, Twitter's snowflake algo
	- Redis' incr / DB's auto increment need to happen on the master node, leading to low performance. 
	- Snowflake algorithm also has the problem of syncing the clock. 
* Not so accurate global unique sequence number? 
	- From the product's perspective, there is no need for a global unique sequence number. As long as there is a unique global sequence number per messaging group, it will be good enough. 
* Problem not completely with unique sequence number per group because
	- IM servers are deployed on a cluster basis. Every machine's performance will be different and different IM servers could be in different states, such as in GC. A message with bigger sequence number could be sent later than another message smaller sequence number. 
	- For a single IM server receiving a msg, the processing will be based on multi-thread basis. It could not be guaranteed that a message with bigger sequence number will be sent to receiver earlier than a message with lower sequence number. 

#### Rerange message 
* When many offline messages need to be pushed to the end-user, there is a need to resort msgs.
* The entire process for sending offline msgs
	1. The connection layer (network gateway) will subscribe to the redis topic for offline msgs. 
	2. User goes online. 
	3. The connection layer (network gateway) will notify business layer that the user is online.
	4. The business layer will publish msgs to redis topic for offline msgs. 
	5. Redis will fan out the offline messages to the connection layer. (The rearrangement happens on this layer)
	6. The conneciton layer will push the message to clients. 

#### Total unread message and unread message against a specific person
* Why inconsistency will occur in the first place?
	- Total unread message increment and unread message against a specific person are two atomic operations. One could fail while the other one succeed. Or other clearing operations are being executed between these two operations. 
* Solution:
	- Distributed lock
		* MC add
		* Redis setNX
	- Transaction
		* Redis's MULTI, DISCARD, EXEC and WATCH operations. Optimistic lock. 		
	- Lua script

### Security
#### Transmission security
* Entrance security: 
	- Router's DNS hijacked: DNS location is set to a location with virus. 
	- Operator's local DNS hijacked: 
		- Operator might send DNS requests to other operators to reduce the resource consumption
		- Operator might modify the TTL for DNS 
	- Ways to prevent DNS from being hijacked
		- HttpDNS protocol: Prevent domain name from being hijacked by operators. It uses HTTP protocol instead of UDP to directly interact with DNS servers. 
		- Combine HttpDNS with localDNS. 		
* TLS transmission layer security: 
	- Cut off network
		- Failover to multiple connection IP address returned by HttpDNS service
		- Change from UDP based QUIC protocol to TCP protocol
	- Intercept/Man in the middle/Forfeit: Use TLS protocol
		- Insymetric encryption and key exchange algorithm are used to guarantee message encryption key not being corrupted or leaked. 
		- Symmetric encryption is used to guarantee that the msg could not be decrypted after being intercepted. 
		- Digital signature and CA certificate could be used to verify the valid status of public key. 

#### Storage security
* Account credentials: Hashing algorithm with salt.
* Message security: End to end encryption

#### Content security
* Link to external phishing website
* Crawler

# Estimation
* DAU: 100M 
* QPS: Suppose a user posts 20 messages / day
	- Average QPS = 100M * 20 / 86400 ~ 20k
	- Peak QPS = 20k * 5 = 100k
* Storage: Suppose A user sends 20 messages / day
	- 100M * 20 * 30 Bytes = 30G

## System components
### Client
* Mobile/Web/Desktop

## Service
### Connection service
#### Why separating connection service
* This layer is only responsible for keeping the connection with client. It doesn't need to be changed on as often as business logic pieces.
* If the connection is not on a stable basis, then clients need to reconnect on a constant basis, which will result in message sent failure, notification push delay. 
* From management perspective, developers working on core business logic no longer needs to consider network protocols (encoding/decoding)

#### Core functionalities
* Keep the connection
* Interpret the protocol. e.g. Protobuf
* Maintain the session. e.g. which user is at which TCP connection
* Forward the message. 

### Business logic service
* The number of unread message
* Update the recent contacts

### Third party service
* To make sure that users could still receive notifications when the app is running in the background or not openned, third party notification (Apple Push Notification Service / Google Cloud Messaging) will be used. 

## Storage
### Message table
* NoSQL database
	- Large amounts of data
	- No modification required
* Schema

| Columns   | Type      |                  | 
|-----------|-----------|------------------| 
| messageId | integer   | userID+Timestamp | 
| threadId  | integer   | the thread it belongs. Foreign key  | 
| userId    | integer   |                  | 
| content   | text      |                  | 
| createdAt | timestamp |                  | 

### Thread table
* SQL database
    - Need to support multiple index
    - Index by 
    	+ Owner user Id: Search all of chat participated by me
    	+ Thread id: Get all detailed info about a thread (e.g. label)
    	+ Participants hash: Find whether a certain group of persons already has a chat group
    	+ Updated time: Order chats by update time
* Schema
	- Primary key is userId + threadId
		+ Why not use UUID as primary key? Need sharding. Not possible to maintain a global ID across different machines. Use UUID, really low efficiency.

| Columns          | Type      |                          | 
|------------------|-----------|--------------------------| 
| userId           | integer   |                          | 
| threadId         | integer   | createUserId + timestamp | 
| participantsId   | text      | json                     | 
| participantsHash | string    | avoid duplicates threads | 
| createdAt        | timestamp |                          | 
| updatedAt        | timestamp | index=true               | 
| label            | string    |                          | 
| mute             | boolean   |                          | 

# Scale
## Sharding
* Message table
	- NoSQL. Do not need to take care of sharding/replica. Just need to do some configuration. 
* Thread table
	- According to userId. 
	- Why not according to threadId?
		+ To make the most frequent queries more efficient: Select * from thread table where user_id = XX order by updatedAt

## Speed up with Push service
### Socket
* HTTP vs Socket
	- HTTP: Only client can ask server for data
	- Socket: Server could push data to client
* What if user A does not connect to server
	- Relies on Android GCM/IOS APNS

### Push service
#### Initialization and termination
* When a user opens the app, the user connects to one of the socket in Push Service
* If a user is inactive for a long period, drops the connection. 

#### Number of push servers
* Each socket connection needs a specific port. So needs a lot of push servers. 
	- The traffic could be sharded according to user_id

#### Steps
1. User A opens the App, it asks the address of push server.
2. User A stays in touch with push server by socket. 
3. User B sends msg to User A. msg is first sent to the message server.
4. Message service finds the responsible push service according to the user id.
5. Push service sends the msg to A.

## Channel service
* In case of large group chatting
	- If there are 500 people in a group, message service needs to send the message to 500 push service. If most of receivers within push service are not connected, it means huge waste of resources. 
* Add a channel service
	- Each thread should have an additional field called channel. Channel service knows which users are online/offline. For big groups, online users need to first subscribe to corresponding channels. 
		+ When users become online, message service finds the users' associated channel and notify channel service to subscribe. When users become offline, push service knows that the user is offline and will notify channel service to subscribe. 
	- Channel service stores all info inside memory. 

## How to check / update online status
### Online status pull
* When users become online, send a heartbeat msg to the server every 3-5 seconds. 
* The server sends its online status to friends every 3-5 seconds. 
* If after 1 min, the server does not receive the heartbeat msg, considers the user is already offline. 

### Online status push
* Wasteful because most users are not online
