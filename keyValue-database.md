
<!-- MarkdownTOC -->

- [LevelDB](#leveldb)
	- [Architecture](#architecture)
		- [Memory](#memory)
		- [Storage](#storage)
			- [Write ahead log](#write-ahead-log)
		- [Compact thread](#compact-thread)
			- [Conditions for compaction](#conditions-for-compaction)
			- [Minor compaction](#minor-compaction)
			- [Major compaction](#major-compaction)
	- [Write process](#write-process)
		- [Downsides of B+ tree](#downsides-of-b-tree)
		- [LSM tree](#lsm-tree)
		- [LSM tree write process](#lsm-tree-write-process)
		- [Memtable format](#memtable-format)
	- [Read process](#read-process)
		- [SStable format](#sstable-format)
	- [Cache](#cache)
	- [MVCC](#mvcc)
		- [Recover process](#recover-process)
		- [Repair process](#repair-process)
- [Reference:](#reference)

<!-- /MarkdownTOC -->


# LevelDB
## Architecture
![levelDB architecture](./images/leveldb_architecture.png)

### Memory

### Storage

#### Write ahead log
* log format
	- WAL log
	- block
	- record

![levelDB log format](./images/leveldb_logFormat.png)

![levelDB log format 2](./images/leveldb_logFormat2.png)

* log record payload
	- writebatch
	- put_op
	- delete_op

![levelDB log format](./images/leveldblogrecordformat.jpg)

### Compact thread
#### Conditions for compaction


#### Minor compaction
1. turn immutable memtable dump into sstable

![levelDB minor compaction](./images/leveldb_compaction_minor.jpg)

2. Determine the level of sstable

![levelDB sstable level](./images/leveldb_compaction_sstable_level.jpg)

#### Major compaction

## Write process

### Downsides of B+ tree
![levelDB BPlus tree](./images/leveldb_BPlusTree.jpg)

### LSM tree

![levelDB lsm tree](./images/leveldb_lsmtree.jpg)

### LSM tree write process
* Write steps:

### Memtable format

![levelDB memtable format](./images/leveldb_memtableformat.jpg)

## Read process

![levelDB read operation](./images/leveldb_readoperation.jpg)

### SStable format

![levelDB sstable format](./images/leveldb_sstableformat.jpg)

![levelDB sstable format v2](./images/leveldb_sstableformatv2.png)

## Cache

![levelDB cache](./images/leveldb_lrucache.jpg)

## MVCC

![levelDB manifest format](./images/leveldb_manifestformat.jpg)

![levelDB manifest format V2](./images/leveldb_manifestformatv2.jpg)

### Recover process

![levelDB manifest recover process](./images/leveldb_mvcc_recoverprocess.jpg)

### Repair process

![levelDB manifest repair process](./images/leveldb_mvcc_repairprocess.jpg)


# Reference: 
1. using level DB and Rocks DB as an example - https://soulmachine.gitbooks.io/system-design/content/cn/key-value-store.html
2. Meituan build on top of tair and redis - https://tech.meituan.com/2020/07/01/kv-squirrel-cellar.html
3. Series of blog on key value store - http://codecapsule.com/2012/11/07/ikvs-implementing-a-key-value-store-table-of-contents/
4. MIT spring 2018. Final course on KV store - http://nil.csail.mit.edu/6.824/2018/projects.html
5. Raft-based implementation 极客时间：https://time.geekbang.org/column/article/217049
6. Taobao tair: https://time.geekbang.org/column/article/217049
