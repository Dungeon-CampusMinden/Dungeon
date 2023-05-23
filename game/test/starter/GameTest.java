package starter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxNativesLoader;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class GameTest {
    Game game;
    MockedStatic<SpriteBatch> spriteBatchMockedStatic;

    @Before
    public void setUp() {
        GdxNativesLoader.load();
        Gdx.app = mock(Application.class);
        Gdx.graphics = mock(Graphics.class);
        Gdx.input = mock(Input.class);
        Gdx.files = new Lwjgl3Files();
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);

        // We need a static mock from the SpriteBatch,
        // because our Game class, etc. instantiates a few SpriteBatch instances
        spriteBatchMockedStatic = mockStatic(SpriteBatch.class);
        spriteBatchMockedStatic
                .when(SpriteBatch::createDefaultShader)
                .thenReturn(mock(ShaderProgram.class));

        game = Game.newGame();
        // libgdx main loop logic triggers:
        game.render(0);
    }

    @After
    public void tearDown() {
        // Every static mock must be closed before a new test runs
        spriteBatchMockedStatic.close();
    }

    @Test
    public void addEntity() {
        Entity e1 = new Entity();
        game.render(0);
        assertEquals(
                1, Game.getEntitiesStream().filter(x -> x.equals(e1)).limit(2).toList().size());
    }

    @Test
    public void removeEntity() {
        Entity e1 = new Entity();
        game.render(0);
        assertEquals(
                1, Game.getEntitiesStream().filter(x -> x.equals(e1)).limit(2).toList().size());
        Game.removeEntity(e1);
        game.render(0);
        assertEquals(
                0, Game.getEntitiesStream().filter(x -> x.equals(e1)).limit(2).toList().size());
    }

    @Test
    public void setHero() {
        Entity hero = new Entity();
        Game.setHero(hero);
        assertEquals(hero, Game.getHero().orElseThrow());
        Entity hero2 = new Entity();
        Game.setHero(hero2);
        assertEquals(hero2, Game.getHero().orElseThrow());
    }
}
