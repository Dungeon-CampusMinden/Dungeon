package mp.client;

import level.elements.ILevel;

public interface IMultiplayerClientObserver {
    void onServerInitializedReceived(boolean isSucceed);
}
