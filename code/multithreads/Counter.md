
- [Thread safe counter](#thread-safe-counter)
  - [With synchronized blocks / methods](#with-synchronized-blocks--methods)
  - [With ReentrantLock](#with-reentrantlock)
  - [Atomic class](#atomic-class)

## Thread safe counter

### With synchronized blocks / methods

```java
public class SynchronizedBlocksCounter
{
	private int value;
	
	public synchronized int get()
	{
		synchronized ( this )
		{
			return value;
		}
	}
	
	public synchronized void increment()
	{
		synchronized ( this )
		{
			value++;
		}
	}
}

public class SynchronizedMethodsCounter
{
	private int value;
	
	public synchronized int get()
	{
		return value;
	}
	
	public synchronized void increment()
	{
		value++;
	}
}
```

### With ReentrantLock

```java
public class ReentrantLockCounter
{
	private final Lock lock = new ReentrantLock();
	private int value;
	
	public int get()
	{
		try
		{
			lock.lock();
			return value;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public synchronized void increment()
	{
		try
		{
			lock.lock();
			value++;
		}
		finally
		{
			lock.unlock();
		}
	}
}

public class ReadWriteLockCounter
{
	private int count;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public void increment()
	{
		try
		{
			lock.writeLock().lock();
			count++;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	public int getCount()
	{
		try
		{
			lock.readLock().lock();
			return count;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}
}
```

### Atomic class

```java
public class AtomicVariableCounter
{
	private AtomicInteger c = new AtomicInteger(0);
	
	public void increment()
	{
		c.incrementAndGet();
	}
	
	public int getCount()
	{
		return c.get();
	}
}
```