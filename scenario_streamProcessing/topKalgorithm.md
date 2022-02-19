# Single treeMap

* TC: O(nlgm)
* SC: O(m)

# HashMap + PriorityQueue

* Parameters
  * n: number of records
  * m: number of distinct entries
  * K: target k
* TC: O(n + mlgk) = O(n)
  * Count frequency: O(n)
  * Calculate top K: O(mlgk)
* SC: O(n + k)
  * HashMap: O(n)
  * PriorityQueue: O(k)

# LFU cache

* DLL + HashMap based LFU cache (approximate answer)
  * TC: O(n + k)
  * SC: O(n)
  * Cons:
    * All low frequency will be hashed to same value, which will result in incorrect result (low possibility)
    * Some low frequency words will come later, which will have a great count, then replace other high frequency words (bloom filter)
      * HashMap will have 3 different hash functions
      * Choose the lowest count from hashmap

# Count-min sketch

* [https://florian.github.io/count-min-sketch/](https://florian.github.io/count-min-sketch/)
* [https://towardsdatascience.com/big-data-with-sketchy-structures-part-1-the-count-min-sketch-b73fb3a33e2a](https://towardsdatascience.com/big-data-with-sketchy-structures-part-1-the-count-min-sketch-b73fb3a33e2a)

# Lossy counting

* [https://micvog.com/2015/07/18/frequency-counting-algorithms-over-data-streams/](https://micvog.com/2015/07/18/frequency-counting-algorithms-over-data-streams/)
