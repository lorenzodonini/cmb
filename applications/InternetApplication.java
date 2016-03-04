package applications;

import core.*;
import routing.ActiveRouter;
import tum_model.WebPage;
import tum_model.WebPageDb;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class InternetApplication extends Application {
    private final static String NS_PAGE_COUNT = "pageCount";
    private final static String NS_MIN_SIZE = "minSize";
    private final static String NS_MAX_SIZE = "maxSize";

    /** Application ID */
    public static final String APP_ID = "tum.cmb.team4.InternetApplication";

    private Map<ActiveRouter, List<Double>> pendingRequests;
    private Map<ActiveRouter, List<WebPage>> finishedRequests;

    private WebPageDb internet;

    public InternetApplication(Settings s) {
        pendingRequests = new HashMap<>();
        finishedRequests = new HashMap<>();

        WebPageDb.initWebPageDb(s.getInt(NS_PAGE_COUNT), s.getInt(NS_MIN_SIZE), s.getInt(NS_MAX_SIZE));
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

    private void processRequests()
    {
        double time = SimClock.getTime();

        Iterator<Map.Entry<ActiveRouter, List<Double>>> requestsIt = pendingRequests.entrySet().iterator();

        while(requestsIt.hasNext()) {
            // iterate through all pending requests of each router

            Map.Entry<ActiveRouter, List<Double>> requestsEntry = requestsIt.next();
            ActiveRouter currentRouter = requestsEntry.getKey();

            Iterator<Double> requestIt = requestsEntry.getValue().iterator();
            while(requestIt.hasNext()) {
                // iterate through each request of the current router (of outer loop)

                if(time >= requestIt.next()) {
                    // request is finished, remove from pending
                    requestIt.remove();

                    // add response to finished
                    List<WebPage> responses = finishedRequests.get(currentRouter);
                    if(responses == null) {
                        responses = new LinkedList<>();
                        finishedRequests.put(currentRouter, responses);
                    }
                    responses.add(internet.getRandomPage());
                }
            }

            if(requestsEntry.getValue().isEmpty()) {
                // all requests for this router have finished, remove router from request map
                requestIt.remove();
            }
        }
    }

    private double getRandomRequestTime()
    {
        return (double)ThreadLocalRandom.current().nextInt(20, 100) / 1000d;
    }


    // add a request with this function
    public void requestWebpage(ActiveRouter router)
    {
        List<Double> routerRequests = pendingRequests.get(router);

        if(routerRequests == null) {
            routerRequests = new LinkedList<>();
            pendingRequests.put(router, routerRequests);
        }

        // add request together with the time when it's going to be finished
        routerRequests.add(SimClock.getTime() + getRandomRequestTime());
    }

    // get finished responses, might return null if none are finished
    public List<WebPage> getResponses(ActiveRouter router)
    {
        processRequests();

        List<WebPage> responses = finishedRequests.get(router);
        finishedRequests.remove(router);
        return responses;
    }

    @Override
    public Application replicate() {
        return new InternetApplication(this);
    }
}
