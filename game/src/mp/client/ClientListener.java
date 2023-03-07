package mp.client;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import mp.packages.request.PingRequest;
import mp.packages.response.PingResponse;

public class ClientListener extends Listener {

    @Override
    public void connected(Connection connection) {
        System.out.println("[Client] connected!");
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("[Client] disconnected!");
    }

    @Override
    public void received(Connection connection, Object object) {
        System.out.println("[Client] Message received!");

        if (object instanceof PingResponse) {
            final PingResponse pingResponse = (PingResponse)object;
            System.out.println("[Client] Time: " + pingResponse.getTime());
        }
    }
}
