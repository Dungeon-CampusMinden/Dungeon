package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;
import core.Entity;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Implements an AI that protects a specific entity with a {@link HealthComponent} if the hero dealt
 * damage to it.
 *
 * <p>Entity will stay in fight mode once entered.
 */
public final class ProtectOnAttack implements Function<Entity, Boolean> {
  private final Collection<Entity> setupProtection;
  private final Set<Entity> toProtect = new HashSet<>();
  boolean setup = true;
  private boolean isInFight = false;

  /**
   * Constructor when only one entity is to protect.
   *
   * @param entity Entity which will be protected.
   */
  public ProtectOnAttack(final Entity entity) {
    this(Set.of(entity));
  }

  /**
   * Constructor for a list of entities to protect.
   *
   * <p>Checks if an entity with a {@link HealthComponent} is present and adds it to the list of
   * entities to protect.
   *
   * @param entities Entities that are protected.
   */
  public ProtectOnAttack(final Collection<Entity> entities) {
    this.setupProtection = entities;
  }

  /**
   * If the entity which caused the last damage to an entity to protect has a {@link
   * PlayerComponent}, switch to fight mode.
   *
   * @param entity Entity which protects.
   * @return true if entity is in fight mode, false if entity is not.
   */
  @Override
  public Boolean apply(final Entity entity) {
    if (setup) doSetup();
    if (isInFight) return true;

    isInFight =
        toProtect.stream()
            .map(
                toProtect ->
                    toProtect
                        .fetch(HealthComponent.class)
                        .orElseThrow(
                            () ->
                                MissingComponentException.build(toProtect, HealthComponent.class)))
            .anyMatch(
                toProtect ->
                    toProtect
                        .lastDamageCause()
                        .map(causeEntity -> causeEntity.fetch(PlayerComponent.class))
                        .isPresent());

    return isInFight;
  }

  private void doSetup() {
    // add every entity with a HealthComponent to the protection list
    // throw an exception for every entity that does not have a HealthComponent
    for (Entity entity : setupProtection) {
      if (entity.isPresent(HealthComponent.class)) toProtect.add(entity);
      else throw MissingComponentException.build(entity, HealthComponent.class);
    }
    setup = false;
  }
}
