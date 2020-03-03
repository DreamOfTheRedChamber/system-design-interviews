
<!-- MarkdownTOC -->

- [02/17](#0217)
	- [Service owner](#service-owner)
	- [System design](#system-design)
		- [逻辑性](#%E9%80%BB%E8%BE%91%E6%80%A7)
		- [Assumption](#assumption)
		- [面试官的expectation](#%E9%9D%A2%E8%AF%95%E5%AE%98%E7%9A%84expectation)
		- [讲自己擅长的一些东西](#%E8%AE%B2%E8%87%AA%E5%B7%B1%E6%93%85%E9%95%BF%E7%9A%84%E4%B8%80%E4%BA%9B%E4%B8%9C%E8%A5%BF)
		- [自己做过的项目](#%E8%87%AA%E5%B7%B1%E5%81%9A%E8%BF%87%E7%9A%84%E9%A1%B9%E7%9B%AE)

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



















