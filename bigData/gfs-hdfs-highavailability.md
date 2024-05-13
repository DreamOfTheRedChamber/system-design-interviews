- [Disk fault tolerance](#disk-fault-tolerance)
- [ChunkServer/DataNode high availability](#chunkserverdatanode-high-availability)
- [Master/NameNode high availability](#masternamenode-high-availability)
  - [Master backup](#master-backup)
  - [Shadow backup](#shadow-backup)
  - [Restart master](#restart-master)

# Disk fault tolerance

# ChunkServer/DataNode high availability

# Master/NameNode high availability

![](../.gitbook/assets/master_high_availability.png)

## Master backup
* The above procedure could handle software but not hardware failures. 
* If the master has hardware failures, then it could failover to the backups which master synchronously replicates to. 

## Shadow backup
* The switch process could take seconds or minutes to complete. 
* In the meanwhile, When compared with shadow master used for availability for asynchronous read. 
* The data in shadow back might be stale. But the chance that client read stale metadata from shadow backup is quite slim because it only happens when all these three conditions are met: 
  * Master is dead. 
  * The metadata on master has not completely been replicated to shadow backup. 
  * The data clients is trying to read is just these metadata not replicated yet. 

## Restart master
* All master metadata is cached inside memory. There will be checkpoints where all memory is dumped to disk. 
* If the master has software failures, then it will first recover from checkpoints. And then operation logs after that timestamp will be replayed. 

