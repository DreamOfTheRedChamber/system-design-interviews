# Messenger

<!-- MarkdownTOC -->

- [Scenario](#scenario)
	- [One to one chatting](#one-to-one-chatting)
	- [Group chatting](#group-chatting)
	- [User online status](#user-online-status)
	- [History view](#history-view)
	- [Log in from multiple devices](#log-in-from-multiple-devices)
- [Service](#service)
	- [Message service](#message-service)
	- [Real-time service](#real-time-service)
- [Storage](#storage)
	- [Message table](#message-table)
	- [Thread table](#thread-table)
	- [Work solution](#work-solution)
- [Scale](#scale)
	- [Sharding](#sharding)
	- [How to speed up](#how-to-speed-up)
	- [How to support large group chat?](#how-to-support-large-group-chat)
	- [How to check / update online status](#how-to-check--update-online-status)

<!-- /MarkdownTOC -->


## Scenario
### One to one chatting
### Group chatting
### User online status
### History view
### Log in from multiple devices

## Service
### Message service
### Real-time service

## Storage
### Message table
* NoSQL because large amounts of data and no modification required
* Schema
	- messageId integer userID+Timestamp
	- threadId integer
	- userID integer
	- content text
	- createdAt timestamp

### Thread table
* SQL - need to support multiple index
* Schema
	- userId integer
	- threadId	integer  createUserId + timestamp
	- participantsId text json
	- participantsHash string avoid duplicates threads
	- CreatedAt timestamp
	- updatedAt timestamp index=true

### Work solution
* Sender sends message and message receiverId to server
* How does user receives information
	- Pull server every 10 second

## Scale
### Sharding
* According to userId

### How to speed up
* Socket connection for realtime push service

### How to support large group chat?
* Add a channel service

### How to check / update online status

