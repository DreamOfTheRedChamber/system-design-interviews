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
	- [Require less latency](#require-less-latency)
	- [Sharding](#sharding)
	- [How to speed up](#how-to-speed-up)
	- [How to support large group chat?](#how-to-support-large-group-chat)
	- [How to check / update online status](#how-to-check--update-online-status)

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
### Require less latency
* 

### Sharding
* According to userId

### How to speed up
* Socket connection for realtime push service

### How to support large group chat?
* Add a channel service

### How to check / update online status

