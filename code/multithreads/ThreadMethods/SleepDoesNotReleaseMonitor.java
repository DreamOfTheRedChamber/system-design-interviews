package threadcoreknowledge.threadobjectclasscommonmethods;

import sun.awt.windows.ThemeReader;

/**
 * Description: Sleep does not release synchronized monitor. 
 */
public class SleepDontReleaseMonitor implements Runnable {

    public static void main(String[] args) {
        SleepDontReleaseMonitor sleepDontReleaseMonitor = new SleepDontReleaseMonitor();
        new Thread(sleepDontReleaseMonitor).start();
        new Thread(sleepDontReleaseMonitor).start();
    }

    @Override
    public void run() {
        syn();
    }

    private synchronized void syn() {
        System.out.println("Thread" + Thread.currentThread().getName() + "obtain monitor.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread" + Thread.currentThread().getName() + "exit synchronized code block.");
    }
}
