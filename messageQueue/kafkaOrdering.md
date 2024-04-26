- [Ordering definition](#ordering-definition)
  - [Ordering across partition](#ordering-across-partition)
- [Flowchart for ordering guarantee within business domain](#flowchart-for-ordering-guarantee-within-business-domain)
  - [Cons - Uneven data distribution](#cons---uneven-data-distribution)
    - [Slot allocation](#slot-allocation)
    - [Consistent hashing](#consistent-hashing)
  - [Cons - Message disorder due to resizing](#cons---message-disorder-due-to-resizing)
    - [Problem](#problem)
    - [Solution](#solution)

# Ordering definition
* If the msg producing order is the same with msg consuming order, then msgs are in order.
  * The msg producing order is the order when msg arrives at broker. 

![](../.gitbook/assets/messageQueue_ordering.png)

## Ordering across partition
* For Kafka, it could not guarantee the order across different partitions. 
* Cross-partition ordering would typically need a role for coordinator. 
  1. Suppose msg1 is produced before msg2 but msg2 arrives at consumer before msg1
  2. Then the coordinator needs to hold msg2 until msg1 is consumed. 

# Flowchart for ordering guarantee within business domain

![](../.gitbook/assets/messageQueue_ordering_flowchart.png)

## Cons - Uneven data distribution

### Slot allocation 
* Steps
  1. Calculate a hashing value according to the business key. 
  2. Calculate a slot index according to hashing_value % slot_num.
  3. Decide the mapping between slot and partition based on load.

* The mapping between slot and partition could be stored inside service registry. 
* Redis uses 16385 slots, and the number of slots could be decided based on business cases. 

![](../.gitbook/assets/messageQueue_ordering_slot_allocation.png)

### Consistent hashing
* Adjust the partition allocation on ring for data distribution.

![](../.gitbook/assets/messageQueue_ordering_consistentHashing.png)

## Cons - Message disorder due to resizing
### Problem
* For example, message id = 3 and id = 7 will land on the same partition after resizing. 
* However, when there are many backlogs on partition 0 for id = 3 and there is no backlog on partition 3 for id = 7 due to resizing, then id = 7 will be consumed before id = 3. 

![](../.gitbook/assets/messageQueue_ordering_resizing.png)

### Solution
* For all new partitions, wait for certain period which is long enough for old backlog messages to be consumed. 
