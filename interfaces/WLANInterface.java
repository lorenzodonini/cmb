package interfaces;

import applications.InfrastructureManager;
import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
import routing.InfrastructureRouter;
import routing.MessageRouter;

public class WLANInterface extends SimpleIpInterface {
    private NetworkInterface currentHotspotInterface;
    private boolean bIsHotspot = false;

    public WLANInterface(Settings s) {
        super(s);
    }

    public WLANInterface(WLANInterface wi) {
        super(wi);
    }

    @Override
    public void init(MessageRouter router) {
        if (router instanceof InfrastructureRouter) {
            InfrastructureManager.getInstance().addWLANHotspot(getHost());
            bIsHotspot = true;
        }
    }

    public void connect(NetworkInterface anotherInterface) {
        // Note: hotspots don't connect to anyone, only mobile nodes do. Hotspots are passive.
        if (!bIsHotspot
                && isScanning()
                && !isConnected(anotherInterface)
                && anotherInterface.getHost().isRadioActive()
                && isWithinRange(anotherInterface)
                && this != anotherInterface) {
            if (anotherInterface.getHost().getRouter() instanceof InfrastructureRouter
                    && currentHotspotInterface != null) {
                // This node won't perform any handover as long as it goes out of range of this hotspot.
                // Since it is already connected to a hotspot, it cannot connect to a second one.
                return;
            }

            // new contact within range
            // connection speed is the lower one of the two speeds
            int conSpeed = anotherInterface.getTransmitSpeed(this);
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }
            Connection con = new CBRConnection(this.host, this,
                    anotherInterface.getHost(), anotherInterface, conSpeed);
            connect(con,anotherInterface);

            if (anotherInterface.getHost().getRouter() instanceof InfrastructureRouter) {
                // Connected to a hotspot. Storing this information locally
                currentHotspotInterface = anotherInterface;
            }
        }
    }

    @Override
    protected void disconnect(Connection con, NetworkInterface anotherInterface) {
        super.disconnect(con, anotherInterface);
        if (anotherInterface.getHost().getRouter() instanceof InfrastructureRouter
                || getHost().getRouter() instanceof InfrastructureRouter) {
            // We remove the information regarding the hotspot we just disconnected from.
            if (bIsHotspot) {
                ((WLANInterface) anotherInterface).setCurrentHotspotInterface(null);
            }
            else {
                currentHotspotInterface = null;
            }
        }
    }

    public void setCurrentHotspotInterface(NetworkInterface ni) {
        currentHotspotInterface = ni;
    }

    @Override
    public NetworkInterface replicate() {
        return new WLANInterface(this);
    }

    @Override
    public String toString() {
        return "WLANInterface " + super.toString();
    }
}
