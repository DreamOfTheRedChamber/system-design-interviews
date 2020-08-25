package designThreadSafeEntity.boundedBlockingQueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Solution 2 for "lost wakeup" bug: 
 *  How to fix: 
 *    1. not use notify only when count == 0 inside enque(), use it each time count is changed
 *    2. use two separate locks to awake waiting enqueue threads/dequeue threads
 * */

public class SyncMethodQueueWaitNotifyImprove2<T>
{
	// concurrency control
	private final Lock lock = new ReentrantLock();
	// Use two condition variables to reduce spurious wake up
	private final Condition notFull = lock.newCondition();
	private final Condition notEmpty = lock.newCondition();
	
	// array-based queue
	private final T[] items;
	private int tail;
	private int head;
	private int count;
	
	@SuppressWarnings("unchecked")
	public SyncMethodQueueWaitNotifyImprove2( int capacity )
	{
		items = ( T[] ) new Object[capacity];
	}

	// blocked if queue is full
	public void enq( T x ) throws InterruptedException
	{
		try
		{
			lock.lock();
			while( count == items.length )
			{
				notFull.await();
			}
			items[tail++] = x;
			if ( tail == items.length )
			{
				tail = 0;
			}
			count++;
			notEmpty.signal(); // 
		}
		finally
		{
			lock.unlock();
		}
	}
	
	// blocked if queue is empty
	public T deq() throws InterruptedException
	{
		try
		{
			while ( count == 0 )
			{
				notEmpty.await();
			}
			T x = items[head++];
			if ( head == items.length )
			{
				head = 0;
			}
			count--;
			notFull.signal(); // will wait up the await() threads inside enque
			return x;			
		}
		finally
		{
			lock.unlock();
		}
	}
}
