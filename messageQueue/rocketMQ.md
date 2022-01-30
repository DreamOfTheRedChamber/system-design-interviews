- [RocketMQ](#rocketmq)
  - [Architecture](#architecture)
  - [Definition](#definition)
  - [Supported advanced message types](#supported-advanced-message-types)
  - [FIFO message](#fifo-message)
  - [Delayed message](#delayed-message)
  - [Transaction message](#transaction-message)
  - [Batch message](#batch-message)
  - [Real world](#real-world)

# RocketMQ

## Architecture

![Architecture](../.gitbook/assets/messageQueue_rocketMQ_architecture.png)

## Definition

* A broker contains a master node and a slave node
  * Broker 1 has topic 1 to 5
  * Broker 2 has topic 6 to 10
* NameNode cluster contains the mapping from Topic=>Broker
* Scenario 1. Consumer group tells name node cluster which topic it subscribe to 2. Broker pulls from name node cluster about the heartbeat message (whether I am alive / topic mapping on the broker) 3. Producer group pushes events to the broker 4. Broker push events to consumer group

## Supported advanced message types

## FIFO message

* The processing of message follows the producing order. 
* Order types
  * Global order
  * Partition order
* How to guarantee

## Delayed message

* Not support any granularity. There are a couple granularity level such as 1s, 5s, 10s, 1 minute, 2 minute, ... 1 hour, 5 hour. 

## Transaction message

* [https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/](https://rocketmq.apache.org/rocketmq/the-design-of-transactional-message/)

**Example**

* Example: A user is purchasing items on an ecommerce website. There are two operations
  1. Create an order in the database
  2. Delete ordered items from the shopping cart. Since this step is not a necessary step to be completed within the order operation, the command could be processed asynchronously, e.g. putting into a message queue. 

**Concept**

* Half (prepare) message: Refers to a message that cannot be delivered temporarily. When a message is successfully sent to the MQ server, but the server did not receive the second acknowledgement of the message from the producer, then the message is marked as “temporarily undeliverable”. The message in this status is called a half message.
* Message status check: Network disconnection or producer application restart may result in the loss of the second acknowledgement of a transactional message. When MQ server finds that a message remains a half message for a long time, it will send a request to the message producer, checking the final status of the message (Commit or Rollback).

**Algorithm**

1. Producer send half message to MQ server.
2. After send half message succeed, execute local transaction.
3. Send commit or rollback message to MQ Server based on local transaction results.
4. If commit/rollback message missed or producer pended during the execution of local transaction，MQ server will send check message to each producers in the same group to obtain transaction status.
5. Producer reply commit/rollback message based on local transaction status.
6. Committed message will be delivered to consumer but rolled back message will be discarded by MQ server.
7. ![Execute flow chart](../.gitbook/assets/mq_transactions_flowchart.png)

## Batch message

## Real world

* Kafka at Netflix: [https://netflixtechblog.com/kafka-inside-keystone-pipeline-dd5aeabaf6bb](https://netflixtechblog.com/kafka-inside-keystone-pipeline-dd5aeabaf6bb)
