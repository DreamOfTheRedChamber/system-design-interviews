# Performance

## DNS resolver

* DNS resolution is a well-known bottleneck in web crawling. Due to the distributed nature of the Domain Name Service, DNS resolution may entail multiple requests and round-trips across the internet, requiring seconds and sometimes even longer. Right away, this puts in jeopardy our goal of fetching several hundred documents a second. 
  * A standard remedy is to introduce caching: URLs for which we have recently performed DNS lookups are likely to be found in the DNS cache, avoiding the need to go to the DNS servers on the internet. However, obeying politeness constraints limits the of cache hit rate.
  * [https://nlp.stanford.edu/IR-book/pdf/20crawl.pdf](https://nlp.stanford.edu/IR-book/pdf/20crawl.pdf) for more details.
