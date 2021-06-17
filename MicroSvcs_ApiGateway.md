- [API Gateway](#api-gateway)
  - [Functionalities [Todo]](#functionalities-todo)
    - [Rate limiting](#rate-limiting)
  - [Existing solutions](#existing-solutions)
    - [Zuul [TODO]](#zuul-todo)
    - [How does Zuul get routing table](#how-does-zuul-get-routing-table)
      - [Eureka](#eureka)
      - [Domain name](#domain-name)
      - [Apollo](#apollo)
  - [References](#references)

# API Gateway

## Functionalities [Todo]
* https://time.geekbang.org/course/detail/100003901-2270

### Rate limiting
![](./images/monitorSystem_HealthCheck_distributedratelimiting_centralized.png)
![](./images/monitorSystem_HealthCheck_distributedratelimiting_distributed.png)

## Existing solutions
### Zuul [TODO]
* Zuul architecture: https://time.geekbang.org/course/detail/100003901-2271

![MySQL HA github](./images/microservices-gateway-deployment.png)

![MySQL HA github](./images/microservices-gateway-deployment2.png)


### How does Zuul get routing table
#### Eureka 

![MySQL HA github](./images/microservices-gateway-eureka.png)

#### Domain name

![MySQL HA github](./images/microservices-gateway-domainName.png)

#### Apollo

![MySQL HA github](./images/microservices-gateway-apollo.png)



## References
* 美团APIGateway: https://zhuanlan.zhihu.com/p/374130800