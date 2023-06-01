package practica5;

import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TSocket_base;
import util.TCPSegment;

public class TSocket_21 extends TSocket_base {

  // Sender variables:
  protected int MSS;
  protected int snd_sndNxt;
  protected int snd_rcvWnd;
  protected int snd_rcvNxt;
  protected TCPSegment snd_UnacknowledgedSeg;
  protected boolean zero_wnd_probe_ON;

  // Receiver variables:
  protected CircularQueue<TCPSegment> rcv_Queue;
  protected int rcv_SegConsumedBytes;
  protected int rcv_rcvNxt;

  protected TSocket_21(Protocol p, int localPort, int remotePort) {
    super(p.getNetwork());
    this.localPort = localPort;
    this.remotePort = remotePort;
    p.addActiveTSocket(this);
    // init sender variables
    MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
    // init receiver variables
    //rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
    //snd_rcvWnd = Const.RCV_QUEUE_SIZE;
    rcv_Queue = new CircularQueue<>(2);
    snd_rcvWnd = 2;

  }

  // -------------  SENDER PART  ---------------
  @Override
  public void sendData(byte[] data, int offset, int length) {
    lock.lock();
    try {
      //throw new RuntimeException("//Completar...");
      TCPSegment segment;
      int quedenPerEnviar = length;
      int bytesAPosar;

      while(quedenPerEnviar>0){

        while(snd_sndNxt - snd_rcvNxt == 1 || zero_wnd_probe_ON){ //Comprueba si ha llegado el ACK y si se esta haciendo sondeo
          appCV.awaitUninterruptibly();
        }

        if(snd_rcvWnd>0) {
          if (quedenPerEnviar > MSS) {
            bytesAPosar = MSS;
          } else {
            bytesAPosar = quedenPerEnviar;
          }

          segment = segmentize(data, offset, bytesAPosar);
          offset += bytesAPosar;
          quedenPerEnviar -= bytesAPosar;

          segment.setSeqNum(snd_sndNxt);
          snd_UnacknowledgedSeg = segment; //Guarda el segmento para poder retransmitir
          network.send(segment);

          snd_sndNxt++;
          startRTO();

        }else{
          bytesAPosar = 1; //Se envia solo un byte
          segment = segmentize(data, offset, bytesAPosar);
          quedenPerEnviar--; //Se envia solo un byte
          offset++;

          segment.setSeqNum(snd_sndNxt);
          snd_UnacknowledgedSeg = segment; //Guarda el segmento de sondeo para poder sondearlo infinitivamente

          snd_sndNxt++;
          System.out.println("----- zero-window probe ON -----");
          zero_wnd_probe_ON = true;
          startRTO();  //Espera un tiempo para que se vacie la cola del receptor
        }
      }
    } finally {
      lock.unlock();
    }
  }

  protected TCPSegment segmentize(byte[] data, int offset, int length) {
    //throw new RuntimeException("//Completar...");
    TCPSegment segment = new TCPSegment();
    byte[] dates = new byte[length];
    System.arraycopy(data,offset,dates,0,length);
    segment.setData(dates);
    segment.setPsh(true);
    segment.setSourcePort(localPort);
    segment.setDestinationPort(remotePort);
    return segment;
  }

  @Override
  protected void timeout() {
    lock.lock();
    try {
      //throw new RuntimeException("//Completar...");
      if(snd_UnacknowledgedSeg != null){
        if (zero_wnd_probe_ON){
          log.printPURPLE(("0-wnd probe: " + snd_UnacknowledgedSeg));
        } else {
          log.printPURPLE("retrans: " + snd_UnacknowledgedSeg);
        }
        network.send(snd_UnacknowledgedSeg);
        startRTO();
      }
    } finally {
      lock.unlock();
    }
  }

  // -------------  RECEIVER PART  ---------------
  @Override
  public int receiveData(byte[] buf, int offset, int maxlen) {
    lock.lock();
    try {
      //throw new RuntimeException("//Completar...");
      while(rcv_Queue.empty()){
        appCV.awaitUninterruptibly();
      }
      int consum = 0;
      while(!rcv_Queue.empty() && consum < maxlen){
        consum += consumeSegment(buf, offset+consum, maxlen-consum);
      }
      return consum;
    } finally {
      lock.unlock();
    }
  }

  protected int consumeSegment(byte[] buf, int offset, int length) {
    TCPSegment seg = rcv_Queue.peekFirst();
    int a_agafar = Math.min(length, seg.getDataLength() - rcv_SegConsumedBytes);
    System.arraycopy(seg.getData(), rcv_SegConsumedBytes, buf, offset, a_agafar);
    rcv_SegConsumedBytes += a_agafar;
    if (rcv_SegConsumedBytes == seg.getDataLength()) {
      rcv_Queue.get();
      rcv_SegConsumedBytes = 0;
    }
    return a_agafar;
  }

  protected void sendAck() {
    //throw new RuntimeException("//Completar...");
    TCPSegment ack = new TCPSegment();
    ack.setAck(true);
    ack.setAckNum(rcv_rcvNxt);   //Pone el numero de seq del segmento que espera recibir
    ack.setSourcePort(localPort);
    ack.setDestinationPort(remotePort);
    ack.setWnd(rcv_Queue.free());  //Pone el espacio de la cola del receptor
    network.send(ack);
  }

  // -------------  SEGMENT ARRIVAL  -------------
  @Override
  public void processReceivedSegment(TCPSegment rseg) {
    lock.lock();
    try{
      //throw new RuntimeException("//Completar...");
      if (rseg.isAck()){
        if(rseg.getAckNum() > snd_rcvNxt) { //Descarta ACK fuera de secuencia (por si llega un ack repetido por culpa del sondeo)
          printRcvSeg(rseg);
          snd_rcvNxt++;
          snd_UnacknowledgedSeg = null;
          stopRTO();
          appCV.signal();
          snd_rcvWnd = rseg.getWnd(); //Guarda valor de la ventana


          if(zero_wnd_probe_ON){
            zero_wnd_probe_ON = false;
            System.out.println("----- zero-window probe OFF -----");
          }

        }
      }

      if(rseg.isPsh()) {
        if(rseg.getSeqNum() == rcv_rcvNxt) { //Descarta segmentos fuera de secuencia (por ejemplo: repetidos, pero solo pasa cuando sondeas)
          if (!rcv_Queue.full()) {
            rcv_Queue.put(rseg);
            rcv_rcvNxt++;
            printRcvSeg(rseg);
            sendAck();
            appCV.signal();
          }
        }else if(rcv_rcvNxt - rseg.getSeqNum() == 1){  //Si llega segmento repetido
          printRcvSeg(rseg);
          sendAck();
        }
      }

    } finally {
      lock.unlock();
    }
  }
}