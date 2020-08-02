package designThreadSafeEntity.singleton;

/**
 * Volatile semantics
 *   changes to volatile variables are visible to other threads immediately
 *   volatile eliminate instruction reordering
 *   
 * Volatile use cases
 *   single writer
 *   the result does not depend on the current value
*/ 
public class SynchronizedDoubleCheckSingletonWithVolatile
{
	// volatile ensures object is fully constructed before assigning to instance
	private static volatile SynchronizedDoubleCheckSingletonWithVolatile instance;
	private SynchronizedDoubleCheckSingletonWithVolatile(){}
	
	public static SynchronizedDoubleCheckSingletonWithVolatile getInstance()
	{
		// only acquire lock after checking instance
		if ( instance == null )
		{
			synchronized ( SynchronizedBlockSingleton.class )
			{
				// Other threads may already create the object
				if ( instance == null )
				{
					instance = new SynchronizedDoubleCheckSingletonWithVolatile();					
				}
			}
		}
		return instance;
	}
	
}
