# Counter

* [Thread safe counter](counter.md#thread-safe-counter)
  * [With synchronized blocks / methods](counter.md#with-synchronized-blocks--methods)
  * [With ReentrantLock](counter.md#with-reentrantlock)
  * [Atomic class](counter.md#atomic-class)
  * [Unsafe class implementation](counter.md#unsafe-class-implementation)

## Thread safe counter

### With synchronized blocks / methods

```java
public class SynchronizedBlocksCounter
{
    private int value;

    public synchronized int get()
    {
        synchronized ( this )
        {
            return value;
        }
    }

    public synchronized void increment()
    {
        synchronized ( this )
        {
            value++;
        }
    }
}

public class SynchronizedMethodsCounter
{
    private int value;

    public synchronized int get()
    {
        return value;
    }

    public synchronized void increment()
    {
        value++;
    }
}
```

### With ReentrantLock

```java
public class ReentrantLockCounter
{
    private final Lock lock = new ReentrantLock();
    private int value;

    public int get()
    {
        try
        {
            lock.lock();
            return value;
        }
        finally
        {
            lock.unlock();
        }
    }

    public synchronized void increment()
    {
        try
        {
            lock.lock();
            value++;
        }
        finally
        {
            lock.unlock();
        }
    }
}

public class ReadWriteLockCounter
{
    private int count;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void increment()
    {
        try
        {
            lock.writeLock().lock();
            count++;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public int getCount()
    {
        try
        {
            lock.readLock().lock();
            return count;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
}
```

### Atomic class

```java
public class AtomicVariableCounter
{
    private AtomicInteger c = new AtomicInteger(0);

    public void increment()
    {
        c.incrementAndGet();
    }

    public int getCount()
    {
        return c.get();
    }
}
```

### Unsafe class implementation

```java
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class CounterUnsafe 
{
    volatile int i = 0; //cas hardware   memory address --Long 232323523454235

    private static  Unsafe unsafe =null;

    private static  long valueOffSet; // The offset address inside memory

    static 
    {
        try 
        {
            // Use reflection to get the unsafe instance
            // Use reflection to get field 
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            // Set the field as visible
            field.setAccessible(true);
            // Get the Unsafe instance
            unsafe = (Unsafe) field.get(null);

            // Get the offset within memory
            // Get the field
            Field field1 = CounterUnsafe.class.getDeclaredField("i");
            // Get the field offset address
            valueOffSet = unsafe.objectFieldOffset(field1);
        } 
        catch (NoSuchFieldException | IllegalAccessException e) 
        {
            e.printStackTrace();
        }
    }

    public void add() 
    {
        for(;;) 
        {
            // Get the field
            int current = unsafe.getIntVolatile(this, valueOffSet); 

            // Performance: increment
            if (unsafe.compareAndSwapInt(this, valueOffSet, current, current + 1))
            { 
                break;
            };
        }

    }

    public static void main(String[] args) {

    }
}
```

