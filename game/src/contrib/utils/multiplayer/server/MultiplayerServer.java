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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiplayerServer extends Listener {

    // TODO: Outsource config parameters
    // Host is already the first client
    private int connectionIdHostClient;
    public static final Version version = new Version(0, 0, 0);
    public static final Integer DEFAULT_TCP_PORT = 25444;
    public static final Integer DEFAULT_UDP_PORT = DEFAULT_TCP_PORT + 1;
    // According to several tests, random generated level can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final Integer maxObjectSizeExpected = 8000000;
    private static final Integer writeBufferSize = maxObjectSizeExpected;
    private static final Integer objectBufferSize = maxObjectSizeExpected;
    private final Server server = new Server(writeBufferSize, objectBufferSize );
    private final GameState gameState = new GameState();
    // Used to pretend initial position for joining clients
    private Point initialHeroPosition = new Point(0, 0);
    private static final int ticks = 128;
    private static final long nanosPerTick = 1000000000 / ticks;
    private ScheduledExecutorService scheduler;
    private boolean isInitialized = false;
    private boolean isMapLoaded = false;

    public MultiplayerServer() {
        server.addListener(this);
        NetworkSetup.register(server);
    }

    @Override
    public void connected(Connection connection) {
    }

    @Override
    public void disconnected(Connection connection) {
        final boolean isHost = connection.getID() == connectionIdHostClient;
        if (isHost) {
            clearSessionData();
            stopListening();
        } else {
            gameState.heroesByClientId().remove(connection.getID());
        }
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof PingRequest) {
            final PingResponse pingResponse = new PingResponse();
            connection.sendTCP(pingResponse);
        } else if (object instanceof InitServerRequest initServerRequest){
            isInitialized = false;
            final Version clientVersion = initServerRequest.clientVersion();
            final boolean clientAndServerVersionAreEqual = version.compareTo(clientVersion) == 0;
            if (clientAndServerVersionAreEqual) {
                isInitialized = true;
                connectionIdHostClient = connection.getID();
            }
            connection.sendTCP(new InitServerResponse(isInitialized));
        } else if (object instanceof LoadMapRequest loadMapRequest) {
            isMapLoaded = false;
            final boolean isHost = connection.getID() == connectionIdHostClient;
            if (!isHost || !isInitialized) {
                server.sendToTCP(connection.getID(), new LoadMapResponse(false, new GameState()));
                return;
            }
            // extract hero from entities
            final Optional<Entity> heroInSendEntitiesSet =
                loadMapRequest.entities().stream()
                    .filter(x -> x.localeID() == loadMapRequest.hero().localeID())
                    .findFirst();
            if (heroInSendEntitiesSet.isPresent()) {
                loadMapRequest.entities().remove(heroInSendEntitiesSet.get());
            }
            gameState.level(loadMapRequest.level());
            gameState.entities(loadMapRequest.entities());
            gameState.heroesByClientId().put(connection.getID(), loadMapRequest.hero());
            // Save new initial position for joining clients/heroes
            initialHeroPosition = gameState.level().startTile().position();
            gameState.heroesByClientId().values().forEach(entity -> {
                PositionComponent pc =
                    entity
                        .fetch(PositionComponent.class)
                        .orElseThrow(
                            () -> MissingComponentException.build(entity, PositionComponent.class));
                pc.position(initialHeroPosition);
            });
            server.sendToAllExceptTCP(connection.getID(), new LoadMapResponse(true, gameState));
            isMapLoaded = true;
        } else if (object instanceof ChangeMapRequest){
            server.sendToTCP(connectionIdHostClient, new ChangeMapResponse());
        } else if (object instanceof JoinSessionRequest joinSessionRequest) {
            PositionComponent pc =
                joinSessionRequest.hero()
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(joinSessionRequest.hero(), PositionComponent.class));
            pc.position(initialHeroPosition);
            final int entityGlobalID = determineNextGlobalID();
            joinSessionRequest.hero().globalID(entityGlobalID);
            gameState.heroesByClientId().put(connection.getID(), joinSessionRequest.hero());
            JoinSessionResponse response = new JoinSessionResponse(true, entityGlobalID, gameState, initialHeroPosition);
            connection.sendTCP(response);
        } else if (object instanceof UpdatePositionRequest positionRequest) {
            boolean success = false;
            Optional<Entity> hero = gameState.heroesByClientId().values().stream()
                .filter(x -> x.globalID() == positionRequest.entityGlobalID())
                .findFirst();

            if (hero.isPresent()) {
                PositionComponent pc =
                    hero.get()
                        .fetch(PositionComponent.class)
                        .orElseThrow(
                            () -> MissingComponentException.build(hero.get(), PositionComponent.class));
                pc.position(positionRequest.position());
                success = true;
            } else {
                // check if client which want to update monster position is host, otherwise not allowed
                if (connection.getID() == connectionIdHostClient) {
                    Optional<Entity> monster = gameState.entities().stream()
                        .filter(x -> x.globalID() == positionRequest.entityGlobalID())
                        .findFirst();
                    PositionComponent pc =
                        monster.get()
                            .fetch(PositionComponent.class)
                            .orElseThrow(
                                () -> MissingComponentException.build(monster.get(), PositionComponent.class));
                    pc.position(positionRequest.position());
                    success = true;
                }
            }
            // TODO: Look if in use, delete if not necessary (rename to event)
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
                if (isMapLoaded) {
                    // Combine hero entities and monster/item entities
                    final Set<Entity> entities = new HashSet<>(gameState.entities());
                    gameState.heroesByClientId().values().forEach(entity -> {
                        entities.add(entity);
                    });
                    server.sendToAllTCP(new GameStateUpdateEvent(entities));
                }
            }
        }, 0, nanosPerTick, TimeUnit.NANOSECONDS );
    }

    /**
     * Closes session and ports.
     */
    public void stopListening() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (server != null) {
            server.close();
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

    private void clearSessionData() {
        gameState.clear();
        isInitialized = false;
        isMapLoaded = false;
    }
}
