- [Functional features](#functional-features)
- [Nonfunctional features](#nonfunctional-features)
- [Industry solutions](#industry-solutions)
  - [Client vs server side storage](#client-vs-server-side-storage)
  - [Slack](#slack)
  - [Hipchat](#hipchat)
  - [Facebook](#facebook)
  - [Discord](#discord)
  - [MirrorFly](#mirrorfly)
  - [Line](#line)

# Functional features

* A user has a list of contacts (1), from which she can see who is online/offline (2). The user can pick any one or multiple of her contacts and start a chat (3). In a chat, all participants can send messages (4). The messages will be displayed in chronological order (5). A message can be “liked” by any participant, whose avatar is then attached to the “liked” message (6). A participant’s avatar is also displayed after the last message she read, signaling her progress to other participants (7). A participant can delete any message in her own chat view (8). Only the sender of a message can delete it for all participants (9). Any user can leave a chat (10).

![](../.gitbook/assets/messenger\_features.png)

* Core features
  * Sending text-only messages
  * One to one chatting
  * Group chatting
  * User online status
* Optional features
  * History info
  * Log in from multiple devices
  * Friendship / Contact book
  * Sending GIFs, emojis, photos, or other visuals
  * Making voice calls
  * Making video calls

# Nonfunctional features
* Latency: Just imagine that users might travel across cities and countries and send messages. 




# Industry solutions

* 知乎专栏：https://www.zhihu.com/people/nan-ren-2600/posts

## Client vs server side storage

* Client-side database:
  * Quite effective in minimizing the data stored in the database by holding the data within the device
  * Example: whatsapp and viber
* Server-side database:
  * web chat providers for collaboration in the market are built on the server-side database
  * Example: Slack, Hipchat

## Slack

* Slack use MySQL as backend with sharding techniques
* [How Slack build shared channels](https://slack.engineering/how-slack-built-shared-channels-8d42c895b19f)
* [Scaling slack](https://www.infoq.com/presentations/slack-scalability-2018/)

## Hipchat

* Elastic
  * 60 messages per second
  * 1.2 billion documents stored
* [How HipChat stores and indexes billions of messages using elasticSearch](http://highscalability.com/blog/2014/1/6/how-hipchat-stores-and-indexes-billions-of-messages-using-el.html)
  * Compatible with Lucene and reduce the number of components

## Facebook

* Evolution process
  1. Start with MySQL and Memcached
  2. TAO - A FB-specific NoSQL graph API built to run on sharded MySQL
* [https://blog.yugabyte.com/facebooks-user-db-is-it-sql-or-nosql/](https://blog.yugabyte.com/facebooks-user-db-is-it-sql-or-nosql/)

## Discord

* Cassandra: KKV store
  * channel\_id as the partition key
  * message\_id as the clustering key

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

## Line
* Line: https://engineering.linecorp.com/en/blog/

