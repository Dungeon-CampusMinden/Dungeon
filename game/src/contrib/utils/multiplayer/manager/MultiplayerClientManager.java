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

public class MultiplayerClientManager implements IClientObserver {
    public static final int DEFAULT_CLIENT_ID_NOT_CONNECTED = -1;
    private static final Version VERSION = new Version(0, 0, 0);
    private static final IClient DEFAULT_CLIENT = new MultiplayerClient();
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final IMultiplayer multiplayer;
    /**
     * Used to hold global state when acting as client. Is updated on incoming messages from server.
     */
    private Set<Entity> entities;
    /** Used to send/receive messages to/from other endpoint/server. */
    private final IClient client;
    /** From server assigned unique id. */
    private int clientID = DEFAULT_CLIENT_ID_NOT_CONNECTED;

    public MultiplayerClientManager(final IMultiplayer multiplayer) {
        this(multiplayer, DEFAULT_CLIENT);
    }

    public MultiplayerClientManager(final IMultiplayer multiplayer, final IClient client) {
        this.multiplayer = multiplayer;
        this.client = requireNonNull(client);
        client.addObserver(this);
        entities = new HashSet<>();
    }

    @Override
    public void onConnectedToServer() {}

    @Override
    public void onDisconnectedFromServer() {
        clearState();
        multiplayer.onMultiplayerSessionConnectionLost();
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
        entities = isSucceed ? requireNonNull(gameState.entities()) : new HashSet<>();
        clientID = isSucceed ? heroGlobalID : DEFAULT_CLIENT_ID_NOT_CONNECTED;
        if (!isSucceed)
            logger.warning("Cannot join multiplayer session. Server responded unsuccessful.");

        multiplayer.onMultiplayerSessionJoined(
                isSucceed, heroGlobalID, gameState.level(), initialHeroPosition);
    }

    @Override
    public void onLoadMapResponseReceived(final boolean isSucceed, final GameState gameState) {
        if (!isSucceed) return;

        entities = gameState.entities();
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
    public void loadLevel(
            final ILevel level, final Set<Entity> currentEntities, final Entity hero) {
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
     * Gets global state of entities.
     *
     * @return (Copy) Global state of entities.
     */
    public Stream<Entity> entityStream() {
        return new ArrayList<>(entities).stream();
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
