package by.mlechka.port.entity;

import by.mlechka.port.type.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {
    static Logger logger = LogManager.getLogger();
    public static final int AMOUNT_OF_PIERS = 3;
    public static final int TIME_FOR_ONE_CONTAINER = 2;
    public static final int CAPACITY = 50;
    private static Port instance;
    private static Lock lock = new ReentrantLock(true);
    private static AtomicBoolean isCreated = new AtomicBoolean();
    private ArrayDeque<Pier> piers;
    private AtomicInteger currentAmountOfContainers = new AtomicInteger(10);
    private Lock pierLock = new ReentrantLock();
    private Condition pierIsFree = lock.newCondition();

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
                    instance.scheduleTimerTask();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public Pier getPier() throws InterruptedException {
        lock.lock();
        try {
            while (piers.isEmpty()) {
                pierIsFree.await();
            }
            return piers.poll();
        } finally {
            lock.unlock();
        }
    }

    public void releasePier(Pier pier) {
        lock.lock();
        try {
            piers.add(pier);
            pierIsFree.signal();
        } finally {
            lock.unlock();
        }
    }

    public void processShip(Ship ship) throws InterruptedException {
            Pier pier = null;
            try {
                pier = getPier();
                if (pier != null) {
                    logger.info(String.format("Ship with id %s docked to the pier %s", ship.getShipId(), pier.getId()));
                    if (!isActionAllowed(ship)) {
                        logger.info("Ship {} is waiting for action: {}", ship.getShipId(), ship.getActionType());
                        TimeUnit.SECONDS.sleep(1);
                    }
                    performAction(ship, pier);
                }
            } finally {
                if (pier != null) {
                    pier.setAvailable(true);
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
        releasePier(pier);
    }

    private int loadContainers(Ship ship) throws InterruptedException {
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
        int availableContainers = currentAmountOfContainers.get();
        int containersToLoad = Math.min(availableContainers, ship.CAPACITY);
        logger.debug("Loading: Amount of containers to load " + containersToLoad);
        TimeUnit.SECONDS.sleep(TIME_FOR_ONE_CONTAINER * containersToLoad);
        logger.debug("Loading: Amount of containers in port before " + currentAmountOfContainers);
        currentAmountOfContainers.addAndGet(-containersToLoad);
        logger.debug("Loading: Amount of containers in port after " + currentAmountOfContainers);
        ship.setCurrentAmountOfContainers(containersToLoad);
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() + " capacity " + ship.CAPACITY);        logger.debug("action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
        return containersToLoad;
    }

    private void unloadContainers(Ship ship) throws InterruptedException {
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
        int containersToUnload = ship.getCurrentAmountOfContainers();
        logger.debug("Unloading: Amount of containers to unload " + containersToUnload);
        TimeUnit.SECONDS.sleep(TIME_FOR_ONE_CONTAINER * containersToUnload);
        logger.debug("Unloading: Amount of containers in port before " + currentAmountOfContainers);
        currentAmountOfContainers.addAndGet(containersToUnload);
        logger.debug("Unloading: Amount of containers in port after " + currentAmountOfContainers);
        ship.setCurrentAmountOfContainers(0);
        logger.debug("ship " + ship.getShipId() + " action: " + ship.getActionType() +
                " current amount of containers in port " + currentAmountOfContainers + " current amount of containers in ship " + ship.getCurrentAmountOfContainers());
    }

    private void scheduleTimerTask() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                checkPortLoad();
            }
        };

        Timer timer = new Timer(true);
        long delay = 0L;
        long period = TimeUnit.SECONDS.toMillis(10);
        timer.scheduleAtFixedRate(timerTask, delay, period);
    }

    private void checkPortLoad() {
        int currentContainers = currentAmountOfContainers.get();
        double loadPercentage = (double) currentContainers / CAPACITY;
        if (loadPercentage >= 0.75) {
            int containersToRemove = (int) (currentContainers * 0.5);
            currentAmountOfContainers.addAndGet(-containersToRemove);
            logger.info("Removing {} containers from the port due to high load", containersToRemove);
        } else if (currentContainers == 0) {
            int containersToAdd = (int) (CAPACITY * 0.25);
            currentAmountOfContainers.addAndGet(containersToAdd);
            logger.info("Adding {} containers to the port", containersToAdd);
        }
    }

}

