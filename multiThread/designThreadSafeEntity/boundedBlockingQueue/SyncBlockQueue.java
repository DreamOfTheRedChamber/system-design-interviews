package designThreadSafeEntity.boundedBlockingQueue;

/**
 * 
 * thread safe
 * not handle enqueue finds a full array
 * not handle dequeue finds an empty array
 */

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
