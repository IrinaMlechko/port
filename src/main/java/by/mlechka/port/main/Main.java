package by.mlechka.port.main;

import by.mlechka.port.entity.Ship;
import by.mlechka.port.exception.CustomException;
import by.mlechka.port.factory.ShipFactory;

import java.util.List;

public class Main {

    public static void main(String[] args) throws CustomException {
        ShipFactory shipFactory = new ShipFactory();
        List<Ship> ships = shipFactory.createShips(5);
        for (Ship ship : ships) {
            ship.start();
        }
    }
}
