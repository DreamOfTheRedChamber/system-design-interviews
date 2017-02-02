package designThreadSafeEntity.boundedBlockingQueue;

/**
 * attempt: handle queue full case: 
 * solution: see comments below
 * result: deadlock
 *
 */

public class SyncMethodQueueWaitWhileloop
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
		// busy waiting while the array is full
		// inside synchronized block, other threads will not be able to change tail/head
		// deadlock
		while ( count == QSIZE )
		{	
		}
		count++;		
		items[(tail++) % QSIZE] = x;
		if ( tail == QSIZE )
		{
			tail = 0;
		}
	}
	
	// TODO: deque follows the same logic as enq, will finish later
	public synchronized int deq()
	{
		count--;
		return items[(head++) % QSIZE];
	}
}
