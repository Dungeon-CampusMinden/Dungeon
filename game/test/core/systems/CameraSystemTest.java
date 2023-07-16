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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class CameraSystemTest {

    private static final Point testPoint = new Point(3, 3);
    private final ILevel level = Mockito.mock(ILevel.class);
    private final Tile startTile = Mockito.mock(Tile.class);
    private CameraSystem cameraSystem;
    private Point expectedFocusPoint;

    @BeforeClass
    public static void initGDX() {
        GdxNativesLoader.load(); // load natives for headless testing
    }

    @Before
    public void setup() {
        cameraSystem = new CameraSystem();
        Mockito.when(startTile.position()).thenReturn(testPoint);
        Mockito.when(level.randomTilePoint(Mockito.any())).thenReturn(testPoint);
        Mockito.when(level.startTile()).thenReturn(startTile);
        new LevelSystem(null, null, () -> {});
    }

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.removeAllSystems();
    }

    @Test
    public void executeWithEntity() {
        Game.currentLevel(level);
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity);
        new CameraComponent(entity);

        expectedFocusPoint = positionComponent.position();

        cameraSystem.execute();
        assertEquals(expectedFocusPoint.x, CameraSystem.camera().position.x, 0.001);
        assertEquals(expectedFocusPoint.y, CameraSystem.camera().position.y, 0.001);
    }

    @Test
    public void executeWithoutEntity() {
        Game.currentLevel(level);

        expectedFocusPoint = level.startTile().position();

        cameraSystem.execute();

        assertEquals(expectedFocusPoint.x, CameraSystem.camera().position.x, 0.001);
        assertEquals(expectedFocusPoint.y, CameraSystem.camera().position.y, 0.001);
    }

    @Test
    public void executeWithoutLevel() {
        Game.currentLevel((ILevel)null);
        Point expectedFocusPoint = new Point(0, 0);
        cameraSystem.execute();
        assertEquals(expectedFocusPoint.x, CameraSystem.camera().position.x, 0.001);
        assertEquals(expectedFocusPoint.y, CameraSystem.camera().position.y, 0.001);
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
