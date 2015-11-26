package tum_model;

import core.Settings;
import core.SimClock;
import movement.MovementModel;
import movement.Path;

/**
 * Created by lorenzodonini on 11/11/15.
 */
public class TumStudentMovement extends TumCharacter {

    public TumStudentMovement(final Settings settings) {
        super(settings);
        if (!StateGenerator.getInstance().bIsInitialized()) {
            StateGenerator.getInstance().initializeStates(settings);
        }
    }

    public TumStudentMovement(final TumCharacter other) {
        super(other);
    }

    @Override
    public double nextPathAvailable() {
        if (getCurrentState() != null) {
            return SimClock.getTime() + getCurrentState().getPauseTimeForCharacter(this);
        }
        //TODO: ONCE WE HAVE A WORKING ENTERING STATE WE NEED TO EDIT THIS
        return super.nextPathAvailable();
    }

    @Override
    public Path getPath() {
        //first need to get a new state
        StateGenerator.getInstance().setNextAction(this);
        StateGenerator.getInstance().setNextState(this);
        if (getCurrentState() != null) {
            return getCurrentState().getPathForCharacter(this);
        }
        return null;
    }

    @Override
    public MovementModel replicate() {
        return new TumStudentMovement(this);
    }
}
