# Messenger

<!-- MarkdownTOC -->

- [Scenario](#scenario)
	- [Core features](#core-features)
	- [Common features](#common-features)
	- [Initial solution](#initial-solution)
- [Message service](#message-service)
	- [Estimation](#estimation)
		- [Small scale](#small-scale)
		- [Large scale](#large-scale)
	- [Features](#features)
		- [Real-time](#real-time)
			- [Approaches](#approaches)
			- [Long Poll](#long-poll)
		- [Reliability](#reliability)
			- [Scenario](#scenario-1)
			- [App layer acknowledgement](#app-layer-acknowledgement)
			- [Network stability](#network-stability)
		- [Consistency](#consistency)
			- [How to find a global time order](#how-to-find-a-global-time-order)
		- [Security](#security)
			- [Transmission security](#transmission-security)
			- [Storage security](#storage-security)
			- [Content security](#content-security)
		- [Optimize for multi-media](#optimize-for-multi-media)
			- [Upload](#upload)
			- [Send](#send)
	- [Data schema](#data-schema)
		- [One-on-One chat schema](#one-on-one-chat-schema)
			- [Requirements](#requirements)
			- [Basic design: Message table](#basic-design-message-table)
		- [Group chat schema](#group-chat-schema)
			- [Requirements](#requirements-1)
			- [Basic design: Message and thread table](#basic-design-message-and-thread-table)
			- [Optimization: User-specific thread](#optimization-user-specific-thread)
	- [Storage](#storage)
		- [SQL vs NoSQL](#sql-vs-nosql)
- [Connection service](#connection-service)
- [Business logic service](#business-logic-service)
- [Notification](#notification)
	- [Online status](#online-status)
		- [Online status pull](#online-status-pull)
		- [Performance bottleneck](#performance-bottleneck)
	- [Message delivery](#message-delivery)
		- [Two modes](#two-modes)
		- [Offline message push](#offline-message-push)
	- [Third party service](#third-party-service)
- [Unread messages](#unread-messages)
	- [Separate storage](#separate-storage)
	- [Inconsistency](#inconsistency)
- [Support large group](#support-large-group)
- [Sync history msg from any device](#sync-history-msg-from-any-device)
- [Industry solutions](#industry-solutions)
	- [Client vs server side storage](#client-vs-server-side-storage)
	- [Slack](#slack)
	- [Hipchat](#hipchat)
	- [Facebook](#facebook)
	- [Discord](#discord)
	- [MirrorFly](#mirrorfly)

<!-- /MarkdownTOC -->

# Scenario
## Core features
* One to one chatting
* Group chatting
* User online status

## Common features
* History info
* Log in from multiple devices
* Friendship / Contact book

## Initial solution
* Sender sends message and message receiverId to server
* Server creates a thread for each receiver and message sender
* Server creates a new message (with thread_id)
* How does user receives information
	- Pull server every 10 second

# Message service
## Estimation
### Small scale
* DAU: 2000, Suppose 50 messages / day per user
* QPS: 
	- 2000 * 50 / 86400 = 1.2
* Storage: 
	- 2000 * 50 * 100 bytes = 10 MB/day = 3.6GB / year

### Large scale
* DAU: 500M, Suppose 50 messages / day per user (Facebook 1.66 billion)
* QPS: 
	- Average QPS = 500M * 50 / 86400 ~ 0.3M 
	- Peak QPS = 0.3M * 3 = 1M
* Storage: 
	- 500M * 50 * 100 Bytes = 2.5 TB/day = 1PB / year

## Features
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
* How does long poll find user's connection among so many long polls
	- There will be a user sign-in process
		- A TCP connection is set after three time hand shake. 
		- Client sends a request based on the connection. 
		- Server interprets the connection. If valid, it will save the mapping between uid and tcp connection socket descriptor. 
		- This descriptor will be saved on local cache or distributed cache. 
* Why maintain long poll connection via heartbeat messages? 
	- This long connection is a virtual connection. How do connection parties know when there is something wrong? 
	- Reduce the connection resource consumption on IM server side
		- Server will maintain a mapping between user device and network connection
		- Server will cache some client info such as app version, os version so that client does not need to pass those information every time
		- If no exception are detected, server will try to push notifications along these corrupted long connection channels, wasting a lot of resources. 
	- Notify the client to reconnect if not receiving the ack of heartbeat msgs after timeout. 
	- To make the long connection live longer
		- Even without any network errors on client and server side, there will be a NAT process happening within network operators. 
		- The NAT process is to transform the internal IP address to external IP address because there are only limited IPv4 addresses. 
		- For optimizing the performance and reduce the resource consumption on network operator devices, some network operators will clear the mapping within NAT if there isn't any msg being sent on the connection. 
* Approaches for heartbeat msgs
	- TCP keepalive heartbeat
		- Pros: Supported by TCP/IP protocol. Disabled by default. Three parameters to be configured: heart beat cycle, number of retries, timeout period. 
		- Cons: Low flexibility in tuning the heartbeat cycle period (always fixed cycle period); Network layer available does not mean application layer available. 
		- Used a lot in scenarios of starting IM servers. e.g. whatsapp
	- Application layer heartbeat
		- Application layer sends heartbeat msgs after certain period.
		- Cons: Will have some additional data transmission cost because not supported natively by TCP/IP protocol.
		- Pro: More flexibility in tuning the heartbeat cycle period; reflect whether the application is avaialble. 
		- Used in most IM servers. 

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

#### Network stability
* Use public allowed ports when possible: 80, 8080, 443, 14000
* Http Tunnel: Use Http protocol to encapsulate other incompatible protocols
* Multi IP addresses: Rely on HttpDNS to return multiple IP addresses
* Connection fast
	- Reduce the latency among multi network operators
	- Race among multiple endpoints: After multiple IP addresses returned by HttpDNS, benchmark against different endpoints. 
* Separating upload and download tunnel: 
	- In case of broadcasting, there will be lots of msgs being sent in the downward channel. 
	- Could use short connection in upload channel, long connection in download channel. 

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

### Optimize for multi-media
#### Upload
* Picture/Video/Voice: 
	- Picture/Video media: Have a dedicated channel for video and picture media. After media (video/picture) is uploaded to the storage, a unique ID will be generated and used along with messages. 
	- Voice media：There is no miniature for preview. Voice media will be transmitted in the same channel as message team. 
* Divide and send:
	- Size of the divide: Divide too big, not enough parrallelism; Divide too small, too many TCP connections and increased cost for merge.
	- Typical size of pieces: WiFi 2M; 4G 1M; 3G/2G 256K. 
	- Since the size of media is big, it will be beneficial to divide and retransmit. 
* Dedupe the media
	- Compute the hash for media before uploading

#### Send
* Prerequisites for supporting watch while send
	1. Format and key frame info is at the top of file. 
	2. Storage support range queries. 
		- Ali OSS / Tencent COS, support range queries
		- Utilize load balance layer range query. (Nginx HTTP Slice)
* CDN
	- Encryption with HLS. 
* Compression
	- Image compression
		* Adaptive resolution
		* WebP: WebP is roughly 30% smaller than PNG/JPEG. Cons is not easy to be integrated on iOS platform / JPEG 		
		* JPEG: Two types of JPEG ??? 
	- Video: 
		* H.265 is 50% less than H.264. But encoding/decoding much more time consuming. 

## Data schema
### One-on-One chat schema
#### Requirements
* Load all recent conversations according to the last updated timestamp
* For each conversation, load all messages within that conversation according to the message create timestamp

#### Basic design: Message table
* The message table is as follows:
	- Create timestamp could be used to load all conversations after certain date

| Columns   | Type      | Example          | 
|-----------|-----------|------------------| 
| messageId | integer   |  1001   | 
| from_user_id  | integer   | sender  | 
| to_user_id  | integer   | receiver  | 
| content  | string   | hello world | 
| create_at  | timestamp   | 2019-07-15 12:00:00 | 

* Cons: 
	- Determine the thread_list to be displayed
	- To load all messages in a chat, the following query needs to be executed. The query has a lot of where clause
	- Suppose to be used in a group chat scenario. The same message needs to copied multiple times for different to_user_id. Not easy to be extended to group chat schema

```
// determine the thread list, meaning the to_user_id below
$contactList = select to_user_id from message_table
				where from_user_id = A

// for each contact, fetch all messages
select * from message_table 
where from_user_id = A and to_user_id = B 
	  or from_user_id = B and to_user_id = A
order by create_at desc

// insert message is simple
```

### Group chat schema
#### Requirements
* Query all group conversations the user participate in according to the last updated timestamp
* For each conversation, load all messages within that conversation according to the message create timestamp

#### Basic design: Message and thread table
* Intuition: 
	1. To be extensible for group chat, to_user_id could be extended as participants_ids
	2. Currently a conversation is identified by a combined query of from_user_id and to_user_id, which results in a lot of query overhead. Give a conversation a unique id so that all messages withinn that conversation could be easily retrieved. 
	3. Since participants_ids in Message table is not a field used frequently according to the query, we could extract that and put it in a separate Thread table. 

* Message table

| Columns   | Type      | Example          | 
|-----------|-----------|------------------| 
| messageId | integer   |  1001   | 
| thread_id  | integer   | createUserId + timestamp  | 
| user_id  | integer   | sender  | 
| content  | string   | 2019-07-15 12:00:00 | 
| create_at  | timestamp   | 2019-07-15 12:00:00 | 

* Thread table
	- update_at could be used to sort all threads. 

| Columns   | Type      | Example          | 
|-----------|-----------|------------------| 
| thread_id | integer   |  createUserId + timestamp   | 
| participants_ids  | text   | conversation id  | 
| participantsHash | string    | avoid duplicates threads | 
| create_at  | timestamp   | 2019-07-15 12:00:00 | 
| update_at  | timestamp   | 2019-07-15 12:00:00 | 

* Queries

```
// determine the thread list, meaning the to_user_id below
$threadId_list = select thread_id from message_table
where user_id == A

// for each thread_id inside threadId_list
select * from message_table 
where thread_id = A
order by create_at desc

// Display all participants for each thread
$participantsId_list = select participants_ids from thread_table
where thread_id in $threadId_list
order by update_at desc
```

* Pros:
	- Easy to be extended to a group chat scenario because to_user_id has been replaced with participants_ids. 
	- To load all messages in a chat, could query only the thread_id in message table. 
	- To order all threads for a user, could query only the update_at in thread table. 

* Cons:
	- There is no place to store information such as the user mutes the thread. 

#### Optimization: User-specific thread
* Intuition:
	- Expand the thread table with three additional fields including owner_id, ismuted, nickname
* Message table

| Columns   | Type      | Example          | 
|-----------|-----------|------------------| 
| messageId | integer   |  1001   | 
| thread_id  | integer   | createUserId + timestamp | 
| user_id  | integer   | sender  | 
| content  | string   | 2019-07-15 12:00:00 | 
| create_at  | timestamp   | 2019-07-15 12:00:00 | 

* Thread table
	- update_at could be used to sort all threads. 
	- Needs to support multi-index. (SQL will be a better fit)
	    + Owner user Id: Search all of chat participated by a user
	    + Thread id: Get all detailed info about a thread (e.g. label)
	    + Participants hash: Find whether a certain group of persons already has a chat group
	    + Updated time: Order chats by update time

| Columns   | Type      | Example          | 
|-----------|-----------|------------------| 
| **owner_id** | integer   |  1001   | 
| thread_id  | integer   | createUserId + timestamp | 
| participants_ids  | json   | conversation id  | 
| participantsHash | string  | avoid duplicates threads | 
| **ismuted**  | bool   | personal setting  | 
| **nickname**  | text   | conversation id  | 
| create_at  | timestamp   | 2019-07-15 12:00:00 | 
| update_at  | timestamp   | 2019-07-15 12:00:00 | 

* Queries

```
// determine the thread list, meaning the to_user_id below
$threadId_list = select thread_id from thread_table
where owner_id == A

// for each thread_id inside threadId_list
select * from message_table 
where thread_id = A
order by create_at desc

// Display all participants for each thread
$participantsId_list = select participants_ids from thread_table
where thread_id in $threadId_list
order by update_at desc
```

## Storage
### SQL vs NoSQL
* Message table
	- NoSQL. Do not need to take care of sharding/replica. Just need to do some configuration. 
* Thread table
	- According to userId. 
	- Why not according to threadId?
		+ To make the most frequent queries more efficient: Select * from thread table where user_id = XX order by updatedAt


# Connection service
* Goal
	* Keep the connection
	* Interpret the protocol. e.g. Protobuf
	* Maintain the session. e.g. which user is at which TCP connection
	* Forward the message. 

* Why separating connection service
	* This layer is only responsible for keeping the connection with client. It doesn't need to be changed on as often as business logic pieces.
	* If the connection is not on a stable basis, then clients need to reconnect on a constant basis, which will result in message sent failure, notification push delay. 
	* From management perspective, developers working on core business logic no longer needs to consider network protocols (encoding/decoding)

# Business logic service
* The number of unread message
* Update the recent contacts


# Notification
## Online status
### Online status pull
* When users become online, send a heartbeat msg to the server every 3-5 seconds. 
* The server sends its online status to friends every 3-5 seconds. 
* If after 1 min, the server does not receive the heartbeat msg, considers the user is already offline. 

### Performance bottleneck
* A central connection service for maintaining user online status and network gateway the user resides in
	- Instead, use a message queue, ask all connection service to subscribe to this message queue. [STILL SOME QUESTIONS 存储和并发：万人群聊系统设计中的几个难点]
	- This mechanism shifts the pressure from business logic layer to connection service layer. 

## Message delivery
### Two modes
* User online: Push message via long poll connection
* User offline: Push message via APNs

### Offline message push 
* When many offline messages need to be pushed to the end-user, there is a need to resort msgs.
* The entire process for sending offline msgs
	1. The connection layer (network gateway) will subscribe to the redis topic for offline msgs. 
	2. User goes online. 
	3. The connection layer (network gateway) will notify business layer that the user is online.
	4. The business layer will publish msgs to redis topic for offline msgs. 
	5. Redis will fan out the offline messages to the connection layer. (The rearrangement happens on this layer)
	6. The conneciton layer will push the message to clients. 

## Third party service
* To make sure that users could still receive notifications when the app is running in the background or not openned, third party notification (Apple Push Notification Service / Google Cloud Messaging) will be used. 

# Unread messages
## Separate storage
* Total unread message and unread message against a specific person
	- Usage scenarios are different

## Inconsistency
* Why inconsistency will occur in the first place?
	- Total unread message increment and unread message against a specific person are two atomic operations. One could fail while the other one succeed. Or other clearing operations are being executed between these two operations. 
* Solution:
	- Distributed lock
		* MC add
		* Redis setNX
	- Transaction
		* Redis's MULTI, DISCARD, EXEC and WATCH operations. Optimistic lock. 		
	- Lua script

# Support large group  
* Problem: Suppose that there is a 5000 people group and there are 10 persons speaking within the group, then QPS for updating unread messges will be 5W; When there are 500 such groups, the QPS will be 500W. 
* Solution: Aggregate and update
	1. There will be multiple queues A/B/C/... for buffering all incoming requests. 
	2. Two components will be pulling from queues
		- Timer: Will be triggered after certain time
		- Flusher: Will be triggered if any of the queue exceed a certain length
	3. Aggregator service will pull msgs from Timer and Flusher, aggregate the read increment and decrement operations

# Sync history msg from any device
* Two modes
	- For multiple devices logging in at the same time, IM server needs to maintain a set of online website. 
	- For offline msgs, 
* Offline msg storage
	1. User A sends a msg to User B. 
	2. Connection layer receives the msg and sent it to business logic laer
	3. Business logic layer stores the offline msg
	4. User B device 1 comes online
	5. Connection layer update the status code of User B's device online status
	6. Connection layers ask business layer for offline msgs User B receive
	7. Business layer looks for offline msgs
	8. Connection layer push the found offline msgs to User B device. 
* How to store offline msgs
	- Offline msgs should not be stored together with normal online msgs because
		* Offline msgs will contain operation instructions which will not be persisted in online cases.
		* The data model for msg index table is based on two parties. The data model for offline msg is based on single unique user. 
		* The offline message only have a certain retention period or upper limit. 
* How to pull offline msgs based on needs
	1. User A sends a msg to User B. 
	2. IM server changes User B's version number VERSION-LATEST within the version service. 
	3. IM server saves the msg along with its version number VERSION-LATEST. 
	4. User B comes online with its latest version number VERSION-OLD. 
	5. IM server compares the two version numbers VERSION-OLD and VERSION-LATEST. 
	6. IM server obtains the offline msgs for User B. 
	7. IM server pushes the offline msgs to User B. 
* What if the offline storage exceeds the maximum limit
	- It could goes back to the msg index table 

# Industry solutions
## Client vs server side storage
* Client-side database: 
	- Quite effective in minimizing the data stored in the database by holding the data within the device 
	- Example: whatsapp and viber
* Server-side database: 
	- web chat providers for collaboration in the market are built on the server-side database
	- Example: Slack, Hipchat

## Slack
* Slack use MySQL as backend with sharding techniques
* [How Slack build shared channels](https://slack.engineering/how-slack-built-shared-channels-8d42c895b19f)
* [Scaling slack](https://www.infoq.com/presentations/slack-scalability-2018/)

## Hipchat
* Elastic
	- 60 messages per second
	- 1.2 billion documents stored
* [How HipChat stores and indexes billions of messages using elasticSearch](http://highscalability.com/blog/2014/1/6/how-hipchat-stores-and-indexes-billions-of-messages-using-el.html)
	- Compatible with Lucene and reduce the number of components 

## Facebook
* Evolution process
	1. Start with MySQL and Memcached
	2. TAO - A FB-specific NoSQL graph API built to run on sharded MySQL
* https://blog.yugabyte.com/facebooks-user-db-is-it-sql-or-nosql/

## Discord
* Cassandra: KKV store
	- channel_id as the partition key
	- message_id as the clustering key

```
CREATE TABLE messages (
  channel_id bigint,
  message_id bigint,
  author_id bigint,
  content text,
  PRIMARY KEY (channel_id, message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);
```

## MirrorFly
* [Basic MirrorFly architecture](https://www.codementor.io/@vigneshwaranb/why-enterprise-chat-apps-isn-t-built-on-server-side-database-like-hangouts-slack-hipchat-10kqdft9xg)
* In a group chat application, the number of messages relayed between the server and client is large, message queuing will be one of the most destructive issues. To handle the message queuing in the servers, MUC & PubSup was introduced to handle the multi-user messaging. MUC (Multi-user Chat) XMPP protocol designed for multiple users to communicate simultaneously and PubSup for senders to send messages directly to receivers.