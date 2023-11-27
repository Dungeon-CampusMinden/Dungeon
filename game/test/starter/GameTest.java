package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import core.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, Gdx.class, Constants.class})
class GameTest {

    private Game game;
    private SpriteBatch batch;
    private int someArbitraryValueGreater0forDelta = 7;

    // Because of use of PowerMockRunner we need an empty constructor here
    public GameTest() {}

    @Before
    public void setup() throws Exception {
        /*
        game = Mockito.spy(Game.class);
        batch = Mockito.mock(SpriteBatch.class);
        Whitebox.setInternalState(Gdx.class, "gl", Mockito.mock(GL20.class));
        PowerMockito.whenNew(Painter.class)
            .withAnyArguments()
            .thenReturn(Mockito.mock(Painter.class));
        PowerMockito.whenNew(SpriteBatch.class)
            .withAnyArguments()
            .thenReturn(Mockito.mock(SpriteBatch.class));
        PowerMockito.whenNew(LevelManager.class)
            .withAnyArguments()
            .thenReturn(Mockito.mock(LevelManager.class));
        PowerMockito.whenNew(DungeonCamera.class)
            .withAnyArguments()
            .thenReturn(Mockito.mock(DungeonCamera.class));
        PowerMockito.whenNew(RandomWalkGenerator.class)
            .withAnyArguments()
            .thenReturn(Mockito.mock(RandomWalkGenerator.class));

        PowerMockito.mockStatic(Constants.class, invocation -> "abc");
        */
    }

    @After
    public void cleanup() throws Exception {
        // Game.getDelayedEntitySet().removeAll(Game.getEntities());
        // Game.getDelayedEntitySet().update();
    }

    @Test
    public void test_render() {
        /*
        game.setSpriteBatch(batch);
         Mockito.verify(game).setSpriteBatch(batch);
         Mockito.verifyNoMoreInteractions(game, batch);

         game.render(someArbitraryValueGreater0forDelta);
         Mockito.verify(game).render(someArbitraryValueGreater0forDelta);
         Mockito.verify(game).setup();
         Mockito.verify(game).frame();
         Mockito.verify(game, Mockito.times(4)).runLoop();
         Mockito.verifyNoMoreInteractions(game);
        */
    }

    @Test
    public void test_render_paused() {
        /*
        game.setSpriteBatch(batch);
        when(game.runLoop()).thenReturn(false);
        Mockito.verify(game).setSpriteBatch(batch);
        Mockito.verifyNoMoreInteractions(game, batch);

        game.render(someArbitraryValueGreater0forDelta);
        Mockito.verify(game).render(someArbitraryValueGreater0forDelta);
        Mockito.verify(game).setup();
        Mockito.verify(game, never()).frame();
        when(game.runLoop()).thenReturn(true);
        Mockito.verify(game, Mockito.times(1)).runLoop();
        Mockito.verifyNoMoreInteractions(game);
         */
    }

    @Test
    public void addEntity() {
        /*    Entity e1 = new Entity();
        Game.addEntity(e1);
        assertFalse(Game.getEntities().contains(e1));
        Game.getDelayedEntitySet().update();
        assertTrue(Game.getEntities().contains(e1));
        assertEquals(1, Game.getEntities().size());*/
    }

    @Test
    public void removeEntity() {
        /*        Entity e1 = new Entity();
        Game.getDelayedEntitySet().update();
        Game.removeEntity(e1);
        Game.getDelayedEntitySet().update();
        assertFalse(Game.getEntities().contains(e1));
        assertEquals(0, Game.getEntities().size());*/
    }

    /*
    Cannot be tested at the moment, render test must be fixed first

    @Test
    public void test_getEntity(){}
     */

    @Test
    public void setHero() {
        /* Entity hero = new Entity();
        Game.setHero(hero);
        assertEquals(hero, Game.getHero().get());
        Entity hero2 = new Entity();
        Game.setHero(hero2);
        assertEquals(hero2, Game.getHero().get());*/
    }
}
