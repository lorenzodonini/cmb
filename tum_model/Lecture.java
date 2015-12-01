package tum_model;

import movement.TumCharacter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rober on 23-Nov-15.
 *
 * This class is just a data struct for lectures
 */
public class Lecture {
    private LectureRoom lectureRoom;
    private List<TumCharacter> participants;
    private short mPopularity;
    private double startTime;
    private double endTime;
    private int timeSlots;
    public static final short POPULARITY_LOW = 0;
    public static final short POPULARITY_MEDIUM = 1;
    public static final short POPULARITY_HIGH = 2;

    public Lecture(double start, double end, int slotsTotal, LectureRoom room) {
        startTime = start;
        endTime = end;
        timeSlots = slotsTotal;
        lectureRoom = room;
        //TODO: parametrize this somehow. Settings ftw
        if (room.getCapacity() > 200) {
            mPopularity = POPULARITY_HIGH;
        }
        else if (room.getCapacity() > 50) {
            mPopularity = POPULARITY_MEDIUM;
        }
        else {
            mPopularity = POPULARITY_LOW;
        }
        participants = new ArrayList<>();
    }

    public LectureRoom getLectureRoom() {
        return lectureRoom;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public short getPopularity() {
        return mPopularity;
    }

    public int getTimeSlots() {
        return timeSlots;
    }

    public int getAvailableSpots() {
        return lectureRoom.getCapacity() - participants.size();
    }

    public boolean register(TumCharacter character) {
        if ((lectureRoom.getCapacity() - participants.size()) > 0) {
            participants.add(character);
            return true;
        }
        return false;
    }
}
