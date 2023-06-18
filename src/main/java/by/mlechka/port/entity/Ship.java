package by.mlechka.port.entity;

import by.mlechka.port.exception.CustomException;
import by.mlechka.port.type.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Ship extends Thread{
    static Logger logger = LogManager.getLogger();
    private UUID id;
    private int currentAmountOfContainers;
    private int capacity;
    private Action actionType;

    public Ship(int currentAmountOfContainers, int capacity, Action actionType) {
        this.id = UUID.randomUUID();
        this.currentAmountOfContainers = currentAmountOfContainers;
        this.capacity = capacity;
        this.actionType = actionType;
    }

    public Action getActionType() {
        return actionType;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentAmountOfContainers() {
        return currentAmountOfContainers;
    }

    @Override
    public void run() {
        logger.debug("run started. ship " + this.id);
        Port port = Port.getInstance();
        try {
            port.processShip(this);
        } catch (InterruptedException e) {
            logger.error("Ship {} was interrupted.", id);
            Thread.currentThread().interrupt();
        }
    }
}
