package mp.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import level.elements.ILevel;
import mp.packages.request.LoadMapRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;

import java.io.IOException;
public class ServerProgram extends Listener {

    private static Server server;

    // TODO: Outsource config parameters
    private static final Integer port = 25444;

    private static ILevel currentLevel;

    public static void main(String[] args){

        server = new Server();
        server.start();

        try {
            server.bind(port);
        } catch (IOException e) {

            e.printStackTrace();
        }

        server.addListener(new ServerProgram());

        // register all packages that should be able to be received and sent
        Kryo kryo = server.getKryo();
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(LoadMapRequest.class);
        kryo.register(LoadMapResponse.class);
    }

    @Override
    public void connected(Connection connection) {
        //System.out.println("[Server] Player " + connection.getID() + " connected with " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        //System.out.println("[Server] Player " + connection.getID() + " disconnected");
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingRequest) {
            System.out.println("[Server] Pingreq from " + connection.getRemoteAddressTCP());
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof LoadMapRequest){
            System.out.println("[Server] LoadMapReq from" + connection.getRemoteAddressTCP());
            final LoadMapRequest loadMapRequest = (LoadMapRequest) object;
            this.currentLevel = LoadMapRequest.getCurrentLevel();
            final LoadMapResponse loadMapResponse = new LoadMapResponse();
            connection.sendTCP(loadMapResponse);
        }
    }

}
