package interfaces;

import core.NetworkInterface;
import core.Settings;

/**
 * Created by lorenzodonini on 11/02/16.
 */
public class WLANInterface extends SimpleBroadcastInterface {
    public WLANInterface(Settings s) {
        super(s);
    }

    public WLANInterface(WLANInterface wi) {
        super(wi);
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
