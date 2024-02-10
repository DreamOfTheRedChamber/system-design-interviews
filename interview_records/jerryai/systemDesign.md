
# 设计Short‍‍‍‍‌‌‌‍‍‍‌‍‍‌‍‍‌‍en URL 服务
会问具体怎么生成短链接，怎么储存，存什么，怎么扩展，怎么确保数据不丢失 等等。。。

# 为Youtube用户设计列出一小时之内top 10播放量的视频
就是top k问题，我还讲了count-min sketch‍‍‍‍‌‌‌‍‍‍‌‍‍‌‍‍‌‍来避免hash collision，面试官说不用考虑这个，就讲了high level的设计，以及一些小细节

# 通过用户信息去多家保险公司的服务器中拿quotes，这些公司都是异步处理请求的
这个题目比较明显的几个可以讨论的点 -
异步相关的问题：比如Fault tolerance，如何设计异步的UX（notification，email）
第三方的dependency：讨论SLA，有没有quota，qps的限制是多少
基本的处理high throughput的方法