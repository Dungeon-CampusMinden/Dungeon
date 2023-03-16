package mp.client;

import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Client;
import mp.packages.NetworkSetup;
import mp.packages.request.DataChunk;

import java.io.IOException;
import java.util.Arrays;

public class MultiplayerClient {

    // TODO: Outsource config parameters
    private static final Integer writeBufferSize = Integer.MAX_VALUE / 2;
    private static final Integer objectBufferSize = Integer.MAX_VALUE / 2;
    private static final Integer connectionTimeout = 5000;
    private static final Integer serverPort = 25444;
    private static final String serverAddress = "127.0.0.1";
    private static final Client client = new Client(writeBufferSize, objectBufferSize);

    public MultiplayerClient() {
        client.addListener(new ClientListener());
        NetworkSetup.register(client);

        client.start();

        try {
            client.connect(connectionTimeout, serverAddress, serverPort);
        } catch (
            IOException e) {
            e.printStackTrace();
        }
    }

    public void send(Object object) {
        try {
            client.sendTCP(object);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendChunked(Object object) {
        try {
            Output output = new Output(1024, -1);

            client.getKryo().writeObject(output, object);
            output.flush();

            byte[] bytes = output.toBytes();
            output.close();
            byte[][] chunks = splitIntoChunks(bytes, 1024);

            for (int i = 0; i < chunks.length; i++) {
                byte[] chunk = chunks[i];
                boolean isLastChunk = (i == chunks.length - 1);
                System.out.println("Sending chunk");
                client.sendTCP(new DataChunk(chunk, isLastChunk));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private byte[][] splitIntoChunks(byte[] data, int chunkSize) {
        int numChunks = (int) Math.ceil((double) data.length / chunkSize);
        byte[][] chunks = new byte[numChunks][];
        for (int i = 0; i < numChunks; i++) {
            int startIndex = i * chunkSize;
            int endIndex = Math.min(startIndex + chunkSize, data.length);
            chunks[i] = Arrays.copyOfRange(data, startIndex, endIndex);
        }
        return chunks;
    }
}
