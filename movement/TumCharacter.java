package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import tum_model.*;

/**
 * Created by lorenzodonini on 10/11/15.
 */
public abstract class TumCharacter extends MovementModel {

    protected Coord lastWaypoint;
    protected boolean bHasEaten;
    protected double bathroomProbability;
    protected double lastBathroomVisitTime;
    protected double prepTimeBeforeLecture;
    protected double enterTime;
    private IState currentState;
    private TumAction currentAction;
    private Coord usedEntry;
    private Coord initialLocation;

    private static final String STUDENTS_NAMESPACE = "Group1";
    private static final String SETTINGS_BATHROOM_PROBABILITY = "bathroomTsProbability";
    private static final String STATES_SCENARIO = "States";
    private static final String SETTINGS_PREP_TIME_BEFORE_LECTURE = "preparationTimeBeforeLecture";

    //CTOR
    public TumCharacter(final Settings settings) {
        super(settings);
        //Other stuff

        settings.setNameSpace(STUDENTS_NAMESPACE);
        bathroomProbability = settings.getDouble(SETTINGS_BATHROOM_PROBABILITY);
        settings.restoreNameSpace();
        settings.setNameSpace(STATES_SCENARIO);
        prepTimeBeforeLecture = settings.getDouble(SETTINGS_PREP_TIME_BEFORE_LECTURE);
        settings.restoreNameSpace();
    }

    public TumCharacter(final TumCharacter other) {
        super(other);
        //Copy state and other stuff

        // this is as ugly as it gets
        // we have to reinitialize our stuff in a copy constructor, since the owner of this class just instantiates once
        // and then copies the class around without calling an init function
        // so that stuff has to go here...

        // It's called prototype pattern :P
        bathroomProbability = other.bathroomProbability;
        prepTimeBeforeLecture = other.prepTimeBeforeLecture;
        initialLocation = FmiBuilding.getInstance().getRandomSpawnPoint();
        lastBathroomVisitTime = 0;
        lastWaypoint = initialLocation;
        currentAction = null;
        currentState = null;
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
        return 2.0;
    }

    public abstract boolean hasOtherScheduledLectures();

    public abstract Lecture getNextScheduledLecture();

    public abstract Lecture getCurrentLecture();

    public abstract double getTimeUntilNextLecture();

    public abstract void attendNextLecture();

    public double getEnterTime() {
        return enterTime;
    }

    public boolean hasEaten() {
        return bHasEaten;
    }

    public void setEaten(boolean bEaten) {
        bHasEaten = bEaten;
    }

    public double getBathroomProbability() {
        if (lastBathroomVisitTime == 0) {
            return 0;
        }
        double timePassed = SimClock.getTime() - lastBathroomVisitTime;
        int timeSlotsPassed = (int) (timePassed / TumUtilities.getInstance().getTimeSlot());
        return bathroomProbability * timeSlotsPassed;
    }

    public void setLastBathroomVisitTime(double visitTime) {
        lastBathroomVisitTime = visitTime;
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

