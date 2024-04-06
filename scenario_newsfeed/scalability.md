- [Scale pull](#scale-pull)
- [Scale push](#scale-push)
- [Hot spot / Thundering herd problem](#hot-spot--thundering-herd-problem)

# Scale pull
* Add cache before visiting DB, faster than 1000 times
* What to cache
  * Cache each user's timeline
    * N DB query request -&gt; N cache requests
    * Trade off: Cache all timeline? Only cache the latest 1000 timeline
  * Cache each user's newsFeed
    * For users without newsfeed cache: Merge N followers' 100 latest tweets, sort and take the latest 100 tweets.
    * For users with newsfeed cache: Merge N followers' tweets after a specific timestamp. And then merge with the cache. 

# Scale push
* Push-based approach stores news feed in disk, much better than the optimized pull approach.
* For inactive users, do not push
  * Rank followers by weight \(for example, last login time\)
* When number of followers > number of following
  * Lady Gaga has 62.5M followers on Twitter. Justin Bieber has 77.6M on Instagram. Asynchronous task may takes hours to finish. 


# Hot spot / Thundering herd problem

* Cache \(Facebook lease get problem\)
