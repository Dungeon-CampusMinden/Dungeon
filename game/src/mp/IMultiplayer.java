package mp;

import level.elements.ILevel;
import tools.Point;

import java.util.HashMap;

public interface IMultiplayer {
    void onMultiplayerSessionStarted(boolean isSucceed);
    void onMultiplayerSessionJoined(ILevel level);
}
