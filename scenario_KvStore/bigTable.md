# Standalone solution

## Design thoughts

1. Sorted file with (Key, Value) entries
   * Disk-based binary search based read O(lgn)
   * Linear read operations write O(n)
2. Unsorted file with (Key, Value) entries. Then build index on top of it.
   * Linear read operations O(n)
   * Constant time write O(1)
3. Combine append-only write and binary search read
   * Process:
     * Break the large table into a list of smaller tables 0 to N
       * 0 to N-1 th tables are all stored in disk in sorted order as File 0 to File N-1.
       * Nth table is stored in disk unsorted as File N.
     * Have a in-memory table mapping mapping tables/files to its address.
   * Write: O(1)
     * Write directly goes to the Nth table/file.
     * If the Nth table is full, sort it and write it to disk. And then create a new table/file.
   * Read: O(n)
     * Linearly scan through the Nth table.
     * If cannot find, perform binary search on N-1, N-2, ..., 0th.
4. Store the Nth table/file in memory
   * Disk-based approach vs in-memory approach
     * Disk-based approach: All data Once disk reading + disk writing + in-memory sorting
     * In-memory approach: All data Once disk writing + in-memory sorting
   * What if memory is lost?
     * Problem: Nth in memory table is lost.
     * Write ahead log / WAL: The WAL is the lifeline that is needed when disaster strikes. Similar to a BIN log in MySQL it records all changes to the data. This is important in case something happens to the primary storage. So if the server crashes it can effectively replay that log to get everything up to where the server should have been just before the crash. It also means that if writing the record to the WAL fails the whole operation must be considered a failure. Have a balance between between latency and durability.
5. Further optimization
   * Write: How to Save disk space. Consume too much disk space due to repetitive entries (Key, Value)
     * Have a background process doing K-way merge for the sorted tables regularly
   * Read:
     * Optimize read with index
       * Each sorted table should have an index inside memory.
         * The index is a sketch of key value pairs
       * More advanced way to build index with B tree.
     * Optimize read with Bloom filter
       * Each sorted table should have a bloomfilter inside memory.
       * Accuracy of bloom filter
         * Number of hash functions
         * Length of bit vector
         * Number of stored entries

## Initial design flow chart

```
      ┌─────────────────────────────┐              ┌─────────────────────────┐         
      │Read tries to find the entry │              │                         │         
      │in the following order:      │              │Write directly happens to│         
      │1. in-memory sorted list     │              │  in-memory sorted list  │         
      │2. If not found, then search │              │                         │         
      │the in-disk sorted list in   │              │                         │         
      │reverse chronological order -│              └──────┬─────────▲────────┘         
      │newer ones first, older ones │                     │         │                  
      │later                        │                     │         │                  
      └─────────────────────────────┘                     │         │                  
                                                          │         │                  
                                                          │         │                  
┌─────────────────────────────────────────────────────────┼─────────┼─────────────────┐
│                                     Data Server         │         │                 │
│                                                         │         │                 │
│   ┌─────────────────────────────────────────────────────┼─────────┼──────────┐      │
│   │                          In-Memory sorted list      │         │          │      │
│   │                                                     │         │          │      │
│   │                               key1, value1          ▼         │          │      │
│   │                               key2, value2                               │      │
│   │                                   ...                                    │      │
│   │                               keyN, valueN                               │      │
│   └──────────────────────────────────────────────────────────────────────────┘      │
│                                                                                     │
│                                                                                     │
│                                                                                     │
│   ┌────────────┐   ┌────────────┐  ┌────────────┐   ┌────────────┐  ┌────────────┐  │
│   │bloom filter│   │bloom filter│  │            │   │bloom filter│  │bloom filter│  │
│   │and index 1 │   │and index 2 │  │   ......   │   │ and index  │  │and index N │  │
│   │            │   │            │  │            │   │    N-1     │  │            │  │
│   └────────────┘   └────────────┘  └────────────┘   └────────────┘  └────────────┘  │
│                                                                                     │
│   ┌────────────┐   ┌────────────┐  ┌────────────┐   ┌────────────┐  ┌────────────┐  │
│   │            │   │            │  │            │   │            │  │            │  │
│   │  In-disk   │   │  In-disk   │  │            │   │  In-disk   │  │  In-disk   │  │
│   │sorted list │   │sorted list │  │   ......   │   │sorted list │  │sorted list │  │
│   │     1      │   │     2      │  │            │   │    N-1     │  │     N      │  │
│   │            │   │            │  │            │   │            │  │            │  │
│   │            │   │            │  │            │   │            │  │            │  │
│   └────────────┘   └────────────┘  └────────────┘   └────────────┘  └────────────┘  │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘


    ┌─────┐                                                               ┌─────┐      
   ─┤older├─────────────────────Chronological order───────────────────────┤newer├─────▶
    └─────┘                                                               └─────┘
```

### Read process

1. First check the Key inside in-memory skip list.
2. Check the bloom filter for each file and decide which file might have this key.
3. Use the index to find the value for the key.
4. Read and return key, value pair.

### Write process

1. Record the write operation inside write ahead log.
2. Write directly goes to the in-memory skip list.
3. If the in-memory skip list reaches its maximum capacity, sort it and write it to disk as a Sstable. At the same time create index and bloom filter for it.
4. Then create a new table/file.

### Pros

* Optimized for write: Write only happens to in-memory sorted list

### Cons

* In the worst case, read needs to go through a chain of units (in-memory, in-disk N, ..., in-disk 1)
  * Compaction could help reduce the problem
