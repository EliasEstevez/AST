package practica4;

import java.util.Iterator;
import util.Protocol_base;
import util.TCPSegment;
import util.SimNet;
import util.TSocket_base;

public class Protocol extends Protocol_base {

    public Protocol(SimNet net) {
      super(net);
    }

    protected void ipInput(TCPSegment seg) {
       // throw new RuntimeException("//Completar...");
       TSocket_base TSocket_Actual=this.getMatchingTSocket(seg.getDestinationPort(), seg.getSourcePort());
       if(TSocket_Actual!=null);
       {
         TSocket_Actual.processReceivedSegment(seg);
       }
    }

    protected TSocket_base getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            TSocket_base aux=null;
            Iterator iterador=this.activeSockets.iterator();
            while(iterador.hasNext()){
               aux=(TSocket_base) iterador.next();
               if((aux.localPort==localPort) && (aux.remotePort==remotePort))
               {
                   return aux;
               }
            }
            return null;
            //throw new RuntimeException("//Completar...");
        } finally {
            lk.unlock();
        }
    }
}
