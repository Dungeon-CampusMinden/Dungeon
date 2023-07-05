package contrib.utils.multiplayer.client;

import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;
import contrib.utils.multiplayer.packages.GameState;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;

public interface IMultiplayerClientObserver {
    void onInitServerResponseReceived(boolean isSucceed, int clientId);
    void onLoadMapResponseReceived(boolean isSucceed, GameState gameState);
    void onChangeMapRequest();
    void onJoinSessionResponseReceived(boolean isSucceed, int clientId, GameState gameState);
    void onGameStateUpdateEventReceived(HashMap<Integer, Entity> heroesByClientId, Set<Entity> entities);
    void onUpdateOwnPositionResponseReceived();
    void onConnected(InetAddress address);
    void onDisconnected(InetAddress address);
}
