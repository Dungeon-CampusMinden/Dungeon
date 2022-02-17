package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.DungeonCamera;
import graphic.HUDCamera;
import graphic.Painter;
import level.LevelAPI;
import level.generator.dungeong.levelg.LevelG;
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
@PrepareForTest({MainController.class, Gdx.class, Constants.class})
class MainControllerTest {
    MainController controller;
    SpriteBatch batch;
    int someArbitraryValueGreater0forDelta = 7;

    // Because of use of PowerMockRunner we need an empty constructor here
    public MainControllerTest() {}

    @Before
    public void setUp() throws Exception {
        controller = Mockito.spy(MainController.class);
        batch = Mockito.mock(SpriteBatch.class);

        Whitebox.setInternalState(Gdx.class, "gl", Mockito.mock(GL20.class));

        PowerMockito.whenNew(EntityController.class)
                .withNoArguments()
                .thenReturn(Mockito.mock(EntityController.class));
        PowerMockito.whenNew(Painter.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(Painter.class));
        PowerMockito.whenNew(SpriteBatch.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(SpriteBatch.class));
        PowerMockito.whenNew(HUDCamera.class)
                .withNoArguments()
                .thenReturn(Mockito.mock(HUDCamera.class));
        PowerMockito.whenNew(HUDController.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(HUDController.class));
        PowerMockito.whenNew(LevelAPI.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(LevelAPI.class));
        PowerMockito.whenNew(DungeonCamera.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(DungeonCamera.class));
        PowerMockito.whenNew(LevelG.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(LevelG.class));

        PowerMockito.mockStatic(Constants.class, invocation -> "abc");
    }

    @Test
    public void test_render() {
        controller.setSpriteBatch(batch);
        Mockito.verify(controller).setSpriteBatch(batch);
        Mockito.verifyNoMoreInteractions(controller, batch);

        controller.render(someArbitraryValueGreater0forDelta);
        Mockito.verify(controller).render(someArbitraryValueGreater0forDelta);
        Mockito.verify(controller).setup();
        Mockito.verify(controller, Mockito.times(2)).endFrame();
        Mockito.verifyNoMoreInteractions(controller);
    }
}
