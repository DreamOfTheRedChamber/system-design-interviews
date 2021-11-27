- [Data structure](#data-structure)
  - [SDS \(Simple dynamic string\)](#sds-simple-dynamic-string)
  - [Hash](#hash)
    - [Structure](#structure)
    - [Incremental resizing](#incremental-resizing)
    - [Encoding](#encoding)
  - [Skiplist](#skiplist)
  - [Memory efficient data structures](#memory-efficient-data-structures)
    - [Ziplist](#ziplist)
    - [IntSet](#intset)
  - [Object](#object)
- [Advanced data structures](#advanced-data-structures)
  - [HyperLogLog](#hyperloglog)
  - [Bloomberg filter](#bloomberg-filter)
  - [Bitmap](#bitmap)
  - [Stream](#stream)

# Data structure

## SDS \(Simple dynamic string\)

* Redis implements SDS on top of c string because of the following reasons:
  1. Reduce the strlen complexity from O\(n\) to O\(1\)
  2. Avoid buffer overflow because C needs to check string has enough capacity before executing operations such as strcat. 
* SDS has the following data structure

```text
struct sdshdr 
{
    int len;
    int free;
    char buf[];
};
```

* SDS relies on the following two mechanisms for unused space.
  1. Space preallocation. The preallocation algorithm used is the following: every time the string is reallocated in order to hold more bytes, the actual allocation size performed is two times the minimum required. So for instance if the string currently is holding 30 bytes, and we concatenate 2 more bytes, instead of allocating 32 bytes in total SDS will allocate 64 bytes. However there is an hard limit to the allocation it can perform ahead, and is defined by SDS\_MAX\_PREALLOC. SDS will never allocate more than 1MB of additional space \(by default, you can change this default\).
  2. Lazy free: When space is freed, it is marked as free but not immediately disallocated.
  3. Binary safety. C structure requires char comply with ASCII standards. 
  4. Compatible with C string functions. SDS will always allocate an additional char as terminating character so that SDS could reuse some C string functions. 

## Hash

### Structure

* dict in Redis is a wrapper on top of hashtable

```text
typedef struct dict 
{
    dictType *type;
    void *privdata;

    // hash table
    dictht ht[2];

    // rehash index
    // rehashing not in progress if rehashidx == -1
    int trehashidx;
}
```

### Incremental resizing

* Load factor = total\_elements / total\_buckets
* Scale up condition: load factor &gt;= 1 \(or load factor &gt; 5\) and no heavy background process \(BGSAVE or BGREWRITEAOF\) is happening
* Scale down condition: load factor &lt; 0.1
* Condition to stop rehash:
  1. Incremental hashing usually follows these conditions: dictAddRaw / dictGenericDelete / dictFind / dictGetRandomKey
  2. Incremental hashing is also scheduled in server cron job.
* During the resizing process, all add / update / remove operations need to be performed on two tables. 

### Encoding

## Skiplist

**Skiplist vs balanced tree in ZSet**

* They are not very memory intensive. It's up to you basically. Changing parameters about the probability of a node to have a given number of levels will make then less memory intensive than btrees.
* A sorted set is often target of many ZRANGE or ZREVRANGE operations, that is, traversing the skip list as a linked list. With this operation the cache locality of skip lists is at least as good as with other kind of balanced trees.
* They are simpler to implement, debug, and so forth. For instance thanks to the skip list simplicity I received a patch \(already in Redis master\) with augmented skip lists implementing ZRANK in O\(log\(N\)\). It required little changes to the code.
* [https://github.com/antirez/redis/blob/90a6f7fc98df849a9890ab6e0da4485457bf60cd/src/ziplist.c](https://github.com/antirez/redis/blob/90a6f7fc98df849a9890ab6e0da4485457bf60cd/src/ziplist.c)

## Memory efficient data structures

### Ziplist

**Structure**

* zlbytes: Is a 4 byte unsigned integer, used to store the entire ziplist number of bytes used.
* zltail: Is a 4 byte unsigned integer, used to store the ziplist of the last node relative to the ziplist first address offset.
* zllen: Is a 2 byte unsigned integer, the number of nodes stored in ziplist, maximum value for \(2^16 - 2\), when zllen is greater than the maximum number of value when, need to traverse the whole ziplist to obtain the ziplist node.
* zlend: Is a 1 byte unsigned integer, value 255 \(11111111\), as the end of the ziplist match.
* entryX: Node ziplist, each node could represent a length-limited int or char.
  1. prev\_entry\_bytes\_length: 
  2. content:

**Complexity**

* Insert operation. Worst case: O\(N^2\). Best case: O\(1\). Average case o\(N\)
  * Cascade update: When an entry is inserted, we need to set the prevlen field of the next entry to equal the length of the inserted entry. It can occur that this length cannot be encoded in 1 byte and the next entry needs to be grow a bit larger to hold the 5-byte encoded prevlen. This can be done for free, because this only happens when an entry is already being inserted \(which causes a realloc and memmove\). However, encoding the prevlen may require that this entry is grown as well. This effect may cascade throughout the ziplist when there are consecutive entries with a size close to ZIP\_BIGLEN, so we need to check that the prevlen can be encoded in every consecutive entry.
* Delete operation. Worst case: O\(N^2\). Best case: O\(1\). Average case o\(N\)
  * Cascade update: Note that this effect can also happen in reverse, where the bytes required to encode the prevlen field can shrink. This effect is deliberately ignored, because it can cause a flapping effect where a chain prevlen fields is first grown and then shrunk again after consecutive inserts. Rather, the field is allowed to stay larger than necessary, because a large prevlenfield implies the ziplist is holding large entries anyway.
* Iterate operation.
* [https://redisbook.readthedocs.io/en/latest/compress-datastruct/ziplist.html](https://redisbook.readthedocs.io/en/latest/compress-datastruct/ziplist.html)

### IntSet

**Structure**

```text
typedef struct intset 
{
    uint32_t encoding; // INSET_ENC_INT16, INTSET_ENC_INT32, INTSET_ENC_INT64
    uint32_t length;
    int8_t contents[]; 
}
```

**Upgrade and downgrade**

* As long as there is one item in the content which has bigger size, the entire content array will be upgraded.
* No downgrade is provided. 

## Object

* Definition

```text
typedef struct redisObject
{
    unsigned type:4;
    unsigned encoding:4;
    void *ptr;
} robj;
```

* Type and encoding. Encoding gives Type the flexibility to use differnt object type under different scenarios. 

| type | encoding |
| :--- | :--- |
| Redis\_String | REDIS\_ENCODING\_INT |
| Redis\_String | REDIS\_ENCODING\_EMBSTR |
| Redis\_String | REDIS\_ENCODING\_RAW |
| Redis\_List | REDIS\_ENCODING\_ZIPLIST |
| Redis\_List | REDIS\_ENCODING\_LINKEDLIST |
| Redis\_Hash | REDIS\_ENCODING\_ZIPLIST |
| Redis\_Hash | REDIS\_ENCODING\_HT |
| Redis\_Set | REDIS\_ENCODING\_INTSET |
| Redis\_Set | REDIS\_ENCODING\_HT |
| Redis\_ZSet | REDIS\_ENCODING\_ZIPLIST |
| Redis\_ZSet | REDIS\_ENCODING\_SKIPLIST |

* string
  * Three coding formats:
    1. Int: if the target could be represented using a long.
    2. Embstr: If the target is smaller than 44 bytes. Embstr is read-only but it only needs one-time to allocate free space. Embstr could also better utilizes local-cache. Represented using SDS.
    3. Raw: Longer than 45 bytes. Represented using SDS.
* Data structure to be memory efficient \([https://redis.io/topics/memory-optimization](https://redis.io/topics/memory-optimization)\)

```text
hash-max-zipmap-entries 512 (hash-max-ziplist-entries for Redis >= 2.6)
hash-max-zipmap-value 64  (hash-max-ziplist-value for Redis >= 2.6)
list-max-ziplist-entries 512
list-max-ziplist-value 64
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
set-max-intset-entries 512
```

# Advanced data structures

## HyperLogLog

* pfadd/pfcount/pfmerge
* pf means Philippe Flajolet

## Bloomberg filter

* bf.add/bf.exists/bf.madd/bf.mexists

## Bitmap

* Commands: setbit/getbit/bitcountt/bitpos/bitfield

## Stream
