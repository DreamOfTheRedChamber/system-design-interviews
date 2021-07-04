package threadcoreknowledge.stopthreads;

/**
 * Description: Once the interrupt exception is being caught, the interruption thread flag will be cleared. 
 * Basically, Thread.currentThread().isInterrupted() will not be able to detect the change. 
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class InterruptFlagBeingClearedbyTryCatch {

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            int num = 0;
            while (num <= 10000 && !Thread.currentThread().isInterrupted()) {
                if (num % 100 == 0) {
                    System.out.println(num + "is multiple of 100.");
                }
                num++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
    }
}