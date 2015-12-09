package movement;

import core.Settings;
import core.SimClock;
import tum_model.FmiBuilding;
import tum_model.Lecture;
import tum_model.StateGenerator;
import tum_model.TumUtilities;

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
        double start = FmiBuilding.getInstance().getBuildingSettingByName(FmiBuilding.SETTINGS_LECTURE_START);
        if (hasOtherScheduledLectures()) {
            Lecture firstLecture = getNextScheduledLecture();
            double random = rng.nextDouble() * TumUtilities.getInstance().getTimeSlot();
            return firstLecture.getStartTime() - (prepTimeBeforeLecture + random);
        }
        else {
            double random = rng.nextDouble() * TumUtilities.getInstance().getTimeSlot();
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
        //TODO: ONCE WE HAVE A WORKING ENTERING STATE WE NEED TO EDIT THIS
        return super.nextPathAvailable();
    }

    @Override
    public Path getPath() {
        //first need to get a new state
        StateGenerator.getInstance().setNextAction(this);
        StateGenerator.getInstance().setNextState(this);
        if (getCurrentState() != null) {
            return getCurrentState().getPathForCharacter(this);
        }
        return null;
    }

    @Override
    public MovementModel replicate() {
        return new TumStudentMovement(this);
    }
}

