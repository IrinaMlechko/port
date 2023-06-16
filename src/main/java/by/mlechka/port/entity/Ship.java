package by.mlechka.port.entity;

import by.mlechka.port.type.Action;

import java.util.UUID;

public class Ship extends Thread{
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

    @Override
    public void run(){
        Port port = Port.getInstance();
        Pier pier = port.takeFreePier();
        doAction(pier);
        port.putFreePier(pier);
    }

    private void doAction(Pier pier){
        Port port = Port.getInstance();
        for(int i=0; i< amountOfContainers; i++){
            if(actionType == Action.LOAD)
        }
    }
}
