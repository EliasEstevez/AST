package practica2.Protocol;

import practica1.Protocol.SimNet_Queue;
import practica1.Protocol.TSocketRecv;
import practica1.Protocol.TSocketSend;
//import practica3.TSocketSend;
import util.Receiver;
import util.Sender;
import util.TCPSegment;
import util.SimNet;

public class Test {

  public static void main(String[] args) throws InterruptedException {
    
    TCPSegment.SHOW_DATA = true;
    
    //<--Excercici_11
    /*
    SimNet net        = new SimNet_Queue();
    Sender sender     = new Sender(new TSocketSend(net), 10, 1, 100);
    Receiver receiver = new Receiver(new TSocketRecv(net), 1, 25); // si el delay del reciver es mas pequeño que 
                                                                   // el del sender habra error(cola vacia)
   //Completar (trobar una manera que demostri que la xarxa utilitzada no funciona bé per aquest cas)
    sender.start();
    receiver.start();
    */
    
    //<--Excercici12
    
    SimNet net        = new SimNet_Monitor();
    Sender sender     = new Sender(new TSocketSend(net), 10, 1, 100);
    Receiver receiver = new Receiver(new TSocketRecv(net), 1, 20);//Comprovacion para el peor caso 
    sender.start();
    receiver.start();
    
  }
}
