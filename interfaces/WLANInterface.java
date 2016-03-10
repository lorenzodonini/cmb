package interfaces;

import applications.InfrastructureManager;
import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
import routing.InfrastructureRouter;
import routing.MessageRouter;
import routing.OffloadingRouter;

import java.util.Iterator;

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

    @Override
    public void connect(NetworkInterface anotherInterface) {
        // Note: hotspots don't connect to anyone, only mobile nodes do. Hotspots are passive.
        if (!bIsHotspot
                && isScanning()
                && !isConnected(anotherInterface)
                && anotherInterface.getHost().isRadioActive()
                && isWithinRange(anotherInterface)
                && this != anotherInterface) {
            if (((WLANInterface)anotherInterface).isHotspot()
                    && currentHotspotInterface != null) {
                // This node won't perform any handover as long as it goes out of range of this hotspot.
                // Since it is already connected to a hotspot, it cannot connect to a second one.
                return;
            }
            else if (anotherInterface.getHost().getRouter() instanceof OffloadingRouter) {
                // The other node is a peer. We can only connect to it in case we both are either in
                // the same WLAN network or both of us are not connected to any hotspot whatsoever.
                if (currentHotspotInterface != null &&
                        ((WLANInterface)anotherInterface).getCurrentHotspot() != currentHotspotInterface) {
                    // The two peers are not connected to the same WLAN hotspot. Cannot connect them.
                    return;
                }
                else if (currentHotspotInterface == null &&
                        ((WLANInterface)anotherInterface).getCurrentHotspot() != null) {
                    // The other peer is already connected to a WLAN hotspot. Cannot connect to it.
                    return;
                }
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

            if (((WLANInterface)anotherInterface).isHotspot()) {
                // Connected to a hotspot. Storing this information locally
                currentHotspotInterface = anotherInterface;
                disconnectFromAllPeers();
            }
        }
    }

    @Override
    protected void disconnect(Connection con, NetworkInterface anotherInterface) {
        super.disconnect(con, anotherInterface);
        //if (anotherInterface.getHost().getRouter() instanceof InfrastructureRouter
        //        || getHost().getRouter() instanceof InfrastructureRouter) {
        if (bIsHotspot || ((WLANInterface)anotherInterface).isHotspot()) {
            // We remove the information regarding the hotspot we just disconnected from.
            if (bIsHotspot) {
                ((WLANInterface) anotherInterface).setCurrentHotspotInterface(null);
            }
            else {
                currentHotspotInterface = null;
                disconnectFromAllPeers();
            }
        }
    }

    /**
     * Getter method, needed for getting information about the current hotspot of this node.
     * @return  Returns the WLAN interface of the hotspot this node is currently connected to,
     * or null if not connected to any hotspot.
     */
    public NetworkInterface getCurrentHotspot() {
        return currentHotspotInterface;
    }

    /**
     * Getter method, needed in order to know if this node is a WLAN hotspot.
     * @return  Returns true if the node is a WLAN hotspot, false otherwise.
     */
    public boolean isHotspot() {
        return bIsHotspot;
    }

    /**
     * This gets called when the node connects or disconnects from a WLAN hotspot.
     * This is because P2P connections can only occur either within the same network or in ad-hoc mode,
     * when neither of the participants is connected to a hotspot.<br>
     * Whenever this happens, the current node needs to forcefully disconnect from all peers,
     * even if these are still in range.
     */
    private void disconnectFromAllPeers() {
        Iterator<Connection> it = getConnections().iterator();
        while (it.hasNext()) {
            Connection con = it.next();
            WLANInterface other = (WLANInterface)con.getOtherInterface(this);
            if (!other.isHotspot()) {
                //Disconnecting from a peer
                disconnect(con, other);
                it.remove();
            }
        }
    }

    /**
     * Setter method, allows a hotspot who initiated a connection/disconnection to tell
     * the other node explicitly that it has just conected to a WLAN hotspot.
     * The other node will automatically disconnect from all its peers since it has just changed network.
     * This method is never (and should never) be called on a hotspot node.
     *
     * @param ni  The network interface of the hotspot, or null if the hotspot
     *            was just disconnected from the node.
     */
    public void setCurrentHotspotInterface(NetworkInterface ni) {
        currentHotspotInterface = ni;
        disconnectFromAllPeers();
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
