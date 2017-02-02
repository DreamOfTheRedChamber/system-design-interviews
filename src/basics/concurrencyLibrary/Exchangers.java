package basics.concurrencyLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

///**
// * 	With the help of Exchanger -> two threads can exchange objects
// * 		
// *     exchange() -> exchanging objects is done via one of the two exchange() methods
// *
// */

class FillingWorker implements Runnable
{

	private List<Integer> currentBuffer;
	private int counter;
	private Exchanger<List<Integer>> exchanger;

	public FillingWorker (Exchanger<List<Integer>> exchanger )
	{
		this.currentBuffer = new ArrayList<>();
		this.exchanger = exchanger;
		this.counter = 0;
	}

	@Override
	public void run()
	{

		while (true)
		{

			if ( currentBuffer.size() == 10 )
			{
				System.out.println( "FillingWorker handed the buffer to EmptyingWorker..." + currentBuffer.size() );
				try
				{
					currentBuffer = exchanger.exchange( currentBuffer );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			}

			this.currentBuffer.add( counter++ );

			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
}

class EmptyingWorker implements Runnable
{

	private List<Integer> currentBuffer;
	private Exchanger<List<Integer>> exchanger;

	public EmptyingWorker (Exchanger<List<Integer>> exchanger )
	{
		this.currentBuffer = new ArrayList<>();
		this.exchanger = exchanger;

		currentBuffer.add( 10 );
		currentBuffer.add( 20 );
		currentBuffer.add( 30 );
	}

	@Override
	public void run()
	{

		while (true)
		{

			if ( currentBuffer.isEmpty() )
			{
				System.out.println( "EmptyingWorker handed the buffer to FillingWorker..." + currentBuffer.size() );
				try
				{
					currentBuffer = exchanger.exchange( currentBuffer );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			}

			this.currentBuffer.remove( 0 );

			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
}

class Exchangers
{

	public static void main(String[] args)
	{

		Exchanger<List<Integer>> exchanger = new Exchanger<>();

		new Thread( new FillingWorker( exchanger ) ).start();
		new Thread( new EmptyingWorker( exchanger ) ).start();

	}
}
