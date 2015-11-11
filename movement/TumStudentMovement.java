package movement;

import core.Coord;
import core.Settings;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lorenzodonini on 11/11/15.
 */
public class TumStudentMovement extends TumCharacter {

    final List<Coord> mainHall = Arrays.asList(
            new Coord( 100, 100 ),
            new Coord( 900, 100 ),
            new Coord( 900, 550 ),
            new Coord( 100, 550 ),
            new Coord( 100, 100 )
    );

    public TumStudentMovement(Settings settings) {
        super(settings);
    }

    public TumStudentMovement(TumCharacter other) {
        super(other);
    }

    @Override
    public Path getPath() {
        // Creates a new path from the previous waypoint to a new one.
        final Path p;
        p = new Path( super.generateSpeed() );
        p.addWaypoint( this.lastWaypoint.clone() );
        lastWaypoint = getRandomInnerCoord(mainHall);
        p.addWaypoint(lastWaypoint);

        return p;
    }

    @Override
    public MovementModel replicate() {
        return new TumStudentMovement(this);
    }
}
