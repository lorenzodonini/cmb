package interfaces;

import core.Settings;
import routing.MessageRouter;

public abstract class SimpleIpInterface extends SimpleBroadcastInterface {
    public SimpleIpInterface(Settings s) {
        super(s);
    }

    public SimpleIpInterface(SimpleBroadcastInterface ni) {
        super(ni);
    }

    public abstract void init(MessageRouter router);
}
