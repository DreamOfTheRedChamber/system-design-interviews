package designThreadSafeEntity.counter;

/*
 * only one thread can enter get()/increment()
 * */

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
