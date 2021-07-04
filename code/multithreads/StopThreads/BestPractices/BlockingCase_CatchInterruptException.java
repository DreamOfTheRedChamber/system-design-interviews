package threadcoreknowledge.stopthreads;

/**
 * Description: No need to check whether it is interrupted each time if sleep/wait is called every time. 
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class BlockingCase_CatchInterruptException {
    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            int num = 0;
            try {
                // No need to check the condition here
                while (num <= 10000) {
                    if (num % 100 == 0) {
                        System.out.println(num + "is multiple of 100");
                    }
                    num++;

                    // sleep method is within the loop
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
    }
}