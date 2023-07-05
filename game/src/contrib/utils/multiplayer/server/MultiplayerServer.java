package contrib.utils.multiplayer.server;

import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.event.GameStateUpdateEvent;
import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.NetworkSetup;
import contrib.utils.multiplayer.packages.request.*;
import contrib.utils.multiplayer.packages.response.*;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiplayerServer extends Listener {

    // TODO: Outsource config parameters
    public static final Version version = new Version(0, 0, 0);
    public static final Integer DEFAULT_TCP_PORT = 25444;
    public static final Integer DEFAULT_UDP_PORT = DEFAULT_TCP_PORT + 1;
    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final Integer maxObjectSizeExpected = 8000000;
    private static final Integer writeBufferSize = maxObjectSizeExpected;
    private static final Integer objectBufferSize = maxObjectSizeExpected;
    private final Server server = new Server(writeBufferSize, objectBufferSize );
   // private ILevel level;
    private final GameState gameState = new GameState();
    // Used to pretend initial position for joining clients
    private Point initialHeroPosition = new Point(0, 0);

    private static final int ticks = 128;
    private static final long nanosPerTick = 1000000000 / ticks;
    private ScheduledExecutorService scheduler;
    private boolean isInitialized = false;
    private boolean isLoaded = false;

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
//        gameState.heroesByClientId().remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingRequest) {
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof InitServerRequest initServerRequest){
            isInitialized = false;
            final Version clientVersion = initServerRequest.clientVersion();
            if (version.compareTo(clientVersion) == 0) {
                isInitialized = true;
            }
            connection.sendTCP(new InitServerResponse(isInitialized));
        } else if (object instanceof LoadMapRequest loadMapRequest) {
            isLoaded = false;
            final boolean isHost = connection.getID() == 1;
            if (!isHost || !isInitialized) {
                // TODO unallow for not-hosts
            }
            gameState.level(loadMapRequest.level());
            gameState.entities(loadMapRequest.entities());
//            gameState.entities().forEach((e) -> {
//                PositionComponent pc =
//                    e.fetch(PositionComponent.class)
//                        .orElseThrow(
//                            () -> MissingComponentException.build(e, PositionComponent.class));
//                pc.position(gameState.level().randomTilePoint());
//            });
//            server.sendToAllTCP(new LoadMapResponse(true, gameState));
            server.sendToAllExceptTCP(connection.getID(), new LoadMapResponse(true, gameState));
            isLoaded = true;
        } else if (object instanceof ChangeMapRequest){
            // TODO just allow for host
            server.sendToTCP(1, new ChangeMapResponse());
        } else if (object instanceof JoinSessionRequest joinSessionRequest) {
            PositionComponent pc =
                joinSessionRequest.hero()
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(joinSessionRequest.hero(), PositionComponent.class));
            pc.position(gameState.level().startTile().position());
            final int entityGlobalID = determineNextGlobalID();
            joinSessionRequest.hero().globalID(entityGlobalID);
//            gameState.heroesByClientId().put(clientId, joinSessionRequest.hero());
            gameState.entities().add(joinSessionRequest.hero());
            JoinSessionResponse response = new JoinSessionResponse(true, entityGlobalID, gameState, initialHeroPosition);
            connection.sendTCP(response);
        } else if (object instanceof UpdatePositionRequest positionRequest) {
            Entity hero = gameState.entities().stream()
                .filter(x -> x.globalID() == positionRequest.entityGlobalID())
                .findFirst()
                .orElse(null);
            if (hero != null) {
                PositionComponent pc =
                    hero.fetch(PositionComponent.class)
                        .orElseThrow(
                            () -> MissingComponentException.build(hero, PositionComponent.class));
                pc.position(positionRequest.position());
            }
            //TODO: Look if in use, delete if not necessary (rename to event)
            // TODO send success so that other endpoint can recognize if received (???)
            connection.sendTCP(new UpdatePositionResponse());
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
                if (isLoaded) {
                    server.sendToAllTCP(new GameStateUpdateEvent(gameState.heroesByClientId(), gameState.entities()));
                }
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

    private int determineNextGlobalID() {
        int highestExistingID = 0;
        for (Entity entity : gameState.entities()) {
            if (entity.globalID() > highestExistingID) {
                highestExistingID = entity.globalID();
            }
        }
        return highestExistingID + 1;
    }
}
