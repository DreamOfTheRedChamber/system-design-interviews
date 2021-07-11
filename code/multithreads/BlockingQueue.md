
- [Blocking queue](#blocking-queue)
  - [Non thread safe queue](#non-thread-safe-queue)
  - [Synchronized methods / blocks](#synchronized-methods--blocks)
  - [Wait / Notify](#wait--notify)

## Blocking queue

### Non thread safe queue

```java
public class ClassicQueue
{
	public static final int QSIZE = 10;
	
	// next slot to deque, empty slot position
	int head = 0, tail = 0;
	
	// array of T items
	int[] items = new int[QSIZE];
	
	public void enq( int x )
	{
		items[(tail++) % QSIZE] = x;
	}
	
	public int deq()
	{
		return items[(head++) % QSIZE];
	}
}
```

### Synchronized methods / blocks

```java

public class SyncMethodQueue
{
	public static final int QSIZE = 10;
	
	// next slot to deque, empty slot position
	int head = 0, tail = 0;
	
	// array of T items
	int[] items = new int[QSIZE];
	
	public synchronized void enq( int x )
	{
		items[(tail++) % QSIZE] = x;
	}
	
	public synchronized int deq()
	{
		return items[(head++) % QSIZE];
	}
}

public class SyncBlockQueue
{
	public static final int QSIZE = 10;
	
	// next slot to deque, empty slot position
	int head = 0, tail = 0;
	
	// array of T items
	int[] items = new int[QSIZE];
	
	public void enq( int x )
	{
		synchronized( this )
		{
			items[(tail++) % QSIZE] = x;
		}
	}
	
	public int deq()
	{
		synchronized( this )
		{
			return items[(head++) % QSIZE];
		}
	}
}
```

### Wait / Notify
