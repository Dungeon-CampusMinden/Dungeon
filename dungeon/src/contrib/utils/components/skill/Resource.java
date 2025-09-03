package contrib.utils.components.skill;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.components.ManaComponent;
import contrib.components.StaminaComponent;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Enumeration of different resources that can be used and consumed by skills.
 *
 * <p>Each resource defines:
 *
 * <ul>
 *   <li>a supplier function that retrieves the current amount of that resource from an {@link
 *       Entity}, and
 *   <li>a consumer function that deducts the resource from the entity when a skill is executed.
 * </ul>
 *
 * <p>Available resources include:
 *
 * <ul>
 *   <li>{@link #HP} – the entity's health points
 *   <li>{@link #ARROW} – the entity's count of {@link ItemWoodenArrow}s in the inventory
 *   <li>{@link #MANA} – the entity's mana
 *   <li>{@link #STAMINA} – the entity's stamina or energy
 * </ul>
 */
public enum Resource {

  /**
   * Health points (HP).
   *
   * <p>Supplied from {@link HealthComponent#currentHealthpoints()}. Consuming HP applies damage to
   * the entity using {@link HealthComponent#receiveHit(Damage)} with type {@link
   * DamageType#LIFE_RESOURCE}.
   */
  HP(
      entity ->
          Float.valueOf(
              entity
                  .fetch(HealthComponent.class)
                  .map(HealthComponent::currentHealthpoints)
                  .orElse(0)),
      (entity, amount) ->
          entity
              .fetch(HealthComponent.class)
              .orElseThrow()
              .receiveHit(new Damage(amount, DamageType.LIFE_RESOURCE, entity))),

  /**
   * Arrows in the inventory.
   *
   * <p>Supplied from {@link InventoryComponent#count(Class)} with {@link ItemWoodenArrow}.
   * Consuming arrows removes them from the inventory.
   */
  ARROW(
      entity ->
          Float.valueOf(
              entity
                  .fetch(InventoryComponent.class)
                  .map(ic -> ic.count(ItemWoodenArrow.class))
                  .orElse(0)),
      (entity, amount) ->
          entity
              .fetch(InventoryComponent.class)
              .orElseThrow()
              .remove(ItemWoodenArrow.class, amount)),

  /**
   * Mana resource.
   *
   * <p>Supplied from {@link ManaComponent#currentAmount()} ()}. Consuming mana calls {@link
   * ManaComponent#consume(float)}.
   */
  MANA(
      entity ->
          entity.fetch(ManaComponent.class).map(ManaComponent::currentAmount).orElse((float) 0),
      (entity, amount) -> entity.fetch(ManaComponent.class).orElseThrow().consume(amount)),

  /**
   * Energy resource.
   *
   * <p>Supplied from {@link StaminaComponent#currentAmount()} ()}. Consuming stamina calls {@link
   * StaminaComponent#consume(float)}.
   */
  STAMINA(
      entity ->
          entity
              .fetch(StaminaComponent.class)
              .map(StaminaComponent::currentAmount)
              .orElse((float) 0),
      (entity, amount) -> entity.fetch(StaminaComponent.class).orElseThrow().consume(amount));

  /** Function that supplies the current resource amount from an entity. */
  private final Function<Entity, Float> supplier;

  /** Function that consumes the resource from an entity. */
  private final BiConsumer<Entity, Integer> consume;

  /**
   * Creates a new resource definition.
   *
   * @param supplier function that provides the current amount of the resource for an entity
   * @param consume function that consumes the resource from an entity
   */
  Resource(Function<Entity, Float> supplier, BiConsumer<Entity, Integer> consume) {
    this.supplier = supplier;
    this.consume = consume;
  }

  /**
   * Returns the current amount of this resource for the given entity.
   *
   * @param entity the entity to query
   * @return the current amount of the resource
   */
  public float apply(Entity entity) {
    return supplier.apply(entity);
  }

  /**
   * Consumes a given amount of this resource from the entity.
   *
   * @param entity the entity to consume from
   * @param amount the amount to consume
   */
  public void consume(Entity entity, Integer amount) {
    consume.accept(entity, amount);
  }
}
