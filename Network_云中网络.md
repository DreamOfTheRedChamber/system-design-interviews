- [Network in the cloud](#network-in-the-cloud)
  - [Virtualization networkcard](#virtualization-networkcard)
    - [Flowchart](#flowchart)
    - [Share access](#share-access)
      - [Connect internally](#connect-internally)
      - [Connect to the outside](#connect-to-the-outside)
        - [Network bridging](#network-bridging)
        - [NAT](#nat)
    - [Separation access](#separation-access)
  - [Overlay network](#overlay-network)
    - [Motivation](#motivation)
    - [Definition](#definition)
    - [Approaches](#approaches)
      - [GRE - Generic Routing Encapsulation](#gre---generic-routing-encapsulation)
      - [VXLAN](#vxlan)
  - [SDN](#sdn)
    - [Use cases](#use-cases)
    - [OpenFlow and Openvswitch](#openflow-and-openvswitch)
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
* Def: Virtual machine and physical machine will share the same IP address. 

![](./images/virtualization_networkcard_share_bridging.png)

![](./images/virtualization_networkcard_share_bridging_flattened.png)

* Within cloud, the virtual machines inside Linux also share the same address as physical machines. 

![](./images/virtualization_networkcard_cloud.png)

![](./images/virtualization_networkcard_cloud_flattened.png)

* Cons: When there is a large scale of machines, broadcast will be a problem because each virtualized machine needs to be broadcasted. 

##### NAT
* Def: Virtual machines will have different ip address as physical machines. 

![](./images/virtualization_networkcard_nat.png)

* A DHCP server will be created for assigning ip addresses to virtual machines dynamically. 

![](./images/virtualization_networkcard_nat_dhcp.png)

### Separation access
* Approach: create VLAN based on physical network card eth0

> vconfig 

* Within the same machine, there is no connectivity between network bridges. 
* Across machines, as long as physical bridge support VLAN, there will be no connectivity between different VLANs. 

![](./images/virtualization_networkcard_separation.png)


## Overlay network
### Motivation
* Limitation of VLAN: Only has 12 bits and a capacity of 4096. 
* Possible solutions
  * Modify the VLAN protocol.
  * Extend the protocol by adding an additional header. 

### Definition
* Underlay network: Physical network
* Overlay network: Virtual network implemented on top of underlay network. 

### Approaches
#### GRE - Generic Routing Encapsulation
* Idea: It increases the number of VLAN ID by the way of tunnel. 

![](./images/virtualization_overlay_gre.png)

* Example

![](./images/virtualization_overlay_gre_example.png)

* Limitation:
  * The number of tunnels
  * It does not support group cast. 

#### VXLAN
* Idea:

![](./images/virtualization_overlay_VXLAN.png)


## SDN
### Use cases
* Control and forward

![](./images/virtualization_sdn.png)

### OpenFlow and Openvswitch
* SDN controller administrates the network by OpenFlow protocol. 

![](./images/virtualization_sdn_openflow.png)

* Within the Openvswitch, there is a flow table which defines flow rules. 
![](./images/virtualization_sdn_openflow_2.png)


## References
* [趣谈网络协议](https://time.geekbang.org/column/article/10742)