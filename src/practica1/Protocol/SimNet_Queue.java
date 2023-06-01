package practica1.Protocol;

import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.SimNet;

public class SimNet_Queue implements SimNet {

  CircularQueue<TCPSegment> queue;

  public SimNet_Queue() {
    queue = new CircularQueue<>(Const.SIMNET_QUEUE_SIZE);
  }

  @Override
  public void send(TCPSegment s) {
    
    //<-- no cal mirar si està plena
    //if(queue.full())throw new RuntimeException("Cua plena");
    this.queue.put(s);
  }

  @Override
  public TCPSegment receive() {
    //<-- no cal mirar si està buida
    //if(this.queue.empty())throw new RuntimeException("Cola vacia");
    TCPSegment aux=this.queue.get();
        return aux;
        
  }

  @Override
  public int getMTU() {
    throw new UnsupportedOperationException("Not supported yet. No cal completar en aquesta pràctica");
  }
}
