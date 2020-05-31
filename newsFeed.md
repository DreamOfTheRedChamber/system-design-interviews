# NewsFeed system 

<!-- MarkdownTOC -->

- [Scenario](#scenario)
	- [Core features](#core-features)
		- [News feed](#news-feed)
		- [Post a tweet](#post-a-tweet)
		- [Timeline](#timeline)
		- [Follow / Unfollow a user](#follow--unfollow-a-user)
	- [Common features](#common-features)
		- [Register / Login](#register--login)
		- [Upload image / video](#upload-image--video)
		- [Search](#search)
- [Service](#service)
	- [User service](#user-service)
	- [Tweet service](#tweet-service)
	- [Media service](#media-service)
	- [Friendship service](#friendship-service)
- [Storage](#storage)
	- [Storage mechanism](#storage-mechanism)
		- [SQL database](#sql-database)
		- [NoSQL database](#nosql-database)
		- [File system](#file-system)
	- [Schema design](#schema-design)
		- [User table](#user-table)
		- [Friendship table](#friendship-table)
		- [Tweet table](#tweet-table)
- [Initial solution](#initial-solution)
	- [Pull-based](#pull-based)
		- [Post tweet](#post-tweet)
			- [Steps for post a tweet](#steps-for-post-a-tweet)
			- [Complexity](#complexity)
		- [NewsFeed](#newsfeed)
			- [Steps for news feed](#steps-for-news-feed)
			- [Complexity](#complexity-1)
			- [Disadvantages](#disadvantages)
	- [Push-based](#push-based)
		- [Additional storage](#additional-storage)
		- [Post tweet](#post-tweet-1)
			- [Steps](#steps)
			- [Complexity](#complexity-2)
		- [Newsfeed](#newsfeed-1)
			- [Steps](#steps-1)
			- [Complexity](#complexity-3)
			- [Disadvantages](#disadvantages-1)
- [Scale](#scale)
	- [Pull-based approach easier to scale](#pull-based-approach-easier-to-scale)
		- [Scale pull](#scale-pull)
		- [Scale push](#scale-push)
		- [Push and Pull](#push-and-pull)
			- [Combined approach](#combined-approach)
			- [Oscillation problems](#oscillation-problems)
		- [Push vs Pull](#push-vs-pull)
			- [Push use case](#push-use-case)
			- [Pull use case](#pull-use-case)
	- [Hot spot / Thundering herd problem](#hot-spot--thundering-herd-problem)
- [Additional feature: Follow and unfollow](#additional-feature-follow-and-unfollow)
- [Additional feature: Likes and dislikes](#additional-feature-likes-and-dislikes)
	- [Schema design](#schema-design-1)
	- [Denormalize](#denormalize)
- [Drafted overflow](#drafted-overflow)
- [Friendly links - good summary on newsfeed](#friendly-links---good-summary-on-newsfeed)

<!-- /MarkdownTOC -->


## Scenario
### Core features
#### News feed
#### Post a tweet
#### Timeline
#### Follow / Unfollow a user

### Common features
#### Register / Login
#### Upload image / video
#### Search

## Service
### User service
* Register
* Login

### Tweet service
* Post a tweet
* Newsfeed
* Timeline

### Media service
* Upload image
* Upload video

### Friendship service
* Follow
* Unfollow

## Storage

### Storage mechanism
#### SQL database
* User table
* Social graph - followers (SQL/NoSQL)
	+ Need to support multiple index

#### NoSQL database
* Tweets (Tweet service) 

#### File system
* Images
* Videos

### Schema design
#### User table

| Columns  | Type    | 
|----------|---------| 
| id       | Integer | 
| username | varchar | 
| email    | varchar | 
| password | varchar | 

#### Friendship table
* Select * from friendship_table where from_user_id = user_id

| Columns      | Type        | 
|--------------|-------------| 
| id           | Integer     | 
| from_user_id | foreign key | 
| to_user_id   | foreign key | 

#### Tweet table

| Columns    | Type        | 
|------------|-------------| 
| id         | Integer     | 
| user_id    | foreign_key | 
| content    | text        | 
| created_at | timestamp   | 

## Initial solution
### Pull-based
#### Post tweet
##### Steps for post a tweet
1. Client asks to add a new tweet record
2. Web server asks tweet service to add a new record

```
postTweet(request, tweet)
	DB.insertTweet(request.User, tweet)
	return success
```

##### Complexity
* Post a tweet
	- Post a tweet: 1 DB write

#### NewsFeed
##### Steps for news feed
1. Client asks web server for news feed.
2. Web server asks friendship service to get all followings.
3. Web server asks tweet service to get tweets from followings.
4. Web server merges each N tweets from each tweet service and merge them together.
5. Web server returns merged results to client. 

```
// each following's first 100 tweets, merge with a key way sort

getNewsFeed(request)
	followings = DB.getFollowings(user=request.user)
	newsFeed = empty
	for follow in followings:
		tweets = DB.getTweets(follow.toUser, 100)
		newsFeed.merge(tweets)
	sort(newsFeeds)
	return newsFeed

```

##### Complexity
* Algorithm level: 
	- 100 KlogK ( K is the number of friends)
* System leveL:
	- Get news feed: N DB reads + K way merge
		+ Bottleneck is in N DB reads, although they could be integrated into one big DB query. 

##### Disadvantages
* High latency
	- Need to wait until N DB reads finish

### Push-based
#### Additional storage
* Need to have an additional newsfeed table. The newsfeed table contains newsfeed for each user. The newsFeed table schema is as follows:
	- Everyone's newsfeed info is stored in the same newsFeed table.
	- Select * from newsFeed Table where owner_id = XX orderBy createdAt desc limit 20;

| Column    | Type        | 
|-----------|-------------| 
| id        | integer     | 
| ownerId   | foreign key | 
| tweetId   | foreign key | 
| createdAt | timestamp   | 

#### Post tweet 
##### Steps
1. Client asks web server to post a tweet.
2. Web server asks the tweet service to insert the tweet into tweet table.
3. Web server asks the tweet service to initiate an asynchronous task.
	1. The asynchronous task gets followers from friendship table.
	2. The asynchronus task fanout new tweet to followers' news feed table. 

```
postTweet(request, tweetInfo)
	tweet = DB.insertTweet(request.user, tweetInfo)

	// Do not need to be blocked until finished. RabbitMQ/Kafka
	AsyncService.fanoutTweet(request.user, tweet)
	return success

AsyncService::fanoutTweet(user, tweet)
	followers = DB.getFollowers(user)
	for follower in followers:
		DB.insertNewsFeed(tweet, follower)
```

##### Complexity
* Post a tweet: N followers, N DB writes. Executed asynchronously. 

#### Newsfeed
##### Steps
1. Get newsfeed from newsFeed Table.  

```
// Each time after a user tweet, fanout his tweets to all followers' feed list

getNewsFeed(request)
	return DB.getNewsFeed(request.user)
```

##### Complexity
* 1 DB query

##### Disadvantages
* When number of followers is really large, the number of asynchronous task will have high latency. 

## Scale 

### Pull-based approach easier to scale
#### Scale pull
* Add cache before visiting DB, faster than 1000 times
* What to cache
	- Cache each user's timeline
	    + N DB query request -&gt; N cache requests
	    + Trade off: Cache all timeline? Only cache the latest 1000 timeline
	- Cache each user's newsFeed
	    + For users without newsfeed cache: Merge N followers' 100 latest tweets, sort and take the latest 100 tweets.
	    + For users with newsfeed cache: Merge N followers' tweets after a specific timestamp. And then merge with the cache. 

#### Scale push
* Push-based approach stores news feed in disk, much better than the optimized pull approach.
* For inactive users, do not push
	- Rank followers by weight (for example, last login time)
* When number of followers >> number of following
	- Lady Gaga has 62.5M followers on Twitter. Justin Bieber has 77.6M on Instagram. Asynchronous task may takes hours to finish. 

#### Push and Pull
##### Combined approach
* For users with a lot of followers, use pull; For other users, use push. 
* Define a threshold (number of followers)
	- Below threshold use push
	- Above threshold use pull
* For popular users, do not push. Followers fetch from their timeline and integrate into news feed. 

##### Oscillation problems
* May miss updates. 
* Solutions:
	- Star users: Pull not push
	- Half star user: Pull + Push
	- Normal user: Push

#### Push vs Pull
##### Push use case
* Bi-direction relationship
	- No star users: Users do not have a lot of followers
* Low latency

##### Pull use case
* Single direction relationship
	- Star users
* High latency

### Hot spot / Thundering herd problem
* Cache (Facebook lease get problem)

## Additional feature: Follow and unfollow
* Asynchronously executed
	- Follow a user: Merge users' timeline into news feed asynchronously
	- Unfollow a user: Pick out tweets from news feed asynchronously
* Benefits:
	- Fast response to users.
* Disadvantages: 
	- Consistency. After unfollow and refreshing newsfeed, users' info still there. 

## Additional feature: Likes and dislikes
### Schema design

* Tweet table

| Columns     | Type        | 
|-------------|-------------| 
| id          | integer     | 
| userId      | foreign key | 
| content     | text        | 
| createdAt   | timestamp   | 
| likeNums    | integer     | 
| commentNums | integer     | 
| retweetNums | integer     | 

* Like table

| Columns   | Type       | 
|-----------|------------| 
| id        | integer    | 
| userId    | foreignKey | 
| tweetId   | foreignKey | 
| createdAt | timestamp  | 

### Denormalize
* Select Count in Like Table where tweet id == 1
* Denormalize: 
	- Store like_numbers within Tweet Table
	- Need distributed transactions. 
* Might resulting in inconsistency, but not a big problem. 
	- Could keep consistency with a background process.

## Drafted overflow
* [Pull push overview](./images/newsfeed_pullPushOverview.jpg)

## Friendly links - good summary on newsfeed
* https://liuzhenglaichn.gitbook.io/systemdesign/news-feed/facebook-news-feed
