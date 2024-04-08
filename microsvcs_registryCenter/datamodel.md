# Storage structure
* Storage structure is as the following:
  * Service layer
  * Cluster layer
  * Info entries as KV

![](../.gitbook/assets/registryCenter\_directory.png)

# Value entry in "Key, Value" pair
* Value contains the location of the server
* And it could also contains the grouping of server. For example, VIP clients should only be directed towards VIP servers. 