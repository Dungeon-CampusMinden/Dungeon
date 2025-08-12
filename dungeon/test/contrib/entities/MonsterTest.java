package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** WTF? . */
public class MonsterTest {
  /** WTF? . */
  @BeforeEach
  public void setup() {
    Game.add(new LevelSystem(() -> {}));
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** WTF? . */
  @Test
  public void checkCreation() throws IOException {
    Game.currentLevel(
        new DungeonLevel(
            new LevelElement[][] {
              new LevelElement[] {
                LevelElement.FLOOR,
              }
            },
            DesignLabel.DEFAULT));

    Game.add(HeroFactory.newHero());
    Entity m = EntityFactory.randomMonster();

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
