package movement;

import core.Coord;
import core.Settings;
import tum_model.FmiBuilding;

import java.util.List;

/**
 * Created by lorenzodonini on 10/11/15.
 */
public abstract class TumCharacter extends MovementModel {

    protected Coord lastWaypoint;


    //CTOR
    public TumCharacter(final Settings settings) {
        super(settings);
        //Other stuff
    }

    public TumCharacter(final TumCharacter other) {
        super(other);
        //Copy state and other stuff
    }

    @Override
    public Coord getInitialLocation() {
        lastWaypoint = new Coord(100, 100);
        return lastWaypoint;
    }

    protected Coord getRandomInnerCoord(final List<Coord> polygon) {
        Coord c;
        do {
            double x = rng.nextDouble() * getMaxX();
            double y = rng.nextDouble() * getMaxY();
            c = new Coord(x,y);
        } while (!FmiBuilding.isInside(polygon, c));
        lastWaypoint = c;
        return lastWaypoint;
    }
}
