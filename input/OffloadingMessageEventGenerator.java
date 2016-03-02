package input;

import core.Settings;
import core.SettingsError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by lorenzodonini on 28/02/16.
 */
public class OffloadingMessageEventGenerator extends MessageEventGenerator {
    private List<Integer> fromIds;
    private List<Integer> randomSources;
    private Random random;
    private double nextEvent;

    /**
     * Constructor, initializes the interval between events,
     * and the size of messages generated, as well as number
     * of hosts in the network.
     *
     * @param s Settings for this generator.
     */
    public OffloadingMessageEventGenerator(Settings s) {
        super(s);

        fromIds = new ArrayList<>();

        if (toHostRange == null) {
            throw new SettingsError("Destination host (" + TO_HOST_RANGE_S +
                    ") must be defined");
        }
        for (int i = hostRange[0]; i < hostRange[1]; i++) {
            fromIds.add(i);
        }
        random = new Random();
    }

    /**
     * Returns the next message creation event
     * @see input.EventQueue#nextEvent()
     */
    public ExternalEvent nextEvent() {
        int responseSize = 0; /* no responses requested */
        int from;
        int to;

        if (randomSources == null || randomSources.size() == 0) {
            //Whenever the source list is empty, we need to refill it with the same input and shuffle it
            randomSources = new ArrayList<>(fromIds);
            Collections.shuffle(randomSources, random);
            //Whenever the source list is empty, we want a new "random" time interval,
            // in which all source nodes will receive an event
            int waitTime = drawNextEventTimeDiff();
            if (fromIds.size() == 0) {
                nextEvent = waitTime;
            }
            else {
                nextEvent = waitTime / fromIds.size();
            }
        }

        //Always remove last element, otherwise we would have to shift the whole array every time
        from = randomSources.remove(randomSources.size() - 1);
        to = drawToAddress(toHostRange, from);

        nextEventsTime += nextEvent;

        return new MessageCreateEvent(from, to, getID(),
                drawMessageSize(), responseSize, nextEventsTime);
    }
}
