# Amazon
## Description
* Comment1
Round 2: Design设计Amazon，他给我画了amazon buy的页面，问我怎么设计database来存信息，然后render page?我先说可以用SQL来存各个table，然后他又问如果数据很大怎么办？我说可以shard data存在各个slave上，靠master去query~然后OOD server interface~怎样读取各个table的信息~然后写了一个server class来用这些interface来render page~他问了如果很多客户同时读怎么办，我的解决方法有cache~他想让我说multi-threading，但是我一直避开，后来解释说我对这里的具体概念不熟，所以没有提~中间也有讨论怎样给suggest product，我提到可以建一个Product weighted graph, 然后用BFS

设计Amazon Product Page, 就是在SQL里面一个产品有多个图片多个价格的话怎么设计数据库。然后后台提取数值render到页面上得时候，class怎么设计，服务器怎么安排之类的, 中间也有讨论怎样给suggest product，我提到可以建一个Product weighted graph, 然后用BFS

* Comment2
Make it work
基本功能：用户注册，商品，购物车，支付。
说清楚每个功能怎么实现的，数据库表单存一些什么

Make It robust
当用户很多，同时去查看同一个商品的时候怎么办？（比如某款商品促销），考点，cache
当支付增多的时候怎么办？考点，异步任务(Async Tasks)
如何进行商品推荐？考点，Machine Learning（如果是对应职位才会问题）
如何实现商品搜索功能？考点，倒排索引(Inverted Index)

