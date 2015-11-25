package tum_model;

import core.Settings;

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
    }

    public boolean bIsInitialized() {
        return !availableStates.isEmpty();
    }

    public IState getStateForAction(TumAction action) {
        return availableStates.get(action);
    }

    public TumAction generateRandomAction() {
        return keyList.get(randomGenerator.nextInt(keyList.size()));
    }

    public void setNextAction(TumCharacter character) {
        //TO IMPLEMENT
        TumAction action = generateRandomAction();
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
