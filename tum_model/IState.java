package tum_model;

import movement.Path;

/**
 * Created by rober on 11-Nov-15.
 */
public interface IState {
    public void enterState(TumCharacter character);
    public Path getPathForCharacter(TumCharacter character);
    public double getPauseTimeForCharacter(TumCharacter character);
    public void exitState(TumCharacter character);
}
