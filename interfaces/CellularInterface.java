package interfaces;

import applications.InfrastructureManager;
import core.*;

public class CellularInterface extends SimpleBroadcastInterface {
    private int cellularTowerAddress = -1;

    public CellularInterface(Settings s) {
        super(s);
    }

    public CellularInterface(CellularInterface ci) {
        super(ci);
    }

    @Override
    public NetworkInterface replicate() {
        return new CellularInterface(this);
    }

    @Override
    public void update() {
        super.update();
        if (cellularTowerAddress < 0) {
            cellularTowerAddress = InfrastructureManager.getInstance().getCellularTower().getAddress();
        }
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

    @Override
    public String toString() {
        return "CellularInterface " + super.toString();
    }
}
