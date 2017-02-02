package basics.concurrencyLibrary;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * BlockingQueue -> an interface that represents a queue that is thread safe Put
 * items or take items from it ...
 * 
 * For example: one thread putting items into the queue and another thread
 * taking items from it at the same time !!! We can do it with producer-consumer
 * pattern !!!
 * 
 * put() putting items to the queue take() taking items from the queue
 * 
 */

class FirstWorker implements Runnable
{

	private BlockingQueue<String> blockingQueue;

	public FirstWorker (BlockingQueue<String> blockingQueue )
	{
		this.blockingQueue = blockingQueue;
	}

	@Override
	public void run()
	{
		try
		{
			blockingQueue.put( "A" );
			Thread.sleep( 1000 );
			blockingQueue.put( "B" );
			Thread.sleep( 1000 );
			blockingQueue.put( "C" );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}

class SecondWorker implements Runnable
{

	private BlockingQueue<String> blockingQueue;

	public SecondWorker (BlockingQueue<String> blockingQueue )
	{
		this.blockingQueue = blockingQueue;
	}

	@Override
	public void run()
	{
		try
		{
			System.out.println( blockingQueue.take() );
			System.out.println( blockingQueue.take() );
			System.out.println( blockingQueue.take() );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}

public class BlockingQueues
{

	public static void main(String[] args)
	{

		BlockingQueue<String> queue = new ArrayBlockingQueue<>( 10 );

		FirstWorker firstWorker = new FirstWorker( queue );
		SecondWorker secondWorker = new SecondWorker( queue );

		new Thread( firstWorker ).start();
		new Thread( secondWorker ).start();

	}
}
