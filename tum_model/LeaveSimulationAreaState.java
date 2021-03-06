package tum_model;

import core.SimScenario;
import movement.Path;
import movement.TumCharacter;

/**
 * Created by rober on 11-Nov-15.
 */
public class LeaveSimulationAreaState implements IState {

    @Override
    public void enterState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,true);
    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {

        Path path = new Path(character.getDefaultSpeed());
        path.addWaypoint(character.getUsedEntry());
        path.addWaypoint(character.getInitialLocation());
        return path;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        return SimScenario.getInstance().getEndTime();
    }

    @Override
    public void exitState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,false);
    }
}
