- [Unread messages](#unread-messages)
  - [Separate storage](#separate-storage)
  - [Inconsistency](#inconsistency)
      - [How to be efficient in 10K group chat](#how-to-be-efficient-in-10k-group-chat)
    - [Sync history msg from multiple devices](#sync-history-msg-from-multiple-devices)
      - [Sync from online devices](#sync-from-online-devices)
      - [Sync from offline devices](#sync-from-offline-devices)
      - [How to scale offline batch Ack](#how-to-scale-offline-batch-ack)

# Unread messages
## Separate storage

* Total unread message and unread message against a specific person
  * Usage scenarios are different

## Inconsistency

* Why inconsistency will occur in the first place?
  * Total unread message increment and unread message against a specific person are two atomic operations. One could fail while the other one succeed. Or other clearing operations are being executed between these two operations.
* Solution:
  * Distributed lock
    * MC add
    * Redis setNX
  * Transaction
    * Redis's MULTI, DISCARD, EXEC and WATCH operations. Optimistic lock.
  * Lua script

#### How to be efficient in 10K group chat

* Problem: Suppose that there is a 5000 people group and there are 10 persons speaking within the group per second, then QPS for updating unread messges will be 50K; When there are 1000 such groups, the QPS will be 50M
* Solution: Aggregate and update
  1. There will be multiple queues A/B/C/... for buffering all incoming requests.
  2. Two components will be pulling from queues
     * Timer: Will be triggered after certain time
     * Flusher: Will be triggered if any of the queue exceed a certain length
  3. Aggregator service will pull msgs from Timer and Flusher, aggregate the read increment and decrement operations
* Cons:
  * Since there is no persistent on queues, if there is a restart, the number of unread messages will be inaccurate

![Aggregate unread messages](images/messenger\_unread\_aggregate.jpg)

### Sync history msg from multiple devices

* Telegram and QQ supports sync history and Wechat don't.

#### Sync from online devices

* Require to record the online status from user devices' perspective.

#### Sync from offline devices

**Flow chart**

![message offline push notification](.gitbook/assets/messenger\_offline\_sync.jpg)

**Storage**

* Offline msgs should not be stored together with normal online msgs because
  * Offline msgs will contain operation instructions which will not be persisted in online cases.
  * The data model for msg index table is based on two parties. The data model for offline msg is based on single unique user.
  * The offline messages only have a certain retention period (1 week) or upper limit (1000 messages). Since the number of users' devices is unknown, offline messages could not stored forever. It should be stored in a FIFO basis.
* Offline message should be sent together a sequence field
  * After the sender sends a message, the sender's local seq number also needs to be updated. An additional step could be performed
* Offline messages could be sent together in a big package.

**How to handle offline write failure**

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

#### How to scale offline batch Ack

* Offline ack usually happens in scenarios where a user just sets up the connection. Usually around this time the connection channel is stable and reliable.
* Could adopt something similar to TCP's Delay Ack to accept ack packages in batch.


