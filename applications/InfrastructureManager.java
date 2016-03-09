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
    private static final String NS_INET_ADDRESS = "internetAddress";
    private static final String NS_CELLULAR_INTERFACE = "cellularInterface";
    private static final String NS_CELLULAR_ADDRESS = "towerAddress";

    private InfrastructureManager() {
        wlanHotspots = new HashSet<>();
        Settings s = new Settings();
        s.setNameSpace(NS_INET_INTERFACE);
        //internetAddress = s.getInt(NS_INET_ADDRESS);
        s.restoreNameSpace();
        s.setNameSpace(NS_CELLULAR_INTERFACE);
        //cellularTowerAddress = s.getInt(NS_CELLULAR_ADDRESS);
        s.restoreNameSpace();
    }

    public static InfrastructureManager getInstance() {
        if (mInstance == null) {
            mInstance = new InfrastructureManager();
        }
        return mInstance;
    }

    public DTNHost getInternetNode() {
        if (internetNode == null) {
            //findInternetNode();
        }
        return internetNode;
    }

    public void setInternetNode(DTNHost internet) {
        internetNode = internet;
    }

    /*private void findInternetNode() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        for (DTNHost host : hosts) {
            if (host.getRouter() instanceof InternetRouter) {
                internetNode = host;
                return;
            }
        }
    }*/

    public DTNHost getCellularTower() {
        if (cellularTower == null) {
            //findCellularTower();
        }
        return cellularTower;
    }

    public void setCellularTower(DTNHost tower) {
        cellularTower = tower;
    }

    /*private void findCellularTower() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        for (DTNHost host : hosts) {
            if (host.getInterfaces().size() == 2
                    && host.getInterface(1) instanceof InternetInterface
                    && host.getInterface(2) instanceof CellularInterface) {
                cellularTower = host;
                return;
            }
        }
    }*/

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
