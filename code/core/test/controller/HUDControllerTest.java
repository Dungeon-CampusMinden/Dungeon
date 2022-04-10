package controller;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import basiselements.HUDElement;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
    private HUDElement element1;
    private HUDElement element2;
    private Stage textStage;
    private HUDController controller;
    private HUDController controllerSpy;
    private Label labelMock;

    @Before
    public void setUp() throws Exception {
        batch = Mockito.mock(SpriteBatch.class);
        element1 = Mockito.mock(HUDElement.class);
        element2 = Mockito.mock(HUDElement.class);
        textStage = Mockito.mock(Stage.class);
        PowerMockito.whenNew(Stage.class).withAnyArguments().thenReturn(textStage);

        controller = new HUDController(batch);

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
        // verify update method logic:
        verify(element1).removable();
        verify(element1).update();
        verify(element1).draw();
        verify(element2).removable();
        verify(element2).update();
        verify(element2).draw();

        verify(textStage).act();
        verify(textStage).draw();
        verifyNoMoreInteractions(element1, element2, batch, textStage);
    }

    @Test
    public void test_update_empty() {
        assumeTrue(controller.isEmpty());

        controller.update();
        // verify HUDController constructor logic:
        // verify update method logic:
        verify(textStage).act();
        verify(textStage).draw();
        verifyNoMoreInteractions(element1, element2, batch, textStage);
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
