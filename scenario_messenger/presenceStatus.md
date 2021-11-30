- [Online status](#online-status)
  - [Flowchart](#flowchart)
  - [Responsibilities](#responsibilities)
  - [Motivation for separation from business logic layer](#motivation-for-separation-from-business-logic-layer)
  - [Scale the long connection storage](#scale-the-long-connection-storage)
- [Manage the number of connections](#manage-the-number-of-connections)
  - [Akka Actors](#akka-actors)
  - [Apply Akka actors concept in connection](#apply-akka-actors-concept-in-connection)
  - [Akka actors and event source](#akka-actors-and-event-source)
  - [Manage multiple connections](#manage-multiple-connections)
- [Heartbeat](#heartbeat)

# Online status

## Flowchart
* Online status pull
  * When users become online, send a heartbeat msg to the server every 3-5 seconds.
  * The server sends its online status to friends every 3-5 seconds.
  * If after 1 min, the server does not receive the heartbeat msg, considers the user is already offline.
* Performance bottleneck
  * A central connection service for maintaining user online status and network gateway the user resides in
    * Instead, use a message queue, ask all connection service to subscribe to this message queue. \[STILL SOME QUESTIONS 存储和并发：万人群聊系统设计中的几个难点]
    * This mechanism shifts the pressure from business logic layer to connection service layer.

## Responsibilities
* Keep the connection
* Interpret the protocol. e.g. Protobuf
* Maintain the session. e.g. which user is at which TCP connection
* Forward the message.

## Motivation for separation from business logic layer
* This layer is only responsible for keeping the connection with client. It doesn't need to be changed on as often as business logic pieces.
* If the connection is not on a stable basis, then clients need to reconnect on a constant basis, which will result in message sent failure, notification push delay.
* From management perspective, developers working on core business logic no longer needs to consider network protocols (encoding/decoding)

## Scale the long connection storage
* When the size of group is big, connection service will become a bottleneck because:
  * When users become online/offline, write pressure to connection service
  * When messages need to be pushed down from the server, it needs to check the online status within the connection service
* Optimization
  * Each connection service cluster doesn't need to maintain a global user online/offline status storage. Only maintain the online/offline users connected to the connection service cluster.
  * Subscribe to a message queue

![connection scale](../.gitbook/assets/messenger\_connection\_scale.png)

# Manage the number of connections
* Akka is a toolkit for building highly confident, message-driven applications. 

## Akka Actors
* Akka Actors are objects which have some state, and they have some behavior. 
  * Each actor has a mailbox, and they communicate exclusively by exchanging messages.
  * An actor is assigned a lightweight thread every time there is a message to be processed. 
    * The behavior defines how the state should be modified when they receive   certain messages. 
    * That thread will look at the behavior that is defined for the message and modify the state of the Akka Actor based on that definition. 
    * Then, once that is done this thread is actually free to be assigned to the next actor. 
  * Roles of actors
    * Since actors are so lightweight, there can be millions of them in the system, and each can have their own state and their own behavior. 
    * A relatively small number of threads, which is proportionate to the number of cores, can be serving these millions of actors all on the same time, because a thread is assigned to an actor only when there is something to process.

## Apply Akka actors concept in connection
* State: Each actor is managing one persistent connection, that's the state that it is managing. 
* Behavior: As it receives an event, the behavior here is defining how to publish that event to the EventSource connection. 
* Those many connections can be managed by the same machine using this concept of Akka Actors. 

![](../.gitbook/assets/presenceStatus_ConnectionManagement.png)

![](../.gitbook/assets/presenceStatus_millionsConnections.png)

## Akka actors and event source
* Let's look at how Akka Actors are assigned to an EventSource connetion. Almost every major server frame will support the EventSource interface natively. At LinkedIn we use the Play Framework, and if you're familiar with Play, we just use a regular Play controller to accept the incoming connection.

* Then, we use the Play EventSource API to convert it into a persistent connection, and assign it a random connectionId. Now we need something to manage the lifecycle of these connections, and this is where Akka Actors fit in. This is where we create an Akka Actor to manage this connection, and we instantiate an Akka Actor with the connectionId, and the handle to the EventSource connection that it is supposed to manage. 

```java
// Client A connects to the server and is assigned connectionIdA
public Result listen() 
{
  return ok(EventSource.whenConnected(
                          eventSource -> 
  {
    String connectionId = UUID.randomUUID().toString();

    // construct an Akka Actor to manage connection
    _actorSystem.actorOf(
        ClientConnectionActor.props(connectionId, eventSource),
        connectionId);
  }));
}
```

![](../.gitbook/assets/presenceStatus_publishEventStep1.png)

## Manage multiple connections
* Each client connection here is managed by its own Akka Actor, and each Akka actor in turn, all of them, are managed by an Akka supervisor actor. 
* Let's see how a like can be distributed to all these clients using this concept. 
  1. The likes backend publishes the like object to the supervisor Akka Actor over a regular HTTP request. 
  2. The supervisor Akka Actor simply broadcasts the like object to all of its child Akka Actors here. 
  3. Then, these Akka Actors have a very simple thing to do. They just need to take the handle of the EventSource connection that they have and send the event down through that connection. For that, it looks something very simple. It's eventSource.send, and the like object that they need to send. They will use that to send the like objects down to the clients.  "eventSource.send(<like object>);"
  4. What does this look like on the client side? The client sees a new chunk of data, as you saw before, and will simply use that to render the like on the screen. It's as simple as that.

![](../.gitbook/assets/presenceStatus_publishEventStep2.png)

![](../.gitbook/assets/presenceStatus_publishEventStep3.png)

![](../.gitbook/assets/presenceStatus_publishEventStep4.png)

![](../.gitbook/assets/presenceStatus_publishEventStep5.png)

![](../.gitbook/assets/presenceStatus_publishEventStep6.png)

# Heartbeat
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
