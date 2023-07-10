package contrib.utils.multiplayer.server;

import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.event.GameStateUpdateEvent;
import contrib.utils.multiplayer.packages.event.MovementEvent;
import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;
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


public class MultiplayerServer extends Listener implements IMultiplayerServer {
    public static final Version VERSION = new Version(0, 0, 0);
    public static final int DEFAULT_TCP_PORT = 25444;
    public static final int DEFAULT_UDP_PORT = DEFAULT_TCP_PORT + 1;
    // According to several tests, random generated level is the largest object to be sent
    // and can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final int MAX_OBJECT_SIZE_EXPECTED = 8000000;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = MAX_OBJECT_SIZE_EXPECTED;
    private static final int DEFAULT_OBJECT_BUFFER_SIZE = MAX_OBJECT_SIZE_EXPECTED;
    /* Ticks for sending game state update cyclically. */
    private static final int DEFAULT_TICKS = 128;
    /* Duration for sending game state update cyclically. */
    private static final long DEFAULT_NANOS_PER_TICK = 1000000000 / DEFAULT_TICKS;
    private final Server server = new Server(DEFAULT_WRITE_BUFFER_SIZE, DEFAULT_OBJECT_BUFFER_SIZE);
    /* Separate scheduler for sending game state update cyclically and on another thread. */
    private ScheduledExecutorService scheduler;
    private boolean isInitialized = false;
    private boolean isMapLoaded = false;
    // Used to hold current level and entities.
    private final GameState gameState = new GameState();
    // Used to pretend initial position for joining clients
    private Point initialHeroPosition = new Point(0, 0);
    private int connectionIdHostClient;

    /**
     * Creates a new Instance.
     */
    public MultiplayerServer() {
        server.addListener(this);
        NetworkSetup.registerCommunicationClasses(server);
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
        if (object instanceof PingRequest pingRequest) {
            processPingRequest(connection, pingRequest);
        } else if (object instanceof InitializeServerRequest initServerRequest){
            processInitializeServerRequest(connection, initServerRequest);
        } else if (object instanceof LoadMapRequest loadMapRequest) {
            processLoadMapRequest(connection, loadMapRequest);
        } else if (object instanceof ChangeMapRequest changeMapRequest){
            processChangeMapRequest(connection, changeMapRequest);
        } else if (object instanceof JoinSessionRequest joinSessionRequest) {
            processJoinSessionRequest(connection, joinSessionRequest);
        } else if (object instanceof MovementEvent movementEvent) {
            processMovementEventReceived(connection, movementEvent);
        }
    }

    /**
     * Starts listening for connections.
     *
     * @param port a preconfigured TCP port. UDP port will be TCP port + 1. If null, default ports are used.
     */
    public void startListening(@Null Integer port) throws IOException {
        server.bind(port != null ? port : DEFAULT_TCP_PORT, port != null ? port + 1 : DEFAULT_UDP_PORT);
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
                    server.sendToAllUDP(new GameStateUpdateEvent(entities));
                }
            }
        }, 0, DEFAULT_NANOS_PER_TICK, TimeUnit.NANOSECONDS );
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

    private void processInitializeServerRequest(final Connection connection, final InitializeServerRequest initServerRequest) {
        isInitialized = false;
        final Version clientVersion = initServerRequest.clientVersion();
        final boolean clientAndServerVersionAreEqual = VERSION.compareTo(clientVersion) == 0;
        if (clientAndServerVersionAreEqual) {
            isInitialized = true;
            connectionIdHostClient = connection.getID();
        }
        connection.sendTCP(new InitializeServerResponse(isInitialized));
    }

    private void processJoinSessionRequest(final Connection connection, final JoinSessionRequest joinSessionRequest) {
        boolean isClientValid = VERSION.compareTo(joinSessionRequest.clientVersion()) == 0;
        if (!isClientValid) {
            JoinSessionResponse response = new JoinSessionResponse(
                false,
                -1,
                new GameState(),
                initialHeroPosition
            );
            connection.sendTCP(response);
            return;
        }
        PositionComponent pc =
            joinSessionRequest.hero()
                .fetch(PositionComponent.class)
                .orElseThrow(
                    () -> MissingComponentException.build(joinSessionRequest.hero(), PositionComponent.class));
        pc.position(initialHeroPosition);
        final int entityGlobalID = determineNextGlobalID();
        joinSessionRequest.hero().globalID(entityGlobalID);
        gameState.heroesByClientId().put(connection.getID(), joinSessionRequest.hero());
        JoinSessionResponse response = new JoinSessionResponse(
            true,
            entityGlobalID,
            gameState,
            initialHeroPosition
        );
        connection.sendTCP(response);
    }

    private void processLoadMapRequest(final Connection connection, LoadMapRequest loadMapRequest) {
        isMapLoaded = false;
        final boolean isHost = connection.getID() == connectionIdHostClient;
        if (!isHost || !isInitialized) {
            server.sendToTCP(connection.getID(), new LoadMapResponse(false, new GameState()));
            return;
        }
        // extract hero from entities
        final Optional<Entity> heroInSendEntitiesSet =
            loadMapRequest.entities().stream()
                .filter(x -> x.localID() == loadMapRequest.hero().localID())
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
    }

    private void processChangeMapRequest(final Connection connection, final ChangeMapRequest changeMapRequest) {
        server.sendToTCP(connectionIdHostClient, new ChangeMapResponse());
    }

    private void processMovementEventReceived(final Connection connection, final MovementEvent positionRequest) {
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

            VelocityComponent vc =
                hero.get()
                    .fetch(VelocityComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(hero.get(), VelocityComponent.class));

            vc.currentXVelocity(positionRequest.xVelocity());
            vc.currentYVelocity(positionRequest.yVelocity());
        } else {
            // check if client which want to update monster position is host, otherwise not allowed
            if (connection.getID() == connectionIdHostClient) {
                Optional<Entity> monster = gameState.entities().stream()
                    .filter(x -> x.globalID() == positionRequest.entityGlobalID())
                    .findFirst();

                if (monster.isPresent()) {
                    PositionComponent pc =
                        monster.get()
                            .fetch(PositionComponent.class)
                            .orElseThrow(
                                () -> MissingComponentException.build(monster.get(), PositionComponent.class));
                    pc.position(positionRequest.position());

                    VelocityComponent vc =
                        monster.get()
                            .fetch(VelocityComponent.class)
                            .orElse(null);

                    if (vc != null) {
                        vc.currentXVelocity(positionRequest.xVelocity());
                        vc.currentYVelocity(positionRequest.yVelocity());
                    }
                }
            }
        }
    }

    private void processPingRequest(final Connection connection, final PingRequest pingRequest) {
        connection.sendTCP(new PingResponse());
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
