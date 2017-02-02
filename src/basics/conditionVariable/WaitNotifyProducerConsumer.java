package basics.conditionVariable;

import java.util.ArrayList;
import java.util.List;

class WaitNotifyProcessor
{

	private List<Integer> list = new ArrayList<>();
	private final int LIMIT = 10;
	private Object lock = new Object();

	public void produce() throws InterruptedException
	{

		int value = 0;

		while (true)
		{

			synchronized (lock)
			{

				while (this.list.size() == LIMIT)
				{
					lock.wait();
				}

				this.list.add( value );
				System.out.println( "Producer method added " + value );
				value++;

				// notify consumer
				lock.notify();
			}
		}
	}

	public void consume() throws InterruptedException
	{

		while (true)
		{

			synchronized (lock)
			{

				// cannot remove any if list is empty
				while (this.list.size() == 0)
				{
					lock.wait();
				}

				int value = this.list.remove( 0 );
				System.out.println( "Consumer method removed " + value );

				// notify producer
				lock.notify();
			}

			Thread.sleep( 1000 );
		}
	}
}

public class WaitNotifyProducerConsumer
{
	static WaitNotifyProcessor processor = new WaitNotifyProcessor();

	public static void main(String[] args)
	{

		Thread t1 = new Thread( new Runnable() {
			public void run()
			{
				try
				{
					processor.produce();
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		} );

		Thread t2 = new Thread( new Runnable() {
			public void run()
			{
				try
				{
					processor.consume();
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		} );

		t1.start();
		t2.start();
	}
}
