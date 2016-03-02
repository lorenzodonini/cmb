package routing;

import applications.InfrastructureManager;
import core.*;

/**
 * Created by lorenzodonini on 20/01/16.
 */
public class InfrastructureRouter extends ActiveRouter {

    public InfrastructureRouter(Settings s) {
        super(s);
    }

    public InfrastructureRouter(InfrastructureRouter other) {
        super(other);
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
            return; // started a transfer, don't try others (yet)
        }

        int internetAddress = InfrastructureManager.getInstance().getInternetNode().getAddress();

        for (Message m : getMessageCollection()) {
            if (m.getTo().getAddress() == internetAddress) {
                //We have a request packet to the internet
                Message result = routePacket(m);
                if (result != null) {
                    break;
                }
            }
            else if (m.getFrom().getAddress() == internetAddress) {
                //We have a response packet from the internet
                Message result = routePacket(m);
                if (result != null) {
                    break;
                }
            }
        }
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
        return new InfrastructureRouter(this);
    }
}
