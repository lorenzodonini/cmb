package tum_model;

import core.Coord;
import core.Settings;
import input.WKTReader;
import movement.TumCharacter;
import movement.map.MapNode;
import movement.map.SimMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by rober on 11-Nov-15.
 */
public final class FmiBuilding {

    private Coord lowerRight;
    private Coord[] entrances;
    private Coord[] spawnAreas;
    private Coord origin;
    private double stretch;
    private List<Coord> buildingPoints;
    private List<List<Coord>> fingers;
    private List<List<Coord>> fingerEntrys;
    private List<Coord> libraryPoints;
    private List<Coord> hs1Points;

    private TimeSlot[] timeSlots;
    private List<LectureRoom> rooms;

    private double lectureStartTime;
    private double lectureDuration;
    private double lectureEndTime;
    private int mMaxLecturePerDay;
    private static FmiBuilding mInstance;
    private Random mRandom;
    private double timeSlot;

    private static final String BUILDING_NAMESPACE = "Building";
    public static final String SETTINGS_LECTURE_START = "lectureStartTime";
    public static final String SETTINGS_LECTURE_DURATION = "lectureDuration";
    public static final String SETTINGS_LECTURE_END = "lectureEndTime";
    public static final String SETTINGS_MAX_LECTURES = "maxLecturesPerDay";
    public static final String SETTINGS_PROB_ATTENDING_NEXT = "probAttendingNextLecture";
    public static final String SETTINGS_PROB_ATTENDING_HIGH_POP = "probAttendingHighPopLecture";
    public static final String SETTINGS_PROB_ATTENDING_MED_POP = "probAttendingMedPopLecture";
    public static final String SETTINGS_PROB_ONE_HOUR_LECTURE = "probOneHourLecture";
    public static final String SETTINGS_PROB_TWO_HOUR_LECTURE = "probTwoHourLecture";
    public static final String SETTINGS_PROB_THREE_HOUR_LECTURE = "probThreeHourLecture";

    //Probability settings
    private Map<String, Double> mBuildingSettings;

    public static FmiBuilding getInstance() {
        if (mInstance == null) {
            mInstance = new FmiBuilding();
        }
        return mInstance;
    }

    private FmiBuilding() {

        mRandom = new Random();

        origin = new Coord(11.666289567947388, 48.263761179294036);
        lowerRight = new Coord(11.66995882987976, 48.26151847535056);

        final double targetWidth = 1000.0d;
        stretch = targetWidth / (lowerRight.getX() - origin.getX());

        WKTReader reader = new WKTReader();
        File buildingFile = new File("data/fmi.wkt");
        try {
            buildingPoints = reader.readPoints(buildingFile);
            System.out.println("read fmi points");
            for (Coord point : buildingPoints) {
                transformToOrigin(point);
            }

        } catch (IOException e) {
            buildingPoints = new ArrayList<>();
            System.out.println("could not read fmi points");
        }

        readFingers();
        readLibrary();
        readHs1();

        entrances = new Coord[3];
        entrances[0] = new Coord(11.66870892047882, 48.26251843315072);
        entrances[1] = new Coord(11.668986529111862, 48.26231666034203);
        entrances[2] = new Coord(11.666999012231827, 48.262704134854864);
        for(Coord entry : entrances) {
            transformToOrigin(entry);
        }

        spawnAreas = new Coord[3];
        spawnAreas[0] = new Coord(11.66920781135559, 48.26312482093316);
        spawnAreas[1] = new Coord(11.666463911533356, 48.26274208560463);
        spawnAreas[2] = new Coord(11.669347286224365, 48.26226434770976);
        for(Coord spawnArea : spawnAreas) {
            transformToOrigin(spawnArea);
        }

        timeSlot = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_TIME_SLOT);

        mBuildingSettings = new HashMap<>();
    }

    public boolean isInitialized() {
        return timeSlots != null;
    }

    public void initializeFmiBuilding(final Settings settings) {
        settings.setNameSpace(BUILDING_NAMESPACE);

        lectureStartTime = settings.getDouble(SETTINGS_LECTURE_START);
        mBuildingSettings.put(SETTINGS_LECTURE_START, lectureStartTime);
        lectureDuration = settings.getDouble(SETTINGS_LECTURE_DURATION);
        lectureEndTime = settings.getDouble(SETTINGS_LECTURE_END);
        mMaxLecturePerDay = settings.getInt(SETTINGS_MAX_LECTURES);
        //Getting probabilities
        mBuildingSettings.put(SETTINGS_PROB_ATTENDING_NEXT, settings.getDouble(SETTINGS_PROB_ATTENDING_NEXT));
        mBuildingSettings.put(SETTINGS_PROB_ATTENDING_HIGH_POP, settings.getDouble(SETTINGS_PROB_ATTENDING_HIGH_POP));
        mBuildingSettings.put(SETTINGS_PROB_ATTENDING_MED_POP, settings.getDouble(SETTINGS_PROB_ATTENDING_MED_POP));
        mBuildingSettings.put(SETTINGS_PROB_ONE_HOUR_LECTURE, settings.getDouble(SETTINGS_PROB_ONE_HOUR_LECTURE));
        mBuildingSettings.put(SETTINGS_PROB_TWO_HOUR_LECTURE, settings.getDouble(SETTINGS_PROB_TWO_HOUR_LECTURE));
        mBuildingSettings.put(SETTINGS_PROB_THREE_HOUR_LECTURE, settings.getDouble(SETTINGS_PROB_THREE_HOUR_LECTURE));

        settings.restoreNameSpace();

        //We build an array. Each element is a time slot, based on the time unit (e.g. 1h)
        int nSlots = (int) ((lectureEndTime - lectureStartTime) / timeSlot);
        timeSlots = new TimeSlot[nSlots];
        for (int i = 0; i < timeSlots.length; i++) {
            double start = lectureStartTime + timeSlot * i;
            double end = start + timeSlot;
            timeSlots[i] = new TimeSlot(start, end);
        }
        //Afterwards we generate rooms and actual lectures
        generateRooms();
    }

    private void generateRooms() {
        rooms = new ArrayList<>();
        //CREATING SOME STATIC ROOMS

        //HOERSAAL 1
        LectureRoom hoersaal1 = new LectureRoom(makeCoord(11.669152826070786, 48.26245719273224), 500);
        hoersaal1.generateLectures(mRandom, lectureStartTime, lectureDuration, lectureEndTime, timeSlot);
        sortPlannedLectures(hoersaal1.getLectures());
        rooms.add(hoersaal1);


        // read seminar rooms from file
        WKTReader reader = new WKTReader();
        String fileName = "seminar_rooms.wkt";
        File fingerFile = new File("data/" + fileName);

        try {
            List<Coord> seminarRooms = reader.readPoints(fingerFile);
            for(Coord location : seminarRooms) {
                transformToOrigin(location);
                int capacity = mRandom.nextInt(26) + 25;
                LectureRoom seminarRoom = new LectureRoom(location, capacity);
                seminarRoom.generateLectures(mRandom, lectureStartTime, lectureDuration, lectureEndTime, timeSlot);
                sortPlannedLectures(seminarRoom.getLectures());
                rooms.add(seminarRoom);
            }
        } catch (IOException e) {
            System.out.println("failed to read seminar rooms");
        }
    }

    private void sortPlannedLectures(List<Lecture> lectures) {
        for (Lecture l : lectures) {
            int start = (int) ((l.getStartTime() - lectureStartTime) / timeSlot);
            timeSlots[start].addLecture(l);
        }
    }

    public Queue<Lecture> getRandomLectureSchedule(TumCharacter character) {
        double prob;
        double probAttending = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_PROB_ATTENDING_NEXT);
        double probPopHigh = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_PROB_ATTENDING_HIGH_POP);
        double probPopMed = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_PROB_ATTENDING_MED_POP);
        /* A student is scheduled to attend between 1 lecture and N lectures, specified in the settings.
        Since each student chooses to attend a lecture based on a random factor either way, this
        variable only represents a maximum threshold. A student may still attend 0 lectures (unlikely though).
         */
        int numOfLectures = mRandom.nextInt(mMaxLecturePerDay) + 1;
        Lecture lecture;
        Queue<Lecture> lectures = new LinkedList<>();
        int i = 0, j = 0;
        while (i < timeSlots.length && j < numOfLectures) {
            prob = mRandom.nextDouble();
            //We will try to attend a lecture in this time slot
            if (prob <= probAttending) {
                //We could get classes which are already full. Gonna try multiple times, until I get a good one.
                prob = mRandom.nextDouble();
                if (prob <= probPopMed) {
                    //Attending lecture with medium popularity
                    lecture = timeSlots[i].getRandomLectureByPopularity(Lecture.POPULARITY_MEDIUM);
                } else if (prob <= probPopMed + probPopHigh) {
                    //Attending a lecture with high popularity
                    lecture = timeSlots[i].getRandomLectureByPopularity(Lecture.POPULARITY_HIGH);
                } else {
                    //Attending a lecture with low popularity {
                    lecture = timeSlots[i].getRandomLectureByPopularity(Lecture.POPULARITY_LOW);
                }
                if (lecture == null) {
                    //There might not be any lecture available in this time slot
                    i++; //Gonna check next time slot
                    continue;
                }
                if (lecture.getAvailableSpots() > 0) {
                    //Incrementing by the amount of slots taken by this lecture, since I'll be busy in that time.
                    i += lecture.getTimeSlots();
                    lecture.register(character);
                    lectures.add(lecture);
                    j++;
                }
            }
            else {
                i++; //Gonna check next time slot again
            }
        }
        return lectures;
    }

    public Coord getNearestEntry(Coord currentPos)
    {
        Coord nearestEntry = entrances[0];
        double minDistance = currentPos.distance(nearestEntry);
        for(int i = 1; i < entrances.length; ++i) {
            double distance = entrances[i].distance(currentPos);
            if(distance < minDistance) {
                minDistance = distance;
                nearestEntry = entrances[i];
            }
        }

        return nearestEntry;
    }

    public Coord getRandomSpawnPoint()
    {
        int index = mRandom.nextInt(3);
        return spawnAreas[index];
    }

    public Coord makeCoord(double x, double y)
    {
        Coord location = new Coord(x, y);
        transformToOrigin(location);
        return location;
    }

    public boolean isInside(Coord pos) {
        return isInside(buildingPoints, pos);
    }

    public Coord[] getEntrances() {
        return entrances;
    }

    public List<LectureRoom> getRooms() {
        return rooms;
    }

    private void transformToOrigin(Coord point) {
        point.setLocation(
                stretch * (point.getX() - origin.getX()),
                stretch * (origin.getY() - point.getY())
        );
    }


    public boolean isInside(final List<Coord> polygon, final Coord point) {
        final int count = countIntersectedEdges(polygon, point, new Coord(-10, 0));
        return ((count % 2) != 0);
    }


    public boolean isInFinger(int finger, Coord pos) {
        return isInside(fingers.get(finger), pos);
    }

    public boolean isInLibrary(Coord pos) {
        return isInside(libraryPoints, pos);
    }

    public boolean isInHs1(Coord pos) {
        return isInside(hs1Points, pos);
    }

    public boolean isInMainHall(Coord pos)
    {
        if(!isInside(pos)) {
            return false;
        }

        for(int i = 1; i <= 12; ++i) {
            if(isInFinger(i, pos)) {
                return false;
            }
        }

        if(isInLibrary(pos)) {
            return false;
        }

        if(isInHs1(pos)) {
            return false;
        }

        return true;
    }

    public List<Coord> getEntryPath(Coord pos)
    {
        for(int i = 0; i <= 12; ++i) {
            if(isInFinger(i, pos)) {
                return fingerEntrys.get(i);
            }
        }
        return new ArrayList<>();
    }

    public List<Coord> getExitPath(Coord pos)
    {
        List<Coord> entryPath = getEntryPath(pos);
        List<Coord> exitPath = new ArrayList<>();
        for(int i = entryPath.size() - 1; i >= 0; --i) {
            exitPath.add(entryPath.get(i));
        }

        return exitPath;
    }

    public SimMap getMap()
    {
        Map<Coord, MapNode> nodeDict = new HashMap<>();
        MapNode connectedNode = new MapNode(buildingPoints.get(buildingPoints.size() - 1));
        for(Coord point : buildingPoints) {
            MapNode node = new MapNode(point);
            node.addNeighbor(connectedNode);
            nodeDict.put(node.getLocation(), node);
            connectedNode = node;
        }

        return new SimMap(nodeDict);
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

    private class TimeSlot {
        private Map<Short, List<Lecture>> plannedLectures;
        public double startTime;
        public double endTime;

        public TimeSlot(double start, double end) {
            startTime = start;
            endTime = end;
            plannedLectures = new HashMap<>(3);
            plannedLectures.put(Lecture.POPULARITY_LOW, new ArrayList<Lecture>());
            plannedLectures.put(Lecture.POPULARITY_MEDIUM, new ArrayList<Lecture>());
            plannedLectures.put(Lecture.POPULARITY_HIGH, new ArrayList<Lecture>());
        }

        public void addLecture(Lecture lecture) {
            plannedLectures.get(lecture.getPopularity()).add(lecture);
        }

        public Lecture getRandomLectureByPopularity(short popularity) {
            List<Lecture> lectures = plannedLectures.get(popularity);
            if (lectures.size() == 0) {
                return null; //Might be empty for this time slot
            }
            int index = mRandom.nextInt(lectures.size());
            return lectures.get(index);
        }

        public List<Lecture> getLecturesByPopularity(short popularity) {
            return plannedLectures.get(popularity);
        }

        public Map<Short, List<Lecture>> getAllLectures() {
            return plannedLectures;
        }
    }


    private void readFingers() {

        fingers = new ArrayList<>();
        fingerEntrys = new ArrayList<>();

        WKTReader reader = new WKTReader();

        for(int i = 0; i <= 12; ++i) {
            // read finger boundary
            String fileName = "finger" + Integer.toString(i) + ".wkt";
            File fingerFile = new File("data/" + fileName);
            try {
                List<Coord> finger = reader.readPoints(fingerFile);
                for(Coord point : finger) {
                    transformToOrigin(point);
                }
                fingers.add(finger);
            } catch (IOException e) {
                List<Coord> empty = new ArrayList<>();
                fingers.add(empty);
            }

            // read finger entry path
            fileName = "finger" + Integer.toString(i) + "_entry.wkt";
            File entryFile = new File("data/" + fileName);
            try {
                List<Coord> entry = reader.readPoints(entryFile);
                for(Coord point : entry) {
                    transformToOrigin(point);
                }
                fingerEntrys.add(entry);
            } catch (IOException e) {
                List<Coord> empty = new ArrayList<>();
                fingerEntrys.add(empty);
            }
        }
    }

    private void readLibrary() {
        WKTReader reader = new WKTReader();
        String fileName = "library.wkt";
        File file = new File("data/" + fileName);
        try {
            libraryPoints = reader.readPoints(file);
            for(Coord point : libraryPoints) {
                transformToOrigin(point);
            }
        } catch (IOException e) {
            libraryPoints = new ArrayList<>();
        }
    }

    private void readHs1() {
        WKTReader reader = new WKTReader();
        String fileName = "hs1.wkt";
        File file = new File("data/" + fileName);
        try {
            hs1Points = reader.readPoints(file);
            for(Coord point : hs1Points) {
                transformToOrigin(point);
            }
        } catch (IOException e) {
            hs1Points = new ArrayList<>();
        }
    }
}
