package mp;

import level.elements.ILevel;
import tools.Point;

public interface IMultiplayer {
    void onMultiplayerSessionStarted(boolean isSucceed);
    void onMultiplayerSessionJoined(boolean isSucceed, ILevel level);
    void onMapLoad(ILevel level);
    void onChangeMapRequest();
    void onMultiplayerSessionLost();
}
