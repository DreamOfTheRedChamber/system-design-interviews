- [Differences between livecast room vs 10K group chat](#differences-between-livecast-room-vs-10k-group-chat)
  - [Number of participants](#number-of-participants)
  - [Relationship between user and group](#relationship-between-user-and-group)
  - [Duration of groups](#duration-of-groups)
- [Challenges of livecast room](#challenges-of-livecast-room)
- [Scale the long connection storage](#scale-the-long-connection-storage)

# Differences between livecast room vs 10K group chat 
## Number of participants
* For pure 10K group chat scenarios, 10K is already super big group
* For livecast room scenarios, 10K is pretty common, and it could be as high as million or 10 millions of participates. 
  * 1M or 10M participates

## Relationship between user and group
* For pure 10K group chat scenarios, the frequency of joining/leaving groups are pretty low. 
* For livecast room scenarios, the frequency of joining/leaving livecast rooms are pretty high.
  * 10K/s-20K/s joining/leaving livecast room per second.

## Duration of groups
* For pure 10K group chat scenarios, group memberships could last at least for months or years. 
* For livecast room scenarios, group memberships could only last a few hours. 

# Challenges of livecast room
* Latency: Livecast room requires realtime interactions and low latency in APIs. 
* End user experience: From end user perspective, each screen could fit 10-20 messages. If there are more than 20 messages per second pushed down to user device, the screen will stuck in a refreshing loop, resulting in bad user experience. 


# Scale the long connection storage
* When the size of group is big, connection service will become a bottleneck because:
  * When users become online/offline, write pressure to connection service
  * When messages need to be pushed down from the server, it needs to check the online status within the connection service
* Optimization
  * Each connection service cluster doesn't need to maintain a global user online/offline status storage. Only maintain the online/offline users connected to the connection service cluster.
  * Subscribe to a message queue

![](../.gitbook/assets/im_connectionlayer_scale.png)

