package feature.entities;

import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.systems.LevelSystem;
import engine.utils.Point;
import feature.components.AIComponent;
import feature.components.CollideComponent;
import feature.components.HealthComponent;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the MonsterBuilder and the DungeonMonster class. */
public class MonsterTest {
  /** Setup a LevelSystem before each test. */
  @BeforeEach
  public void setup() {
    Game.add(new LevelSystem());
  }

  /** Cleanup after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** Tests the creation of a DungeonMonster. */
  @Test
  public void checkCreation() {
    Game.currentLevel(
        new DungeonLevel(
            new LevelElement[][] {
              new LevelElement[] {
                LevelElement.FLOOR,
              }
            },
            DesignLabel.DEFAULT));

    Entity m = DungeonMonster.randomMonster().builder().build(new Point(0, 0));
    Optional<DrawComponent> drawComponent = m.fetch(DrawComponent.class);
    assertTrue(drawComponent.isPresent());

    Optional<PositionComponent> positionComponent = m.fetch(PositionComponent.class);
    assertTrue(positionComponent.isPresent());
    PositionComponent pc = positionComponent.get();

    Optional<HealthComponent> HealthComponent = m.fetch(HealthComponent.class);
    assertTrue(HealthComponent.isPresent());

    Optional<AIComponent> AiComponent = m.fetch(AIComponent.class);
    assertTrue(AiComponent.isPresent());

    Optional<CollideComponent> collideComponent = m.fetch(CollideComponent.class);
    assertTrue(collideComponent.isPresent());
  }
}
