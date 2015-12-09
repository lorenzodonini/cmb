package tum_model;

import movement.Path;
import movement.TumCharacter;

/**
 * Created by lorenzodonini on 09/12/15.
 */
public class TravelState implements IState {
    @Override
    public void enterState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,true);
    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {
        return null;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        return character.getEnterTime();
    }

    @Override
    public void exitState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,false);
    }
}
