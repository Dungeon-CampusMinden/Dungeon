package contrib.utils.multiplayer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import contrib.utils.multiplayer.client.IClient;
import contrib.utils.multiplayer.packages.event.MovementEvent;
import contrib.utils.multiplayer.packages.request.ChangeMapRequest;
import contrib.utils.multiplayer.packages.request.JoinSessionRequest;
import contrib.utils.multiplayer.packages.request.LoadMapRequest;
import contrib.utils.multiplayer.server.IServer;

import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.utils.Point;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class MultiplayerManagerTest {
    @Mock private IMultiplayer multiplayer;
    @Mock private IClient client;
    @Mock private IServer server;

    private MultiplayerManager multiplayerManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        multiplayerManager = new MultiplayerManager(multiplayer, client, server);
    }

    @Test
    public void testOnConnectedToServer() {
        multiplayerManager.onConnectedToServer();
        // Add your assertions or verifications here
    }

    @Test
    public void testOnDisconnectedFromServer() {
        multiplayerManager.onDisconnectedFromServer();
        assertFalse(multiplayerManager.isHost());
        assertFalse(multiplayerManager.isConnectedToSession());
        assertEquals(multiplayerManager.entityStream().count(), 0);

    }

    // Write similar test methods for other methods in MultiplayerManager class

    @Test
    public void testJoinSession_SuccessfulConnection() throws IOException {
        String address = "127.0.0.1";
        int port = 1234;

        when(client.connectToHost(eq(address), eq(port))).thenReturn(true);

        multiplayerManager.joinSession(address, port, Game.hero().get());

        verify(client).connectToHost(eq(address), eq(port));
        verify(client).sendTCP(any(JoinSessionRequest.class));
    }

    @Test(expected = IOException.class)
    public void testJoinSession_FailedConnection() throws IOException {
        String address = "127.0.0.1";
        int port = 1234;

        when(client.connectToHost(eq(address), eq(port))).thenReturn(false);
        multiplayerManager.joinSession(address, port, Game.hero().get());
    }

    @Test
    public void testStopSession() {
        multiplayerManager.stopSession();

        verify(client).disconnect();
        verify(server).stopListening();

        assertFalse(multiplayerManager.isConnectedToSession());
    }

    @Test
    public void testLoadLevel_AsHost() {
        ILevel level = mock(ILevel.class);
        Set<Entity> entities = new HashSet<>();
        Entity hero = mock(Entity.class);

        multiplayerManager.onInitializeServerResponseReceived(true, 1); // Simulate being a host
        multiplayerManager.loadLevel(level, entities, hero);

        verify(client).sendTCP(any(LoadMapRequest.class));
    }

    @Test
    public void testLoadLevel_NotHost() {
        ILevel level = mock(ILevel.class);
        Set<Entity> entities = new HashSet<>();
        Entity hero = mock(Entity.class);

        multiplayerManager.loadLevel(level, entities, hero);

        verify(client).sendTCP(any(ChangeMapRequest.class));
    }

    @Test
    public void testRequestNewLevel() {
        multiplayerManager.requestNewLevel();

        verify(client).sendTCP(any(ChangeMapRequest.class));
    }

    @Test
    public void testSendMovementUpdate() {
        int entityGlobalID = 1;
        Point newPosition = new Point(0, 0);
        float xVelocity = 0.5f;
        float yVelocity = 0.5f;

        multiplayerManager.sendMovementUpdate(entityGlobalID, newPosition, xVelocity, yVelocity);

        verify(client).sendUDP(any(MovementEvent.class));
    }

    @Test
    public void testIsConnectedToSession() {
        assertFalse(multiplayerManager.isConnectedToSession());
        when(client.isConnected()).thenReturn(true);
        assertTrue(multiplayerManager.isConnectedToSession());
    }

    @Test
    public void testIsHost() {
        multiplayerManager.onInitializeServerResponseReceived(true, 1);
        assertTrue(multiplayerManager.isHost());
        assertFalse(multiplayerManager.isHost(2));
    }

    @Test
    public void testOnGameUpdateEventReceived() throws IOException {
        Entity entity1 = new Entity();
        Entity entity2 = new Entity();
        HashSet<Entity> entities = new HashSet<>(Set.of(entity1, entity2));
        multiplayerManager.onGameStateUpdateEventReceived(entities);

        assertEquals(entities.stream().count(), multiplayerManager.entityStream().count());
    }
}
