- [Network in the cloud](#network-in-the-cloud)
  - [Virtualization networkcard](#virtualization-networkcard)
    - [Flowchart](#flowchart)
    - [Share access](#share-access)
      - [Connect internally](#connect-internally)
      - [Connect to the outside](#connect-to-the-outside)
        - [Network bridging](#network-bridging)
        - [NAT](#nat)
  - [References](#references)

# Network in the cloud
## Virtualization networkcard
### Flowchart
* The steps are shown in the graph below:
  1. Virtual machine could open a char dev file called TUN/TAP.
  2. After opening this file, a virtual network card driver will be seen on the physical machine. 
  3. This virtual network card will intercept network packets and send it through TCP/IP. 
  4. The virtual network card tap0 will send network packets. 

![](./images/virtualization_networkcard.png)

### Share access
#### Connect internally
1. Use the Linux command to create an ethernet bridge 

> brctl addbr br0

2. Connect two virtual network card to br0

![](./images/virtualization_networkcard_share.png)

#### Connect to the outside
##### Network bridging
* 

![](./images/virtualization_networkcard_share_bridging.png)

![](./images/virtualization_networkcard_share_bridging_flattened.png)

##### NAT


## References
* [趣谈网络协议](https://time.geekbang.org/column/article/10742)