package by.mlechka.port.factory;

import by.mlechka.port.entity.Ship;
import by.mlechka.port.type.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ShipFactory {
    static Logger logger = LogManager.getLogger();

    private static Action getRandomAction() {
        Random random = new Random();
        Action[] actions = Action.values();
        int index = random.nextInt(actions.length);
        return actions[index];
    }

    public List<Ship> createShips(int amount, int minCapacity, int maxCapacity) {
        List<Ship> ships = new ArrayList<>();
        int capacity = ThreadLocalRandom.current().nextInt(minCapacity, maxCapacity + 1);
        for (int i = 0; i < amount; i++) {
            ships.add(createShip(capacity, getRandomAction()));
        }
        return ships;
    }

    public Ship createShip(int capacity, Action action) {
        int currentAmountOfContainers = 0;

        if (action == Action.UNLOAD || action == Action.LOAD_UNLOAD) {
            currentAmountOfContainers = capacity;
        }
        Ship ship = new Ship(currentAmountOfContainers, capacity, action);
        logger.info(String.format("Ship with max capacity %s has been created and loaded with %s containers with a task %s", capacity, currentAmountOfContainers, action));
        return ship;
    }

}
