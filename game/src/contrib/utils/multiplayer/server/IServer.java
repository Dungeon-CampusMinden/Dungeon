package contrib.utils.multiplayer.server;

import com.badlogic.gdx.utils.Null;

import java.io.IOException;

public interface IServer {

    int DEFAULT_TCP_PORT = 25444;

    int DEFAULT_UDP_PORT = DEFAULT_TCP_PORT + 1;

    /**
     * Used to implement inherited instances to listen on connections.
     *
     * @param port A preconfigured TCP port.
     * @throws IOException Should throw IOException if port can not be used.
     */
    void startListening(@Null Integer port) throws IOException;

    /**
     * Used to implement closing session and ports.
     */
    void stopListening();

    /**
     * Sending objects over TCP to single client.
     *
     * <p>NOTE: Should be used for messages that are sent at low frequently and are sure to get to the client.
     * <p>NOTE: For messages that should be sent high frequently and where package lost is not that bad, instead use
     * {@link IServer#sendUDP(int, Object)}
     *
     * @param clientID Unique client ID assigned from server.
     * @param object To be sent object.
     */
    void sendTCP(int clientID, Object object);

    /**
     * Sending objects over UDP to single client.
     *
     * <p>NOTE: Should be used for messages that are sent high frequently and do not need to arrive safely at the client.
     * <p>NOTE: For messages that should be sent low frequently and where package lost is not acceptable, instead use
     * {@link IServer#sendTCP(int, Object)}
     *
     * @param clientID Unique client ID assigned from server.
     * @param object To be sent object.
     */
    void sendUDP(int clientID, Object object);

    /**
     * Send object over TCP to all clients.
     *
     * <p>NOTE: Should be used for messages that are sent at low frequently and are sure to get to the client.
     * <p>NOTE: For messages that should be sent high frequently and where package lost is not that bad, instead use
     * {@link IServer#sendToAllUDP(Object)}
     *
     * @param object To be sent object.
     */
    void sendToAllTCP(Object object);

    /**
     * Send object over UDP to all clients.
     *
     * <p>NOTE: Should be used for messages that are sent high frequently and do not need to arrive safely at the client.
     * <p>NOTE: For messages that should be sent low frequently and where package lost is not acceptable, instead use
     * {@link IServer#sendToAllTCP(Object)}}
     *
     * @param object To be sent object.
     */
    void sendToAllUDP(Object object);

    /**
     * Send object over TCP to all clients.
     *
     * <p>NOTE: Should be used for messages that are sent at low frequently and are sure to get to the client.
     * <p>NOTE: For messages that should be sent high frequently and where package lost is not that bad, instead use
     * {@link IServer#sendToAllExceptUDP(int, Object)}
     *
     * @param object To be sent object.
     */
    void sendToAllExceptTCP(int clientID, Object object);

    /**
     * Send object over UDP to all clients.
     *
     * <p>NOTE: Should be used for messages that are sent high frequently and do not need to arrive safely at the client.
     * <p>NOTE: For messages that should be sent low frequently and where package lost is not acceptable, instead use
     * {@link IServer#sendToAllExceptTCP(int, Object)}
     *
     * @param object To be sent object.
     */
    void sendToAllExceptUDP(int clientID, Object object);

    /**
     * Add observer to implement customized actions.
     *
     * @param observer Observer reference to be added.
     */
    void addObserver(IServerObserver observer);

    /**
     * Remove observer.
     *
     * @param observer Observer reference to be removed.
     */
    void removeObserver(IServerObserver observer);
}
