- [Reliability](#reliability)
  - [Goal](#goal)
  - [When user is online](#when-user-is-online)
    - [Improve naive solution with app layer acknowledgement](#improve-naive-solution-with-app-layer-acknowledgement)
    - [Improve with resend and dedupe](#improve-with-resend-and-dedupe)
  - [When user is offline](#when-user-is-offline)
    - [Improve naive solution with app layer acknowledgement](#improve-naive-solution-with-app-layer-acknowledgement-1)
    - [Improve with resend and dedupe](#improve-with-resend-and-dedupe-1)
    - [Reduce the roundtrip between offline client and server](#reduce-the-roundtrip-between-offline-client-and-server)
  - [Flow chart](#flow-chart)
  - [Resend and dedupe](#resend-and-dedupe)
  - [Completeness check](#completeness-check)

# Reliability 
## Goal
* No missing 
* No duplication

## When user is online

### Improve naive solution with app layer acknowledgement

* Many things could go wrong even if client A successfully receives message from IM but client B does not receive message at all:
  1. IM server crashes and fails to send 3
  2. Network jitter and package get lost
  3. Client B crashes
* Client B sends a confirmation request after it successfully processed the message. Its flow will be symmetric to the naive solution.

![](../.gitbook/assets/im_nonfunc_reliability_online.png)

### Improve with resend and dedupe
* Potential issues
  * Any of packages (Msg: Request / Msg: Ack) is lost: 
    * Client A could simply resend will solve the problem. 
  * Any of packages (Msg: Notify / Applayer: Request / Applayer: Ack / Applayer: Notify) is lost:
    * The reason could be server crash, network jitter, client crash.

![](../.gitbook/assets/im_nonfunc_reliability_online_resenddedupe.png)

## When user is offline

### Improve naive solution with app layer acknowledgement

![](../.gitbook/assets/im_nonfunc_reliability_offline.png)

### Improve with resend and dedupe

### Reduce the roundtrip between offline client and server
* In the above flowchart, client B retrieves offline message from client A. And this process will repeat for each of its contact

```java
// When client B becomes online
for(all senderId in B’s friend-list)
{ 
     // Get offline message sent to B from uid
     get_offline_msg(B,senderId);   
}
```

* Optimization ways:
  1. Only fetch the number of offline messages for each friend. Only when the user enters the conversation, load actual messages. 
  2. Pull all offline messages sent to client B at once. Then dedupe and categorize by senderId. In practice, this solution is preferred over 1 because it reduces number of round trips. 

## Flow chart

* Among the IM forwarding model, the process of User A send a message to User B consists of the following steps:
  1. User A sends a msg to IM server (possible failure: the request failed midway)
  2. IM server stores the msg (possible failure: fails to store the message)
  3. IM server sends User A an acknowledge (possible failure: the server does not return within timeout period)
  4. IM server forwards the msg to User B (possible failure: after writing the msg to kernel send space, the server gets suspended because of power outage / User B receives the message but there is an exception happening resulting in message not put into queue.)
     1. When IM server forwards a msg to User B, it will carry a unique SID. This unique SID will be put inside an acknowledgement list (possible failure: the message never reaches user b's device because network is down).
     2. When User B receives the msg successfully, it will reply with an ACK package (possible failure: the acknowledgement package gets lost in the midway / User B's device gets corrupted before it could send an acknowledgement package); IM server will maintain an acknowledgement list with a timeout. If it does not get an acknowledgement package from user B, it will retry the message from the acknowledgement list.
     3. IM server will delete the msg with unique SID from the acknowledgement list (possible failure: IM server crash).

![Resend message](../.gitbook/assets/messenger\_resend.jpg)

## Resend and dedupe

* User A and IM server has resend and acknowledgement in place.
* IM server and User B needs to have dedupe mechanism in place.

## Completeness check

![message completeness](../.gitbook/assets/messenger\_completeness.jpg)

* What if the IM gets corrupted when it is resending the msg: Unique msg sequence id for guaranteeing the completeness
  1. IM server forwards a msg MESSAGE to User B, it carries a unique sequence id SEQ1.
  2. When user B gets the msg MESSAGE, it update its local msg sequence id to SEQ1.
  3. IM server gets the acknowledge.
  4. User B becomes offline.
  5. IM server forwards another msg MESSAGE to User B, it carries another unique sequence id SEQ2 and message gets lost.
  6. User B reconnects online, carrying the latest local msg sequence id SEQ1 to IM server.
  7. IM server detects that User B needs more msgs, so it delivers all of msgs with sequence id between SEQ1 and SEQ2.
  8. User B receives the msg and update the local sequence id to SEQ2.
* Why needs an acknowledgement even if TCP layer already acknowledges msg:
  * These acknowledgement are at different layers. TCP acknowledgement is at network layer. App layer acknowledgement happens at acknowledge layer. There could be some error happening during the process from network layer to app layer.
