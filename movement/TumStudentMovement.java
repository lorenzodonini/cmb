package movement;

import core.Coord;
import core.Message;
import core.Settings;
import core.SimClock;
import tum_model.*;

import java.util.List;
import java.util.Queue;

/**
 * Created by lorenzodonini on 11/11/15.
 */
public class TumStudentMovement extends TumCharacter {
    private Queue<Lecture> registeredLectures;
    private Lecture currentLecture;

    public TumStudentMovement(final Settings settings) {
        super(settings);
        if (!StateGenerator.getInstance().isInitialized()) {
            StateGenerator.getInstance().initializeStates(settings);
        }
        if (!FmiBuilding.getInstance().isInitialized()) {
            FmiBuilding.getInstance().initializeFmiBuilding(settings);
        }
    }

    public TumStudentMovement(final TumCharacter other) {
        super(other);
        registeredLectures = FmiBuilding.getInstance().getRandomLectureSchedule(this);
        enterTime = generateEnterTime();
        lastBathroomVisitTime = enterTime;
    }

    private double generateEnterTime() {
        double start = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_LECTURE_START);
        double timeSlot = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_TIME_SLOT);
        if (hasOtherScheduledLectures()) {
            Lecture firstLecture = getNextScheduledLecture();
            double random = rng.nextDouble() * (timeSlot/2);
            return firstLecture.getStartTime() - (prepTimeBeforeLecture + random);
        }
        else {
            double random = rng.nextDouble() * (timeSlot/2);
            return start + random;
        }
    }

    @Override
    public boolean hasOtherScheduledLectures() {
        return !registeredLectures.isEmpty();
    }

    @Override
    public Lecture getNextScheduledLecture() {
        if (registeredLectures.isEmpty()) {
            return null;
        }
        return registeredLectures.peek();
    }

    @Override
    public double getTimeUntilNextLecture() {
        if (registeredLectures.isEmpty()) {
            return -1;
        }
        return registeredLectures.peek().getStartTime() - SimClock.getTime();
    }

    @Override
    public Lecture getCurrentLecture() {
        return currentLecture;
    }

    @Override
    public void attendNextLecture() {
        currentLecture = registeredLectures.poll();
    }

    @Override
    public double nextPathAvailable() {
        if (getCurrentState() != null) {
            return SimClock.getTime() + getCurrentState().getPauseTimeForCharacter(this);
        }
        return super.nextPathAvailable();
    }

    @Override
    public Path getPath() {
        //first need to get a new state
        exitOldState();
        TumAction nextAction = StateGenerator.getInstance().getNextAction(this);
        setCurrentAction(nextAction);
        IState nextState = StateGenerator.getInstance().getNextState(this);
        setNewState(nextState);
        //StateGenerator.getInstance().changeToNextState(this); //Simple alternative

        //I might need to follow a fixed path on my way out of a location
        List<Coord> oldPath = getLastForcedPath();
        final Path p = new Path();
        p.setSpeed(getDefaultSpeed());
        p.addWaypoint(getLastLocation());
        if (oldPath != null) {
            for (Coord c : oldPath) {
                p.addWaypoint(c);
            }
        }
        setLastForcedPath(null);

        //Now I need to get the actual set of coordinates to follow in order to reach the new location
        if (getCurrentState() != null) {
            Path newPath = getCurrentState().getPathForCharacter(this);
            if (newPath == null) {
                return p;
            }

            for (Coord c : newPath.getCoords()) {
                p.addWaypoint(c);
            }
            return p;
        }
        else {
            return null;
        }
    }

    @Override
    public MovementModel replicate() {
        return new TumStudentMovement(this);
    }
}

