- [Reliability (No missing and duplication)](#reliability-no-missing-and-duplication)
  - [Flow chart](#flow-chart)
  - [Resend and dedupe](#resend-and-dedupe)
  - [Completeness check](#completeness-check)
- [[Must-have functional features] Consistency](#must-have-functional-features-consistency)
  - [Define a global order](#define-a-global-order)
  - [Applicability of the global order](#applicability-of-the-global-order)
  - [Guarantee the global order != consistent order](#guarantee-the-global-order--consistent-order)
    - [Reorder](#reorder)
- [[Nice-to-have functional features] Security](#nice-to-have-functional-features-security)
  - [Transmission security](#transmission-security)
  - [Storage security](#storage-security)
  - [Content security](#content-security)
- [[Nice-have nonfunctional features] Perf for multi-media](#nice-have-nonfunctional-features-perf-for-multi-media)
  - [Upload](#upload)
  - [Send](#send)
  - [Network stability](#network-stability)

# Reliability (No missing and duplication)

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

# [Must-have functional features] Consistency

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

# [Nice-to-have functional features] Security
## Transmission security
* Entrance security:
  * Router's DNS hijacked: DNS location is set to a location with virus.
  * Operator's local DNS hijacked:
    * Operator might send DNS requests to other operators to reduce the resource consumption
    * Operator might modify the TTL for DNS
  * Ways to prevent DNS from being hijacked
    * HttpDNS protocol: Prevent domain name from being hijacked by operators. It uses HTTP protocol instead of UDP to directly interact with DNS servers.
    * Combine HttpDNS with localDNS.
* TLS transmission layer security:
  * Cut off network
    * Failover to multiple connection IP address returned by HttpDNS service
    * Change from UDP based QUIC protocol to TCP protocol
  * Intercept/Man in the middle/Forfeit: Use TLS protocol
    * Insymetric encryption and key exchange algorithm are used to guarantee message encryption key not being corrupted or leaked.
    * Symmetric encryption is used to guarantee that the msg could not be decrypted after being intercepted.
    * Digital signature and CA certificate could be used to verify the valid status of public key.

## Storage security
* Account credentials: Hashing algorithm with salt.
* Message security: End to end encryption

## Content security
* Link to external phishing website
* Crawler

# [Nice-have nonfunctional features] Perf for multi-media
## Upload
* Picture/Video/Voice:
  * Picture/Video media: Have a dedicated channel for video and picture media. After media (video/picture) is uploaded to the storage, a unique ID will be generated and used along with messages.
  * Voice media：There is no miniature for preview. Voice media will be transmitted in the same channel as message team.
* Divide and send:
  * Size of the divide: Divide too big, not enough parrallelism; Divide too small, too many TCP connections and increased cost for merge.
  * Typical size of pieces: WiFi 2M; 4G 1M; 3G/2G 256K.
  * Since the size of media is big, it will be beneficial to divide and retransmit.
* Dedupe the media
  * Compute the hash for media before uploading

## Send
* Prerequisites for supporting watch while send
  1. Format and key frame info is at the top of file.
  2. Storage support range queries.
     * Ali OSS / Tencent COS, support range queries
     * Utilize load balance layer range query. (Nginx HTTP Slice)
* CDN
  * Encryption with HLS.
* Compression
  * Image compression
    * Adaptive resolution
    * WebP: WebP is roughly 30% smaller than PNG/JPEG. Cons is not easy to be integrated on iOS platform / JPEG
    * JPEG: Two types of JPEG ???
  * Video:
    * H.265 is 50% less than H.264. But encoding/decoding much more time consuming.

## Network stability

* Use public allowed ports when possible: 80, 8080, 443, 14000
* Http Tunnel: Use Http protocol to encapsulate other incompatible protocols
* Multi IP addresses: Rely on HttpDNS to return multiple IP addresses
* Connection fast
  * Reduce the latency among multi network operators
  * Race among multiple endpoints: After multiple IP addresses returned by HttpDNS, benchmark against different endpoints.
* Separating upload and download tunnel:
  * In case of broadcasting, there will be lots of msgs being sent in the downward channel.
  * Could use short connection in upload channel, long connection in download channel.
