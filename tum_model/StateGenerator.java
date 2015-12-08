package tum_model;

import core.Settings;
import movement.TumCharacter;

import java.util.*;

/**
 * Created by lorenzodonini on 11/11/15.
 */
public class StateGenerator {
    private static StateGenerator ourInstance = new StateGenerator();

    private HashMap<TumAction, IState> availableStates;
    private List<TumAction> keyList;
    private Random randomGenerator;

    public static StateGenerator getInstance() {
        return ourInstance;
    }

    private StateGenerator() {
        availableStates = new HashMap<>();
        keyList = new ArrayList<>();
        randomGenerator = new Random();
    }

    public void initializeStates(final Settings settings) {
        IState commonState = new MainHallState(settings);
        keyList.add(TumAction.EAT);
        availableStates.put(TumAction.EAT, commonState);
        keyList.add(TumAction.GROUP_STUDY);
        availableStates.put(TumAction.GROUP_STUDY, commonState);
        keyList.add(TumAction.INDIVIDUAL_STUDY);
        availableStates.put(TumAction.INDIVIDUAL_STUDY, commonState);
        keyList.add(TumAction.SOCIAL);
        availableStates.put(TumAction.SOCIAL, commonState);

        IState bathroomState = new BathroomState();
        keyList.add(TumAction.RESTROOM);
        availableStates.put(TumAction.RESTROOM, bathroomState);

        IState lectureState = new LectureState();
        availableStates.put(TumAction.LECTURE, lectureState);

        IState enterState = new EnterSimulationAreaState();
        availableStates.put(TumAction.ENTER, enterState);
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

    public void setNextAction(TumCharacter character) {
        if(character.getCurrentAction() == null) {
            character.setCurrentAction(TumAction.ENTER);
            return;
        }

        TumAction action;
        double timeUntilNextLecture = character.getTimeUntilNextLecture();
        if (timeUntilNextLecture > 0 && timeUntilNextLecture < 5 * 60) {
            action = TumAction.LECTURE;
        } else {
            action = generateRandomAction();
        }
        //NEED TO IMPLEMENT MORE CONDITIONS
        character.setCurrentAction(action);
    }

    public void setNextState(TumCharacter character) {
        TumAction action = character.getCurrentAction();
        if (action != null) {
            IState nextState = getStateForAction(action);
            if (nextState != null) {
                character.setNewState(nextState);
            }
        }
    }
}

