package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import graphic.HUDCamera;
import interfaces.IHUDElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HUDController.class})
public class HUDControllerTest {
    private SpriteBatch batch;
    private HUDCamera camera;
    private IHUDElement element1;
    private IHUDElement element2;
    private HUDController controller;

    @Before
    public void setUp() {
        batch = Mockito.mock(SpriteBatch.class);
        camera = Mockito.mock(HUDCamera.class);
        element1 = Mockito.mock(IHUDElement.class);
        element2 = Mockito.mock(IHUDElement.class);

        Vector3 vector3toReturn = new Vector3();
        when(camera.getPosition()).thenReturn(vector3toReturn);

        controller = new HUDController(batch, camera);
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
        verifyNoMoreInteractions(camera, batch, element1, element2);
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
        verifyNoMoreInteractions(camera, batch, element1, element2);
    }
}
