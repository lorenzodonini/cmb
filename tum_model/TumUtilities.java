package tum_model;

import core.SimClock;
import core.SimScenario;
import movement.TumCharacter;

public class TumUtilities {
    private static TumUtilities ourInstance = new TumUtilities();

    private static boolean bStateChangesReport;

    public static TumUtilities getInstance() {
        return ourInstance;
    }

    public static void printStateAccessDetails(TumCharacter character, boolean entering) {
        if (!bStateChangesReport) {
            return;
        }
        if (entering) {
            System.out.println(character.getHost().toString() + " entered " + character.getCurrentAction().name());
        }
        else {
            System.out.println(character.getHost().toString() + " exited " + character.getCurrentAction().name());
        }
    }

    private TumUtilities() {
        //Initializing
        bStateChangesReport = true;
    }

    public static double getRemainingTimeToday() {
        return SimScenario.getInstance().getEndTime() - SimClock.getTime();
    }
}
