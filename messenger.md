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
- [Estimation](#estimation)
- [Service](#service)
	- [Message service](#message-service)
- [Storage](#storage)
	- [Message table](#message-table)
	- [Thread table](#thread-table)
	- [Initial solution](#initial-solution)
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

## Scenario
### Core features
#### One to one chatting
#### Group chatting
#### User online status

### Common features
#### History info
#### Log in from multiple devices
#### Friendship / Contact book

## Estimation
* DAU: 100M 
* QPS: Suppose a user posts 20 messages / day
	- Average QPS = 100M * 20 / 86400 ~ 20k
	- Peak QPS = 20k * 5 = 100k
* Storage: Suppose A user sends 20 messages / day
	- 100M * 20 * 30 Bytes = 30G

## Service
### Message service

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

### Initial solution
* Sender sends message and message receiverId to server
* Server creates a thread for each receiver and message sender
* Server creates a new message (with thread_id)
* How does user receives information
	- Pull server every 10 second

## Scale
### Sharding
* Message table
	- NoSQL. Do not need to take care of sharding/replica. Just need to do some configuration. 
* Thread table
	- According to userId. 
	- Why not according to threadId?
		+ To make the most frequent queries more efficient: Select * from thread table where user_id = XX order by updatedAt

### Speed up with Push service
#### Socket
* HTTP vs Socket
	- HTTP: Only client can ask server for data
	- Socket: Server could push data to client
* What if user A does not connect to server
	- Relies on Android GCM/IOS APNS

#### Push service
##### Initialization and termination
* When a user opens the app, the user connects to one of the socket in Push Service
* If a user is inactive for a long period, drops the connection. 

##### Number of push servers
* Each socket connection needs a specific port. So needs a lot of push servers. 
	- The traffic could be sharded according to user_id

##### Steps
1. User A opens the App, it asks the address of push server.
2. User A stays in touch with push server by socket. 
3. User B sends msg to User A. msg is first sent to the message server.
4. Message service finds the responsible push service according to the user id.
5. Push service sends the msg to A.

### Channel service
* In case of large group chatting
	- If there are 500 people in a group, message service needs to send the message to 500 push service. If most of receivers within push service are not connected, it means huge waste of resources. 
* Add a channel service
	- Each thread should have an additional field called channel. Channel service knows which users are online/offline. For big groups, online users need to first subscribe to corresponding channels. 
		+ When users become online, message service finds the users' associated channel and notify channel service to subscribe. When users become offline, push service knows that the user is offline and will notify channel service to subscribe. 
	- Channel service stores all info inside memory. 

### How to check / update online status
#### Online status pull
* When users become online, send a heartbeat msg to the server every 3-5 seconds. 
* The server sends its online status to friends every 3-5 seconds. 
* If after 1 min, the server does not receive the heartbeat msg, considers the user is already offline. 

#### Online status push
* Wasteful because most users are not online
