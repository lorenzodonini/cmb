package applications;

import core.DTNHost;
import core.Settings;
import core.SimScenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lorenzodonini on 09/02/16.
 */
public class InfrastructureManager {
    private int internetAddress;
    private int cellularTowerAddress;
    private DTNHost internetNode;
    private DTNHost cellularTower;
    private List<DTNHost> wlanHotspots;
    private static InfrastructureManager mInstance;
    private static final String NS_INET_INTERFACE = "internetInterface";
    private static final String NS_INET_ADDRESS = "internetAddress";
    private static final String NS_CELLULAR_INTERFACE = "cellularInterface";
    private static final String NS_CELLULAR_ADDRESS = "towerAddress";

    private InfrastructureManager() {
        wlanHotspots = new ArrayList<>();
        Settings s = new Settings();
        s.setNameSpace(NS_INET_INTERFACE);
        internetAddress = s.getInt(NS_INET_ADDRESS);
        s.restoreNameSpace();
        s.setNameSpace(NS_CELLULAR_INTERFACE);
        cellularTowerAddress = s.getInt(NS_CELLULAR_ADDRESS);
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
            List<DTNHost> hosts = SimScenario.getInstance().getHosts();
            for (DTNHost host : hosts) {
                if(host.getAddress() == internetAddress) {
                    internetNode = host;
                    break;
                }
            }
        }
        return internetNode;
    }

    public DTNHost getCellularTower() {
        if (cellularTower == null) {
            List<DTNHost> hosts = SimScenario.getInstance().getHosts();
            for (DTNHost host : hosts) {
                if (host.getAddress() == cellularTowerAddress) {
                    cellularTower = host;
                    break;
                }
            }
        }
        return cellularTower;
    }

    public List<DTNHost> getWLANHotspots() {
        return wlanHotspots;
    }

    public void addWLANHotspot(DTNHost host) {
        wlanHotspots.add(host);
    }
}
