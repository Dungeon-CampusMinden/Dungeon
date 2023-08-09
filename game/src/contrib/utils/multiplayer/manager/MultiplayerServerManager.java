package contrib.utils.multiplayer.manager;

import static java.util.Objects.requireNonNull;

import contrib.utils.multiplayer.network.packages.GameState;
import contrib.utils.multiplayer.network.packages.Version;
import contrib.utils.multiplayer.network.packages.event.GameStateUpdateEvent;
import contrib.utils.multiplayer.network.packages.event.MovementEvent;
import contrib.utils.multiplayer.network.packages.event.OnAuthenticatedEvent;
import contrib.utils.multiplayer.network.packages.request.*;
import contrib.utils.multiplayer.network.packages.response.*;
import contrib.utils.multiplayer.network.server.IServer;
import contrib.utils.multiplayer.network.server.IServerObserver;
import contrib.utils.multiplayer.network.server.MultiplayerServer;

import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Inherits logic to host und manage multiplayer session. */
public class MultiplayerServerManager implements IServerObserver {
    private static final Version VERSION = new Version(0, 0, 0);
    private static final int DEFAULT_HOST_ID_NOT_SET = -1;
    private static final IServer DEFAULT_SERVER = new MultiplayerServer();
    /** Ticks for sending game state update cyclically. */
    private static final int DEFAULT_TICKS_FOR_SCHEDULER = 128;
    /** Duration for sending game state update cyclically. */
    private static final long DEFAULT_NANOS_PER_TICK_FOR_SCHEDULER =
            1000000000 / DEFAULT_TICKS_FOR_SCHEDULER;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    /** Used to hold global state based when acting as host. */
    private final GameState globalState = new GameState();
    /** Used to send/receive messages to/from other endpoints/clients. */
    private final IServer server;

    private final Set<Integer> authorizedClientIDs = new HashSet<>();
    private final IMultiplayerServerManagerObserver observer;
    /** From server assigned unique id. */
    /** Separate scheduler for sending game state update tick wise through separate thread. */
    private ScheduledExecutorService scheduler;
    /** Used to save id of host, to determine if own client is host. */
    private int connectionIdHostClient = DEFAULT_HOST_ID_NOT_SET;
    /** Used to notice when map set up to send game state update tick wise when acting as server. */
    private boolean isMapLoaded = false;
    /** Used to pretend initial position for joining clients when acting as client. */
    private Point initialHeroPosition = new Point(0, 0);

    /**
     * Create new server manager.
     *
     * <p>Will use default server {@link #DEFAULT_SERVER} to send and receive messages.
     *
     * @param observer Observer for custom event handling.
     */
    public MultiplayerServerManager(final IMultiplayerServerManagerObserver observer) {
        this(observer, DEFAULT_SERVER);
    }

    /**
     * Create new server manager.
     *
     * @param observer Observer for custom event handling.
     * @param server Server to send and receive messages over TCP/UDP.
     */
    public MultiplayerServerManager(
            final IMultiplayerServerManagerObserver observer, final IServer server) {
        this.server = requireNonNull(server);
        server.addObserver(this);
        this.observer = requireNonNull(observer);
    }

    @Override
    public void onClientConnected(final int clientID) {
        /* Forces client to authorize. */
        server.sendTCP(clientID, new AuthenticationRequest());
    }

    @Override
    public void onClientDisconnected(final int clientID) {
        if (isHost(clientID)) {
            if (scheduler != null) {
                scheduler.shutdown();
            }
            server.stopListening();
            clearState();
        } else {
            globalState.heroesByClientId().remove(clientID);
            authorizedClientIDs.remove(clientID);
        }
    }

    @Override
    public void onAuthenticationResponseReceived(
            final int clientID, final AuthenticationResponse authenticationResponse) {
        final Version clientVersion = authenticationResponse.clientVersion();
        final boolean clientAndServerVersionAreEqual = VERSION.compareTo(clientVersion) == 0;
        if (!clientAndServerVersionAreEqual) {
            server.disconnectClient(clientID);
            return;
        }
        server.sendTCP(clientID, new OnAuthenticatedEvent(clientID));
    }

    @Override
    public void onJoinSessionRequestReceived(
            final int clientID, final JoinSessionRequest joinSessionRequest) {
        if (!isMapLoaded || !joinSessionRequest.hero().fetch(PositionComponent.class).isPresent()) {
            JoinSessionResponse response =
                    new JoinSessionResponse(false, -1, new GameState(), initialHeroPosition);
            server.sendTCP(clientID, response);
            return;
        }
        PositionComponent pc = joinSessionRequest.hero().fetch(PositionComponent.class).get();
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
        /* Only host is allowed to load map. */
        if (connectionIdHostClient != DEFAULT_HOST_ID_NOT_SET
                && connectionIdHostClient != clientID) {
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
                                            .get(); /* is always present because of previous checks. */
                            pc.position(initialHeroPosition);
                        });
        server.sendToAllExceptTCP(clientID, new LoadMapResponse(true, globalState));
        /* Save id of host for further validation processes and checks. */
        connectionIdHostClient = clientID;
        isMapLoaded = true;
    }

    @Override
    public void onChangeMapRequestReceived(
            final int clientID, final ChangeMapRequest changeMapRequest) {
        server.sendTCP(connectionIdHostClient, new ChangeMapResponse());
    }

    @Override
    public void onMovementEventReceived(final int clientID, final MovementEvent movementEvent) {
        Optional<Entity> heroInGlobalState = Optional.empty();
        if (globalState.entities() != null) {
            heroInGlobalState =
                    globalState.heroesByClientId().values().stream()
                            .filter(x -> x.globalID() == movementEvent.entityGlobalID())
                            .findFirst();
        }

        if (heroInGlobalState.isPresent()) {
            determineNewPosition(
                    heroInGlobalState.get(), movementEvent.xVelocity(), movementEvent.yVelocity());
            setVelocity(
                    heroInGlobalState.get(), movementEvent.xVelocity(), movementEvent.yVelocity());
        } else {
            // check if client which want to update monster position is host, otherwise not allowed
            if (isHost(clientID)) {
                Optional<Entity> monsterInGlobalState =
                        globalState.entities().stream()
                                .filter(x -> x.globalID() == movementEvent.entityGlobalID())
                                .findFirst();

                if (monsterInGlobalState.isPresent()) {
                    determineNewPosition(
                            monsterInGlobalState.get(),
                            movementEvent.xVelocity(),
                            movementEvent.yVelocity());
                    setVelocity(
                            monsterInGlobalState.get(),
                            movementEvent.xVelocity(),
                            movementEvent.yVelocity());
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
     * <p>To handle whether session started successfully or not, check {@link
     * IMultiplayerClientManagerObserver}.
     *
     * <p>NOTE: After server started, level and hero position has to be set up.
     *
     * @param port Port through which other clients can join.
     * @throws IOException if port not accessible.
     */
    public boolean start(final int port) throws IOException {
        clearState();
        stop();
        boolean isSuccessed = false;
        try {
            server.startListening(port);
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
                            server.sendToAllTCP(new GameStateUpdateEvent(entities));
                        }
                    },
                    0,
                    DEFAULT_NANOS_PER_TICK_FOR_SCHEDULER,
                    TimeUnit.NANOSECONDS);
            isSuccessed = true;
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }

        return isSuccessed;
    }

    /** Stop running all internal processes and closes sessions and ports. */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        server.stopListening();
        clearState();
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

    private int determineNextGlobalID() {
        int highestExistingID = 0;
        if (globalState != null && globalState.entities() != null) {
            for (Entity entity : globalState.entities()) {
                if (entity.globalID() > highestExistingID) {
                    highestExistingID = entity.globalID();
                }
            }
            highestExistingID = highestExistingID + 1;
        }
        return highestExistingID;
    }

    private void determineNewPosition(
            final Entity entity, final float xVelocity, final float yVelocity) {
        requireNonNull(entity);
        Optional<PositionComponent> pc = entity.fetch(PositionComponent.class);

        if (pc.isPresent()) {
            final float xNew = pc.get().position().x + xVelocity;
            final float yNew = pc.get().position().y + yVelocity;
            final Point newPosition = new Point(xNew, yNew);
            if (globalState.level().tileAt(newPosition) != null
                    && globalState.level().tileAt(newPosition).isAccessible()) {
                pc.get().position(newPosition);
            }
        }
    }

    private void setVelocity(final Entity entity, final float xVelocity, final float yVelocity) {
        requireNonNull(entity);
        Optional<VelocityComponent> vc = entity.fetch(VelocityComponent.class);

        if (vc.isPresent()) {
            vc.get().currentXVelocity(xVelocity);
            vc.get().currentYVelocity(yVelocity);
        }
    }

    private void clearState() {
        globalState.clear();
        isMapLoaded = false;
    }
}
