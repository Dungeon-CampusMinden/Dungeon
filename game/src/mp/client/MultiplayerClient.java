package mp.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import core.level.elements.ILevel;
import core.utils.Point;
import mp.packages.GameState;
import mp.packages.NetworkSetup;
import mp.packages.response.*;
import mp.packages.event.GameStateUpdateEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

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
        } else if (object instanceof LoadMapResponse initializeServerResponse) {
            final boolean isSucceed = initializeServerResponse.getIsSucceed();
            final ILevel level = initializeServerResponse.getLevel();
            final HashMap<Integer, Point> heroPositionByClientId = initializeServerResponse.getHeroPositionByClientId();
            for (IMultiplayerClientObserver observer : observers) {
                observer.onLoadMapResponseReceived(isSucceed, level, heroPositionByClientId);
            }
        } else if (object instanceof ChangeMapResponse){
            for (IMultiplayerClientObserver observer : observers){
                observer.onChangeMapRequest();
            }
        } else if (object instanceof JoinSessionResponse response) {
            for (IMultiplayerClientObserver observer: observers) {
                observer.onJoinSessionResponseReceived(
                    response.getIsSucceed(),
                    response.getLevel(),
                    response.getClientId(),
                    response.getHeroPositionByClientId()
                );
            }
        } else if (object instanceof GameStateUpdateEvent gameStateUpdateEvent){
            final GameState gameState = gameStateUpdateEvent.getGameState();
            for (IMultiplayerClientObserver observer: observers) {
                observer.onGameStateUpdateEventReceived(gameState);
            }
        } else if (object instanceof UpdateOwnPositionResponse) {
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
