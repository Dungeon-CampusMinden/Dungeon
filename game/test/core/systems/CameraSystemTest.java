package core.systems;

import static org.junit.Assert.*;

import com.badlogic.gdx.utils.GdxNativesLoader;

import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.utils.Point;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class CameraSystemTest {

    private CameraSystem cameraSystem;
    private final ILevel level = Mockito.mock(ILevel.class);
    private final Tile startTile = Mockito.mock(Tile.class);

    private Point expectedFocusPoint;

    @BeforeClass
    public static void initGDX() {
        GdxNativesLoader.load(); // load natives for headless testing
    }

    @Before
    public void setup() {
        cameraSystem = new CameraSystem();
        Mockito.when(level.getRandomFloorTile()).thenReturn(startTile);
        Mockito.when(level.getStartTile()).thenReturn(startTile);
        Mockito.when(startTile.getCoordinateAsPoint()).thenReturn(new Point(3, 3));
    }

    @Test
    public void executeWithEntity() {
        Game.currentLevel = level;
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity);
new CameraComponent(entity);

        expectedFocusPoint = positionComponent.getPosition();

        cameraSystem.execute();
        assertEquals(expectedFocusPoint.x, CameraSystem.CAMERA.position.x, 0.001);
        assertEquals(expectedFocusPoint.y, CameraSystem.CAMERA.position.y, 0.001);
    }

    @Test
    public void executeWithoutEntity() {
        Game.removeAllEntities();
        Game.currentLevel = level;

        expectedFocusPoint = level.getStartTile().getCoordinateAsPoint();

        cameraSystem.execute();

        assertEquals(expectedFocusPoint.x, CameraSystem.CAMERA.position.x, 0.001);
        assertEquals(expectedFocusPoint.y, CameraSystem.CAMERA.position.y, 0.001);
    }

    @Test
    public void executeWithNullLevel() {
        Game.currentLevel = null;
        Point expectedFocusPoint = new Point(0, 0);
        cameraSystem.execute();
        assertEquals(expectedFocusPoint.x, CameraSystem.CAMERA.position.x, 0.001);
        assertEquals(expectedFocusPoint.y, CameraSystem.CAMERA.position.y, 0.001);
    }

    @Test
    public void isPointInFrustumWithVisiblePoint() {
        float x = 1.0f;
        float y = 1.0f;
        assertTrue(CameraSystem.isPointInFrustum(x, y));
    }

    @Test
    public void isPointInFrustumWithInvisiblePoint() {
        float x = 100.0f;
        float y = 100.0f;
        assertFalse(CameraSystem.isPointInFrustum(x, y));
    }
}
