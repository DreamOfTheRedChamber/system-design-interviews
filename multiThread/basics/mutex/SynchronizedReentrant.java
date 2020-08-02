package basics.mutex;

/**
 * synchronized method/block is reentrant to the same thread, but will block other threads
 */

public class SynchronizedReentrant
{
	public void foo() throws InterruptedException
	{
		synchronized ( this )
		{
			System.out.println( "Inside foo" );
		
			// T1 will be able to enter bar(), T2 will be blocked.
			bar();
			
			Thread.sleep( 2000 );
		}
	}
	
	public void bar()
	{
		synchronized ( this )
		{
			System.out.print( "Inside bar" );
		}
	}
	
	public static void main( String[] args ) throws InterruptedException
	{
		final SynchronizedReentrant s = new SynchronizedReentrant();
		
		Thread T1 = new Thread( new Runnable() {
			@Override
			public void run()
			{
				try
				{
					s.foo( );
				}
				catch ( InterruptedException e )
				{
					//
				}
			}
		} );
		
		Thread T2 = new Thread( new Runnable() {
			@Override
			public void run()
			{
				s.bar( );
			}
		} );
		
		T1.start( );
		T1.join( );
		
		T2.start( );
		T2.join( );
	}
}
