package practica2.Protocol;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.SimNet;

public class SimNet_Monitor implements SimNet {

    protected CircularQueue<TCPSegment> queue;
    //Completar
    protected final ReentrantLock l;
    protected final Condition buida, plena;

    public SimNet_Monitor() {
        queue = new CircularQueue<>(Const.SIMNET_QUEUE_SIZE);
        //Completar
        this.l = new ReentrantLock();
        this.buida = l.newCondition();
        this.plena = l.newCondition();

    }

    @Override
    public void send(TCPSegment seg) {

        l.lock();
        try {
            while (this.queue.full()) {
                this.plena.awaitUninterruptibly();
            }

            this.queue.put(seg);
            this.buida.signal();

        }  finally {
            l.unlock();
        }

    }

    @Override
    public TCPSegment receive() {
        l.lock();
        try {
            while (this.queue.empty()) {
                this.buida.awaitUninterruptibly();
            }

            TCPSegment aux = this.queue.get();
            this.plena.signal();
            return aux;
        } finally {
            l.unlock();
        }
    }

    @Override
    public int getMTU() {
    /*
        //Practica_2 
        throw new UnsupportedOperationException("Not supported yet. NO cal completar fins a la pràctica 3...");
    */
        //Practica_3
        return Const.MTU_ETHERNET;
    }

}
