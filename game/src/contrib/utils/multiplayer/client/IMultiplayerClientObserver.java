package contrib.utils.multiplayer.client;

import contrib.utils.multiplayer.packages.event.MovementEvent;
import core.Entity;
import core.utils.Point;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.request.*;

import java.net.InetAddress;
import java.util.Set;


/**
 * Used to customize actions based on received multiplayer server messages.
 */
public interface IMultiplayerClientObserver {
    /**
     * Called after response of request {@link InitializeServerRequest} received from server.
     *
     * @param isSucceed State whether server has been initialized successfully or not.
     * @param clientId From server assigned client ID.
     */
    void onInitializeServerResponseReceived(boolean isSucceed, int clientId);

    /**
     * Called after response of request {@link LoadMapRequest} received from server.
     *
     * @param isSucceed State whether data has been loaded successfully or not.
     * @param gameState Game state that has been set up on server side based on request data.
     */
    void onLoadMapResponseReceived(boolean isSucceed, GameState gameState);

    /**
     * Called after response of request {@link ChangeMapRequest} received from server.
     */
    void onChangeMapRequest();

    /**
     * Called after response of request {@link JoinSessionRequest} received from server.
     *
     * @param isSucceed State whether join request accepted or not.
     * @param clientId From server assigned client ID.
     * @param gameState Server side current global game state. Has to be synchronized with local state.
     * @param initialHeroPosition From server assigned start position of the hero. Has to be assigned locally.
     */
    void onJoinSessionResponseReceived(boolean isSucceed, int clientId, GameState gameState, Point initialHeroPosition);

    /**
     * Called after game state update received from server.
     *
     * @param entities Entities of current multiplayer state.
     */
    void onGameStateUpdateEventReceived(Set<Entity> entities);

    /**
     * Called after response of request {@link MovementEvent} received from server.
     */
    void onUpdatePositionResponseReceived();

    /**
     * Called after successfully connected to server.
     *
     * @param address Address of connected device.
     */
    void onConnected(InetAddress address);

    /**
     * Called after connection to server lost.
     *
     * @param address Address of disconnected device.
     */
    void onDisconnected(InetAddress address);
}
