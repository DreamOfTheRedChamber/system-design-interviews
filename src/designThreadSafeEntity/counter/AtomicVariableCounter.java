package designThreadSafeEntity.counter;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicVariableCounter
{
	private AtomicInteger c = new AtomicInteger(0);
	
	public void increment()
	{
		c.incrementAndGet();
	}
	
	public int getCount()
	{
		return c.get();
	}
}
