
- [Workable solutions](#workable-solutions)
  - [Non-lazy loading](#non-lazy-loading)
  - [Lazy loading](#lazy-loading)
    - [Based on synchronized keyword](#based-on-synchronized-keyword)
    - [Double checked locking + Volatile](#double-checked-locking--volatile)
    - [Static inner class](#static-inner-class)
    - [Winner: Enumeration](#winner-enumeration)

## Workable solutions

### Non-lazy loading
* Ideas: Create the singleton instance during the class loading phase.
* Cons: 
  * There is no lazy loading and it might resulting in waste of resources. 

```java
public class Singleton1 {

    private final static Singleton1 INSTANCE = new Singleton1();

    private Singleton1() {

    }

    public static Singleton1 getInstance() {
        return INSTANCE;
    }

}

public class Singleton2 {

    private final static Singleton2 INSTANCE;

    static {
        INSTANCE = new Singleton2();
    }

    private Singleton2() {
    }

    public static Singleton2 getInstance() {
        return INSTANCE;
    }
}
```

### Lazy loading
#### Based on synchronized keyword
* Idea: Solve the problem with synchronized keyword.
* Pros:
  * Lazy loading
* Cons: 
  * Low performance. Only the instance creation part needs to be synchronized.

```java
public class Singleton4 {

    private static Singleton4 instance;

    private Singleton4() {

    }

    public synchronized static Singleton4 getInstance() {
        if (instance == null) {
            instance = new Singleton4();
        }
        return instance;
    }
}
```

#### Double checked locking + Volatile
* Why need volatile?
    * Creating a new object has three steps
      1. Allocate memory for an object
      2. Call the constructor method on the object
      3. Assign the memory address to the object
    * CPU instruction reordering mght change the order of 2,3. 
      * Thread 1 enters code line B, then it assigns memory address to the object (3 step above). However, it might has not already finished 1 and 2 steps above. 
      * Thread 2 enters code line A, skips line B but end up finding the object empty at line C. 
    * References: http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
* Why need the if condition on Code line O
  * For performance reasons, without this condition, the code will still works. 
    * However, each time when trying to get the instance variable, it needs to enter the synchronized code block. 
    * With this if condition, the synchronized code block will only be invoked during the creation of the instance. 
* Why need the if condition on Code line A
  * If without the block, then code line B could be executed twice by different threads, creating multiple singleton objects. 


```java
public class Singleton6 {

    private volatile static Singleton6 instance;

    private Singleton6() {

    }

    public static Singleton6 getInstance() 
    {
        // Code line O
        if (instance == null) 
        {
            synchronized (Singleton6.class) 
            {
                // Code line A
                if (instance == null) 
                {
                    // Code line B
                    instance = new Singleton6();
                }
            }
        }
        // Code line C
        return instance;
    }
}
```

#### Static inner class

```java
public class Singleton7 {

    private Singleton7() {
    }

    private static class SingletonInstance {
        // Only when getInstance() method is invoked
        private static final Singleton7 INSTANCE = new Singleton7();
    }

    public static Singleton7 getInstance() {
        return SingletonInstance.INSTANCE;
    }
}

```

#### Winner: Enumeration
* This approach is recommended in the "Effective Java" book.
* It has the following advantages: https://dzone.com/articles/java-singletons-using-enum
  * Works with serialization and deserialization. 
    * In order to serialize the above singleton classes, we must implement those classes with a Serializable interface. But doing that is not enough. When deserializing a class, new instances are created. Now it doesn't matter the constructor is private or not. Now there can be more than one instance of the same singleton class inside the JVM, violating the singleton property. 
    * The solution is that we have to implement the readResolve method, which is called when preparing the deserialized object before returning it to the caller.
    * References: 
  * Works with reflection. 
    * An advanced user can change the private access modifier of the constructor to anything they want at runtime using reflection. If this happens, our only mechanism for non-instantiability breaks.

```java
public enum SingletonEnum {
    INSTANCE;

    int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

// Usage:
public class EnumDemo 
{
    public static void main(String[] args) {
        SingletonEnum singleton = SingletonEnum.INSTANCE;

        System.out.println(singleton.getValue());
        singleton.setValue(2);
        System.out.println(singleton.getValue());
    }
}
```