package threadcoreknowledge.stopthreads.volatiledemo;

/**
 * Description: The lucky case for volatile where the thread gets stopped because canceled volatile variable gets picked up. 
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class VolatileLuckyCase implements Runnable {

    private volatile boolean canceled = false;

    @Override
    public void run() {
        int num = 0;
        try {
            while (num <= 100000 && !canceled) {
                if (num % 100 == 0) {
                    System.out.println(num + "Is a multiply of 100");
                }
                num++;
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileLuckyCase r = new VolatileLuckyCase();
        Thread thread = new Thread(r);
        thread.start();
        Thread.sleep(5000);
        r.canceled = true;
    }
}
