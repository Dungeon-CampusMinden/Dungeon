package contrib.utils.multiplayer;

import contrib.utils.multiplayer.client.MultiplayerClient;
import contrib.utils.multiplayer.server.MultiplayerServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class MultiplayerManagerTest {
//    private IMultiplayer multiplayer;
//    private MultiplayerManager multiplayerManager;
//    private MultiplayerClient multiplayerClient;
//    private MultiplayerServer multiplayerServer;
//
//    @Before
//    public void setUp() {
//        multiplayer = mock(IMultiplayer.class);
//        multiplayerClient = mock(MultiplayerClient.class);
//        multiplayerServer = mock(MultiplayerServer.class);
//
//        multiplayerManager = new MultiplayerManager(multiplayer);
//        multiplayerManager.multiplayerClient = multiplayerClient;
//        multiplayerManager.multiplayerServer = multiplayerServer;
//    }
//
//    @Test
//    public void testStartSession() throws IOException {
//        int serverPort = 12345;
//
//        // Mock the behavior of multiplayerServer.startListening()
//        doNothing().when(multiplayerServer).startListening(serverPort);
//
//        // Mock the behavior of multiplayerClient.connectToHost()
//        when(multiplayerClient.connectToHost("127.0.0.1", serverPort)).thenReturn(true);
//
//        // Mock the behavior of multiplayerClient.sendTCP()
//        doNothing().when(multiplayerClient).sendTCP(any(InitializeServerRequest.class));
//
//        multiplayerManager.startSession();
//
//        // Verify that multiplayerServer.startListening() was called with the correct port
//        verify(multiplayerServer).startListening(serverPort);
//
//        // Verify that multiplayerClient.connectToHost() was called with the correct address and port
//        verify(multiplayerClient).connectToHost("127.0.0.1", serverPort);
//
//        // Verify that multiplayerClient.sendTCP() was called with the correct request
//        verify(multiplayerClient).sendTCP(any(InitializeServerRequest.class));
//    }
//
//    @Test
//    public void testLoadLevel_AsHost() {
//        ILevel level = mock(ILevel.class);
//        Set<Entity> entities = new HashSet<>();
//        Entity hero = mock(Entity.class);
//
//        // Mock the behavior of multiplayerClient.sendTCP()
//        doNothing().when(multiplayerClient).sendTCP(any(LoadMapRequest.class));
//
//        multiplayerManager.loadLevel(level, entities, hero);
//
//        // Verify that multiplayerClient.sendTCP() was called with the correct request
//        verify(multiplayerClient).sendTCP(
//            argThat(request -> request instanceof LoadMapRequest
//                && ((LoadMapRequest) request).getLevel() == level
//                && ((LoadMapRequest) request).getCurrentEntities() == entities
//                && ((LoadMapRequest) request).getHero() == hero
//            )
//        );
//    }
}
