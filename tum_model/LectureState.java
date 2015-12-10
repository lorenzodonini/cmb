package tum_model;

import core.Coord;
import movement.Path;
import movement.TumCharacter;

import java.util.List;

/**
 * Created by rober on 22-Nov-15.
 */
public class LectureState implements IState
{
    public LectureState() {
    }

    @Override
    public void enterState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,true);
        character.attendNextLecture();
    }

    @Override
    public Path getPathForCharacter(TumCharacter character) {
        Coord lectureLocation = character.getCurrentLecture().getLectureRoom().getPosition();

        final Path p = new Path(character.getDefaultSpeed());
        //Not setting last location. That is handled inside the MovementModel

        List<Coord> entryPath = FmiBuilding.getInstance().getEntryPath(lectureLocation);
        for(Coord point : entryPath) {
            p.addWaypoint(point);
        }
        p.addWaypoint(lectureLocation);
        return p;
    }

    @Override
    public double getPauseTimeForCharacter(TumCharacter character) {
        Lecture lecture = character.getCurrentLecture();
        return lecture.getEndTime() - lecture.getStartTime();
    }

    @Override
    public void exitState(TumCharacter character) {
        TumUtilities.printStateAccessDetails(character,false);
        Coord lectureLocation = character.getCurrentLecture().getLectureRoom().getPosition();
        List<Coord> exitPath = FmiBuilding.getInstance().getExitPath(lectureLocation);
        character.setLastForcedPath(exitPath);
    }
}

