package mp;

import level.elements.ILevel;

public interface IMultiplayer {
    void onMultiplayerSessionStarted(boolean isSucceed);
    void onMultiplayerSessionJoined(ILevel level);
}
