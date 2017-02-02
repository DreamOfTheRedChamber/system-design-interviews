package basics.terminateThread;

/**
 * Interrupt threads -> for example making I/O operations and the user stop the
 * operation via UI ... we have to terminate the thread
 * 
 * boolean Thread.isInterrupted() -> check whether is it interrupted boolean
 * interrupted() -> checks + interrupt the thread !!!
 * 
 * Terminate a thread -> volatile boolean flags !!!
 * 
 * Thread states:
 * 
 * 1.) RUNNABLE: if we create a new thread + call start() method The run()
 * method can be called... new MyThread().start();
 * 
 * 2.) BLOCKED: if it is waiting for an object's monitor lock - waiting to enter
 * a synchronized block, like synchronized(new Object()) { } - after wait():
 * waiting for the monitor lock to be free
 * 
 * 3.) WAITING: when we call wait() on a thread ... it is going to loose the
 * monitor lock and wait for notify() notifyAll()
 * 
 * 4.) TERMINATED: when the run() method is over ... We can check it with
 * isAlive() method
 * 
 */

class Worker extends Thread
{
	// use volatile variables to terminate thread
	private volatile Thread thread = this;

	public void finish()
	{
		this.thread = null;
	}

	@Override
	public void run()
	{
		while (this.thread == this)
		{
			System.out.println( "Thread is running..." );
			try
			{
				Thread.sleep( 500 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
}

public class TerminateThread
{
	public static void main(String[] args)
	{

		Worker worker = new Worker();
		worker.start();

		try
		{
			Thread.sleep( 4000 );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}

		worker.finish();
	}
}
