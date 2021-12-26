- [Websockets](#websockets)
- [Heartbeat](#heartbeat)
  - [Why TCP keepalive heartbeat not enough](#why-tcp-keepalive-heartbeat-not-enough)
  - [Motivation](#motivation)
  - [Benefits](#benefits)
- [References](#references)

# Websockets


# Heartbeat
## Why TCP keepalive heartbeat not enough
* Pros:
  * Supported by TCP/IP protocol. Disabled by default. Three parameters to be configured: heart beat cycle (default 2 hour), number of retries (retry 9 time), timeout period (75s).
  * No extra development work.
  * Used in industry. For example, WhatsApp uses 10 seconds duration TCP keepalive.
* Cons:
  * Low flexibility in tuning the heartbeat cycle period (always fixed cycle period);
  * Network layer available does not mean application layer available. For example, application is stuck in a dead cycle.

## Motivation
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

## Benefits
  * This long connection is a virtual connection. There will be cases that the connection could be broken. For example:
    * The user enters an area where the network connection is bad.
    * Or even without any network errors on client and server side, there will be a NAT process happening within network operators. For optimizing the performance and reduce the resource consumption on network operator devices, some network operators will clear the mapping within NAT if there isn't any msg being sent on the connection.
      * The NAT process is to transform the internal IP address to external IP address because there are only limited IPv4 addresses.
  * Reduce the connection resource consumption on IM server side
    * Server will maintain a mapping between user device and network connection
    * Server will cache some client info such as app version, os version so that client does not need to pass those information every time
    * If no exception are detected, server will try to push notifications along these corrupted long connection channels, wasting a lot of resources.
  * Notify the client to reconnect if not receiving the ack of heartbeat msgs after timeout.

# References
* http://www.52im.net/thread-281-1-1.html
* http://www.52im.net/forum.php?mod=collection&action=view&ctid=17&page=1
* http://www.52im.net/forum.php?mod=viewthread&tid=3780&ctid=7