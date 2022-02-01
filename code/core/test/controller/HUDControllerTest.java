package controller;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import graphic.HUDCamera;
import interfaces.IHUDElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HUDController.class})
public class HUDControllerTest {
    private SpriteBatch batch;
    private HUDCamera camera;
    private IHUDElement element1;
    private IHUDElement element2;
    private Stage textStage;
    private HUDController controller;
    private HUDController controllerSpy;
    private Label labelMock;

    @Before
    public void setUp() throws Exception {
        batch = Mockito.mock(SpriteBatch.class);
        camera = Mockito.mock(HUDCamera.class);
        element1 = Mockito.mock(IHUDElement.class);
        element2 = Mockito.mock(IHUDElement.class);
        textStage = Mockito.mock(Stage.class);
        PowerMockito.whenNew(Stage.class).withAnyArguments().thenReturn(textStage);
        Vector3 vector3toReturn = new Vector3();
        when(camera.getPosition()).thenReturn(vector3toReturn);

        controller = new HUDController(batch, camera);

        controllerSpy = Mockito.spy(controller);
        labelMock = Mockito.mock(Label.class);

        Whitebox.setInternalState(Gdx.class, "files", Mockito.mock(Files.class));
        PowerMockito.whenNew(FreeTypeFontGenerator.class)
                .withAnyArguments()
                .thenReturn(Mockito.mock(FreeTypeFontGenerator.class));
        PowerMockito.whenNew(Label.class).withAnyArguments().thenReturn(labelMock);
    }

    @Test
    public void test_update() {
        assumeTrue(controller.add(element1));
        assumeTrue(controller.add(element2));

        controller.update();
        // verify HUDController constructor logic:
        verify(camera).getPosition();
        verify(camera, atLeastOnce()).update();
        // verify update method logic:
        verify(batch).setProjectionMatrix(camera.combined);
        verify(element1).draw(batch);
        verify(element2).draw(batch);
        verify(textStage).act();
        verify(textStage).draw();
        verifyNoMoreInteractions(camera, batch, element1, element2, textStage);
    }

    @Test
    public void test_update_empty() {
        assumeTrue(controller.isEmpty());

        controller.update();
        // verify HUDController constructor logic:
        verify(camera).getPosition();
        verify(camera, atLeastOnce()).update();
        // verify update method logic:
        verify(batch).setProjectionMatrix(camera.combined);
        verify(textStage).act();
        verify(textStage).draw();
        verifyNoMoreInteractions(camera, batch, element1, element2, textStage);
    }

    /**
     * The method "drawText(text, fontPath, color, size, width, height, x, y, borderWidth)" is to be
     * tested (with the parameter "borderWidth"). For this, it is checked whether the returned
     * "label" matches "labelMock" and the corresponding methods ("width, height, x, y") were called
     * on the "labelMock". There should be no other calls on the mock label.
     */
    @Test
    public void test_drawText_1() {
        String text = "hello";
        String fontPath = "path";
        Color color = Color.CORAL;
        int size = 15;
        int width = 300;
        int height = 200;
        int x = 12;
        int y = 14;
        int borderWidth = 5;

        Label label =
                controllerSpy.drawText(
                        text, fontPath, color, size, width, height, x, y, borderWidth);
        // By using PowerMockito we additionally have to verify the initial call of drawText on the
        // controllerSpy. This is related to the final verifyNoMoreInteractions call.
        verify(controllerSpy)
                .drawText(text, fontPath, color, size, width, height, x, y, borderWidth);
        Assert.assertEquals(labelMock, label);
        verify(labelMock).setSize(width, height);
        verify(labelMock).setPosition(x, y);
        verifyNoMoreInteractions(controllerSpy, labelMock);
    }

    /**
     * The method "drawText(text, fontPath, color, size, width, height, x, y)" is to be tested
     * (without the parameter "borderWidth"). For this purpose, it is only checked whether
     * delegation is made to the corresponding method with the "borderWidth" parameter. The
     * "borderWidth" parameter should always be 1. In addition, the methods ("width, height, x, y")
     * are to be called on the "labelMock". There should be no other calls on the mock label.
     */
    @Test
    public void test_drawText_2_delegation() {
        String text = "hello";
        String fontPath = "path";
        Color color = Color.CORAL;
        int size = 15;
        int width = 300;
        int height = 200;
        int x = 12;
        int y = 14;

        Label label = controllerSpy.drawText(text, fontPath, color, size, width, height, x, y);
        // By using PowerMockito we additionally have to verify the initial call of drawText on the
        // controllerSpy. This is related to the final verifyNoMoreInteractions call.
        verify(controllerSpy).drawText(text, fontPath, color, size, width, height, x, y);
        verify(controllerSpy).drawText(text, fontPath, color, size, width, height, x, y, 1);
        Assert.assertEquals(labelMock, label);
        verify(labelMock).setSize(width, height);
        verify(labelMock).setPosition(x, y);
        verifyNoMoreInteractions(controllerSpy, labelMock);
    }
}
