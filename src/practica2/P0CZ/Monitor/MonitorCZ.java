package practica2.P0CZ.Monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorCZ {

    private int x = 0;
    //Completar...
    private final ReentrantLock l = new ReentrantLock();

    public void inc() {
        //Incrementa en una unitat el valor d'x
        //throw new RuntimeException("//Completar...");
        l.lock();
        try {
            x=x+1;
            

        }finally
        {
            l.unlock();
        }

    }

    public int getX() {
        //Ha de retornar el valor d'x
        //throw new RuntimeException("//Completar...");
    l.lock();
        try {
            return this.x;
            

        }finally
        {
            l.unlock();
        }
    }

}
