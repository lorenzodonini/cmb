package tum_model;

import core.Coord;
import input.WKTReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rober on 11-Nov-15.
 */
public final class FmiBuilding {

    private static final Coord lowerRight;
    private static final Coord[] entrances;
    private static final Coord origin;
    private static final double stretch;
    private static List<Coord> buildingPoints;
    private static List<LectureRoom> rooms;

    static {

        entrances = new Coord[1];
        entrances[0] = new Coord(100, 0);

        origin = new Coord(11.666289567947388, 48.263761179294036);
        lowerRight = new Coord(11.66995882987976,48.26151847535056);

        final double targetWidth = 100.0d;
        stretch = targetWidth / (lowerRight.getX() - origin.getX());

        WKTReader reader = new WKTReader();
        File buildingFile = new File("/data/fmi.wkt");
        try {
            buildingPoints = reader.readPoints(buildingFile);
            System.out.println("read fmi points");
            for(Coord point : buildingPoints) {
                transformToOrigin(point);
            }

        } catch (IOException e) {
            buildingPoints = new ArrayList<>();
            System.out.println("could not read fmi points");
        }


    }

    public static boolean isInside(Coord pos)
    {
        return isInside(buildingPoints, pos);
    }

    public static Coord[] getEntrances()
    {
        return entrances;
    }

    public static List<LectureRoom> getRooms()
    {
        return rooms;
    }

    // private constructor because class is just a collection of static methods
    private FmiBuilding() { }


    private static void transformToOrigin(Coord point)
    {
        point.setLocation(
                stretch * (point.getX() - origin.getX()),
                stretch * (point.getY() - origin.getY()));
    }


    public static boolean isInside(final List<Coord> polygon, final Coord point ) {
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

        return (dotProduct <= squaredLength);
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


    private static void initRooms()
    {
        // generate or initialize lecture rooms


        //>>

    }
}
