- [Notification flows](#notification-flows)
  - [Flowchart: For both online and offline notifications](#flowchart-for-both-online-and-offline-notifications)
    - [Online notification](#online-notification)
    - [Offline notification](#offline-notification)
- [TODO](#todo)

# Notification flows

![](../.gitbook/assets/messenger-connectionService.png)

## Flowchart: For both online and offline notifications
### Online notification
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

# TODO
* http://www.52im.net/forum.php?mod=viewthread&tid=1762&ctid=11
* http://www.52im.net/forum.php?mod=collection&action=view&ctid=11&fromop=all