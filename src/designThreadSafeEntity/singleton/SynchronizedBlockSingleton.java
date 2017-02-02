package designThreadSafeEntity.singleton;

/**
 * 
 * threadsafe
 */

public class SynchronizedBlockSingleton
{
	private static SynchronizedBlockSingleton instance;
	private SynchronizedBlockSingleton(){}
	
	public static SynchronizedBlockSingleton getInstance()
	{
		// use mutex to protect object creation
		synchronized ( SynchronizedBlockSingleton.class )
		{
			if ( instance == null )
			{
				instance = new SynchronizedBlockSingleton();
			}
			return instance;
		}
	}
	
}
