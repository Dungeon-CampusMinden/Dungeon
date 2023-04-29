package mp.server;

import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import level.elements.ILevel;
import mp.packages.NetworkSetup;
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.JoinSessionRequest;
import mp.packages.request.PingRequest;
import mp.packages.request.UpdateOwnPositionRequest;
import mp.packages.response.InitializeServerResponse;
import mp.packages.response.JoinSessionResponse;
import mp.packages.response.PingResponse;
import mp.packages.event.HeroPositionsChangedEvent;
import mp.packages.response.UpdateOwnPositionResponse;
import tools.Point;

import java.io.IOException;
import java.util.HashMap;

public class MultiplayerServer extends Listener {

    // TODO: Outsource config parameters
    public static final Integer DEFAULT_TCP_PORT = 25444;
    public static final Integer DEFAULT_UDP_PORT = DEFAULT_TCP_PORT + 1;
    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final Integer maxObjectSizeExpected = 8000000;
    private static final Integer writeBufferSize = maxObjectSizeExpected;
    private static final Integer objectBufferSize = maxObjectSizeExpected;
    private final Server server = new Server(writeBufferSize, objectBufferSize );
    private ILevel level;

    private HashMap<Integer, Point> playerPositions = new HashMap<Integer, Point>();

    public MultiplayerServer() {
        server.addListener(this);
        NetworkSetup.register(server);
    }

    @Override
    public void connected(Connection connection) {
//        System.out.println("Player " + connection.getID() + " connected with " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        playerPositions.remove(connection.getID());
        server.sendToAllTCP(new HeroPositionsChangedEvent(playerPositions));
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingRequest) {
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof InitializeServerRequest){
            level = ((InitializeServerRequest) object).getLevel();
            connection.sendTCP(new InitializeServerResponse(true));
        } else if (object instanceof JoinSessionRequest) {
            connection.sendTCP(new JoinSessionResponse(level, connection.getID(), playerPositions));
        } else if (object instanceof UpdateOwnPositionRequest) {
            UpdateOwnPositionRequest posReq = (UpdateOwnPositionRequest) object;
            playerPositions.put(posReq.getClientId(), posReq.getHeroPosition());
            connection.sendTCP(new UpdateOwnPositionResponse());

            /** For now: directly emit event to all clients, that position of one hero changed.
             * Later: Emit positions of all heros tickwise, to avoid high network use.
             * */
            server.sendToAllTCP(new HeroPositionsChangedEvent(playerPositions));
        }
    }

    /**
     * Starts listening for connections.
     *
     * @param port a preconfigured port. If null, default port is used.
     */
    public void startListening(@Null Integer port) throws IOException {
        server.bind(port != null ? port : DEFAULT_TCP_PORT);
        server.start();
    }

    /**
     * Closes ports and stops the server.
     */
    public void stop() {
        server.stop();
    }
}
