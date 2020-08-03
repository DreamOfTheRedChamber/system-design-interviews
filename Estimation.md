## Example: Design load balancing mechanism for an application with 10M DAU
* 10M DAU will be normal for applications such as Github. 

* Traffic voluem estimation
1. 10M DAU. Suppose each user operate 10 times a day. Then the QPS will be roughly ~ 1160 QPS
2. Peak value 10 times average traffic ~ 11600 QPS
3. Suppose volume need to increase due to static resource, microservices. Suppose 10. QPS ~ 116000 QPS. 

* Capacity planning
1. Multiple DC: QPS * 2 = 232000
2. Half-year volume increase: QPS * 1.5 = 348000

* Mechanism
1. No DNS layer 
2. LVS

## Single machine capability
* For a 32 core 64GB machine, it could at mmost process 20K "hello world" per second. 
* For the actual business logic, the RPS will be much lower, several hundreds per second. 