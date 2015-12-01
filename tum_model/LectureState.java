package tum_model;

import core.Coord;
import movement.Path;
import movement.TumCharacter;

/**
 * Created by rober on 22-Nov-15.
 */
public class LectureState implements IState
{
    public LectureState() {
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
        Coord coord = character.getNextScheduledLecture().getLectureRoom().getPosition();
        final Path p = new Path(character.getDefaultSpeed());
        p.addWaypoint(character.getLastLocation());
        p.addWaypoint(coord);
        return p;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        Lecture lecture = character.getNextScheduledLecture();
        return lecture.getEndTime() - lecture.getStartTime();
    }

    @Override
    public void exitState(TumCharacter character) {
        character.attendNextLecture();
    }
}

