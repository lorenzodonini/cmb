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

    @Override
    protected void transferDone(Connection con) {
        super.transferDone(con);

        Message transferred = con.getMessage();
        if(transferred.getProperty(MobileWebApplication.PROP_P2P_RESPONSE) != null) {
            /* Removing the response message from the queue directly.
            We know that is was delivered already, and since we don't want the
            router to try resending it until the TLL expires, we drop it immediately. */
            deleteMessage(transferred.getId(),false);
        }
    }

    @Override
    public MessageRouter replicate() {
        return new OffloadingRouter(this);
    }
}
