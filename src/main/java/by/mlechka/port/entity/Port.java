package by.mlechka.port.entity;

import by.mlechka.port.type.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.concurrent.Semaphore;
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
    private Semaphore semaphore = new Semaphore(AMOUNT_OF_PIERS);

    public Port() {
        piers = new ArrayDeque<>(AMOUNT_OF_PIERS);
        for (int i = 0; i < AMOUNT_OF_PIERS; i++) {
            Pier pier = new Pier(i + 1);
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

    public void processShip(Ship ship) throws InterruptedException {
        Optional<Pier> optionalPier = acquirePier();
        if(optionalPier.isPresent()) {
            Pier pier = optionalPier.get();
            try {
                logger.info(String.format("Ship with id %s docked to the pier %s", ship.getShipId(), pier.getId()));
                if (isActionAllowed(ship)) {
                    performAction(ship, pier);
                } else {
                    logger.info("Ship {} is waiting for action: {}", ship.getShipId(), ship.getActionType());
                    TimeUnit.SECONDS.sleep(1);
                }
            } finally {
                releasePier(pier);
            }
        }
    }

    public Optional<Pier> acquirePier() throws InterruptedException {
        semaphore.acquire();
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
            semaphore.release();
        }
    }

    private Optional<Pier> getAvailablePier() {
        for (Pier pier : piers) {
            if (pier.isAvailable()) {
                pier.setAvailable(false);
                return Optional.of(pier);
            }
        }
        return Optional.empty();
    }


    private boolean isActionAllowed(Ship ship) {
        Action actionType = ship.getActionType();
        int availableContainers = currentAmountOfContainers.get();
        int availableSpace = CAPACITY - currentAmountOfContainers.get();
        int shipCapacity = ship.CAPACITY;
        int shipContainers = ship.getCurrentAmountOfContainers();

        if (actionType == Action.LOAD) {
            return availableContainers >= shipCapacity;
        } else if (actionType == Action.UNLOAD) {
            return availableSpace >= shipContainers;
        } else if (actionType == Action.LOAD_UNLOAD) {
            return availableContainers >= shipCapacity && availableSpace >= shipContainers;
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
        releasePier(pier);
        if (ship.getActionType() == Action.UNLOAD) {
            currentAmountOfContainers.addAndGet(-containersLoaded);
        }
    }

    private int loadContainers(Ship ship) throws InterruptedException {
        int availableContainers = currentAmountOfContainers.get();
        int containersToLoad = Math.min(availableContainers, ship.CAPACITY);
        TimeUnit.SECONDS.sleep(TIME_FOR_ONE_CONTAINER * containersToLoad);
        currentAmountOfContainers.addAndGet(-containersToLoad);
        ship.setCurrentAmountOfContainers(containersToLoad);
        return containersToLoad;
    }

    private void unloadContainers(Ship ship) throws InterruptedException {
        int containersToUnload = ship.getCurrentAmountOfContainers();
        TimeUnit.SECONDS.sleep(TIME_FOR_ONE_CONTAINER * containersToUnload);
        currentAmountOfContainers.addAndGet(containersToUnload);
        ship.setCurrentAmountOfContainers(0);
    }

}