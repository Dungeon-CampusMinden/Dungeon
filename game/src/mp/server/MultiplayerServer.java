package mp.server;

import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import level.elements.ILevel;
import mp.GameState;
import mp.packages.NetworkSetup;
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.JoinSessionRequest;
import mp.packages.request.PingRequest;
import mp.packages.request.UpdateOwnPositionRequest;
import mp.packages.response.InitializeServerResponse;
import mp.packages.response.JoinSessionResponse;
import mp.packages.response.PingResponse;
import mp.packages.event.GameStateUpdateEvent;
import mp.packages.response.UpdateOwnPositionResponse;
import tools.Point;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final GameState gameState = new GameState();
    // Used to pretend initial position for joining clients
    private Point initialHeroPosition = new Point(0, 0);

    private static final int ticks = 128;
    private static final long nanosPerTick = 1000000000 / ticks;
    private ScheduledExecutorService scheduler;

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
        gameState.getHeroPositionByClientId().remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingRequest) {
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof InitializeServerRequest initializeServerRequest){
            level = initializeServerRequest.getLevel();
            final Point initialHeroPosition = initializeServerRequest.getHeroInitialPosition();
            if (initialHeroPosition != null) {
                this.initialHeroPosition = initialHeroPosition;
            }
            gameState.getHeroPositionByClientId().put(connection.getID(), this.initialHeroPosition);
            connection.sendTCP(new InitializeServerResponse(true, this.initialHeroPosition));
        } else if (object instanceof JoinSessionRequest) {
            final int clientId = connection.getID();
            gameState.getHeroPositionByClientId().put(clientId, this.initialHeroPosition);
            JoinSessionResponse response =
                new JoinSessionResponse(true, level, clientId, gameState.getHeroPositionByClientId());
            connection.sendTCP(response);
        } else if (object instanceof UpdateOwnPositionRequest positionRequest) {
            gameState.getHeroPositionByClientId().put(positionRequest.getClientId(), positionRequest.getHeroPosition());
            //TODO: Look if in use, delete if not necessary
            connection.sendTCP(new UpdateOwnPositionResponse());
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

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                server.sendToAllTCP(new GameStateUpdateEvent(gameState));
            }
        }, 0, nanosPerTick, TimeUnit.NANOSECONDS );
    }

    /**
     * Closes ports and stops the server.
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (server != null) {
            server.close();
            server.stop();
        }
    }
}
