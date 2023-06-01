package practica1.Protocol;

import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketRecv extends TSocket_base {

    public TSocketRecv(SimNet net) {
        super(net);
    }

    @Override
    public int receiveData(byte[] data, int offset, int length) {
        //throw new RuntimeException("//Completar...");

        TCPSegment paquetin = this.network.receive();

        printRcvSeg(paquetin);

        //<-- millor utilitzar el mètode de TCPSegment: getDataLength()
        int min = Math.min(paquetin.getDataLength(), length);
        //System.out.println(data.length);
        //<-- els offsets estan al revés
        System.arraycopy(paquetin.getData(), 0, data, offset, min);
        
        return min;
    }
}
