package threadcoreknowledge.stopthreads.volatiledemo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Description: The unlucky case for volatile where the thread cannot get stopped because canceled volatile variable did not get picked up. 
 * Citation: Chinese course: https://coding.imooc.com/lesson/362.html#mid=27728
 */
public class VolatileUnluckyCase {

    public static void main(String[] args) throws InterruptedException {
        ArrayBlockingQueue storage = new ArrayBlockingQueue(10);

        Producer producer = new Producer(storage);
        Thread producerThread = new Thread(producer);
        producerThread.start();
        Thread.sleep(1000);

        Consumer consumer = new Consumer(storage);
        while (consumer.needMoreNums()) {
            System.out.println(consumer.storage.take()+"is being consumed.");
            Thread.sleep(100);
        }
        System.out.println("Consumer has been stopped and does not need more data。");

        // Once consumer does not need more data, want to stop producer as well by setting the canceled volatile variable
        producer.canceled=true;
        System.out.println(producer.canceled);
        // However, producer won't be able to stop
    }
}

class Producer implements Runnable {

    public volatile boolean canceled = false;

    BlockingQueue storage;

    public Producer(BlockingQueue storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        int num = 0;
        try {
            while (num <= 100000 && !canceled) {
                if (num % 100 == 0) {
                    // !!! This is a blocking call. 
                    // When the queue is full this operation will always be blocked here. 
                    // As a result, the !canceled condition inside while will not be invoked. 
                    storage.put(num);
                    System.out.println(num + "Is a multiple of 100, and being put inside queue");
                }
                num++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Producer stopped");
        }
    }
}

class Consumer {

    BlockingQueue storage;

    public Consumer(BlockingQueue storage) {
        this.storage = storage;
    }

    public boolean needMoreNums() {
        if (Math.random() > 0.95) {
            return false;
        }
        return true;
    }
}