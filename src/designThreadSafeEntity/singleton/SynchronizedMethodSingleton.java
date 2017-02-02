package designThreadSafeEntity.singleton;

/**
 * 
 * threadsafe
 */

public class SynchronizedMethodSingleton
{
	private static SynchronizedMethodSingleton instance;
	private SynchronizedMethodSingleton(){}
	
	// use mutex to protect object creation
	public synchronized static SynchronizedMethodSingleton getInstance()
	{
		if ( instance == null )
		{
			instance = new SynchronizedMethodSingleton();
		}
		return instance;
	}
	
}
