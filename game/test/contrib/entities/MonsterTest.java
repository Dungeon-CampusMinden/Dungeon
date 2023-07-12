package contrib.entities;

import static org.junit.Assert.assertTrue;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

public class MonsterTest {
    @Before
    public void setup() {
        new LevelSystem(null, null, () -> {});
    }

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.currentLevel(null);
        Game.removeAllSystems();
    }

    @Test
    public void checkCreation() throws IOException {
        Game.currentLevel(
                new TileLevel(
                        new LevelElement[][] {
                            new LevelElement[] {
                                LevelElement.FLOOR,
                            }
                        },
                        DesignLabel.DEFAULT));

        Game.hero(EntityFactory.newHero());
        Entity m = EntityFactory.randomMonster();

        Optional<DrawComponent> drawComponent = m.fetch(DrawComponent.class);
        assertTrue("Entity needs the DrawComponent.", drawComponent.isPresent());

        Optional<PositionComponent> positionComponent = m.fetch(PositionComponent.class);
        assertTrue("Entity needs the PositionComponent.", positionComponent.isPresent());
        PositionComponent pc = positionComponent.get();
        assertTrue(
                "Entity needs to spawn somewhere accessible",
                Game.currentLevel().tileAt(pc.position().toCoordinate()).isAccessible());

        Optional<HealthComponent> HealthComponent = m.fetch(HealthComponent.class);
        assertTrue("Entity needs the HealthComponent to take damage", HealthComponent.isPresent());

        Optional<AIComponent> AiComponent = m.fetch(AIComponent.class);
        assertTrue("Entity needs the AIComponent to collide with things", AiComponent.isPresent());

        Optional<CollideComponent> collideComponent = m.fetch(CollideComponent.class);
        assertTrue(
                "Entity needs the CollideComponent to collide with things",
                collideComponent.isPresent());
    }
}
