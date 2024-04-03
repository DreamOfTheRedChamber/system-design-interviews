- [Follow-up: What if disallowing same long urls mapped to different short urls](#follow-up-what-if-disallowing-same-long-urls-mapped-to-different-short-urls)
  - [Naive sharding key](#naive-sharding-key)
  - [Combine short and long Url as sharding key](#combine-short-and-long-url-as-sharding-key)
    - [Idea](#idea)
    - [Implementation](#implementation)
  - [Geo location sharding key](#geo-location-sharding-key)

# Follow-up: What if disallowing same long urls mapped to different short urls
* Before insert random url into database. Check whether it exists within database, if not then insert. 
* Additional considerations: Not only short-to-long mapping needs to be stored. The long-to-short mapping needs to be stored as well. 
* This will create additional complexity when choosing sharding key

## Naive sharding key
* Use Long Url as sharding key
  * Short to long operation will require lots of cross-shard joins
* Use Short Url as sharding key
  * Short to long url: Find database according to short url; Find long url in the corresponding database
  * Long to short url: Broadcast to N databases to see whether the link exist before. If not, get the next ID and insert into database. 

## Combine short and long Url as sharding key
### Idea 
* Problem: For "31bJF4", how do we find which physical machine the code locate at? 
* Our query is 
  * Select original\_url from tiny\_url where short\_url = XXXXX;
* The simple one
  * We query all nodes. But there will be n queries
  * This will be pretty expensive, even if we fan out database calls. 
  * But the overall QPS to database will be n times QPS 
* Solution
  * Add virtual node number to prefix "31bJF4" =&gt; "131bJF4"
  * When querying, we can find virtual nodes from prefix \(the tinyUrl length is 6\). Then we can directly query node1.

```text
// Assign virtual nodes to physical nodes
// Store within database
{
    0: "db0.tiny_url.com",
    1: "db1.tiny_url.com",
    2: "db2.tiny_url.com",
    3: "db3.tiny_url.com"
}
```

### Implementation

* Hash\(longUrl\)%62 + shortkey
* Given shortURL, we can get the sharding machine by the first bit of shortened url.
* Given longURL, get the sharding machine according to Hash\(longURL\) % 62. Then take the first bit.

## Geo location sharding key
* Sharding according to the geographical info. 
  * First know which websites are more popular in which region. Put all websites popular in US in US DB.
