
# Flowchart

![Flowchart](../.gitbook/assets/tinyurl_flowchart.png)

# Deal with expired records

* Solution1: Check data in the service level, if expired, return null
  * Pros: Simple and keep historical recordds
  * Cons: Waste disks
* Solution2: Remove expired data in the database and cache using daemon job
  * Pros: Reduce storage and save cost, improve query performance
  * Cons: 
    * Lost historical records
    * Complicated structure
    * Easy to fail

```text
while(true)
{
    List<TinyUrlRecord> expiredRecords = getExpiredRecords();
    For (TinyUrlRecord r: expiredRecords)
    {
        deleteFromDb(r);
        removeFromCache(r);
    }
}
```