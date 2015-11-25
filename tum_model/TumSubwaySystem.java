package tum_model;

import java.util.Queue;

/**
 * Created by lorenzodonini on 11/11/15.
 */
public class TumSubwaySystem {
    private static TumSubwaySystem ourInstance = new TumSubwaySystem();

    private Queue<TumCharacter> charactersOnTrain;

    public static TumSubwaySystem getInstance() {
        return ourInstance;
    }

    private TumSubwaySystem() {
    }
}
