package mp.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import mp.packages.request.PingRequest;
import mp.packages.response.PingResponse;

public class ServerListener extends Listener {

    @Override
    public void connected(Connection connection) {
        System.out.println("[Server] connected!");
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("[Server] disconnected!");
    }

    @Override
    public void received(Connection connection, Object object) {
        System.out.println("[Server] Message received!");

        if (object instanceof PingRequest) {
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        }
    }
}
