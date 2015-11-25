package tum_model;

import core.Coord;
import core.SimClock;
import movement.Path;

/**
 * Created by lorenzodonini on 25/11/15.
 */
public class BathroomState implements IState {
    private Coord bathroomCoord;


    public BathroomState() {
        bathroomCoord = new Coord(400,545);
    }

    @Override
    public void enterState(TumCharacter character) {
        //Will need to handle a queue somehow
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
    }
}
