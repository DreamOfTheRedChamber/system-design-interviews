package basics.volatileKeyword;

/**
 *  This class demonstrates where volatile is not suitable when multiple writers exist.  
 */

public class MultipleWriters
{
	public static volatile int race = 0;
	private static final int THREADS_COUNT = 20;
	
	public static void increase()
	{
		// main memory -> work memory, work memory increment, write back to main memory
		// volatile could guarantee it reads the latest value, but could not guarantee while it does increment, other threads do not increment on the old latest value and increment as well.
		race++; 
	}
	
	public static void main( String[] args )
	{
		Thread[] threads = new Thread[THREADS_COUNT];
		
		for ( int i = 0; i < THREADS_COUNT; ++i )
		{
			threads[i] = new Thread( new Runnable() {
				@Override
				public void run()
				{
					for ( int j = 0; j < 10000; ++j )
					{
						increase();
					}
				}
			} );
			threads[i].start( );
		}
		
		while ( Thread.activeCount( ) > 1 )
		{
			Thread.yield( );
		}
		
		// race value will not be 200,000
		// 
		System.out.println( race );
	}
}
