
<!-- MarkdownTOC -->

- [02/17](#0217)
	- [Service owner](#service-owner)
	- [System design](#system-design)
		- [逻辑性](#%E9%80%BB%E8%BE%91%E6%80%A7)
		- [Assumption](#assumption)
		- [面试官的expectation](#%E9%9D%A2%E8%AF%95%E5%AE%98%E7%9A%84expectation)
		- [讲自己擅长的一些东西](#%E8%AE%B2%E8%87%AA%E5%B7%B1%E6%93%85%E9%95%BF%E7%9A%84%E4%B8%80%E4%BA%9B%E4%B8%9C%E8%A5%BF)
		- [自己做过的项目](#%E8%87%AA%E5%B7%B1%E5%81%9A%E8%BF%87%E7%9A%84%E9%A1%B9%E7%9B%AE)
- [03/02](#0302)
	- [Self introduction](#self-introduction)
	- [Design Uber eats](#design-uber-eats)
	- [Architecture design](#architecture-design)
	- [Dive into detail](#dive-into-detail)
	- [疏忽的地方](#%E7%96%8F%E5%BF%BD%E7%9A%84%E5%9C%B0%E6%96%B9)
	- [可扩展性](#%E5%8F%AF%E6%89%A9%E5%B1%95%E6%80%A7)

<!-- /MarkdownTOC -->

## 02/17
### Service owner
* 对于自己service的认知还缺很多
	- 知道service的bottleneck
	- 知道service的scale up/down方式
	- 知道service的metric
* 又做development又做maintainence
	- Estimation怎么做，自己选什么样的machine
	- 为什么选8个cores, 而不是6个cores
	- 别人把这个requirement定义好了，对于service本身考虑
* License based, 又是从AAD拿的
	- 自己的cache
	- 自己的cache怎么和AAD做synchronization
* 怎么样represent自己作为service owner

### System design
#### 逻辑性
* 想问题的方式，customer需要什么
* 一定要从customer入手，tiny url需要什么，长什么样
* Customer看到五个character, 可以怎么来

#### Assumption
* Teams - 非常modern的application
* read heavy, write less
* Relation database支持的QPS
	- 50K, 普通的hardware肯定是不行的
	- 愿意用这么costy的hardware来support
* Make了一些decision
	- 设计系统需要什么
	- 考察面试者有没有经过自己认真的思考
	- 有没有好的reason去支持他的决定
	- 要么你改了，要么自己不确定自己是否是正确的
	- Take feedback
		+ 是否跟自己的想法，要做的是否是一致的
		+ 了解面试官给自己feedback的原因，两个人想法有没有在一条线上
			* 从Slave那里去读的
			* 没有去想自己怎么样去回答
		+ Balance为什么好，是否所有的都需要balance

#### 面试官的expectation
* Level +1 or -1
	- 如果很懂很懂，会调节面试
	- 如果只是知道一些concept，但是没法灵活运用
* 先要figure out出requirement
* Clarify assumption
* 做些basic estimation
* 做个high level design, 他们之间怎么talk to
	- 讲两三个detailed component:
		+ API design
		+ DB sharding
* Scale
* Disater recovery

#### 讲自己擅长的一些东西
* 擅长API design
	- naming
* 擅长Decouple
* 那个很表面
	- 做system design的时候他们不会和你说非常detail的东西
	- 把这些东西都往一个上面套的时候

#### 自己做过的项目
* 哪些可以换个方式做，都走的是short-term
* 怎么样把short-term变成long-term
	- 这样就会有很多的想法
* 去找senior和principal
	- 怎样让system考虑得更多，他们提到一些词或者想法
	- 很少有人
* 知道很多edge case的东西是别人不知道的
	- 面试官会很surprise的，
* metrics不会无缘无故的
	- 既然fire了能不能dive deep一点
	- 有些时候host, metrics
	- 各式各样的问题，整个analysis的过程会让自己对system更加了解
		+ 要用怎么样的load balancer
		+ build shape
	- flaky的问题
	- service不是我own的时候需要去刨根接地



## 03/02
### Self introduction
* 准备一些常见的问题：biggest challenge
	- 一开始还是比较high level, 能给一些具体的example
	- 和其他人怎么合作
	- 你和其他人意见不一致

### Design Uber eats
* Clarify更清楚
	- 需不需要ranking
	- 怎么样ranking
	- 这一页显示些什么东西
	- Feeds上有个照片或者meta data
	- Open hour
	- Menu

### Architecture design
* 可以做得更好是强调solution的trade off, 用MySQL和NoSQL的database用哪个更好
* 区分Senior和Level4/5, 知道哪个会更好些

### Dive into detail
* geo-index: 如果没有了解过Geo Index
* Elastic Search里面已经有了，用Tree的模式，是一个BiTree
* 多刷题，多看题，GeoIndex比较经典的面试问题
* NonSQL database: 
	- 更容易改变Schema, 
* 熟悉哪些database, Cassandra

### 疏忽的地方
* 下一步应该怎么做，直接return给user
* 哪些restaurant是直接可以送到user这里的，
* Filtering
	- Query时就可以做Filter
	- 有一个阶段去fetch restaurant data
* Ranking
	- 即使是Product software engineer
	- 用什么样的machine learning model

### 可扩展性
* 做estimation
* 更重要的是sense of scalability
	- 具体的solution应该怎么样做 10K QPS或者1000K QPS
	- 最简单的sharding方式
		+ 根据city来sharding
		+ 一个区来sharding
* 从Web到CDN
* Server之前
* Service data base: 
* 画个图，到哪几层















