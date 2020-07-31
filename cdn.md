<!-- MarkdownTOC -->

- [Why CDN, not web storage / distributed cache?](#why-cdn-not-web-storage--distributed-cache)
- [How to put an item on CDN](#how-to-put-an-item-on-cdn)

<!-- /MarkdownTOC -->


## Why CDN, not web storage / distributed cache? 
* Web storage or distributed cache could not necessarily be deployed as close as CDN to the end user. 
* Static resource such as video or images are so big. 
* If they were to serve from web storage / distributed cache, it will be 
  1. a huge requirement for network bandwidth
  2. high latency for such content

## How to put an item on CDN

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│                     1. You have a video hello.avi to put on CDN                     │
│                                                                                     │
└──────────────────────────────────────────┬──────────────────────────────────────────┘
                                           │                                           
                                           ▼                                           
┌────────────────────────────────────────────────────────────────────────────────────┐ 
│                                                                                    │ 
│     2. CDN Provider provides a domain name for you such as aaa.cdn.akamai.com      │ 
│                                                                                    │ 
└───────────────────────────────────────────┬────────────────────────────────────────┘ 
                                            │                                          
                                            │                                          
                                            ▼                                          
 ┌────────────────────────────────────────────────────────────────────────────────────┐
 │                                                                                    │
 │                                                                                    │
 │  3. You configure a CName mapping between your preferred domain name and the one   │
 │                              provided by CDN provider                              │
 │                                                                                    │
 │                    video.yourcompany.com => aaa.cdn.aka.mai.com                    │
 │                                                                                    │
 │                                                                                    │
 └────────────────────────────────────────────────────────────────────────────────────┘
                                            │                                          
                                            │                                          
                                            ▼                                          
 ┌────────────────────────────────────────────────────────────────────────────────────┐
 │             4. You upload the video to CDN, which could be accessed by             │
 │                      https://video.yourcompany.com/hello.avi                       │
 │                                                                                    │
 └────────────────────────────────────────────────────────────────────────────────────┘
```