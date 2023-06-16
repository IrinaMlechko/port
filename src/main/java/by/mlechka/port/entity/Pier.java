package by.mlechka.port.entity;

import java.util.UUID;

public class Pier {
    private UUID id;

    public Pier() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }
}
