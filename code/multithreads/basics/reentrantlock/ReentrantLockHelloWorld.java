package basics.reentrantlock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * Lock interface
 * 
 * Advantages over synchronized keyword:
 * 	- More flexible APIs: tryLock() / lockInterruptibly() / tryLock(10, TimeUnits.SECONDS)
 *    tryLock(): If the lock is not free -> with tryLock(*), the thread will not be blocked. As a result, there will be no unnecessary waiting
 *    tryLock(10, TimeUnits.SECONDS): timeout lock
 *    lockInterruptibly(): acquires the lock unless the current thread is interrupted
 *    on the contrary, synchronized keyword might block for infinite time.
 *  - Fairness: new ReentrantLock(true)
 *    guarantess that every thread will get the lock. 
 *    on the contrary, for synchronized keyword, after unlocking every other thread can get the lock. There might be thread waiting for infinite time, starving
 * 
 * Disadvantages:
 *  - Ugly nested try catch blocks
 */

public class ReentrantLockHelloWorld
{

	private static int counter = 0;
	private static Lock lock = new ReentrantLock();

	public static void increment()
	{
		lock.lock();
		counter++;
		lock.unlock();
	}

	public static void firstThread()
	{
		for ( int i = 0; i < 1000; i++ )
		{
			increment();
		}
	}

	public static void secondThread()
	{
		for ( int i = 0; i < 1000; i++ )
		{
			increment();
		}
	}

	public static void main(String[] args)
	{

		Thread t1 = new Thread( new Runnable() {
			public void run()
			{
				firstThread();
			}
		} );

		Thread t2 = new Thread( new Runnable() {
			public void run()
			{
				firstThread();
			}
		} );

		t1.start();
		t2.start();

		try
		{
			t1.join();
			t2.join();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}

		System.out.println( counter );

	}
}
