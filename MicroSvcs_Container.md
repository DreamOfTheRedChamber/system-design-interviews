- [MicroSvcs container](#microsvcs-container)
  - [Docker file system](#docker-file-system)
    - [Mount points](#mount-points)
    - [UnionFS](#unionfs)
      - [Motivation](#motivation)
      - [Implementation](#implementation)
    - [Overlay2](#overlay2)
  - [Docker storage](#docker-storage)
    - [Bind mounts](#bind-mounts)
    - [In-memory storage](#in-memory-storage)
    - [Docker volumes](#docker-volumes)
  - [Container network](#container-network)
  - [References](#references)
  - [Real world](#real-world)

# MicroSvcs container

## Docker file system
### Mount points
* Def: Unix file system is organized into a tree structure. Storage devices are attached to specific locations in that tree. These locations are called mount points.
* A mount point contains three parts:
  * The location in the tree
  * The access properties to the data at that point (for example, writability)
  * The source of the data mounted at that point (for example, a specific hard disk, USB device, or memory-backed virtual disk)

![](./images/container_mountpoint.png)

### UnionFS
#### Motivation
* If without a container specific file system, then file systems such as XFS or ext4 need to be used. For these file systems, the entire system needs to be downloaded to each container, resulting in much redundancy. 

#### Implementation
* UnionFS has many implementations, including Docker's AUFS and OverlayFS. Since Linux 3.18, OverlayFS has been part of Linux and default container file system impl. 
* OverlayFS is a modern union filesystem that is similar to AUFS, but faster and with a simpler implementation.
  * Lower layer is readonly.
  * Upper layer is writable and modifiable. 

![](./images/container_overlay_constructs.png)

### Overlay2


## Docker storage

![](./images/container_differentStorageTypes.png)

### Bind mounts
* Use case: Bind mounts are useful when the host provides a file or directory that is needed by a program running in a container, or when that containerized program produces a file or log that is processed by users or programs running outside containers
* Cons:
  * The first problem with bind mounts is that they tie otherwise portable container descriptions to the filesystem of a specific host.
  * The next big problem is that they create an opportunity for conflict with other containers

![](./images/container_bindmounts_example.png)

### In-memory storage
* Use case: Most service software and web applications use private key files, database passwords, API key files, or other sensitive configuration files, and need upload buffering space.
In these cases, it is important that you never include those types of files in an image or write them to disk. 

### Docker volumes
* Use case: 
  * Docker volumes are named filesystem trees managed by Docker. They can be implemented with disk storage on the host filesystem, or another more exotic backend such as cloud storage. All operations on Docker volumes can be accomplished using the docker volume subcommand set. Using volumes is a method of decoupling storage from specialized locations on the filesystem that you might specify with bind mounts.
* Cons: 

## Container network

## References
* [container and CICD](https://time.geekbang.org/course/detail/100003901-2279)
* [Why container and Docker](https://time.geekbang.org/column/article/41977)
* [Container management with Mesos](https://time.geekbang.org/course/detail/100003901-2280)
* [Docker image repo and deployment](https://time.geekbang.org/column/article/42167)
* [Docker orchestration](https://time.geekbang.org/column/article/42477)
* [Container DevOps platform](https://time.geekbang.org/column/article/42604)
* [CI, CD](https://time.geekbang.org/column/article/42825)

## Real world
* Netflix container journey: https://netflixtechblog.com/the-evolution-of-container-usage-at-netflix-3abfc096781b