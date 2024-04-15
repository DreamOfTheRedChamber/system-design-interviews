- [AP model - Redis SetNX](#ap-model---redis-setnx)
  - [Ideas](#ideas)
  - [Example](#example)
    - [Initial implementation](#initial-implementation)
    - [Encapsulated implementation](#encapsulated-implementation)
  - [Pros](#pros)
  - [Cons](#cons)
    - [Limited use cases](#limited-use-cases)
  - [Link to history on RedLock](#link-to-history-on-redlock)
  - [Redisson](#redisson)
  - [Optimistic / Pessimistic lock](#optimistic--pessimistic-lock)

# AP model - Redis SetNX

## Ideas

* SET resource\_name my\_random\_value NX PX 30000
  * resource\_name: key
  * my\_random\_value: UUID, used for validation when releasing lock among different threads. Only release lock if the random\_value is the same.
  * NX: succeed only if key does not exist; Otherwise fail the operation. Use the atomic property of NX to guarantee that only one client could configure it successfully.
  * PX: automatic expiration time in case there are some exceptions happening

```text
// Flowchart for potential problems without random_value 

  ┌─────────────┐   ┌─────────────┐  ┌─────────────┐                                    ┌ ─ ─ ─ ─ ─ ─ ┐
  │             │   │  A execute  │  │  A's lock   │                                      A releases   
  │ A get lock  │   │    task     │  │   expire    │                                    │  B's lock   │
  │             │   │             │  │             │                                                   
  └─────────────┘   └─────────────┘  └─────────────┘                                    └ ─ ─ ─ ─ ─ ─ ┘

─────────────────────────────────────────────────────────────────────────────────────────────────────▶ 

                                                     ┌─────────────┐   ┌─────────────┐                 
                                                     │             │   │  B execute  │                 
                                                     │ B get lock  │   │    task     │                 
                                                     │             │   │             │                 
                                                     └─────────────┘   └─────────────┘
```

```text
// Script to release the lock with Lua script

if redis.call("get", KEYS[1]) == ARGV[1] then
    return redis.call("del", KEYS[1])
else
    return 0
end
```

## Example

### Initial implementation

```java
@RestController
@Slf4j
public class RedisLockController {
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("redisLock")
    public String redisLock(){
        log.info("Enter the method！");
        string key = "redisKey";
        string value = UUID.randomUUID().toString();

        // set up redis connection
        RedisCallBack<Boolean> redisCallback = connection -> {
            // Set up NX
            RedisStringCommands.SetOption setOption = RedisStringCommands.SetOption.IfAbsent();

            // Set up expiration time
            Expiration expiration = Expiration.seconds(30);
            byte[] redisKey = redisTemplate.getKeySerializer().serialize(key);
            byte[] redisValue = redisTemplate.getValueSerializer().serialize(value);

            Boolean result = connection.set(redisKey, redisValue, expiration, setOption);
            return result;
        };

        // Get distributed lock
        Boolean lock = (Boolean) redisTemplate.execute(redisCallback);
        if (lock)
        {
            log.info("entered the lock！！");
            Thread.sleep(15000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            String script = "if redis.call(\"get\", KEYS[1]) == ARGV[1] then\n"+
            "   return redis.call(\"del\", KEYS[1])" +
            "else\n" +
            "   return 0\n" +
            "end";
            RedisScrit<Boolean> redisScript = RedisScript.of(script, Boolean.class)
            List<String> keys = Arrays.asList(key);
            boolean result = redisTemplate.execute(redisScript, keys, value);
            // finished releasing lock
            e.printStackTrace();
        }
        return "finished executing method";
    }
}
```

### Encapsulated implementation

```java
@RestController
@Slf4j
public class RedisLockController {
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("redisLock")
    public String redisLock(){
        log.info("Enter the method！");
        RedisLock redisLock = new RedisLock(redisTemplate, key: "redisKey", expireTime: 30);

        if (redisLock.getLock())
        {
            log.info("entered the lock！！");
            Thread.sleep(15000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            boolean result = redisLock.unLock();
            log.info("the result of releasing lock" + result);
        }
        return "finished executing method";
    }
}
```

```java
// RedisLock.cs
@Slf4j
public class RedisLock implements AutoCloseable {

    private RedisTemplate redisTemplate;
    private String key;
    private String value;
    private int expireTime;

    public RedisLock(RedisTemplate redisTemplate,String key,int expireTime){
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.expireTime=expireTime;
        this.value = UUID.randomUUID().toString();
    }

    public boolean getLock(){
        RedisCallback<Boolean> redisCallback = connection -> {
            // configure NX
            RedisStringCommands.SetOption setOption = RedisStringCommands.SetOption.ifAbsent();
            // Configure expiration time
            Expiration expiration = Expiration.seconds(expireTime);
            // Serialize key
            byte[] redisKey = redisTemplate.getKeySerializer().serialize(key);
            // Serialize value
            byte[] redisValue = redisTemplate.getValueSerializer().serialize(value);
            // Execute SetNx operation
            Boolean result = connection.set(redisKey, redisValue, expiration, setOption);
            return result;
        };

        // Get distributed lock
        Boolean lock = (Boolean)redisTemplate.execute(redisCallback);
        return lock;
    }

    public boolean unLock() {
        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";
        RedisScript<Boolean> redisScript = RedisScript.of(script,Boolean.class);
        List<String> keys = Arrays.asList(key);

        Boolean result = (Boolean)redisTemplate.execute(redisScript, keys, value);
        log.info("释放锁的结果："+result);
        return result;
    }


    @Override
    public void close() throws Exception {
        unLock();
    }
}
```

## Pros

* Lock is stored in memory. No need to access disk

## Cons

### Limited use cases

* Only applicable for efficiency use cases, not for correctness use cases.
  * Efficiency
    * You could use a single Redis instance, of course you will drop some locks if the power suddenly goes out on your Redis node, or something else goes wrong. But if you’re only using the locks as an efficiency optimization, and the crashes don’t happen too often, that’s no big deal. This “no big deal” scenario is where Redis shines. At least if you’re relying on a single Redis instance, it is clear to everyone who looks at the system that the locks are approximate, and only to be used for non-critical purposes.
    * Add on top of the single application case, you could use master-slave setup for high availability. 
  * Correctness
    * A simple master - slave setup won't work. Think about the following scenario: 
      1. Client A writes an entry A to master. 
      2. Master dies before the asynchronous replication of the write operation reaches slave. 
      3. The slave becomes the master
      4. Client B writes the same entry A to original salve \(current master\)
      5. Now A and B share the same lock.
    * You will need to rely on Redlock. However, there are [some concerns](https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html) about it. To summarize:
      * Redlock does not have any facility to generate fencing tokens. And it is not straightforward to repurpose Redlock for generating fencing tokens. 
        * Relying on expiration time to avoid deadlock is not reliable. 
          * What if the lock owner dies? The lock will be held forever and we could be in a deadlock. To prevent this issue Redis will set an expiration time on the lock, so the lock will be auto-released. However, if the time expires before the task handled by the owner isn't yet finish, another microservice can acquire the lock, and both lock holders can now release the lock causing inconsistency. 
          * A fencing token needed to be used to avoid race conditions. Please see [this post](https://medium.com/@davidecerbo/everything-i-know-about-distributed-locks-2bf54de2df71) for details. 
      * Redlock depends on a lot of timing assumptions
        1. All Redis nodes hold keys for approximately the right length of time before expiring
        2. The network delay is small compared to the expiry duration
        3. Process pauses are much shorter than the expiry duration

## Link to history on RedLock

* Typical failures causing [failures of distributed locks](https://redislabs.com/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-2-distributed-locking/6-2-2-simple-locks/)
* What Redlock tries to solve?
  * The simplest way to use Redis to lock a resource is to create a key in an instance. The key is usually created with a limited time to live, using the Redis expires feature, so that eventually it will get released \(property 2 in our list\). When the client needs to release the resource, it deletes the key.
  * Superficially this works well, but there is a problem: this is a single point of failure in our architecture. What happens if the Redis master goes down? Well, let’s add a slave! And use it if the master is unavailable. This is unfortunately not viable. By doing so we can’t implement our safety property of mutual exclusion, because Redis replication is asynchronous.
  * There is an obvious race condition with this model:
    * Client A acquires the lock in the master.
    * The master crashes before the write to the key is transmitted to the slave.
    * The slave gets promoted to master.
    * Client B acquires the lock to the same resource A already holds a lock for. SAFETY VIOLATION!
* How to implement distributed lock with Redis, an algorithm called [RedLock](https://redis.io/topics/distlock)
  * How to implement it in a single instance case
  * How to extend the single instance algorithm to cluster
* [A hot debate on the security perspective of RedLock algorithm](http://zhangtielei.com/posts/blog-redlock-reasoning.html).

## Redisson

* Relationship with Redis could be thought as similar to Curator to Zookeeper

## Optimistic / Pessimistic lock

* Pessimistic lock - Fencing token

![MySQL HA github](./..gitbook/assets/monitorSystem_HealthCheck_distributedlock_fencingToken.png)

* Industrial implementation -ShedLock

