package practica3;

import practica2.Protocol.SimNet_Monitor;
//import practica1.Protocol.TSocketRecv;

import util.Receiver;
import util.Sender;
import util.SimNet;

public class Test {

    public static void main(String[] args) {
        SimNet net = new SimNet_Loss(0.0);
         //SimNet net = new SimNet_Monitor();//Practica_3,Exercici_14
         
        new Sender(new TSocketSend(net), 5, 3000, 100).start();
        new Receiver(new TSocketRecv(net), 2000, 10).start();
        //Modificaciones de comprovación
      //  new Sender(new TSocketSend(net), 10, 1000, 100).start();
        //new Receiver(new TSocketRecv(net), 3000, 200).start();
    }
}
