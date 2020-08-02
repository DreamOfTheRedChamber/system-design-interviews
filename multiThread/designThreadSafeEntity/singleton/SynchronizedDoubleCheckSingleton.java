package designThreadSafeEntity.singleton;

/**
 * 
 * although more efficient, non threadsafe
 * 
 * Many writes for a statement "instance = new SynchronizedDoubleBlockSingleton();"
 *   initialize instance: call the constructor, assign fields ( many writes )
 *   write to the instance field
 *   
 * Compiler may reorder writes:
 *    the write which initialize instance and the write to the instance field can be reordered
 * 
 * Returns a partially constructed object
 * Threads may read an uninitialized object
 */

public class SynchronizedDoubleCheckSingleton
{
	private static SynchronizedDoubleCheckSingleton instance;
	private SynchronizedDoubleCheckSingleton(){}
	
	public static SynchronizedDoubleCheckSingleton getInstance()
	{
		// only acquire lock after checking instance
		if ( instance == null )
		{
			synchronized ( SynchronizedBlockSingleton.class )
			{
				// Other threads may already create the object
				if ( instance == null )
				{
					instance = new SynchronizedDoubleCheckSingleton();					
				}
			}
		}
		return instance;
	}
	
}
