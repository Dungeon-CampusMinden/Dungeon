package mp.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import level.elements.ILevel;
import mp.packages.NetworkSetup;
import mp.packages.request.InitializeServerRequest;
import mp.packages.response.InitializeServerResponse;
import mp.packages.response.JoinSessionResponse;
import mp.packages.response.PingResponse;

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
        client.start();
    }

    @Override
    public void connected(Connection connection) {
//        System.out.println("Connected to server!");
    }

    @Override
    public void disconnected(Connection connection) {
//        System.out.println("Disconnected from server!");
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingResponse) {
            final PingResponse pingResponse = (PingResponse)object;
            System.out.println("Ping response received. Time: " + pingResponse.getTime());
        } else if (object instanceof InitializeServerResponse){
            boolean isSucceed = ((InitializeServerResponse)object).isSucceed();
            for (IMultiplayerClientObserver observer: observers) {
                observer.onServerInitializedReceived(isSucceed, connection.getID());
            }
        } else if (object instanceof JoinSessionResponse) {
            ILevel level = ((JoinSessionResponse)object).getLevel();
            for (IMultiplayerClientObserver observer: observers) {
                observer.onSessionJoined(level, connection.getID());
            }
        }
    }

    public void send(Object object) {
        client.sendTCP(object);
    }

    public boolean connectToHost(String address, Integer port) {
        try {
            client.connect(connectionTimeout, address, port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addObserver(IMultiplayerClientObserver observer) {
        observers.add(observer);
    }
}
