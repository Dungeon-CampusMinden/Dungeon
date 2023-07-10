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
public class MultiplayerClient extends Listener implements IClient {

    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final int DEFAULT_MAX_OBJECT_SIZE_EXPECTED = 8000000;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = DEFAULT_MAX_OBJECT_SIZE_EXPECTED;
    private static final int DEFAULT_OBJECT_BUFFER_SIZE = DEFAULT_MAX_OBJECT_SIZE_EXPECTED;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final Client client = new Client(DEFAULT_WRITE_BUFFER_SIZE, DEFAULT_OBJECT_BUFFER_SIZE);
    private final ArrayList<IClientObserver> observers = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * <p>To customize actions based on internal events. use {@link IClient#addObserver(IClientObserver)}.
     */
    public MultiplayerClient() {
        client.addListener(this);
        NetworkSetup.registerCommunicationClasses(client);
    }

    @Override
    public void connected(Connection connection) {
        for (IClientObserver observer: observers) {
            observer.onConnectedToServer();
        }
    }

    @Override
    public void disconnected(Connection connection) {
        for (IClientObserver observer: observers) {
            observer.onDisconnectedFromServer();
        }
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingResponse pingResponse) {
            System.out.println("Ping response received. Time: " + pingResponse.getTime());
        } else if (object instanceof InitializeServerResponse initServerResponse){
            final boolean isSucceed = initServerResponse.isSucceed();
            for (IClientObserver observer: observers){
                observer.onInitializeServerResponseReceived(isSucceed, connection.getID());
            }
        } else if (object instanceof LoadMapResponse loadMapResponse) {
            final boolean isSucceed = loadMapResponse.isSucceed();
            final GameState gameState = loadMapResponse.gameState();
            for (IClientObserver observer : observers) {
                observer.onLoadMapResponseReceived(isSucceed, gameState);
            }
        } else if (object instanceof ChangeMapResponse){
            for (IClientObserver observer : observers){
                observer.onChangeMapRequestReceived();
            }
        } else if (object instanceof JoinSessionResponse response) {
            for (IClientObserver observer: observers) {
                observer.onJoinSessionResponseReceived(
                    response.isSucceed(),
                    response.heroGlobalID(),
                    response.gameState(),
                    response.initialPosition()
                );
            }
        } else if (object instanceof GameStateUpdateEvent gameStateUpdateEvent){
            for (IClientObserver observer: observers) {
                observer.onGameStateUpdateEventReceived(gameStateUpdateEvent.entities());
            }
        }
    }

    /**
     * Send instance over TCP.
     *
     * @param object To be sent instance.
     */
    @Override
    public void sendTCP(Object object) {
        client.sendTCP(object);
    }

    /**
     * Send instance over UDP.
     *
     * @param object To be sent instance.
     */
    @Override
    public void sendUDP(Object object) {
        client.sendUDP(object);
    }

    /**
     * Connect to a endpoint/device.
     *
     * @param address IP address of device to be connected to.
     * @param port Port to be connected to. Will be used as TCP port. UDP port will be TCP port + 1;
     * @return True, if connected successfully. False, otherwise.
     */
    @Override
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
    @Override
    public void disconnect() {
        client.close();
    }

    /**
     * @return True, if connected to an endpoint/server. False, otherwise.
     */
    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Add observer to implement customized actions.
     *
     * @param observer Observer reference to be added.
     */
    @Override
    public void addObserver(final IClientObserver observer) {
        observers.add(requireNonNull(observer));
    }

    /**
     * Remove observer.
     *
     * @param observer Observer reference to be removed.
     */
    @Override
    public void removeObserver(final IClientObserver observer) {
        observers.remove(requireNonNull(observer));
    }
}
