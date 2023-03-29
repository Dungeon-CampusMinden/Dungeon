package mp.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import mp.packages.request.PingRequest;
import mp.packages.response.PingResponse;

import java.io.IOException;

public class ClientClass {

    // TODO: Outsource config parameters
    private static final Integer connectionTimeout = 5000;
    private static final Integer serverPort = 25444;
    private static final String serverAddress = "127.0.0.1";

    public static void main(String[] args) {
        final Client client = new Client();

        client.start();

        try {
            client.connect(connectionTimeout, serverAddress, serverPort);
        } catch (
        IOException e) {
            e.printStackTrace();
        }

        client.addListener(new ClientListener());

        Kryo kryo = client.getKryo();
        // register all packages that should be able to be received and sent
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);

        PingRequest pingRequest = new PingRequest();
        client.sendTCP(pingRequest);

        while (true) {

        }
    }
}
