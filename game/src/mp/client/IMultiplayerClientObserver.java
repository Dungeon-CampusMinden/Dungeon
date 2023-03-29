package mp.client;

import level.elements.ILevel;

public interface IMultiplayerClientObserver {
    void onServerInitializedReceived(boolean isSucceed, int id);
    void onSessionJoined(ILevel level, int id);
}
