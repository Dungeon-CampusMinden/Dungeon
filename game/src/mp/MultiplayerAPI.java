package mp;

import level.elements.ILevel;
import mp.client.IMultiplayerClientObserver;
import mp.client.MultiplayerClient;
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.JoinSessionRequest;
import mp.packages.request.UpdateOwnPositionRequest;
import mp.server.MultiplayerServer;
import tools.Point;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

public class MultiplayerAPI implements IMultiplayerClientObserver {

    private final MultiplayerClient multiplayerClient;
    private final MultiplayerServer multiplayerServer;
    private final IMultiplayer multiplayer;
    /** From server assigned unique client/player id. */
    private int playerId = 0;
    /** Current state of hero positions, identified by client id. */
    private HashMap<Integer, Point> heroPositionByPlayerId;

    public MultiplayerAPI(IMultiplayer multiplayer) {
        this.multiplayer = requireNonNull(multiplayer);
        this.multiplayerClient = new MultiplayerClient();
        this.multiplayerServer = new MultiplayerServer();
        this.multiplayerClient.addObserver(this);
        this.heroPositionByPlayerId = new HashMap<>();
    }

    @Override
    public void onInitializeServerResponseReceived(final boolean isSucceed, final int clientId) {
        playerId = isSucceed ? clientId : 0;
        multiplayer.onMultiplayerSessionStarted(isSucceed);
    }

    @Override
    public void onJoinSessionResponseReceived(
        final ILevel level,
        final int clientId,
        final HashMap<Integer, Point> heroPositionByClientId) {
        requireNonNull(level);
        requireNonNull(heroPositionByClientId);
        playerId = level != null ? clientId : 0;
        multiplayer.onMultiplayerSessionJoined(level);
    }

    @Override
    public void onHeroPositionsChangedEventReceived(final HashMap<Integer, Point> heroPositionByClientId) {
        requireNonNull(heroPositionByClientId);
        this.heroPositionByPlayerId = heroPositionByClientId;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        playerId = 0;
        heroPositionByPlayerId.clear();
    }

    /** */
    public void startSession(final ILevel level) throws IOException {
        requireNonNull(level);
        // Check whether which random port is not already in use and listen to this on serverside
        boolean isRandomPortAlreadyInUse = false;
        int serverPort;
        do {
            // Create random 5 digit port
            serverPort = ThreadLocalRandom.current().nextInt(10000, 65535 + 1);
            try {
                multiplayerServer.startListening(serverPort);
            } catch (Exception e) {
                isRandomPortAlreadyInUse = true;
            }
        } while(isRandomPortAlreadyInUse);
        multiplayerClient.connectToHost("127.0.0.1", serverPort);
        multiplayerClient.send(new InitializeServerRequest(level));
    }

    /** */
    public void stopSession() {
        this.multiplayerServer.stop();
    }

    /** */
    public void joinSession(final String address, final Integer port) throws IOException {
        requireNonNull(address);
        requireNonNull(port);
        multiplayerClient.connectToHost(address, port);
        multiplayerClient.send(new JoinSessionRequest());
    }

    /** */
    public void updateOwnPosition(final Point newPosition) {
        multiplayerClient.send(new UpdateOwnPositionRequest(playerId, newPosition));
    }

    public HashMap<Integer, Point> getHeroPositionByPlayerId() {
        return heroPositionByPlayerId;
    }

    /**  */
    public HashMap<Integer, Point> getHeroPositionByPlayerIdExceptOwn() {
        final HashMap<Integer, Point> heroPositionByPlayerIdExceptOwn = new HashMap<>();
        heroPositionByPlayerId.forEach((Integer playerId, Point position) -> {
            if (playerId != this.playerId) {
                heroPositionByPlayerIdExceptOwn.put(playerId, position);
            }
        });
        return heroPositionByPlayerIdExceptOwn;
    }

    public boolean isConnectedToSession() {
        return playerId != 0;
    }
}
