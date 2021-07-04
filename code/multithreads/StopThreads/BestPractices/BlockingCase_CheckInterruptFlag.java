package threadcoreknowledge.stopthreads;

/**
 * Description: Use the interruption pattern will always be able to be picked up by the other threads
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class BlockingCase_InterruptException {

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            int num = 0;
            try {
                // There is a chance that when interrupt is thrown out, the program is within the loop execution. 
                // So need to check the value of isInterrupted() flag inside while condition
                while (num <= 300 && !Thread.currentThread().isInterrupted()) {
                    if (num % 100 == 0) {
                        System.out.println(num + "is a multiple of 100");
                    }
                    num++;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Thread.sleep(500);
        thread.interrupt();
    }
}
