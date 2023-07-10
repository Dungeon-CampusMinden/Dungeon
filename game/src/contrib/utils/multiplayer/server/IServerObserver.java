package contrib.utils.multiplayer.server;

import contrib.utils.multiplayer.packages.event.MovementEvent;
import contrib.utils.multiplayer.packages.request.*;


public interface IServerObserver {

    /**
     * Called when new connection to a client opened.
     *
     * @param clientID clientID Unique endpoint ID assigned from server.
     */
    void onClientConnected(int clientID);

    /**
     * Called when connection to a client has been lost.
     *
     * @param clientID Unique endpoint ID assigned from server.
     */
    void onClientDisconnected(int clientID);

    /**
     * Called when ping request received.
     *
     * @param clientID Unique endpoint ID assigned from server.
     * @param request Request data sent by client.
     */
    void onPingRequestReceived(int clientID, PingRequest request);

    /**
     * Called when request received to initialize the server.
     *
     * @param clientID Unique endpoint ID assigned from server.
     * @param request Request data sent by client.
     */
    void onInitializeRequestReceived(int clientID, InitializeServerRequest request);

    /**
     * Called when request received to load a map based on request data.
     *
     * @param clientID Unique endpoint ID assigned from server.
     * @param request Request data sent by client.
     */
    void onLoadMapRequestReceived(int clientID, LoadMapRequest request);

    /**
     * Called when request received to change the map.
     *
     * @param clientID Unique endpoint ID assigned from server.
     * @param request Request data sent by client.
     */
    void onChangeMapRequestReceived(int clientID, ChangeMapRequest request);

    /**
     * Called when request received to join the session.
     *
     * @param clientID Unique endpoint ID assigned from server.
     * @param request Request data sent by client.
     */
    void onJoinSessionRequestReceived(int clientID, JoinSessionRequest request);

    /**
     * Called when endpoint informs about movement of an entity.
     *
     * @param clientID Unique endpoint ID assigned from server.
     * @param event Movement data sent by client.
     */
    void onMovementEventReceived(int clientID, MovementEvent event);
}
