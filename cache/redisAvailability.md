## Availability - Sentinel

### Definition

* Sentinel is Redis' resiliency solution to standalone redis instance. 
  * See [Compare redis deployments](https://blog.octo.com/en/what-redis-deployment-do-you-need/) for details.
* It will monitor your master & slave instances, notify you about changed behaviour, handle automatic failover in case a master is down and act as a configuration provider, so your clients can find the current master instance.
* Redis sentinel was born in 2012 and first released when Redis 2.4 was stable. 

### Advanced concepts

* Initialization: Sentinel is a redis server running on a special mode
  * Sentinel will not load RDB or AOF file.
  * Sentinel will load a special set of Sentinel commands.
* Downstate
  * Subjective downstate: An SDOWN condition is reached when it does not receive a valid reply to PING requests for the number of seconds specified in the configuration as is-master-down-after-milliseconds parameter.
  * Objectively downstate: When enough Sentinels \(at least the number configured as the quorum parameter of the monitored master\) have an SDOWN condition, and get feedback from other Sentinels using the SENTINEL is-master-down-by-addr command.
* Sentinels and slaves auto discovery
  * You don't need to configure a list of other Sentinel addresses in every Sentinel instance you run, as Sentinel uses the Redis instances Pub/Sub capabilities in order to discover the other Sentinels that are monitoring the same masters and slaves. Similarly you don't need to configure what is the list of the slaves attached to a master, as Sentinel will auto discover this list querying Redis.
  * Process
    * Every Sentinel publishes a message to every monitored master and slave Pub/Sub channel **sentinel**:hello, every two seconds, announcing its presence with ip, port, runid.
    * Every Sentinel is subscribed to the Pub/Sub channel **sentinel**:hello of every master and slave, looking for unknown sentinels. When new sentinels are detected, they are added as sentinels of this master.
    * Hello messages also include the full current configuration of the master. If the receiving Sentinel has a configuration for a given master which is older than the one received, it updates to the new configuration immediately.
* Slave selection: Relies on [Raft protocol](http://thesecretlivesofdata.com/raft/)
  * The slave selection process evaluates the following information about slaves:
    * Disconnection time from the master.
    * Slave priority.
    * Replication offset processed.
    * Run ID.
  * A slave that is found to be disconnected from the master for more than ten times the configured master timeout \(down-after-milliseconds option\), plus the time the master is also not available from the point of view of the Sentinel doing the failover, is considered to be not suitable for the failover and is skipped.
  * The slave selection only considers the slaves that passed the above test, and sorts it based on the above criteria, in the following order.
    * The slaves are sorted by slave-priority as configured in the redis.conf file of the Redis instance. A lower priority will be preferred.
    * If the priority is the same, the replication offset processed by the slave is checked, and the slave that received more data from the master is selected.
    * If multiple slaves have the same priority and processed the same data from the master, a further check is performed, selecting the slave with the lexicographically smaller run ID. Having a lower run ID is not a real advantage for a slave, but is useful in order to make the process of slave selection more deterministic, instead of resorting to select a random slave.
* Sentinel's server cron operations:
  * Detect instance's objective and subjective downstate by sending PING commands
  * Automatically discover sentinel and slave nodes by subscribing to channel **sentinel**:hello
  * Leader selection and failover

### Algorithms and internals**

* Quorum
  * Use cases: Considering a master as objectively downstate; Authorizing the failover process
  * Quorum could be used to tune sentinel in two ways:
    * If a the quorum is set to a value smaller than the majority of Sentinels we deploy, we are basically making Sentinel more sensible to master failures, triggering a failover as soon as even just a minority of Sentinels is no longer able to talk with the master.
    * If a quorum is set to a value greater than the majority of Sentinels, we are making Sentinel able to failover only when there are a very large number \(larger than majority\) of well connected Sentinels which agree about the master being down.
* Configuratin epochs
  * Epoch is similar to Raft algorithm's term.
  * When a Sentinel is authorized, it gets a unique configuration epoch for the master it is failing over. This is a number that will be used to version the new configuration after the failover is completed. Because a majority agreed that a given version was assigned to a given Sentinel, no other Sentinel will be able to use it.
* [https://redis.io/topics/sentinel](https://redis.io/topics/sentinel)
