package practica5;

import util.SimNet_FullDuplex;
import practica4.Protocol;
import util.Const;
import util.Receiver;
import util.Sender;
import util.SimNet;

public class Test {

    public static void main(String[] args) {
        /*//Excercici 19
        //SimNet_FullDuplex net = new SimNet_FullDuplex(0,0);
        */
        SimNet_FullDuplex net = new SimNet_FullDuplex(Const.LOSS_RATE_PSH, Const.LOSS_RATE_ACK);
        new Thread(new HostSnd(net.getSndEnd())).start();
        new Thread(new HostRcv(net.getRcvEnd())).start();
    }
}

class HostSnd implements Runnable {

    public static final int PORT = 10;

    protected Protocol proto;

    public HostSnd(SimNet net) {
        this.proto = new Protocol(net);
    }

    public void run() {
        new Sender(new TSocket_21(proto, HostSnd.PORT, HostRcv.PORT)).start();
    }
}

class HostRcv implements Runnable {

    public static final int PORT = 80;

    protected Protocol proto;

    public HostRcv(SimNet net) {
        this.proto = new Protocol(net);
    }

    public void run() {
        new Receiver(new TSocket_21(proto, HostRcv.PORT, HostSnd.PORT)).start();
    }
}
