package interfaces;

import applications.InfrastructureManager;
import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;

public class InternetInterface extends SimpleBroadcastInterface {
    private int internetNodeAddress = -1;

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

    public void update() {
        super.update();
        if (internetNodeAddress < 0) {
            internetNodeAddress = InfrastructureManager.getInstance().getInternetNode().getAddress();
        }
    }

    @Override
    public void connect(NetworkInterface anotherInterface) {
        if (anotherInterface.getHost().getAddress() == internetNodeAddress
                && getHost().getAddress() != internetNodeAddress
                && !isConnected(anotherInterface)) {
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
