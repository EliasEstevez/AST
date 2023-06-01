package practica2.P0CZ;

public class CounterThread extends Thread {

    public static int x;
    private final int I = 1000;//ya a partir de I=1000 comien
                               //cominza a tener valores no esperados

    @Override
    public void run() {
        for (int i = 0; i < I; i++) {
            x = x + 1;
        }
    }
}
