package mp.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import mp.packages.request.PingRequest;
import mp.packages.response.PingResponse;

import java.io.IOException;
public class ServerClass {

    // TODO: Outsource config parameters
    private static final Integer port = 25444;

    public static void main(String[] args){

        final Server server = new Server();

        server.start();

        try {
            server.bind(port);
        } catch (IOException e) {

            e.printStackTrace();
        }

        server.addListener(new ServerListener());

        Kryo kryo = server.getKryo();
        // register all packages that should be able to be received and sent
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
    }

}
