package contrib.utils.multiplayer.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.NetworkSetup;
import contrib.utils.multiplayer.packages.response.*;
import contrib.utils.multiplayer.packages.event.GameStateUpdateEvent;

import java.io.*;
import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

/**
 * Used to send/receive data to/from multiplayer server.
 */
public class MultiplayerClient extends Listener {

    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final int DEFAULT_MAX_OBJECT_SIZE_EXPECTED = 8000000;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = DEFAULT_MAX_OBJECT_SIZE_EXPECTED;
    private static final int DEFAULT_OBJECT_BUFFER_SIZE = DEFAULT_MAX_OBJECT_SIZE_EXPECTED;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final Client client = new Client(DEFAULT_WRITE_BUFFER_SIZE, DEFAULT_OBJECT_BUFFER_SIZE);
    private final ArrayList<IMultiplayerClientObserver> observers = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public MultiplayerClient() {
        client.addListener(this);
        NetworkSetup.registerCommunicationClasses(client);
    }

    @Override
    public void connected(Connection connection) {
        for (IMultiplayerClientObserver observer: observers) {
            if (connection.getRemoteAddressTCP() != null)
                observer.onConnected(connection.getRemoteAddressTCP().getAddress());
            else
                observer.onConnected(null);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        for (IMultiplayerClientObserver observer: observers) {
            if (connection.getRemoteAddressTCP() != null)
                observer.onDisconnected(connection.getRemoteAddressTCP().getAddress());
            else
                observer.onDisconnected(null);
        }
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingResponse pingResponse) {
            System.out.println("Ping response received. Time: " + pingResponse.getTime());
        } else if (object instanceof InitializeServerResponse initServerResponse){
            final boolean isSucceed = initServerResponse.isSucceed();
            for (IMultiplayerClientObserver observer: observers){
                observer.onInitializeServerResponseReceived(isSucceed, connection.getID());
            }
        } else if (object instanceof LoadMapResponse loadMapResponse) {
            final boolean isSucceed = loadMapResponse.isSucceed();
            final GameState gameState = loadMapResponse.gameState();
            for (IMultiplayerClientObserver observer : observers) {
                observer.onLoadMapResponseReceived(isSucceed, gameState);
            }
        } else if (object instanceof ChangeMapResponse){
            for (IMultiplayerClientObserver observer : observers){
                observer.onChangeMapRequest();
            }
        } else if (object instanceof JoinSessionResponse response) {
            for (IMultiplayerClientObserver observer: observers) {
                observer.onJoinSessionResponseReceived(
                    response.isSucceed(),
                    response.heroGlobalID(),
                    response.gameState(),
                    response.initialPosition()
                );
            }
        } else if (object instanceof GameStateUpdateEvent gameStateUpdateEvent){
            for (IMultiplayerClientObserver observer: observers) {
                observer.onGameStateUpdateEventReceived(gameStateUpdateEvent.entities());
            }
        }
    }

    /**
     * Send instance over TCP.
     *
     * @param object To be sent instance.
     */
    public void sendTCP(Object object) {
        client.sendTCP(requireNonNull(object));
    }

    /**
     * Send instance over UDP.
     *
     * @param object To be sent instance.
     */
    public void sendUDP(Object object) {
        client.sendUDP(requireNonNull(object));
    }

    /**
     * Connect to a endpoint/device.
     *
     * @param address IP address of device to be connected to.
     * @param port Port to be connected to. Will be used as TCP port. UDP port will be TCP port + 1;
     * @return True, if connected successfully. False, otherwise.
     */
    public boolean connectToHost(String address, int port) {
        try {
            client.start();
            client.connect(DEFAULT_CONNECTION_TIMEOUT, address, port, port + 1);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Disconnect from other endpoint.
     * Messages can not be sent and received anymore until reconnect.
     */
    public void disconnect() {
        client.close();
    }

    /**
     * Add observer to implement customized actions.
     *
     * @param observer Observer reference to be added.
     */
    public void addObserver(final IMultiplayerClientObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove observer.
     *
     * @param observer Observer reference to be removed.
     */
    public void removeObserver(final IMultiplayerClientObserver observer) {
        observers.remove(observer);
    }
}
