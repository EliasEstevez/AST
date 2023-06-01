package practica1.CircularQ;

import java.util.Iterator;
import util.Queue;

public class CircularQueue<E> implements Queue<E> {

    private final E[] queue;
    private final int N;
    //variables añadidas
    protected int head, tail, numelem;
    //Completar...

    public CircularQueue(int N) {
        this.N = N;
        queue = (E[]) (new Object[N]);
        this.head = 0; //pointer inicio cola
        this.tail = 0; //pointer al sitio donde pondremos el nuevo elemento 
        this.numelem = 0; //numero de elementos en la cola
    }

    @Override
    public int size() {
        //throw new RuntimeException("//Completar...");
        return this.numelem;
    }

    @Override
    public int free() {
        //throw new RuntimeException("//Completar...");
        return this.N - this.numelem;
    }

    @Override
    public boolean empty() {
        //throw new RuntimeException("//Completar...");
        return this.numelem == 0;
    }

    @Override
    public boolean full() {
        //throw new RuntimeException("//Completar...");
        return this.numelem == this.N;
    }

    @Override
    public E peekFirst() {
        //throw new RuntimeException("//Completar...");
        return this.queue[head];
    }

    @Override
    public E get() {
        if (this.empty()) {
            throw new RuntimeException("***COLA VACIA**");
        }
        E aux;
        aux = this.queue[this.head]; //Guardamos el elemento en la variable auxiliar
        this.queue[this.head] = null; //Eliminamos el elemento que apunta el head
        this.head = (this.head + 1) % N; //avanzamos el head
        this.numelem = this.numelem - 1; //decrementamos el numero de elementos en la cola
        return aux;

    }

    @Override
    public void put(E e) {
        if (this.full()) {
            throw new RuntimeException("***COLA LLENA***");
        }
        this.queue[this.tail] = e; //Añadimos el nuevo elemento en el tail actual 
        this.tail = (this.tail + 1) % this.N; //Desplazamos una posicion el tail (acutalizandolo)
        this.numelem++;

    }

    @Override
    public String toString() {
        if (empty()) {
            throw new RuntimeException("***COLA VACIA***");
        }
        String text = "[";
        for (int i = 0; i < this.numelem; i++) {
            if (i < this.numelem - 1) {
                text = text + queue[(i + head) % N] + ",";
            } else {
                text = text + queue[(i + head) % N] + "]";
            }
        }
        return text;

    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator {

        //Completar...
        protected int posi = head; //Puntero de partida del iterador
        protected int posiant = 0; //Queremos saber la posiscion anteriror por si hacemos un remove()
        protected int numiter = 0; //Numero de iteraciones echas

        @Override
        public boolean hasNext() {

            if (empty()) {  //comprovamos que no este vacio

                //throw new RuntimeException("No hi ha elements");
                return false;
            }
            if (this.posi == tail && this.numiter==numelem) { //si ya ha llegado al ultimo elemento de la cola reinicia la iteracion
                posi = head;
                numiter = 0;

                return false;

            } else {

                return true;
            }
        }

        @Override
        public E next() {
            E aux;
            aux = queue[posi]; //devuelve el elemento que indica el hasNext()
            this.posiant = posi; //activamos la variable auxiliar por si hay que hacer un remove()apuntando al elemento proporcionado
            //<-- el %N ha d'afectar a la suma, no només a l'1
            posi = (posi + 1) % N; //avanzamos el ponter 
            this.numiter++; //ha havanzado una posicion por tanto incrementa la variable
            return aux;
        }

        @Override
        public void remove() {
            //System.out.println("HOLAaaaaaaa");
            if (empty()) {
                throw new RuntimeException("No hay elementos");
            }
            if (this.posiant == head) {
                queue[posiant] = null;
                head = this.posi;

            } else {
                for (int i = 0; i < numelem - this.numiter + 1; i++) {
                    if (((this.posi + i) % N) == tail) {
                        /*Si ya ha llegado al ultimo elemento pondra dicho elemento como
                    null y actualizando el tail que tendra una posicioin menos a la que tenia*/
                        queue[(this.posiant + i) % N] = null;//<-- falta %N en aquesta i altres intruccions de sota
                        tail = (this.posiant + i) % N;
                        this.posi = this.posiant;
                        this.posiant = (this.posiant + N - 1) % N;

                    } else {
                        queue[(this.posiant + i) % N] = queue[(this.posi + i) % N];
                        /*Aqui vamos moviendo los elementos despues del
                    elemento a borrar una posicion para que se acutalice la cola*/

                    }

                }

            }

            numelem--;
            this.numiter--;

        }

    }
}
