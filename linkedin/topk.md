# TopK

<!-- MarkdownTOC -->

- [Description](#description)
- [Features](#features)
- [Algorithms](#algorithms)
- [References](#references)

<!-- /MarkdownTOC -->


## Description
* Question1
问有很多host上面都跑着不同或者相同的service, 如果这些service有抛出exception都会被写到log里。请设计一个service能够返回过去24小时以内出现频率最高的K个exception。 

* Question2
On LinkedIn, connections of an user may have activities like changing a job, sharing an article, having new connection and so on. Was asked to create a metric so that you can rank those activities and display the top k, next top k activities to to a user. This is open ended questions. Discussion covered feature engineering, supervised learning, recommendation system and so on. Did not do well in this round.

* Question3 
LinkedIn有个share功能，在里面会出现一些URL，问题是找出在过去一天，一小时，五分钟被share的top 5/10/...多的URL。/ dashboard to monitor the top shared url in the last 5 minutes

算是两部分吧。当用户分享的时候，这里有一个client-server。当用户query top 5/10/...多的URL的时候，这里还有一个client-server。

先说第一部分。一开始说我们有10M users，我上过XX算法的系统设计，按照里面的方法，先算了QPS，面试官用自己的电脑给算的，大概150。没等我开口，面试官说要考虑Peak QPS哦，我说那就3X吧，450，他表示同意。我一看不是很高，感觉DB有很多选择，就先试试MySQL吧，还能跟面试官讨论一下。结果面试官说不用把这些数据存起来，第二天就不用了。感觉这是在给我提示。我说那就用in-memory key-value store吧，memcached，key是URL，value是count。说实话我不是很懂这些NoSQL的工具，没上手用过，这样瞎扯真是没底。DB算是先选好了。我接着说加个"URL Extractor"service吧，用户把请求发过来，这个service把URL找出来然后存到数据库里。他问如果想让用户得到快速的响应应该咋办。这里我也不太懂，他说你加个message queue，Kafka那种，类似producer-consumer。这个我是真不知道。然后就是如何把统计数据加起来了，这里也有点儿不太会。我的疑问是咋判断URL在过去的五分钟里出现过呢？他说你可以用好几个db。一开始真的没懂。他看我没懂，说第一个db存0~5min的，第二个db存6~10min的，等等，还有存过去一个小时的，一天的。我一开始还是没懂，比如说我想要3min~8min的咋办？一开始我没问，想了一会儿才问，他说不用太real time，忘跟你说了，不好意思。他说其实要做到3min~8min也行，你有60个db就好了，到时候一加就行了。

接下来就是咋找出来top5/10/...的URL了，窃以为要用heap啥的呢，面试官说memcached功能不太行啊，对value部分没啥操作啊，你换个别的吧。我一想那就Redis吧，听说那个比较厉害，有一些set操作啥的，直接找出top多的URL。面试官表示一拍即合，Redis应该是不错的选择。我说再加个service吧，用于处理这些请求。他说怎么处理这些请求呢？是直接从数据库里读数据还是咋办？我一想这是read-only啊，加个cache吧，另一个deamon定时去读数据库做计算，然后把算好的数据写到cache里，用户直接读cache就行了，反正也不用太real time。他表示同意，然后时间就到了。

* Question4
R4 data product design. On LinkedIn, connections of an user may have activities like changing a job, sharing an article, having new connection and so on. Was asked to create a metric so that you can rank those activities and display the top k, next top k activities to to a user. This is open ended questions. Discussion covered feature engineering, supervised learning, recommendation system and so on. Did not do well in this round.

* Question5
Sytem design, top 10 logs in last 60 min. Design the entire product, from how to get log data to how to show it in UI.


* Question6

如果是精确统计的话，定义一个长度为（24* 60 / 5 ＋ 1）的数组，每个元素对应一个 5min 的 CounterMap，记录 post -> count。

另外需要两个 Map，分别对应1小时和24小时的 rolling counter。

每次有数据来，对应的 5min CounterMap 做 inc，该分钟结束后，将这个统计值累加到 1hour 和 24hour 的counter map中，同时减去一小时和一天前对应的分钟级的 counter 值，完成后，转到下一个分钟，继续。

需要统计的时候遍历一遍对应的Counter Map即可。

参考：http://stackoverflow.com/a/10190836/404145

至于这种sliding window的效率，counter的 加减 操作都是O（1）的，统计最多分享的post的操作为O（N），足不足够高，可能还看整个的吞吐是多少了。


2015-11-15 Liu Mingmin
面筋翻到Google考了一个类似的题：

设计一个CPU和内存占用是Deterministic Behavior的Event Class，支持:

IncrementCount(), 
GetEventCountLastMin(), GetEventCountLastHour(), GetEventCountLastDay(),

每秒可以有多达million个call，也可能什么都没有。
其他的都好，这儿说明每秒多达million个call，是想说明什么不太清楚，就是说如果incrementCount 操作会很频繁么？需要解决什么问题？


2016-12-12 Q'c
要每秒多达million个call单台机器handle不了的吧，但这里只是设计class好像又不相关


2016-12-13 Jay Wang
应该是要考虑多线程同步的问题吧


2016-12-15 东邪黄药师
这个题是一个很好的面试题，因为可以从算法和系统两个角度进行考察。

从算法的角度分析
从算法的角度，可以简单的称之为 Top K Frequent Elements in Recent X mins.
算法的角度，本质就是设计一个数据结构，支持给某个key的count+1（有一个post被分享了），给某个key的count-1（有一个分享的计数已经过期了），然后查询Top k。

做法是维护一个有序序列（用链表来维护），每个链表的节点的key是 count，value是list of elements that has this count，也用linked list串起来。 比如 a出现2次，b出现3次，c出现2次，d出现1次。那么这个链表就是：

{3: [b]} --> {2: [a ->c]} --> {1: [d]}
然后另外还需要一个hashmap，key是element，value是这个element在链表上的具体位置。
因为每一次的操作都是 count + 1和 count - 1，那么每次你通过 hashmap 找到对应的element在数据结构中的位置，+1的话，就是往头移动一格，-1的话，就是往尾巴移动一格。总而言之复杂度都是 O(1)。当你需要找 Top K 的时候，也是 O(k)的时间 可以解决的。

这个数据结构来自 LFU 的论文：
http://dhruvbird.com/lfu.pdf
LintCode上有LFU的题， 可以做一下：
www.lintcode.com/problem/lfu-cache

从系统设计的角度分析
所以一般来说，你可能首先需要按照 LFU 的思路答出上述的方法。这个就过了第一关，算法关。但是还没结束，这个题还有第二关，那就是系统设计关。上面的算法从算法的角度没办法更优了，每个分享操作都是O(1)的代价，每个求Top K都是O(k)的代价。已经很棒了。但是系统的角度出发，会存在这样一些问题：

如果QPS比较高，比如 1m，这个数据结构因为要加锁才能处理，所以会很慢。
分享的数据本身是分布式的，而不是中心化的，也就是说，比如有1000台web服务器，那么这1000台web服务器的是最先获得哪个帖子被分享的数据的，但是这些数据又都分布在这1000台web服务器中，如果用一个中心化的节点来做这个数据结构的服务，那么很显然这个中心节点会成为瓶颈。
比如这个系统用在twitter 这样的服务中，根据长尾理论，有80%或者更多的帖子连 Top 20% 都排不进去。而通常来说，从产品的角度，我们可能只需要知道 Top 20 最多是 Top 100 的数据就可以了。整个系统浪费了很多时间去统计那些永远不会成为Top 100的数据。
题目的要求是“5分钟，1小时，24小时”，而不是“最近2分零30秒”，“最近31秒”，也存在较大的优化空间
真实产品实时性要求和准确性没有那么高。你需要查询最近5分钟的Top K，结果得出的是最近5分02秒的Top K在产品中是没有太大问题的。
查询Top k 的次数远低于count + 1和 count -1 的次数。
综上所述我们给出一些针对性优化策略：

分布式统计 Distributed: 每隔5~10秒向中心节点汇报数据

也就是说，哪些帖子被分享了多少次这些数据，首先在 web server 中进行一次缓存，也就是说web server的一个进程接收到一个分享的请求之后，比如 tweet_id=100 的tweet被分享了。那么他把这个数据先汇报给web server上跑着的 agent 进程，这个agent进程在机器刚启动的时候，就会一直运行着，他接受在台web server上跑着的若干个web 进程(process) 发过来的 count +1 请求。

这个agent整理好这些数据之后，每隔5~10秒汇报给中心节点。这样子通过5~10s的数据延迟，解决了中心节点访问频率过高的问题。这个设计的思路在业界是非常常用的（做这种数据统计服务的都是这么做的），我们在《系统设计班》的datadog一节的课中，就讲到过用这种思路来统计每一个event发生了多少次。

分阶段统计 Level

在《系统设计班》的 ratelimiter 一节课中，我们也提到了这种分阶段统计的思想。即如果我要去算最近5分钟的数据，我就按照1秒钟为一个bucket的单位，收集最近300个buckets里的数据。如果是统计最近1小时的数据，那么就以1分钟为单位，收集最近60个Buckets的数据，如果是最近1天，那么就以小时为单位，收集最近24小时的数据。那么也就是说，当来了一个某个帖子被分享了1次的数据的时候，这条数据被会分别存放在当前时间(以秒为单位），当前分钟，当前小时的三个buckets里，用于服务之后最近5分钟，最近1小时和最近24小时的数据统计。

你可能会疑惑，为什么要这么做呢？这么做有什么好处呢？这样做的好处是，比如你统计最近1小时的数据的时候，就可以随着时间的推移，每次增加当前分钟的所有数据的统计，然后扔掉一小时里最早的1分钟里的所有数据。这样子就不用真的一个一个的+1或者-1了，而是整体的 +X 和 -X。当然，这样做之后，前面的算法部分提出来的数据结构就不work了，但是可以结合下面提到的数据抽样的方法，来减小所有候选 key 的数目，然后用普通的 Top K 的算法来解决问题。

参考练习题：http://www.lintcode.com/en/problem/top-k-frequent-words/

数据抽样 Sample

可以进行一定程度的抽样，因为那些Top K 的post，一定是被分享了很多很多次的，所以可以进行抽样记录。
如果是5分钟以内的数据，就不抽样，全记录。如果是最近1小时，就可以按照比如 1/100 的概率进行 sample。
这个思想我们在Web Crawler 的那节课中提到过。

缓存 Cache

对于最近5分钟的结果，每隔5s才更新一次。
对于最近1小时的结果，每隔1分钟更新一次。
对于最近24小时的结果，每隔10分钟才更新一次。

用户需要看结果的时候，永远看的是 Cache 里的结果。另外用一个进程按照上面的更新频率去逐渐更新Cache。

以上的这些优化方法，基本都基于一个基本原则：在很多的系统设计问题中，不需要做到绝对精确和绝对实时。特别是这种统计类的问题。如果你刷算法题刷很多，很容易陷入设计一个绝对精确和绝对实时的系统的误区。一般来说面试中不会这么要求，如果这么要求了，那说明考你的是算法，算法才需要绝对准确和实时。

总的来说，这道题考了算法，大数据和系统设计。Top K 的算法我们在九章算法班中讲过，Top K的大数据算法我们在大数据班中讲过，系统设计中用到的各类思想基本也都在课上讲过。所以好好听课很重要。


2016-12-15 Q'c
感谢老师的答案 只是中心节点是不是一个single point of failure？要怎么解决比较好 也像DB一样搞一个slave node？或者隔一段时间checkpoint一下data到DB里？感觉这两个差不多


2016-12-16 东邪黄药师
1000台机器，每台机器每隔10秒钟汇报一次，那么这台机器的QPS是100。这样的QPS一般不会搞挂机器。
用slave或者double master之类的方式，我觉得在这种应用的情况下不是一个最优的选择。
要解决这个问题，我们必须明白两件事情：

1. 首先这个中心节点做什么？存数据么？
这个中心节点的任务是，接受来自1000台web server的统计数据，进行整理，并存储数据库中。
所以这台机器不是数据库。数据库是另外的专门的集群。这条机器只负责做一些整理工作，把一些有用的信息放到数据库和缓存里。方便你查询Top K 的时候用。他自己并不存储数据，只负责计算。

2. 中心节点挂了之后严重么？
不是那么严重。首先这个节点不直接服务普通用户，也就是说用户的request去的仍然是1000台web servers。这1000台web servers回去 cache 或者db里要数据，不会问这个中心节点要数据。所以他挂了，不会直接影响用户的感受。但是会间接影响数据的实时性和准确性。那么怎么解决这个问题呢？通常的做法是，用一个监控系统，监控这台机器是不是或者是不是正常工作，如果发现这台机器挂了，就发一个命令重启这台机器。通常来说更轻量级的做法是，在这台机器上有一个监控程序（比如supervisor），监控着主程序是不是正常执行中，如果挂了重启就行。这样已经足够用了。没有必要分摊成两台中心节点之类的，这样数据不统一在一台机器处理还会导致一些其他的维护麻烦的问题，得不偿失。


2016-12-16 Q'c
谢谢老师回复 
不过实际工作中有时候host有hardware failure是不能靠重启来解决的 所以是不是还是要准备一个standby 否者就得当场紧急build一个新host

double master 或者master slave 在什么情况下比较好用呢？

Top K计算因为有sampling是不是用heap就够了？


2016-12-22 彭珂
多谢黄老师的回复。
请问一下，既然这个题要求返回的是过去5分钟，1个小时，24个小时的top K, 那么我做以下两个假设可以么？
1. 这个中心节点只用三个Cache存储数据， 一个存储过去5分钟的（以一秒为一个bucket单位）， 一个存储过去一个小时的（以1分钟为一个bucket）， 最后一个存储过去24个小时的（以10分钟为一个bucket单位）的。用Cache的话可以提高读写速度。
2. 中心节点和local节点（1000个web server）均不需要将数据写入DB做持久化处理。 因为24小时之前的数据已经没有任何用处了。
请问这两个思想对么？
以及最后还有一个问题，如果这个题又一个follow up是，需要很频繁得调用这个Top K service（每半分钟需要看一次当下的Top K），这样的话还能怎么优化呢？ 该如何缓存上一次的结果呢？
谢谢


2016-12-22 Q'c
1.中心节点的作用就是统计并且persist结果到db 
2.QPS高无所谓啊 web server都是返回cached results

## Features

## Algorithms
* LFU cache

## References
* [Jiuzhang Q and A](http://www.jiuzhang.com/qa/219/)
