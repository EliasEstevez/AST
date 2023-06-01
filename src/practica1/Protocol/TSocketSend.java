package practica1.Protocol;

import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketSend extends TSocket_base {

  public TSocketSend(SimNet net) {
    super(net);
  }

  @Override
  public void sendData(byte[] data, int offset, int length) {
    //throw new RuntimeException("//Completar...");
    TCPSegment paquetout=new TCPSegment();
    paquetout.setData(data, offset, length);
    paquetout.setPsh(true);
    this.network.send(paquetout);
    System.out.println("snd --> "+paquetout.toString());
      
    
  }
}
