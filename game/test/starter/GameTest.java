package starter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ecs.entities.Entity;
import graphic.DungeonCamera;
import graphic.Painter;
import level.LevelAPI;
import level.generator.randomwalk.RandomWalkGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import tools.Constants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, Gdx.class, Constants.class})
class GameTest {
    Game game;
    SpriteBatch batch;
    int someArbitraryValueGreater0forDelta = 7;

    // Because of use of PowerMockRunner we need an empty constructor here
    public GameTest() {}

    @Before
    public void setUp() throws Exception {
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();

        game = Mockito.spy(Game.class);
        batch = Mockito.mock(SpriteBatch.class);

        Whitebox.setInternalState(Gdx.class, "gl", Mockito.mock(GL20.class));
        PowerMockito.whenNew(Painter.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(Painter.class));
        PowerMockito.whenNew(SpriteBatch.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(SpriteBatch.class));
        PowerMockito.whenNew(LevelAPI.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(LevelAPI.class));
        PowerMockito.whenNew(DungeonCamera.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(DungeonCamera.class));
        PowerMockito.whenNew(RandomWalkGenerator.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(RandomWalkGenerator.class));

        PowerMockito.mockStatic(Constants.class, invocation -> "abc");
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
        Entity e1 = Mockito.mock(Entity.class);
        Game.addEntity(e1);
        assertTrue(Game.getEntitiesToAdd().contains(e1));
        assertEquals(1, Game.getEntitiesToAdd().size());
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
    }

    @Test
    public void removeEntity() {
        Entity e1 = Mockito.mock(Entity.class);
        Game.removeEntity(e1);
        assertTrue(Game.getEntitiesToRemove().contains(e1));
        assertEquals(1, Game.getEntitiesToRemove().size());
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    /*
    Cannot be tested at the moment, render test must be fixed first

    @Test
    public void test_getEntity(){}
     */

    @Test
    public void setHero() {
        Entity hero = Mockito.mock(Entity.class);
        Game.setHero(hero);
        assertEquals(hero, Game.getHero().get());
        Entity hero2 = Mockito.mock(Entity.class);
        Game.setHero(hero2);
        assertEquals(hero2, Game.getHero().get());
    }
}
