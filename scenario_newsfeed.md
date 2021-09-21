# Scenario\_NewsFeed

* [NewsFeed](scenario_newsfeed.md#newsfeed)
  * [Scenario](scenario_newsfeed.md#scenario)
    * [Core features](scenario_newsfeed.md#core-features)
    * [Common features](scenario_newsfeed.md#common-features)
  * [Service](scenario_newsfeed.md#service)
    * [User service](scenario_newsfeed.md#user-service)
    * [Tweet service](scenario_newsfeed.md#tweet-service)
    * [Media service](scenario_newsfeed.md#media-service)
    * [Friendship service](scenario_newsfeed.md#friendship-service)
  * [Storage](scenario_newsfeed.md#storage)
    * [Storage mechanism](scenario_newsfeed.md#storage-mechanism)
      * [SQL database](scenario_newsfeed.md#sql-database)
      * [NoSQL database](scenario_newsfeed.md#nosql-database)
      * [File system](scenario_newsfeed.md#file-system)
    * [Schema design](scenario_newsfeed.md#schema-design)
      * [User table](scenario_newsfeed.md#user-table)
      * [Friendship table](scenario_newsfeed.md#friendship-table)
      * [Tweet table](scenario_newsfeed.md#tweet-table)
  * [Initial solution](scenario_newsfeed.md#initial-solution)
    * [Pull-based](scenario_newsfeed.md#pull-based)
      * [Post tweet](scenario_newsfeed.md#post-tweet)
        * [Steps for post a tweet](scenario_newsfeed.md#steps-for-post-a-tweet)
        * [Complexity](scenario_newsfeed.md#complexity)
      * [NewsFeed](scenario_newsfeed.md#newsfeed-1)
        * [Steps for news feed](scenario_newsfeed.md#steps-for-news-feed)
        * [Complexity](scenario_newsfeed.md#complexity-1)
        * [Disadvantages](scenario_newsfeed.md#disadvantages)
    * [Push-based](scenario_newsfeed.md#push-based)
      * [Additional storage](scenario_newsfeed.md#additional-storage)
      * [Post tweet](scenario_newsfeed.md#post-tweet-1)
        * [Steps](scenario_newsfeed.md#steps)
        * [Complexity](scenario_newsfeed.md#complexity-2)
      * [Newsfeed](scenario_newsfeed.md#newsfeed-2)
        * [Steps](scenario_newsfeed.md#steps-1)
        * [Complexity](scenario_newsfeed.md#complexity-3)
        * [Disadvantages](scenario_newsfeed.md#disadvantages-1)
  * [Scale](scenario_newsfeed.md#scale)
    * [Pull-based approach easier to scale](scenario_newsfeed.md#pull-based-approach-easier-to-scale)
      * [Scale pull](scenario_newsfeed.md#scale-pull)
      * [Scale push](scenario_newsfeed.md#scale-push)
      * [Push and Pull](scenario_newsfeed.md#push-and-pull)
        * [Combined approach](scenario_newsfeed.md#combined-approach)
        * [Oscillation problems](scenario_newsfeed.md#oscillation-problems)
      * [Push vs Pull](scenario_newsfeed.md#push-vs-pull)
        * [Push use case](scenario_newsfeed.md#push-use-case)
        * [Pull use case](scenario_newsfeed.md#pull-use-case)
    * [Hot spot / Thundering herd problem](scenario_newsfeed.md#hot-spot--thundering-herd-problem)
  * [Additional feature: Follow and unfollow](scenario_newsfeed.md#additional-feature-follow-and-unfollow)
  * [Additional feature: Likes and dislikes](scenario_newsfeed.md#additional-feature-likes-and-dislikes)
    * [Schema design](scenario_newsfeed.md#schema-design-1)
    * [Denormalize](scenario_newsfeed.md#denormalize)
  * [Drafted overflow](scenario_newsfeed.md#drafted-overflow)
  * [Friendly links - good summary on newsfeed](scenario_newsfeed.md#friendly-links---good-summary-on-newsfeed)
* [Notifications](scenario_newsfeed.md#notifications)
  * [Type of notifications](scenario_newsfeed.md#type-of-notifications)
  * [Structure](scenario_newsfeed.md#structure)
  * [High level design](scenario_newsfeed.md#high-level-design)

## NewsFeed

### Scenario

#### Core features

* News feed
* Post a tweet
* Timeline
* Follow / Unfollow a user
* Notifications

#### Common features

* Register / Login
* Upload image / video
* Search

### Service

#### User service

* Register
* Login

#### Tweet service

* Post a tweet
* Newsfeed
* Timeline

#### Media service

* Upload image
* Upload video

#### Friendship service

* Follow
* Unfollow

### Storage

#### Storage mechanism

**SQL database**

* User table
* Social graph - followers \(SQL/NoSQL\)
  * Need to support multiple index

**NoSQL database**

* Tweets \(Tweet service\) 

**File system**

* Images
* Videos

#### Schema design

**User table**

| Columns | Type |
| :--- | :--- |
| id | Integer |
| username | varchar |
| email | varchar |
| password | varchar |

**Friendship table**

* Select \* from friendship\_table where from\_user\_id = user\_id

| Columns | Type |
| :--- | :--- |
| id | Integer |
| from\_user\_id | foreign key |
| to\_user\_id | foreign key |

**Tweet table**

| Columns | Type |
| :--- | :--- |
| id | Integer |
| user\_id | foreign\_key |
| content | text |
| created\_at | timestamp |

### Initial solution

#### Pull-based

**Post tweet**

**Steps for post a tweet**

1. Client asks to add a new tweet record
2. Web server asks tweet service to add a new record

```text
postTweet(request, tweet)
    DB.insertTweet(request.User, tweet)
    return success
```

**Complexity**

* Post a tweet
  * Post a tweet: 1 DB write

**NewsFeed**

**Steps for news feed**

1. Client asks web server for news feed.
2. Web server asks friendship service to get all followings.
3. Web server asks tweet service to get tweets from followings.
4. Web server merges each N tweets from each tweet service and merge them together.
5. Web server returns merged results to client. 

```text
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

**Complexity**

* Algorithm level: 
  * 100 KlogK \( K is the number of friends\)
* System leveL:
  * Get news feed: N DB reads + K way merge
    * Bottleneck is in N DB reads, although they could be integrated into one big DB query. 

**Disadvantages**

* High latency
  * Need to wait until N DB reads finish

#### Push-based

**Additional storage**

* Need to have an additional newsfeed table. The newsfeed table contains newsfeed for each user. The newsFeed table schema is as follows:
  * Everyone's newsfeed info is stored in the same newsFeed table.
  * Select \* from newsFeed Table where owner\_id = XX orderBy createdAt desc limit 20;

| Column | Type |
| :--- | :--- |
| id | integer |
| ownerId | foreign key |
| tweetId | foreign key |
| createdAt | timestamp |

**Post tweet**

**Steps**

1. Client asks web server to post a tweet.
2. Web server asks the tweet service to insert the tweet into tweet table.
3. Web server asks the tweet service to initiate an asynchronous task.
   1. The asynchronous task gets followers from friendship table.
   2. The asynchronus task fanout new tweet to followers' news feed table. 

```text
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

**Complexity**

* Post a tweet: N followers, N DB writes. Executed asynchronously. 

**Newsfeed**

**Steps**

1. Get newsfeed from newsFeed Table.  

```text
// Each time after a user tweet, fanout his tweets to all followers' feed list

getNewsFeed(request)
    return DB.getNewsFeed(request.user)
```

**Complexity**

* 1 DB query

**Disadvantages**

* When number of followers is really large, the number of asynchronous task will have high latency. 

### Scale

#### Pull-based approach easier to scale

**Scale pull**

* Add cache before visiting DB, faster than 1000 times
* What to cache
  * Cache each user's timeline
    * N DB query request -&gt; N cache requests
    * Trade off: Cache all timeline? Only cache the latest 1000 timeline
  * Cache each user's newsFeed
    * For users without newsfeed cache: Merge N followers' 100 latest tweets, sort and take the latest 100 tweets.
    * For users with newsfeed cache: Merge N followers' tweets after a specific timestamp. And then merge with the cache. 

**Scale push**

* Push-based approach stores news feed in disk, much better than the optimized pull approach.
* For inactive users, do not push
  * Rank followers by weight \(for example, last login time\)
* When number of followers &gt;&gt; number of following
  * Lady Gaga has 62.5M followers on Twitter. Justin Bieber has 77.6M on Instagram. Asynchronous task may takes hours to finish. 

**Push and Pull**

**Combined approach**

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

**Push vs Pull**

**Push use case**

* Bi-direction relationship
  * No star users: Users do not have a lot of followers
* Low latency

**Pull use case**

* Single direction relationship
  * Star users
* High latency

#### Hot spot / Thundering herd problem

* Cache \(Facebook lease get problem\)

### Additional feature: Follow and unfollow

* Asynchronously executed
  * Follow a user: Merge users' timeline into news feed asynchronously
  * Unfollow a user: Pick out tweets from news feed asynchronously
* Benefits:
  * Fast response to users.
* Disadvantages: 
  * Consistency. After unfollow and refreshing newsfeed, users' info still there. 

### Additional feature: Likes and dislikes

#### Schema design

* Tweet table

| Columns | Type |
| :--- | :--- |
| id | integer |
| userId | foreign key |
| content | text |
| createdAt | timestamp |
| likeNums | integer |
| commentNums | integer |
| retweetNums | integer |

* Like table

| Columns | Type |
| :--- | :--- |
| id | integer |
| userId | foreignKey |
| tweetId | foreignKey |
| createdAt | timestamp |

#### Denormalize

* Select Count in Like Table where tweet id == 1
* Denormalize: 
  * Store like\_numbers within Tweet Table
  * Need distributed transactions. 
* Might resulting in inconsistency, but not a big problem. 
  * Could keep consistency with a background process.

### Drafted overflow

* [Pull push overview](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/newsfeed_pullPushOverview.jpg)

### Friendly links - good summary on newsfeed

* [https://liuzhenglaichn.gitbook.io/systemdesign/news-feed/facebook-news-feed](https://liuzhenglaichn.gitbook.io/systemdesign/news-feed/facebook-news-feed)

## Notifications

### Type of notifications

* iOS: 
  * APN: A remote notification service built by Apple to push notification to iOS devices. 
* Android:
  * Firebase Cloud Messaging: Instead of APNs, FCM is commonly used to send notifications to mobile devices. 
* SMS:
  * Third party SMS providers such as Twillo, Nexmo, etc.
* Email:
  * Set up their own email servers
  * Or commercial email service such as Sendgrid, Mailchimp, etc.

### Structure

* Provider builds notification with device token and a notification payload.

### High level design

* Notification servers could be the bottleneck
  * A single notification server means a single point of failure
  * Performance bottleneck: Processing and sending notifications can be resource intensive. For example, constructing HTML pages and waiting for responses from third party services could take time. Handling everything in one system can result in the system overload, especially during peak hours.
* Add message queue to decouple:
  * Move the database and cache out of the notification server.
  * Introduce message queues to decouple the system components.

![Improved flow](.gitbook/assets/newsfeed_notification_improvedflow.png)

