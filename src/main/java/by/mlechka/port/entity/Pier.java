package by.mlechka.port.entity;

import java.util.concurrent.atomic.AtomicBoolean;

public class Pier {
    private int id;
    private AtomicBoolean available;

    public Pier(int id) {
        this.id = id;
        this.available = new AtomicBoolean(true);
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
    }

    public int getId() {
        return id;
    }
}
