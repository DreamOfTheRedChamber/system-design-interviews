
- [Blocking queue](#blocking-queue)
	- [Non thread safe queue](#non-thread-safe-queue)
	- [Synchronized methods / blocks](#synchronized-methods--blocks)
	- [Condition lock's impl - Wait / Notify](#condition-locks-impl---wait--notify)

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

### Condition lock's impl - Wait / Notify

```java

public class BlockedQueue<T>{
  final Lock lock =
    new ReentrantLock();
  // Condition variable: queue is not full
  final Condition notFull = lock.newCondition();
  // Condition variable: queue is not empty
  final Condition notEmpty = lock.newCondition();

  // enqueue
  void enq(T x) 
  {
    lock.lock();
    try 
	{
      while (queue is full)
	  {
        // Wait until queue is not full
        notFull.await();
      }  
	  // Enqueue operations
	  ...

	  // After enqueue, ready to dequeue
      notEmpty.signal();
    }
	finally 
	{
      lock.unlock();
    }
  }

  // dequeue
  void deq()
  {
    lock.lock();
    try 
	{
      while (queue is empty)
	  {
		// Wait until queue is not empty
        notEmpty.await();
      }  
	  // Dequeue operations
	  ...

	  // After dequeue, queue has capacity for enqueue
      notFull.signal();
    }
	finally 
	{
      lock.unlock();
    }  
  }
}
```