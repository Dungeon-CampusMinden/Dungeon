package controller;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import graphic.DungeonCamera;
import interfaces.IEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.Point;

import static org.mockito.Mockito.*;

public class GraphicControllerTest {

    private GraphicController gc;
    private DungeonCamera camera;
    private IEntity drawable;
    private Texture texture;
    private Point p;
    private SpriteBatch batch;
    private Frustum frustum;
    private int buffer = 2;

    @BeforeEach
    public void init() {
        camera = mock(DungeonCamera.class);
        gc = new GraphicController(camera);
        drawable = mock(IEntity.class);
        texture = mock(Texture.class);
        p = new Point(3, 3);
        batch = mock(SpriteBatch.class);
        frustum = mock(Frustum.class);
    }

    @Test
    public void draw_PointInFrustumRightTop_verify() {
        when(camera.getFrustum()).thenReturn(frustum);
        when(texture.getHeight()).thenReturn(1);
        when(texture.getWidth()).thenReturn(1);
        when(frustum.pointInFrustum(p.x + buffer, p.y - buffer, 0)).thenReturn(true);
        gc.draw(texture, p, batch);
        // seems like the only way to check if he tries to draw
        verify(batch).begin();
        verify(batch).end();
    }

    @Test
    public void draw_PointInFrustumRightBottom_verify() {
        when(camera.getFrustum()).thenReturn(frustum);
        when(texture.getHeight()).thenReturn(1);
        when(texture.getWidth()).thenReturn(1);
        when(frustum.pointInFrustum(p.x + buffer, p.y - buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x + buffer, p.y + buffer, 0)).thenReturn(true);
        gc.draw(texture, p, batch);
        // seems like the only way to check if he tries to draw
        verify(batch).begin();
        verify(batch).end();
    }

    @Test
    public void draw_PointInFrustumLeftTop_verify() {
        when(camera.getFrustum()).thenReturn(frustum);
        when(texture.getHeight()).thenReturn(1);
        when(texture.getWidth()).thenReturn(1);
        when(frustum.pointInFrustum(p.x + buffer, p.y - buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x + buffer, p.y + buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x - buffer, p.y - buffer, 0)).thenReturn(true);
        gc.draw(texture, p, batch);
        // seems like the only way to check if he tries to draw
        verify(batch).begin();
        verify(batch).end();
    }

    @Test
    public void draw_PointInFrustumLeftBottom_verify() {
        when(camera.getFrustum()).thenReturn(frustum);
        when(texture.getHeight()).thenReturn(1);
        when(texture.getWidth()).thenReturn(1);
        when(frustum.pointInFrustum(p.x + buffer, p.y - buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x + buffer, p.y + buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x - buffer, p.y - buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x - buffer, p.y + buffer, 0)).thenReturn(true);
        gc.draw(texture, p, batch);
        // seems like the only way to check if he tries to draw
        verify(batch).begin();
        verify(batch).end();
    }

    @Test
    public void draw_PointNotInFrustum_verify() {
        when(camera.getFrustum()).thenReturn(frustum);
        when(texture.getHeight()).thenReturn(1);
        when(texture.getWidth()).thenReturn(1);
        when(frustum.pointInFrustum(p.x + buffer, p.y - buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x + buffer, p.y + buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x - buffer, p.y - buffer, 0)).thenReturn(false);
        when(frustum.pointInFrustum(p.x - buffer, p.y + buffer, 0)).thenReturn(false);
        gc.draw(texture, p, batch);
        // seems like the only way to check if he tries to draw
        verify(batch, never()).begin();
        verify(batch, never()).end();
    }
}
