package by.mlechka.port.entity;

import by.mlechka.port.type.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {
    static Logger logger = LogManager.getLogger();
    public static final int AMOUNT_OF_PIERS = 3;
    public static final int TIME_FOR_ONE_CONTAINER = 2;
    public static final int CAPACITY = 30;
    private static Port instance;
    private static Lock lock = new ReentrantLock(true);
    private static AtomicBoolean isCreated = new AtomicBoolean();
    private ArrayDeque<Pier> piers;
    private AtomicInteger currentAmountOfContainers = new AtomicInteger(10);
    private Lock pierLock = new ReentrantLock();

    public Port() {
        piers = new ArrayDeque<>(AMOUNT_OF_PIERS);
        for (int i = 0; i < AMOUNT_OF_PIERS; i++) {
            Pier pier = new Pier(i+1);
            piers.add(pier);
        }
    }

    public static Port getInstance() {
        if (instance == null) {
            try {
                lock.lock();
                if (!isCreated.get()) {
                    instance = new Port();
                    isCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public Pier acquirePier() throws InterruptedException {
        pierLock.lock();
        try {
            return getAvailablePier();
        } finally {
            pierLock.unlock();
        }
    }

    public void releasePier(Pier pier) {
        pierLock.lock();
        try {
            pier.setAvailable(true);
        } finally {
            pierLock.unlock();
        }
    }

    private Pier getAvailablePier() {
        for (Pier pier : piers) {
            if (pier.isAvailable()) {
                pier.setAvailable(false);
                return pier;
            }
        }
        return null;
    }

    public void processShip(Ship ship) throws InterruptedException {
        int shipsRemaining = 1;

        while (shipsRemaining > 0) {
            if (!isActionAllowed(ship)) {
                logger.info("Ship {} is waiting for action: {}", ship.getShipId(), ship.getActionType());
                TimeUnit.SECONDS.sleep(1);
                continue;
            }

            Pier pier = null;
            try {
                pier = acquirePier();
                if (pier != null) {
                    logger.info(String.format("Ship with id %s docked to the pier %s", ship.getShipId(), pier.getId()));
                    performAction(ship, pier);
                    shipsRemaining--;
                }
            } finally {
                if (pier != null) {
                    pier.setAvailable(true);
                }
            }
        }
    }


    private boolean isActionAllowed(Ship ship) {
        Action actionType = ship.getActionType();
        if (actionType == Action.LOAD) {
            int availableContainers = currentAmountOfContainers.get();
            return availableContainers >= ship.CAPACITY;
        } else if (actionType == Action.UNLOAD) {
            int availableSpace = CAPACITY - currentAmountOfContainers.get();
            return availableSpace >= ship.getCurrentAmountOfContainers();
        } else if (actionType == Action.LOAD_UNLOAD) {
            int availableSpace = CAPACITY - currentAmountOfContainers.get();
            int availableContainers = currentAmountOfContainers.get();
            return availableContainers >= ship.CAPACITY && availableSpace >= ship.getCurrentAmountOfContainers();
        }
        return false;
    }

    private void performAction(Ship ship, Pier pier) throws InterruptedException {
        logger.info("Ship {} is performing action: {}", ship.getShipId(), ship.getActionType());
        int containersLoaded = 0;
        if (ship.getActionType() == Action.LOAD || ship.getActionType() == Action.LOAD_UNLOAD) {
            containersLoaded = loadContainers(ship);
        }
        if (ship.getActionType() == Action.UNLOAD || ship.getActionType() == Action.LOAD_UNLOAD) {
            unloadContainers(ship);
        }
        logger.info("Ship {} finished action: {}", ship.getShipId(), ship.getActionType());
        releasePier(pier, ship, containersLoaded);
    }

    private int loadContainers(Ship ship) throws InterruptedException {
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
        int availableContainers = currentAmountOfContainers.get();
        int containersToLoad = Math.min(availableContainers, ship.CAPACITY);
        TimeUnit.SECONDS.sleep(TIME_FOR_ONE_CONTAINER * containersToLoad);
        currentAmountOfContainers.addAndGet(-containersToLoad);
        ship.setCurrentAmountOfContainers(containersToLoad);
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() + " capacity " + ship.CAPACITY);        logger.debug("action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
        return containersToLoad;
    }

    private void unloadContainers(Ship ship) throws InterruptedException {
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
        int containersToUnload = ship.getCurrentAmountOfContainers();
        TimeUnit.SECONDS.sleep(TIME_FOR_ONE_CONTAINER * containersToUnload);
        currentAmountOfContainers.addAndGet(containersToUnload);
        ship.setCurrentAmountOfContainers(0);
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
    }

    private void releasePier(Pier pier, Ship ship, int containersLoaded) {
//        if (ship.getActionType() == Action.UNLOAD) {
//            currentAmountOfContainers.addAndGet(-containersLoaded);
//            logger.debug("pier released. " +
//                    " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
//        }
        pier.setAvailable(true);
    }
}
