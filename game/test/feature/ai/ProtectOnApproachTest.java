package feature.ai;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.ai.fight.AIChaseBehaviour;
import feature.ai.idle.RadiusWalk;
import feature.ai.transition.ProtectOnApproach;
import feature.ai.transition.RangeTransition;
import feature.components.AIComponent;
import org.junit.jupiter.api.BeforeEach;

/** WTF? . */
public class ProtectOnApproachTest {
  private final Point pointOfProtect = new Point(0, 0);
  private Entity entity;
  private AIComponent entityAI;
  private Entity protectedEntity;
  private Entity hero;

  /** WTF? . */
  @BeforeEach
  public void setup() {

    // Protected Entity
    protectedEntity = new Entity();

    // Add AI Component
    AIComponent protectedAI =
        new AIComponent(new AIChaseBehaviour(0.2f), new RadiusWalk(0, 50), new RangeTransition(2));
    entity.add(protectedAI);

    // Add Position Component
    entity.add(new PositionComponent(pointOfProtect));

    // Protecting Entity
    entity = new Entity();

    // Add AI Component
    entityAI =
        new AIComponent(
            new AIChaseBehaviour(0.2f),
            new RadiusWalk(0, 50),
            new ProtectOnApproach(2f, protectedEntity));
    entity.add(entityAI);

    // Add Position Component
    entity.add(new PositionComponent(new Point(0f, 0f)));

    // Hero
    hero = Game.player().orElse(new Entity());
  }
}
