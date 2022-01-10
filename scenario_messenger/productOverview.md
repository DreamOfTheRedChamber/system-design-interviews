- [Functional features](#functional-features)
  - [Common core features](#common-core-features)
  - [Features in 2B applications](#features-in-2b-applications)
- [Industry solutions](#industry-solutions)
  - [Client vs server side storage](#client-vs-server-side-storage)
    - [Client side](#client-side)
    - [Server side](#server-side)
  - [WhatsApp](#whatsapp)
  - [Netease 网易云信](#netease-网易云信)
  - [Hipchat](#hipchat)
  - [Facebook](#facebook)
  - [Discord](#discord)
  - [Viber](#viber)
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

## Features in 2B applications

| `Feature`  | `Def`  | `Use case` |
|---|---|---|
| `Read receipt` | Each message has a list of already read and unread people. | Ecommerce, Business admin. Designed for completing business functionalities. |
| `Message roaming` | User could see past msg history from different devices. | Audit. Work across platforms. Data is corporate asset. |
| `Recall message` | Recall msgs within 24 hours of send time. | Misspreading wrong information in a business environment could be detrimental. |
| `Large group chat` | Group chat with members > 10K. | Put all members in an organization inside a group chat |

# Industry solutions
* Different chat apps architecture: https://www.simform.com/blog/how-to-build-messaging-app-whatsapp-telegram-slack/#decen
* https://yalantis.com/blog/messaging-apps-development-telegram-whatsapp-others-work/
  * Telegram open source: https://yalantis.com/blog/whats-wrong-telegram-open-api/
* Chart overview: https://addevice.io/blog/how-to-create-a-messaging-application-from-zero/ 
* https://getstream.io/blog/build-chat-messaging-app/
* https://sudarakayasindu.medium.com/behind-the-scenes-of-chat-applications-38634f584758
* Wechat backend from 0 to 1: http://www.52im.net/thread-168-1-1.html
* Behind the scenes of Chat Applications: https://sudarakayasindu.medium.com/behind-the-scenes-of-chat-applications-38634f584758

## Client vs server side storage
* Client-side database:
  * Quite effective in minimizing the data stored in the database by holding the data within the device
  * Example: whatsapp and viber
* Server-side database:
  * web chat providers for collaboration in the market are built on the server-side database
  * Example: Slack, Hipchat

### Client side
* Wechat SqlLite: http://www.52im.net/thread-789-1-1.html

### Server side


## WhatsApp
* https://blog.whatsapp.com/
* Erlang: http://highscalability.com/blog/2014/2/26/the-whatsapp-architecture-facebook-bought-for-19-billion.html
* [Scaling to Millions of Simultaneous Connections.](https://vimeo.com/44312354)
* http://highscalability.com/blog/2014/2/26/the-whatsapp-architecture-facebook-bought-for-19-billion.html
* Disappearing messages: https://blog.whatsapp.com/?page=2
* WhatsApp 10 year milestone: https://blog.whatsapp.com/thank-you-for-10-years
* Deleting messages: https://blog.whatsapp.com/deleting-messages-for-everyone
* Introducing the WhatsApp Business App: https://blog.whatsapp.com/?page=5

## Netease 网易云信
* https://segmentfault.com/blog/yunxin?page=3
* 网易云信聊天室系统架构: https://segmentfault.com/a/1190000040177789
* 6000 字干货详解：直播聊天室的无限用户优化: https://segmentfault.com/a/1190000041153170
* 



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
* Facebook chat architecture: https://www.slideshare.net/udayslideshare/facebook-chat-architecture
* Scaling the Messages Application Back End: https://www.facebook.com/notes/10158791520957200/

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

## Viber
* https://www.viber.com/en/blog/page/2/
* https://developers.viber.com/blog/

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
* 瓜子IM: 手把手教你开发生产级IM系统 https://mp.weixin.qq.com/s/_Direcn6tk2P2KDpncFdgQ
