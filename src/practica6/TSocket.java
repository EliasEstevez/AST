package practica6;

import java.util.Iterator;
import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

public class TSocket extends TSocket_base {

    // Sender variables:
    protected int MSS;
    protected int snd_sndNxt;
    protected int snd_rcvNxt;
    protected int snd_rcvWnd;
    protected int snd_cngWnd;
    protected int snd_minWnd;
    protected CircularQueue<TCPSegment> snd_unacknowledged_segs;
    protected boolean zero_wnd_probe_ON;

    // Receiver variables:
    protected int rcv_rcvNxt;
    protected CircularQueue<TCPSegment> rcv_Queue;
    protected int rcv_SegConsumedBytes;

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        // init sender variables:
        MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        MSS = 10;
        // init receiver variables:
        rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        snd_rcvWnd = Const.RCV_QUEUE_SIZE;
        snd_cngWnd = 3;
        snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);
        snd_unacknowledged_segs = new CircularQueue(snd_cngWnd);
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

                while((snd_sndNxt - snd_rcvNxt) == snd_minWnd && snd_rcvWnd > 0 || zero_wnd_probe_ON){ //Comprueba si ha llegado el ACK y si se esta haciendo sondeo
                    appCV.awaitUninterruptibly();                                                      //snd_rcvWnd > 0 sirve para tratar el caso de ventana 0 aparte y poder sondear
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
                    snd_unacknowledged_segs.put(segment); //Guarda el segmento en la cola para poder retransmitirlo mas tarde
                    network.send(segment);

                    snd_sndNxt++;
                    if(snd_unacknowledged_segs.size() == 1){   //Activa solo el timer del primero sin reconocer
                        startRTO();
                    }


                }else{  //sondeo
                    bytesAPosar = 1; //Se envia solo un byte
                    segment = segmentize(data, offset, bytesAPosar);
                    quedenPerEnviar--; //Se envia solo un byte
                    offset++;

                    segment.setSeqNum(snd_sndNxt);
                    snd_unacknowledged_segs.put(segment); //Guarda el segmento de sondeo para poder sondearlo infinitivamente

                    snd_sndNxt++;
                    System.out.println("----- zero-window probe ON -----");
                    zero_wnd_probe_ON = true;
                    startRTO();
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
            if(!snd_unacknowledged_segs.empty()) {
                boolean primer = true;
                for (TCPSegment i : snd_unacknowledged_segs) {

                    if (zero_wnd_probe_ON) {
                        log.printPURPLE(("0-wnd probe: " + i));  //Solo hay un segmento: el de sondeo
                    } else {
                        log.printPURPLE("retrans: " + i);
                    }

                    network.send(i);
                    if(primer){
                        startRTO();
                        primer = false;
                    }
                }

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
    public void processReceivedSegment(TCPSegment rseg) {

        lock.lock();
        try {
            //throw new RuntimeException("//Completar...");
            if (rseg.isAck()){
                if(rseg.getAckNum() > snd_rcvNxt) {  //No se hace caso a los paquetes que piden retransmision (se espera a que salte el timeout)
                   

                    while(!snd_unacknowledged_segs.empty() && rseg.getAckNum() > snd_unacknowledged_segs.peekFirst().getSeqNum()){  //Vacía los paquetes reconocidos
                        snd_unacknowledged_segs.get();
                    }

                    snd_rcvNxt = rseg.getAckNum();

                    if(snd_unacknowledged_segs.empty()){
                        stopRTO();
                    }

                    appCV.signal();
                    snd_rcvWnd = rseg.getWnd(); //Guarda valor de la ventana
                    snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);

                    if (zero_wnd_probe_ON) {
                        zero_wnd_probe_ON = false;
                        System.out.println("----- zero-window probe OFF -----");
                    }
                }

            }

            if(rseg.isPsh()) {
                if(rseg.getSeqNum() == rcv_rcvNxt) { //Descarta segmentos fuera de secuencia
                    if (!rcv_Queue.full()) {
                        rcv_Queue.put(rseg);
                        rcv_rcvNxt++;
                        printRcvSeg(rseg);
                        sendAck();
                        appCV.signal();
                    }
                }else{  //Si se ha perdido el PSH y me llega el siguiente PSH envio el ACK pidiendo el PSH que se ha perdido
                    printRcvSeg(rseg);
                    sendAck();
                }
            }

        } finally {
            lock.unlock();
        }
    }

    private void unacknowledgedSegments_content() {
        Iterator<TCPSegment> ite = snd_unacknowledged_segs.iterator();
        log.printBLACK("\n-------------- content begins  --------------");
        while (ite.hasNext()) {
            log.printBLACK(ite.next().toString());
        }
        log.printBLACK("-------------- content ends    --------------\n");
    }
}