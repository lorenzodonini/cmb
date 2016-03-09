package routing;

import applications.InfrastructureManager;
import applications.MobileWebApplication;
import core.*;
import interfaces.SimpleIpInterface;
import interfaces.WLANInterface;

import java.util.*;

public class OffloadingRouter extends ActiveRouter {
    private boolean p2pOffloadingEnabled;
    private boolean wifiOffloadingEnabled;
    private int offloadingTimeLimit;
    private List<Connection> currentHotspots;
    private Set<Connection> currentNeighbors;
    private Connection currentCellularTower;

    //Settings
    private static final String S_P2P_ENABLED = "p2pEnabled";
    private static final String S_OFFLOAD_ENABLED = "wifiOffloadEnabled";
    private static final String S_OFFLOAD_WAIT_TIME = "offloadWaitTime";

    private static final String NS_ROUTING = "OffloadingRouter"; //Namespace

    private static final String PROP_CONTACTED_NEIGHBORS = "neighbors";

    public OffloadingRouter(Settings s) {
        super(s);
        s.setNameSpace(NS_ROUTING);

        p2pOffloadingEnabled = s.getBoolean(S_P2P_ENABLED, false);
        wifiOffloadingEnabled = s.getBoolean(S_OFFLOAD_ENABLED, false);
        offloadingTimeLimit = s.getInt(S_OFFLOAD_WAIT_TIME, -1);
        s.restoreNameSpace();
    }

    public OffloadingRouter(OffloadingRouter wr) {
        super(wr);

        offloadingTimeLimit = wr.offloadingTimeLimit;
        p2pOffloadingEnabled = wr.p2pOffloadingEnabled;
        wifiOffloadingEnabled = wr.wifiOffloadingEnabled;
        currentHotspots = new LinkedList<>();

        if (p2pOffloadingEnabled) {
            currentNeighbors = new HashSet<>();
        }
    }

    @Override
    public void init(DTNHost host, List<MessageListener> mListeners) {
        super.init(host, mListeners);
        for (NetworkInterface ni : getHost().getInterfaces()) {
            if (ni instanceof SimpleIpInterface) {
                ((SimpleIpInterface)ni).init(this);
            }
        }
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
            /* We still return, since a transfer was started. This may be because the router delivered
            a message to its final recipient, or because the router asked the
            connected hosts for further messages.*/
            return;
        }

        Collection<Message> collection = getMessageCollection();
        Message [] messages = collection.toArray(new Message[collection.size()]);
        double currentTime = SimClock.getTime();

        for (Message m : messages) {
            double routeStartTime = m.getCreationTime();

            //P2P offloading
            if (p2pOffloadingEnabled) {
                Set<Connection> triedNeighbors;
                Object prop = m.getProperty(PROP_CONTACTED_NEIGHBORS);
                if (prop == null) {
                    triedNeighbors = new HashSet<>();
                    m.addProperty(PROP_CONTACTED_NEIGHBORS, triedNeighbors);
                }
                else {
                    triedNeighbors = (Set<Connection>)prop;
                }
                if (tryMessageToPeers(m,triedNeighbors)) {
                    // Not removing the message itself, because this will be done by
                    // the application, in case we receive a response from a peer.

                    return; //We managed to start the transfer
                }
            }
            //WLAN offloading
            if (wifiOffloadingEnabled &&
                    (currentTime - routeStartTime <= offloadingTimeLimit)) {
                //We try to offload only if the time limit hasn't been exceeded yet
                if (tryOffloadToHotspots(m)) {
                    return; //We managed to start the transfer
                }
            }
            //Traditional routing
            if (currentTime - routeStartTime > offloadingTimeLimit) {
                //We route normally only if there are no offloading time constraints
                if (tryMessageToInternet(m)) {
                    return; //We managed to start the transfer
                }
            }
        }
    }

    private boolean tryMessageToPeers(Message m, Set<Connection> triedNeighbors) {
        //Replicating the original message, since we are playing around with the app ID
        Message replica = m.replicate();
        //Peers won't handle this message unless we set this app id
        replica.setAppID(MobileWebApplication.APP_ID);

        for (Connection con : currentNeighbors) {
            if (!triedNeighbors.contains(con)) {
                triedNeighbors.add(con);
                int retVal = startTransfer(replica,con);
                //We tried to this neighbor already, don't wanna do it again in the future -> remove it
                if (retVal == RCV_OK) {
                    notifyAppListeners(MobileWebApplication.E_REQ_SENT_P2P,new Object[] {m,SimClock.getTime()});
                    return true; //Accepted the message, don't try others
                }
                /* In case retVal is != RCV_OK, something went wrong. We don't care and move on.
                That peer has been added to the list of tried neighbors anyway. */
            }
        }
        return false;
    }

    private boolean tryOffloadToHotspots(Message m) {
        for (Connection con : currentHotspots) {
            int retVal = startTransfer(m, con);
            if (retVal == RCV_OK) {
                notifyAppListeners(MobileWebApplication.E_REQ_SENT_OFFLOADED,new Object[] {m,SimClock.getTime()});
                return true; //Accepted the message, don't try others
            }
        }
        return false;
    }

    /*
    OLD LOGIC, NOT USED ANYMORE
    private boolean tryMessageToPeers(Message m, List<Connection> peers) {
        //Replicating the original message, since we are playing around with the app ID
        Message replica = m.replicate();
        //Peers won't handle this message unless we set this app id
        replica.setAppID(MobileWebApplication.APP_ID);

        //Trying to send the message
        Iterator<Connection> it = peers.iterator();
        while (it.hasNext()) {
            Connection con = it.next();
            int retVal = startTransfer(replica,con);
            //We tried to this neighbor already, don't wanna do it again in the future -> remove it
            it.remove();
            if (retVal == RCV_OK) {
                return true; //Accepted the message, don't try others
            }
            /* In case retVal is != RCV_OK, something went wrong. Maybe the neighbor went out of range.
            We don't care and move on. That peer has been deleted from the neighbor list anyway.
        }
        return false;
    }*/

    private boolean tryMessageToInternet(Message m) {
        for (Connection con : currentHotspots) {
            //Trying over WiFi first, by default
            int retVal = startTransfer(m,con);
            if (retVal == RCV_OK) {
                notifyAppListeners(MobileWebApplication.E_REQ_SENT_WIFI,new Object[] {m,SimClock.getTime()});
                return true; //Accepted the message, don't try others
            }
        }
        if (currentCellularTower != null) {
            //Trying over 3G
            int retVal = startTransfer(m,currentCellularTower);
            if (retVal == RCV_OK) {
                notifyAppListeners(MobileWebApplication.E_REQ_SENT_CELLULAR,new Object[] {m,SimClock.getTime()});
                return true; //Accepted the message, don't try others
            }
        }
        //If none of the above work, it means there is currently not connection at all
        return false;
    }

    /*private List<Connection> getWifiNeighbors() {
        List<Connection> neighbors = new LinkedList<>();
        InfrastructureManager infrastructure = InfrastructureManager.getInstance();
        DTNHost host = getHost();

        for (NetworkInterface i: getHost().getInterfaces()) {
            //Only doing this for the proper interface
            if (i instanceof WLANInterface) {
                //Checking all connections over the WLAN interface
                for (Connection con: i.getConnections()) {
                    //If the other endpoint of the connection isn't an infrastructure node,
                    // then it's a peer (neighbor)
                    if (!infrastructure.isWLANHotspot(con.getOtherNode(host))) {
                        neighbors.add(con);
                    }
                }
            }
        }
        return neighbors;
    }*/

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);

        if (con.isUp()) {
            if (InfrastructureManager.getInstance().isWLANHotspot(con.getOtherNode(getHost()))) {
                currentHotspots.add(con);
            }
            else if (con.getOtherInterface(getWLANInterface()) instanceof WLANInterface && p2pOffloadingEnabled) {
                currentNeighbors.add(con);
            }
            else if (InfrastructureManager.getInstance().getCellularTower() == con.getOtherNode(getHost())) {
                currentCellularTower = con;
            }
        }
        else {
            if (InfrastructureManager.getInstance().isWLANHotspot(con.getOtherNode(getHost()))) {
                currentHotspots.remove(con);
            }
            else if (con.getOtherInterface(getWLANInterface()) instanceof WLANInterface && p2pOffloadingEnabled) {
                currentNeighbors.remove(con);
            }
            else if (InfrastructureManager.getInstance().getCellularTower() == con.getOtherNode(getHost())) {
                currentCellularTower = null;
            }
        }
    }

    private NetworkInterface getWLANInterface() {
        for (NetworkInterface ni : getHost().getInterfaces()) {
            if (ni instanceof WLANInterface) {
                return ni;
            }
        }
        return null;
    }

    private void notifyAppListeners(String event, Object params) {
        DTNHost host = getHost();
        for (Application a : getApplications(MobileWebApplication.APP_ID)) {
            a.sendEventToListeners(event,params,host);
        }
    }

    /*@Override
    public void update() {
        super.update();

        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        // Try first the messages that can be delivered to final recipient
        Connection delivered = exchangeDeliverableMessages();
        if (delivered != null) {
            // We still return, since a transfer was started. This may be because the router delivered
            //a message to its final recipient, or because the router asked the
            //connected hosts for further messages.
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
    }*/

    @Override
    public MessageRouter replicate() {
        return new OffloadingRouter(this);
    }
}
