package threadcoreknowledge.stopthreads;

/**
 * Description: Once the interrupt exception is being caught, the interruption thread flag will be cleared.
 * And it could be restored by using Thread.currentThread().interrupt()
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class RightWayStopThreadInProd2 implements Runnable {

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Interrupted, program is terminated.");
                break;
            }
            reInterrupt();
        }
    }

    private void reInterrupt() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new RightWayStopThreadInProd2());
        thread.start();
        Thread.sleep(1000);
        thread.interrupt();
    }
}
