package tum_model;

import core.Coord;
//import jdk.nashorn.internal.parser.Lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by robert on 22-Nov-15.
 */
public class LectureRoom {
    private Coord position;
    private int capacity;
    private double probabilityOneHour;
    private double probabilityTwoHours;
    private double probabilityThreeHours;
    private List<Lecture> lectures;

    public LectureRoom(Coord pos, int cap)
    {
        position = pos;
        capacity = cap;
        lectures = new ArrayList<>();
    }

    public Coord getPosition()
    {
        return position;
    }

    public int getCapacity()
    {
        return capacity;
    }


    public List<Lecture> getLectures()
    {
        return lectures;
    }

    // generates a random list of lectures for this room
    public void generateLectures(Random random, double lectureStart, double lectureDuration, double lectureEnd, double timeSlot)
    {
        double probOneHour =
                FmiBuilding.getInstance().getBuildingSettingByName(FmiBuilding.SETTINGS_PROB_ONE_HOUR_LECTURE);
        double probTwoHour =
                FmiBuilding.getInstance().getBuildingSettingByName(FmiBuilding.SETTINGS_PROB_TWO_HOUR_LECTURE);
        double probThreeHour =
                FmiBuilding.getInstance().getBuildingSettingByName(FmiBuilding.SETTINGS_PROB_THREE_HOUR_LECTURE);

        double time = lectureStart;
        while (time < lectureEnd) {
            double result = random.nextDouble();
            if (result <= probOneHour && (lectureEnd - time) > lectureDuration) {
                double startOffset = random.nextInt(15);
                double endOffset = 15 - startOffset;
                Lecture newLecture = new Lecture(time + startOffset, time + lectureDuration + endOffset, 1,this);
                lectures.add(newLecture);
                time += timeSlot;
            }
            else if (result <= probOneHour+probTwoHour && (lectureEnd - time) > lectureDuration*2 ) {
                double startOffset = random.nextInt(15);
                double endOffset = 30 - startOffset + random.nextInt(5);
                Lecture newLecture = new Lecture(time + startOffset, time + lectureDuration*2 + endOffset, 2,this);
                lectures.add(newLecture);
                time += timeSlot * 2;
            }
            else if (result <= probOneHour+probTwoHour+probThreeHour && (lectureEnd - time) > lectureDuration*3) {
                double startOffset = random.nextInt(15);
                double endOffset = 45 - startOffset + random.nextInt(10);
                Lecture newLecture = new Lecture(time + startOffset, time + lectureDuration*3 + endOffset, 3,this);
                lectures.add(newLecture);
                time += timeSlot * 3;
            }
            else {
                time += timeSlot;
            }
        }
    }

    public void printLectures() {
        for (Lecture l : lectures) {
            System.out.println("Lecture: " + l.getStartTime() + " - " + l.getEndTime());
        }
    }
}
