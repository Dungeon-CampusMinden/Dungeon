package contrib.utils.multiplayer;

import static org.mockito.Mockito.mock;

import contrib.utils.multiplayer.client.IClient;
import contrib.utils.multiplayer.server.IServer;

import org.junit.Before;

public class MultiplayerManagerTest {
    private IMultiplayer multiplayer;
    private MultiplayerManager multiplayerManager;
    private IClient client;
    private IServer server;

    @Before
    public void setUp() {
        multiplayer = mock(IMultiplayer.class);
        client = mock(IClient.class);
        server = mock(IServer.class);
        multiplayerManager = new MultiplayerManager(multiplayer);
    }
}
