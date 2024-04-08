
- [Java Concurrent Utilities - JCU](#java-concurrent-utilities---jcu)
  - [List](#list)
  - [Map](#map)
  - [Set](#set)
  - [Queue](#queue)

# Java Concurrent Utilities - JCU

## List

* CopyOnWriteArrayList: It will have two lists inside. Each time a write operation happens, a new copied list will be created for write operations. Read operations will be performed on the original list

## Map

* ConcurrentHashMap: key not ordered
* ConcurrentSkiplistMap: key ordered

## Set

* CopyOnWriteArraySet
* ConcurrentSkipListSet

## Queue

**Single end queue**

* Blocking queue:
  * ArrayBlockingQueue: array as inside queue
  * LinkedBlockingQueue: list as inside queue
  * SynchronousQueue: No queue inside
  * LinkedTransferQueue: combination of synchronousQueue and LinkedBlockingQueue
* PriorityBlockingQueue
* DelayQueue

**Deque**

* BlockingDeque
* ConcurrentLinkedQueue
* ConcurrentLinkedDeque