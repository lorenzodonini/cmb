package applications;

import core.DTNHost;
import core.NetworkInterface;
import core.Settings;
import core.SimScenario;
import interfaces.CellularInterface;
import interfaces.InternetInterface;
import javafx.scene.control.Cell;
import routing.InternetRouter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lorenzodonini on 09/02/16.
 */
public class InfrastructureManager {
    private DTNHost internetNode;
    private DTNHost cellularTower;
    private Set<DTNHost> wlanHotspots;
    private static InfrastructureManager mInstance;
    private static final String NS_INET_INTERFACE = "internetInterface";
    private static final String NS_CELLULAR_INTERFACE = "cellularInterface";

    private InfrastructureManager() {
        wlanHotspots = new HashSet<>();
        Settings s = new Settings();
        s.setNameSpace(NS_INET_INTERFACE);
        s.restoreNameSpace();
        s.setNameSpace(NS_CELLULAR_INTERFACE);
        s.restoreNameSpace();
    }

    public static InfrastructureManager getInstance() {
        if (mInstance == null) {
            mInstance = new InfrastructureManager();
        }
        return mInstance;
    }

    public DTNHost getInternetNode() {
        return internetNode;
    }

    public void setInternetNode(DTNHost internet) {
        internetNode = internet;
    }

    public DTNHost getCellularTower() {
        return cellularTower;
    }

    public void setCellularTower(DTNHost tower) {
        cellularTower = tower;
    }

    public Set<DTNHost> getWLANHotspots() {
        return wlanHotspots;
    }

    public boolean isWLANHotspot(DTNHost host) {
        return wlanHotspots.contains(host);
    }

    public void addWLANHotspot(DTNHost host) {
        wlanHotspots.add(host);
    }
}
