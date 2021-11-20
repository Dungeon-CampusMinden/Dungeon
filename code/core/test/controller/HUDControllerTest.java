package controller;

import com.badlogic.gdx.graphics.Texture;
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
    private GraphicController gc;
    private HUDCamera camera;

    @BeforeEach
    public void init() {
        this.gc = mock(GraphicController.class);
        this.batch = mock(SpriteBatch.class);
        this.camera = mock(HUDCamera.class);
        when(camera.getPosition()).thenReturn(new Vector3());
        this.hc = new HUDController(this.batch, this.gc, this.camera);
    }

    @Test
    public void clearHUD_True() {
        IHUDElement e1 = mock(IHUDElement.class);
        IHUDElement e2 = mock(IHUDElement.class);
        hc.addElement(e1);
        hc.addElement(e2);
        hc.clearHUD();
        assertTrue(hc.getElements().isEmpty());
    }

    @Test
    public void update_FilledList_verify() {
        IHUDElement e1 = mock(IHUDElement.class);
        IHUDElement e2 = mock(IHUDElement.class);
        when(e1.getTexture()).thenReturn(mock(Texture.class));
        when(e2.getTexture()).thenReturn(mock(Texture.class));
        when(e1.getPosition()).thenReturn(mock(Point.class));
        when(e2.getPosition()).thenReturn(mock(Point.class));
        hc.addElement(e1);
        hc.addElement(e2);
        hc.update();
        verify(gc).draw(e1.getTexture(), e1.getPosition(), batch);
        verify(gc).draw(e2.getTexture(), e2.getPosition(), batch);
    }

    @Test
    public void update_FilledListWithOneRemove_True() {
        IHUDElement e1 = mock(IHUDElement.class);
        IHUDElement e2 = mock(IHUDElement.class);
        when(e1.getTexture()).thenReturn(mock(Texture.class));
        when(e2.getTexture()).thenReturn(mock(Texture.class));
        when(e1.getPosition()).thenReturn(mock(Point.class));
        when(e2.getPosition()).thenReturn(mock(Point.class));
        hc.addElement(e1);
        hc.addElement(e2);
        hc.removeElement(e2);
        hc.update();
        verify(gc).draw(e1.getTexture(), e1.getPosition(), batch);
        verify(gc, never()).draw(e2.getTexture(), e2.getPosition(), batch);
    }

    @Test
    public void update_EmptyList_NoException() {
        hc.update();
    }
}
