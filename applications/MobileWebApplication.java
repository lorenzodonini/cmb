package applications;

import core.*;
import tum_model.WebPageDb;

import java.util.Random;

/**
 * Created by lorenzodonini on 01/03/16.
 */
public class MobileWebApplication extends Application {
    private final static String NS_TIME_INTERVAL = "requestInterval";
    private final static String NS_REQ_SIZE = "requestSize";

    //Intervals in seconds
    private int minRequestInterval;
    private int maxRequestInterval;
    private int requestSize;
    private Random mRandom;
    private int nextRequestTime;

    private static int mId = 1;

    /** Application ID */
    public static final String APP_ID = "tum.cmb.team4.MobileWebApplication";

    /** Message Prefix */
    private final static String MESSAGE_PREFIX = "M";

    public MobileWebApplication(Settings s) {
        int interval [] = s.getCsvInts(NS_TIME_INTERVAL);
        minRequestInterval = interval[0];
        maxRequestInterval = interval[1];
        requestSize = s.getInt(NS_REQ_SIZE);

        mRandom = new Random();

        super.setAppID(APP_ID);
    }

    public MobileWebApplication(MobileWebApplication other) {
        minRequestInterval = other.minRequestInterval;
        maxRequestInterval = other.maxRequestInterval;
        requestSize = other.requestSize;
        mRandom = other.mRandom;

        //Already setting the first wait time
        nextRequestTime = mRandom.nextInt(maxRequestInterval - minRequestInterval) + minRequestInterval;
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        //Mobile nodes don't have passive behaviour, i.e. we don't care when we receive a response
        // from the internet, we just need to actively send requests. See the update() method.

        //TODO: IMPLEMENT P2P LOGIC?!
        //Mobile nodes have passive behaviour only when doing P2P offloading. In that case we need to check
        return null;
    }

    @Override
    public void update(DTNHost host) {
        int current = SimClock.getIntTime();

        // Should start a new request
        if (current >= nextRequestTime) {
            DTNHost to = InfrastructureManager.getInstance().getInternetNode();
            String id = MESSAGE_PREFIX + mId;
            mId++;

            Message m = new Message(host, to, id, requestSize);
            //Adding the requested message ID property
            int randPage = WebPageDb.getInstance().getRandomPageId();
            m.addProperty(WebPageDb.WEB_REQUESTED_ID_PROPERTY, randPage);
            m.setAppID(InternetApplication.APP_ID);

            host.createNewMessage(m);

            nextRequestTime = current +
                    mRandom.nextInt(maxRequestInterval - minRequestInterval) + minRequestInterval;
        }
    }

    @Override
    public Application replicate() {
        return new MobileWebApplication(this);
    }
}
