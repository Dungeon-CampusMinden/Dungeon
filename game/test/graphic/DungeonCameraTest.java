package graphic;

import static org.junit.Assert.*;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxNativesLoader;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import org.junit.BeforeClass;
import org.junit.Test;
import starter.Game;
import tools.Constants;
import tools.Point;

public class DungeonCameraTest {

    @BeforeClass
    public static void setUpGdx() {
        GdxNativesLoader.load(); // load natives for headless testing
    }

    @Test
    public void test_update() {
        // Prepare
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity, new Point(2, 2));
        DungeonCamera camera =
                new DungeonCamera(
                        positionComponent, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        assertEquals(
                "Pos should be (0, 0, 0) initially.",
                camera.position,
                new Vector3(0, 0, 0)); // Camera is initially at (0, 0, 0)
        camera.update();
        assertEquals(
                "Pos should be (2, 2, 0) after update.",
                camera.position,
                new Vector3(2, 2, 0)); // Because it follows the positionComponent
        positionComponent.setPosition(new Point(15, 23));
        camera.update();
        assertEquals(
                "Pos should be (15, 23, 0) after update.",
                camera.position,
                new Vector3(15, 23, 0)); // Because it follows the positionComponent

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_update_noFollow() {
        // Prepare
        DungeonCamera camera =
                new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        camera.update();
        assertEquals("Pos should be (0, 0, 0) initially.", camera.position, new Vector3(0, 0, 0));
        camera.setFocusPoint(new Point(15, 23));
        assertEquals(
                "Pos should be (0, 0, 0) before update.", camera.position, new Vector3(0, 0, 0));
        camera.update();
        assertEquals(
                "Pos should be (15, 23, 0) after update.", camera.position, new Vector3(15, 23, 0));

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_follow() {
        // Prepare
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity, new Point(2, 2));
        DungeonCamera camera =
                new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        assertEquals(
                "Camera should be at (0, 0, 0) initially.", camera.position, new Vector3(0, 0, 0));
        camera.follow(positionComponent);
        assertSame("Camera should follow entity.", camera.getFollowedObject(), positionComponent);
        assertEquals(
                "Camera should still be at (7, 11, 0). (No update() after follow())",
                camera.position,
                new Vector3(0, 0, 0));
        camera.update();
        assertEquals(
                "Camera should be at (2, 2, 0) after update.",
                camera.position,
                new Vector3(2, 2, 0));

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_follow_noFollow() {
        // Prepare
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity, new Point(2, 2));
        DungeonCamera camera =
                new DungeonCamera(
                        positionComponent, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        camera.update();
        assertEquals(
                "Camera should follow given positionComponent",
                camera.getFollowedObject(),
                positionComponent);
        camera.follow(null);
        assertNull("Camera should not follow anything", camera.getFollowedObject());

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_getFollowedObject() {
        // Prepare
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity, new Point(2, 2));
        DungeonCamera camera =
                new DungeonCamera(
                        positionComponent, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        assertSame("Camera should follow entity.", camera.getFollowedObject(), positionComponent);

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_getFollowedObject_noFollow() {
        // Prepare
        DungeonCamera camera =
                new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        assertNull("Camera should not follow anything", camera.getFollowedObject());

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_setFocusPoint() {
        // Prepare
        Entity entity = new Entity();
        PositionComponent positionComponent = new PositionComponent(entity, new Point(2, 2));
        DungeonCamera camera =
                new DungeonCamera(
                        positionComponent, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.update();

        // Testing
        assertEquals("Camera should be at (2, 2, 0).", camera.position, new Vector3(2, 2, 0));
        assertSame("Camera should follow entity.", camera.getFollowedObject(), positionComponent);
        camera.setFocusPoint(new Point(15, 23));
        camera.update();
        assertEquals(
                "Camera should be at (15, 23, 0) after setFocusPoint().",
                camera.position,
                new Vector3(15, 23, 0));
        assertNull("Camera should not follow anything", camera.getFollowedObject());

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_setFocusPoint_noFollow() {
        // Prepare
        DungeonCamera camera =
                new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Testing
        assertEquals(
                "Camera should be at (0, 0, 0) initially.", camera.position, new Vector3(0, 0, 0));
        assertNull("Camera should not follow anything", camera.getFollowedObject());
        camera.setFocusPoint(new Point(15, 23));
        camera.update();
        assertEquals(
                "Camera should be at (15, 23, 0) after setFocusPoint().",
                camera.position,
                new Vector3(15, 23, 0));
        assertNull("Camera should not follow anything", camera.getFollowedObject());

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }

    @Test
    public void test_isPointInFrustum() {
        // Prepare
        DungeonCamera camera =
                new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.update();

        // Testing
        assertTrue("Point should be in frustum.", camera.isPointInFrustum(0, 0));
        assertTrue(
                "Point should be in frustum",
                camera.isPointInFrustum(
                        camera.position.x + Constants.VIEWPORT_WIDTH / 2,
                        camera.position.y + Constants.VIEWPORT_HEIGHT / 2));
        assertFalse("Point should not be in frustum.", camera.isPointInFrustum(100, 100));
        assertFalse(
                "Point should not be in frustum",
                camera.isPointInFrustum(
                        camera.position.x + Constants.VIEWPORT_WIDTH / 2 + 1,
                        camera.position.y + Constants.VIEWPORT_HEIGHT / 2 + 1));

        // Cleanup
        Game.getEntities().clear();
        Game.getEntitiesToRemove().clear();
    }
}
