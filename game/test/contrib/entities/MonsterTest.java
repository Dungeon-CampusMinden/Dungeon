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

        Optional<DrawComponent> drawComponent = m.fetch(DrawComponent.class);
        assertTrue(
            "Entity needs the DrawComponent.",
            drawComponent.isPresent());

        Optional<PositionComponent> positionComponent = m.fetch(PositionComponent.class);
        assertTrue(
            "Entity needs the PositionComponent.",
            positionComponent.isPresent());
        PositionComponent pc = positionComponent.get();
        assertTrue(
            "Entity needs to spawn somewhere accessible",
            Game.currentLevel().tileAt(pc.position().toCoordinate()).isAccessible()
        );

        Optional<HealthComponent> HealthComponent = m.fetch(HealthComponent.class);
        assertTrue(
            "Entity needs the HealthComponent to take damage",
            HealthComponent.isPresent());

        Optional<AIComponent> AiComponent = m.fetch(AIComponent.class);
        assertTrue(
            "Entity needs the AIComponent to collide with things",
            AiComponent.isPresent());
        AIComponent ai = (AIComponent) AiComponent.get();
        assertNotNull("The Ai needs a FightAI", ai.fightAI());
        assertNotNull("The Ai needs an IdleAI", ai.idleAI());
        assertNotNull("The Ai needs a TransitionAI", ai.transitionAI());


        Optional<CollideComponent> collideComponent = m.fetch(CollideComponent.class);
        assertTrue(
            "Entity needs the CollideComponent to collide with things",
            collideComponent.isPresent());

        cleanup();
    }
}
