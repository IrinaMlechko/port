package by.mlechka.port.entity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

//public class Pier {
//    private UUID id;
//    private AtomicBoolean available;
//
//    public Pier() {
//        this.id = UUID.randomUUID();
//        this.available = new AtomicBoolean(true);
//    }
//
//    public UUID getId() {
//        return id;
//    }
//    public boolean isAvailable() {
//        return available.get();
//    }
//
//    public void setAvailable(boolean available) {
//        this.available.set(available);
//    }
//}

public class Pier {
    private UUID id;
    private AtomicBoolean available;
//    private Ship ship;

    public Pier() {
        this.id = UUID.randomUUID();
        this.available = new AtomicBoolean(true);
//        this.ship = null;
    }

    public boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
    }

//    public Ship getShip() {
//        return ship;
//    }
//
//    public void setShip(Ship ship) {
//        this.ship = ship;
//    }

    public UUID getId() {
        return id;
    }
}
