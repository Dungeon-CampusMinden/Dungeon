package mp.server;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import level.elements.ILevel;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import mp.packages.NetworkSetup;
import mp.packages.request.LoadMapRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;
import mp.player.PlayersAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerServer extends Listener {

    // TODO: Outsource config parameters
    private static final Integer writeBufferSize = Integer.MAX_VALUE / 2;
    private static final Integer objectBufferSize = 4096;
    private static final Integer port = 25444;
    private final Server server = new Server(writeBufferSize, objectBufferSize );

    private ILevel level;
    private final PlayersAPI playersAPI = new PlayersAPI();

    public MultiplayerServer() {
        server.addListener(this);
        NetworkSetup.register(server);

        level = new WallGenerator(new RandomWalkGenerator()).getLevel();

        try {
            server.bind(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(Connection connection) {
//        System.out.println("Player " + connection.getID() + " connected with " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
//        System.out.println("Player " + connection.getID() + " disconnected");
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingRequest) {
            System.out.println("Pingrequest received");
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof LoadMapRequest){
            System.out.println("LoadMapRequest received");
            final LoadMapResponse loadMapResponse = new LoadMapResponse(level);
            connection.sendTCP(loadMapResponse);
        }
    }

    public static void main(String[] args){
        new MultiplayerServer();
    }
}
