package contrib.utils.multiplayer;


import core.level.elements.ILevel;

public interface IMultiplayer {
    void onMultiplayerServerInitialized(boolean isSucceed);
    void onMultiplayerSessionJoined(boolean isSucceed, ILevel level);
    void onMapLoad(ILevel level);
    void onChangeMapRequest();
    void onMultiplayerSessionLost();
}
