package designThreadSafeEntity.boundedBlockingQueue;

/**
 * attempt: How to fix "lost wakeup" bug  
 *    
 * Solution 1 for "lost wakeup" bug: 
 *  Always use signalAll() and notifyAll() not signal() and notify()
 *  or use wait( timeout )
 *  
 * q.notify() 
 *   1. awakens one waiting thread
 *   2. which will reacquire lock & returns   
 * q.notifyAll()
 *   1. awakens all waiting threads
 *   2. which will reacquire lock & return
 *   
 * Result:
 *  but fixed but lots of unnecessary wakeups  
 */

public class SyncMethodQueueWaitNotifyImprove1
{
	public static final int QSIZE = 10;
	
	// next slot to deque, empty slot position
	int head = 0, tail = 0;
	
	// number of elements inside array
	int count = 0;
	
	// array of T items
	int[] items = new int[QSIZE];
		
	public synchronized void enq( int x )
	{
		// keep retest condition
		while ( count == QSIZE )
		{
			// release lock and sleep
			try
			{
				this.wait();
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		
		items[(tail++) % QSIZE] = x;
		if ( tail == QSIZE )
		{
			tail = 0;
		}

		// notify if queue was empty
		if ( count == 0 )
		{
			this.notifyAll();
		}
		count++;

	}
	
	// TODO: deque follows the same logic as enq, will finish later
	public synchronized int deq()
	{
		count--;
		return items[(head++) % QSIZE];
	}
}
