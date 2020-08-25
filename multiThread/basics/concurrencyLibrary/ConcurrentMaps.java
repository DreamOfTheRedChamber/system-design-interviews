package basics.concurrencyLibrary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class FirstWorker3 implements Runnable {

	private ConcurrentMap<String, Integer> map;
	
	public FirstWorker3(ConcurrentMap<String, Integer> map) {
		this.map = map;
	}

	@Override
	public void run() {
		try {
			map.put("B",1);
			map.put("H",2);
			map.put("F",3);
            Thread.sleep(1000);
            map.put("A",4);
            Thread.sleep(1000);
            map.put("E",5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }	
	}
}

class SecondWorker3 implements Runnable {

	private ConcurrentMap<String, Integer> map;
	
	public SecondWorker3(ConcurrentMap<String, Integer> map) {
		this.map = map;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
            System.out.println(map.get("A"));
            Thread.sleep(1000);
            System.out.println(map.get("E"));
            Thread.sleep(1000);
            System.out.println(map.get("C"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}

public class ConcurrentMaps {

	public static void main(String[] args) {
		
		ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();

		FirstWorker3 firstWorker = new FirstWorker3(map);
		SecondWorker3 secondWorker = new SecondWorker3(map);

        new Thread(firstWorker).start();
        new Thread(secondWorker).start();
		
	}
}
