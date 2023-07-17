package contrib.utils.multiplayer.manager;

import static java.util.Objects.requireNonNull;

import contrib.utils.multiplayer.network.client.IClient;
import contrib.utils.multiplayer.network.client.IClientObserver;
import contrib.utils.multiplayer.network.client.MultiplayerClient;
import contrib.utils.multiplayer.network.packages.GameState;
import contrib.utils.multiplayer.network.packages.Version;
import contrib.utils.multiplayer.network.packages.event.MovementEvent;
import contrib.utils.multiplayer.network.packages.request.AuthenticationResponse;
import contrib.utils.multiplayer.network.packages.request.ChangeMapRequest;
import contrib.utils.multiplayer.network.packages.request.JoinSessionRequest;
import contrib.utils.multiplayer.network.packages.request.LoadMapRequest;

import core.Entity;
import core.components.PlayerComponent;
import core.level.elements.ILevel;
import core.utils.Point;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Inherits logic to join multiplayer sessions.
 *
 * <p>Saves current state of entities and level received from server.
 *
 * <p>To join sessions, see {@link #joinSession(String, int, Entity)}
 *
 * <p>To inform server about entity movement, see {@link #sendMovementUpdate(int, Point, float,
 * float)}
 *
 * <p>To force loading a map as host client, see {@link #loadMap(ILevel, Set, Entity)}
 *
 * <p>To request changing the level as not-host client, see {@link #requestNewLevel()}
 */
public class MultiplayerClientManager implements IClientObserver {
    public static final int DEFAULT_CLIENT_ID_NOT_CONNECTED = -1;
    private static final Version VERSION = new Version(0, 0, 0);
    private static final IClient DEFAULT_CLIENT = new MultiplayerClient();
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final IMultiplayerClientManagerObserver observer;
    /** Used to hold global state of entities. Is updated on incoming messages from server. */
    private Set<Entity> entities;
    /** Used to hold global level. Is updated on incoming messages from server. */
    private ILevel level;
    /** Used to send/receive messages to/from other endpoint/server. */
    private final IClient client;
    /** From server assigned unique id. */
    private int clientID = DEFAULT_CLIENT_ID_NOT_CONNECTED;

    /**
     * Create new client manager.
     *
     * <p>Will use default client {@link #DEFAULT_CLIENT} to send and receive messages.
     *
     * @param observer Observer for custom event handling.
     */
    public MultiplayerClientManager(final IMultiplayerClientManagerObserver observer) {
        this(observer, DEFAULT_CLIENT);
    }

    /**
     * Create new client manager.
     *
     * @param observer Observer for custom event handling.
     * @param client Client that would be used to send and receive messages.
     */
    public MultiplayerClientManager(
            final IMultiplayerClientManagerObserver observer, final IClient client) {
        this.observer = observer;
        this.client = requireNonNull(client);
        client.addObserver(this);
        entities = new HashSet<>();
    }

    @Override
    public void onConnectedToServer() {}

    @Override
    public void onDisconnectedFromServer() {
        clearState();
        observer.onMultiplayerSessionConnectionLost();
    }

    @Override
    public void onAuthenticationRequestReceived() {
        client.sendTCP(new AuthenticationResponse(VERSION));
    }

    @Override
    public void onAuthenticatedEventReceived(final int clientID) {
        this.clientID = clientID;
    }

    @Override
    public void onJoinSessionResponseReceived(
            final boolean isSucceed,
            final int heroGlobalID,
            final GameState gameState,
            final Point initialHeroPosition) {
        level = isSucceed ? requireNonNull(gameState.level()) : null;
        entities = isSucceed ? requireNonNull(gameState.entities()) : new HashSet<>();
        clientID = isSucceed ? heroGlobalID : DEFAULT_CLIENT_ID_NOT_CONNECTED;
        if (!isSucceed)
            logger.warning("Cannot join multiplayer session. Server responded unsuccessful.");

        observer.onMultiplayerSessionJoined(
                isSucceed, heroGlobalID, gameState.level(), initialHeroPosition);
    }

    @Override
    public void onLoadMapResponseReceived(final boolean isSucceed, final GameState gameState) {
        level = gameState.level();
        entities = gameState.entities();
        observer.onMapLoad(gameState.level());
    }

    @Override
    public void onChangeMapRequestReceived() {
        observer.onChangeMapRequest();
    }

    @Override
    public void onGameStateUpdateEventReceived(final Set<Entity> entities) {
        this.entities = entities;
    }

    /**
     * Join hosted session.
     *
     * @param address Address of the device that is hosting the session.
     * @param port Port of the device to access the session.
     * @param playable Own playable entity.
     * @throws IOException if the address or port is not accessible.
     * @throws NoSuchElementException if {@link PlayerComponent} is not present for playable.
     */
    public void joinSession(final String address, final int port, final Entity playable)
            throws IOException, NoSuchElementException {
        requireNonNull(address);
        requireNonNull(playable);
        playable.fetch(PlayerComponent.class).orElseThrow();
        clearState();
        disconnect();
        if (!client.connectToHost(address, port)) {
            throw new IOException("No host found - invalid address or port");
        }
        client.sendTCP(new JoinSessionRequest(playable));
    }

    public boolean connect(final String address, final int port) {
        return client.connectToHost(address, port);
    }

    public void disconnect() {
        client.disconnect();
    }

    /**
     * Used to init or change the multiplayer global state, including the level and entities, that
     * should be part of the game.
     *
     * @param level Level that should be used for session.
     * @param currentEntities Entities that should be part of the level.
     * @param hero Own hero.
     */
    public void loadMap(final ILevel level, final Set<Entity> currentEntities, final Entity hero) {
        client.sendTCP(
                new LoadMapRequest(
                        requireNonNull(level),
                        requireNonNull(currentEntities),
                        requireNonNull(hero)));
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
     * @param newPosition New/current local position of the entity, after movement action. Needed
     *     for validation process.
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
     * @return (Copy) Global state of entities.
     */
    public Stream<Entity> entityStream() {
        return new ArrayList<>(entities).stream();
    }

    /**
     * @return Global active level.
     */
    public ILevel level() {
        return level;
    }

    /**
     * @return From server assigned client ID, if connected. otherwise default {@link
     *     #DEFAULT_CLIENT_ID_NOT_CONNECTED}
     */
    public int clientID() {
        return clientID;
    }

    private void clearState() {
        clientID = DEFAULT_CLIENT_ID_NOT_CONNECTED;
        entities.clear();
    }
}
