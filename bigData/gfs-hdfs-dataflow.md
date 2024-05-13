- [Data flow](#data-flow)
  - [Identify the bottleneck](#identify-the-bottleneck)
  - [Terminology](#terminology)
  - [GFS write (control signal/data) flow](#gfs-write-control-signaldata-flow)
    - [Separation of control signal and data flow](#separation-of-control-signal-and-data-flow)
    - [GFS data flow](#gfs-data-flow)
      - [Process](#process)
      - [Reason](#reason)

# Data flow
## Identify the bottleneck
* In GFS case
  * It uses 100Mbps network card, it has a maximum throughput of 12.5MB/s. 
  * It uses 5400 rpm disk, its bandwidth is usually 60~90MB/s. And multiple hard disks could be plugged together, and there will be a maximum bandwidth of 500MB/s. 
  * The bottleneck is in network layer. 

## Terminology
* Master: Stores the metadata.
* Primary replica.
* Secondary replica.

## GFS write (control signal/data) flow
### Separation of control signal and data flow
* Master only tells GFS client which chunk servers to read/write data. After that it is out of the business. 

![](../.gitbook/assets/gfs_writeprocess.png)

1. **Control signal**: Client queries master for locations of chunk servers. 
2. **Control signal**: Master replies with primary and secondary replica locations of chunk servers. 
3. **Data**: Client sends data to all replicas (by picking the nearest replica first). However, after secondary replicas receive the data, they will not immediately write it to disk. Instead, they will cache it in the memory. 
4. **Control signal**: After all secondary replicas receive data, clients will send a write request to primary replica. Primary replica will order all the write requests. 
5. **Control signal**: Primary replica will forward all write requests to secondary replicas. Then all secondary replicas will write data to disk with the same order. 
6. **Control signal**: After secondary replicas finish writing, they will reply to primary replica that they have finished. 
7. **Control signal**: Primary replica will tell clients that write requests have completed successfully. 

### GFS data flow
#### Process
* Data might not first be transmitted to primary replica. It depends on which replica is closer to the client. 
* Then the closer replica will send the data to the next replica. 

![](../.gitbook/assets/gfs_writeBandwidth.png)

#### Reason
* All servers on the same rack will be plugged to the same access switch. 
* All switches on the rack will connect to a single aggregate switch. 
* Aggregate switches will connect to core switch. 

![](../.gitbook/assets/gfs_network_topology.png)

