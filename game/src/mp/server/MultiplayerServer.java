package mp.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import level.elements.ILevel;
import mp.packages.NetworkSetup;
import mp.packages.request.DataChunk;
import mp.packages.request.LoadMapRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;

import java.io.IOException;

public class MultiplayerServer {

    // TODO: Outsource config parameters
    private static final Integer writeBufferSize = Integer.MAX_VALUE / 2;
    private static final Integer objectBufferSize = Integer.MAX_VALUE / 2;
    private static final Integer port = 25444;
    private static final Server server = new Server(writeBufferSize, objectBufferSize );


    private static ILevel currentLevel;

    public static void main(String[] args){
        server.addListener(new ServerListener());
        NetworkSetup.register(server);

        try {
            server.bind(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
