
- [Use cases](#use-cases)
- [Functional requirements](#functional-requirements)
  - [Core](#core)
  - [Optional](#optional)
  - [Non functional](#non-functional)
    - [High performance](#high-performance)
    - [High availability](#high-availability)
    - [Not predictable](#not-predictable)

# Use cases
* Data analysis like click events, user sources.
* Short url length to fit social media content limit \(140 characters\).
* Avoid the website is blacklisted by domain name.

# Functional requirements
## Core
* Shortening: Take a url and return a much shorter url. 
* Redirection: Take a short url and redirect to the original url. 

## Optional
* Custom url: Allow the users to pick custom shortened url. 
* Analytics: Usage statistics for site owner. 
  * Ex: How many people clicked the shortened url in the last day.
  * What were user locations
* What if two people try to shorten the same URL?
  * Each url can have multiple tiny urls 
* URL is not guessable? 
  * Yes
* Needs original url validation
  * No
* Automatic link expiration
* Manual link removal
* UI vs API

## Non functional

### High performance
* 80% latency smaller than 5ms, 99% latency smaller than 20ms, average latency smaller than 10ms.
* Our system is similar to DNS resolution, higher latency on URL shortener is similar to a failure to resolve. 

### High availability
* It should be high available. 

### Not predictable
* Short url should be not predictable to avoid hacking and leaking important information. 
