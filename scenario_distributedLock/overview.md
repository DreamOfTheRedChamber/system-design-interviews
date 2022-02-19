- [Distributed lock](#distributed-lock)
  - [Use cases](#use-cases)
  - [Requirementss](#requirementss)
  - [Comparison](#comparison)
  - [Usage example in oversell](#usage-example-in-oversell)
    - [Case 1: Subtract purchased count in program and update database to target count](#case-1-subtract-purchased-count-in-program-and-update-database-to-target-count)
    - [Case 2: Decrement database count with purchased count](#case-2-decrement-database-count-with-purchased-count)

# Distributed lock

## Use cases

* Efficiency: Taking a lock saves you from unnecessarily doing the same work twice \(e.g. some expensive computation\).
  * e.g. If the lock fails and two nodes end up doing the same piece of work, the result is a minor increase in cost \(you end up paying 5 cents more to AWS than you otherwise would have\)
  * e.g. SNS scenarios: A minor inconvenience \(e.g. a user ends up getting the same email notification twice\).
  * e.g. eCommerce website inventory control
* Correctness: Taking a lock prevents concurrent processes from stepping on each others’ toes and messing up the state of your system. If the lock fails and two nodes concurrently work on the same piece of data, the result is a corrupted file, data loss, permanent inconsistency, the wrong dose of a drug administered to a patient, or some other serious problem.

## Requirementss

* Exclusive
* Avoid deadlock
* High available
* Reentrant

## Comparison

| Approach | Pros | Cons |
| :--- | :--- | :--- |
| Database | Easy to understand | High pressure on DB |
| Redis | Easy to understand | Not support blocking |
| Zookeeper | Support blocking | Rely on Zookeeper, high complexity |
| Curator | Easy to use | Rely on Zookeeper, |
| Redisson | Easy to use, support blocking |  |

## Usage example in oversell
### Case 1: Subtract purchased count in program and update database to target count

```text
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
                        Program                        │
│                                                       
     ┌──────────────────────────────────────────┐      │
│    │    Get the number of inventory items     │       
     └──────────────────────────────────────────┘      │
│                          │                            
                           ▼                           │
│     ┌─────────────────────────────────────────┐       
      │Subtract the number of purchased items to│      │
│     │           get target count B            │       
      │                                         │      │
│     └─────────────────────────────────────────┘       
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┬ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
                           │                            
                           │                            
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
                           ▼                           │
│     ┌─────────────────────────────────────────┐       
      │    Update database to target count B    │      │
│     └─────────────────────────────────────────┘       
                                                       │
│                       Database                        
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
```

### Case 2: Decrement database count with purchased count

```text
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
                        Program                        │
│                                                       
     ┌──────────────────────────────────────────┐      │
│    │    Get the number of inventory items     │       
     └──────────────────────────────────────────┘      │
│                          │                            
                           │                           │
└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
                           │                            
                           ▼                            
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ 

│     ┌─────────────────────────────────────────┐     │ 
      │     Decrement database count with B     │       
│     └─────────────────────────────────────────┘     │ 

│                      Database                       │ 
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
```
