package contrib.components;

import contrib.systems.AISystem;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import core.Component;
import core.Entity;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Define the behavior of AI-controlled entities.
 *
 * <p>An AI-controlled entity can have two different states which define the behaviour of the
 * entity. The "idle state" describes the default behaviour of the entity, like walking around in
 * the level. The "combat state" describes the fighting behaviour, like throwing fireballs at the
 * hero. The {@link AISystem} will execute the correct behavior.
 *
 * <p>The {@link #idleBehavior} defines the behaviour in idle state, e.g. walking on a specific path
 * {@link PatrolWalk}.
 *
 * <p>The {@link #fightBehavior} defines the combat behaviour, e.g. attacking with a fireball skill
 * {@link contrib.utils.components.ai.fight.RangeAI}.
 *
 * <p>The {@link #shouldFight} defines when the entity goes into fight mode, e.g. if the player is
 * too close to the entity {@link RangeTransition}.
 *
 * @see AISystem
 */
public final class AIComponent implements Component {
  private final Consumer<Entity> fightBehavior;
  private final Consumer<Entity> idleBehavior;
  private final Function<Entity, Boolean> shouldFight;
  private boolean active = true;

  /**
   * Create an AIComponent with the given behavior.
   *
   * @param fightBehavior The combat behavior.
   * @param idleBehavior The idle behavior.
   * @param shouldFight Determines when to fight.
   */
  public AIComponent(
      final Consumer<Entity> fightBehavior,
      final Consumer<Entity> idleBehavior,
      final Function<Entity, Boolean> shouldFight) {
    this.fightBehavior = fightBehavior;
    this.idleBehavior = idleBehavior;
    this.shouldFight = shouldFight;
  }

  /**
   * Create an AIComponent with default behavior.
   *
   * <p>The default behavior uses {@link RadiusWalk} as the idle behavior, {@link RangeTransition}
   * as the transition function, and {@link CollideAI} as the fight behavior.
   */
  public AIComponent() {
    this(new CollideAI(2f), new RadiusWalk(5, 2), new RangeTransition(5f));
  }

  /**
   * Get the function that decides if the fight behavior should be executed.
   *
   * @return Transition function between idle and fight behavior.
   */
  public Function<Entity, Boolean> shouldFight() {
    if (!this.active) {
      return (entity) -> false;
    }
    return this.shouldFight;
  }

  /**
   * Get the function to execute for fighting.
   *
   * @return Function that implements the fight behavior.
   */
  public Consumer<Entity> fightBehavior() {
    return this.fightBehavior;
  }

  /**
   * Get the function to execute for idle.
   *
   * @return Function that implements the idle behavior.
   */
  public Consumer<Entity> idleBehavior() {
    if (!this.active) {
      return (entity) -> {};
    }
    return this.idleBehavior;
  }

  /**
   * Set the active state of the AI.
   *
   * @param active The new active state.
   */
  public void active(boolean active) {
    this.active = active;
  }

  /**
   * Get the active state of the AI.
   *
   * @return The current active state.
   */
  public boolean active() {
    return this.active;
  }
}
