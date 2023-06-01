package practica2.P1Sync.Monitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CounterThreadIDSync extends Thread {

    private final MonitorSync mon;
    private final int id;

    public CounterThreadIDSync(MonitorSync mon, int id) {
        this.mon = mon;
        this.id = id;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                //throw new RuntimeException("//Completar...");
                mon.waitForTurn(id);
                System.out.println(id);
            } catch (InterruptedException ex) {
                Logger.getLogger(CounterThreadIDSync.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        mon.transferTurn();
        }
    }

}
