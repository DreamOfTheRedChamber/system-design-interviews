package threadcoreknowledge.threadobjectclasscommonmethods;

/**
 * Description: 
 * Main thread executes child thread's join is equivalent to:
 * Main thread gets the lock for the child thread and wait on the lock until notified by the JVM
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class JoinPrinciple {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "finished execution.");
            }
        });

        thread.start();
        System.out.println("Main thread starts to wait for child thread to finish by calling join.");
        thread.join();
        // Equivalent to join
//        synchronized (thread) {
//            thread.wait();
//        }
        System.out.println("All children threads have finished.");
    }
}
