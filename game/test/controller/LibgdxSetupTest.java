package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import starter.Game;
import starter.LibgdxSetup;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LibgdxSetup.class})
class LibgdxSetupTest {
    Game game;
    SpriteBatch batch;
    LibgdxSetup setup;

    // Because of use of PowerMockRunner we need an empty constructor here
    public LibgdxSetupTest() {}

    @Before
    public void setUp() throws Exception {
        game = Mockito.mock(Game.class);
        batch = Mockito.mock(SpriteBatch.class);
        PowerMockito.whenNew(SpriteBatch.class).withNoArguments().thenReturn(batch);

        setup = Mockito.spy(new LibgdxSetup(game));
        PowerMockito.doNothing().when(setup, "setScreen", game);
        PowerMockito.doNothing().when(batch).dispose();

        setup.create();
    }

    @Test(expected = NullPointerException.class)
    public void test_constructor_null() {
        LibgdxSetup l = Mockito.spy(new LibgdxSetup(null));
        l.create();
    }

    @Test
    public void test_create() {
        Mockito.verify(setup).create();
        Mockito.verify(game).setSpriteBatch(batch);
        Mockito.verify(setup).setScreen(game);
        Mockito.verifyNoMoreInteractions(setup, batch, game);
    }

    @Test
    public void test_dispose() {
        Mockito.verify(setup).create();
        Mockito.verify(game).setSpriteBatch(batch);
        Mockito.verify(setup).setScreen(game);
        Mockito.verifyNoMoreInteractions(setup, batch, game);

        setup.dispose();
        Mockito.verify(setup).dispose();
        Mockito.verify(batch, Mockito.times(2)).dispose();
        Mockito.verifyNoMoreInteractions(setup, batch, game);
    }
}
