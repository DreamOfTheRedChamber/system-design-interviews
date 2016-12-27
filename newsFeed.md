# NewsFeed system 

<!-- MarkdownTOC -->

- [Scenario](#scenario)
	- [Post a tweet](#post-a-tweet)
	- [Timeline](#timeline)
	- [News feed](#news-feed)
	- [Follow / Unfollow a user](#follow--unfollow-a-user)
	- [Register / Login](#register--login)
- [Service](#service)
	- [User service](#user-service)
	- [Tweet service](#tweet-service)
	- [Media service](#media-service)
	- [Friendship service](#friendship-service)
- [Storage](#storage)
	- [User table](#user-table)
	- [Friendship table](#friendship-table)
	- [Tweet table](#tweet-table)
	- [How to store news feed](#how-to-store-news-feed)
		- [Storage for pull model](#storage-for-pull-model)
		- [Storage for push model](#storage-for-push-model)
		- [Push vs Pull](#push-vs-pull)
- [Scale](#scale)
	- [Solve push related problem](#solve-push-related-problem)
	- [Solve pull related problem](#solve-pull-related-problem)
	- [Oscillation related problem](#oscillation-related-problem)
	- [Hot spot / Thundering herd problem](#hot-spot--thundering-herd-problem)

<!-- /MarkdownTOC -->


## Scenario
### Post a tweet
### Timeline
### News feed
### Follow / Unfollow a user
### Register / Login

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
### User table
* id: integer
* username: varchar
* email: varchar
* password: varchar

### Friendship table
* id: integer
* from_user_id: foreign key
* to_user_id: foreign key

### Tweet table
* id: integer
* user_id: foreign key
* content: text
* created_at: timestamp

### How to store news feed
#### Storage for pull model

#### Storage for push model

#### Push vs Pull

## Scale 
### Solve push related problem
### Solve pull related problem
### Oscillation related problem
### Hot spot / Thundering herd problem
