- [CP model](#cp-model)
  - [Database](#database)
  - [Zookeeper](#zookeeper)
  - [etcd](#etcd)

# CP model

## Database

**Ideas**

* Use database locks
  * Table lock 
  * Unique index
* "SELECT ... For UPDATE" adds a row lock on record
  * e.g. SELECT \* FROM distributed\_lock WHERE business\_code='demo' FOR UPDATE

**Pros and Cons**

* Pros:
  * Easy to build
* Cons: 
  * Big pressure on database if there are high number of concurrent requests. Recommend to separate the business logic DB and lock DB

**Example**

```text
// DistributeLockMapper.xml
  <select id="selectDistributeLock" resultType="com.example.distributelock.model.DistributeLock">
    select * from distribute_lock
    where business_code = #{businessCode,jdbcType=VARCHAR}
    for update
  </select>

// DemoController.java
@RestController
@Slf4j
public class DemoController 
{
    @Resource
    private DistributeLockMapper distributeLockMapper;

    @RequestMapping("singleLock")
    @Transactional(rollbackFor = Exception.class)
    public String singleLock() throws Exception 
    {
        DistributeLock distributeLock = distributeLockMapper.selectDistributeLock("demo");
        if (distributeLock==null) throw new Exception("cannot get distributed lock");
        try 
        {
            Thread.sleep(20000);
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
        return "Finished execution！";
    }
}
```

## Zookeeper

![Distributed lock](.gitbook/assets/zookeeper_distributedlock.png)

* How will the node be deleted:
  * Client deletes the node proactively
    * How will the previous node get changed?
      1. Watch mechanism get -w /gupao. 
  * Too many notifications:
    * Each node only needs to monitor the previous node

```text
@Slf4j
public class ZkLock implements AutoCloseable, Watcher 
{

    private ZooKeeper zooKeeper;
    private String znode;

    public ZkLock() throws IOException 
    {
        this.zooKeeper = new ZooKeeper("localhost:2181",
                10000,this);
    }

    public boolean getLock(String businessCode) 
    {
        try 
        {
            // Create business root node, e.g. /root
            Stat stat = zooKeeper.exists("/" + businessCode, false);
            if (stat==null)
            {
                zooKeeper.create("/" + businessCode,businessCode.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                        CreateMode.PERSISTENT); 
            }

            // Create temporary sequential node  /order/order_00000001
            znode = zooKeeper.create("/" + businessCode + "/" + businessCode + "_", businessCode.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);

            // Get all nodes under business node
            List<String> childrenNodes = zooKeeper.getChildren("/" + businessCode, false);

            // Sort children nodes under root
            Collections.sort(childrenNodes);

            // Obtain the node which has the least sequential number
            String firstNode = childrenNodes.get(0);

            // If the node created is the first one, then get the lock
            if (znode.endsWith(firstNode))
            {
                return true;
            }

            // If not the first child node, then monitor the previous node
            String lastNode = firstNode;
            for (String node:childrenNodes)
            {
                if (znode.endsWith(node))
                {
                    // watch parameter is implemented in the process method below
                    zooKeeper.exists("/"+businessCode+"/"+lastNode, watch: true);
                    break;
                }
                else 
                {
                    lastNode = node;
                }
            }

            // Wait for the previous node to release
            // This is 
            synchronized (this)
            {
                wait();
            }

            return true;

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void close() throws Exception 
    {
        zooKeeper.delete(znode, -1); // path, version: version is to avoid deleting wrong node. Passing -1 here because it is not used before at all
        zooKeeper.close();
        log.info("I have unlocked！");
    }

    @Override
    public void process(WatchedEvent event) 
    {
        // Only get notification when the previous node get deleted. 
        if (event.getType() == Event.EventType.NodeDeleted)
        {
            synchronized (this)
            {
                notify();
            }
        }
    }
}

@Slf4j
public class ZookeeperController 
{
    @Autowired
    private CuratorFramework client;

    @RequestMapping("zkLock")
    public String zookeeperLock()
    {
        log.info("entered method！");
        try (ZkLock zkLock = new ZkLock()) 
        {
            if (zkLock.getLock("order"))
            {
                log.info("get the lock");
                Thread.sleep(10000);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        log.info("finish method execution！");
        return "finish method execution！";
    }
}
```

**Curator**

* Motivation: Curator encapsulates the one-time watch logic so easier to use. 
  * There are three methods which could set watcher: GetData\(\); getChildren\(\); exists\(\). 
  * Whenever there is a change to the watched data, the result will be returned to client. 
  * However, the watcher could be used only once. 

**Implementation**

```text
@RestController
@Slf4j
public class ZookeeperController {
    @Autowired
    private CuratorFramework client;

    @RequestMapping("curatorLock")
    public String curatorLock()
    {
        log.info("Entered method！");
        InterProcessMutex lock = new InterProcessMutex(client, "/order");
        try
        {            
            if (lock.acquire(30, TimeUnit.SECONDS)) // 
            {
                log.info("Get the lock！！");
                Thread.sleep(10000);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally 
        {
            try 
            {
                log.info("Release lock！！");
                lock.release();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
        log.info("method finish execution！");
        return "method finish execution！";
    }
}
```

## etcd

**Operations**

1. business logic layer apply for lock by providing \(key, ttl\)
2. etcd will generate uuid, and write \(key, uuid, ttl\) into etcd
3. etcd will check whether the key already exist. If no, then write it inside. 
4. After getting the lock, the heartbeat thread starts and heartbeat duration is ttl/3. It will compare and swap uuid to refresh lock

```text
// acquire lock
curl http://127.0.0.1:2379/v2/keys/foo -XPUT -d value=bar -d ttl=5 prevExist=false

// renew lock based on CAS
curl http://127.0.0.1；2379/v2/keys/foo?prevValue=prev_uuid -XPUT -d ttl=5 -d refresh=true -d prevExist=true

// delete lock
curl http://10.10.0.21:2379/v2/keys/foo?prevValue=prev_uuid -XDELETE
```
