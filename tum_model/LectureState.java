package tum_model;

import core.Settings;
import movement.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rober on 22-Nov-15.
 */
public class LectureState implements IState
{
    private List<LectureRoom> lectureRooms;

    public LectureState(final Settings settings) {
        lectureRooms = new ArrayList<>();
    }

    @Override
    public void enterState(TumCharacter character) {

    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {
        // get random lecture from available lectures
        // first get all lecutre rooms from fmi building
        // then select random room and then select random lecture that starts within the next x minutes

        //>>



        // calculate path to lecture (it's ok if its just a direct path for the start)

        //>>



        // add a waiting time at the end of the path
        // the waiting time needs to be long enough so that the node remains in the lecture until it is over
        // take the time until the lecture starts into account

        //>>

        return null;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        return 0;
    }

    @Override
    public void exitState(TumCharacter character) {

    }
}
