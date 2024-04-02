- [API contract](#api-contract)
  - [Long to short url](#long-to-short-url)
    - [Request payload](#request-payload)
    - [Response code](#response-code)
  - [Short to long url](#short-to-long-url)
    - [Request payload](#request-payload-1)
    - [Response code 301 vs 302](#response-code-301-vs-302)
- [API implementation (Base62 with globalId)](#api-implementation-base62-with-globalid)

# API contract
## Long to short url
### Request payload
* Post https://tinyurl.com/

```json
data:
{
    original_url: xxxx,
    expired_date: xxxx, 
    ip: xxxx
}
```

### Response code
* 200

## Short to long url
### Request payload
* Get https://tinyurl.com/xxxxx

### Response code 301 vs 302
* 301: Permanent redirection. 
* 302: Temporarily moved. 
* Comparison: 
  * Using a 302 response code, each time calling https://tinyurl.com/{shortened_Url} will call the server. It adds to the server load. However, if statistics about the short url is needed, then 301 will be better. 

# API implementation (Base62 with globalId)

```python
class UrlShortener:

    def __init__():
        # Suppose that the hash seed is based on the auto-increment global id
        self.globalID = 0
    
        # Suppose that we truncate the hashed url by the mapping between short and long url
        self.shortToLongMap = dict()

        # Hardcode constants
        self.base62Str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        self.shortLength = 6
        self.domainName = https://tinyurl.com/" 

    # Suppose that we need to store the url map    
    def longToShort(longUrl: str) -> str:
        self.globalID += 1
        
        currId = self.globalID
        shortened = ""
        while currId > 0:
            shortened = ""
            index = currId % 62
            shortened += self.base62Str[index] 
            currId = currId // 62
        
        if len(shortened) < self.shortLength:
            shortened = "0" * (self.shortLength - len(shortened)) + shortened

        self.shortToLongMap[shortened] = longUrl

        return self.domainName + shortened

    def shortToLong(shortUrl: str) -> str:
        shortened = shortUrl[len(self.domainName):]
        return self.shortToLongMap[shortUrl]

```

