package dungine.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import dungine.components.HealthComponent;
import dungine.util.health.DamageType;
import dungine.util.health.IHealthObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class HealthSystem extends System<HealthSystem> {

  protected final List<IHealthObserver> observers = new ArrayList<>();

  /** Create a new HealthSystem. */
  public HealthSystem() {
    super(1);
  }

  @Override
  public void update(ECS ecs) {
    // filter entities for components and partition into alive and dead
    Map<Boolean, List<HSData>> deadOrAlive = new HashMap<>();
    ecs.forEachEntity(e -> {
      e.component(HealthComponent.class).ifPresent(hc -> {
        HSData hsd = new HSData(e, hc);
        deadOrAlive.computeIfAbsent(hc.isDead(), k -> new ArrayList<>()).add(hsd);
      });
    });

    // apply damage to all entities which are still alive
    if(deadOrAlive.containsKey(false)) {
      deadOrAlive.get(false).forEach(this::applyDamage);
    }

    // handle dead entities
    if(deadOrAlive.containsKey(true)) {
      deadOrAlive.get(true).forEach(hsd -> {
        ecs.removeEntity(hsd.e);
      });
    }
  }

  protected HSData applyDamage(final HSData hsd) {
    int dmgAmount = this.calculateDamage(hsd);

    // reset all damage objects in health component and apply damage
    hsd.hc.clearDamage();
    hsd.hc.currentHealthpoints(hsd.hc.currentHealthpoints() - dmgAmount);
    this.observers.forEach(observer -> observer.onHealthEvent(hsd, IHealthObserver.HealthEvent.DAMAGE));

    // return data object to enable method chaining/streaming
    return hsd;
  }

  protected int calculateDamage(final HSData hsd) {
    return Stream.of(DamageType.values()).mapToInt(hsd.hc()::calculateDamageOf).sum();
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

  protected void removeDeadEntities(final HSData hsd) {
    // Entity appears to be dead, so let's clean up the mess
    hsd.hc.triggerOnDeath(hsd.e);
    this.observers.forEach(observer -> observer.onHealthEvent(hsd, IHealthObserver.HealthEvent.DEATH));

    //TODO: Remove entity from game
  }

  /**
   * Record class to store the data of an entity with HealthComponent and DrawComponent.
   *
   * @param e The entity that owns the components
   * @param hc The HealthComponent of the entity
   */
  public record HSData(Entity e, HealthComponent hc) {}

}
