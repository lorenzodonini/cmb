package tum_model;

import core.Settings;
import util.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class TumModelSettings {
    private static TumModelSettings mInstance;

    public static TumModelSettings getInstance() {
        if (mInstance == null) {
            mInstance = new TumModelSettings();
        }
        return mInstance;
    }

    //Students
    private static final String STUDENTS_NS = "Group1";
    public static final String TUM_BATHROOM_PROBABILITY = "bathroomTsProbability";
    //Building
    private static final String BUILDING_NS = "Building";
    public static final String TUM_LECTURE_START = "lectureStartTime";
    public static final String TUM_LECTURE_END = "lectureEndTime";
    public static final String TUM_MAX_DAILY_LECTURES = "maxLecturesPerDay";
    public static final String TUM_PROB_ATTENDING_NEXT = "probAttendingNextLecture";
    public static final String TUM_HIGH_POPULATION = "highPopulation";
    public static final String TUM_PROB_ATTENDING_HIGH_POP = "probAttendingHighPopLecture";
    public static final String TUM_MED_POPULATION = "mediumPopulation";
    public static final String TUM_PROB_ATTENDING_MED_POP = "probAttendingMedPopLecture";
    public static final String TUM_PROB_ONE_HOUR_LECTURE = "probOneHourLecture";
    public static final String TUM_PROB_TWO_HOUR_LECTURE = "probTwoHourLecture";
    public static final String TUM_PROB_THREE_HOUR_LECTURE = "probThreeHourLecture";
    public static final String TUM_LECTURE_DURATION = "lectureDuration";
    //Scenario
    private static final String SCENARIO_NS = "Scenario";
    public static final String TUM_TIME_SLOT = "timeSlot";
    //States
    private static final String STATES_NS = "States";
    public static final String TUM_BATHROOM_TIME = "bathroomStayTime";
    public static final String TUM_PREP_BEFORE_LECTURE = "preparationTimeBeforeLecture";
    public static final String TUM_PROB_STAY_NO_LECTURE = "probStayNoLecture";
    public static final String TUM_EATING_PERIOD = "eatingPeriod";
    public static final String TUM_EATING_PROBABILITY = "eatingProbability";
    public static final String TUM_EATING_MIN_TIME = "minEatTime";
    public static final String TUM_GROUP_STUDY_MIN_TIME = "minGroupStudyTime";
    public static final String TUM_GROUP_MIN_PEOPLE = "minGroupPeople";
    public static final String TUM_GROUP_MAX_PEOPLE = "maxGroupPeople";
    public static final String TUM_INDIVIDUAL_STUDY_MIN_TIME = "minIndividualStudyTime";
    public static final String TUM_MAX_SOCIAL_MEETING_PEOPLE = "peoplePerSocialGroupMax";

    private Map<String, Double> mDoubleProperties;
    private Map<String, Range> mRangeProperties;
    private Map<String, Integer> mIntegerProperties;

    private TumModelSettings() {
        mDoubleProperties = new HashMap<>();
        mRangeProperties = new HashMap<>();
        mIntegerProperties = new HashMap<>();
        loadSettings();
    }

    private void loadSettings() {
        Settings settings = new Settings();
        settings.setNameSpace(STUDENTS_NS);
        loadDoubleSetting(settings,TUM_BATHROOM_PROBABILITY);

        settings.setNameSpace(BUILDING_NS);;
        loadDoubleSetting(settings,TUM_LECTURE_START);
        loadDoubleSetting(settings,TUM_LECTURE_END);
        loadDoubleSetting(settings,TUM_MAX_DAILY_LECTURES);
        loadDoubleSetting(settings,TUM_PROB_ATTENDING_NEXT);
        loadIntegerSetting(settings,TUM_HIGH_POPULATION);
        loadDoubleSetting(settings,TUM_PROB_ATTENDING_HIGH_POP);
        loadIntegerSetting(settings,TUM_MED_POPULATION);
        loadDoubleSetting(settings,TUM_PROB_ATTENDING_MED_POP);
        loadDoubleSetting(settings,TUM_PROB_ONE_HOUR_LECTURE);
        loadDoubleSetting(settings,TUM_PROB_TWO_HOUR_LECTURE);
        loadDoubleSetting(settings,TUM_PROB_THREE_HOUR_LECTURE);
        loadDoubleSetting(settings,TUM_LECTURE_DURATION);

        settings.setNameSpace(SCENARIO_NS);
        loadDoubleSetting(settings,TUM_TIME_SLOT);

        settings.setNameSpace(STATES_NS);
        loadDoubleSetting(settings,TUM_BATHROOM_TIME);
        loadDoubleSetting(settings,TUM_PREP_BEFORE_LECTURE);
        loadDoubleSetting(settings,TUM_PROB_STAY_NO_LECTURE);
        loadRangeSetting(settings,TUM_EATING_PERIOD);
        loadDoubleSetting(settings,TUM_EATING_PROBABILITY);
        loadDoubleSetting(settings,TUM_EATING_MIN_TIME);
        loadDoubleSetting(settings,TUM_GROUP_STUDY_MIN_TIME);
        loadIntegerSetting(settings,TUM_GROUP_MIN_PEOPLE);
        loadIntegerSetting(settings,TUM_GROUP_MAX_PEOPLE);
        loadDoubleSetting(settings,TUM_INDIVIDUAL_STUDY_MIN_TIME);
        loadIntegerSetting(settings,TUM_MAX_SOCIAL_MEETING_PEOPLE);
    }

    private void loadDoubleSetting(Settings settings, String name) {
        mDoubleProperties.put(name, settings.getDouble(name));
    }

    private void loadRangeSetting(Settings settings, String name) {
        Range [] ranges = settings.getCsvRanges(name);
        if (ranges.length > 0) {
            mRangeProperties.put(name, ranges[0]);
        }
    }

    private void loadIntegerSetting(Settings settings, String name) {
        mIntegerProperties.put(name, settings.getInt(name));
    }

    public Double getDouble(String name) {
        return mDoubleProperties.get(name);
    }

    public Range getRange(String name) {
        return mRangeProperties.get(name);
    }

    public Integer getInt(String name) {
        return mIntegerProperties.get(name);
    }

}
