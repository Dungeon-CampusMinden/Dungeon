package ecs.systems;

import static org.junit.Assert.*;

import controller.SystemController;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.SimpleCounter;
import ecs.components.collision.ICollide;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import org.junit.Test;
import starter.Game;
import tools.Point;

public class CollisionSystemTest {

    private static final String DIRECTION_MESSAGE = "The Direction of the Collision should be.";
    private static final String NO_COLLISION_DETECTION_MESSAGE =
            "No Collision between the two hitboxes should be detected.";
    private static final String COLLISION_DETECTED_MESSSAGE =
            "Collision between the two hitboxes should be detected.";
    private static final String MISSING_POSITION_COMPONENT =
            "PositionComponent did get removed Test no longer valid";

    /**
     * Helper to clean up used Class Attributes to avoid interfering with other tests
     *
     * <p>all Systems add themselves to the Class Attribute SystemController of the Game. to Check
     * the correct processing of the CollisionSystemController the entities are added to the
     * entities list
     */
    private static void cleanUpEnvironment() {
        Game.systems = null;
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
    }

    /** Creating a clean Systemcontroller to avoid interferences */
    private static void prepareEnvironment() {
        cleanUpEnvironment();
        Game.systems = new SystemController();
    }

    /**
     * Helper to create an Entity and keep Testcode a bit more clean
     *
     * @param point1 Position of the newly created Entity
     * @return thr configured Entity
     */
    private static Entity prepareEntityWithPosition(Point point1) {
        Entity e1 = new Entity();
        new PositionComponent(e1, point1);
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        return e1;
    }

    /**
     * Check if the Collision is detected when the hitbox A is on the left of hitbox B
     *
     * <p>Left means the Position of B is higher on the x-axis
     */
    @Test
    public void checkForCollisionRight() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(.5f, 0));

        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertTrue(COLLISION_DETECTED_MESSSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(DIRECTION_MESSAGE, Tile.Direction.E, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox A is on the left of hitbox B and not
     * colliding
     *
     * <p>Left means the Position of B is higher on the x-axis not colliding means there is no
     * possible intersection between A and B and there is A gap between to avoid float inaccuracy
     */
    @Test
    public void checkForCollisionRightNoIntersection() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));

        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(1.5f, 0));

        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertFalse(NO_COLLISION_DETECTION_MESSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(DIRECTION_MESSAGE, Tile.Direction.E, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox A is on the right of hitbox B
     *
     * <p>Right means the Position of B is lower on the x-axis not colliding means there is no
     * possible intersection between A and B and there is A gap between to avoid float inaccuracy
     */
    @Test
    public void checkForCollisionLeft() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(-.5f, 0));
        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertTrue(COLLISION_DETECTED_MESSSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(Tile.Direction.W, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox A is on the right of hitbox B and not
     * colliding
     *
     * <p>Right means the Position of B is lower on the x-axis not colliding means there is no
     * possible intersection between A and B and there is A gap between to avoid float inaccuracy
     */
    @Test
    public void checkForCollisionLeftNoIntersection() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();

        Entity e1 = prepareEntityWithPosition(new Point(0, 0));

        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(-1.5f, 0));

        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertFalse(NO_COLLISION_DETECTION_MESSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(Tile.Direction.W, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox A is above hitbox B
     *
     * <p>above means the Position of B is higher on the y-axis
     */
    @Test
    public void checkForCollisionBottomWithIntersection() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(0, .5f));
        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertTrue(COLLISION_DETECTED_MESSSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(Tile.Direction.S, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if no Collision is detected when the hitbox A is above hitbox B and not colliding
     *
     * <p>above means the Position of B is higher on the y-axis not colliding means there is no
     * possible intersection between A and B and there is A gap between to avoid float inaccuracy
     */
    @Test
    public void checkForCollisionBottomWithNoIntersection() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(0, 1.5f));
        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertFalse(NO_COLLISION_DETECTION_MESSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(Tile.Direction.S, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox A is below hitbox B
     *
     * <p>below means the Position of B is lower on the y-axis
     */
    @Test
    public void checkForCollisionTopWithIntersection() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(0, -0.5f));
        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertTrue(COLLISION_DETECTED_MESSSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(Tile.Direction.N, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if no Collision is detected when the hitbox A is below hitbox B and not colliding *
     *
     * <p>below means the Position of B is lower on the y-axis not colliding means there is no
     * possible intersection between A and B and there is A gap between to avoid float inaccuracy
     */
    @Test
    public void checkForCollisionTopNoIntersection() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(e1, new Point(offset), new Point(size), collider, collider);

        Entity e2 = prepareEntityWithPosition(new Point(0, -1.5f));
        HitboxComponent hb2 =
                new HitboxComponent(e2, new Point(offset), new Point(size), collider, collider);

        assertFalse(NO_COLLISION_DETECTION_MESSAGE, cs.checkForCollision(hb1, hb2));
        assertEquals(Tile.Direction.N, cs.checkDirectionOfCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox A is bigger and every Corner is around
     * hitbox B
     */
    @Test
    public void checkForCollisionBoxAAroundB() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(-.1f, -.1f));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(
                        e1,
                        new Point(new Point(0, 0)),
                        new Point(new Point(1.2f, 1.2f)),
                        collider,
                        collider);

        Entity e2 = prepareEntityWithPosition(new Point(0, 0f));
        HitboxComponent hb2 =
                new HitboxComponent(
                        e2,
                        new Point(new Point(0, 0)),
                        new Point(new Point(1, 1)),
                        collider,
                        collider);

        assertTrue(COLLISION_DETECTED_MESSSAGE, cs.checkForCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /**
     * Check if the Collision is detected when the hitbox B is bigger and every Corner is around
     * hitbox A
     */
    @Test
    public void checkForCollisionBoxBAroundA() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        ICollide collider = (a, b, c) -> {};
        HitboxComponent hb1 =
                new HitboxComponent(
                        e1,
                        new Point(new Point(0, 0)),
                        new Point(new Point(1, 1)),
                        collider,
                        collider);

        Entity e2 = prepareEntityWithPosition(new Point(-.1f, -.1f));
        HitboxComponent hb2 =
                new HitboxComponent(
                        e2,
                        new Point(new Point(0, 0)),
                        new Point(new Point(1.2f, 1.2f)),
                        collider,
                        collider);

        assertTrue(COLLISION_DETECTED_MESSSAGE, cs.checkForCollision(hb1, hb2));
        cleanUpEnvironment();
    }

    /** Checks the inverse Direction of the Tile.Direction N -> S */
    @Test
    public void checkInverseN() {
        prepareEnvironment();
        assertEquals(Tile.Direction.S, new CollisionSystem().inverse(Tile.Direction.N));
        cleanUpEnvironment();
    }

    /** Checks the inverse Direction of the Tile.Direction E -> W */
    @Test
    public void checkInverseE() {
        prepareEnvironment();
        assertEquals(Tile.Direction.W, new CollisionSystem().inverse(Tile.Direction.E));
        cleanUpEnvironment();
    }

    /** Checks the inverse Direction of the Tile.Direction S -> N */
    @Test
    public void checkInverseS() {
        prepareEnvironment();
        assertEquals(Tile.Direction.N, new CollisionSystem().inverse(Tile.Direction.S));
        cleanUpEnvironment();
    }

    /** Checks the inverse Direction of the Tile.Direction W -> E */
    @Test
    public void checkInverseW() {
        prepareEnvironment();
        assertEquals(Tile.Direction.E, new CollisionSystem().inverse(Tile.Direction.W));
        cleanUpEnvironment();
    }

    /** Checks if the System is still Working even if there is no Entity */
    @Test
    public void checkUpdateNoEntities() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        cs.update();
        cleanUpEnvironment();
    }

    /** Checks that the System is still working when there is no Entity with A hitboxComponent */
    @Test
    public void checkUpdateNoEntitiesWithHitboxComponent() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        prepareEntityWithPosition(new Point(0, 0));
        cs.update();
        cleanUpEnvironment();
    }

    /**
     * Checks that there is no call off the collider Methods when there is only one hitbox entity
     */
    @Test
    public void checkUpdateOneEntityWithHitboxComponent() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        cs.update();
        assertEquals("No interaction begins for e1", 0, sc1OnEnter.getCount());
        assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
        cleanUpEnvironment();
    }

    /** Checks that there is no call off the collider Methods when there is no Collision */
    @Test
    public void checkUpdateTwoEntitiesWithHitboxComponentNonColliding() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(1, 1));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        cs.update();
        assertEquals("No interaction begins for e1", 0, sc1OnEnter.getCount());
        assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
        assertEquals("No interaction begins for e2", 0, sc2OnEnter.getCount());
        assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());

        cleanUpEnvironment();
    }

    /**
     * Checks the call of the onEnterCollider when the Collision started happening
     *
     * <p>the collision between A and B was happening in between CollisionSystem#update calls
     */
    @Test
    public void checkUpdateTwoEntitiesWithHitboxComponentColliding() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(1, 2));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
        assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
        assertEquals("No interaction begins for e3", 0, sc3OnEnter.getCount());
        assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }

    /**
     * Checks the call of the onEnterCollider when the Collision started happening only being called
     * once
     *
     * <p>the collision between A and B was happening in between CollisionSystem#update calls
     */
    @Test
    public void checkUpdateTwoEntitiesWithHitboxComponentCollidingOnlyOnce() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(1, 2));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        cs.update();
        assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
        assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
        assertEquals("No interaction begins for e3", 0, sc3OnEnter.getCount());
        assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }

    /**
     * Checks the call of the onLeaveCollider when the Collision is no longer happening
     *
     * <p>the collision between A and B was brocken up in between CollisionSystem#update calls
     */
    @Test
    public void checkUpdateTwoEntitiesWithHitboxComponentNoLongerColliding() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(1, 2));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        e1.getComponent(PositionComponent.class)
                .map(PositionComponent.class::cast)
                .ifPresent(x -> x.getPosition().x += 2);
        cs.update();
        assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
        assertEquals("One interaction ends for e1", 1, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("One interaction ends for e2", 1, sc2OnLeave.getCount());
        assertEquals("No interaction begins for e3", 0, sc3OnEnter.getCount());
        assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }

    /**
     * Checks the call of the onLeaveCollider when the Collision is no longer happening only once
     *
     * <p>the collision between A and B was brocken up in between CollisionSystem#update calls
     */
    @Test
    public void checkUpdateTwoEntitiesWithHitboxComponentNoLongerCollidingOnlyOnce() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(1, 2));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        e1.getComponent(PositionComponent.class)
                .map(PositionComponent.class::cast)
                .ifPresentOrElse(
                        x -> x.getPosition().x += 2,
                        () -> fail("PositionComponent not available and test not valid "));
        cs.update();
        cs.update();
        assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
        assertEquals("Only one interaction ends for e1", 1, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("Only one interaction ends for e2", 1, sc2OnLeave.getCount());
        assertEquals("No interaction begins for  e3", 0, sc3OnEnter.getCount());
        assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }

    /**
     * Checks if an Entity can collide Multiple Times
     *
     * <p>E1 collides with e1 and e3 while e2 and e3 do not
     */
    @Test
    public void checkUpdateCollisionNotBlockingOtherCollisions() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(.7f, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(-.7f, 0));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        cs.update();
        assertEquals("Two interactions begin for e1", 2, sc1OnEnter.getCount());
        assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
        assertEquals("Only one interaction begins for e3", 1, sc3OnEnter.getCount());
        assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }

    /**
     * Checks if an Entity can stop colliding with one Entity
     *
     * <p>on first update e1 collides with e1 and e3 while e2 and e3 do not on the second update e1
     * stops colliding with e3
     */
    @Test
    public void checkUpdateCollisionNotCallingEveryOnLeaveCollider() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(.7f, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(-.7f, 0));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        e1.getComponent(PositionComponent.class)
                .map(PositionComponent.class::cast)
                .ifPresentOrElse(
                        x -> x.getPosition().x += 1, () -> fail(MISSING_POSITION_COMPONENT));
        cs.update();
        assertEquals("Two interactions begin for e1", 2, sc1OnEnter.getCount());
        assertEquals("No interaction ends for e1", 1, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
        assertEquals("Only one interaction begins for e3", 1, sc3OnEnter.getCount());
        assertEquals("One interaction ends for e3", 1, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }

    /** Checks if all Entity can stop colliding with each other */
    @Test
    public void checkUpdateCollisionCallingEveryOnLeaveCollider() {
        prepareEnvironment();
        CollisionSystem cs = new CollisionSystem();
        Entity e1 = prepareEntityWithPosition(new Point(0, 0));
        SimpleCounter sc1OnEnter = new SimpleCounter();
        SimpleCounter sc1OnLeave = new SimpleCounter();
        new HitboxComponent(
                e1,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc1OnEnter.inc(),
                (a, b, c) -> sc1OnLeave.inc());
        Entity e2 = prepareEntityWithPosition(new Point(.7f, 0));
        SimpleCounter sc2OnEnter = new SimpleCounter();
        SimpleCounter sc2OnLeave = new SimpleCounter();
        new HitboxComponent(
                e2,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc2OnEnter.inc(),
                (a, b, c) -> sc2OnLeave.inc());
        Entity e3 = prepareEntityWithPosition(new Point(-.7f, 0));
        SimpleCounter sc3OnEnter = new SimpleCounter();
        SimpleCounter sc3OnLeave = new SimpleCounter();
        new HitboxComponent(
                e3,
                new Point(0, 0),
                new Point(1, 1),
                (a, b, c) -> sc3OnEnter.inc(),
                (a, b, c) -> sc3OnLeave.inc());

        cs.update();
        e1.getComponent(PositionComponent.class)
                .map(PositionComponent.class::cast)
                .ifPresentOrElse(
                        x -> x.getPosition().y += 2, () -> fail(MISSING_POSITION_COMPONENT));

        cs.update();
        assertEquals("Two interactions begin for e1", 2, sc1OnEnter.getCount());
        assertEquals("Two interactions end for e1", 2, sc1OnLeave.getCount());
        assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
        assertEquals("One interaction ends for e2", 1, sc2OnLeave.getCount());
        assertEquals("Only one interaction begins for e3", 1, sc3OnEnter.getCount());
        assertEquals("One interaction ends for e3", 1, sc3OnLeave.getCount());
        cleanUpEnvironment();
    }
}
