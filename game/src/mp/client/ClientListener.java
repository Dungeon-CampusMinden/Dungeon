package mp.client;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import mp.packages.request.LoadMapRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;

public class ClientListener extends Listener {

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
            final LoadMapResponse loadMapResponse = (LoadMapResponse)object;
            System.out.println("Map loaded: " + loadMapResponse.isLoaded());
        }
    }
}
