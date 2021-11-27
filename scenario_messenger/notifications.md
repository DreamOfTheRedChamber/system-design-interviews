- [Notifications](#notifications)
  - [Flowchart: For both online and offline notifications](#flowchart-for-both-online-and-offline-notifications)
    - [Online](#online)
    - [Offline notification](#offline-notification)
  - [Notification technologies](#notification-technologies)
    - [Pull model (Periodical short pull)](#pull-model-periodical-short-pull)
    - [Pull model (Periodical long pull)](#pull-model-periodical-long-pull)
    - [Push model (WebSocket)](#push-model-websocket)
      - [Websocket](#websocket)
      - [Heartbeat](#heartbeat)

# Notifications
## Flowchart: For both online and offline notifications
### Online
* User online: Push message via long poll connection
  * How does long poll find user's connection among so many long polls? There will be a user sign-in process
    1. A TCP connection is set after three time hand shake.
    2. Client sends a request based on the connection.
    3. Server interprets the connection. If valid, it will save the mapping between uid and tcp connection socket descriptor.
    4. This descriptor will be saved on local cache or distributed cache.

### Offline notification

* User offline: Push message via APNs
  * To make sure that users could still receive notifications when the app is running in the background or not openned, third party notification (Apple Push Notification Service / Google Cloud Messaging) will be used.
* Offline message push
  * When many offline messages need to be pushed to the end-user, there is a need to resort msgs.
  * The entire process for sending offline msgs
    1. The connection layer (network gateway) will subscribe to the redis topic for offline msgs.
    2. User goes online.
    3. The connection layer (network gateway) will notify business layer that the user is online.
    4. The business layer will publish msgs to redis topic for offline msgs.
    5. Redis will fan out the offline messages to the connection layer. (The rearrangement happens on this layer)
    6. The conneciton layer will push the message to clients.

## Notification technologies
### Pull model (Periodical short pull)
* User periodically ask for new messages from server
* Use case:
  * Used on reconnection
* Cons if used for messaging:
  * High latency if pulling on a low frequency
  * High resource consumption if pulling on a high frequency.
    * It wastes client devices' electricity because most polling are useless.
    * It puts high pressure on server resources and implies a high QPS.

### Pull model (Periodical long pull)
![Periodical long pull](../.gitbook/assets/messenger\_periodicalLongPull.png)

* Periodical long poll: The difference with short poll is that the client request does not return immediately after the request reaches the server. Instead, it hangs on the connection for a certain period of time. If there is any incoming messages during the hanging period, it could be returned immediately.
  * Cons:
    * Hanging on the server for a period reduces the QPS but does not really reduce the pressure on server resources such as thread pool. (If there are 1000 connections, server side still needs to have 1000 threads handling the connection.)
    * Long pull will return if not getting a response after a long time. There will still be many waste of connections.

### Push model (WebSocket)
#### Websocket
* Websocket: Client and server need one-time handshake for bi-directional data transfer. When server side has a new notification, it could push to the client via the websocket connection.
  * Websocket is a duplex protocol based on a single TCP connection.
  * Pros:
    * Support bidirectional communication, client no longer needs to pull periodically.
    * Reduce the setup time. A new TCP connection does not need to be established.
    * Support natively by the web after HTML5 appears.
  * TODO: HOW DOES WEBSOCKET WORK INTERNALLy
* Many other protocols based on TCP long connection such as XMPP/MQTT.
  * XMPP is mature and easy to extend. But the XML based transfer schema consumes a lot of network bandwidth and has a complicated design.
  * MQTT is based on pub/sub mode, reserve network bandwidth, easy to extend. But it is not a protocol for IM so does not support many IM features such as group chatting, offline messages.

#### Heartbeat
* Approaches to maintain connection (heartbeat)
  * TCP keepalive heartbeat
    * Pros:
      * Supported by TCP/IP protocol. Disabled by default. Three parameters to be configured: heart beat cycle (default 2 hour), number of retries (retry 9 time), timeout period (75s).
      * No extra development work.
      * Used in industry. For example, WhatsApp uses 10 seconds duration TCP keepalive.
    * Cons:
      * Low flexibility in tuning the heartbeat cycle period (always fixed cycle period);
      * Network layer available does not mean application layer available. For example, application is stuck in a dead cycle.
  * Application layer heartbeat
    * To overcome the cons of network layer TCP keep-alive, application layer heartbeat messages are used.
    * Strategies:
      * Only send hearbeat messages when application has additional bandwidth
      * Based on a fixed frequency
    * Pros:
      * More flexibility in tuning the heartbeat cycle period
      * Reflect whether the application is avaialble.
      * Used in industry. For example, WhatsApp use 30 seconds or 1 minutes app level heartbeat; Wechat use 4.5 minutes and twitter uses 2 minutes.
    * Cons:
      * Will have some additional data transmission cost because not supported natively by TCP/IP protocol.
* Benefits
  * This long connection is a virtual connection. There will be cases that the connection could be broken. For example:
    * The user enters an area where the network connection is bad.
    * Or even without any network errors on client and server side, there will be a NAT process happening within network operators. For optimizing the performance and reduce the resource consumption on network operator devices, some network operators will clear the mapping within NAT if there isn't any msg being sent on the connection.
      * The NAT process is to transform the internal IP address to external IP address because there are only limited IPv4 addresses.
  * Reduce the connection resource consumption on IM server side
    * Server will maintain a mapping between user device and network connection
    * Server will cache some client info such as app version, os version so that client does not need to pass those information every time
    * If no exception are detected, server will try to push notifications along these corrupted long connection channels, wasting a lot of resources.
  * Notify the client to reconnect if not receiving the ack of heartbeat msgs after timeout.
