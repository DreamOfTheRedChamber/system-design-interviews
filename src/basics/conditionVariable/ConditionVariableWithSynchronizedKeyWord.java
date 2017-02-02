package basics.conditionVariable;

/**
 * 
 * This example demonstrates how to use wait() and notify() to realize the semantics of join() function
 */

public class ConditionVariableWithSynchronizedKeyWord
{
	private static final long SLEEP_INTERVAL_MS = 1000;
	private boolean running = true;
	private Thread thread;
	
	public void start()
	{
		thread = new Thread( ( Runnable ) () -> {
			print( "Hello world" );
			try
			{
				Thread.sleep( SLEEP_INTERVAL_MS );
			}
			catch ( InterruptedException e )
			{
				Thread.currentThread( ).interrupt( );
			}
						
			synchronized ( thread )
			{
				running = false;
				
				// notify main thread
				// note: here could not this because this is inside an anonymous function
				ConditionVariableWithSynchronizedKeyWord.this.notify();
			}
		} );
		
		thread.start( );
	}
	
	public void join( ) throws InterruptedException 
	{
		synchronized ( thread )
		{
			while ( running )
			{
				print( "Waiting for the peer thread to finish." );
				wait(); // add thread to waiting queue of ConditionVariableExample object
			}
			print("Peer thread finished");
		}
	}
	
	private void print( String s)
	{
		System.out.println( s );
	}
	
	public static void main( String[] args ) throws InterruptedException
	{
		ConditionVariableWithSynchronizedKeyWord cve = new ConditionVariableWithSynchronizedKeyWord( );
		cve.start( );
		cve.join( );
	}
	
}
