package routing;

import core.*;

/**
 * Created by lorenzodonini on 28/02/16.
 */
public class InternetRouter extends ActiveRouter {

    public InternetRouter(Settings s) {
        super(s);
    }

    public InternetRouter(InternetRouter other) {
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

        for (Message m : getMessageCollection()) {
            if (m.getFrom() == getHost()) {
                Message req = m.getRequest();
                if (req == null) {
                    //Invalid message
                    continue;
                }

                /* We are routing towards the same infrastructure node (hotspot/celltower) from which we received
                the request to which we are answering. This is always the second hop, hence get(1).
                Since we are not doing any handovers, if the mobile node is not connected to the
                infrastructure node anymore when we send this reply, that node will never receive
                a response to his request. */
                DTNHost firstStep = req.getHops().get(1);
                for (Connection con : getConnections()) {
                    if (con.getOtherNode(getHost()) == firstStep) {
                        //This is the infrastructure node where the request came from
                        int retVal = startTransfer(m, con);
                        if (retVal == RCV_OK) {
                            return; //Accepted m, don't try others
                        }
                    }
                }
            }
        }
    }

    @Override
    public MessageRouter replicate() {
        return new InternetRouter(this);
    }
}
