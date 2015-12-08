package tum_model;

import core.Coord;
import movement.Path;
import movement.TumCharacter;

/**
 * Created by rober on 11-Nov-15.
 */


public class EnterSimulationAreaState implements IState {

    @Override
    public void enterState(TumCharacter character) {

    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {

        Coord lastLocation = character.getLastLocation();
        Coord entryLocation = FmiBuilding.getInstance().getNearestEntry(lastLocation);


        Path path = new Path();
        path.addWaypoint(lastLocation);
        path.addWaypoint(entryLocation);

        return path;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        return 0;
    }

    @Override
    public void exitState(TumCharacter character) {

    }
}
