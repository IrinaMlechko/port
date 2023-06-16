package by.mlechka.port.entity;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {
    public static final int AMOUNT_OF_PIERS = 10;
    private static Port port;
    private static Lock lock = new ReentrantLock(true);
    private static AtomicBoolean isCreated = new AtomicBoolean();
    private ArrayDeque<Pier> piers;

    public Port() {
        piers = new ArrayDeque<>(AMOUNT_OF_PIERS);
        Pier pier = new Pier();
        piers.add(pier);
    }

    public static Port getInstance() {
        if (port == null) {
            try {
                lock.lock();
                if (!isCreated.get()) {
                    port = new Port();
                    isCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return port;
    }

    public Pier getPier(){
        Pier pier;
        lock.lock();
        try{
            pier = piers.poll();
        } finally {
            lock.unlock();
        }
        return pier;
    }
}
