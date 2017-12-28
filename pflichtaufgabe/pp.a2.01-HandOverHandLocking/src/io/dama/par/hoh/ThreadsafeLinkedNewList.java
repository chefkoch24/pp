package io.dama.par.hoh;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadsafeLinkedNewList<T> implements NewList<T> {
    private class ListElement<T> {
        private T                    element;
        private ListElement<T>       prev;
        private final ListElement<T> next;
        public ReentrantLock lock = new ReentrantLock();

        private ListElement(final T element, final ListElement<T> prev, final ListElement<T> next) {
            this.element = element;
            this.prev = prev;
            this.next = next;
        }
    }

    private ListElement<T> first;

    public ThreadsafeLinkedNewList() {
        this.first = null;
    }

    @Override
    public T get(final int i) {
    	int j = 0;
        ListElement<T> ptr =this.first;
        
        Lock current = ptr.lock;
        current.lock();
        while (j++ < i) {
        	// das nächste Listenelement wird kurzfristig gelockt
        	Lock next = ptr.next.lock;
        	next.lock();
        	// das aktuelle Listenelement wird das aktuelle Listenelement
        	ptr = ptr.next;
        	// das vorherige Element wird freigegeben
        	current.unlock();
        	// das aktuelle Element wird gesetzt
        	current = next;
        	
        }        
        try{
        	return ptr.element;
        }
        finally{
        	// Freigabe des ausgegebenen Elementes
        	current.unlock();
        }
        
    }

    // einfügen an erster Stelle
    @Override
    public void add(final T e) {
        final ListElement<T> insert = new ListElement<>(e, null, this.first);
        if (this.first != null) {
        	// erste Element locken
        	Lock current = this.first.lock;
        	current.lock();
            // vor dem ersten Element Einfügen
        	this.first.prev = insert;
            // Lock vom ersten Element entfernen
        	current.unlock();
        }
        this.first = insert;       
    }

    // Ändern des Wertes an bstimmter Position
    @Override
    public void mod(final int i, final T e) {
        int j = 0;
        ListElement<T> ptr = this.first;
        
        //gleiche Prinzip wie beim get
        Lock current = ptr.lock;
        current.lock();
        while (j++ < i) {
        	Lock next = ptr.next.lock;
        	next.lock();
            ptr = ptr.next;
            current.unlock();
        	current = next;
        }
        ptr.element = e;
        current.unlock();
    }

}