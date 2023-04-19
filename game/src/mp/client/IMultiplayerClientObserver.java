package mp.client;

import level.elements.ILevel;

import java.util.HashMap;

public interface IMultiplayerClientObserver {
    void onServerInitializedReceived(boolean isSucceed, int id);
    void onSessionJoined(ILevel level, int id, HashMap playerPositions);
    void onPositionUpdate(HashMap playerPositions);
}
