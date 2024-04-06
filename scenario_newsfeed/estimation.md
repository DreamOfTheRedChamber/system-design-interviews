## Storage

* Take the example of Friendster with 75 million DAU 

```
// Write amplification
// 1. When a user creates a post, the system will amplify writes to his/her friends (assume 150), 20 percent users create 3 posts per day
75 M * 0.2 * 150 * 3 ~ 7.5 billion

// 2. If distributed into 10 hours, then random write RPS will be 
7.5 billion / (3600 * 10) = 200K / s
```
