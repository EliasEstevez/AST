package practica3;

import java.util.Random;
import util.Const;
import util.Log;
import util.TCPSegment;
import practica2.Protocol.SimNet_Monitor;

public class SimNet_Loss extends practica2.Protocol.SimNet_Monitor {

    private double lossRate;
    private Random rand;
    private Log log;

    public SimNet_Loss(double lossRate) {
        this.lossRate = lossRate;
        rand = new Random(Const.SEED);
        log = Log.getLog();
    }

    @Override
    public void send(TCPSegment seg) {
        l.lock();
        try {
            while (this.queue.full()) {
                this.plena.awaitUninterruptibly();
            }
            double num_rnd=this.rand.nextDouble();
            //System.out.println(num_rnd);
            if (num_rnd < this.lossRate) {
                System.out.println("\t\t\t\t+++++++++ SEGMENT LOST:"+seg.toString());

            } else {
                this.queue.put(seg);
                this.buida.signalAll();

            }
        } finally {
            l.unlock();
        }
    }

    @Override
    public int getMTU() {
        //throw new RuntimeException("//Completar...");
        return Const.MTU_ETHERNET;
    }
}
