package practica2.P1Sync.Monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorSync {

    private final int N;

    //Completar...
    private final ReentrantLock l;
    private final Condition condicion;
    private int numPermiso;

    public MonitorSync(int N) {
        this.N = N;
        this.l = new ReentrantLock();
        this.condicion=l.newCondition();
        this.numPermiso=0;
    
    }

    public void waitForTurn(int id) throws InterruptedException {
        //throw new RuntimeException("//Completar...");
    l.lock();
    try{
        while(id!=this.numPermiso){
            condicion.awaitUninterruptibly();
        }
        
    }finally{
        l.unlock();
    }
    }

    public void transferTurn() {
        l.lock();
        try{
            this.numPermiso=(this.numPermiso+1)%N;
            this.condicion.signal();// En este caso seria lo mismo que si 
                                    // si se pusiese signalAll;
            
        }finally{
            l.unlock();
        }
        //throw new RuntimeException("//Completar...");
    
    }
}
