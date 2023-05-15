package mp;


import core.level.elements.ILevel;

public interface IMultiplayer {
    void onMultiplayerSessionStarted(boolean isSucceed);
    void onMultiplayerSessionJoined(boolean isSucceed, ILevel level);
    void onMapLoad(ILevel level);
    void onChangeMapRequest();
    void onMultiplayerSessionLost();
}
