package tum_model;

import core.Coord;
import core.Settings;
import movement.MovementModel;

import java.util.List;

/**
 * Created by lorenzodonini on 10/11/15.
 */
public abstract class TumCharacter extends MovementModel {

    protected Coord lastWaypoint;
    protected boolean bHasEaten;
    protected double bathroomProbability;
    protected int attendedLectures;
    protected int scheduledLectures;
    private IState currentState;
    private TumAction currentAction;


    //CTOR
    public TumCharacter(final Settings settings) {
        super(settings);
        //Other stuff
    }

    public TumCharacter(final TumCharacter other) {
        super(other);
        //Copy state and other stuff
    }

    //PUBLIC ACCESSORS
    public Coord getLastLocation() {
        return lastWaypoint.clone();
    }

    public double getDefaultSpeed() {
        return super.generateSpeed();
    }

    public boolean hasEaten() {
        return bHasEaten;
    }

    public void setEaten(boolean bEaten) {
        bHasEaten = bEaten;
    }

    public double getBathroomProbability() {
        return bathroomProbability;
    }

    public void setBathroomProbability(double probability) {
        bathroomProbability = probability;
    }

    public int getScheduledLectures() {
        return scheduledLectures;
    }

    public int getAttendedLectures() {
        return attendedLectures;
    }

    public void incrementAttendedLectures() {
        attendedLectures++;
    }

    public TumAction getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(TumAction action) {
        currentAction = action;
    }

    public IState getCurrentState() {
        return currentState;
    }

    public void setNewState(IState state) {
        if (currentState != null) {
            currentState.exitState(this);
        }
        currentState = state;
        currentState.enterState(this);
    }

    @Override
    public Coord getInitialLocation() {
        lastWaypoint = new Coord(100, 100);
        return lastWaypoint;
    }

    protected Coord getRandomInnerCoord(final List<Coord> polygon) {
        Coord c;
        do {
            double x = rng.nextDouble() * getMaxX();
            double y = rng.nextDouble() * getMaxY();
            c = new Coord(x,y);
        } while (!FmiBuilding.isInside(polygon, c));
        lastWaypoint = c;
        return lastWaypoint;
    }
}
