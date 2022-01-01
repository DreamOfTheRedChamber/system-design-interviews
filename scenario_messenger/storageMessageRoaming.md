
- [Goal](#goal)
- [Differences between message roaming and offline message pulling](#differences-between-message-roaming-and-offline-message-pulling)
  - [Offline message pulling](#offline-message-pulling)
    - [Trigger](#trigger)
    - [Storage key](#storage-key)
    - [Retention period](#retention-period)
  - [Message roaming](#message-roaming)
    - [Trigger](#trigger-1)
    - [Storage key](#storage-key-1)
- [Storage model](#storage-model)
- [Flowchart](#flowchart)
  - [Offline message implementation](#offline-message-implementation)
  - [History message implementation](#history-message-implementation)
- [TODO](#todo)

# Goal
* Whatever device a user logs in, he could access past messaging history. 
* For example, Telegram/QQ could support it but WeChat does not support it. 

# Differences between message roaming and offline message pulling
* Offline msgs should not be stored together with normal online msgs because
  * Offline msgs will contain operation instructions which will not be persisted in online cases. For example, client A deletes a message in device A. When syncing from Client A's device B, the message should still be deleted. 

## Offline message pulling
### Trigger
* Whenever a user opens app, the app will pull the offline messags. After network jittery, app will pull the offline messages. Pulling offline message is a high frequency operation.

### Storage key
* Offline message includes different types of messages including 1-on-1 chat, group chat, system notifications, etc.
* So the offline messages should be keyed on each user's identity, such as UserID. 

### Retention period
* The offline messages only have a certain retention period (1 week) or upper limit (1000 messages). Since the number of users' devices is unknown, offline messages could not stored forever. It should be stored in a FIFO basis.

## Message roaming
### Trigger
* Whenever a user opens a conversation and scroll down, app will pull all history conversations. Message roaming is a low frequent operation. 
* Typical scenarios:
  * User installs a new IM app and will be able to see history messages. 
  * When user chats on PC device, the history should also be available on mobile app. 

### Storage key
* Historical message should be keyed on a per conversation id. 

# Storage model

![](../.gitbook/assets/im_messageroaming_storageModel.png)

# Flowchart
* Differences: 
  * Move all offline message storage to cache, keyed by per user.
  * Move all history message storage separate, keyed by per thread. 

![](../.gitbook/assets/im_messageRoaming_1to1.png)

## Offline message implementation
* For each user, created a SortedSet structure keyed by UserID. 
* Within the SortedSet, offline messages are sorted by their sequence order.
* Complexity for adding an entry: O(logN)
* Complexity for reading an entry: O(logN + M)

## History message implementation
* Store history messages according to the group id. 
* For each user in the group, store a timestamp when the user joined the group.

# TODO
* http://www.52im.net/forum.php?mod=collection&action=view&ctid=29&fromop=all
