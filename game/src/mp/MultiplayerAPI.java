package mp;

import com.badlogic.gdx.utils.Null;
import level.elements.ILevel;
import mp.client.IMultiplayerClientObserver;
import mp.client.MultiplayerClient;
import mp.packages.GameState;
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.JoinSessionRequest;
import mp.packages.request.UpdateOwnPositionRequest;
import mp.server.MultiplayerServer;
import tools.Point;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

/** Used to handle multiplayer sessions. */
public class MultiplayerAPI implements IMultiplayerClientObserver {

    private final MultiplayerClient multiplayerClient;
    private final MultiplayerServer multiplayerServer;
    private final IMultiplayer multiplayer;
    /* From server assigned unique client/player id. */
    private int playerId = 0;
    /* Current state of hero positions, identified by client/player id. */
    private HashMap<Integer, Point> heroPositionByPlayerId;

    public MultiplayerAPI(IMultiplayer multiplayer) {
        this.multiplayer = requireNonNull(multiplayer);
        this.multiplayerClient = new MultiplayerClient();
        this.multiplayerServer = new MultiplayerServer();
        this.multiplayerClient.addObserver(this);
        this.heroPositionByPlayerId = new HashMap<>();
    }

    @Override
    public void onInitializeServerResponseReceived(
        final boolean isSucceed,
        final int clientId,
        final Point initialHeroPosition) {
        if (heroPositionByPlayerId == null) {
            heroPositionByPlayerId = new HashMap<>();
        }

        if (isSucceed) {
            playerId = clientId;
            heroPositionByPlayerId.put(clientId, initialHeroPosition);
        } else {
            playerId = 0;
        }
        multiplayer.onMultiplayerSessionStarted(isSucceed);
    }

    @Override
    public void onJoinSessionResponseReceived(
        final boolean isSucceed,
        final ILevel level,
        final int clientId,
        final HashMap<Integer, Point> heroPositionByClientId) {
        playerId = 0;
        if (isSucceed) {
            requireNonNull(level);
            heroPositionByPlayerId = requireNonNull(heroPositionByClientId);
            playerId = clientId;
        }
        multiplayer.onMultiplayerSessionJoined(isSucceed, level);
    }

    @Override
    public void onUpdateOwnPositionResponseReceived() {

    }

    @Override
    public void onGameStateUpdateEventReceived(final GameState gameState) {
        requireNonNull(gameState);
        this.heroPositionByPlayerId = requireNonNull(gameState.getHeroPositionByClientId());
    }

    @Override
    public void onConnected(@Null final InetAddress address) {
        // For now no action needed when connected
    }

    @Override
    public void onDisconnected(@Null final InetAddress address) {
//        clearSessionData();
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
        if (ownHeroInitialPosition != null) {
            multiplayerClient.send(new InitializeServerRequest(level, ownHeroInitialPosition));
        } else {
            multiplayerClient.send(new InitializeServerRequest(level));
        }
    }

    /** */
    public void stopSession() {
        clearSessionData();
        stopEndpoints();
    }

    public HashMap<Integer, Point> getHeroPositionByPlayerId() {
        return heroPositionByPlayerId;
    }

    public int getOwnPlayerId() { return playerId; }

    /** */
    public void joinSession(final String address, final int port) throws IOException {
        requireNonNull(address);
        clearSessionData();
        stopEndpoints();
        if (!multiplayerClient.connectToHost(address, port)) {
            throw new IOException("No host found - invalid address or port");
        }
        multiplayerClient.send(new JoinSessionRequest());
    }

    /** */
    public void updateOwnPosition(final Point newPosition) {
        multiplayerClient.send(new UpdateOwnPositionRequest(playerId, newPosition));
    }

    public boolean isConnectedToSession() {
        return playerId != 0;
    }

    private void clearSessionData() {
        playerId = 0;
        heroPositionByPlayerId.clear();
    }

    private void stopEndpoints() {
        multiplayerClient.disconnect();
        multiplayerServer.stop();
    }
}
