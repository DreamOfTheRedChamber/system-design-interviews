package basics.mutex;

/**
 * when synchronized keyword is applied before a method
 * it means for the current instance, there can be only one thread to visit the synchronized methods simultaneously
 * class A {
 * 	public synchronized void foo() {...}
 *  public synchronized void bar() {...}
 *  public void foobar() {...}
 * }
 * 
 * A a1 = new A();
 * T1: a1.foo()
 * T2: a1.bar(); // T2 is blocked
 * T3: a1.foobar(); // T3 is not blocked
 * */

public class SynchronizedMethods
{
	private static final long SLEEP_INTERVAL_MS = 5000;
	
	public synchronized void foo()
	{
		System.out.println( "Inside foo" );
		try
		{
			Thread.sleep( SLEEP_INTERVAL_MS );
		}
		catch ( InterruptedException e )
		{
			
		}
	}
	
	// bar() will be blocked until foo() finished
	public synchronized void bar() 
	{
		System.out.println( "Inside bar" );
	}
	
	// note: no synchronized keyword applied here
	public void foobar()
	{
		System.out.println( "Inside foo bar" );
	}
	
	public static void main( String[] args ) throws InterruptedException
	{
		final SynchronizedMethods s = new SynchronizedMethods();
		
		Thread T1 = new Thread( new Runnable() {
			@Override
			public void run()
			{
				s.foo( );
			}
		} );
		
		Thread T2 = new Thread( new Runnable() {
			@Override
			public void run()
			{
				s.bar( );
			}
		} );
		
		Thread T3 = new Thread( new Runnable() {
			@Override
			public void run()			
			{
				s.foobar( );
			}
		} );
		
		T1.start( );
		T2.start( );
		T3.start( );
	}
}
