package tum_model;

import core.Settings;
import core.SimClock;
import movement.TumCharacter;
import movement.TumStudentMovement;
import util.Range;

import java.util.*;

public class StateGenerator {
    private static StateGenerator ourInstance = new StateGenerator();

    private HashMap<TumAction, IState> availableStates;
    private List<TumAction> keyList;
    private Random randomGenerator;

    private double preparationTimeBeforeLecture;
    private double probStayNoLecture;
    private double eatingProbability;
    private Range eatingPeriod;
    private double minEatPeriod;
    private double minGroupStudyTime;
    private double minIndividualStudyTime;
    private double minLibraryTime;
    private double bathroomTime;

    public static StateGenerator getInstance() {
        return ourInstance;
    }

    private StateGenerator() {
        availableStates = new HashMap<>();
        keyList = new ArrayList<>();
        randomGenerator = new Random();
    }

    public void initializeStates(final Settings settings) {
        TumModelSettings params = TumModelSettings.getInstance();
        preparationTimeBeforeLecture = params.getDouble(TumModelSettings.TUM_PREP_BEFORE_LECTURE);
        probStayNoLecture = params.getDouble(TumModelSettings.TUM_PROB_STAY_NO_LECTURE);
        eatingProbability = params.getDouble(TumModelSettings.TUM_EATING_PROBABILITY);
        eatingPeriod = params.getRange(TumModelSettings.TUM_EATING_PERIOD);
        minEatPeriod = params.getDouble(TumModelSettings.TUM_EATING_MIN_TIME);
        minGroupStudyTime = params.getDouble(TumModelSettings.TUM_GROUP_STUDY_MIN_TIME);
        minIndividualStudyTime = params.getDouble(TumModelSettings.TUM_INDIVIDUAL_STUDY_MIN_TIME);
        minLibraryTime = params.getDouble(TumModelSettings.TUM_LIBRARY_MIN_STAY);
        bathroomTime = params.getDouble(TumModelSettings.TUM_BATHROOM_TIME);

        //Main hall activities
        IState commonState = new MainHallState(settings);
        availableStates.put(TumAction.EAT, commonState);
        keyList.add(TumAction.GROUP_STUDY);
        availableStates.put(TumAction.GROUP_STUDY, commonState);
        keyList.add(TumAction.INDIVIDUAL_STUDY);
        availableStates.put(TumAction.INDIVIDUAL_STUDY, commonState);
        keyList.add(TumAction.SOCIAL);
        availableStates.put(TumAction.SOCIAL, commonState);

        //Library
        IState libraryState = new LibraryState();
        keyList.add(TumAction.LIBRARY);
        availableStates.put(TumAction.LIBRARY, libraryState);

        //Bathroom
        IState bathroomState = new BathroomState();
        availableStates.put(TumAction.RESTROOM, bathroomState);

        //Lecture
        IState lectureState = new LectureState();
        availableStates.put(TumAction.LECTURE, lectureState);

        //Enter/exit
        IState travelState = new TravelState();
        availableStates.put(TumAction.TRAVEL, travelState);
        IState enterState = new EnterSimulationAreaState();
        availableStates.put(TumAction.ENTER, enterState);
        IState exitState = new LeaveSimulationAreaState();
        availableStates.put(TumAction.EXIT, exitState);
    }

    public boolean isInitialized() {
        return !availableStates.isEmpty();
    }

    public IState getStateForAction(TumAction action) {
        return availableStates.get(action);
    }

    public TumAction generateRandomAction() {
        return keyList.get(randomGenerator.nextInt(keyList.size()));
    }

    public void changeToNextState(TumCharacter character) {
        character.exitOldState();
        character.setCurrentAction(getNextAction(character));
        character.setNewState(getNextState(character));
    }

    public TumAction getNextAction(TumCharacter character) {
        if(character.getCurrentAction() == null) {
            return TumAction.TRAVEL;
        }
        else if(character.getCurrentAction() == TumAction.TRAVEL) {
            character.setCurrentAction(TumAction.ENTER);
            return TumAction.ENTER;
        }

        TumAction action;
        double timeUntilNextLecture = character.getTimeUntilNextLecture();
        if (character.hasOtherScheduledLectures()) {
            //We still have planned lectures today.
            // Beware, the variable could be negative and we could be slightly late
            if (timeUntilNextLecture <= preparationTimeBeforeLecture) {
                action = TumAction.LECTURE;
            }
            else {
                action = generateActionBasedOnNeeds(character);
            }
        }
        else {
            if (randomGenerator.nextDouble() <= probStayNoLecture) {
                //We don't have any more lectures planned, but are still staying for a while
                action = generateActionBasedOnNeeds(character);
            }
            else {
                action = TumAction.EXIT;
            }
        }
        return action;
    }

    private TumAction generateActionBasedOnNeeds(TumCharacter character) {
        //If we still have a lecture, we get the time we can spend before having to attend that lecture.
        //If not, we get the remaining time until the end of the simulation
        double maxAvailableTime = (character.hasOtherScheduledLectures()) ?
                character.getTimeUntilNextLecture() - preparationTimeBeforeLecture :
                TumUtilities.getRemainingTimeToday();
        //Bathroom has highest priority
        double prob = randomGenerator.nextDouble();
        double bathroomProb = character.getBathroomProbability();
        if (prob <= bathroomProb && maxAvailableTime >= bathroomTime) {
            return TumAction.RESTROOM;
        }
        //Eating has lower priority
        if (!character.hasEaten() && eatingPeriod.isInRange(SimClock.getTime())
                && maxAvailableTime >= minEatPeriod) {
            prob = randomGenerator.nextDouble();
            if (prob <= eatingProbability) {
                return TumAction.EAT;
            }
        }
        //Random generation, since the character does not have any particular needs
        TumAction action;
        double timeNeeded;
        do {
            action = generateRandomAction();
            if (action == TumAction.GROUP_STUDY) {
                timeNeeded = minGroupStudyTime;
            }
            else if (action == TumAction.INDIVIDUAL_STUDY) {
                timeNeeded = minIndividualStudyTime;
            }
            else if (action == TumAction.LIBRARY) {
                timeNeeded = minLibraryTime;
            }
            else {
                timeNeeded = 0;
            }
        } while (maxAvailableTime < timeNeeded);
        return action;
    }

    public IState getNextState(TumCharacter character) {
        TumAction action = character.getCurrentAction();
        if (action != null) {
            IState nextState = getStateForAction(action);
            if (nextState != null) {
                return nextState;
            }
        }
        return null;
    }
}

