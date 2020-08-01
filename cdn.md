<!-- MarkdownTOC -->

- [Why CDN, not web storage / distributed cache?](#why-cdn-not-web-storage--distributed-cache)
- [Flowchart](#flowchart)
  - [How to put an item on CDN](#how-to-put-an-item-on-cdn)
  - [How to get an item from CDN](#how-to-get-an-item-from-cdn)
- [Cache-Control headers](#cache-control-headers)
  - [Definition](#definition)
  - [Flow chart](#flow-chart)
- [Conditional Get headers](#conditional-get-headers)
- [What could be put on CDN](#what-could-be-put-on-cdn)
- [CDN internal](#cdn-internal)
  - [Global server load balance - GSLB](#global-server-load-balance---gslb)
  - [Cache proxy](#cache-proxy)
    - [Architecture](#architecture)
- [What could be cached on CDN?](#what-could-be-cached-on-cdn)

<!-- /MarkdownTOC -->


## Why CDN, not web storage / distributed cache? 
* Web storage or distributed cache could not necessarily be deployed as close as CDN to the end user. 
* Static resource such as video or images are so big. 
* If they were to serve from web storage / distributed cache, it will be 
  1. a huge requirement for network bandwidth
  2. high latency for such content

## Flowchart
### How to put an item on CDN

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│        1. A user has a video xxx.avi to put on CDN and wants to access it as        │
│                        https://video.yourcompany.com/xxx.avi                        │
│                                                                                     │
└──────────────────────────────────────────┬──────────────────────────────────────────┘
                                           │                                           
                                           ▼                                           
┌────────────────────────────────────────────────────────────────────────────────────┐ 
│                                                                                    │ 
│     2. CDN Provider provides a domain name for you such as xxx.akamai.cdn.com      │ 
│                                                                                    │ 
└───────────────────────────────────────────┬────────────────────────────────────────┘ 
                                            │                                          
                                            │                                          
                                            ▼                                          
 ┌────────────────────────────────────────────────────────────────────────────────────┐
 │ 3. a CName mapping between your preferred domain name and the one provided by CDN  │
 │              provider is configured on the .com DNS authority server               │
 │                                                                                    │
 │                    video.yourcompany.com => xxx.akamai.cdn.com                     │
 │                                                                                    │
 └──────────────────────────────────────────┬─────────────────────────────────────────┘
                                            │                                          
                                            │                                          
                                            ▼                                          
 ┌────────────────────────────────────────────────────────────────────────────────────┐
 │ 4. A CName mapping between cdn domain name returned from 3 and the CDN global load │
 │        balancer address is configured on the .cdn.com DNS authority server         │
 │                                                                                    │
 │                 xxx.akamai.cdn.com => global.loadbalancer.cdn.com                  │
 │                                                                                    │
 └────────────────────────────────────────────────────────────────────────────────────┘
                                           │                                           
                                           │                                           
                                           │                                           
                                           ▼                                           
  ┌─────────────────────────────────────────────────────────────────────────────────┐  
  │           5. The video is uploaded to CDN, which could be accessed by           │  
  │                      https://video.yourcompany.com/xxx.avi                      │  
  │                                                                                 │  
  └─────────────────────────────────────────────────────────────────────────────────┘  
```

### How to get an item from CDN

```
 ┌────────────────────────────────────────────────────────────────────────────────────┐
 │                                                                                    │
 │              1. A user accesses https://video.yourcompany.com/xxx.avi              │
 │                                                                                    │
 └─────────────────────────────────────────┬──────────────────────────────────────────┘
                                           │                                           
                                           │                                           
                                           │                                           
                                           ▼                                           
┌────────────────────────────────────────────────────────────────────────────────────┐ 
│          2. A request is sent to .com DNS authority server for resolving           │ 
│    video.yourcompany.com. Based on the CName mapping configured, a domain name     │ 
│                          xxx.akamai.cdn.com is returned.                           │ 
│                                                                                    │ 
│                    video.yourcompany.com => xxx.akamai.cdn.com                     │ 
│                                                                                    │ 
└────────────────────────────────────────────────────────────────────────────────────┘ 
                                           │                                           
                                           │                                           
                                           │                                           
                                           ▼                                           
┌────────────────────────────────────────────────────────────────────────────────────┐ 
│        3. A request is sent to .cdn.com DNS authority server for resolving         │ 
│      xxx.akamai.cdn.com. Based on the CName mapping configured, a domain name      │ 
│                    xxx.global.loadbalander.cdn.com is returned.                    │ 
│                                                                                    │ 
│               xxx.akamai.cdn.com => xxx.global.loadbalancer.cdn.com                │ 
│                                                                                    │ 
└────────────────────────────────────────────────────────────────────────────────────┘ 
                                           │                                           
                                           │                                           
                                           │                                           
                                           │                                           
                                           ▼                                           
┌────────────────────────────────────────────────────────────────────────────────────┐ 
│4. The CDN Global balancer xxx.global.loadbalancer.cdn.com returns an IP address    │ 
│based on the following conditions:                                                  │ 
│                                                                                    │ 
│a). User's IP address                                                               │ 
│b). User's network operator such as ATT                                             │ 
│c). Request url to determine which CDN server has it                                │ 
│d). Current traffic distribution condition                                          │ 
│                                                                                    │ 
└────────────────────────────────────────────────────────────────────────────────────┘ 
                                           │                                           
                                           │                                           
                                           │                                           
                                           ▼                                           
┌────────────────────────────────────────────────────────────────────────────────────┐ 
│                                                                                    │ 
│   5. A user accesses the resource by using https://{returned ip address}/xxx.avi   │ 
│                                                                                    │ 
└────────────────────────────────────────────────────────────────────────────────────┘ 
```

## Cache-Control headers
### Definition
* Request headers:

```
Cache-Control: max-age=<seconds>
Cache-Control: max-stale[=<seconds>]
Cache-Control: min-fresh=<seconds>
Cache-Control: no-cache 
Cache-Control: no-store
Cache-Control: no-transform
Cache-Control: only-if-cached
```

* Response headers

```
Cache-Control: must-revalidate
Cache-Control: no-cache
Cache-Control: no-store
Cache-Control: no-transform
Cache-Control: public
Cache-Control: private
Cache-Control: proxy-revalidate
Cache-Control: max-age=<seconds>
Cache-Control: s-maxage=<seconds>
```

* Extension headers

```
Cache-Control: immutable 
Cache-Control: stale-while-revalidate=<seconds>
Cache-Control: stale-if-error=<seconds>
```

### Flow chart
* [Flow chart](https://github.com/NeilMadden/cache-control-flowchart) for determining what Cache-Control header to use. 

![Cache-Control headers](./images/cacheControl-headers.png)

## Conditional Get headers

* If-Match: Succeeds if the ETag of the distant resource is equal to one listed in this header. By default, unless the etag is prefixed with 'W/', it performs a strong validation.
* If-None-Match: Succeeds if the ETag of the distant resource is different to each listed in this header. By default, unless the etag is prefixed with 'W/', it performs a strong validation.
* If-Modified-Since: Succeeds if the Last-Modified date of the distant resource is more recent than the one given in this header.
* If-Unmodified-Since: Succeeds if the Last-Modified date of the distant resource is older or the same than the one given in this header.
* If-Range: Similar to If-Match, or If-Unmodified-Since, but can have only one single etag, or one date. If it fails, the range request fails, and instead of a 206 Partial Content response, a 200 OK is sent with the complete resource.


![Resource changed](./images/conditionalGetResourceChanged.png)

![Resource unchanged](./images/conditionalGetResourceUnchanged.png)

## What could be put on CDN

## CDN internal
### Global server load balance - GSLB

### Cache proxy

#### Architecture
* There could be multiple layer of cache clusters
  - L1 cache cluster is also called edge node. 
  - There are more L1 cache cluster than L2 cache cluster. In addition, L1 cache cluster are usually closer to the end user.
* Components within each cache cluster
  - Level 4 load balancer:
    + Is faster than level 7 load balancer
    + Could only load balance on transport layer properties such as Source IP address, source port number, dest IP address, dest port number. These propperties necessarily guarantee a high cache hit ratio. 
  - Level 7 load balancer
    + Could load balance on HTTP layer properties such as Cookie, URL, method, parameter. 
  - Cache server

```
 ┌────────────────────────────────────────────────────────────────────────────────────────┐  
 │                                                                                        │  
 │                                    Source of truth                                     │  
 │                                                                                        │  
 └────────────────────────▲──────────────────────────────────────────┬────────────────────┘  
                          │                                          │                       
                          │                                          │                       
┌─────────────────────────┴──────────────────────────────────────────┼────────────────────┐  
│                                                                    ▼                    │  
│                               ...... Level N cache ......                               │  
│                                                                                         │  
└─────────────────────────▲──────────────────────────────────────────┬────────────────────┘  
                          │                                          │                       
                          │                                          │                       
                          │                                          │                       
 ┌───────────────────────────────────────────────────────────────────▼─────────────────────┐ 
 │                                      CDN L2 Cache                                       │ 
 │ ┌────────────┐   ┌────────────┐        ┌────────────┐     ┌────────────┐  ┌────────────┐│ 
 │ │            │   │            │        │            │     │LVS with VIP│  │LVS with VIP││ 
 │ │LVS with VIP│   │LVS with VIP│        │   ......   │     │  address   │  │  address   ││ 
 │ │address VIP3│   │address VIP4│        │            │     │   VIP10    │  │   VIP11    ││ 
 │ │            │   │            │        │            │     │            │  │            ││ 
 │ └────────────┘   └────────────┘        └────────────┘     └────────────┘  └────────────┘│ 
 │                         ▲                                        ▲                      │ 
 └─────────────────────────┼────────────────────────────────────────┼──────────────────────┘ 
                           │                                        │                        
                      Request 1                                     │                        
                           │                                   Request 2                     
                           │                                        │                        
                           │                                        │                        
                           │                                        │                        
  ┌─────────────────────────────────────────────────────────────────┴───────────────────────┐
  │ Usually two L1 CDN cache cluster is deployed within a single region for resiliency and  │
  │                                   performance purpose                                   │
  │                                                                                         │
  │┌────────────────────────────────┐                     ┌────────────────────────────────┐│
  ││           Cluster 1            │                     │           Cluster 2            ││
  ││                                │     .─────────.     │                                ││
  ││                                │ _.─'           `──. │                                ││
  ││  ┌──────┐  ┌──────┐  ┌──────┐  │╱Local cache server ╲│ ┌──────┐  ┌──────┐  ┌──────┐   ││
  ││  │Cache │  │Cache │  │Cache │  │   such as Nginx,    │ │Cache │  │Cache │  │Cache │   ││
  ││  │server│  │server│  │server│  │  Varnish, Traffic   │ │server│  │server│  │server│   ││
  ││  └──────┘  └──────┘  └──────┘  │╲      server       ╱│ └──────┘  └──────┘  └──────┘   ││
  ││      ▲         ▲         ▲     │ ╲                 ╱ │     ▲         ▲         ▲      ││
  ││      │         │         │     │  `──.         _.─'  │     │         │         │      ││
  ││      ├─────────┼─────────┤     │      `───────'      │     ├─────────┼─────────┤      ││
  ││      │         │         │     │                     │     │         │         │      ││
  ││      │         │         │     │                     │     │         │         │      ││
  ││      │         │         │     │    .───────────.    │     │         │         │      ││
  ││  ┌──────┐  ┌──────┐  ┌──────┐  │_.─'             `──.│ ┌──────┐  ┌──────┐  ┌──────┐   ││
  ││  │Nginx │  │Nginx │  │Nginx │  │  L7 load balancer   │ │Nginx │  │Nginx │  │Nginx │   ││
  ││  └──────┘  └──────┘  └──────┘  │   such as Nginx,    │ └──────┘  └──────┘  └──────┘   ││
  ││      ▲         ▲         ▲     │╲      HAProxy      ╱│     ▲         ▲         ▲      ││
  ││      │         │         │     │ `──.           _.─' │     │         │         │      ││
  ││      └─────────┼─────────┘     │     `─────────'     │     └─────────┼─────────┘      ││
  ││                │               │                     │               │                ││
  ││                │               │                     │               │                ││
  ││                │               │                     │               │                ││
  ││         ┌────────────┐         │   .───────────.     │        ┌────────────┐          ││
  ││         │LVS with    │         _.─'             `──. │        │LVS with    │          ││
  ││         │virtual IP  │        ;    Level 4 load     :│        │virtual IP  │          ││
  ││         │address VIP1│        :  balancer such as   ;│        │address VIP2│          ││
  ││         │            │         ╲     LVS / F5      ╱ │        │            │          ││
  ││         └────────────┘         │`──.           _.─'  │        └────────────┘          ││
  ││                                │    `─────────'      │                                ││
  │└────────────────────────────────┘                     └────────────────────────────────┘│
  │                 ▲                                                      ▲                │
  └─────────────────┼──────────────────────────────────────────────────────┼────────────────┘
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │            return  ┌──────────────┐       return     │                 
                  Request 1       VIP1   │CDN global    │        VIP2    Request 2           
                    │         ◀───────── │load balancer ├──────────────▶   │                 
                    │                    │              │                  │                 
                    │                    └──────────────┘                  │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
                    │                                                      │                 
```

## What could be cached on CDN?
* 