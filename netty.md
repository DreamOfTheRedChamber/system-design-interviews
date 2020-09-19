
<!-- MarkdownTOC -->

- [Netty](#netty)
	- [Architecture](#architecture)
	- [Use cases](#use-cases)
	- [IO Model](#io-model)
		- [Synchronous vs asynchronous](#synchronous-vs-asynchronous)
		- [Blocking vs non-blocking](#blocking-vs-non-blocking)
			- [Synchronous blocking](#synchronous-blocking)
			- [Synchronous nonblocking](#synchronous-nonblocking)
			- [Asynchronous blocking](#asynchronous-blocking)
			- [Asynchronous nonblocking](#asynchronous-nonblocking)
		- [BIO](#bio)
		- [NIO](#nio)
		- [AIO](#aio)
	- [Proactor model](#proactor-model)
	- [Netty's supported IO modes](#nettys-supported-io-modes)
	- [Netty's NIO implementation](#nettys-nio-implementation)
	- [How Netty support three Reactor models](#how-netty-support-three-reactor-models)
	- [Netty thread model](#netty-thread-model)
		- [Components](#components)
	- [Netty lock](#netty-lock)
	- [Encoding](#encoding)
	- [TCP 拆包粘包](#tcp-%E6%8B%86%E5%8C%85%E7%B2%98%E5%8C%85)
	- [Heartbeat mechanism](#heartbeat-mechanism)
	- [Zero copy](#zero-copy)
	- [Memory](#memory)
		- [Direct memory](#direct-memory)
	- [Serialization](#serialization)
		- [Marshalling](#marshalling)
		- [Protobuf](#protobuf)
	- [High performance](#high-performance)
		- [Reactor mode](#reactor-mode)
		- [NIO multi IO non-blocking](#nio-multi-io-non-blocking)
		- [ByteBuf memory buffer design](#bytebuf-memory-buffer-design)
	- [Protocol](#protocol)
		- [自定义协议栈 protocol](#%E8%87%AA%E5%AE%9A%E4%B9%89%E5%8D%8F%E8%AE%AE%E6%A0%88-protocol)
		- [Netty HTTP protocol](#netty-http-protocol)
			- [Netty Http application on RxNetty Http](#netty-http-application-on-rxnetty-http)
	- [实战项目](#%E5%AE%9E%E6%88%98%E9%A1%B9%E7%9B%AE)
		- [Idle detection](#idle-detection)
		- [Chat room](#chat-room)
		- [数据可靠性通信场景与架构设计](#%E6%95%B0%E6%8D%AE%E5%8F%AF%E9%9D%A0%E6%80%A7%E9%80%9A%E4%BF%A1%E5%9C%BA%E6%99%AF%E4%B8%8E%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1)
			- [数据结构](#%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
			- [Server端架构](#server%E7%AB%AF%E6%9E%B6%E6%9E%84)
			- [Client端架构](#client%E7%AB%AF%E6%9E%B6%E6%9E%84)
			- [Netty 负载均衡与高可用](#netty-%E8%B4%9F%E8%BD%BD%E5%9D%87%E8%A1%A1%E4%B8%8E%E9%AB%98%E5%8F%AF%E7%94%A8)
			- [Netty 异步化数据处理](#netty-%E5%BC%82%E6%AD%A5%E5%8C%96%E6%95%B0%E6%8D%AE%E5%A4%84%E7%90%86)
			- [Netty linux性能调优](#netty-linux%E6%80%A7%E8%83%BD%E8%B0%83%E4%BC%98)
		- [RPC communication](#rpc-communication)
			- [Application in Dubbo](#application-in-dubbo)
		- [WebSocket](#websocket)
		- [Heartbeat mechanism](#heartbeat-mechanism-1)
		- [弹幕系统](#%E5%BC%B9%E5%B9%95%E7%B3%BB%E7%BB%9F)
	- [Toy project](#toy-project)
	- [Netty source code](#netty-source-code)

<!-- /MarkdownTOC -->


# Netty
## Architecture

![Job state flow](./images/netty_architecture.png)

## Use cases
* Netty related projects: https://netty.io/wiki/related-projects.html
	* Distributed system: Dubbo/RocketMQ
	* Game industry: 
	* Hadoop and Avro RPS

## IO Model
### Synchronous vs asynchronous
* After IO calling thread invokes IO execution thread, whether IO calling thread could continue executing unrelevant logic related to IO execution thread
	- If could continue executing unrelevant logic, then asynchronous
	- If could't continue executing unrelevant logic, then synchronous

### Blocking vs non-blocking
* After IO calling thread invokes IO execution thread, whether the IO calling thread gets proactively waiting or passively waiting. 
	- If proactively waiting (e.g. use a while block to constantly check), then it is blocking
	- If reactively waiting (e.g. Register an event and fall into sleep)

#### Synchronous blocking

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                               ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     User Space                                                    Kernel Space                
│                   │                               │                                         │
  ┌──────────────┐                                     ┌─────────┐                ┌─────────┐  
│ │IO thread (one│  │                               │  │ System  │                │ Network │ │
  │   for both   │                                     │ kernel  │                │  card   │  
│ │ calling and  │  │                               │  │         │                │ buffer  │ │
  │  execution)  │                                     └─────────┘                └─────────┘  
│ └──────────────┘  │                               │       │                          │      │
          │                                                                                    
│         │         │                               │       │                          │      │
          │                   Step1.a. System                                                  
│         ├─────────┼────────call to instruct ──────┼───────▶                          │      │
          │                     the kernel                                                     
│         │         │                               │       │         Step2.           │      │
   Step1.b. Thread                                          ────────Read data ─────────▶       
│ actively checking │                               │       │        request           │      │
 whether user buffer                                                                           
│     is ready      │                               │       │                          │      │
          │                                                                                    
│         ▼         │                               │       │                          │      │
     ┌─────────┐                                       ┌─────────┐                             
│    │  User   │    │      Step4. Copy data         │  │ Kernel  │       Step3. Copy   │      │
     │ buffer  │◀────────────to user space ────────────│  space  │◀────────data to ────        
│    └─────────┘    │           buffer              │  └────┬────┘      system kernel  │      │
          │                                                                                    
│         │         │                               │       │                          │      │
       Step5.                                                                                  
│     Continue      │                               │       │                          │      │
     execution                                                                                 
│         │         │                               │       │                          │      │
          │                                                                                    
│         │         │                               │       │                          │      │
          ▼                                                                                    
│                   │                               │                                         │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                                 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                                                                                                                                             
```

#### Synchronous nonblocking

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                               ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     User Space                                                    Kernel Space                
│                   │                               │                                         │
  ┌──────────────┐                                     ┌─────────┐                ┌─────────┐  
│ │IO thread (one│  │                               │  │ System  │                │ Network │ │
  │   for both   │                                     │ kernel  │                │  card   │  
│ │ calling and  │  │                               │  │         │                │ buffer  │ │
  │  execution)  │                                     └─────────┘                └─────────┘  
│ └──────────────┘  │                               │       │                          │      │
          │                                                                                    
│         │         │                               │       │                          │      │
          │                   Step1.a. System                                                  
│         ├─────────┼────────call to instruct ──────┼───────▶                          │      │
          │                     the kernel                                                     
│         │         │                               │       │         Step2.           │      │
   Step1.b. Thread                                          ────────Read data ─────────▶       
│ actively checking │                               │       │        request           │      │
 whether user buffer                                                                           
│     is ready      │                               │       │                          │      │
          │                                                                                    
│         ▼         │                               │       │                          │      │
     ┌─────────┐                                       ┌─────────┐                             
│    │  User   │    │      Step4. Copy data         │  │ Kernel  │       Step3. Copy   │      │
     │ buffer  │◀────────────to user space ────────────│  space  │◀────────data to ────        
│    └─────────┘    │           buffer              │  └────┬────┘      system kernel  │      │
          │                                                                                    
│         │         │                               │       │                          │      │
       Step5.                                                                                  
│     Continue      │                               │       │                          │      │
     execution                                                                                 
│         │         │                               │       │                          │      │
          │                                                                                    
│         │         │                               │       │                          │      │
          ▼                                                                                    
│                   │                               │                                         │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                                 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
```

#### Asynchronous blocking

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                   ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
               User Space                                                  Kernel Space                
│                                       │                   │                                         │
  ┌─────────┐              ┌─────────┐                           ┌─────────┐              ┌─────────┐  
│ │   IO    │              │   IO    │  │                   │    │ System  │              │ Network │ │
  │ calling │              │execution│                           │ kernel  │              │  card   │  
│ │ thread  │              │ thread  │  │                   │    │         │              │ buffer  │ │
  └─────────┘              └─────────┘                           └─────────┘              └─────────┘  
│      │                        │       │                   │         │                        │      │
       │         Step1.a                                                                               
│      │         Invoke         │       │                   │         │                        │      │
       ├────────execution ──────▶                                                                      
│      │         thread         │       │                   │         │                        │      │
       │                                 Step2. system call                                            
│      │                        ├───────┼──to instruct the ─┼─────────▶                        │      │
    Step1.b. Wait for the                      kernel                                                  
│  finish of IO execution       │       │                   │         │        Step3.          │      │
       │   thread                                                     ───────read data ────────▶       
│      │                        │       │                   │         │       request          │      │
       │                                                                                               
│      │                        │       │                   │         │                        │      │
       │                    ┌─────────┐                          ┌─────────┐                           
│      ▼       Step6.       │  User   │ │  Step5. Copy data │    │ Kernel  │      Step4.       ┘      │
       ◀─────Return to ─────│ buffer  │◀─────to user space ──────│  space  │◀───Copy data ────┤        
│      │      calling       └───┬─────┘ │       buffer      │    └────┬────┘    to system             │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       Step7. Continue                                                                        │        
│    executing relevant         │       │                   │         │                               │
       business logic                                                                         │        
│      │                        │       │                   │         │                               │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       ▼                                                                                               
│                                       │                   │                                         │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                     ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
```

#### Asynchronous nonblocking

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                   ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
               User Space                                                  Kernel Space                
│                                       │                   │                                         │
  ┌─────────┐              ┌─────────┐                           ┌─────────┐              ┌─────────┐  
│ │   IO    │              │   IO    │  │                   │    │ System  │              │ Network │ │
  │ calling │              │execution│                           │ kernel  │              │  card   │  
│ │ thread  │              │ thread  │  │                   │    │         │              │ buffer  │ │
  └─────────┘              └─────────┘                           └─────────┘              └─────────┘  
│      │                        │       │                   │         │                        │      │
       │         Step1.a                                                                               
│      │         Invoke         │       │                   │         │                        │      │
       ├────────execution ──────▶                                                                      
│      │         thread         │       │                   │         │                        │      │
       │                                 Step2. system call                                            
│      │                        ├───────┼──to instruct the ─┼─────────▶                        │      │
      Step1.b. Continue                        kernel                                                  
│   executing unrelevant        │       │                   │         │        Step3.          │      │
       business logic                                                 ───────read data ────────▶       
│      │                        │       │                   │         │       request          │      │
       │                                                                                               
│      │                        │       │                   │         │                        │      │
       │                    ┌─────────┐                          ┌─────────┐                           
│      ▼       Step6.       │  User   │ │  Step5. Copy data │    │ Kernel  │      Step4.       ┘      │
       ◀─────Return to ─────│ buffer  │◀─────to user space ──────│  space  │◀───Copy data ────┤        
│      │      calling       └───┬─────┘ │       buffer      │    └────┬────┘    to system             │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       Step7. Continue                                                                        │        
│    executing relevant         │       │                   │         │                               │
       business logic                                                                         │        
│      │                        │       │                   │         │                               │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       │                                                                                      │        
│      │                        │       │                   │         │                               │
       ▼                                                                                               
│                                       │                   │                                         │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                     ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
```


### BIO
### NIO
### AIO

## Proactor model
* Reactor synchronous non-blocking IO model
* Proactor asynchronous non-blocking IO model
* IO 


## Netty's supported IO modes

![Job state flow](./images/netty_supportForIOs.png)

## Netty's NIO implementation

![Job state flow](./images/netty_multiINIOImplem.png)

## How Netty support three Reactor models

## Netty thread model

![Job state flow](./images/netty_threadmodel.png)

### Components
* Bootstrap/ServerBootstrap
* Future/ChannelFuture
* Channel
* Selector
* NioEventLoop
* NioEventLoopGroup

* ByteBuf

## Netty lock

## Encoding
* Encoding and decoding
	* ChannelHandler
	* ChannelHandlerContext
	* ChannelPipeline

![Job state flow](./images/netty_encoding.png)


## TCP 拆包粘包

![Job state flow](./images/netty_tcp_stickOrBreak.png)

## Heartbeat mechanism

## Zero copy

## Memory
### Direct memory

![Job state flow](./images/netty_directmemory.png)

## Serialization
### Marshalling
### Protobuf

## High performance
### Reactor mode
### NIO multi IO non-blocking
### ByteBuf memory buffer design

## Protocol
### 自定义协议栈 protocol
### Netty HTTP protocol
#### Netty Http application on RxNetty Http

## 实战项目

### Idle detection

### Chat room

### 数据可靠性通信场景与架构设计
#### 数据结构
#### Server端架构
#### Client端架构
#### Netty 负载均衡与高可用
#### Netty 异步化数据处理
#### Netty linux性能调优

### RPC communication

#### Application in Dubbo

### WebSocket

### Heartbeat mechanism

### 弹幕系统

## Toy project
* 极客时间

## Netty source code