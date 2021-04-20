
# API Design
## Yelp
## Uber
## Instagram
## Youtube
## Google Drive
## Twitter
## Facebook messenger
## Trends
* Count
	1. countViewEvent(videoId)
	2. countEvent(videoId, eventType) 
		+ eventType: view/like/share
	3. processEvent(video, eventType, func)
		+ func: count/sum/avg
	4. processEvents(listOfEvents)
* Query
	1. getViewsCount(videoId, startTime, endTime)
	2. getCount(videoId, eventType, startTime, endTime)
	3. getStats(videoId, eventType, func, startTime, endTime) 

## Real world 
### Netflix
* GraphQL at Netflix: 
  * https://netflixtechblog.com/beyond-rest-1b76f7c20ef6
  * https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-2-bbe71aaec44a
  * https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-1-ae3557c187e2
  * https://netflixtechblog.com/our-learnings-from-adopting-graphql-f099de39ae5f
* API migration at Netflix:
  * https://netflixtechblog.com/seamlessly-swapping-the-api-backend-of-the-netflix-android-app-3d4317155187