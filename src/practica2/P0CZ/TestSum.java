package practica2.P0CZ;

public class TestSum {

    public static void main(String[] args) throws InterruptedException {
        //throw new RuntimeException("//Completar...");
        //Exercici 7
        /*CounterThread f1 = new CounterThread();
        CounterThread f2 = new CounterThread();
        f1.start();
        f2.start();
        f1.join();
        f2.join();
        System.out.println(CounterThread.x);
        */
        //Exercici 8
        CounterThread1 f1 = new CounterThread1();
        CounterThread1 f2 = new CounterThread1();
        f1.start();
        f2.start();
        f1.join();
        f2.join();
        System.out.println(CounterThread1.x);
        

    }
}
