package by.mlechka.composite.entity;

public class Ship extends Thread{
    private int id;
    private int amountOfContainers;

    public Ship(int id, int amountOfContainers) {
        this.id = id;
        this.amountOfContainers = amountOfContainers;
    }

    public int getAmountOfContainers() {
        return amountOfContainers;
    }

    public void setAmountOfContainers(int amountOfContainers) {
        this.amountOfContainers = amountOfContainers;
    }
}
