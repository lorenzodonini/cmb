package tum_model;

import movement.Path;
import movement.TumCharacter;

public interface IState {
    void enterState(TumCharacter character);
    Path getPathForCharacter(TumCharacter character);
    double getPauseTimeForCharacter(TumCharacter character);
    void exitState(TumCharacter character);
}
