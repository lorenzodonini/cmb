package routing;

import applications.InfrastructureManager;
import core.*;
import interfaces.SimpleIpInterface;

import java.util.List;

public class InfrastructureRouter extends ActiveRouter {

    public InfrastructureRouter(Settings s) {
        super(s);
    }

    public InfrastructureRouter(InfrastructureRouter other) {
        super(other);
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
            return; // started a transfer, don't try others (yet)
        }

        DTNHost internetNode = InfrastructureManager.getInstance().getInternetNode();

        for (Message m : getMessageCollection()) {
            if (m.getTo() == internetNode) {
                //We have a request packet to the internet
                Message result = routePacket(m);
                if (result != null) {
                    break;
                }
            }
            else if (m.getFrom() == internetNode) {
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

    /*@Override
    public Message messageTransferred(String id, DTNHost from) {
        Message transferred = super.messageTransferred(id, from);

        /* This is a passive infrastructure router. If it received a response,
        then it was because a request message had been relayed before that.
        Once a valid response has been received, the request message should
        never be relayed again.
        if (transferred.getRequest() != null) {
            deleteMessage(transferred.getRequest().getId(),false);
        }

        return transferred;
    }*/

    @Override
    protected void transferDone(Connection con) {
        super.transferDone(con);

        Message transferred = con.getMessage();
        if (transferred.getRequest() != null) {
            if (transferred.getTo() == transferred.getRequest().getFrom()) {
                deleteMessage(transferred.getId(),false);
                deleteMessage(transferred.getRequest().getId(),false);
            }
        }
    }

    @Override
    public MessageRouter replicate() {
        return new InfrastructureRouter(this);
    }
}
