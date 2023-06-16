package by.mlechka.port.factory;

import by.mlechka.port.entity.Ship;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class ShipFactory {
    public List<Ship> createShips(int count, int minCapacity, int maxCapacity) {
        List<Ship> ships = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int capacity = ThreadLocalRandom.current().nextInt(minCapacity, maxCapacity + 1);
            Ship ship = new Ship(i, capacity);
            addContainers(ship);
            ships.add(ship);
        }
        return ships;
    }

    private void addContainers(Ship ship) {
        int totalContainers = ship.getAmountOfContainers();
        ship.setAmountOfContainers(totalContainers);
    }

}
