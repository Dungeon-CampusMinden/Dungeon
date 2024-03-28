package contrib.systems;

import contrib.components.HealthComponent;
import contrib.entities.IHealthObserver;
import contrib.utils.components.draw.AdditionalAnimations;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the death of an entity when the health-points have fallen below 0.
 *
 * <p>Entities with the {@link HealthComponent} and {@link DrawComponent} will be processed by this
 * system.
 */
public class HealthSystem extends System {
  protected final List<IHealthObserver> observers = new ArrayList<>();

  /** Create a new HealthSystem. */
  public HealthSystem() {
    super(HealthComponent.class, DrawComponent.class);
  }

  @Override
  public void execute() {
    entityStream()
        // Consider only entities that have a HealthComponent
        // Form triples (e, hc, dc)
        .map(this::buildDataObject)
        // Apply damage
        .map(this::applyDamage)
        // Filter all dead entities
        .filter(hsd -> hsd.hc.isDead())
        // Set DeathAnimation if possible and not yet set
        .map(this::activateDeathAnimation)
        // Filter by state of animation
        .filter(this::testDeathAnimationStatus)
        // Remove all dead entities
        .forEach(this::removeDeadEntities);
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
  protected boolean testDeathAnimationStatus(final HSData hsd) {
    DrawComponent dc = hsd.dc;
    // test if hsd has a DeathAnimation
    Predicate<DrawComponent> hasDeathAnimation =
        (drawComponent) -> drawComponent.hasAnimation(AdditionalAnimations.DIE);
    // test if Animation is looping
    Predicate<DrawComponent> isAnimationLooping = DrawComponent::isCurrentAnimationLooping;
    // test if Animation has finished playing
    Predicate<DrawComponent> isAnimationFinished = DrawComponent::isCurrentAnimationFinished;

    return !hasDeathAnimation.test(dc)
        || isAnimationLooping.test(dc)
        || isAnimationFinished.test(dc);
  }

  protected HSData activateDeathAnimation(final HSData hsd) {
    // set DeathAnimation as active animation
    Optional<Animation> deathAnimation = hsd.dc.animation(AdditionalAnimations.DIE);
    deathAnimation.ifPresent(
        animation -> hsd.dc.queueAnimation(animation.duration(), AdditionalAnimations.DIE));
    return hsd;
  }

  protected HSData buildDataObject(final Entity entity) {

    HealthComponent hc =
        entity
            .fetch(HealthComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, HealthComponent.class));
    DrawComponent ac =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
    return new HSData(entity, hc, ac);
  }

  protected HSData applyDamage(final HSData hsd) {
    doDamageAndAnimation(
        hsd, Stream.of(DamageType.values()).mapToInt(hsd.hc::calculateDamageOf).sum());
    return hsd;
  }

  protected void doDamageAndAnimation(final HSData hsd, final int dmgAmount) {
    if (dmgAmount > 0) {
      Optional<Animation> hitAnimation = hsd.dc.animation(AdditionalAnimations.HIT);
      // we have some damage - let's show a little dance
      hitAnimation.ifPresent(
          animation -> hsd.dc.queueAnimation(animation.duration(), AdditionalAnimations.HIT));
    }
    // reset all damage objects in health component and apply damage
    hsd.hc.clearDamage();
    hsd.hc.currentHealthpoints(hsd.hc.currentHealthpoints() - dmgAmount);
    this.observers.forEach(
        observer -> observer.onHeathEvent(hsd.e, hsd.hc, IHealthObserver.HealthEvent.DAMAGE));
  }

  protected void removeDeadEntities(final HSData hsd) {
    // Entity appears to be dead, so let's clean up the mess
    hsd.hc.triggerOnDeath(hsd.e);
    this.observers.forEach(
        observer -> observer.onHeathEvent(hsd.e, hsd.hc, IHealthObserver.HealthEvent.DEATH));
    Game.remove(hsd.e);
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
    this.observers.add(observer);
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
    this.observers.remove(observer);
  }

  // private record to hold all data during streaming
  protected record HSData(Entity e, HealthComponent hc, DrawComponent dc) {}
}
