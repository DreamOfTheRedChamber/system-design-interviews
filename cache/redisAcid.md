- [Persistence options](#persistence-options)
  - [COW](#cow)
  - [Pros and Cons between RDB and AOF](#pros-and-cons-between-rdb-and-aof)
  - [RDB](#rdb)
  - [AOF](#aof)

# Persistence options
## COW
* Both RDB and AOF relies on Unix Copy on Write mechanism
* [http://oldblog.antirez.com/post/a-few-key-problems-in-redis-persistence.html](http://oldblog.antirez.com/post/a-few-key-problems-in-redis-persistence.html)

## Pros and Cons between RDB and AOF

* [https://redis.io/topics/persistence](https://redis.io/topics/persistence)

## RDB

* Command: SAVE vs BGSAVE. Whether a child process is forked to create RDB file. 
* BGSAVE - Automatic save condition
  * saveparam format: save   seconds   changes
  * dirty attribute: How many databse operations have been performed after the last time.
  * lastsave: A unix timestamp - the last time the server executes SAVE or BGSAVE. 

```text
def serverCron():
    for saveParam in server.saveparams:
        save_internal = unixtime_now() - server.lastsave

        if server.dirty >= saveparam.changes 
            and save_internal > saveparams.seconds:
                BGSAVE()
```

## AOF

* FlushAppendOnlyFile's behavior depends on appendfsync param:
  * always: Write aof\_buf to AOF file and sync to slaves on each file event loop.
  * everysec: Write aof\_buf to AOF file on each file event loop. If the last synchronization happens before 1 sec, then sync to slaves on each file event loop. 
  * no: Write aof\_buf to AOF file on each file event loop. Depend on the OS to determine when to sync.
* AOF rewrite:
  * Goal: Reduce the size of AOF file.
  * AOF rewrite doesn't need to read the original AOF file. It directly reads from database. 
  * Redis fork a child process to execute AOF rewrite dedicatedly. Redis opens a AOF rewrite buffer to keep all the instructions received during the rewriting process. At the end of rewriting AOF file, all instructions within AOF rewrite buffer will be flushed to the new AOF file. 