package tum_model;

import core.Coord;
import movement.TumCharacter;

import java.util.*;

public class SocialPool {
    private static SocialPool ourInstance = new SocialPool();

    private List<SocialGroup> highFillRatio; //Has lowest priority
    private List<SocialGroup> lowFillRatio; //Has highest priority
    private Random mRandom;

    public static SocialPool getInstance() {
        return ourInstance;
    }

    private SocialPool() {
        mRandom = new Random();
        highFillRatio = new LinkedList<>();
        lowFillRatio = new LinkedList<>();
    }

    //Doesn't check for duplicates!!
    public SocialGroup findFriends(TumCharacter character, Coord destination) {
        SocialGroup target;
        //High priority queue
        if (lowFillRatio.size() > 0) {
            target = tryJoinSocialGroup(lowFillRatio.iterator(),character);
            if (target != null) {
                return target;
            }
        }
        //Low priority queue
        if (highFillRatio.size() > 0) {
            target = tryJoinSocialGroup(highFillRatio.iterator(),character);
            if (target != null) {
                return target;
            }
        }

        //No groups exist or all are full
        target = new SocialGroup(character,destination);
        lowFillRatio.add(target);
        return target;
    }

    private SocialGroup tryJoinSocialGroup(Iterator<SocialGroup> iterator, TumCharacter character) {
        SocialGroup target = null;
        while (iterator.hasNext()) {
            target = iterator.next();
            if (target.getFillRatio() < 1) {
                break;
            }
            else {
                target = null;
            }
        }
        if (target != null) {
            double oldRatio = target.getFillRatio();
            target.addCharacter(character);
            updatePriority(target,oldRatio);
            return target;
        }
        return null;
    }

    private void updatePriority(SocialGroup g, double oldRatio) {
        double newRatio = g.getFillRatio();
        if (newRatio == 0) {
            //Deleting social group
            if (oldRatio <= 0.5) {
                lowFillRatio.remove(g);
            }
            else {
                highFillRatio.remove(g);
            }
        }
        if ((newRatio <= 0.5 && oldRatio <= 0.5) || (newRatio > 0.5 && oldRatio > 0.5)) {
            return;
        }
        if (oldRatio <= 0.5) {
            lowFillRatio.remove(g);
            highFillRatio.add(g);
        }
        else {
            highFillRatio.remove(g);
            lowFillRatio.add(g);
        }
    }

    public void leaveSocialGroup(TumCharacter character) {
        SocialGroup group = character.getSocialGroup();
        double oldRatio = group.getFillRatio();
        if(group.removeCharacter(character)) {
            updatePriority(group, oldRatio);
        }
    }

    public class SocialGroup implements Comparable {
        private Set<TumCharacter> people;
        private Coord location;
        private int maxCapacity;

        public SocialGroup(TumCharacter starter, Coord coord) {
            maxCapacity = mRandom.nextInt(TumModelSettings.getInstance().getInt(
                    TumModelSettings.TUM_MAX_SOCIAL_MEETING_PEOPLE)) + 1;
            people = new HashSet<>(maxCapacity);
            people.add(starter);
            location = coord;
        }

        public void addCharacter(TumCharacter character) {
            people.add(character);
        }

        public boolean removeCharacter(TumCharacter character) {
            return people.remove(character);
        }

        public Coord getLocation() {
            return location;
        }

        private double getFillRatio() {
            return people.size() / maxCapacity;
        }

        @Override
        public int compareTo(Object o) {
            SocialGroup other = (SocialGroup)o;
            if (getFillRatio() < other.getFillRatio()) {
                return 1;
            }
            else if (getFillRatio() > other.getFillRatio()) {
                return -1;
            }
            return 0;
        }
    }
}
