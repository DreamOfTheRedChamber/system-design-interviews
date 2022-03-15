- [Consensus algorithm](#consensus-algorithm)
  - [BFT (Byzantine fault tolerance)](#bft-byzantine-fault-tolerance)

# Consensus algorithm

|             |                         |                      |               |                |
| ----------- | ----------------------- | -------------------- | ------------- | -------------- |
| `Algorithm` | `Crash fault tolerance` | `Consistency`        | `Performance` | `Availability` |
| 2PC         | No                      | Strong consistency   | Low           | Low            |
| TCC         | No                      | Eventual consistency | Low           | Low            |
| Paxos       | No                      | Strong consistency   | Middle        | Middle         |
| ZAB         | No                      | Eventual consistency | Middle        | Middle         |
| Raft        | No                      | Strong consistency   | Middle        | Middle         |
| Gossip      | No                      | Eventual consistency | High          | High           |
| Quorum NWD  | No                      | Strong consistency   | Middle        | Middle         |
| PBFT        | Yes                     | N/A                  | Low           | Middle         |
| POW         | Yes                     | N/A                  | Low           | Middle         |

## BFT (Byzantine fault tolerance)

* Within a distributed system, there are no malicious behaviors but could be fault behaviors such as process crashing, hardware bugs, etc.
