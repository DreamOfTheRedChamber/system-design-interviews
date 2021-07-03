package basics.createThread;

class RunnerRunnable implements Runnable
{

	@Override
	public void run()
	{
		for ( int i = 0; i < 5; i++ )
		{
			System.out.println("Hello: " + i + " Thread: " + Thread.currentThread().getName());

			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}

public class ThreadRunnable
{
	public static void main(String[] args)
	{
		Thread thread1 = new Thread(new RunnerRunnable());
		Thread thread2 = new Thread(new RunnerRunnable());
		thread1.start();
		thread2.start();
	}
}
