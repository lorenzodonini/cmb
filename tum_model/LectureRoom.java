package tum_model;

import core.Coord;
import jdk.nashorn.internal.parser.Lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rober on 22-Nov-15.
 */
public class LectureRoom {

    private Coord position;
    private int capacity;
    List<Lecture> lectures;

    public LectureRoom(Coord position, int capacity)
    {
        this.position = position;
        this.capacity = capacity;
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
        if(lectures == null) {
            generateLectures();
        }

        return lectures;
    }

    // generates a random list of lectures for this room
    private void generateLectures()
    {
        lectures = new ArrayList<>();

        // loop over some breaking condition
        for(int i = 0; i < 10; ++i) { // CHANGE this condition

            // create a new lecture
            Lecture newLecture = new Lecture();
            newLecture.room = this;

            // assign a time slot to the lecture here
            // the timeslots of all the lectures of this room MUST NOT overlap
            // maybe change the loop condition to something related to the time

            //>>


            // add new lecture to internal lecture storage
            lectures.add(newLecture);
        }
    }

}
