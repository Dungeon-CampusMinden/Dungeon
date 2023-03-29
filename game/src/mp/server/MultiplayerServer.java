package mp.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import level.elements.ILevel;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import mp.packages.NetworkSetup;
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.InitializeServerResponse;
import mp.packages.response.PingResponse;
import mp.player.PlayersAPI;

import java.io.IOException;

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
            System.out.println("Ping request received");
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof InitializeServerRequest){
            System.out.println("Initialize request received");
            level = ((InitializeServerRequest) object).getLevel();
            connection.sendTCP(new InitializeServerResponse(true));
        }
    }

    public static void main(String[] args){
        new MultiplayerServer();
    }
}
