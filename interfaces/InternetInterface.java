package interfaces;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;

/**
 * Created by lorenzodonini on 08/02/16.
 */
public class InternetInterface extends SimpleBroadcastInterface {
    private int internetNodeAddress;
    private static final String NS_INTERNET_ADDRESS = "internetAddress";

    public InternetInterface(Settings s) {
        super(s);
        internetNodeAddress = s.getInt(NS_INTERNET_ADDRESS);
    }

    public InternetInterface(InternetInterface ii) {
        super(ii);
        internetNodeAddress = ii.internetNodeAddress;
    }

    @Override
    public NetworkInterface replicate() {
        return new InternetInterface(this);
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
