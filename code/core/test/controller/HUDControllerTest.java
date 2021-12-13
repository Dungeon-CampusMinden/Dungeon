package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import graphic.HUDCamera;
import interfaces.IHUDElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.Point;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class HUDControllerTest {

    private HUDController hc;
    private SpriteBatch batch;
    private HUDCamera camera;

    @BeforeEach
    public void init() {
        this.batch = mock(SpriteBatch.class);
        this.camera = mock(HUDCamera.class);
        when(camera.getPosition()).thenReturn(new Vector3());
        this.hc = new HUDController(this.batch, this.camera);
    }

    @Test
    public void clearHUD_True() {
        IHUDElement e1 = mock(IHUDElement.class);
        IHUDElement e2 = mock(IHUDElement.class);
        hc.add(e1);
        hc.add(e2);
        hc.removeAll();
        assertTrue(hc.getList().isEmpty());
    }

    @Test
    public void update_FilledList_verify() {
        IHUDElement e1 = mock(IHUDElement.class);
        IHUDElement e2 = mock(IHUDElement.class);
        when(e1.getPosition()).thenReturn(mock(Point.class));
        when(e2.getPosition()).thenReturn(mock(Point.class));
        hc.add(e1);
        hc.add(e2);
        hc.update();
        verify(e1).draw(batch);
        verify(e2).draw(batch);
    }

    @Test
    public void update_FilledListWithOneRemove_True() {
        IHUDElement e1 = mock(IHUDElement.class);
        IHUDElement e2 = mock(IHUDElement.class);
        when(e1.getPosition()).thenReturn(mock(Point.class));
        when(e2.getPosition()).thenReturn(mock(Point.class));
        hc.add(e1);
        hc.add(e2);
        hc.remove(e2);
        hc.update();
        verify(e1).draw(batch);
        verify(e2, never()).draw(batch);
    }

    @Test
    public void update_EmptyList_NoException() {
        hc.update();
    }
}
