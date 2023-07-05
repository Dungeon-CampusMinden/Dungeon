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

public class MultiplayerClient extends Listener {

    // TODO: Outsource config parameters
    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final Integer maxObjectSizeExpected = 8000000;
    private static final Integer writeBufferSize = maxObjectSizeExpected;
    private static final Integer objectBufferSize = maxObjectSizeExpected;
    private static final Integer connectionTimeout = 5000;
    private static final Client client = new Client(writeBufferSize, objectBufferSize);
    private final ArrayList<IMultiplayerClientObserver> observers = new ArrayList<>();

    public MultiplayerClient() {
        client.addListener(this);
        NetworkSetup.register(client);
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
        } else if (object instanceof InitServerResponse initServerResponse){
            final boolean isSucceed = initServerResponse.isSucceed();
            for (IMultiplayerClientObserver observer: observers){
                observer.onInitServerResponseReceived(isSucceed, connection.getID());
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
        } else if (object instanceof UpdatePositionResponse) {
            for (IMultiplayerClientObserver observer: observers){
                observer.onUpdateOwnPositionResponseReceived();
            }
        }
    }

    public void send(Object object) {
        client.sendTCP(object);
    }

    public boolean connectToHost(String address, int port) {
        try {
            client.start();
            client.connect(connectionTimeout, address, port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        client.close();
        client.stop();
    }

    public void addObserver(IMultiplayerClientObserver observer) {
        observers.add(observer);
    }
}
