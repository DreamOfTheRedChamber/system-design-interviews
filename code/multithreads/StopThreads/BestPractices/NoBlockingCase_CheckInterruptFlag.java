package threadcoreknowledge.stopthreads;

/**
 * Description: There is no sleep or wait method, stop the thread with interrupt method
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class NoBlockingCase_CatchInterruptFlag implements Runnable {

    @Override
    public void run() {
        int num = 0;
        while (!Thread.currentThread().isInterrupted() && num <= Integer.MAX_VALUE / 2) {
            if (num % 10000 == 0) {
                System.out.println(num + "is a multiple of 10000");
            }
            num++;
        }
        System.out.println("task finished");
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new InterruptMethodForNonBlockingCase());
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
    }
}
