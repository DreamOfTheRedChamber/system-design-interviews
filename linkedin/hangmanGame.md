# Design hangman game

<!-- MarkdownTOC -->

- [Past comments](#past-comments)
- [Scenario](#scenario)
	- [Features](#features)
		- [Serialize](#serialize)
		- [Deserialize](#deserialize)
		- [MySQL Databases](#mysql-databases)
		- [Select a random row from database](#select-a-random-row-from-database)
		- [Multiplayer vs single player](#multiplayer-vs-single-player)
		- [Pick category and difficulty for words](#pick-category-and-difficulty-for-words)
			- [Databases](#databases)
		- [Achievement history](#achievement-history)
	- [Common features](#common-features)
		- [User system](#user-system)
- [Service](#service)
	- [](#)

<!-- /MarkdownTOC -->


## Past comments
* Comment1
need to clarify one question first: what does the distributed hangman game mean or which part should be distributed?

there are 2 types: stateless || stateful server
stateless server might be the best way to solve the problem, since the game is very simple, nothing about security, and with a good encoding, client side can do all the caching of the game process with very cheap network communication. This makes the server very scalable.(in another sense, the server is just a skeleton to implement the logic. the game is just like running on users' machine and doing the calculation on server)

stateful: this one is a little bit tricky. The server side may need to record users session obtained by users' cookie or the user ID.
Considering millions of users playing at the same time, we can store the user session in a distributed database or file system like Chord File system or Google file system. using SHA-1 hash to distribute the user session, and do the calculation on that machine. To do the load balancing, we can use open source software like Haproxy as master node to dispatch the calculation.

To implement some features like leader border: we might need to use a master node to implement a B+ tree or other better data structure (fix sized priority queue, if only first K records are needed)

* Comment2
Assume we are designing just this game and none other, otherwise we would have a generic Game class and a Hangman class would implement the IGame interface.

Also lets design such that Hangman can be played both with and without a hint.

Required classes and their purpose
Hangman - main class that plays the game. It will have both the startgame and startgamewith hint option.
WordHintTable - class that keeps track of words and their hints. Most likely a dictionary of some sort
IWordHint - interface that implements the WordHintTable class method signatures
IDraw - an interface that is responsible for drawing different objects
AlphabetBoard - responsible for the letters chosen by the player to guess the word. Also implements IDraw since it would handle drawing of the letters and alphabet board differently.
Word - the actual word, its state, current pointer and its storage during the game. Implements IWordHint and IDraw

* Comment3
一开始什么要求都没有 就问我知不知道猜词游戏 解释了一下 说让设计一个web上玩的
于是一开始我就写了一个hangman的class 这个网上一搜有很多
然后面试官问这个code放在哪里 我说用户开始游戏 就把code下载到本地 鏉ユ簮涓€浜�.涓夊垎鍦拌鍧�. 
每猜对或者猜错一个字母 都在本地更新. from: 1point3acres.com/bbs 
只有这轮游戏输了才发request到server更新用户的score
然后面试官问那这样如果用户想作弊 在前台就能看到是什么词了 
我说是的 那看你想让这游戏多serious了 如果就是给用户玩着玩的 那他想作弊就作吧. more info on 1point3acres.com
面试官说那假设挺serious的呢
我说那就只能存server上了 用户猜一个发一个request 面试官表示可以
然后就讨论了一下database怎么设计 我就说一个user table一个word table
然后选了sql database
然后面试官问假设500m+用户了
我给他算了算qps 发现不是很大 就说那现在的设计没什么问题
面试官说那假设超级popular qps特别大
就扯了memcached 多台服务器 多台数据库blahblah
基本上把自己知道的东西都往上扯 数据库sharding replica server用master slave啥的
面试官感觉你起码有这个概念 聊聊时间就过去了

* Comment4
 { They expect MVC architecture. }

* Comment5
面系统设计被问到这个题应该如何回答呢？
这整个看起来就像一个OOD Design, 看到面经上有同学提到面试官的要求: 
1. 要求用户可以输钱，赢钱
2. 用户增加到5M +

整个题目看起来非常像OOD design, 没有很多系统设计的考点。
弱弱的问一下这个题应该从哪些方面去考虑呢？
然后有哪些需要scale的问题呢？

自己想了一些，希望有idea的同学补充一下。
(里面有很多没有展开说，只是想在这里有个基本设计的idea)
Scenario:
用户登录玩 hangman,
5M user
QPS ：来源于用户登录/充值，lookup 操作，start game, store game。

service?
user service : register/login/look up(check balance, game history)/ update profile/buy god(充值)

word dict （根据难度分组，easy, medium, hard， 或者level1-10）

load game (different style game ?)
根据用户选择的难度，load game. 然后开始。

如果有5个Million的game，要根据用户的情况生成不同的session?
如果用户过多，我就多产生一些page, 例如 
hangman.com/game1-session1
hangman.com/game1-session2
hangman.com/game2-session12
hangman.com/game1-session13

如何存储？
实际上就是一个serialize 和 deserialize 的过程，存储当前游戏的状态，到了哪一步，还有多少次猜的机会，还有什么character available.

如果用户掉线？
如果游戏还没完，检测到用户掉线，就把当前游戏存进DB（因为涉及到输钱赢钱的问题，不可能用户看快输了就拔线= =），以及想到此处可以和面试官讨论并且用memcache优化。 

* Comment 6
好的，我大概说说。当时讲的比较散乱，基本是他们问一个方面我答一个方面。

首先我不懂这个游戏，让他们介绍了一下。
然后我确认了一下前台（web端）的基本内容，把几个简单的交互画了一下跟他们确认
要实现的内容：1. 用户登录；2.可以查看历史游戏结果；3. 可以load未完成游戏；4.
可以new game。
再就是问每个game的词从哪来，有多少词可以选。

再就是开始整理前后台交互的api了，我是边讲边想的，就假装自己在做前台页面。

首先页面全静态放CDN，前台向后台ajax请求/get_info，后台校验cookies的session是
否过期，过期则返回需要登陆的错误码，前台弹登陆框（这里他们问我返回什么状态码
，我说200，他们表示质疑，我说不用跳转我们直接根据错误码在原页面显示登陆框也
一样），这个登录态校验所有的请求都会有。如果用户已登陆就返回用户的历史游戏记
录，列了下json格式。

然后实现查看历史游戏结果，这里就不需要后台了，因为登陆之后所有游戏记录都返回
了。

再是load一个游戏，请求/load 输入游戏id，返回游戏status（id，时间，猜过的字母
，当前单词，剩余次数）

或者create一个游戏，请求/create，返回游戏status

再就是玩游戏猜词了，前台请求/guess 发送游戏id，所猜字母，后台返回游戏status

基本就这些，再就说说存储，这个全都可以拿key-value的storage存着，一个是用户的
key，再就是游戏的key，用户key下面存历史游戏，游戏key下面存游戏status；词典也
可以存里面，搞个不一样的前缀加上数字，每次创建游戏时random一下取一个出来。

* Comment7
面系统设计被问到这个题应该如何回答呢？
这整个看起来就像一个OOD Design, 看到面经上有同学提到面试官的要求: 
1. 要求用户可以输钱，赢钱
2. 用户增加到5M +

整个题目看起来非常像OOD design, 没有很多系统设计的考点。
弱弱的问一下这个题应该从哪些方面去考虑呢？
然后有哪些需要scale的问题呢？

自己想了一些，希望有idea的同学补充一下。
(里面有很多没有展开说，只是想在这里有个基本设计的idea)
Scenario:
用户登录玩 hangman,
5M user
QPS ：来源于用户登录/充值，lookup 操作，start game, store game。

service?
user service : register/login/look up(check balance, game history)/ update profile/buy god(充值)

word dict （根据难度分组，easy, medium, hard， 或者level1-10）

load game (different style game ?)
根据用户选择的难度，load game. 然后开始。

如果有5个Million的game，要根据用户的情况生成不同的session?
如果用户过多，我就多产生一些page, 例如 
hangman.com/game1-session1
hangman.com/game1-session2
hangman.com/game2-session12
hangman.com/game1-session13

如何存储？
实际上就是一个serialize 和 deserialize 的过程，存储当前游戏的状态，到了哪一步，还有多少次猜的机会，还有什么character available.

如果用户掉线？
如果游戏还没完，检测到用户掉线，就把当前游戏存进DB（因为涉及到输钱赢钱的问题，不可能用户看快输了就拔线= =），以及想到此处可以和面试官讨论并且用memcache优化。

5 个回复 


2016-11-30 东邪黄药师
没玩过 hangman ....你可否先解释一下游戏规则。。。


2016-11-30 Hang Zhang
这里有一个简单的hangman http://www.webhangman.com/hangman-highscores.php
其实就是猜词语，例如给你5次机会，你每次可以猜一个字母，
比如Google, 你每次可以猜a-z之间的一个。 你如果猜错（猜的word不在google里，机会-1）
如果你在5次之内才对，就算赢。
为什么叫hangman 是因为，这个上吊的man, 可以5次画完，每猜错就画一笔，如果画完这个上吊的小儿就算输。


2016-12-13 Q'c
顶一下 我也不明白重点在哪


2016-12-21 彭珂
顶一下这个帖子。
请问有人能提供一些思路么？ 
多谢


2016-12-23 陈老师
这道题是一个比较开发性的题目， 一般会根据面试者的要求来取有所偏向。
好的答案需要做到下面这一点 System diagrams, possibly interface sketches, and descriptions of the interactions through the life of the game.
如果考虑到用户注册登录， 怎么样加入payment等相关功能，怎么做scale(表单怎么shard)那就更好了。 
所以这种题目，前期一般是OOD 或者就是数据库设计， 如果答的好，后期就是转化为 系统设计，考虑上面我所说的那三点。



## Scenario
### Features
* Type of games
	- Multiplayer game
	- Human against computer game 
* Human against computer
	- Select a category
		+ Pick word topics, difficult level
		+ Randomly 
	- Guess a character
	- Game termination
		+ Success
		+ Failure
	- Hint
	- Time tracking
		+ Best time
* User system
	- Ladders
	- Scoring
	- Load / Store current status
	- Leader board
* UI

#### Serialize
* Simple 1D representation
* [Command log based representation](http://www.indieflashblog.com/how-to-create-async-part2.html)

#### Deserialize
* [Load games from the database](http://www.indieflashblog.com/how-to-create-async-part3.html)

#### MySQL Databases
* MySQL	- The table “hm_games” is used to store all of the relevant game data. The “cmdLog” field will store the actual list of notated commands the game engine will use to rebuild the game state.

| Field	        | Type	     	|   Notes  |
| ------------- |:-------------:| ----------------------------------:|
| ID_GAME       | int(10)	 	|   Stores the unique game record id |
| ranked        | tinyint(4) 	|	Indicates whether or not game is counted for rank |
| timeRecorded	| int(10)	 	|	The epoch time in seconds game was last updated | 
| version 		| varchar(16)	|	Indicates client version game was created with |
| cmdLog  		| TEXT		 	|	The game state represented via command log |
| status  		| tinyint(4) 	|	Indicates whether the game is in progress or complete |
| timeCreated 	| int(10)	 	|	The epoch time in seconds game was created |
| whoseTurn		| mediumint(8)  |	The user id of the player whose turn is active |
| timeLastTurn	| int(10)	 	| 	The epoch time in seconds last turn was completed |
| isAsync		| tinyint(4) 	|	Indicates whether or not game is async or synced |
	
* MySQL - The table “hm_gameresults” is used to store player-specific information related to the game. All of the players for a particular game are connected to the hm_games table via ID_GAME. This table stores the result (whether or not the player won or lost), rating change (if game is ranked), and will also be developed further later to help determine which animations the player needs to see when they rejoin the game.

| Field          | Type         | Notes                                              | 
|----------------|--------------|----------------------------------------------------| 
| ID_MEMBER      | mediumint(8) | The unique id of player participating in this game | 
| ID_GAME        | int(10)      | The unique id of the game record for this result   | 
| result         | tinyint(4)   | The outcome of the game for this player (win/loss) | 
| ratingChange   | tinyint(4)   | The change in players rating for ranked games      | 

#### Select a random row from database
* [MySQL](http://jan.kneschke.de/projects/mysql/order-by-rand/)

#### Multiplayer vs single player
#### Pick category and difficulty for words

| UIUD | Word  | Category | Difficulty | 
|------|-------|----------|------------| 
| 1    | apple | fruit    | easy       | 
| 2    | car   | ford     | middle     | 

##### Databases
* MySQL
* NoSQL

#### Achievement history

### Common features
#### User system

## Service 
### 


