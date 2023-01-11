package controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import graphic.DungeonCamera;
import graphic.Painter;
import hud.LevelEditorGui;
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
    Stage stage;
    int someArbitraryValueGreater0forDelta = 7;

    // Because of use of PowerMockRunner we need an empty constructor here
    public GameTest() {}

    @Before
    public void setUp() throws Exception {
        game = Mockito.spy(Game.class);
        batch = Mockito.mock(SpriteBatch.class);
        stage = Mockito.mock(Stage.class);

        Whitebox.setInternalState(Gdx.class, "gl", Mockito.mock(GL20.class));
        Whitebox.setInternalState(Gdx.class, "graphics", Mockito.mock(Graphics.class));
        Whitebox.setInternalState(Gdx.class, "input", Mockito.mock(Input.class));

        PowerMockito.whenNew(EntityController.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(EntityController.class));
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
        PowerMockito.whenNew(LevelEditorGui.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(LevelEditorGui.class));

        PowerMockito.when(stage.getViewport()).thenReturn(Mockito.mock(Viewport.class));

        PowerMockito.mockStatic(Constants.class, invocation -> "abc");
    }

    @Test
    public void test_render() {
        game.setSpriteBatch(batch);
        game.setStage(stage);

        Mockito.verify(game).setSpriteBatch(batch);
        Mockito.verify(game).setStage(stage);
        Mockito.verifyNoMoreInteractions(game, batch, stage);

        game.render(someArbitraryValueGreater0forDelta);
        Mockito.verify(game).render(someArbitraryValueGreater0forDelta);
        Mockito.verify(game).setup();
        Mockito.verify(game).frame();
        Mockito.verify(game, Mockito.times(4)).runLoop();
        Mockito.verifyNoMoreInteractions(game);
    }

    @Test
    public void test_render_paused() {
        game.setSpriteBatch(batch);
        game.setStage(stage);
        when(game.runLoop()).thenReturn(false);
        Mockito.verify(game).setSpriteBatch(batch);
        Mockito.verify(game).setStage(stage);
        Mockito.verifyNoMoreInteractions(game, batch, stage);

        game.render(someArbitraryValueGreater0forDelta);
        Mockito.verify(game).render(someArbitraryValueGreater0forDelta);
        Mockito.verify(game).setup();
        Mockito.verify(game, never()).frame();
        when(game.runLoop()).thenReturn(true);
        Mockito.verify(game, Mockito.times(1)).runLoop();
        Mockito.verifyNoMoreInteractions(game);
    }
}
