package controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import starter.Game;
import starter.Game.LibgdxSetup;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LibgdxSetup.class})
class LibgdxSetupTest {
    private Game game;
    private Game.LibgdxSetup setup;

    // Because of use of PowerMockRunner we need an empty constructor here
    public LibgdxSetupTest() {}

    @Before
    public void setUp() throws Exception {
        game = Mockito.mock(Game.class);
        setup = Mockito.spy(new LibgdxSetup(game));
        PowerMockito.doNothing().when(setup, "setScreen", game);
        setup.create();
    }

    @Test
    public void test_create() {
        Mockito.verify(setup).create();
        Mockito.verify(setup).setScreen(game);
        Mockito.verifyNoMoreInteractions(setup, game);
    }
}
