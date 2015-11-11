package tum_model;

import movement.Path;
import movement.TumCharacter;

/**
 * Created by rober on 11-Nov-15.
 */
public interface IState {

    Path getPath(TumCharacter node);

}
