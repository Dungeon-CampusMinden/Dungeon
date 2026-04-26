package contrib.modules.levelhide;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import org.junit.jupiter.api.Test;

class LevelHideFactoryTest {

  @Test
  void createLevelHideAddsAllRequiredComponents() {
    Point bottomLeft = new Point(3f, 4f);

    Entity entity = LevelHideFactory.createLevelHide(bottomLeft, 4f, 3f, 1.5f);

    assertTrue(entity.isPresent(PositionComponent.class));
    assertTrue(entity.isPresent(LevelHideComponent.class));
    assertTrue(entity.isPresent(LevelHideStateComponent.class));

    PositionComponent position = entity.fetch(PositionComponent.class).orElseThrow();
    LevelHideComponent hide = entity.fetch(LevelHideComponent.class).orElseThrow();
    LevelHideStateComponent state = entity.fetch(LevelHideStateComponent.class).orElseThrow();

    assertEquals(bottomLeft, position.position());
    assertEquals(4f, hide.region().width());
    assertEquals(3f, hide.region().height());
    assertEquals(1.5f, hide.transitionSize());
    assertTrue(state.hiding());
  }
}
