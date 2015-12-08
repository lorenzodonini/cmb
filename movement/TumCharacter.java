package movement;

import core.Coord;
import core.Settings;
import tum_model.FmiBuilding;
import tum_model.IState;
import tum_model.Lecture;
import tum_model.TumAction;

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
    private Coord usedEntry;
    private Coord initialLocation;

    //CTOR
    public TumCharacter(final Settings settings) {
        super(settings);
        //Other stuff

        initialLocation = FmiBuilding.getInstance().getRandomSpawnPoint();
        lastWaypoint = initialLocation;
    }

    public TumCharacter(final TumCharacter other) {
        super(other);
        //Copy state and other stuff


        // this is as ugly as it gets
        // we have to reinitialize our stuff in a copy constructor, since the owner of this class just instantiates once
        // and then copies the class around without calling an init function
        // so that stuff has to go here...

        initialLocation = FmiBuilding.getInstance().getRandomSpawnPoint();
        lastWaypoint = initialLocation;
    }

    //PUBLIC ACCESSORS
    public Coord getLastLocation() {
        return lastWaypoint.clone();
    }

    public Coord getUsedEntry() {
        return usedEntry;
    }

    public void setUsedEntry(Coord usedEntry) {
        this.usedEntry = usedEntry;
    }

    public double getDefaultSpeed() {
        return 10.0;
    }

    public abstract boolean hasOtherScheduledLectures();

    public abstract Lecture getNextScheduledLecture();

    public abstract double getTimeUntilNextLecture();

    public abstract void attendNextLecture();

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
        lastWaypoint = initialLocation.clone();
        return lastWaypoint;
    }
}

