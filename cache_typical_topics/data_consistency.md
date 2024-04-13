- [Data inconsistency](#data-inconsistency)
  - [Native cache aside pattern](#native-cache-aside-pattern)
  - [Transaction](#transaction)
  - [Messge queue](#messge-queue)
  - [Subscribe MySQL binlog as a slave](#subscribe-mysql-binlog-as-a-slave)
  - [inconsistency between local and distributed cache](#inconsistency-between-local-and-distributed-cache)


# Data inconsistency

* Inconsistency between DB and distributed cache

* Solutions
  * Native cache aside pattern

## Native cache aside pattern

* Cons:
  * If updating to database succeed and updating to cache fails, 

```
┌───────────┐       ┌───────────────┐                             ┌───────────┐
│  Client   │       │  distributed  │                             │ Database  │
│           │       │     cache     │                             │           │
└───────────┘       └───────────────┘                             └───────────┘

      │                     │                                           │      
      │                     │                                           │      
      ├─────────────────────┼────write database─────────────────────────▶      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     ◀──────────────invalidate cache─────────────┤      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │      
      │                     │                                           │
```

## Transaction

* Put redis and mySQL update inside a transaction
  * Performance cost

## Messge queue

* Cons:
  * Additional cost for maintaining a message queue
  * If there are multiple updates to the DB, its sequence in message queue might be mixed.

```
┌───────────┐       ┌───────────────┐       ┌───────────┐         ┌───────────┐
│  Client   │       │  distributed  │       │  Message  │         │ Database  │
│           │       │     cache     │       │   Queue   │         │           │
└───────────┘       └───────────────┘       └───────────┘         └───────────┘

      │                     │                     │                     │      
      │                     │                     │                     │      
      ├─────────────────────┼────write database───┼─────────────────────▶      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │        Send a       │      
      │                     │                     │◀─────message to─────┤      
      │                     │                     │      invalidate     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │      invalidate     │                     │      
      │                     ◀─────────cache───────┤                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │
```

## Subscribe MySQL binlog as a slave

```
┌───────────┐    ┌───────────────┐     ┌───────────────┐    ┌─────────────┐      ┌─────────────┐
│           │    │               │     │               │    │Fake db slave│      │  Database   │
│  Client   │    │  distributed  │     │ Message queue │    │             │      │             │
│           │    │     cache     │     │               │    │(e.g. canal) │      │(e.g. MySQL) │
│           │    │               │     │               │    │             │      │             │
└───────────┘    └───────────────┘     └───────────────┘    └─────────────┘      └─────────────┘
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      ├──────────Subscribe to MQ────────────▶                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │    subscribe to     │       
      │                 │                   │                     ├──binlog as a slave──▶       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      ├─────────────────┼──────────────write database─────────────┼─────────────────────▶       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │    publish binlog   │       
      │                 │                   │                     │◀──────to slave──────┤       
      │                 │                   │        convert      │                     │       
      │                 │                   │       binlog to     │                     │       
      │                 │                   ◀──────message and ───┤                     │       
      │                 │                   │        publish      │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      ◀───────receive published message─────┤                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │   update        │                   │                     │                     │       
      ├───cache─────────▶                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │       
      │                 │                   │                     │                     │
```

## inconsistency between local and distributed cache

```
// Scenario: update distributed cache as administrator operations
┌───────────┐       ┌───────────────┐       ┌───────────┐         ┌───────────┐
│application│       │  local cache  │       │distributed│         │ Database  │
│           │       │               │       │   cache   │         │           │
└───────────┘       └───────────────┘       └───────────┘         └───────────┘

      │                     │                     │                     │      
      │                     │                     │                     │      
      ├──────────────subscribe to change──────────▶                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │        update       │      
      │                     │                     │◀──────value as ─────┤      
      │                     │                     │        admin        │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      ◀──────────receive published message────────┤                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │     update          │                     │                     │      
      ├───local cache───────▶                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │      
      │                     │                     │                     │
```
