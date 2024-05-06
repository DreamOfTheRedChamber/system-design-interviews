- [Index structure](#index-structure)
- [Zero copy](#zero-copy)
  - [Definition](#definition)
  - [Flowchart](#flowchart)

# Index structure
* Suppose we want to look for topic = test_topic, partition = 1, and offset = 1051
  1. First look for the directory = test_topic_1
  2. Binary search by ".log" file name, 1051 should be inside 01051.index
  3. Binary search by ".index" file content, 1051 hits the first record. 
  4. Suppose the offset is not available in ".index", then search line by line inside corresponding .log file. 

![Flowchart](../.gitbook/assets/messageQueue_kafka_indexStructure.png) 

# Zero copy
## Definition
* There is no CPU involved in the process. 

## Flowchart
* NIC: Network interface card
* DMA: Direct memory access

![Flowchart](../.gitbook/assets/messageQueue_zeroCopy.png) 