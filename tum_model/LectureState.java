package tum_model;

import movement.Path;
import movement.TumCharacter;

/**
 * Created by rober on 22-Nov-15.
 */
public class LectureState implements IState
{
    @Override
    public Path getPath(TumCharacter node) {

        // get random lecture from available lectures
        // first get all lecutre rooms from fmi building
        // then select random room and then select random lecture that starts within the next x minutes




        // calculate path to lecture (it's ok if its just a direct path for the start)



        // add a waiting time at the end of the path
        // the waiting time needs to be long enough so that the node remains in the lecture until it is over
        // take the time until the lecture starts into account


        return null; // return path
    }
}
