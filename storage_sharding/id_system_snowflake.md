
# Snowflake

* The IDs are made up of the following components:
  1. Epoch timestamp in millisecond precision - 41 bits (gives us 69 years with a custom epoch)
  2. Configured machine id - 10 bits (gives us up to 1024 machines)
  3. Sequence number - 12 bits (A local counter per machine that rolls over every 4096)

![Snowflake algorithm](../.gitbook/assets/uniqueIDGenerator_snowflake.png)

## Pros

1. 64-bit unique IDs, half the size of a UUID
2. Can use time as first component and remain sortable
3. Distributed system that can survive nodes dying

## Cons
1. Would introduce additional complexity and more ‘moving parts’ (ZooKeeper, Snowflake servers) into our architecture.
2. If local system time is not accurate, it might generate duplicated IDs. For example, when time is reset/rolled back, duplicated ids will be generated.
3. (Minor) If the QPS is not high such as 1 ID per second, then the generated ID will always end with "1" or some number, which resulting in uneven shards when used as primary key. 
   * Solutions: 1. timestamp uses ms instead of s. 2. the seed for generating unique number could be randomized.

