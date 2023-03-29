package mp.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import level.elements.ILevel;
import mp.packages.NetworkSetup;
import mp.packages.request.LoadMapRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;

import java.io.*;

public class MultiplayerClient extends Listener {

    // TODO: Outsource config parameters
    private static final Integer writeBufferSize = 4096;
    private static final Integer objectBufferSize = Integer.MAX_VALUE / 2;
    private static final Integer connectionTimeout = 5000;
    private static final Integer serverPort = 25444;
    private static final String serverAddress = "127.0.0.1";
    private static final Client client = new Client(writeBufferSize, objectBufferSize);

    public ILevel currentLevel;

    public MultiplayerClient() {
        client.addListener(this);
        NetworkSetup.register(client);

        client.start();

        try {
            client.connect(connectionTimeout, serverAddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(Connection connection) {
        System.out.println("Connected to server!");
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("Disconnected from server!");
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingResponse) {
            final PingResponse pingResponse = (PingResponse)object;
            System.out.println("Ping response received. Time: " + pingResponse.getTime());
            connection.sendTCP(new LoadMapRequest());
        } else if (object instanceof LoadMapResponse){
            currentLevel = ((LoadMapResponse)object).getLevel();
        }
    }

    public void send(Object object) {
        client.sendTCP(object);
    }
}
