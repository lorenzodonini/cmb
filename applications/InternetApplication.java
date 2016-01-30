package applications;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import core.*;
import routing.ActiveRouter;
import tum_model.WebPage;
import tum_model.WebPageDb;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class InternetApplication extends Application {

    public InternetApplication()
    {
        pendingRequests = new HashMap<>();
        finishedRequests = new HashMap<>();
    }

    static {
        Settings s = new Settings();
        s.setNameSpace("Internet");
        internet = new WebPageDb(s.getInt("pageCount"), s.getInt("minSize"), s.getInt("maxSize"));
    }


    @Override
    public Message handle(Message msg, DTNHost host) {
        return null;
    }

    @Override
    public void update(DTNHost host) {
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
        return new InternetApplication();
    }


    private Map<ActiveRouter, List<Double>> pendingRequests;
    private Map<ActiveRouter, List<WebPage>> finishedRequests;

    static private WebPageDb internet;

}
