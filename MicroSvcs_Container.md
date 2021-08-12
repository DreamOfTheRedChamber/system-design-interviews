- [MicroSvcs container](#microsvcs-container)
  - [Docker file system](#docker-file-system)
    - [Mount points](#mount-points)
    - [UnionFS](#unionfs)
      - [Motivation](#motivation)
      - [Implementation](#implementation)
      - [Limitations](#limitations)
  - [Docker storage](#docker-storage)
    - [Bind mounts (host path)](#bind-mounts-host-path)
      - [Use case](#use-case)
      - [Pros](#pros)
      - [Cons](#cons)
      - [Command](#command)
    - [Docker volumes](#docker-volumes)
      - [Pros](#pros-1)
      - [Command](#command-1)
    - [In-memory storage](#in-memory-storage)
      - [Use case](#use-case-1)
      - [Internal mechanism](#internal-mechanism)
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

#### Limitations
* For write, it uses Copy-On-Write mechanism, resulting in low efficiency; For read, it also needs to read from top down. 
* The RW layer has the same lifetime as the container. When container stops, the RW layer will disappear. If you need to store the RW layer, you could commit it to the image. 
* There is no mechanism for sharing the data. 

![](./images/container_overlay_constructs.jpeg)
 
## Docker storage

![](./images/container_differentStorageTypes.png)

| `Types` | `Pros` | `Cons` |
|--------------|--------------------|---|
| Bind mounts | Most straightforward/flexible.  | Must explicitly specify a file path on host  |
| Volumes | Cross disk/file system; Docker manage volumes, no need to worry about conflict;  | Data exist on host could not easily be shared to containers.  |
| tmpfs mounts  | High performant; Secure |  Could not share among multiple containers  |

### Bind mounts (host path)
#### Use case
* Bind mounts are useful when the host provides a file or directory that is needed by a program running in a container, or when that containerized program produces a file or log that is processed by users or programs running outside containers
* History: Exist since Linux 2.4 kernel 2001. 

#### Pros
* Much more performant than unionfilesystem. 
* Across file systems
* Across disks

#### Cons
* It ties otherwise portable container descriptions to the filesystem of a specific host.
* It creates an opportunity for conflict with other containers

![](./images/container_volume_comparison.png)

#### Command

```
// category 2: --mount format
docker run -d --name test1 --mount type=bind,src=/host/app,dst=/app
```

### Docker volumes
* Use case: Using volumes is a method of decoupling storage from specialized locations on the filesystem that you might specify with bind mounts.

#### Pros
* User does not need to remember a hardcoded hostpath. It only needs to use the correct volume name. 

#### Command

```
docker volume create/rm/ls/inspect/prune [-d local] volName

-v volumeName:containerPath
-v containerPath
--mount type=volume, src={volumeName}, dest={containerPath}
```

### In-memory storage
#### Use case
* Most service software and web applications use private key files, database passwords, API key files, or other sensitive configuration files, and need upload buffering space.
In these cases, it is important that you never include those types of files in an image or write them to disk. 

#### Internal mechanism
* Linux tmpfs: 

```
mkdir /my-tmp && mount -t tmpfs -o size=20m tmpfs /my-tmp
```

* tmpfs volume

```
docker run -d --name tmptest --mount type=tmpfs, dst=/app, tmpfs-size=10k, busybox:1.24
```

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