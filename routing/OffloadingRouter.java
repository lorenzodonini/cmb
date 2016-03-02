package routing;

import applications.InfrastructureManager;
import core.*;
import interfaces.CellularInterface;
import interfaces.WLANInterface;
import tum_model.WebPage;
import tum_model.WebPageDb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lorenzodonini on 11/02/16.
 */
public class OffloadingRouter extends ActiveRouter {
    private LinkedList<WebPage> cachedWebPages;
    private int cacheSize;
    private RouteMode mMode;
    private int offloadWaitTime;
    private static final String NS_MAX_CACHE = "cacheSize";
    private static final String NS_ROUTE_MODE = "routeMode";
    private static final String NS_OFFLOAD_WAIT_TIME = "offloadWaitTime";
    private static final String NS_ROUTING = "Routing"; //Namespace

    public OffloadingRouter(Settings s) {
        super(s);
        s.setNameSpace(NS_ROUTING);
        cacheSize = s.getInt(NS_MAX_CACHE);
        mMode = RouteMode.NORMAL; //Default
        int mode = s.getInt(NS_ROUTE_MODE);
        for (RouteMode r : RouteMode.values()) {
            if (r.ordinal() == mode) {
                mMode = r;
                break;
            }
        }
        offloadWaitTime = s.getInt(NS_OFFLOAD_WAIT_TIME);
        s.restoreNameSpace();
    }

    public OffloadingRouter(OffloadingRouter wr) {
        super(wr);
        cacheSize = wr.cacheSize;
        mMode = wr.mMode;
        offloadWaitTime = wr.offloadWaitTime;
        cachedWebPages = new LinkedList<>();
    }

    @Override
    public void update() {
        super.update();

        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        // Try first the messages that can be delivered to final recipient
        Connection delivered = exchangeDeliverableMessages();
        if (delivered != null) {
            Object lastWP = delivered.getMessage().getProperty(WebPageDb.WEB_PAGE_PROPERTY);
            if (lastWP != null) {
                //Caching
                if (cachedWebPages.size() >= cacheSize) {
                    cachedWebPages.removeFirst();
                }
                cachedWebPages.addLast((WebPage)lastWP);
            }
            /* We still return, since a transfer was started. This may be because the router delivered
            a message to its final recipient, or because the router asked the connected hosts for further messages.*/
            System.out.println(getHost().toString()+ " - exchangeDeliverableMessages complete");
            return;
        }

        List<Message> messages = new ArrayList<>(getMessageCollection());
        sortByQueueMode(messages);
        for (Message m : messages) {
            //OffloadingMessageEventGenerator already takes case of getting a random page ID
            Message routed = null;
            switch (mMode) {
                case NORMAL:
                    for (NetworkInterface i: getHost ().getInterfaces()) {
                        //We will use the first interface we find.
                        // Typically there should be only one anyway (either 3G or WLAN)
                        if (i instanceof WLANInterface) {
                            routed = routeToWifiRouter(m, (WLANInterface)i);
                            break;
                        }
                        else if (i instanceof CellularInterface) {
                            routed = routeToCellularTower(m, (CellularInterface)i);
                            break;
                        }
                    }
                    break;
                case WIFI_OFFLOADING:
                    for (NetworkInterface i: getHost ().getInterfaces()) {
                        if (i instanceof WLANInterface) {
                            routed = routeToWifiRouter(m, (WLANInterface)i);
                            break;
                        }
                    }
                    break;
                case P2P_CACHING:
                    for (NetworkInterface i: getHost ().getInterfaces()) {
                        if (i instanceof WLANInterface) {
                            routed = routeToPeer(m, (WLANInterface)i);
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
            if (routed != null) {
                System.out.println(getHost().toString() + " - message routed");
                break;
            }
        }
    }

    private Message routeToWifiRouter(Message m, WLANInterface wi) {
        for (Connection con : wi.getConnections()) {
            //Checking if the connection is to a WiFi router

            if (con.getOtherInterface(wi).getHost().getRouter() instanceof InfrastructureRouter) {
                int retVal = startTransfer(m, con);
                if (retVal == RCV_OK) {
                    return m; //Accepted m, don't try others
                }
                else if (retVal > 0) {
                    return null; //Couldn't send message through to WiFi router
                }
            }
        }
        return null; //WiFi router not found
    }

    private Message routeToCellularTower(Message m, CellularInterface ci) {
        DTNHost cellularTower = InfrastructureManager.getInstance().getCellularTower();
        for (Connection con : ci.getConnections()) {
            if (con.getOtherInterface(ci).getHost() == cellularTower) {
                int retVal = startTransfer(m, con);
                if (retVal == RCV_OK) {
                    return m; //Accepted m, don't try others
                }
            }
        }
        return null; //Cellular tower not found
    }

    private Message routeToPeer(Message m, WLANInterface wi) {
        //Send message to all known connections?! Besides WiFi router maybe?!
        return m;
    }

    //TODO: to remove if not needed anymore
    /*@Override
    public boolean createNewMessage(Message m) {
        //Adding the requested message ID property
        int randPage = WebPageDb.getInstance().getRandomPageId();
        m.addProperty(WebPageDb.WEB_REQUESTED_ID_PROPERTY, randPage);
        m.setAppID(InternetApplication.APP_ID);
        return super.createNewMessage(m);
    }*/

    @Override
    public MessageRouter replicate() {
        return new OffloadingRouter(this);
    }

    private enum RouteMode {
        NORMAL, WIFI_OFFLOADING, P2P_CACHING
    }
}
