
- [Overview](#overview)
  - [Pluggable engines](#pluggable-engines)
    - [Selection criteria](#selection-criteria)
  - [Redo logs](#redo-logs)
  - [Undo logs](#undo-logs)
  - [Server layer](#server-layer)
    - [Binlog - TODO](#binlog---todo)
    - [Slow query log](#slow-query-log)
  - [General purpose log](#general-purpose-log)
  - [Relay log](#relay-log)
    - [InnoDB engine](#innodb-engine)
      - [Components](#components)

# Overview

## Pluggable engines

* Theoretically, different tables could be configured with different engines. 
* There are a list of innoDB engines such as Innodb
  * InnoDB: support transaction, support row level lock 
  * MyISAM: not support transaction, only table level lock
  * Archive
  * Memory
  * CSV
  * Federated
  * TokuDB: 
* InnoDB vs MyISAM: 

### Selection criteria

* Need to support transaction? 
* Need to support hot online backup?
  * mysqldump
  * Innodb is the only engine supports online backup
* Need to support crush recovery?

## Redo logs

## Undo logs

## Server layer

### Binlog - TODO

* Reference: [https://coding.imooc.com/lesson/49.html#mid=486](https://coding.imooc.com/lesson/49.html#mid=486)

### Slow query log

## General purpose log

## Relay log

### InnoDB engine

#### Components

![](.gitbook/assets/mysql_internal_innodb_arch.png)
