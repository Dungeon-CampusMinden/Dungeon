package contrib.systems;

import contrib.components.HealthComponent;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.health.IHealthObserver;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the {@link HealthComponent#triggerOnDeath(Entity)} of an entity when the health-points have
 * fallen below 0.
 *
 * <p>Entities with the {@link HealthComponent} and {@link DrawComponent} will be processed by this
 * system.
 */
public class HealthSystem extends System {
  protected final List<IHealthObserver> observers = new ArrayList<>();

  private static final String DEATH_STATE = "dead";
  private static final String DEATH_SIGNAL = "die";
  private static final String DAMAGE_SIGNAL = "hit";

  /** Create a new HealthSystem. */
  public HealthSystem() {
    super(HealthComponent.class, DrawComponent.class);
  }

  @Override
  public void execute() {
    // filter entities for components and partition into alive and dead
    Map<Boolean, List<HSData>> deadOrAlive =
        filteredEntityStream(HealthComponent.class, DrawComponent.class)
            .map(
                e ->
                    new HSData(
                        e,
                        e.fetch(HealthComponent.class).orElseThrow(),
                        e.fetch(DrawComponent.class).orElseThrow()))
            .collect(Collectors.partitioningBy(hsd -> hsd.hc.isDead()));

    // apply damage to all entities which are still alive
    deadOrAlive.get(false).forEach(this::applyDamage);

    // handle dead entities
    deadOrAlive.get(true).stream()
        .map(this::activateDeathAnimation)
        .filter(this::isDeathAnimationFinished)
        .forEach(this::triggerOnDeath);
  }

  protected HSData applyDamage(final HSData hsd) {
    int dmgAmount = calculateDamage(hsd);

    // if we have some damage, let's show a little dance
    if (dmgAmount > 0) hsd.dc.sendSignal(DAMAGE_SIGNAL);

    // reset all damage objects in health component and apply damage
    hsd.hc.clearDamage();
    hsd.hc.currentHealthpoints(hsd.hc.currentHealthpoints() - dmgAmount);
    observers.forEach(observer -> observer.onHealthEvent(hsd, IHealthObserver.HealthEvent.DAMAGE));

    // return data object to enable method chaining/streaming
    return hsd;
  }

  protected int calculateDamage(final HSData hsd) {
    return Stream.of(DamageType.values()).mapToInt(hsd.hc()::calculateDamageOf).sum();
  }

  protected HSData activateDeathAnimation(final HSData hsd) {
    hsd.e
        .fetch(PositionComponent.class)
        .ifPresentOrElse(
            pc -> hsd.dc.sendSignal(DEATH_SIGNAL, pc.viewDirection()),
            () -> hsd.dc.sendSignal(DEATH_SIGNAL));
    return hsd;
  }

  /**
   * Tests the existence and current status of the DeathAnimation of an entity.
   *
   * <p>Returns a corresponding Boolean value if the entity can be removed from the game. This is
   * the case if the entity does not have a death animation, or it has already finished. Also, if
   * the entity has a death animation, and it is in loop mode, the entity will be marked for
   * removal.
   *
   * @param hsd HSData to check Animations in.
   * @return true if Entity can be removed from the game.
   */
  protected boolean isDeathAnimationFinished(final HSData hsd) {
    // test if hsd has a DeathAnimation
    Predicate<DrawComponent> hasDeathAnimation =
        (drawComponent) -> drawComponent.hasState(DEATH_STATE);
    // test if Animation is looping
    Predicate<DrawComponent> isAnimationLooping = DrawComponent::isCurrentAnimationLooping;
    // test if Animation has finished playing
    Predicate<DrawComponent> isAnimationFinished = DrawComponent::isCurrentAnimationFinished;
    Predicate<DrawComponent> currentAnimationIsDeath =
        drawComponent -> hsd.dc().stateMachine().getCurrentStateName().equals(DEATH_STATE);

    return !hasDeathAnimation.test(hsd.dc)
        || (currentAnimationIsDeath.test(hsd.dc) && isAnimationLooping.test(hsd.dc))
        || (currentAnimationIsDeath.test(hsd.dc) && isAnimationFinished.test(hsd.dc));
  }

  /**
   * Registers an observer to the HealthSystem.
   *
   * <p>This method adds an observer to the list of observers that are notified of health events.
   * The observer must implement the IHealthObserver interface.
   *
   * @param observer The observer to be registered.
   * @see IHealthObserver
   */
  public void registerObserver(IHealthObserver observer) {
    observers.add(observer);
  }

  /**
   * Removes an observer from the HealthSystem.
   *
   * <p>This method removes an observer from the list of observers that are notified of health
   * events. If the observer is not in the list, the method has no effect.
   *
   * @param observer The observer to be removed.
   * @see IHealthObserver
   */
  public void removeObserver(IHealthObserver observer) {
    observers.remove(observer);
  }

  protected void triggerOnDeath(final HSData hsd) {
    observers.forEach(observer -> observer.onHealthEvent(hsd, IHealthObserver.HealthEvent.DEATH));
    hsd.hc.triggerOnDeath(hsd.e);
  }

  /**
   * Record class to store the data of an entity with HealthComponent and DrawComponent.
   *
   * @param e The entity that owns the components
   * @param hc The HealthComponent of the entity
   * @param dc The DrawComponent of the entity
   */
  public record HSData(Entity e, HealthComponent hc, DrawComponent dc) {}
}
