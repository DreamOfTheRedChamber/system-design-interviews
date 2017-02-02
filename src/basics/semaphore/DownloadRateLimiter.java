package basics.semaphore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 
 * - semaphore maintains a set of permits - acquire() -> if a permit is
 * available then takes it - release() -> adds a permit
 * 
 * Semaphore just keeps a count of the number available Semaphore(int permits,
 * boolean fair) !!!
 */

// use enum to create a thread-safe singleton class
enum Downloader {

	INSTANCE;

	private Semaphore semaphore = new Semaphore( 3, true );

	public void downloadData()
	{
		try
		{
			semaphore.acquire();
			download();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			semaphore.release();
		}
	}

	private void download()
	{
		System.out.println( "Downloading data..." );
		try
		{
			Thread.sleep( 1000 );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}

public class DownloadRateLimiter
{
	public static void main(String[] args)
	{

		ExecutorService executorService = Executors.newCachedThreadPool();

		for ( int i = 0; i < 12; i++ )
		{
			executorService.execute( new Runnable() {
				public void run()
				{
					Downloader.INSTANCE.downloadData();
				}
			} );
		}

		executorService.shutdown();
	}
}
