package contrib.skill;

import contrib.components.EnergyComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.components.ManaComponent;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum Resource {
  HP(
      entity ->
          entity.fetch(HealthComponent.class).map(HealthComponent::currentHealthpoints).orElse(0),
      (entity, amount) ->
          entity
              .fetch(HealthComponent.class)
              .orElseThrow()
              .receiveHit(new Damage(amount, DamageType.SKILL, entity))),
  ARROW(
      entity ->
          entity
              .fetch(InventoryComponent.class)
              .map(ic -> ic.count(ItemWoodenArrow.class))
              .orElse(0),
      (entity, amount) ->
          entity
              .fetch(InventoryComponent.class)
              .orElseThrow()
              .remove(ItemWoodenArrow.class, amount)),
  MANA(
      entity -> entity.fetch(ManaComponent.class).map(ManaComponent::getCurrentAmount).orElse(0),
      (entity, amount) -> entity.fetch(ManaComponent.class).orElseThrow().consume(amount)),
  ENERGY(
      entity ->
          entity.fetch(EnergyComponent.class).map(EnergyComponent::getCurrentAmount).orElse(0),
      (entity, amount) -> entity.fetch(EnergyComponent.class).orElseThrow().consume(amount));

  private final Function<Entity, Integer> supplier;
  private final BiConsumer<Entity, Integer> consume;

  Resource(Function<Entity, Integer> supplier, BiConsumer<Entity, Integer> consume) {
    this.supplier = supplier;
    this.consume = consume;
  }

  public int apply(Entity entity) {
    return supplier.apply(entity);
  }

  public void consume(Entity entity, Integer amount) {
    consume.accept(entity, amount);
  }
}
