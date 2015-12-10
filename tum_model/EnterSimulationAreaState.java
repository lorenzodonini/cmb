package tum_model;

import core.Coord;
import core.SimClock;
import movement.Path;
import movement.TumCharacter;

/**
 * Created by rober on 11-Nov-15.
 */


public class EnterSimulationAreaState implements IState {

    @Override
    public void enterState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,true);
    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {

        Coord lastLocation = character.getLastLocation();
        Coord entryLocation = FmiBuilding.getInstance().getNearestEntry(lastLocation);

        character.setUsedEntry(entryLocation);

        Path path = new Path(character.getDefaultSpeed());
        path.addWaypoint(entryLocation);

        return path;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        return 0;
    }

    @Override
    public void exitState(TumCharacter character) {
        //Setting some post-coditions
        character.setLastBathroomVisitTime(SimClock.getTime());
        character.setEaten(false);
        TumUtilities.printStateAccessDetails(character,false);
    }
}
