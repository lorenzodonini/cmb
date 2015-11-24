package tum_model;

import movement.TumCharacter;

import java.util.List;

/**
 * Created by rober on 23-Nov-15.
 *
 * This class is just a data struct for lectures
 */
public class Lecture {
    public LectureRoom room;
    public List<TumCharacter> participants;
    public int start;   // Not shure what the time format in the ONE is, going with int timestamps for now. Change as you see fit.
    public int end;
}
