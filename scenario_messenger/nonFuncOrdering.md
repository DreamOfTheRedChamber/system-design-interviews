# Ordering

## Define a global order

* Sender's local timestamp/sequence number?
  * Sender sends its local timestamp/sequence number along with message to the receiver. Receiver reorders all messages according to sender's local timestamp/sequence number
    * First order according to timestamp
    * Then order by sequence number
  * No because
    * Senders' could reset its timestamp to a specific value
    * Sender's sequence number could be reset to 0 after a operation like reinstall
    * In a scenario like group chat, participants' clock might not be synced; Or in a scenario where the same user logs in from multiple devices, different devices' timestamp might not be synced.
* IM server's timestamp?
  * Sender sends the message to the IM server. IM server sends its server timestamp/sequence number to the receiver. Receiver reorders all messages according to IM servers' local timestamp/sequence number
  * No because
    * Usually IM server will be a cluster and the clock is synced using NTP
    * When the cluster size is really big, it is challenging to maintain uniqueness

## Applicability of the global order

* IM server's sequence number? Maybe
  * Could be implemented [in these ways](https://github.com/DreamOfTheRedChamber/system-design/blob/master/uniqueIDGenerator.md)
    * From the product's perspective, there is no need for a global unique sequence number.
      * For scenario like group chat and logging from multiple devices, as long as there is a unique global sequence number per messaging group, it will be good enough.
      * It is best practices adopted by industry standards like Wechat/Weibo.

## Guarantee the global order != consistent order
* Even have the global order defined, it is still not enough because
  * IM servers are deployed on a cluster basis. Every machine's performance will be different and different IM servers could be in different states, such as in GC. A message with bigger sequence number could be sent later than another message smaller sequence number.
  * For a single IM server receiving a msg, the processing will be based on multi-thread basis. It could not be guaranteed that a message with bigger sequence number will be sent to receiver earlier than a message with lower sequence number.

### Reorder
* Why reorder is needed given most scenarios could stand small randomness in order?
  * However, there are some scenarios which have a higher sensitivity to order such as
    * (Corner case) After user A sends a "Goodbye" message to user B, delete the user B from the contact list. If the order is reversed, then the "Goodbye" message will fail.
  * Even the order could be guaranteed on the IM server side, due to the nature of multithreading on the server/receiver side, there are still chances that messages are delivered in different order.
* Solution
  * For the corner case above, the two messages could be sent in a single package.
  * For a scenario like receiving offline messages.
    * ??? How different from the global order Id
