package by.mlechka.port.entity;

import by.mlechka.port.type.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ship extends Thread {
    static Logger logger = LogManager.getLogger();
    public static final int CAPACITY = 10;
    private final Action actionType;
    private int id;
    private int currentAmountOfContainers;


    public Ship(int id, int currentAmountOfContainers, Action actionType) {
        this.id = id;
        this.currentAmountOfContainers = currentAmountOfContainers;
        this.actionType = actionType;
    }

    public int getShipId() {
        return id;
    }

    public Action getActionType() {
        return actionType;
    }

    public int getCurrentAmountOfContainers() {
        return currentAmountOfContainers;
    }

    public void setCurrentAmountOfContainers(int currentAmountOfContainers) {
        this.currentAmountOfContainers = currentAmountOfContainers;
    }

    @Override
    public void run() {
        Port port = Port.getInstance();
        try {
            port.processShip(this);
        } catch (InterruptedException e) {
            logger.error("Ship {} was interrupted.", id);
            Thread.currentThread().interrupt();
        }
    }
}
