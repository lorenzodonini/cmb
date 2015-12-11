package tum_model;

import core.Coord;
import core.Settings;
import core.SimScenario;
import movement.Path;
import movement.TumCharacter;

import java.util.Random;

public class MainHallState implements IState {
    private Coord upperLeftCorner;
    private Coord lowerRightCorner;
    private Random randomGenerator;
    private double minEatTime;
    private double minGroupStudyTime;
    private double preparationTimeBeforeLecture;

    private static final String SETTINGS_UPPER_LEFT = "hallUpperLeft";
    private static final String SETTINGS_SIZE = "hallSize";
    private static final String SCENARIO_NAMESPACE = "Scenario";
    private static final String STATES_NAMESPACE = "States";
    private static final String SETTINGS_EAT_MIN_PERIOD = "minEatTime";
    private static final String SETTINGS_GROUP_STUDY_MIN_PERIOD = "minGroupStudyTime";
    private static final String SETTINGS_PREPARATION_LECTURE_TIME = "preparationTimeBeforeLecture";

    //DEFAULT CTOR
    public MainHallState(final Settings settings) {
        settings.setNameSpace(SCENARIO_NAMESPACE);
        double [] upperLeft = settings.getCsvDoubles(SETTINGS_UPPER_LEFT);
        double [] size = settings.getCsvDoubles(SETTINGS_SIZE);
        settings.restoreNameSpace();
        settings.setNameSpace(STATES_NAMESPACE);
        minEatTime = settings.getDouble(SETTINGS_EAT_MIN_PERIOD);
        minGroupStudyTime = settings.getDouble(SETTINGS_GROUP_STUDY_MIN_PERIOD);
        preparationTimeBeforeLecture = settings.getDouble(SETTINGS_PREPARATION_LECTURE_TIME);
        settings.restoreNameSpace();

        //Defining a size for the main hall
        upperLeftCorner = new Coord(upperLeft[0], upperLeft[1]);
        lowerRightCorner = new Coord(upperLeftCorner.getX() + size[0], upperLeftCorner.getY() + size[1]);
        randomGenerator = new Random();
    }

    @Override
    public void enterState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,true);
        if (character.getCurrentAction() == TumAction.EAT) {
            character.setEaten(true);
        }
    }

    public Path getPathForCharacter(TumCharacter character) {
        final Path p = new Path(character.getDefaultSpeed());
        Coord coord;
        do {
            coord = new Coord(randomGenerator.nextDouble() * lowerRightCorner.getX(),
                    randomGenerator.nextDouble() * lowerRightCorner.getY());
        } while (!FmiBuilding.getInstance().isInMainHall(coord));

        //If we are meeting some people, we may want to go to a specific location
        if (character.getCurrentAction() == TumAction.SOCIAL
                || character.getCurrentAction() == TumAction.GROUP_STUDY) {
            SocialPool.SocialGroup group = SocialPool.getInstance().findFriends(character,coord);
            character.setSocialGroup(group);
            coord = group.getLocation();
        }
        p.addWaypoint(coord);
        return p;
    }

    public double getPauseTimeForCharacter(TumCharacter character) {
        double maxAvailableTime = character.getTimeUntilNextLecture() - preparationTimeBeforeLecture;
        double actionTime;
        if (maxAvailableTime <= 0) {
            return 0;
        }
        switch (character.getCurrentAction()) {
            case EAT:
                actionTime = generateActionTime(minEatTime, maxAvailableTime);
                break;
            case INDIVIDUAL_STUDY:
                actionTime = generateActionTime(0, maxAvailableTime);
                break;
            case GROUP_STUDY:
                actionTime = generateActionTime(minGroupStudyTime, maxAvailableTime);
                break;
            case SOCIAL:
                actionTime = generateActionTime(0, maxAvailableTime);
                break;
            default:
                actionTime = 0;
                break;
        }
        return actionTime;
    }

    private double generateActionTime(double minTime, double maxTime) {
        //Because of movement time, we might have minTime > maxTime
        if (minTime > maxTime) {
            return maxTime; //Returning the actual minimum
        }
        double time;
        do {
            time = randomGenerator.nextDouble() * maxTime;
        } while (time + minTime > maxTime);
        return time;
    }

    @Override
    public void exitState(TumCharacter character) {
        if (character.getCurrentAction() == TumAction.SOCIAL
                || character.getCurrentAction() == TumAction.GROUP_STUDY) {
            SocialPool.getInstance().leaveSocialGroup(character);
        }
        TumUtilities.printStateAccessDetails(character,false);
    }
}

