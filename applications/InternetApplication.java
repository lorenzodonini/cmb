package applications;

import core.*;
import routing.ActiveRouter;
import tum_model.WebPage;
import tum_model.WebPageDb;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class InternetApplication extends Application {

    //Settings
    /** Setting name for the total amount of web pages that need to be offered to the users */
    private final static String S_PAGE_COUNT = "pageCount";
    /** Setting name for the minimum size of a web page. This represents the size in bytes of a response. */
    private final static String S_MIN_SIZE = "minSize";
    /** Setting name for the maximum size of a web page. This represents the size in bytes of a response. */
    private final static String S_MAX_SIZE = "maxSize";

    /** Application ID */
    public static final String APP_ID = "tum.cmb.team4.InternetApplication";

    private Map<ActiveRouter, List<Double>> pendingRequests;
    private Map<ActiveRouter, List<WebPage>> finishedRequests;

    /** Instance of a {@link WebPageDb}. This is actually singleton and could be accessed statically
     * for every call, but a direct reference is stored here as an optimization.
     * This is because the internet application has to respond to all mobile nodes and this database,
     * containing all the available web pages, needs to be accessed by the application on each request. */
    private WebPageDb internet;

    public InternetApplication(Settings s) {
        pendingRequests = new HashMap<>();
        finishedRequests = new HashMap<>();

        WebPageDb.initWebPageDb(s.getInt(S_PAGE_COUNT), s.getInt(S_MIN_SIZE), s.getInt(S_MAX_SIZE));
        internet = WebPageDb.getInstance();
        super.setAppID(APP_ID);
    }

    public InternetApplication(InternetApplication other) {
        super(other);
        pendingRequests = new HashMap<>();
        finishedRequests = new HashMap<>();
        internet = other.internet;
    }


    @Override
    public Message handle(Message msg, DTNHost host) {
        //The sender should have requested a specific web page
        Object request = msg.getProperty(WebPageDb.WEB_REQUESTED_ID_PROPERTY);
        if (request == null) {
            return msg; //Not a valid app request
        }
        int reqId = (Integer)request;

        //We are indeed the receiver
        if (msg.getTo() == host) {
            //The internet replies with the desired web request
            WebPage page = WebPageDb.getInstance().getPageById(reqId);
            String id = ActiveRouter.RESPONSE_PREFIX + msg.getId();

            Message m = new Message(host, msg.getFrom(), id, page.size);
            m.addProperty(WebPageDb.WEB_PAGE_PROPERTY, page);
            //By setting the original request, we already have the full path, needed for routing purposes
            m.setRequest(msg);
            m.setAppID(MobileWebApplication.APP_ID);

            //Response message gets created, this will be routed inside the update function (of the router)
            host.createNewMessage(m);
        }

        return msg;
    }

    @Override
    public void update(DTNHost host) {
        //"The internet" is passive and doesn't create messages unless explicitly requested to.
        // See the handle() method.
    }

    @Override
    public Application replicate() {
        return new InternetApplication(this);
    }
}
