package basics.mutex;

/**
 * when synchronized keyword is applied on static variables, the lock is class level and no longer tied to a thread.
 * this could guarantee that only one thread can access foo() all instances of A
 * 
 * 
 * Simplified abstraction of the scenario: how to guarantee T2 will be blocked in the following case
 * 
 * class A {
 * 	public void foo();
 * }
 * 
 * A a1 = new A();
 * A a2 = new A();
 * a1.foo(); // T1
 * a2.foo(); // T2 blocked
*/

public class SynchronizedObjectsClassLevel
{
	private static final long SLEEP_INTERVAL_MS = 5000;

	// 
	private static final Object lock = new Object();
	
	private void foo()
	{
		synchronized ( lock )
		{
			System.out.println("Inside foo");
			
			try
			{
				Thread.sleep( SLEEP_INTERVAL_MS );
			}
			catch ( InterruptedException e )
			{
				
			}
		}
	}
	
	public void bar()
	{
		synchronized ( lock )
		{
			System.out.println("Inside bar");
		}
	}
	
	public void foobar() 
	{
		System.out.println( "Inside foobar" );
	}
	
	public static void main( String[] args ) throws InterruptedException
	{
		final SynchronizedObjectsClassLevel s = new SynchronizedObjectsClassLevel();
		final SynchronizedObjectsClassLevel s2 = new SynchronizedObjectsClassLevel();
		
		Thread T1 = new Thread( ( Runnable ) () -> { 
								s.foo( ); 
		} );
		
		Thread T2 = new Thread( ( Runnable ) () -> { 
								s2.bar( ); 
		} );
		
		Thread T3 = new Thread( new Runnable() { 
			@Override
			public void run()
			{
				s.foo();
			}
		} );
		
		T1.start( );
		T2.start( );
		T3.start( );
		
		T1.join( );
		T2.join( );
		T3.join( );
	}
}
