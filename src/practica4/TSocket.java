package practica4;

import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

public class TSocket extends TSocket_base {

    //sender variable:
    protected int MSS;

    //receiver variables:
    protected CircularQueue<TCPSegment> rcvQueue;
    protected int rcvSegConsumedBytes;
    //reciver variables Isma

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        MSS = network.getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        rcvQueue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        rcvSegConsumedBytes = 0;
    }

    @Override
    public void sendData(byte[] data, int offset, int length) {
        // throw new RuntimeException("//Completar...");
        TCPSegment paquetout;
        int length_actual = length;
        int mida_minima = Math.min(MSS, length);
        int offset_actual = offset;
        while (length_actual > 0) {
            paquetout = segmentize(data, offset_actual, mida_minima);
            this.network.send(paquetout);
            // System.out.println("snd -> " + paquetout.toString());//Lo pongo para comprovar
            offset_actual = offset_actual + mida_minima;
            length_actual = length_actual - mida_minima;
            mida_minima = Math.min(this.MSS, length_actual);

        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        //throw new RuntimeException("//Completar...");
        TCPSegment paquetout = new TCPSegment();
        paquetout.setData(data, offset, length);
        paquetout.setSourcePort(localPort);
        paquetout.setDestinationPort(remotePort);
        paquetout.setPsh(true);
        return paquetout;
    }

    @Override
    public int receiveData(byte[] buf, int offset, int length) {
        lock.lock();
        try {
            //throw new RuntimeException("//Completar...");
            while (this.rcvQueue.empty()) {
                this.appCV.awaitUninterruptibly();
            }
            int byte_consum = 0;
            int offset_actual = offset;
            int length_actual = length;
            while (length_actual > 0 && !this.rcvQueue.empty()) {
                byte_consum = byte_consum + consumeSegment(buf, offset_actual, length_actual);
                offset_actual = offset + byte_consum;
                length_actual = length - byte_consum;
            }
            return byte_consum;

        } finally {
            lock.unlock();
        }
    }

    protected int consumeSegment(byte[] buf, int offset, int length) {
        TCPSegment seg = rcvQueue.peekFirst();
        int a_agafar = Math.min(length, seg.getDataLength() - rcvSegConsumedBytes);
        System.arraycopy(seg.getData(), rcvSegConsumedBytes, buf, offset, a_agafar);
        rcvSegConsumedBytes += a_agafar;
        if (rcvSegConsumedBytes == seg.getDataLength()) {
            rcvQueue.get();
            rcvSegConsumedBytes = 0;
        }
        return a_agafar;
    }

    protected void sendAck() {
        TCPSegment Segment_ACK = new TCPSegment();
        Segment_ACK.setDestinationPort(remotePort);
        Segment_ACK.setSourcePort(localPort);        
        Segment_ACK.setAck(true);
        this.network.send(Segment_ACK);
        //throw new RuntimeException("//Completar...");

    }

    @Override
    public void processReceivedSegment(TCPSegment rseg) {

        lock.lock();
        try {
//throw new RuntimeException("//Completar...");
           
            if (rseg.isPsh()) {
                 if (this.rcvQueue.full()) {
                System.out.println("SEGMENT DESCARTAT");
                return;
            }
                 else{
                this.rcvQueue.put(rseg);
                this.printRcvSeg(rseg);

                //this.Segment_ACK.setDestinationPort(rseg.getSourcePort());
                //this.Segment_ACK.setSourcePort(rseg.getDestinationPort());
                this.sendAck();
                this.appCV.signalAll();
            }}
            if (rseg.isAck()) {
                // System.out.println("\t\t\trecived: " + rseg.toString());
                System.out.println("recived: " + rseg.toString());

                //nothing to be done in this exercise.
            } else {

            }

        } finally {
            lock.unlock();
        }
    }

}
