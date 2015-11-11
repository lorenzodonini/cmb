package movement;

import core.Coord;
import core.Settings;

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
        } while (!isInside(polygon, c));
        lastWaypoint = c;
        return lastWaypoint;
    }

    private static boolean isInside(final List <Coord> polygon, final Coord point ) {
        final int count = countIntersectedEdges( polygon, point, new Coord( -10,0 ) );
        return ( ( count % 2 ) != 0 );
    }

    private static int countIntersectedEdges(
            final List <Coord> polygon,
            final Coord start,
            final Coord end ) {
        int count = 0;
        for ( int i = 0; i < polygon.size() - 1; i++ ) {
            final Coord polyP1 = polygon.get( i );
            final Coord polyP2 = polygon.get( i + 1 );

            final Coord intersection = intersection( start, end, polyP1, polyP2 );
            if ( intersection == null ) continue;

            if ( isOnSegment( polyP1, polyP2, intersection )
                    && isOnSegment( start, end, intersection ) ) {
                count++;
            }
        }
        return count;
    }

    private static boolean isOnSegment(
            final Coord L0,
            final Coord L1,
            final Coord point ) {
        final double crossProduct
                = ( point.getY() - L0.getY() ) * ( L1.getX() - L0.getX() )
                - ( point.getX() - L0.getX() ) * ( L1.getY() - L0.getY() );
        if ( Math.abs( crossProduct ) > 0.0000001 ) return false;

        final double dotProduct
                = ( point.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
                + ( point.getY() - L0.getY() ) * ( L1.getY() - L0.getY() );
        if ( dotProduct < 0 ) return false;

        final double squaredLength
                = ( L1.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
                + (L1.getY() - L0.getY() ) * (L1.getY() - L0.getY() );
        if ( dotProduct > squaredLength ) return false;

        return true;
    }

    private static Coord intersection(
            final Coord L0_p0,
            final Coord L0_p1,
            final Coord L1_p0,
            final Coord L1_p1 ) {
        final double[] p0 = getParams( L0_p0, L0_p1 );
        final double[] p1 = getParams( L1_p0, L1_p1 );
        final double D = p0[ 1 ] * p1[ 0 ] - p0[ 0 ] * p1[ 1 ];
        if ( D == 0.0 ) return null;

        final double x = ( p0[ 2 ] * p1[ 1 ] - p0[ 1 ] * p1[ 2 ] ) / D;
        final double y = ( p0[ 2 ] * p1[ 0 ] - p0[ 0 ] * p1[ 2 ] ) / D;

        return new Coord( x, y );
    }

    private static double[] getParams(
            final Coord c0,
            final Coord c1 ) {
        final double A = c0.getY() - c1.getY();
        final double B = c0.getX() - c1.getX();
        final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
        return new double[] { A, B, C };
    }
}
