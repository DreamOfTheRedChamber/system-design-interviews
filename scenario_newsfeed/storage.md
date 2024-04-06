- [SQL database](#sql-database)
  - [User table](#user-table)
  - [Friendship table](#friendship-table)
  - [Tweet table](#tweet-table)
  - [Newsfeed table](#newsfeed-table)
- [NoSQL database](#nosql-database)
- [Object storage](#object-storage)

# SQL database

* User table
* Social graph - followers \(SQL/NoSQL\)
  * Need to support multiple index

## User table

| Columns | Type |
| :--- | :--- |
| id | Integer |
| username | varchar |
| email | varchar |
| password | varchar |

## Friendship table

* Select \* from friendship\_table where from\_user\_id = user\_id

| Columns | Type |
| :--- | :--- |
| id | Integer |
| from\_user\_id | foreign key |
| to\_user\_id | foreign key |

## Tweet table

| Columns | Type |
| :--- | :--- |
| id | Integer |
| user\_id | foreign\_key |
| content | text |
| created\_at | timestamp |

## Newsfeed table

* Need to have an additional newsfeed table. The newsfeed table contains newsfeed for each user. The newsFeed table schema is as follows:
  * Everyone's newsfeed info is stored in the same newsFeed table.

| Column | Type |
| :--- | :--- |
| id | integer |
| ownerId | foreign key |
| tweetId | foreign key |
| createdAt | timestamp |

```sql
Select * from newsFeed Table where owner_id = XX orderBy createdAt desc limit 20;
```

# NoSQL database

* Tweets \(Tweet service\) 

# Object storage

* Images
* Videos
