package contrib.utils.multiplayer;

import com.badlogic.gdx.utils.Null;
import contrib.utils.multiplayer.client.IMultiplayerClient;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.event.MovementEvent;
import contrib.utils.multiplayer.server.IMultiplayerServer;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.ILevel;
import core.utils.Point;
import contrib.utils.multiplayer.client.IMultiplayerClientObserver;
import contrib.utils.multiplayer.client.MultiplayerClient;
import contrib.utils.multiplayer.packages.request.*;
import contrib.utils.multiplayer.server.MultiplayerServer;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Used to handle multiplayer sessions.
 */
public class MultiplayerManager implements IMultiplayerClientObserver {

    private static final Version VERSION = new Version(0, 0, 0);
    private static final IMultiplayerClient DEFAULT_CLIENT = new MultiplayerClient();
    private static final IMultiplayerServer DEFAULT_SERVER = new MultiplayerServer();
    private final IMultiplayerClient client;
    private final IMultiplayerServer server;
    private final IMultiplayer multiplayer;
    /* From server assigned unique player id. */
    private int playerId = 0;
    /* Global state of entities. Is updated on game actions, like clients joining session. */
    private Set<Entity> entities;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Create new instance.
     *
     * <p>Will use default client {@link MultiplayerClient} for communication.
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
        final IMultiplayer multiplayer,
        final IMultiplayerClient client,
        final IMultiplayerServer server) {
        this.multiplayer = requireNonNull(multiplayer);
        this.client = requireNonNull(client);
        this.server = requireNonNull(server);
        this.client.addObserver(this);
        this.entities = new HashSet<>();
    }

    @Override
    public void onInitializeServerResponseReceived(
        final boolean isSucceed,
        final int clientId) {

        if (isSucceed) {
            playerId = clientId;
        } else {
            playerId = 0;
        }
        multiplayer.onMultiplayerServerInitialized(isSucceed);
    }

    @Override
    public void onLoadMapResponseReceived(final boolean isSucceed, final GameState gameState) {
        if (!isSucceed) return;

        this.entities = gameState.entities();
        multiplayer.onMapLoad(gameState.level());
    }

    @Override
    public void onChangeMapRequest() {
        multiplayer.onChangeMapRequest();
    }

    @Override
    public void onJoinSessionResponseReceived(
        final boolean isSucceed,
        final int heroGlobalID,
        final GameState gameState,
        final Point initialHeroPosition) {
        playerId = 0;
        if (isSucceed) {
            this.entities = requireNonNull(gameState.entities());
            playerId = heroGlobalID;
            Game.hero().get().globalID(heroGlobalID);
            PositionComponent heroPositionComponent =
            (PositionComponent) Game.hero().get()
                .fetch(PositionComponent.class)
                .orElseThrow();
            heroPositionComponent.position(initialHeroPosition);

            try {
                Game.currentLevel(gameState.level());
            }
            catch (Exception ex) {
                logger.warning(String.format("Failed to set received level from server.\n%s", ex.getMessage()));
            }
        } else {
            logger.warning("Cannot join multiplayer session. Server responded unsuccessful.");
        }

        multiplayer.onMultiplayerSessionJoined(isSucceed);
    }

    @Override
    public void onUpdatePositionResponseReceived() {

    }

    @Override
    public void onGameStateUpdateEventReceived(Set<Entity> entities) {
        this.entities = requireNonNull(entities);
    }

    @Override
    public void onConnected(@Null final InetAddress address) {
    }

    @Override
    public void onDisconnected(@Null final InetAddress address) {
        clearSessionData();
        multiplayer.onMultiplayerSessionConnectionLost();
    }

    /**
     * Hosting a multiplayer session, which other players can join.
     *
     * <p>Asks server to listen to random port.
     * <p>To handle whether session started successfully or not, check {@link IMultiplayer}.
     * <p>NOTE: After server started, level and entities has to be configured.
     *
     * @throws IOException if currently now free port found on device to host session. (should never occur)
     */
    public void startSession() throws IOException {
        clearSessionData();
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
                isRandomPortAlreadyInUse = false;
            } catch (Exception e) {
                generatePortTriesCount++;
            }
        } while((generatePortTriesCount < generatePortTriesMaxCount) && isRandomPortAlreadyInUse);

        if (isRandomPortAlreadyInUse) {
            throw new IOException("No available port on device found");
        }

        client.connectToHost("127.0.0.1", serverPort);
        client.sendTCP(new InitializeServerRequest(VERSION));
    }

    /**
     * Used to init or change the multiplayer global state,
     * including the level and entities, that should be part of the game.
     *
     * @param level
     * @param currentEntities Entities that should be part of the level.
     * @param hero Own hero.
     */
    public void loadLevel(
        final ILevel level,
        final Set<Entity> currentEntities,
        final Entity hero){
        if (isHost()) {
            client.sendTCP(new LoadMapRequest(
                requireNonNull(level),
                requireNonNull(currentEntities),
                requireNonNull(hero)
            ));
        } else {
            requestNewLevel();
        }
    }

    /**
     * Used to request session to change the level.
     *
     * <p>Has to be used for scenarios where Not-Hosting-Client enters end tile of a level.
     * Then the Host-Client will be asked to change the level
     * (On server side, only Host-Clients are able to change level / set entities.)
     */
    public void requestNewLevel(){
        client.sendTCP(new ChangeMapRequest());
    }

    /**
     * Stops multiplayer session.
     *
     * <p>Global state will be cleared and all endpoints will be closed, so all player will be disconnected.
     */
    public void stopSession() {
        clearSessionData();
        stopEndpoints();
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
        clearSessionData();
        stopEndpoints();
        if (!client.connectToHost(address, port)) {
            throw new IOException("No host found - invalid address or port");
        }
        client.sendTCP(new JoinSessionRequest(Game.hero().get(), VERSION));
    }

    /**
     * Used to store movement state globally, so that position and movement animation
     * can be synchronized on each player device.
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
     * <p>Can be used to control request flows / game logic.
     *
     * @return True, if own device is host. False, otherwise.
     */
    public boolean isHost() { return playerId == 1; }

    /**
     * Gets global state of entities.
     *
     * @return Global state of entities.
     */
    public Set<Entity> entities() { return this.entities; }

    private void clearSessionData() {
        playerId = 0;
        entities.clear();
    }

    private void stopEndpoints() {
        client.disconnect();
        server.stopListening();
    }
}
