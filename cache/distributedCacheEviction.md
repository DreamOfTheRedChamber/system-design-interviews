# Least Recently Used (LRU)
## Def
* One of the most used strategies is LRU. In most caching use cases, applications access the same data again & again. Say in any Google search engine, when you search for something, you will get the same results again & again at least for some time window. When you search flights or bus or train, you get the same routes unless & until some route gets deactivated or fully reserved. In such use cases, the cached data that is not used very recently or sort of cold data can be safely evicted.

## Pros
1. Is nearest to the most optimal algorithm
2. Selects and removes elements that are not used recently

## Cons
1. Only mild success rate since often it is more important how often an element was accessed than when it was last accessed

# Least Frequently Used (LFU)
## Def
* Your mobile keyboard uses LFU. When you type some letters you can see few suggested words at the top of the keyboard matching with the letters you have typed. At the begining when the keyboard app's cache is empty, it may show you these 4 words ( Lets assume, you typed letters "STA". Suggested words may pop like ex. start, stand, statue, staff). The idea here is that, based on the words you use, it will ignore the LRU word in the suggestions after a certain time. You may not see the word "staff" in the suggesions later on if you haven't used it.
* If you have a case where you know that the data is pretty repetative, surely go for LFU to avoid cache miss. It seems that these both are independent quite clearly and have isolative significance. It depends on the use case of where you want to use any of these.

## Pros
1. Takes age of the element into account
2. Takes reference frequency of the element into account
3. Works better under high load when quickly a lot of elements is requested (less false eviction)

## Cons
1. A frequently accessed element will only be evicted after lots of misses
2. More important to have invalidation on elements that can change

# MRU
## Def
Most Recently Used (MRU): Let’s consider Tinder. Tinder personalises matching profiles for you and say it buffers those result in a cache or a high performance cache. So you can assume that some space for every user is allocated to queue corresponding personalised results. When you see Tinder’s recommendation page, the moment you right or left swipe, you don’t need that recommendation view any more. So in this use case, Tinder can remove the recommendation from that user’s queue & free up space in memory. This strategy removes most recently used items as they are not required at least in the near future.

# First In, First Out (FIFO)
* It’s more of like MRU but it follows strict ordering of inserted data items. MRU does not honour insertion order.
* In some use cases, you might need to apply a combination of eviction policies such as LRU + LFU to decide on which data to evict. That’s your use case dependent, so try to choose such technologies which are inline with the eviction policies you thought of.