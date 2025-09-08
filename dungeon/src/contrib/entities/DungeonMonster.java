package contrib.entities;

import contrib.utils.components.health.DamageType;
import core.Game;
import java.util.*;
import java.util.function.Supplier;

/** Defines and build Monster-Entities for the Dungeon. */
public enum DungeonMonster {
  /**
   * A small, mischievous demon. Fast and light, deals low damage, and can enter open pits. Has a
   * high-pitched death sound and random fight/idle AI.
   */
  IMP(
      () ->
          new MonsterBuilder<>()
              .name("Imp")
              .speed(5.0f)
              .mass(1.0f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/imp")
              .health(3)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of())
              .dropChance(0.2f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.HIGH_PITCH)
              .fightAI(AIFactory::randomFightAI)
              .idleAI(AIFactory::randomIdleAI)
              .transitionAI(() -> (self) -> AIFactory.randomTransition(self).apply(self))
              .collideDamage(5)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /**
   * A slow-moving undead. Relatively low health but moderate mass. Cannot enter open pits. Emits a
   * low-pitched death sound and has random fight/idle AI.
   */
  ZOMBIE(
      () ->
          new MonsterBuilder<>()
              .name("Zombie")
              .speed(3.2f)
              .mass(1.3f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/big_zombie")
              .health(6)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOW_PITCH)
              .drops(Set.of())
              .dropChance(0.15f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(AIFactory::randomFightAI)
              .idleAI(AIFactory::randomIdleAI)
              .transitionAI(() -> (self) -> AIFactory.randomTransition(self).apply(self))
              .collideDamage(4)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /**
   * A large, powerful monster. Slow but deals high collision damage. Cannot enter open pits. Emits
   * a lower-pitched death sound and uses random fight/idle AI.
   */
  OGRE(
      () ->
          new MonsterBuilder<>()
              .name("Ogre")
              .speed(2.5f)
              .mass(2.5f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/ogre")
              .health(9)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.25f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(AIFactory::randomFightAI)
              .idleAI(AIFactory::randomIdleAI)
              .transitionAI(() -> (self) -> AIFactory.randomTransition(self).apply(self))
              .collideDamage(8)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /**
   * A small, agile goblin. Fast and light, deals low collision damage. Cannot enter open pits. Uses
   * basic death and idle sounds with random fight/idle AI.
   */
  GOBLIN(
      () ->
          new MonsterBuilder<>()
              .name("Goblin")
              .speed(4.2f)
              .mass(1.0f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/goblin")
              .health(4)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.BASIC)
              .drops(Set.of())
              .dropChance(0.18f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BASIC)
              .fightAI(AIFactory::randomFightAI)
              .idleAI(AIFactory::randomIdleAI)
              .transitionAI(() -> (self) -> AIFactory.randomTransition(self).apply(self))
              .collideDamage(3)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /**
   * An undead ice-themed monster. Moderate speed and health. Cannot enter open pits. Emits a
   * lower-pitched death sound and uses random fight/idle AI.
   */
  ICE_ZOMBIE(
      () ->
          new MonsterBuilder<>()
              .name("Ice Zombie")
              .speed(2.8f)
              .mass(1.3f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/ice_zombie")
              .health(6)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOW_PITCH)
              .drops(Set.of())
              .dropChance(0.20f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(AIFactory::randomFightAI)
              .idleAI(AIFactory::randomIdleAI)
              .transitionAI(() -> (self) -> AIFactory.randomTransition(self).apply(self))
              .collideDamage(4)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /**
   * A magical orc shaman. Moderate speed and health, cannot enter open pits. Uses basic death
   * sound, lower-pitched idle sound, and random fight/idle AI.
   */
  ORC_SHAMAN(
      () ->
          new MonsterBuilder<>()
              .name("Orc Shaman")
              .speed(3.4f)
              .mass(1.2f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/orc_shaman")
              .health(5)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.BASIC)
              .drops(Set.of())
              .dropChance(0.22f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(AIFactory::randomFightAI)
              .idleAI(AIFactory::randomIdleAI)
              .transitionAI(() -> (self) -> AIFactory.randomTransition(self).apply(self))
              .collideDamage(4)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL));

  /** Random instance for monsters. */
  public static Random RANDOM = new Random();

  private final Supplier<MonsterBuilder<?>> builderSupplier;

  /**
   * Constructor for DungeonMonster enum.
   *
   * @param builderSupplier A supplier that provides a new instance of MonsterBuilder for the enum
   *     constant.
   */
  DungeonMonster(Supplier<MonsterBuilder<?>> builderSupplier) {
    this.builderSupplier = builderSupplier;
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

  /**
   * Returns a random {@link DungeonMonster} from this enum.
   *
   * @return a random {@link DungeonMonster}
   */
  public static DungeonMonster randomMonster() {
    DungeonMonster[] builders = DungeonMonster.values();
    return builders[RANDOM.nextInt(builders.length)];
  }
}
