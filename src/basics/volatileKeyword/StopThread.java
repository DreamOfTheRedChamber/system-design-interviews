package basics.volatileKeyword;

import java.util.concurrent.TimeUnit;

/**
 * stop an infinite thread with volatile variables. Each time background thread tries to read stopRequested boolean variable, it will load it again from main memory. 
 */

public class StopThread
{
	private static volatile boolean stopRequested;
	
	public static void main ( String[] args ) throws InterruptedException
	{
		Thread backgroundThread = new Thread( new Runnable() {
			public void run()
			{
				int i = 0;
				while ( !stopRequested )
				{
					// sleep means current thread will enter timedwaiting status. It will release CPU for other threads to use and context switch will occur. variables will be reload from main memory after context switch
					// Thread.sleep(1000); 

					// background thread does io. current thread will enter blocked status. Context switch will occur. variables will be reloaded from main memory after context switch.
					// System.out.println(i);
					
					i++;
				}
			}
		} );
		
		backgroundThread.start( );
		TimeUnit.SECONDS.sleep( 1 );
		stopRequested = true;
	}
}
