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
    private static final double timeSlot = 3600;
    private Coord position;
    private int capacity;
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
        double singleHourProbability = 0.1;
        double doubleHourProbability = 0.9;
        double longLectureDuration = lectureDuration * 2;

        double time = lectureStart;
        while (time < lectureEnd) {
            double result = random.nextDouble();
            if (result <= singleHourProbability) {
                double startOffset = random.nextInt(15);
                double endOffset = (startOffset < 10) ? random.nextInt(5) : 0;
                Lecture newLecture = new Lecture(time + startOffset, time + lectureDuration + endOffset, 1,this);
                lectures.add(newLecture);
                time += timeSlot;
            }
            else if (result <= doubleHourProbability && (lectureEnd - time) > longLectureDuration ) {
                double startOffset = random.nextInt(15);
                double endOffset = random.nextInt(5);
                Lecture newLecture = new Lecture(time + startOffset, time + longLectureDuration + endOffset, 2,this);
                lectures.add(newLecture);
                time += timeSlot * 2;
            }
            else {
                time += timeSlot;
            }
        }
        //We will be moving forward from second
    }

    public void printLectures() {
        for (Lecture l : lectures) {
            System.out.println("Lecture: " + l.getStartTime() + " - " + l.getEndTime());
        }
    }
}
