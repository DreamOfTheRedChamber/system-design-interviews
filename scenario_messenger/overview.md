- [Functional features](#functional-features)
  - [Common core features](#common-core-features)
  - [Features in 2B vs 2C applications](#features-in-2b-vs-2c-applications)
- [Architecture](#architecture)
  - [Connection layer](#connection-layer)
    - [Responsibilities](#responsibilities)
    - [Motivation for separation from business logic layer](#motivation-for-separation-from-business-logic-layer)
- [Industry solutions](#industry-solutions)
  - [Client vs server side storage](#client-vs-server-side-storage)
  - [Hipchat](#hipchat)
  - [Facebook](#facebook)
  - [Discord](#discord)
  - [MirrorFly](#mirrorfly)
  - [Line](#line)
  - [Slack](#slack)
  - [LinkedIn](#linkedin)
  - [Chinese only](#chinese-only)

# Functional features

* A user has a list of contacts (1), from which she can see who is online/offline (2). The user can pick any one or multiple of her contacts and start a chat (3). In a chat, all participants can send messages (4). The messages will be displayed in chronological order (5). A message can be “liked” by any participant, whose avatar is then attached to the “liked” message (6). A participant’s avatar is also displayed after the last message she read, signaling her progress to other participants (7). A participant can delete any message in her own chat view (8). Only the sender of a message can delete it for all participants (9). Any user can leave a chat (10).

![](../.gitbook/assets/messenger\_features.png)

## Common core features
  * Sending text-only messages
  * One to one chatting
  * Group chatting
  * User online status
* Optional features
  * Friendship / Contact book
  * Sending GIFs, emojis, photos, or other visuals
  * Making voice calls
  * Making video calls

## Features in 2B vs 2C applications

| `Feature`  | `Def`  | `Use case` | `Reason`  |
|---|---|---|---|
| `Read receipt` | Sender gets noticed when receiver reads msg. | Typically required in 2B products, e.g. Ecommerce, Business admin. Social apps among unfamiliar people | Designed for completing business functionalities. |
| `Message roaming` | User could see past msg history from different devices. | Typically required in 2B products. | Audit. Work across platforms. |

# Architecture

![](../.gitbook/assets/im_architecture_overview.png)

## Connection layer

### Responsibilities
* Keep the connection
* Interpret the protocol. e.g. Protobuf
* Maintain the session. e.g. which user is at which TCP connection
* Forward the message.

### Motivation for separation from business logic layer
* This layer is only responsible for keeping the connection with client. It doesn't need to be changed on as often as business logic pieces.
* If the connection is not on a stable basis, then clients need to reconnect on a constant basis, which will result in message sent failure, notification push delay.
* From management perspective, developers working on core business logic no longer needs to consider network protocols (encoding/decoding)


# Industry solutions
## Client vs server side storage

* Client-side database:
  * Quite effective in minimizing the data stored in the database by holding the data within the device
  * Example: whatsapp and viber
* Server-side database:
  * web chat providers for collaboration in the market are built on the server-side database
  * Example: Slack, Hipchat

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
* Understanding Real-time Conversations on Facebook: https://www.infoq.com/presentations/facebook-real-time-conversations/
* Facebook Iris: 
  * https://www.youtube.com/watch?v=eADBCKKf8PA&ab_channel=%40Scale
  * https://engineering.fb.com/2014/10/09/production-engineering/building-mobile-first-infrastructure-for-messenger/
* HBase
  * https://www.infoq.com/presentations/HBase-at-Facebook/

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

## Slack
* Overview on Slack group messaging initial days: https://www.youtube.com/watch?v=WE9c9AZe-DY&ab_channel=InfoQ
* Flannel: Edge cache engine
  * https://www.youtube.com/watch?v=s4xgfT81BTg&ab_channel=GeekWire
  * Evolution of flannel: https://www.youtube.com/watch?v=x1Uz3rMlOBo&ab_channel=InfoQ
* Scaling slack: https://www.youtube.com/watch?v=C4AUHFhzYZo&ab_channel=SINFO
* Overview of scaling slack Flannel/Sharding/Microservices: https://www.infoq.com/presentations/slack-scalability-2018/
* Scaling slack infra Organization level questions: https://www.infoq.com/presentations/slack-scaling-infrastructure/
* https://www.youtube.com/watch?v=o4f5G9q_9O4&ab_channel=GOTOConferences
* https://www.analyticsvidhya.com/blog/2021/08/slack-data-engineering-design-and-architecture/
* Slack use MySQL as backend with sharding techniques
* [How Slack build shared channels](https://slack.engineering/how-slack-built-shared-channels-8d42c895b19f)

## LinkedIn
* Real time video messaging: https://www.infoq.com/presentations/linkedin-play-akka-distributed-systems/
* Instant Messaging at LinkedIn: Scaling to Hundreds of Thousands of Persistent Connections on One Machine: https://engineering.linkedin.com/blog/2016/10/instant-messaging-at-linkedin--scaling-to-hundreds-of-thousands-
* Now You See Me, Now You Don’t: LinkedIn’s Real-Time Presence Platform: https://engineering.linkedin.com/blog/2018/01/now-you-see-me--now-you-dont--linkedins-real-time-presence-platf

## Chinese only
* 得到: <<飞书：责之数字化工具9讲>>
* Rainbow Chat 知乎专栏：https://www.zhihu.com/people/nan-ren-2600/posts
* Weibo 袁武林：微博消息系统架构演进 https://daxue.qq.com/content/content/id/2600
