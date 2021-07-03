package basics.concurrencyLibrary;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A CyclicBarrier is used in situations where you want to create a group of
 * tasks to perform work in parallel + wait until they are all finished before
 * moving on to the next step -> something like join() -> something like
 * CountDownLatch
 * 
 * CountDownLatch: one-shot event CyclicBarrier: it can be reused over and over
 * again
 * 
 * + cyclicBarrier has a barrier action: a runnable, that will run automatically
 * when the count reaches 0 !!
 * 
 * new CyclicBArrier(N) -> N threads will wait for each other
 * 
 */

class MyBarrierAction implements Runnable
{

	@Override
	public void run()
	{
		System.out.println( "ALL TASKS ARE COMPLETED...runnable called" );
	}
}

class Worker2 implements Runnable
{

	private int id;
	private Random random;
	private CyclicBarrier cyclicBarrier;

	public Worker2 (int id, CyclicBarrier cyclicBarrier )
	{
		this.cyclicBarrier = cyclicBarrier;
		this.id = id;
		this.random = new Random();
	}

	@Override
	public void run()
	{
		doWork();
	}

	public void doWork()
	{
		try
		{
			System.out.println( "Thread with ID " + this.id + " starts working..." );
			Thread.sleep( this.random.nextInt( 2000 ) );
			System.out.println( "Thread with ID " + this.id + " finished working..." );
			cyclicBarrier.await();
		}
		catch ( InterruptedException | BrokenBarrierException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public String toString()
	{
		return "" + this.id;
	}
}

public class CyclicBarriers
{

	public static void main(String[] args)
	{

		ExecutorService executorService = Executors.newFixedThreadPool( 5 );
		CyclicBarrier cyclicBarrier = new CyclicBarrier( 5, new MyBarrierAction() );

		for ( int i = 0; i < 5; i++ )
			executorService.execute( new Worker2( i + 1, cyclicBarrier ) );

		executorService.shutdown();
	}
}
