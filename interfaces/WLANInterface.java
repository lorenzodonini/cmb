package interfaces;

import applications.InfrastructureManager;
import core.NetworkInterface;
import core.Settings;
import routing.InfrastructureRouter;

public class WLANInterface extends SimpleBroadcastInterface {
    private boolean bInitialized = false;

    public WLANInterface(Settings s) {
        super(s);
    }

    public WLANInterface(WLANInterface wi) {
        super(wi);
    }

    @Override
    public void update() {
        super.update();
        if (!bInitialized) {
            if (getHost().getRouter() instanceof InfrastructureRouter) {
                InfrastructureManager.getInstance().addWLANHotspot(getHost());
            }
            bInitialized = true;
        }
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
