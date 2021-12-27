
- [Goal](#goal)
- [Sync from offline devices](#sync-from-offline-devices)
  - [Flow chart](#flow-chart)
  - [Storage](#storage)
  - [How to handle offline write failure](#how-to-handle-offline-write-failure)
  - [How to scale offline batch Ack](#how-to-scale-offline-batch-ack)
- [TODO](#todo)

# Goal
* Whatever device a user logs in, he could access past messaging history. 
* For example, Telegram/QQ could support it but WeChat does not support it. 

# Sync from offline devices

## Flow chart

![message offline push notification](../.gitbook/assets/messenger\_offline\_sync.png)

## Storage

* Offline msgs should not be stored together with normal online msgs because
  * Offline msgs will contain operation instructions which will not be persisted in online cases.
  * The data model for msg index table is based on two parties. The data model for offline msg is based on single unique user.
  * The offline messages only have a certain retention period (1 week) or upper limit (1000 messages). Since the number of users' devices is unknown, offline messages could not stored forever. It should be stored in a FIFO basis.
* Offline message should be sent together a sequence field
  * After the sender sends a message, the sender's local seq number also needs to be updated. An additional step could be performed
* Offline messages could be sent together in a big package.

## How to handle offline write failure

* How to pull offline msgs based on needs
  1. User A sends a msg to User B.
  2. IM server changes User B's version number VERSION-LATEST within the version service.
  3. IM server saves the msg along with its version number VERSION-LATEST.
  4. User B comes online with its latest version number VERSION-OLD.
  5. IM server compares the two version numbers VERSION-OLD and VERSION-LATEST.
  6. IM server obtains the offline msgs for User B.
  7. IM server pushes the offline msgs to User B.
* What if the offline storage exceeds the maximum limit
  * It could goes back to the msg index table

## How to scale offline batch Ack

* Offline ack usually happens in scenarios where a user just sets up the connection. Usually around this time the connection channel is stable and reliable.
* Could adopt something similar to TCP's Delay Ack to accept ack packages in batch.


# TODO
* http://www.52im.net/forum.php?mod=collection&action=view&ctid=29&fromop=all
