package by.mlechka.port.type;

public enum Action {
    LOAD (5),
    UNLOAD (5),
    LOAD_UNLOAD (10);

    private int time;

    Action(int time) {
        this.time = time;
    }
}
