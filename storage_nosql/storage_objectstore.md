# Storage\_ObjectStore

* [File system](storage_objectstore.md#file-system)
  * [Scenario](storage_objectstore.md#scenario)
  * [Storage](storage_objectstore.md#storage)
    * [How to save a file in one machine](storage_objectstore.md#how-to-save-a-file-in-one-machine)
    * [How to save a much larger file in one machine](storage_objectstore.md#how-to-save-a-much-larger-file-in-one-machine)
  * [Scale](storage_objectstore.md#scale)
    * [Architecture style](storage_objectstore.md#architecture-style)
    * [How to save an extra large file on several machines](storage_objectstore.md#how-to-save-an-extra-large-file-on-several-machines)
      * [Move chunk offset from master to slaves](storage_objectstore.md#move-chunk-offset-from-master-to-slaves)
    * [Write process](storage_objectstore.md#write-process)
    * [Do not support modification](storage_objectstore.md#do-not-support-modification)
    * [Read process](storage_objectstore.md#read-process)
    * [Master task](storage_objectstore.md#master-task)
    * [Failure and recovery](storage_objectstore.md#failure-and-recovery)
      * [Single master](storage_objectstore.md#single-master)
      * [What if a chunk is broken](storage_objectstore.md#what-if-a-chunk-is-broken)
      * [Avoid loss of data when chunk server is down](storage_objectstore.md#avoid-loss-of-data-when-chunk-server-is-down)
      * [How to recover when a chunk is broken](storage_objectstore.md#how-to-recover-when-a-chunk-is-broken)
      * [How to find whether a chunk server is down](storage_objectstore.md#how-to-find-whether-a-chunk-server-is-down)
      * [How to solve client bottleneck](storage_objectstore.md#how-to-solve-client-bottleneck)
      * [How to solve chunk server failure](storage_objectstore.md#how-to-solve-chunk-server-failure)

## Scenario

* Write a file
* Read a file
* Use multiple machines to store these files

## Storage

### How to save a file in one machine

* Metadata
  * FileInfo
    * Name = dengchao.mp4
    * CreatedTime = 201505031232
    * Size = 2044323
  * Index
    * Block 11 -&gt; diskOffset1
    * Block 12 -&gt; diskOffset2
    * Block 13 -&gt; diskOffset3
* Block
  * 1 block = 1024 Byte
  * Advantages
    * Error checking
    * Fragmenting the data for storage

### How to save a much larger file in one machine

* Change chunk size
  * 1 chunk = 64M = 64 \* 1024K
  * Advantages
    * Reduce size of metadata
  * Disadvantages
    * Waste space for small files

## Scale

### Architecture style

* Peer 2 Peer \(BitComet, Cassandra\)
  * Advantage: No single point of failure
  * Disadvantage: Multiple machines need to negotiate with each other
* Master slave
  * Advantage: Simple design. Easy to keep data consistent
  * Disadvantage: Master is a single point of failure
* Final decision
  * Master + slave
  * Restart the single master

### How to save an extra large file on several machines

* One master + many chunk servers

#### Move chunk offset from master to slaves

* Master don't record the disk offset of a chunk
  * Advantage: Reduce the size of metadata in master; Reduce the traffic between master and chunk server

### Write process

1. The client divides the file into chunks. Create a chunk index for each chunk
2. Send \(FileName, chunk index\) to master and master replies with assigned chunk servers
3. The client transfer data with the assigned chunk server.

### Do not support modification

### Read process

1. The client sends \(FileName\) to master and receives a chunk list \(chunk index, chunk server\) from the master 
2. The client connects with different server for reading files

### Master task

* Store metadata for different files
* Store Map \(file name + chunk index -&gt; chunk server\)
  * Find corresponding server when reading in data
  * Write to more available chunk server

### Failure and recovery

#### Single master

* Double master \(Apache Hadoop Goes Realtime at Facebook\)
* Multi master \(Paxos algorithm\)

#### What if a chunk is broken

* Check sum 4bytes = 32 bit
* Each chunk has a checksum 
* Write checksum when writing out a chunk
* Check checsum when reading in a chunk

#### Avoid loss of data when chunk server is down

* Replica: 3 copies
  * Two copies in the same data center but on different racks
  * Third copy in a different data center
* How to choose chunk servers
  * Find servers which are not busy
  * Find servers with lots of available disk space

#### How to recover when a chunk is broken

* Ask master for help

#### How to find whether a chunk server is down

* Heart beat message

#### How to solve client bottleneck

* Client only writes to a leader chunk server. The leader chunk server is responsible for communicating with other chunk servers. 
* How to select leading slaves

#### How to solve chunk server failure

* Ask the client to retry

