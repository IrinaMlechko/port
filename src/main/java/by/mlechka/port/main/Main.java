package by.mlechka.port.main;

import by.mlechka.port.entity.Port;
import by.mlechka.port.entity.Ship;
import by.mlechka.port.exception.CustomException;
import by.mlechka.port.factory.ShipFactory;
import by.mlechka.port.type.Action;

import java.util.List;

public class Main {

    public static void main(String[] args) throws CustomException {
        ShipFactory shipFactory = new ShipFactory();
        List<Ship> ships = shipFactory.createShips(5,5, 10);
        for(Ship ship:ships){
            ship.start();
        }
    }
}
