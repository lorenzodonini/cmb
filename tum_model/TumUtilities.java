package tum_model;

import core.Settings;
import core.SimClock;
import core.SimScenario;
import movement.TumCharacter;
import util.Range;

/**
 * Created by lorenzodonini on 09/12/15.
 */
public class TumUtilities {
    private static TumUtilities ourInstance = new TumUtilities();

    private double defaultTimeSlot;

    private final static String SETTINGS_SCENARIO_NAMESPACE = "Scenario";
    private final static String SETTINGS_TIME_SLOT = "timeSlot";

    public static TumUtilities getInstance() {
        return ourInstance;
    }

    public static void printStateAccessDetails(TumCharacter character, boolean entering) {
        if (entering) {
            System.out.println(character.getHost().toString() + " entered " + character.getCurrentAction().name());
        }
        else {
            System.out.println(character.getHost().toString() + " exited " + character.getCurrentState().getClass().getName());
        }
    }

    private TumUtilities() {
        //Initializing
        Settings settings = new Settings();
        settings.setNameSpace(SETTINGS_SCENARIO_NAMESPACE);
        defaultTimeSlot = settings.getDouble(SETTINGS_TIME_SLOT);
    }

    public double getTimeSlot() {
        return defaultTimeSlot;
    }

    public static double getRemainingTimeToday() {
        return SimScenario.getInstance().getEndTime() - SimClock.getTime();
    }
}
