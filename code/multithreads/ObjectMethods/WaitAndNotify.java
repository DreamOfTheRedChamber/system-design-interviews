package threadcoreknowledge.threadobjectclasscommonmethods;

/**
 * Description: Wait will release the monitor lock associated with this object. It will be awakened when any of the following cases happen: 
 *              1. Another thread calls the notify() method on this object, and happens to wake this exact thread. 
 *              2. Another thread calls the notifyAll() method on this object. 
 *              3. Pass the time specified by wait(long timeout).
 *              4. Thread itself calls interrupt() method. 
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class Wait {

    public static Object object = new Object();

    static class Thread1 extends Thread {

        @Override
        public void run() {
            synchronized (object) {
                System.out.println(Thread.currentThread().getName() + "starts execution");
                try {
                    object.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread" + Thread.currentThread().getName() + "obtain the lock");
            }
        }
    }

    static class Thread2 extends Thread {

        @Override
        public void run() {
            synchronized (object) {
                object.notify();
                System.out.println("Thread" + Thread.currentThread().getName() + "calls notify()");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {        
        Thread1 thread1 = new Thread1();
        Thread2 thread2 = new Thread2();

        // Thread1 will obtain the global variable Object's lock first.
        // Then it will release the lock by object.wait() method. 
        thread1.start();        
        Thread.sleep(200);

        // Thread2 will obtain the global variable Object's lock first. 
        // Then it will notify Thread1 that this lock has been released. 
        thread2.start();
    }
}
