package mp.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import level.elements.ILevel;
import mp.packages.request.DataChunk;
import mp.packages.request.LoadMapRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;

public class ServerListener extends Listener {
    @Override
    public void connected(Connection connection) {
        System.out.println("Player " + connection.getID() + " connected with " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("Player " + connection.getID() + " disconnected");
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof byte[]) {
            byte[] chunk = (byte[])object;
        } else if (object instanceof DataChunk) {
            System.out.println("Data Chunk received");
            // Todo: concatenate chunks to objects that was splitted
        } else if (object instanceof PingRequest) {
            System.out.println("Pingrequest received");
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof LoadMapRequest){
            System.out.println("LoadMapRequest received");
            final LoadMapRequest loadMapRequest = (LoadMapRequest) object;
            ILevel currentLevel = loadMapRequest.getLevel();
            final LoadMapResponse loadMapResponse = new LoadMapResponse();
            connection.sendTCP(loadMapResponse);
        }
    }
}
