package designThreadSafeEntity.boundedBlockingQueue;

/**
 * 
 * thread safe
 *
 * not handle enqueue finds a full array
 * not handle dequeue finds an empty array
 */

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
