Monitor system

<!-- MarkdownTOC -->

- [Description](#description)

<!-- /MarkdownTOC -->


## Description
* Comment1
Design monitor system for thounsands of machines to get throughput, errors, logs....etc

3 个回复 


2015-08-16 张老师
最基本的解法，让这上千台机器向一个server汇报，这个server一个服务负责收集，然后写入本地。

之后，有另外一个服务跟进本地数据，统计汇报。

具体可以参考我们课上BigTable的log方式。


2015-08-16 Kevin Chen
这个monitor system是real time 还是允许延时?
如果是real time, 张老师的方案是正解. 还可以加上replication服务器, 以防主server挂掉.
如果允许延时, 从减少网络traffic考虑, 可以把logs 写在本地机上. 每隔一段时间(3 minutes?), 把saved的logs压缩打包, 收集到monitor service的服务器上再统计处理.

仅供参考.


2015-08-16 Levins Day
一般都需要local aggregation，否则数据量太大，server根本没法处理过来。

* design monitor system，比较麻烦，考虑了partition，replication，easy to use
