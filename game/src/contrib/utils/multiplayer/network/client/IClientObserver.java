package contrib.utils.multiplayer.network.client;

import contrib.utils.multiplayer.network.packages.GameState;
import contrib.utils.multiplayer.network.packages.request.ChangeMapRequest;
import contrib.utils.multiplayer.network.packages.request.JoinSessionRequest;
import contrib.utils.multiplayer.network.packages.request.LoadMapRequest;

import core.Entity;
import core.utils.Point;

import java.util.Set;

/** Used to customize actions based on received multiplayer server messages. */
public interface IClientObserver {

    /** Called when successfully connected to server. */
    void onConnectedToServer();

    /** Called when connection to server lost. */
    void onDisconnectedFromServer();

    /** Called when request received to authenticate from server. */
    void onAuthenticationRequestReceived();

    /**
     * Called when event received that client has been authenticated from server.
     *
     * @param clientID From server assigned client ID.
     */
    void onAuthenticatedEventReceived(int clientID);

    /**
     * Called when response of request {@link LoadMapRequest} received from server.
     *
     * @param isSucceed State whether data has been loaded successfully or not.
     * @param gameState Game state that has been set up on server side based on request data.
     */
    void onLoadMapResponseReceived(boolean isSucceed, GameState gameState);

    /** Called when response of request {@link ChangeMapRequest} received from server. */
    void onChangeMapRequestReceived();

    /**
     * Called when response of request {@link JoinSessionRequest} received from server.
     *
     * @param isSucceed State whether join request accepted or not.
     * @param clientId From server assigned client ID.
     * @param gameState Server side current global game state. Has to be synchronized with local
     *     state.
     * @param initialHeroPosition From server assigned start position of the hero. Has to be
     *     assigned locally.
     */
    void onJoinSessionResponseReceived(
            boolean isSucceed, int clientId, GameState gameState, Point initialHeroPosition);

    /**
     * Called when game state update received from server.
     *
     * @param entities Entities of current multiplayer state.
     */
    void onGameStateUpdateEventReceived(Set<Entity> entities);
}
