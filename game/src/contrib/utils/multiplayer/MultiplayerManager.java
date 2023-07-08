package contrib.utils.multiplayer;

import com.badlogic.gdx.utils.Null;
import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.Version;
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

import static java.util.Objects.requireNonNull;

/** Used to handle multiplayer sessions. */
public class MultiplayerManager implements IMultiplayerClientObserver {

    private final MultiplayerClient multiplayerClient;
    private final MultiplayerServer multiplayerServer;
    private final IMultiplayer multiplayer;
    /* From server assigned unique player id. */
    private int playerId = 0;
    private Set<Entity> entities;

    public MultiplayerManager(IMultiplayer multiplayer) {
        this.multiplayer = requireNonNull(multiplayer);
        this.multiplayerClient = new MultiplayerClient();
        this.multiplayerServer = new MultiplayerServer();
        this.multiplayerClient.addObserver(this);
        this.entities = new HashSet<>();
    }

    @Override
    public void onInitServerResponseReceived(
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
//            if (!Game.hero().get().fetch(MultiplayerComponent.class).isPresent()) {
//                new MultiplayerComponent(Game.hero().get());
//            }
            PositionComponent heroPositionComponent =
            (PositionComponent) Game.hero().get()
                .fetch(PositionComponent.class)
                .orElseThrow();
            heroPositionComponent.position(initialHeroPosition);
        }
        multiplayer.onMultiplayerSessionJoined(isSucceed, gameState.level());
    }

    @Override
    public void onUpdateOwnPositionResponseReceived() {

    }

    @Override
    public void onGameStateUpdateEventReceived(Set<Entity> entities) {
        this.entities = requireNonNull(entities);
    }

    @Override
    public void onConnected(@Null final InetAddress address) {
        // For now no action needed when connected
    }

    @Override
    public void onDisconnected(@Null final InetAddress address) {
        clearSessionData();
        multiplayer.onMultiplayerSessionLost();
    }

    /** */
    public void startSession(final ILevel level, @Null final Point ownHeroInitialPosition) throws IOException {
        requireNonNull(level);
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
                multiplayerServer.startListening(serverPort);
                isRandomPortAlreadyInUse = false;
            } catch (Exception e) {
                generatePortTriesCount++;
            }
        } while((generatePortTriesCount < generatePortTriesMaxCount) && isRandomPortAlreadyInUse);


        if (isRandomPortAlreadyInUse) {
            throw new IOException("No available port on device found");
        }

        multiplayerClient.connectToHost("127.0.0.1", serverPort);
        // TODO replace with configured version
        final Version clientVersion = new Version(0, 0, 0);
        multiplayerClient.send(new InitServerRequest(clientVersion));
//        if (ownHeroInitialPosition != null) {
//            multiplayerClient.send(new LoadMapRequest(level, ownHeroInitialPosition));
//        } else {
//            multiplayerClient.send(new LoadMapRequest(level));
//        }
    }

    public void changeLevel(final ILevel level, final Set<Entity> currentEntities, final Entity hero){
//        currentEntities.forEach(entity -> {
//            if (!entity.fetch(MultiplayerComponent.class).isPresent()) {
//                new MultiplayerComponent(entity);
//            }
//        });
        multiplayerClient.send(new LoadMapRequest(level, currentEntities, hero));
    }

    public void requestNewLevel(){
        multiplayerClient.send(new ChangeMapRequest());
    }

    /** */
    public void stopSession() {
        clearSessionData();
        stopEndpoints();
    }

    /** */
    public void joinSession(final String address, final int port) throws IOException {
        requireNonNull(address);
        clearSessionData();
        stopEndpoints();
        if (!multiplayerClient.connectToHost(address, port)) {
            throw new IOException("No host found - invalid address or port");
        }
        multiplayerClient.send(new JoinSessionRequest(Game.hero().get()));
    }

    /** */
    public void sendPositionUpdate(final int entityGlobalID, final Point newPosition, final float xVelocity, final float yVelocity) {
        multiplayerClient.send(new UpdatePositionRequest(entityGlobalID, newPosition, xVelocity, yVelocity));
    }

    public boolean isConnectedToSession() {
        return playerId != 0;
    }

    public boolean isHost() { return playerId == 1; }

    public Set<Entity> entities() { return this.entities; }

    private void clearSessionData() {
        playerId = 0;
        entities.clear();
    }

    private void stopEndpoints() {
        multiplayerClient.disconnect();
        multiplayerServer.stopListening();
    }
}
