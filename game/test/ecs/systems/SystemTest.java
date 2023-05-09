package ecs.systems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import trashcan.SystemController;
import org.junit.Before;
import org.junit.Test;
import starter.Game;
import api.System;

public class SystemTest {

    private System testSystem;
    private int updates;

    @Before
    public void setup() {
        updates = 0;
        Game.systems = new SystemController();
        testSystem =
                new System() {
                    @Override
                    public void update() {
                        updates++;
                    }
                };
    }

    @Test
    public void cTor() {
        assertTrue(Game.systems.contains(testSystem));
    }

    @Test
    public void pause() {
        assertEquals(0, updates);
        Game.systems.update();
        assertEquals(1, updates);
        testSystem.toggleRun();
        Game.systems.update();
        assertEquals(1, updates);
        testSystem.toggleRun();
        Game.systems.update();
        assertEquals(2, updates);
    }
}
