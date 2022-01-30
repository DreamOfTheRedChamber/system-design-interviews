- [ActiveMQ](#activemq)
  - [JMS](#jms)
  - [Persistence](#persistence)
  - [Transaction support](#transaction-support)
  - [Supported protocols](#supported-protocols)
  - [High availability](#high-availability)

# ActiveMQ

## JMS

* Fully support JMS standard. JMS standards defined API client set will use. JMS defines the  following: 
  * Concept: Connection/Session/MessageProducer/MessageConsumer/Broker/Message
  * Message model: Point-to-Point, Pub/Sub
  * Message structure: Header, body

## Persistence

* Support JDBC, AMQ, KahaDB, LevelDB
  * JDBC: easy to manage but low performant
  * AMQ: File based storage mechanism. Performance better than JDBC
  * KahaDB: Default persistence storage
    * Write batch size
    * Cache size
    * File size
  * LevelDB: KV storage

## Transaction support

## Supported protocols

* Supported protocols:
  * AUTO, OpenWire, AMQP, Stomp, MQTT
* Transmission mechanism
  * VM, TCP, SSL, UDP, Peer, Multicast, HTTPS
  * Failover, fanout, discovery, zeroConf

## High availability

* Shared storage master slave (file system/database/sharded database)
* Broker cluster