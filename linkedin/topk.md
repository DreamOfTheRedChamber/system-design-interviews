# TopK

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
