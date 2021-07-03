package designThreadSafeEntity.boundedBlockingQueue;

/**
 * attempt: handle queue full case: 
 * 
 * solution:
 * q.wait() performs the following operations
 *    1. releases lock on q
 *    2. sleeps ( gives up processor )
 *    3. awakens ( resumes running )
 *    4. reacquires lock and returns
 *    
 * result: "lost wakeup" bug because not enough threads awakended
 *       bug description: 
 *       1. Thread A and B both try to dequeue an item from an empty queue.
 *          Both blocked on notEmpty
 *       2. Producer C enqueues and signals notEmpty, waking A. Before A can acquire the lock, however, another producer D puts a second item in the queue, 
 *          and because the queue is not empty, it does not signal notEmpty
 *       3. Then A acquires the lock, removes the first item, but B, victim of a lost wakeup, waits forever even though there is an item in the buffer to be consumed.
 * 
 * how to fix the bug:
 *       1. refer to ~~Improved1.java for first solution
 *       2. refer to ~~Improved2.java for second solution
 */

public class SyncMethodQueueWaitNotify
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

		// this will result in lost wakeup problem
		if ( count == 0 )
		{
			notify();
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
