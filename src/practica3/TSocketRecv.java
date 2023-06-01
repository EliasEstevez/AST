package practica3;

import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketRecv extends TSocket_base {

  protected Thread thread;
  protected CircularQueue<TCPSegment> rcvQueue;
  protected int rcvSegConsumedBytes;

  public TSocketRecv(SimNet net) {
    super(net);
    rcvQueue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
    rcvSegConsumedBytes = 0;
    new ReceiverTask().start();
  }

  @Override
  public int receiveData(byte[] buf, int offset, int length) {
    lock.lock();
    try {
      //throw new RuntimeException("//Completar...");
      while(this.rcvQueue.empty()){
          this.appCV.awaitUninterruptibly();
      }
      int byte_consum=0;
      int offset_actual=offset;
      int length_actual=length;
      while(length_actual>0 && !this.rcvQueue.empty()){
          byte_consum=byte_consum+consumeSegment(buf,offset_actual,length_actual);
          offset_actual=offset+byte_consum;
          length_actual=length-byte_consum;
      }
      return byte_consum;
      
    } finally {
      lock.unlock();
    }
  }

  protected int consumeSegment(byte[] buf, int offset, int length) {
    TCPSegment seg = rcvQueue.peekFirst();
     // System.out.println("length_out="+length+"   ,length_data_recv="+seg.getDataLength());
    int a_agafar = Math.min(length, seg.getDataLength() - rcvSegConsumedBytes);
    System.arraycopy(seg.getData(), rcvSegConsumedBytes, buf, offset, a_agafar);
    rcvSegConsumedBytes += a_agafar;
    if (rcvSegConsumedBytes == seg.getDataLength()) {
      rcvQueue.get();
      rcvSegConsumedBytes = 0;
    
    }
    

    return a_agafar;
  }

  @Override
  public void processReceivedSegment(TCPSegment rseg) {  
    
     
    lock.lock();
    try {
      //throw new RuntimeException("//Completar...");
      if(this.rcvQueue.full()){
          System.out.println("SEGMENT DESCARTAT");
      }
      else{
      this.rcvQueue.put(rseg);
      this.appCV.signal();
    }
    } finally {
      lock.unlock();
    
    }
  }

  class ReceiverTask extends Thread {

    @Override
    public void run() {
      while (true) {
        TCPSegment rseg = network.receive();
        processReceivedSegment(rseg);
        TSocketRecv.super.printRcvSeg(rseg);
        
      }
    }
  }
}
