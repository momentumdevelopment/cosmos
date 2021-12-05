package cope.cosmos.client.managment.managers;

import cope.cosmos.client.managment.Manager;

public class AnimationManager extends Manager {

    private final int time;

    private boolean initialState;
    private State previousState = State.STATIC;
    private State currentState = State.STATIC;
    private long currentStateStart = 0;

    public AnimationManager(int time, boolean initialState) {
        super("AnimationManager", "Manages simple two-way animations");

        this.time = time;
        this.initialState = initialState;

        if (initialState) {
            previousState = State.EXPANDING;
        }
    }

    public double getAnimationFactor() {
        if (currentState == State.EXPANDING) {
            return (System.currentTimeMillis() - currentStateStart) / (double) time;
        }

        if (currentState == State.RETRACTING) {
            return ((long) time - (System.currentTimeMillis() - currentStateStart)) / (double) time;
        }

        return previousState == State.EXPANDING ? 1 : 0;
    }

    public boolean getState() {
        return initialState;
    }

    public void setState(boolean expand) {
        if (expand) {
            currentState = State.EXPANDING;
            initialState = true;
        }

        else {
            currentState = State.RETRACTING;
        }

        currentStateStart = System.currentTimeMillis();
    }

    public void setStateHard(boolean expand) {
        if (expand) {
            currentState = State.EXPANDING;
            initialState = true;
            currentStateStart = System.currentTimeMillis();
        }

        else {
            previousState = State.RETRACTING;
            currentState = State.RETRACTING;
            initialState = false;
        }
    }

    public enum State {
        EXPANDING, RETRACTING, STATIC
    }
}
