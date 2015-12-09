package tum_model;

import core.Coord;
import core.Settings;
import core.SimClock;
import movement.Path;
import movement.TumCharacter;

/**
 * Created by lorenzodonini on 25/11/15.
 */
public class BathroomState implements IState {
    private Coord bathroomCoord;

    public BathroomState() {
        bathroomCoord = FmiBuilding.getInstance().makeCoord(11.668698191642761, 48.26249450894131);
    }

    @Override
    public void enterState(TumCharacter character) {
        //Will need to handle a queue somehow
        System.out.println(character.getHost().toString() + " entered " + character.getCurrentAction().name());
        character.setLastBathroomVisitTime(SimClock.getTime());
    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {
        final Path p = new Path(character.getDefaultSpeed());

        p.addWaypoint(character.getLastLocation());
        p.addWaypoint(bathroomCoord);
        return p;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        System.out.println(character.getHost().toString() + " entered bathroom at "+ SimClock.getTime());

        return 3 * 60;
    }

    @Override
    public void exitState(TumCharacter character) {
        //Will need to handle a queue somehow
        System.out.println(character.getHost().toString() + " exited " + character.getCurrentAction().name());
    }
}
