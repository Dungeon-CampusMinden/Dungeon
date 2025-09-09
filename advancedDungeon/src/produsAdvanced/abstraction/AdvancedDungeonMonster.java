package produsAdvanced.abstraction;

import contrib.entities.MonsterBuilder;
import contrib.entities.MonsterDeathSound;
import contrib.entities.MonsterIdleSound;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.utils.components.health.DamageType;
import java.util.*;
import java.util.function.Supplier;

/** Monster for the AdvancedDungeon. */
public enum AdvancedDungeonMonster {
  /** Elemental monster. */
  ELEMENTAL(
      () ->
          new MonsterBuilder<>()
              .name("Monster Elemental")
              .speed(5f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/elemental_goo_small")
              .health(4)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of())
              .dropChance(0f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> (entity) -> {})
              .idleAI(() -> (entity) -> {})
              .transitionAI(() -> (entity) -> false)
              .collideDamage(0)
              .collideCooldown(0)
              .damageType(DamageType.PHYSICAL)),
  /** A non moving chort. */
  STATIC_CHORT(
      () ->
          new MonsterBuilder<>()
              .name("Static Chort")
              .speed(0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/chort")
              .health(1)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.1f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> (entity) -> {})
              .idleAI(() -> (entity) -> {})
              .transitionAI(() -> (entity) -> false)
              .collideDamage(0)
              .collideCooldown(0)
              .damageType(DamageType.PHYSICAL)),
  /** A small red imp. */
  IMP(
      () ->
          new MonsterBuilder<>()
              .name("Imp")
              .speed(4f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/imp")
              .health(5)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of(new ItemPotionHealth()))
              .dropChance(0.1f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> (entity) -> {})
              .idleAI(() -> (entity) -> {})
              .transitionAI(() -> (entity) -> false)
              .collideDamage(0)
              .collideCooldown(0)
              .damageType(DamageType.PHYSICAL)),
  /** Dr. acula. */
  DOC(
      () ->
          new MonsterBuilder<>()
              .name("Doc")
              .speed(4f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/doc")
              .health(5)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of())
              .dropChance(0.0f)
              .guaranteedDrops(Set.of(new ItemPotionHealth()))
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> (entity) -> {})
              .idleAI(() -> (entity) -> {})
              .transitionAI(() -> (entity) -> false)
              .collideDamage(0)
              .collideCooldown(0)
              .damageType(DamageType.PHYSICAL)),
  /** Looks familiar. */
  GOBLIN(
      () ->
          new MonsterBuilder<>()
              .name("Goblin")
              .speed(4f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/goblin")
              .health(5)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of(new ItemPotionWater()))
              .dropChance(0.0f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> (entity) -> {})
              .idleAI(() -> (entity) -> {})
              .transitionAI(() -> (entity) -> false)
              .collideDamage(0)
              .collideCooldown(0)
              .damageType(DamageType.PHYSICAL));

  private final Supplier<MonsterBuilder<?>> builderSupplier;

  AdvancedDungeonMonster(Supplier<MonsterBuilder<?>> builder) {
    this.builderSupplier = builder;
  }

  /**
   * Returns a new {@link MonsterBuilder} for this enum constant.
   *
   * <p>Each call to this method returns a new instance of {@link MonsterBuilder}, allowing for
   * independent configurations.
   *
   * @return a new {@link MonsterBuilder} instance
   */
  public MonsterBuilder<?> builder() {
    return builderSupplier.get();
  }
}
