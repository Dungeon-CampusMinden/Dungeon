package mp.client;

import level.elements.ILevel;
import mp.GameState;
import tools.Point;

import java.util.HashMap;

public interface IMultiplayerClientObserver {
    void onInitializeServerResponseReceived(boolean isSucceed, int clientId, Point initialHeroPosition);
    void onJoinSessionResponseReceived(boolean isSucceed, ILevel level, int clientId, HashMap<Integer, Point> heroPositionByClientId);
    void onGameStateUpdateEventReceived(GameState gameState);
    void onUpdateOwnPositionResponseReceived();
    void onConnected();
    void onDisconnected();
}
