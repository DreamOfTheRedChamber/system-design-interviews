- [NonFunc](#nonfunc)
  - [Requirements](#requirements)
  - [Data transmission](#data-transmission)

# NonFunc
## Requirements

* Realtime: Incident handling typically requires real-time data.
* High availability: Monitoring system
* High throughput: Lots of data to monitor
* Lose messsage is tolerated

## Data transmission
* Protocol
  * Use UDP protocol to directly transmit to servers
  * Send to specific topic inside Kafka, and consumers read from Kafka topic. 
* Serialization
  * Protobuf
  * Json