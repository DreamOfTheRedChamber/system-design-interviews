- [Algorithm](#algorithm)
  - [UUID](#uuid)
  - [Hash - MD5 / SHA-256 / Murmur / Jenkins](#hash---md5--sha-256--murmur--jenkins)
  - [Encoding - Base10 / Base62 / Base64](#encoding---base10--base62--base64)
  - [Escape invalid characters](#escape-invalid-characters)

# Algorithm
## UUID

* Hash based on MAC address + Current datetime 
  * 36 characters: 32 alphanumeric characters and 4 hyphens
  * The granularity of current datetime: microseconds. Lots of collision
* Pros
  * Pure random
  * Not guessable and reversable
* Cons
  * Too long
  * Key duplication if only use 6 prefix characters

## Hash - MD5 / SHA-256 / Murmur / Jenkins

* Traditional Crypto hash function: MD5 and SHA-1
  * Secure but slow
* Fast hash function: Murmur and Jenkins
  * Performance
  * Have 32-, 64-, and 128-bit variants available
* Pros
  * Not guessable and reverserable
* Cons
  * Too long
  * Key duplication if only use 6 prefix characters
  * Not random -&gt; hash \(current time or UUID + url\)
* Pros
  * No need to write additional hash function, easy to implement
  * Are randomly distributed
  * Support URL clean

| Problem | Possible solution |
| :--- | :--- |
| Not short enough \(At least 4 bytes\) | Only retrieve first 4-5 digit of the hash result |
| Collision cannot be avoided | Use long\_url + timestamp as hash argument, if conflict happens, try again \(timestamp changes\) -&gt; multiple attempts, highly possible conflicts when data is big |
| Slow |  |

## Encoding - Base10 / Base62 / Base64

* Retention period varies with base radix. For example, assume 500M new records per month
  * If length == 8, 62^8 ~ 200 trillion ~ 33333 years
  * If length == 7, 62^7 ~ 3 trillion ~ 600 years
  * If length == 6, 62^6 ~ 57 B ~ 10 years

| Encoding | Base10 | Base62 | Base64 |
| :--- | :--- | :--- | :--- |
| Year | 36,500,000 | 36,500,000 |  |
| Usable characters | \[0-9\] | \[0-9a-zA-Z\] | \[0-9a-zA-Z+/=\] |
| Encoding length | 8 | 5 |  |

* Pros:
  * Shorter URL compared with hash
  * No collision
  * Simple computation
  * Easy to generate url without duplication \(increment id\)
  * Advantages of Base64 vs Base62
* Cons:
  * Too long, reversable if directly apply base62 to URL

## Escape invalid characters

* For example, Base64 = 
