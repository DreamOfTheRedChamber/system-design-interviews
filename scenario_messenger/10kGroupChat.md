- [Scale the long connection storage](#scale-the-long-connection-storage)

# Scale the long connection storage
* When the size of group is big, connection service will become a bottleneck because:
  * When users become online/offline, write pressure to connection service
  * When messages need to be pushed down from the server, it needs to check the online status within the connection service
* Optimization
  * Each connection service cluster doesn't need to maintain a global user online/offline status storage. Only maintain the online/offline users connected to the connection service cluster.
  * Subscribe to a message queue

![](../.gitbook/assets/im_connectionlayer_scale.png)
