
- [Goal](#goal)
- [Differences between message roaming and normal offline handling](#differences-between-message-roaming-and-normal-offline-handling)
  - [Flow chart](#flow-chart)
  - [How to avoid too many offline acknowledgement](#how-to-avoid-too-many-offline-acknowledgement)
- [TODO](#todo)

# Goal
* Whatever device a user logs in, he could access past messaging history. 
* For example, Telegram/QQ could support it but WeChat does not support it. 

# Differences between message roaming and normal offline handling
* Offline msgs should not be stored together with normal online msgs because
  * Offline msgs will contain operation instructions which will not be persisted in online cases. For example, client A deletes a message in device A. When syncing from Client A's device B, the message should still be deleted. 
  * The data model for msg index table is based on two parties. The data model for offline msg is based on single unique user.
  * The offline messages only have a certain retention period (1 week) or upper limit (1000 messages). Since the number of users' devices is unknown, offline messages could not stored forever. It should be stored in a FIFO basis.

![](../.gitbook/assets/messenger_offline_sync_original.png)

## Flow chart

![](../.gitbook/assets/messenger\_offline\_sync.png)

## How to avoid too many offline acknowledgement
* When a client syncs offline messages, it is usually a large amount of original and acknowledgement package. 
* Could adopt something similar to TCP's Delay Ack to accept ack packages in batch, meaning acknownledging a batch of packages at once. 

# TODO
* http://www.52im.net/forum.php?mod=collection&action=view&ctid=29&fromop=all
