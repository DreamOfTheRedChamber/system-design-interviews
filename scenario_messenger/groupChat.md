- [Naive solution](#naive-solution)
  - [Flowchart](#flowchart)
  - [Storage requirements](#storage-requirements)
  - [Naive Storage design: Single message table](#naive-storage-design-single-message-table)
  - [Improved Storage design: Message and thread table](#improved-storage-design-message-and-thread-table)
    - [Intuition](#intuition)
    - [Message table](#message-table)
    - [Thread table](#thread-table)
    - [Queries to load the recent contacts](#queries-to-load-the-recent-contacts)
    - [Pros](#pros)
    - [Cons](#cons)
  - [One-on-One chat schema](#one-on-one-chat-schema)
    - [Optimization: Message content should be decoupled from sender and receiver](#optimization-message-content-should-be-decoupled-from-sender-and-receiver)
    - [Optimization: Loading recent contacts should be faster](#optimization-loading-recent-contacts-should-be-faster)
  - [Group chat schema](#group-chat-schema)
    - [Optimization: User could customize properties on chat thread](#optimization-user-could-customize-properties-on-chat-thread)
    - [Optimization: Users who just joined could only see new messages](#optimization-users-who-just-joined-could-only-see-new-messages)
      - [SQL vs NoSQL](#sql-vs-nosql)
- [Real world](#real-world)
  - [Slack](#slack)
  - [LinkedIn](#linkedin)
  - [Weibo](#weibo)
  - [Facebook messenger](#facebook-messenger)

# Naive solution
## Flowchart
* Sender sends message and message receiverId to server
* Server creates a thread for each receiver and message sender
* Server creates a new message (with thread\_id)
* How does user receives information
  * Pull server every 10 second

## Storage requirements
* Query all group conversations the user participate in according to the last updated timestamp
* For each conversation, load all messages within that conversation according to the message create timestamp

## Naive Storage design: Single message table
* The message table is as follows:
  * Create timestamp could be used to load all conversations after certain date

| Columns        | Type      | Example             |
| -------------- | --------- | ------------------- |
| messageId      | integer   | 1001                |
| from\_user\_id | integer   | sender              |
| to\_user\_id   | integer   | receiver            |
| content        | string    | hello world         |
| create\_at     | timestamp | 2019-07-15 12:00:00 |

* Cons:
  * Determine the thread\_list to be displayed
  * To load all messages in a chat, the following query needs to be executed. The query has a lot of where clause
  * Suppose to be used in a group chat scenario. The same message needs to copied multiple times for different to\_user\_id. Not easy to be extended to group chat schema

* Query for getting all messages between user A and B:

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

## Improved Storage design: Message and thread table
### Intuition
1. To be extensible for group chat, to\_user\_id could be extended as participants\_ids 
2. Currently a conversation is identified by a combined query of from\_user\_id and to\_user\_id, which results in a lot of query overhead. Give a conversation a unique id so that all messages withinn that conversation could be easily retrieved. 
3. Since participants\_ids in Message table is not a field used frequently according to the query, we could extract that and put it in a separate Thread table.

### Message table

| Columns    | Type      | Example                  |
| ---------- | --------- | ------------------------ |
| messageId  | integer   | 1001                     |
| thread\_id | integer   | createUserId + timestamp |
| user\_id   | integer   | sender                   |
| content    | string    | 2019-07-15 12:00:00      |
| create\_at | timestamp | 2019-07-15 12:00:00      |

### Thread table
* update\_at could be used to sort all threads.

| Columns           | Type      | Example                  |
| ----------------- | --------- | ------------------------ |
| thread\_id        | integer   | createUserId + timestamp |
| participants\_ids | text      | conversation id          |
| participantsHash  | string    | avoid duplicates threads |
| create\_at        | timestamp | 2019-07-15 12:00:00      |
| update\_at        | timestamp | 2019-07-15 12:00:00      |

### Queries to load the recent contacts

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

### Pros
* Easy to be extended to a group chat scenario because to\_user\_id has been replaced with participants\_ids.
* To load all messages in a chat, could query only the thread\_id in message table.
* To order all threads for a user, could query only the update\_at in thread table.

### Cons
* There is no place to store information such as the user mutes the thread.


## One-on-One chat schema
### Optimization: Message content should be decoupled from sender and receiver

* Intuition:
  * Even if sender A deletes the message on his machine, the receiver B should still be able to see it
  * Create a message\_content table and message\_index table
* message\_content

| Columns    | Type      | Example             |
| ---------- | --------- | ------------------- |
| messageId  | integer   | 1001                |
| content    | string    | hello world         |
| create\_at | timestamp | 2019-07-15 12:00:00 |

* message\_index
  * ??? What are the reason isInbox is needed

| Columns        | Type    | Example                 |
| -------------- | ------- | ----------------------- |
| messageId      | string  | 1029                    |
| from\_user\_id | integer | sender                  |
| to\_user\_id   | integer | receiver                |
| isInbox        | integer | 1 (inbox) / 0 (sendbox) |

### Optimization: Loading recent contacts should be faster

* Intuition:
  * Loading recent contacts is a high frequent operation on every startup.
  * Querying recent contacts should not require querying the entire message\_index
  * Create a recent\_contacts table to separate the use case. Though schema looks similar, the differences between message\_index table are:
    * message\_index table stores the entire chat history and recent\_contacts only contains the most recent 1 chat
    * message\_index table is usually insertion operation while recent\_contacts is update operation

* recent\_contacts

| Columns        | Type    | Example  |
| -------------- | ------- | -------- |
| messageId      | string  | 1029     |
| from\_user\_id | integer | sender   |
| to\_user\_id   | integer | receiver |

## Group chat schema

### Optimization: User could customize properties on chat thread

* Intuition:
  * User could mute a chat thread. Create a customized name for a group chat.
  * Expand the thread table with three additional fields including owner\_id, ismuted, nickname
* Message table

| Columns    | Type      | Example                  |
| ---------- | --------- | ------------------------ |
| messageId  | integer   | 1001                     |
| thread\_id | integer   | createUserId + timestamp |
| user\_id   | integer   | sender                   |
| content    | string    | 2019-07-15 12:00:00      |
| create\_at | timestamp | 2019-07-15 12:00:00      |

* Thread table
  * update\_at could be used to sort all threads.
  * Needs to support multi-index. (SQL will be a better fit)
    * Owner user Id: Search all of chat participated by a user
    * Thread id: Get all detailed info about a thread (e.g. label)
    * Participants hash: Find whether a certain group of persons already has a chat group
    * Updated time: Order chats by update time

| Columns           | Type      | Example                  |
| ----------------- | --------- | ------------------------ |
| **owner\_id**     | integer   | 1001                     |
| thread\_id        | integer   | createUserId + timestamp |
| participants\_ids | json      | conversation id          |
| participantsHash  | string    | avoid duplicates threads |
| **ismuted**       | bool      | personal setting         |
| **nickname**      | text      | conversation id          |
| create\_at        | timestamp | 2019-07-15 12:00:00      |
| update\_at        | timestamp | 2019-07-15 12:00:00      |

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

### Optimization: Users who just joined could only see new messages

#### SQL vs NoSQL

* Message table
  * NoSQL. Do not need to take care of sharding/replica. Just need to do some configuration.
* Thread table
  * According to userId.
  * Why not according to threadId?
    * To make the most frequent queries more efficient: Select \* from thread table where user\_id = XX order by updatedAt

# Real world

## Slack
* Overview on Slack group messaging initial days: https://www.youtube.com/watch?v=WE9c9AZe-DY&ab_channel=InfoQ
* Flannel: Edge cache engine
  * https://www.youtube.com/watch?v=s4xgfT81BTg&ab_channel=GeekWire
  * Evolution of flannel: https://www.youtube.com/watch?v=x1Uz3rMlOBo&ab_channel=InfoQ
* Scaling slack: https://www.youtube.com/watch?v=C4AUHFhzYZo&ab_channel=SINFO
* Overview of scaling slack Flannel/Sharding/Microservices: https://www.infoq.com/presentations/slack-scalability-2018/
* Scaling slack infra Organization level questions: https://www.infoq.com/presentations/slack-scaling-infrastructure/

## LinkedIn
* Real time video messaging: https://www.infoq.com/presentations/linkedin-play-akka-distributed-systems/
* Instant Messaging at LinkedIn: Scaling to Hundreds of Thousands of Persistent Connections on One Machine: https://engineering.linkedin.com/blog/2016/10/instant-messaging-at-linkedin--scaling-to-hundreds-of-thousands-
* Now You See Me, Now You Don’t: LinkedIn’s Real-Time Presence Platform: https://engineering.linkedin.com/blog/2018/01/now-you-see-me--now-you-dont--linkedins-real-time-presence-platf


## Weibo
* 袁武林：微博消息系统架构演进 https://daxue.qq.com/content/content/id/2600

## Facebook messenger
* Understanding Real-time Conversations on Facebook: https://www.infoq.com/presentations/facebook-real-time-conversations/
* Facebook Iris: 
  * https://www.youtube.com/watch?v=eADBCKKf8PA&ab_channel=%40Scale
  * https://engineering.fb.com/2014/10/09/production-engineering/building-mobile-first-infrastructure-for-messenger/
* HBase
  * https://www.infoq.com/presentations/HBase-at-Facebook/