package practica1.LinkedQ;

import java.util.Iterator;
import util.Queue;

public class LinkedQueue<E> implements Queue<E> {

    //Completar
    protected Node fstNode;
    protected Node utmNode;
    protected int numelem = 0;

    @Override
    public int size() {
        return numelem;
    }

    @Override
    public int free() {
        //throw new UnsupportedOperationException();
        return 1;
    }

    @Override
    public boolean empty() {
        //throw new RuntimeException("//Completar...");
        return this.numelem == 0;
    }

    @Override
    public boolean full() {
        return false;
    }

    @Override
    public E peekFirst() {
        if (empty()) {
            System.out.println("Cola vacia");
        }
        return (E) this.fstNode.getValue();
    }

    @Override
    public E get() {
        if (empty()) {
            throw new RuntimeException("Cola vacia");
        }
        E aux = (E) this.fstNode.getValue();
        this.fstNode = this.fstNode.getNext();
        this.numelem--;
        return aux;
    }

    @Override
    public void put(E e) {
        //throw new RuntimeException("");
        Node nouNode = new Node();
        nouNode.setValue(e);
        nouNode.setNext(null);
        if (this.numelem == 0) {
            this.fstNode = nouNode;
            this.utmNode = nouNode;
        } else {
            this.utmNode.setNext(nouNode);
            this.utmNode = nouNode;
        }
        this.numelem++;
    }

    @Override
    public String toString() {
        if (empty()) {

            return "[]";
        }
        String contenido = "[";
        Node aux = this.fstNode;
        for (int i = 0; i < this.numelem; i++) {
            if (i == this.numelem - 1) {
                contenido = contenido + this.utmNode.getValue() + "]";
            } else {
                contenido += aux.getValue() + ",";
                aux = aux.getNext();
            }
        }
        return contenido;
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator {

        //Completar...
        Node NodeActual = fstNode;
        Node NodeAnterior = null;
        Node NodeAntAnt = null;
        Node Nodeaux = null;

        @Override
        public boolean hasNext() {
            //if(empty())throw new RuntimeException("Cola vacia");
            if (numelem == 0 || this.NodeAnterior == utmNode) {
                this.NodeActual = fstNode;
                this.NodeAnterior = null;
                this.NodeAntAnt = null;
                this.Nodeaux = null;
                return false;
            } else {
                return NodeActual != null;
                //return true;
            }

        }

        @Override
        public E next() {
            E aux;
            aux = (E) this.NodeActual.getValue();
            if (numelem > 1) {
                if (this.NodeAnterior != null) {
                    this.NodeAntAnt = this.NodeAnterior;
                }
                if (numelem > 2) {
                    if (this.NodeAntAnt != null) {
                        this.Nodeaux = this.NodeAntAnt;
                    }
                }
            }
            this.NodeAnterior = this.NodeActual;
            this.NodeActual = this.NodeActual.getNext();
            return aux;

        }

        @Override
        public void remove() {
            if (empty()) {
                throw new RuntimeException("Cola vacia");
            }
            if (this.NodeAnterior == fstNode) {
                //System.out.println("***********+Hola********");
                fstNode = fstNode.getNext();
                this.NodeAnterior = null;
                fstNode = this.NodeActual;//redundancia
            } else if (this.NodeAnterior == utmNode) {

                this.NodeAntAnt.setNext(null);
                //Posible correcion this.NodeActaul=NADA XD
                //this.NodeActual=this.NodeAnterior;
                this.NodeAnterior = this.NodeAntAnt;
                utmNode = this.NodeAnterior;
                if (this.Nodeaux != null) {
                    this.NodeAntAnt = this.Nodeaux;
                } else {
                    this.NodeAntAnt = null;
                    this.Nodeaux = null;
                }
            } else {
                this.NodeAntAnt.setNext(this.NodeActual);
                this.NodeAnterior = this.NodeAntAnt;
                if (numelem - 1 > 3) {
                    this.NodeAntAnt = this.Nodeaux;
                }

            }
            //Completar...
            numelem--;
        }

    }
}
