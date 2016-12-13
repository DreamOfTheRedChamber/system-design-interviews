# Amazon
## Description
Round 2: Design设计Amazon，他给我画了amazon buy的页面，问我怎么设计database来存信息，然后render page?我先说可以用SQL来存各个table，然后他又问如果数据很大怎么办？我说可以shard data存在各个slave上，靠master去query~然后OOD server interface~怎样读取各个table的信息~然后写了一个server class来用这些interface来render page~他问了如果很多客户同时读怎么办，我的解决方法有cache~他想让我说multi-threading，但是我一直避开，后来解释说我对这里的具体概念不熟，所以没有提~中间也有讨论怎样给suggest product，我提到可以建一个Product weighted graph, 然后用BFS

设计Amazon Product Page, 就是在SQL里面一个产品有多个图片多个价格的话怎么设计数据库。然后后台提取数值render到页面上得时候，class怎么设计，服务器怎么安排之类的, 中间也有讨论怎样给suggest product，我提到可以建一个Product weighted graph, 然后用BFS