package tum_model;

import core.Coord;
import movement.Path;
import movement.TumCharacter;

import java.util.List;
import java.util.Random;

public class LibraryState implements IState {
    private Coord libraryCoord;
    private double minLibraryStay;
    private double preparationTimeBeforeLecture;
    private Random mRandom;

    public LibraryState() {
        libraryCoord = FmiBuilding.getInstance().makeCoord(11.666903793811798, 48.26253058357581);
        preparationTimeBeforeLecture =
                TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_PREP_BEFORE_LECTURE);
        minLibraryStay = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_LIBRARY_MIN_STAY);
        mRandom = new Random();
    }

    @Override
    public void enterState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,true);
    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {
        final Path p = new Path(character.getDefaultSpeed());

        List<Coord> coords = FmiBuilding.getInstance().getLibraryEntryPath();
        for (Coord c : coords) {
            p.addWaypoint(c);
        }
        p.addWaypoint(libraryCoord);
        return p;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        double maxAvailableTime = character.getTimeUntilNextLecture() - preparationTimeBeforeLecture;
        if (maxAvailableTime <= minLibraryStay) {
            return maxAvailableTime;
        }
        double time;
        do {
            time = mRandom.nextDouble() * maxAvailableTime;
        } while(time + minLibraryStay > maxAvailableTime);
        return time + minLibraryStay;
    }

    @Override
    public void exitState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,false);
        List<Coord> exitPath = FmiBuilding.getInstance().getLibraryExitPath();
        character.setLastForcedPath(exitPath);
    }
}
