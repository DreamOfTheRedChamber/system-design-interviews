package designThreadSafeEntity.counter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockCounter
{
	private final Lock lock = new ReentrantLock();
	private int value;
	
	public int get()
	{
		try
		{
			lock.lock();
			return value;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public synchronized void increment()
	{
		try
		{
			lock.lock();
			value++;
		}
		finally
		{
			lock.unlock();
		}
	}
}
