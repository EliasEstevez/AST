package practica5;

import java.util.concurrent.locks.Condition;
import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TSocket_base;
import util.TCPSegment;

//**********************Este TSocket resuelve los problemas 19 y 20*********************

public class TSocket extends TSocket_base {

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

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        //this.zero_wnd_probe_ON = true;
        // init sender variables
        MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        // init receiver variables
        rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        snd_rcvWnd = Const.RCV_QUEUE_SIZE;
        //Añadido isma
        this.snd_sndNxt = 0;
        this.snd_rcvNxt = 0;
        this.rcv_rcvNxt = 0;

    }

    // -------------  SENDER PART  ---------------
    @Override
    public void sendData(byte[] data, int offset, int length) {
        lock.lock();
        try {
            TCPSegment paquetout;
            int length_actual = length;
            int mida_minima = Math.min(MSS, length);
            int offset_actual = offset;
            while (length_actual > 0) {
                while (this.snd_sndNxt != this.snd_rcvNxt) {
                    this.appCV.awaitUninterruptibly();
                }
                paquetout = segmentize(data, offset_actual, mida_minima);
                this.snd_UnacknowledgedSeg = paquetout;
                this.network.send(paquetout);
                //this.zero_wnd_probe_ON = false;
                this.snd_sndNxt++;

                // System.out.println("snd -> " + paquetout.toString());//Lo pongo para comprovar
                offset_actual = offset_actual + mida_minima;
                length_actual = length_actual - mida_minima;
                mida_minima = Math.min(this.MSS, length_actual);
                this.startRTO();

            }
            //throw new RuntimeException("//Completar...");
        } finally {
            lock.unlock();
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        //throw new RuntimeException("//Completar...");
        TCPSegment paquetout = new TCPSegment();
        paquetout.setData(data, offset, length);
        paquetout.setSourcePort(localPort);
        paquetout.setDestinationPort(remotePort);
        paquetout.setSeqNum(this.snd_sndNxt);
        paquetout.setPsh(true);
        return paquetout;
    }

    @Override
    protected void timeout() {
        lock.lock();
        try {
            //throw new RuntimeException("//Completar...");
            if (snd_UnacknowledgedSeg != null) {
                if (zero_wnd_probe_ON) {
                    log.printPURPLE("0−wnd probe: " + snd_UnacknowledgedSeg);
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
    public int receiveData(byte[] buf, int offset, int maxlen
    ) {
        lock.lock();
        try {
            //throw new RuntimeException("//Completar...");
            while (this.rcv_Queue.empty()) {
                this.appCV.awaitUninterruptibly();
            }
            int byte_consum = 0;
            int offset_actual = offset;
            int length_actual = maxlen;
            while (length_actual > 0 && !this.rcv_Queue.empty()) {
                byte_consum = byte_consum + consumeSegment(buf, offset_actual, length_actual);
                offset_actual = offset + byte_consum;
                length_actual = maxlen - byte_consum;
            }
            return byte_consum;
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
        TCPSegment ack_out = new TCPSegment();
        ack_out.setDestinationPort(remotePort);
        ack_out.setSourcePort(localPort);
        ack_out.setAck(true);
        ack_out.setWnd(this.rcv_Queue.free());
        ack_out.setAckNum(this.rcv_rcvNxt);
        this.snd_UnacknowledgedSeg = ack_out;
        this.network.send(ack_out);

    }

    // -------------  SEGMENT ARRIVAL  -------------
    @Override
    public void processReceivedSegment(TCPSegment rseg) {
        lock.lock();
        try {

            if (rseg.isPsh()) {

                if (this.rcv_rcvNxt == rseg.getSeqNum()) {
                    if (this.rcv_Queue.full()) {
                        System.out.println("SEGMENT DESCARTAT");
                        return;
                    } else {

                        this.rcv_Queue.put(rseg);
                        this.printRcvSeg(rseg);
                        this.rcv_rcvNxt++;
                        //this.Segment_ACK.setDestinationPort(rseg.getSourcePort());
                        //this.Segment_ACK.setSourcePort(rseg.getDestinationPort());
                        this.sendAck();

                        this.appCV.signalAll();
                        return;
                    }
                }
                if ((this.rcv_rcvNxt - 1) == rseg.getSeqNum()) {

                    this.printRcvSeg(rseg);
                    this.sendAck();
                }
                else{
                    System.out.println("ERROR");    
                }
            }

            if (rseg.isAck() && this.snd_sndNxt == rseg.getAckNum()) {
                // System.out.println("\t\t\trecived: " + rseg.toString());
                this.stopRTO();
                System.out.println("recived: " + rseg.toString());
                //this.zero_wnd_probe_ON = true;
                this.snd_rcvNxt = rseg.getAckNum();
                this.appCV.signalAll();

                //nothing to be done in this exercise.
            } else {
                System.out.println("ERROR");

            }
            //throw new RuntimeException("//Completar...");
        } finally {
            lock.unlock();
        }
    }
}
