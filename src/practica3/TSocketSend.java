package practica3;

import util.Const;
import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketSend extends TSocket_base {

    protected int MSS;       // Maximum Segment Size

    public TSocketSend(SimNet net) {
        super(net);
        MSS = net.getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
    }

    @Override
    public void sendData(byte[] data, int offset, int length) {
        //throw new RuntimeException("//Completar...");

        TCPSegment paquetout;
        int length_actual = length;
        int mida_minima = Math.min(MSS, length);
        int offset_actual = offset;
        while (length_actual > 0) {
            paquetout = segmentize(data, offset_actual, mida_minima);
            this.network.send(paquetout);
            System.out.println("snd --> " + paquetout.toString());
            offset_actual = offset_actual + mida_minima;
            length_actual = length_actual - mida_minima;
            mida_minima = Math.min(this.MSS, length_actual);

        }

    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        //throw new RuntimeException("//Completar...");
        TCPSegment paquetout = new TCPSegment();
        paquetout.setData(data, offset, length);
        paquetout.setPsh(true);
        return paquetout;

    }

}
