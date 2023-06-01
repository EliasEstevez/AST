package practica2.P0CZ.Monitor;

public class TestSumCZ {

    public static void main(String[] args) throws InterruptedException {
        //throw new RuntimeException("//Completar...");
        MonitorCZ monitor = new MonitorCZ();

        CounterThreadCZ f1 = new CounterThreadCZ(monitor);
        CounterThreadCZ f2 = new CounterThreadCZ(monitor);
        f1.start();
        f2.start();
        f1.join();
        f2.join();
        System.out.println(monitor.getX());

    }
}
