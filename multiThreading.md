# Multithreading

<!-- MarkdownTOC -->

- [Thread basics](#thread-basics)
	- [Create threads](#create-threads)
		- [Implementing the Runnable interface](#implementing-the-runnable-interface)
		- [Extending the Thread class](#extending-the-thread-class)
		- [Extending the Thread Class vs Implementing the Runnable Interface](#extending-the-thread-class-vs-implementing-the-runnable-interface)
- [Thread-safe producer and consumer](#thread-safe-producer-and-consumer)
- [Delayed scheduler](#delayed-scheduler)
	- [Interfaces to be implemented](#interfaces-to-be-implemented)
	- [Single thread](#single-thread)
	- [One thread for each task](#one-thread-for-each-task)
	- [PriorityQueue + A background thread](#priorityqueue--a-background-thread)

<!-- /MarkdownTOC -->



# Thread basics
## Create threads
### Implementing the Runnable interface
* The runnable interface has the following very simple structure

```java
public interface Runnable
{
	void run();
}
```

* Steps
	- Create a class which implements the Runnable interface. An object of this class is a Runnable object
	- Create an object of type Thread by passing a Runnable object as argument to the Thread constructor. The Thread object now has a Runnable object that implements the run() method. 
	- The start() method is invoked on the Thread object created in the previous step. 

```java
public class RunnableThreadExample implements Runnable
{
	public int count = 0;

	public void run()
	{
		System.out.println( "RunnableThread starting.");

		try
		{
			while ( count < 5 )
			{
				Thread.sleep( 500 );
				count++;
			}
		}
		catch ( InterruptedException exc )
		{
			System.out.println( "RunnableThread interrupted" );
		}

		System.out.println( "RunnableThread terminating" );
	}
}

public static void main( String[] args )
{
	RunnableThreadExample instance = new RunnableThreadExample();
	Thread thread = new Thread( instance );
	thread.start();

	/* waits until above thread counts to 5 (slowly) */
	while ( instance.count != 5 )
	{
		try 
		{
			Thread.sleep( 250 );
		}
		catch ( InterruptedException exc )
		{
			exc.printStackTrace();
		}		
	}
}
```


### Extending the Thread class
* We can create a thread by extending the Thread class. This will almost always mean that we override the run() method, and the subclass may also call the thread constructor explicitly in its constructor. 

```java
public class ThreadExample extends Thread
{
	int count = 0;

	public void run()
	{
		System.out.println( "Thread starting" );
		try
		{
			while ( count < 5 )
			{
				Thread.sleep( 500 );
				System.out.println( "In thread, count is " + count );
				count++;
			}
		}
		catch ( InterruptedException exc )
		{
			System.out.println( "Thread interrupted" );
		}

		System.out.println( "Thread terminating" );
	}
}

public static void main( String[] args )
{
	ThreadExample instance = new ThreadExample();
	instance.start();

	while ( instance.count != 5 )
	{
		try
		{
			Thread.sleep( 250 );
		}
		catch ( InterruptedException exc )
		{
			exc.printStackTrace();
		}
	}
}
```

### Extending the Thread Class vs Implementing the Runnable Interface
* Implementing the Runnable interface is preferrable to extending the Thread class
	- Java does not support multiple inheritance. Therefore, extending the Thread class means that the subclass cannot extend any other class. A class implementing the Runnable interface will be able to extend another class. 
	- A class might only be interested in being runnable, and therefore, inheriting the full overhead of the Thread class would be excessive. 

# Thread-safe producer and consumer
# Delayed scheduler

## Interfaces to be implemented

```java
public interface Scheduler
{
	void schedule( Task t, long delayMs );
}

public interface Task
{
	void run();
}

```

## Single thread
* Main thread is in Timedwaiting state for delayMs for each call of schedule()
* Only one thread, very low CPU utilization
* Also, this is not working as later call
* How about sleeping in other threads

```java
public class SchedulerImpl implements Scheduler
{
	public void schedule( Task t, long delayMs )
	{
		try
		{
			// sleep for delayMs, and then execute the task
			Thread.sleep( delayMs );
			t.run();
		}
		catch ( InterruptedException e )
		{
			// ignore
		}
	}

	public static void main( String[] args )
	{
		Scheduler scheduler = new SchedulerImpl();
		Task t1 = new TaskImpl( 1 );
		Task t2 = new TaskImpl( 2 );

		// main thread in timedwaiting state for 10000 ms
		scheduler.schedule( t1, 10000 );
		scheduler.schedule( t2, 1 );
	}
}

```

## One thread for each task
* No blocking when calling schedule
* What happens if we call schedule many times
	- A lot of thread creation overhead
* Call be alleviated by using a thread pool, but still not ideal

```java
public class SchedulerImpl implements Scheduler
{
	public void schedule( Task t, long delayMs )
	{
		Thread t = new Thread( new Runnable() {
			public void run()
			{
				try 
				{
					Thread.sleep( delayMs );
					t.run();
				}
				catch ( InterruptedException e )
				{
					// ignore;
				}
			}
		} );
		t.start();
	}
}
```

## PriorityQueue + A background thread

```java
package designThreadSafeEntity.delayedTaskScheduler;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler
{
	// order task by time to run
	private PriorityQueue<Task> tasks;

	// 
	private final Thread taskRunnerThread;

	// State indicating the scheduler is running
	// Why volatile? As long as main thread stops, runner needs to has visibility.
	private volatile boolean running;

	// Task id to assign to submitted tasks
	// AtomicInteger: Threadsafe. Do not need to add locks when assigning task Ids
	// Final: Reference of atomicInteger could not be changed
	private final AtomicInteger taskId;
	
	public Scheduler()
	{
		tasks = new PriorityQueue<>();
		taskRunnerThread = new Thread( new TaskRunner() );
		running = true;
		taskId = new AtomicInteger( 0 );

		// start task runner thread
		taskRunnerThread.start();
	}
	
	public void schedule( Task task, long delayMs )
	{
		// Set time to run and assign task id
		long timeToRun = System.currentTimeMillis() + delayMs;
		task.setTimeToRun( timeToRun );
		task.setId( taskId.incrementAndGet() );

		// Put the task in queue
		synchronized ( this )
		{
			tasks.offer( task );
			this.notify(); // only a single background thread waiting
		}
	}
	
	public void stop( ) throws InterruptedException
	{
		// Notify the task runner as it may be in wait()
		synchronized ( this )
		{
			running = false;
			this.notify();
		}

		// Wait for the task runner to terminate
		taskRunnerThread.join();
	}
	
	private class TaskRunner implements Runnable
	{
		@Override
		public void run()
		{
			while ( running )
			{
				// Need to synchronize with main thread
				synchronized( Scheduler.this )
				{
					try 
					{
						// task runner is blocked when no tasks in queue
						while ( running && tasks.isEmpty() )
						{
							Scheduler.this.wait();
						}

						// check the first task in queue
						long now = System.currentTimeMillis();
						Task t = tasks.peek();

						// delay exhausted, execute task
						if ( t.getTimeToRun() < now )
						{
							tasks.poll();
							t.run();
						}			
						else
						{
							// no task executable, wait
							Scheduler.this.wait( t.getTimeToRun() - now );
						}
					}
					catch ( InterruptedException e )
					{
						Thread.currentThread().interrupt();	
					}
				}
			}
		}
	}
	
	public static void main( String[] args ) throws InterruptedException
	{
		Scheduler scheduler = new Scheduler();
		scheduler.schedule( new Task(), 1000000 );
		scheduler.schedule( new Task(), 1000 );
		Thread.sleep( 7000 );
		scheduler.stop();
	}
}

class Task implements Comparable<Task> 
{
	// When the task will be run
	private long timeToRun;
	private int id;
	
	public void setId( int id )
	{
		this.id = id;
	}
	
	public void setTimeToRun( long timeToRun )
	{
		this.timeToRun = timeToRun;
	}
	
	public void run()
	{
		System.out.println( "Running task " + id );
	}
	
	public int compareTo( Task other )
	{
		return (int) ( timeToRun - other.getTimeToRun() );
	}
	
	public long getTimeToRun()
	{
		return timeToRun;
	}
}
```