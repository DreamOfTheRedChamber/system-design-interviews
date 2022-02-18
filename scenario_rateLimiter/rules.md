- [Rules](#rules)
- [Protocols](#protocols)
  - [Response headers](#response-headers)

# Rules

* Using the example of lyft envoy: [https://github.com/envoyproxy/ratelimit](https://github.com/envoyproxy/ratelimit)

```
domain: auth descriptors:
- key: auth_type 
  Value: login 
  rate_limit:
    unit: minute 
    requests_per_unit: 5
```

# Protocols

## Response headers

* X-Ratelimit-Remaining: The remaining number of allowed requests within the window. 
* X-Ratelimit-Limit: It indicates how many calls the client can make per time window.
* X-Ratelimit-Retry-After: The number of seconds to wait until you can make a request again without being throttled.
* X-RateLimit-Reset: should contain a UNIX timestamp describing the moment when the limit will be reset

```
// Once the request quota is drained, the API should return a 429 Too Many Request response, with a helpful error message wrapped in the usual error envelope: 


X-RateLimit-Limit: 2000
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1404429213925
{
    "error": {
        "code": "bf-429",
        "message": "Request quota exceeded. Wait 3 minutes and try again.",
        "context": {
            "renewal": 1404429213925
        }
    }
}
```
