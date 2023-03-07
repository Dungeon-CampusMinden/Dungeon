package mp.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import mp.packages.request.PingRequest;
import mp.packages.response.PingResponse;

import java.io.IOException;
public class ServerClass {

    public static void main(String[] args){

        Server server = new Server();

        server.start();

        try {
            server.bind(25444);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Kryo kryo = server.getKryo();
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
    }

}
