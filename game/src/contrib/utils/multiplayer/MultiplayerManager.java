package contrib.utils.multiplayer;

import static java.util.Objects.requireNonNull;

import contrib.utils.multiplayer.client.IClient;
import contrib.utils.multiplayer.client.IClientObserver;
import contrib.utils.multiplayer.client.MultiplayerClient;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.event.GameStateUpdateEvent;
import contrib.utils.multiplayer.packages.event.MovementEvent;
import contrib.utils.multiplayer.packages.request.*;
import contrib.utils.multiplayer.packages.response.*;
import contrib.utils.multiplayer.server.IServer;
import contrib.utils.multiplayer.server.IServerObserver;
import contrib.utils.multiplayer.server.MultiplayerServer;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.ILevel;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** Used to handle multiplayer sessions. */
public class MultiplayerManager implements IClientObserver, IServerObserver {

    private static final Version VERSION = new Version(0, 0, 0);
    private static final IClient DEFAULT_CLIENT = new MultiplayerClient();
    private static final IServer DEFAULT_SERVER = new MultiplayerServer();
    /* Ticks for sending game state update cyclically. */
    private static final int DEFAULT_TICKS_FOR_SCHEDULER = 128;
    /* Duration for sending game state update cyclically. */
    private static final long DEFAULT_NANOS_PER_TICK_FOR_SCHEDULER =
            1000000000 / DEFAULT_TICKS_FOR_SCHEDULER;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    // Used to hold current level and entities.
    private final GameState globalState = new GameState();
    private final IClient client;
    private final IServer server;
    private final IMultiplayer multiplayer;
    /* From server assigned unique player id. */
    private int clientID = 0;
    /* Global state of entities. Is updated on game actions, like clients joining session. */
    private Set<Entity> entities;
    /* Separate scheduler for sending game state update cyclically and on another thread. */
    private ScheduledExecutorService scheduler;
    private boolean isInitialized = false;
    private int connectionIdHostClient;
    private boolean isMapLoaded = false;
    // Used to pretend initial position for joining clients
    private Point initialHeroPosition = new Point(0, 0);

    /**
     * Create new instance.
     *
     * <p>Will use default client {@link MultiplayerClient} for communication.
     *
     * <p>Will use default server {@link MultiplayerServer} for communication.
     *
     * @param multiplayer {@link IMultiplayer} to customize actions based on internal events.
     */
    public MultiplayerManager(final IMultiplayer multiplayer) {
        this(multiplayer, DEFAULT_CLIENT, DEFAULT_SERVER);
    }

    /**
     * Create a new instance.
     *
     * @param multiplayer Multiplayer to customize actions based on internal events.
     * @param client Client that should be used for communication.
     * @param server Server that should be used for communication.
     */
    public MultiplayerManager(
            final IMultiplayer multiplayer, final IClient client, final IServer server) {
        this.multiplayer = requireNonNull(multiplayer);
        this.client = requireNonNull(client);
        this.server = requireNonNull(server);
        this.client.addObserver(this);
        this.server.addObserver(this);
        this.entities = new HashSet<>();
    }

    @Override
    public void onConnectedToServer() {}

    @Override
    public void onDisconnectedFromServer() {
        clearLocalSessionData();
        multiplayer.onMultiplayerSessionConnectionLost();
    }

    @Override
    public void onInitializeServerResponseReceived(final boolean isSucceed, final int clientId) {
        if (isSucceed) {
            clientID = clientId;
        } else {
            clientID = 0;
        }
        multiplayer.onMultiplayerServerInitialized(isSucceed);
    }

    @Override
    public void onJoinSessionResponseReceived(
            final boolean isSucceed,
            final int heroGlobalID,
            final GameState gameState,
            final Point initialHeroPosition) {
        clientID = 0;
        if (isSucceed) {
            this.entities = requireNonNull(gameState.entities());
            clientID = heroGlobalID;
            Game.hero().get().globalID(heroGlobalID);
            PositionComponent heroPositionComponent =
                    Game.hero().get().fetch(PositionComponent.class).orElseThrow();
            heroPositionComponent.position(initialHeroPosition);

            try {
                Game.currentLevel(gameState.level());
            } catch (Exception ex) {
                logger.warning(
                        String.format(
                                "Failed to set received level from server.\n%s", ex.getMessage()));
            }
        } else {
            logger.warning("Cannot join multiplayer session. Server responded unsuccessful.");
        }

        multiplayer.onMultiplayerSessionJoined(isSucceed);
    }

    @Override
    public void onLoadMapResponseReceived(final boolean isSucceed, final GameState gameState) {
        if (!isSucceed) return;

        this.entities = gameState.entities();
        multiplayer.onMapLoad(gameState.level());
    }

    @Override
    public void onChangeMapRequestReceived() {
        multiplayer.onChangeMapRequest();
    }

    @Override
    public void onGameStateUpdateEventReceived(final Set<Entity> entities) {
        this.entities = entities;
    }

    @Override
    public void onClientConnected(final int clientID) {}

    @Override
    public void onClientDisconnected(final int clientID) {
        if (isHost(clientID)) {
            if (scheduler != null) {
                scheduler.shutdown();
            }
            server.stopListening();
            clearGlobalState();
        } else {
            globalState.heroesByClientId().remove(clientID);
        }
    }

    @Override
    public void onInitializeRequestReceived(
            final int clientID, final InitializeServerRequest initServerRequest) {
        isInitialized = false;
        final Version clientVersion = initServerRequest.clientVersion();
        final boolean clientAndServerVersionAreEqual = VERSION.compareTo(clientVersion) == 0;
        if (clientAndServerVersionAreEqual) {
            isInitialized = true;
            connectionIdHostClient = clientID;
        }
        server.sendTCP(clientID, new InitializeServerResponse(isInitialized));
    }

    @Override
    public void onJoinSessionRequestReceived(
            final int clientID, final JoinSessionRequest joinSessionRequest) {
        boolean isClientValid = VERSION.compareTo(joinSessionRequest.clientVersion()) == 0;
        if (!isClientValid) {
            JoinSessionResponse response =
                    new JoinSessionResponse(false, -1, new GameState(), initialHeroPosition);
            server.sendTCP(clientID, response);
            return;
        }
        PositionComponent pc =
                joinSessionRequest
                        .hero()
                        .fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                joinSessionRequest.hero(),
                                                PositionComponent.class));
        pc.position(initialHeroPosition);
        final int entityGlobalID = determineNextGlobalID();
        joinSessionRequest.hero().globalID(entityGlobalID);
        globalState.heroesByClientId().put(clientID, joinSessionRequest.hero());
        JoinSessionResponse response =
                new JoinSessionResponse(true, entityGlobalID, globalState, initialHeroPosition);
        server.sendTCP(clientID, response);
    }

    @Override
    public void onLoadMapRequestReceived(final int clientID, LoadMapRequest loadMapRequest) {
        isMapLoaded = false;
        final boolean isHost = clientID == connectionIdHostClient;
        if (!isHost || !isInitialized) {
            server.sendTCP(clientID, new LoadMapResponse(false, new GameState()));
            return;
        }
        // extract hero from entities
        final Optional<Entity> heroInSendEntitiesSet =
                loadMapRequest.entities().stream()
                        .filter(x -> x.localID() == loadMapRequest.hero().localID())
                        .findFirst();
        heroInSendEntitiesSet.ifPresent(entity -> loadMapRequest.entities().remove(entity));
        globalState.level(loadMapRequest.level());
        globalState.entities(loadMapRequest.entities());
        globalState.heroesByClientId().put(clientID, loadMapRequest.hero());
        // Save new initial position for joining clients/heroes
        initialHeroPosition = globalState.level().startTile().position();
        globalState
                .heroesByClientId()
                .values()
                .forEach(
                        entity -> {
                            PositionComponent pc =
                                    entity.fetch(PositionComponent.class)
                                            .orElseThrow(
                                                    () ->
                                                            MissingComponentException.build(
                                                                    entity,
                                                                    PositionComponent.class));
                            pc.position(initialHeroPosition);
                        });
        server.sendToAllExceptTCP(clientID, new LoadMapResponse(true, globalState));
        isMapLoaded = true;
    }

    @Override
    public void onChangeMapRequestReceived(
            final int clientID, final ChangeMapRequest changeMapRequest) {
        server.sendTCP(connectionIdHostClient, new ChangeMapResponse());
    }

    @Override
    public void onMovementEventReceived(final int clientID, final MovementEvent positionRequest) {
        Optional<Entity> hero =
                globalState.heroesByClientId().values().stream()
                        .filter(x -> x.globalID() == positionRequest.entityGlobalID())
                        .findFirst();

        if (hero.isPresent()) {
            PositionComponent pc =
                    hero.get()
                            .fetch(PositionComponent.class)
                            .orElseThrow(
                                    () ->
                                            MissingComponentException.build(
                                                    hero.get(), PositionComponent.class));
            pc.position(positionRequest.position());

            VelocityComponent vc =
                    hero.get()
                            .fetch(VelocityComponent.class)
                            .orElseThrow(
                                    () ->
                                            MissingComponentException.build(
                                                    hero.get(), VelocityComponent.class));

            vc.currentXVelocity(positionRequest.xVelocity());
            vc.currentYVelocity(positionRequest.yVelocity());
        } else {
            // check if client which want to update monster position is host, otherwise not allowed
            if (isHost(clientID)) {
                Optional<Entity> monster =
                        globalState.entities().stream()
                                .filter(x -> x.globalID() == positionRequest.entityGlobalID())
                                .findFirst();

                if (monster.isPresent()) {
                    PositionComponent pc =
                            monster.get()
                                    .fetch(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            monster.get(),
                                                            PositionComponent.class));
                    pc.position(positionRequest.position());

                    VelocityComponent vc =
                            monster.get().fetch(VelocityComponent.class).orElse(null);

                    if (vc != null) {
                        vc.currentXVelocity(positionRequest.xVelocity());
                        vc.currentYVelocity(positionRequest.yVelocity());
                    }
                }
            }
        }
    }

    @Override
    public void onPingRequestReceived(final int clientID, final PingRequest pingRequest) {
        server.sendTCP(clientID, new PingResponse());
    }

    /**
     * Hosting a multiplayer session, which other players can join.
     *
     * <p>Asks server to listen to random port.
     *
     * <p>To handle whether session started successfully or not, check {@link IMultiplayer}.
     *
     * <p>NOTE: After server started, level and entities has to be configured.
     *
     * @throws IOException if currently now free port found on device to host session. (should never
     *     occur)
     */
    public void startSession() throws IOException {
        clearLocalSessionData();
        stopEndpoints();
        // Check whether which random port is not already in use and listen to this on serverside
        // it's unlikely that no port is free but to not run into infinite loop, limit tries.
        int generatePortTriesMaxCount = 20;
        int generatePortTriesCount = 0;
        boolean isRandomPortAlreadyInUse = true;
        int serverPort;
        do {
            // Create random 5 digit port
            serverPort = ThreadLocalRandom.current().nextInt(10000, 65535 + 1);
            try {
                server.startListening(serverPort);
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(
                        () -> {
                            if (isMapLoaded) {
                                // Combine hero entities and monster/item entities
                                final Set<Entity> entities = new HashSet<>(globalState.entities());
                                globalState
                                        .heroesByClientId()
                                        .values()
                                        .forEach(
                                                entity -> {
                                                    entities.add(entity);
                                                });
                                server.sendToAllUDP(new GameStateUpdateEvent(entities));
                            }
                        },
                        0,
                        DEFAULT_NANOS_PER_TICK_FOR_SCHEDULER,
                        TimeUnit.NANOSECONDS);
                isRandomPortAlreadyInUse = false;
            } catch (Exception e) {
                generatePortTriesCount++;
            }
        } while ((generatePortTriesCount < generatePortTriesMaxCount) && isRandomPortAlreadyInUse);

        if (isRandomPortAlreadyInUse) {
            throw new IOException("No available port on device found");
        }

        client.connectToHost("127.0.0.1", serverPort);
        client.sendTCP(new InitializeServerRequest(VERSION));
    }

    /**
     * Join hosted session.
     *
     * @param address Address of the device that is hosting the session.
     * @param port Port of the device to access the session.
     * @throws IOException if the address or port is not accessible.
     */
    public void joinSession(final String address, final int port) throws IOException {
        requireNonNull(address);
        clearLocalSessionData();
        stopEndpoints();
        if (!client.connectToHost(address, port)) {
            throw new IOException("No host found - invalid address or port");
        }
        client.sendTCP(new JoinSessionRequest(Game.hero().get(), VERSION));
    }

    /**
     * Stops multiplayer session.
     *
     * <p>Global state will be cleared and all endpoints will be closed, so all player will be
     * disconnected.
     */
    public void stopSession() {
        clearLocalSessionData();
        stopEndpoints();
    }

    /**
     * Used to init or change the multiplayer global state, including the level and entities, that
     * should be part of the game.
     *
     * @param level Level that should be used for session.
     * @param currentEntities Entities that should be part of the level.
     * @param hero Own hero.
     */
    public void loadLevel(
            final ILevel level, final Set<Entity> currentEntities, final Entity hero) {
        if (isHost()) {
            client.sendTCP(
                    new LoadMapRequest(
                            requireNonNull(level),
                            requireNonNull(currentEntities),
                            requireNonNull(hero)));
        } else {
            requestNewLevel();
        }
    }

    /**
     * Used to request session to change the level.
     *
     * <p>Has to be used for scenarios where Not-Hosting-Client enters end tile of a level. Then the
     * Host-Client will be asked to change the level (On server side, only Host-Clients are able to
     * change level / set entities.)
     */
    public void requestNewLevel() {
        client.sendTCP(new ChangeMapRequest());
    }

    /**
     * Used to store movement state globally, so that position and movement animation can be
     * synchronized on each player device.
     *
     * @param entityGlobalID Global ID of entity that has been moved.
     * @param newPosition New/current local position of the entity, after movement action.
     * @param xVelocity X velocity of movement action.
     * @param yVelocity Y velocity of movement action.
     */
    public void sendMovementUpdate(
            final int entityGlobalID,
            final Point newPosition,
            final float xVelocity,
            final float yVelocity) {
        client.sendUDP(new MovementEvent(entityGlobalID, newPosition, xVelocity, yVelocity));
    }

    /**
     * Check whether local state is connected to multiplayer session or not.
     *
     * @return True, if connected to multiplayer session. False, otherwise.
     */
    public boolean isConnectedToSession() {
        return client.isConnected();
    }

    /**
     * Check whether own device is host of multiplayer session or not.
     *
     * <p>Can be used to control request flows / game logic.
     *
     * @return True, if own device is host. False, otherwise.
     */
    public boolean isHost() {
        return isHost(this.clientID);
    }

    /**
     * Check whether given client is host of multiplayer session or not.
     *
     * <p>Can be used to control request flows / game logic.
     *
     * @return True, if host. False, otherwise.
     */
    public boolean isHost(final int clientID) {
        return clientID == connectionIdHostClient;
    }

    /**
     * Gets global state of entities.
     *
     * @return Global state of entities.
     */
    public Stream<Entity> entityStream() {
        return this.entities.stream();
    }

    private void stopEndpoints() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        client.disconnect();
        server.stopListening();
    }

    private int determineNextGlobalID() {
        int highestExistingID = 0;
        for (Entity entity : globalState.entities()) {
            if (entity.globalID() > highestExistingID) {
                highestExistingID = entity.globalID();
            }
        }
        return highestExistingID + 1;
    }

    private void clearGlobalState() {
        globalState.clear();
        isInitialized = false;
        isMapLoaded = false;
    }

    private void clearLocalSessionData() {
        clientID = 0;
        entities.clear();
    }
}
