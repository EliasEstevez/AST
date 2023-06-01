package practica2.P0CZ;

public class CounterThread1 extends Thread {

    public static int x;
    private final int I = 100;//ya a partir de I=1000 comien
                               //cominza a tener valores no esperados

    @Override
    public void run() {
        int R;
        for (int i = 0; i < I; i++) {
            x = x + 1;
            R=x;
            try{
                sleep(1);
            }catch(InterruptedException ex){
            }
            R=R+1;
            x=R;
            }
        }
}
    

