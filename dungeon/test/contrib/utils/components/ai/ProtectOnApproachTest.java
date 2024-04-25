package contrib.utils.components.ai;

import contrib.components.AIComponent;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.ProtectOnApproach;
import contrib.utils.components.ai.transition.RangeTransition;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import org.junit.Before;

/** WTF? . */
public class ProtectOnApproachTest {
  private final Point pointOfProtect = new Point(0, 0);
  private Entity entity;
  private AIComponent entityAI;
  private Entity protectedEntity;
  private Entity hero;

  /** WTF? . */
  @Before
  public void setup() {

    // Protected Entity
    protectedEntity = new Entity();

    // Add AI Component
    AIComponent protectedAI =
        new AIComponent(new CollideAI(0.2f), new RadiusWalk(0, 50), new RangeTransition(2));
    entity.add(protectedAI);

    // Add Position Component
    entity.add(new PositionComponent(pointOfProtect));

    // Protecting Entity
    entity = new Entity();

    // Add AI Component
    entityAI =
        new AIComponent(
            new CollideAI(0.2f), new RadiusWalk(0, 50), new ProtectOnApproach(2f, protectedEntity));
    entity.add(entityAI);

    // Add Position Component
    entity.add(new PositionComponent(new Point(0f, 0f)));

    // Hero
    hero = Game.hero().orElse(new Entity());
  }
}
