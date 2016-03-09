package interfaces;

import applications.InfrastructureManager;
import core.*;
import routing.InfrastructureRouter;
import routing.MessageRouter;

public class CellularInterface extends SimpleIpInterface {

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
    public void init(MessageRouter router) {
        if (router instanceof InfrastructureRouter) {
            InfrastructureManager.getInstance().setCellularTower(getHost());
        }
    }

    @Override
    public void connect(NetworkInterface anotherInterface) {
        if (isScanning()
                && anotherInterface.getHost() == InfrastructureManager.getInstance().getCellularTower()
                && anotherInterface.getHost().isRadioActive()
                && !isConnected(anotherInterface)
                && isWithinRange(anotherInterface)
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
