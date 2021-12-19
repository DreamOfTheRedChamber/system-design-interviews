- [Notification technologies](#notification-technologies)
  - [Pull model (Periodical short pull)](#pull-model-periodical-short-pull)
    - [Flowchart](#flowchart)
  - [Pull model (Periodical long pull)](#pull-model-periodical-long-pull)
    - [Flowchart](#flowchart-1)
  - [Pull model XMPP/MQTT](#pull-model-xmppmqtt)
  - [Push model SSE](#push-model-sse)
    - [Flowchart](#flowchart-2)
  - [Push model (WebSocket)](#push-model-websocket)
    - [Flowchart](#flowchart-3)
  - [Comparison](#comparison)

# Notification technologies
## Pull model (Periodical short pull)
* User periodically ask for new messages from server
* Use case:
  * Used on reconnection

### Flowchart
1. A client makes an HTTP request requesting a web page from a server.
2. The server calculates the response
3. The server sends the response to the client

![](../.gitbook/assets/notifications_shortpoll.png)

## Pull model (Periodical long pull)
### Flowchart
* The difference with short poll is that the client request does not return immediately after the request reaches the server. Instead, it hangs on the connection for a certain period of time. If there is any incoming messages during the hanging period, it could be returned immediately.
  1. The client makes a request to the server.
  2. The server receives the request and delays sending anything to the client until the requested data is available or there is any other update related to data.
  3. When the data is available, the response is sent to the client.
  4. The client receives the response.
  5. The client usually makes a new request right away or after some defined interval so that the connection with the server is established again.

![](../.gitbook/assets/notifications_longpoll.png)


## Pull model XMPP/MQTT
* Many other protocols based on TCP long connection such as XMPP/MQTT.
  * XMPP is mature and easy to extend. But the XML based transfer schema consumes a lot of network bandwidth and has a complicated design.
  * MQTT is based on pub/sub mode, reserve network bandwidth, easy to extend. But it is not a protocol for IM so does not support many IM features such as group chatting, offline messages.

## Push model SSE
* Unlike WebSockets, Server-Sent Events are a one-way communication channel where events flow from server to client only. Server-Sent Events allows browser clients to receive a stream of events from a server over an HTTP connection without polling.
* A client subscribes to a “stream” from a server and the server will send messages (“event-stream”) to the client until the server or the client closes the stream. It is up to the server to decide when and what to send the client, for instance as soon as data changes.

### Flowchart
* A flow for server send events will be as follows:
  1. Browser client creates a connection using an EventSource API with a server endpoint which is expected to return a stream of events over time. This essentially makes an HTTP request at given URL.
  2. The server receives a regular HTTP request from the client and opens the connection and keeps it open. The server can now send the event data as long as it wants or it can close the connection if there are no data.
  3. The client receives each event from the server and process it. If it receives a close signal from the server it can close the connection. The client can also initiate the connection close request.

![](../.gitbook/assets/notifications_serverSentEvent.png)


## Push model (WebSocket)
### Flowchart
* Websocket: Client and server need one-time handshake for bi-directional data transfer. When server side has a new notification, it could push to the client via the websocket connection.
  * Websocket is a duplex protocol based on a single TCP connection.

![](../.gitbook/assets/notifications_websockets.png)

## Comparison

|            | `Pros           ` | `Cons`  |
|------------|--------|---|
| `Short poll` |  Easy | 1) High latency if pulling on a low frequency. 2) High resource consumption if pulling on a high frequency. It wastes client devices' electricity because most polling are useless. It puts high pressure on server resources and implies a high QPS.  | 
| `Long poll`  | 1) Since there are probably a lot of devices that do not support newer methods such as SSEs and WebSockets, Long-Polling can be useful in such situations. Also, Long-Polling can be used as a fallback option. | 1) Performance degradation: server needs to do more jobs such as holding the connection open, establishing what pieces of data are already sent to the client in the previous connections and what more needs to be sent. Also, a lot of time is lost in the process of setting up connections itself. 2) Message ordering: there is a possibility that the same data will be written multiple times in the client’s local storage. That can happen when the client sends more than one request for the same data in parallel. 3) Maximal Latency: because of the way Long-Polling works, once the server sends a response to the client, it can not send anything else and it needs to wait until a new request is made. There are some methods that can reduce latency such as HTTP pipelining, but they are not always available.  | 
| `SSE`        | 1) Simple to implement and use, both on the client and the server side. 2) You can use built-in events or create custom ones. 3) It is supported by most of the commonly used web browsers such as Chrome, Mozilla Firefox and Safari but it is not supported by Internet Explorer.                   | 1) Unidirectional nature can cause a problem if the connection is lost. In this situation, the server may not immediately realize that the connection is lost since the client cannot notify the server about it. 2) Limitation related to the number of connections that can be opened between the client and server at the same time. | An example where it is not good to use SSE is some chat application where messages are sent and received constantly.  |
| `Websockets` | 1) Support bidirectional communication, client no longer needs to pull periodically. 2) Reduce the setup time. A new TCP connection does not need to be established. 3) Support natively by the web after HTML5 appears. | 1 )WebSockets don’t automatically recover when connections are terminated – this is something you need to implement yourself, and is part of the reason why there are many client-side libraries in existence. 2) Browsers older than 2011 aren’t able to support WebSocket connections - but this is increasingly less relevant.  | 
