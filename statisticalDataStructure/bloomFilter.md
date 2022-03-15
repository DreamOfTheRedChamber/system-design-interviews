# Bloom filter

## Use case
* An empty bloom filter is merely a Bit Vector storing bits with all set to value as 0.
* To add an element to the Bloom filter, we simply hash it by using our hash function and set the bits in the bit vector at the index of those hashes to 1.
* To test for membership, you simply hash the string with the same hash functions, then see if those values are set in the bit vector. If the bits are 1 then the element is probably present, but if zero then the word is definitely not present.

![](.gitbook/assets/datastructure_bloomfilter.png)

## Pros
* Space efficiency: Bloom filter does not store the actual items. In this way it’s space efficient. It’s just an array of integers.
* Saves expensive data scanning across several servers depending on the use case.

## Cons
* Removal Of Item: If you remove an item or more specifically clear set bits corresponding to some item from bloom filter, it might erase some other data as well because many set bits might be shared among multiple items. So remember if you use bloom filter, removal is not an option for you. What’s inserted in bloom filter, stays in bloom filter.
* Inserted items are not retrievable: Bloom filters does not track items, it just sets different positions in the array. So inserted items can not be checked out.
* False Positive result: The size of the bloom filter has to be known beforehand. Otherwise with a small size, the array will saturate and the amount of false positive result will increase. In case of saturation, you can try designing some strategy to reset the bloom filter or use a scalable bloom filter. If you keep adding more & more elements to the bloom filter, the probability of false positive increases.
* Insertion & Search Cost: Every item goes through k hash functions. So performance during insertion or search operation depends on the efficiency of the chosen hash functions as well. Inserting element & searching element takes O(k) time as you run k hash functions and just set or check k number of indices in the array.

## Real life applications
### Cassandra usage
* Apache Cassandra uses SSTable data structure on disk to save rows. So at a millions of scale, there will be thousands of SSTable files on disk. Even tens of thousands of read requests per unit time will cause very expensive disk IO operations to search for concerned row(s) in all the SSTables one by one in some order when data is not found in the in-memory tables of Cassandra. So bloom filters are used to approximately identify if some row / column with the given data or id exists in a SSTable. If the result is ‘May Be Present’, Cassandra searches in the corresponding SSTable, in case the row is not found, Cassandra continues search operation in other SSTables. So using bloom filter, Cassandra saves a lot of unnecessary SSTable scan hence saving huge disk IO operations cost.

### Content Recommendation System
* Imagine some site recommending you some articles, news, videos etc which you might not have seen earlier. So there are probably thousands of stuffs which can be recommended and you might have seen tens or hundreds of recommendation already. So in order to skip the recommendations that are already served to you, bloom filters are used. Medium uses bloom filter to avoid showing duplicate recommendations.

### One-hit-wonders
* Akamai & Facebook uses bloom filters to avoid caching the items that are very rarely searched or searched only once. Only when they are searched more than once, they will get cached. Several strategies might be designed to avoid such situations.

### Financial Fraud Detection
* If you have a credit card, your credit card company knows about your spending history — the vendors that you have transacted with previously, the category of vendors, the cities where the card was used etc. So when you make a new transaction, in the background some rules can execute which will decide whether the vendor or city or any parameter is already seen or suspicious. Bloom filters can be used to design such strategy.

## References
* https://medium.datadriveninvestor.com/bloom-filter-a-simple-but-interesting-data-structure-37fd53b11606

### Todo
* [When bloomfilters don't bloom](https://blog.cloudflare.com/when-bloom-filters-dont-bloom/)