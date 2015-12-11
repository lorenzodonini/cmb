package tum_model;

import core.Coord;
import core.SimClock;
import movement.Path;
import movement.TumCharacter;

public class TravelState implements IState {
    private double subwayPeriod;

    public TravelState() {
        subwayPeriod = TumModelSettings.getInstance().getDouble(TumModelSettings.TUM_UBAHN_PERIOD);
    }

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
        Coord initialLocation = character.getInitialLocation();
        Coord ubahnEntrance = FmiBuilding.getInstance().getSpawnAreas()[0]; //Ubahn entrance
        if (ubahnEntrance.equals(initialLocation)) {
            return computeUbahnArrivalTime(character.getEnterTime()) - SimClock.getTime();
        }
        return character.getEnterTime() - SimClock.getTime();
    }

    private double computeUbahnArrivalTime(double rawTime) {
        double diff = rawTime % subwayPeriod;
        //Approximating
        if (diff < (subwayPeriod / 2)) {
            return rawTime - diff;
        }
        return rawTime + diff;
    }

    @Override
    public void exitState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,false);
    }
}
