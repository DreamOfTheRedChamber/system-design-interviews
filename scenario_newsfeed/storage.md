- [Storage mechanism](#storage-mechanism)
  - [SQL database](#sql-database)
  - [NoSQL database](#nosql-database)
  - [File system](#file-system)
  - [Schema design](#schema-design)
    - [User table](#user-table)
    - [Friendship table](#friendship-table)
    - [Tweet table](#tweet-table)
  - [Additional storage](#additional-storage)

# Storage mechanism

## SQL database

* User table
* Social graph - followers \(SQL/NoSQL\)
  * Need to support multiple index

## NoSQL database

* Tweets \(Tweet service\) 

## File system

* Images
* Videos

## Schema design

### User table

| Columns | Type |
| :--- | :--- |
| id | Integer |
| username | varchar |
| email | varchar |
| password | varchar |

### Friendship table

* Select \* from friendship\_table where from\_user\_id = user\_id

| Columns | Type |
| :--- | :--- |
| id | Integer |
| from\_user\_id | foreign key |
| to\_user\_id | foreign key |

### Tweet table

| Columns | Type |
| :--- | :--- |
| id | Integer |
| user\_id | foreign\_key |
| content | text |
| created\_at | timestamp |

## Additional storage

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
