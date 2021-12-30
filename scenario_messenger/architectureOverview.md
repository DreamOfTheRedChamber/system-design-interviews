- [Architecture](#architecture)
  - [Connection layer](#connection-layer)
    - [Components](#components)
    - [Responsibilities](#responsibilities)
    - [Motivation for separation from business logic layer](#motivation-for-separation-from-business-logic-layer)
  - [Session data](#session-data)

# Architecture

![](../.gitbook/assets/im_architecture_overview.png)

## Connection layer
### Components
* Please refer to [load balancer architecture section](https://eric-zhang-seattle.gitbook.io/mess-around/network/loadbalancer#multi-layer)

### Responsibilities
* Keep the connection
* Interpret the protocol. e.g. Protobuf
* Maintain the session. e.g. which user is at which TCP connection
* Forward the message.

### Motivation for separation from business logic layer
* This layer is only responsible for keeping the connection with client. It doesn't need to be changed on as often as business logic pieces.
* If the connection is not on a stable basis, then clients need to reconnect on a constant basis, which will result in message sent failure, notification push delay.
* From management perspective, developers working on core business logic no longer needs to consider network protocols (encoding/decoding)

## Session data

![](../.gitbook/assets/im_session_data.png)