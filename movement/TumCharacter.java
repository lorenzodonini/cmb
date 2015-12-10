package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import tum_model.*;

import java.util.List;

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
    private List<Coord> lastForcedPath;
    private SocialPool.SocialGroup socialGroup;

    //CTOR
    public TumCharacter(final Settings settings) {
        super(settings);
        //Other stuff
        bathroomProbability = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_BATHROOM_PROBABILITY);
        prepTimeBeforeLecture = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_PREP_BEFORE_LECTURE);
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
        socialGroup = null;
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
        double timeSlot = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_TIME_SLOT);
        int timeSlotsPassed = (int) (timePassed / timeSlot);
        return bathroomProbability * timeSlotsPassed;
    }

    public void setLastForcedPath(List<Coord> path) {
        lastForcedPath = path;
    }

    public List<Coord> getLastForcedPath() {
        return lastForcedPath;
    }

    public void setSocialGroup(SocialPool.SocialGroup group) {
        socialGroup = group;
    }

    public SocialPool.SocialGroup getSocialGroup() {
        return socialGroup;
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

    public void exitOldState() {
        if (currentState != null) {
            currentState.exitState(this);
        }
    }

    public void setNewState(IState state) {
        currentState = state;
        currentState.enterState(this);
    }

    @Override
    public Coord getInitialLocation() {
        lastWaypoint = initialLocation.clone();
        return lastWaypoint;
    }

}

