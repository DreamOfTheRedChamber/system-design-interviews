- [Pull-based](#pull-based)
  - [Use case](#use-case)
  - [Cons](#cons)
  - [Scale pull](#scale-pull)
  - [Post tweet](#post-tweet)
    - [Steps for post a tweet](#steps-for-post-a-tweet)
    - [Complexity](#complexity)
  - [NewsFeed](#newsfeed)
    - [Steps for news feed](#steps-for-news-feed)
    - [Complexity](#complexity-1)
- [Push-based](#push-based)
  - [Use case](#use-case-1)
  - [Cons](#cons-1)
  - [Scale push](#scale-push)
  - [Additional storage](#additional-storage)
  - [Post tweet](#post-tweet-1)
    - [Steps](#steps)
    - [Complexity](#complexity-2)
  - [Newsfeed](#newsfeed-1)
    - [Steps](#steps-1)
    - [Complexity](#complexity-3)
- [Push and Pull Combined approach](#push-and-pull-combined-approach)

# Pull-based
## Use case
* Single direction relationship
  * Star users
* High latency

## Cons
* High latency
  * Need to wait until N DB reads finish

## Scale pull
* Add cache before visiting DB, faster than 1000 times
* What to cache
  * Cache each user's timeline
    * N DB query request -&gt; N cache requests
    * Trade off: Cache all timeline? Only cache the latest 1000 timeline
  * Cache each user's newsFeed
    * For users without newsfeed cache: Merge N followers' 100 latest tweets, sort and take the latest 100 tweets.
    * For users with newsfeed cache: Merge N followers' tweets after a specific timestamp. And then merge with the cache. 


## Post tweet
### Steps for post a tweet
1. Client asks to add a new tweet record
2. Web server asks tweet service to add a new record

```text
postTweet(request, tweet)
    DB.insertTweet(request.User, tweet)
    return success
```

### Complexity

* Post a tweet
  * Post a tweet: 1 DB write

## NewsFeed
### Steps for news feed
1. Client asks web server for news feed.
2. Web server asks friendship service to get all followings.
3. Web server asks tweet service to get tweets from followings.
4. Web server merges each N tweets from each tweet service and merge them together.
5. Web server returns merged results to client. 

```python
# each following's first 100 tweets, merge with a key way sort

getNewsFeed(request)
    followings = DB.getFollowings(user=request.user)
    newsFeed = empty
    for follow in followings:
        tweets = DB.getTweets(follow.toUser, 100)
        newsFeed.merge(tweets)
    sort(newsFeeds)
    return newsFeed
```

### Complexity
* Algorithm level: 
  * 100 KlogK \( K is the number of friends\)
* System leveL:
  * Get news feed: N DB reads + K way merge
    * Bottleneck is in N DB reads, although they could be integrated into one big DB query. 

# Push-based
## Use case

* Bi-direction relationship
  * No star users: Users do not have a lot of followers
* Low latency

## Cons
* When number of followers is really large, the number of asynchronous task will have high latency. 

## Scale push
* Push-based approach stores news feed in disk, much better than the optimized pull approach.
* For inactive users, do not push
  * Rank followers by weight \(for example, last login time\)
* When number of followers &gt;&gt; number of following
  * Lady Gaga has 62.5M followers on Twitter. Justin Bieber has 77.6M on Instagram. Asynchronous task may takes hours to finish. 


## Additional storage

* Need to have an additional newsfeed table. The newsfeed table contains newsfeed for each user. The newsFeed table schema is as follows:
  * Everyone's newsfeed info is stored in the same newsFeed table.
  * Select \* from newsFeed Table where owner\_id = XX orderBy createdAt desc limit 20;

| Column | Type |
| :--- | :--- |
| id | integer |
| ownerId | foreign key |
| tweetId | foreign key |
| createdAt | timestamp |

## Post tweet
### Steps
1. Client asks web server to post a tweet.
2. Web server asks the tweet service to insert the tweet into tweet table.
3. Web server asks the tweet service to initiate an asynchronous task.
   1. The asynchronous task gets followers from friendship table.
   2. The asynchronus task fanout new tweet to followers' news feed table. 

```python
postTweet(request, tweetInfo)
    tweet = DB.insertTweet(request.user, tweetInfo)

    # Do not need to be blocked until finished. RabbitMQ/Kafka
    AsyncService.fanoutTweet(request.user, tweet)
    return success

AsyncService::fanoutTweet(user, tweet)
    followers = DB.getFollowers(user)
    for follower in followers:
        DB.insertNewsFeed(tweet, follower)
```

### Complexity

* Post a tweet: N followers, N DB writes. Executed asynchronously. 

## Newsfeed

### Steps

1. Get newsfeed from newsFeed Table.  

```text
// Each time after a user tweet, fanout his tweets to all followers' feed list

getNewsFeed(request)
    return DB.getNewsFeed(request.user)
```

### Complexity

* 1 DB query


# Push and Pull Combined approach
* For users with a lot of followers, use pull; For other users, use push. 
* Define a threshold \(number of followers\)
  * Below threshold use push
  * Above threshold use pull
* For popular users, do not push. Followers fetch from their timeline and integrate into news feed. 

**Oscillation problems**

* May miss updates. 
* Solutions:
  * Star users: Pull not push
  * Half star user: Pull + Push
  * Normal user: Push

