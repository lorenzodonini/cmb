package routing;

import core.*;

import java.util.List;

/**
 * Created by lorenzodonini on 20/01/16.
 */
public class CellularRouter extends ActiveRouter {
    protected final int KB_SIZE = 1024;
    protected final int MB_SIZE = 1024*1024;
    private int counter = 0;
    private int mInternetAddress;
    private Message lastMessageToInet;
    private Message lastMessageFromInet;
    private static final String NS_INTERNET_INTERFACE = "internetInterface";
    private static final String NS_INTERNET_ADDRESS = "internetAddress";

    public CellularRouter(Settings s) {
        super(s);
        s.setNameSpace(NS_INTERNET_INTERFACE);
        mInternetAddress = s.getInt(NS_INTERNET_ADDRESS);
        s.restoreNameSpace();
    }

    public CellularRouter(CellularRouter other) {
        super(other);
        mInternetAddress = other.mInternetAddress;
    }

    @Override
    public void update() {
        super.update();

        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        for (Message m : getMessageCollection()) {
            if (m.getTo().getAddress() == mInternetAddress) {
                //We have a request packet to the internet
                Message result = routePacket(m);
                if (result != null) {
                    lastMessageToInet = result;
                    break;
                }
            }
            else if (m.getFrom().getAddress() == mInternetAddress) {
                //We have a response packet from the internet
                Message result = routePacket(m);
                if (result != null) {
                    lastMessageFromInet = result;
                    break;
                }
            }
        }

        // Try first the messages that can be delivered to final recipient
        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }

        List<Connection> conns = getConnections();
    }

    private Message routePacket(Message m) {
        for (Connection con: getConnections()) {
            if (con.getOtherNode(getHost()) == m.getTo()) {
                int retVal = startTransfer(m, con);
                if (retVal == RCV_OK) {
                    return m; //Accepted m, don't try others
                }
            }
        }
        return null;
    }

    @Override
    public MessageRouter replicate() {
        return new CellularRouter(this);
    }
}
