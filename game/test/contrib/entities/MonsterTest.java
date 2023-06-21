package contrib.entities;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.InventoryComponent;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import contrib.components.HealthComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public class MonsterTest {

    /** Helper cleans up class attributes used by Chest Initializes the Item#ITEM_REGISTER */
    private static void cleanup() {
        Game.removeAllEntities();
    }

    /** checks the correct creation of the Chest */
    @Test
    public void checkCreation() throws IOException {
        cleanup();
        Point position = new Point(0, 0);
        Entity m = null;
        m = EntityFactory.getRandomizedMonster();

        Optional<Component> drawComponent = m.getComponent(DrawComponent.class);
        assertTrue(
            "Needs the DrawComponent to be visible to the player.",
            drawComponent.isPresent());

        Optional<Component> positionComponent = m.getComponent(PositionComponent.class);
        assertTrue(
            "Needs the PositionComponent to be somewhere in the Level",
            positionComponent.isPresent());
        PositionComponent pc = (PositionComponent) positionComponent.get();
        assertTrue(
            "Needs to spawn somewhere accessible",
            Game.currentLevel.getTileAt(pc.getPosition().toCoordinate()).isAccessible()
        );

        Optional<Component> HealthComponent = m.getComponent(HealthComponent.class);
        assertTrue(
            "Needs the HealthComponent to take damage",
            HealthComponent.isPresent());

        Optional<Component> AiComponent = m.getComponent(AIComponent.class);
        assertTrue(
            "Needs the AIComponent to collide with things",
            AiComponent.isPresent());
        AIComponent ai = (AIComponent) AiComponent.get();
        assertNotNull("The Ai needs a FightAI", ai.getFightAI());
        assertNotNull("The Ai needs an IdleAI", ai.getIdleAI());
        assertNotNull("The Ai needs a TransitionAI", ai.getTransitionAI());


        Optional<Component> collideComponent = m.getComponent(CollideComponent.class);
        assertTrue(
            "Needs the PositionComponent to be somewhere in the Level",
            collideComponent.isPresent());

        cleanup();
    }
}
