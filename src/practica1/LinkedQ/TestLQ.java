package practica1.LinkedQ;

import java.util.Arrays;

public class TestLQ {

  public static void main(String[] args) {
    LinkedQueue<Integer> q = new LinkedQueue<>();
    //throw new RuntimeException("//Completar...");
    
      System.out.println("Esta llena="+q.full());
    System.out.println("Esta vacia="+q.empty());
    System.out.println("Añadimos elementos a la cola");
    for (int i = 0; i < 5; i++) {
          q.put(i);
          System.out.println(q.toString());
      }
   System.out.println("Cuanto espacio queda libre="+q.free());
   System.out.println("Dame el primer valor"+q.peekFirst());
   System.out.println("Numero de elementos en la cola="+q.size());
  }
}
