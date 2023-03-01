package ecs.systems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import controller.SystemController;
import org.junit.Before;
import org.junit.Test;
import starter.ECS;

public class ECS_SystemTest {

    private ECS_System testSystem;
    private int updates;

    @Before
    public void setup() {
        updates = 0;
        ECS.systems = new SystemController();
        testSystem =
                new ECS_System() {
                    @Override
                    public void update() {
                        updates++;
                    }
                };
    }

    @Test
    public void cTor() {
        assertTrue(ECS.systems.contains(testSystem));
    }

    @Test
    public void pause() {
        assertEquals(0, updates);
        ECS.systems.update();
        assertEquals(1, updates);
        testSystem.toggleRun();
        ECS.systems.update();
        assertEquals(1, updates);
        testSystem.toggleRun();
        ECS.systems.update();
        assertEquals(2, updates);
    }
}
