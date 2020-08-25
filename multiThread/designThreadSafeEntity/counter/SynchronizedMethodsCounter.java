package designThreadSafeEntity.counter;

/*
 * only one thread can enter get()/increment()
 * */

public class SynchronizedMethodsCounter
{
	private int value;
	
	public synchronized int get()
	{
		return value;
	}
	
	public synchronized void increment()
	{
		value++;
	}
}
