package interfaces;

import core.*;

/**
 * Created by lorenzodonini on 30/01/16.
 */
public class CellularInterface extends SimpleBroadcastInterface {
    private int cellularTowerAddress;
    private static final String NS_TOWER_ADDRESS = "towerAddress";

    public CellularInterface(Settings s) {
        super(s);
        cellularTowerAddress = s.getInt(NS_TOWER_ADDRESS);
    }

    public CellularInterface(CellularInterface ci) {
        super(ci);
        cellularTowerAddress = ci.cellularTowerAddress;
    }

    @Override
    public NetworkInterface replicate() {
        return new CellularInterface(this);
    }

    @Override
    public void connect(NetworkInterface anotherInterface) {
        if (isScanning()
                && anotherInterface.getHost().getAddress() == cellularTowerAddress
                && anotherInterface.getHost().isRadioActive()
                && !isConnected(anotherInterface)
                && (this != anotherInterface)) {
            // new contact within range
            // connection speed is the lower one of the two speeds
            int conSpeed = anotherInterface.getTransmitSpeed(this);
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }

            Connection con = new CBRConnection(this.host, this,
                    anotherInterface.getHost(), anotherInterface, conSpeed);
            connect(con,anotherInterface);
        }
    }

    public String toString() {
        return "CellularInterface " + super.toString();
    }
}
