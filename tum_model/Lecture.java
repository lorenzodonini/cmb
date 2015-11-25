package tum_model;

import java.util.List;

/**
 * Created by rober on 23-Nov-15.
 *
 * This class is just a data struct for lectures
 */
public class Lecture {
    public LectureRoom room;
    public List<TumCharacter> participants;
    public int start;
    public int end;
}
