package basics.createThread;

public class ThreadAnonymous
{
	public static void main(String[] args)
	{
		Thread thread1 = new Thread(new Runnable() {
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
					catch (InterruptedException ignored)
					{
					}
				}
			}
		});

		thread1.start();
	}
}
