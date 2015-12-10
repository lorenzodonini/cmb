package tum_model;

import core.Settings;
import core.SimClock;
import movement.TumCharacter;
import util.Range;

import java.util.*;

/**
 * Created by lorenzodonini on 11/11/15.
 */
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
    private double bathroomTime;

    private static final String STATES_NAMESPACE = "States";
    private static final String SETTINGS_PREP_TIME_BEFORE_LECTURE = "preparationTimeBeforeLecture";
    private static final String SETTINGS_PROB_STAY_NO_LECTURE = "probStayNoLecture";
    private static final String SETTINGS_EATING_PERIOD = "eatingPeriod";
    private static final String SETTINGS_EATING_PROBABILITY = "eatingProbability";
    private static final String SETTINGS_EAT_MIN_TIME = "minEatTime";
    private static final String SETTINGS_BATHROOM_TIME = "bathroomStayTime";
    private static final String SETTINGS_GROUP_STUDY_MIN_TIME = "minGroupStudyTime";
    private static final String SETTINGS_INDIVIDUAL_STUDY_MIN_TIME = "minIndividualStudyTime";

    public static StateGenerator getInstance() {
        return ourInstance;
    }

    private StateGenerator() {
        availableStates = new HashMap<>();
        keyList = new ArrayList<>();
        randomGenerator = new Random();
    }

    public void initializeStates(final Settings settings) {
        settings.setNameSpace(STATES_NAMESPACE);
        preparationTimeBeforeLecture = settings.getDouble(SETTINGS_PREP_TIME_BEFORE_LECTURE);
        probStayNoLecture = settings.getDouble(SETTINGS_PROB_STAY_NO_LECTURE);
        eatingProbability = settings.getDouble(SETTINGS_EATING_PROBABILITY);
        eatingPeriod = settings.getCsvRanges(SETTINGS_EATING_PERIOD)[0];
        minEatPeriod = settings.getDouble(SETTINGS_EAT_MIN_TIME);
        minGroupStudyTime = settings.getDouble(SETTINGS_GROUP_STUDY_MIN_TIME);
        minIndividualStudyTime = settings.getDouble(SETTINGS_INDIVIDUAL_STUDY_MIN_TIME);
        bathroomTime = settings.getDouble(SETTINGS_BATHROOM_TIME);
        settings.restoreNameSpace();

        //Main hall activities
        IState commonState = new MainHallState(settings);
        availableStates.put(TumAction.EAT, commonState);
        keyList.add(TumAction.GROUP_STUDY);
        availableStates.put(TumAction.GROUP_STUDY, commonState);
        keyList.add(TumAction.INDIVIDUAL_STUDY);
        availableStates.put(TumAction.INDIVIDUAL_STUDY, commonState);
        keyList.add(TumAction.SOCIAL);
        availableStates.put(TumAction.SOCIAL, commonState);

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
        if (timeUntilNextLecture > 0) {
            //We still have planned lectures today
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

