package interfaces;

import applications.InfrastructureManager;
import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
import routing.InternetRouter;
import routing.MessageRouter;

public class InternetInterface extends SimpleIpInterface {

    public InternetInterface(Settings s) {
        super(s);
    }

    public InternetInterface(InternetInterface ii) {
        super(ii);
    }

    @Override
    public NetworkInterface replicate() {
        return new InternetInterface(this);
    }

    @Override
    public void init(MessageRouter router) {
        if (router instanceof InternetRouter) {
            InfrastructureManager.getInstance().setInternetNode(getHost());
        }
    }

    @Override
    public void connect(NetworkInterface anotherInterface) {
        if (anotherInterface.getHost() == InfrastructureManager.getInstance().getInternetNode()
                && getHost() != InfrastructureManager.getInstance().getInternetNode()
                && !isConnected(anotherInterface)
                && isWithinRange(anotherInterface)) {
            int conSpeed = anotherInterface.getTransmitSpeed(this);
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }

            Connection con = new CBRConnection(this.host, this, anotherInterface.getHost(),
                    anotherInterface, conSpeed);
            connect(con,anotherInterface);
        }
    }

    @Override
    public String toString() {
        return "InternetInterface " + super.toString();
    }
}
