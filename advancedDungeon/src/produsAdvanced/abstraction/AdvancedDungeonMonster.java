package produsAdvanced.abstraction;

import contrib.entities.MonsterBuilder;
import contrib.entities.MonsterDeathSound;
import contrib.entities.MonsterIdleSound;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.utils.components.health.DamageType;
import java.util.*;

/** Monster for the AdvancedDungeon. */
public class AdvancedDungeonMonster {

  /**
   * Elemental monster.
   *
   * @return a builder for an elemental monster
   */
  public static MonsterBuilder<?> ELEMENTAL() {
    return createElemental();
  }

  private static MonsterBuilder<?> createElemental() {
    return new MonsterBuilder<>()
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
        .damageType(DamageType.PHYSICAL);
  }

  /**
   * A non moving chort.
   *
   * @return a builder for a static chort monster
   */
  public static MonsterBuilder<?> STATIC_CHORT() {
    return createStaticChort();
  }

  private static MonsterBuilder<?> createStaticChort() {
    return new MonsterBuilder<>()
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
        .damageType(DamageType.PHYSICAL);
  }

  /**
   * A small red imp.
   *
   * @return a builder for an imp monster
   */
  public static MonsterBuilder<?> IMP() {
    return createImp();
  }

  private static MonsterBuilder<?> createImp() {
    return new MonsterBuilder<>()
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
        .damageType(DamageType.PHYSICAL);
  }

  /**
   * Dr. acula.
   *
   * @return a builder for a doc monster
   */
  public static MonsterBuilder<?> DOC() {
    return createDoc();
  }

  private static MonsterBuilder<?> createDoc() {
    return new MonsterBuilder<>()
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
        .damageType(DamageType.PHYSICAL);
  }

  /**
   * Looks familiar.
   *
   * @return a builder for a goblin monster
   */
  public static MonsterBuilder<?> GOBLIN() {
    return createGoblin();
  }

  private static MonsterBuilder<?> createGoblin() {
    return new MonsterBuilder<>()
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
        .damageType(DamageType.PHYSICAL);
  }

  private AdvancedDungeonMonster() {}
}
