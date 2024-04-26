- [Ordering definition](#ordering-definition)
  - [Ordering across partition](#ordering-across-partition)
- [Ordering flowchart](#ordering-flowchart)
  - [Single partition](#single-partition)
  - [Ordering guarantee within business domain](#ordering-guarantee-within-business-domain)

# Ordering definition
* If the msg producing order is the same with msg consuming order, then msgs are in order.
  * The msg producing order is the order when msg arrives at broker. 

![](../.gitbook/assets/messageQueue_ordering.png)

## Ordering across partition
* For Kafka, it could not guarantee the order across different partitions. 
* Cross-partition ordering would typically need a role for coordinator. 
  1. Suppose msg1 is produced before msg2 but msg2 arrives at consumer before msg1
  2. Then the coordinator needs to hold msg2 until msg1 is consumed. 

# Ordering flowchart
## Single partition
* The performance is low because all messages are sent to the same partition. 

## Ordering guarantee within business domain

