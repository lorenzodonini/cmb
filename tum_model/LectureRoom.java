package tum_model;

import core.Coord;

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

        // remember set lectures room to this


    }

}
