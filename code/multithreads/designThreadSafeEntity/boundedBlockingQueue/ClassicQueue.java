package designThreadSafeEntity.boundedBlockingQueue;

/**
 * 
 * not thread safe
 */

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
