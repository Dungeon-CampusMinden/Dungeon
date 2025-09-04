package contrib.entities;

import contrib.components.*;
import contrib.utils.components.health.DamageType;
import core.Game;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Defines and build Monster-Entities for the Dungeon. */
public final class DungeonMonster {
  private static final Logger LOGGER = Logger.getLogger(DungeonMonster.class.getName());

  /**
   * A small, mischievous demon. Fast and light, deals low damage, and can enter open pits. Has a
   * high-pitched death sound and random fight/idle AI.
   */
  public static MonsterBuilder<?> IMP() {
    return createImp();
  }

  private static MonsterBuilder<?> createImp() {
    return new MonsterBuilder<>()
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
      .damageType(DamageType.PHYSICAL);
  }

  /**
   * A slow-moving undead. Relatively low health but moderate mass. Cannot enter open pits. Emits a
   * low-pitched death sound and has random fight/idle AI.
   */
  public static MonsterBuilder<?> ZOMBIE() {
    return createZombie();
  }

  private static MonsterBuilder<?> createZombie() {
    return new MonsterBuilder<>()
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
      .damageType(DamageType.PHYSICAL);
  }

  /**
   * A large, powerful monster. Slow but deals high collision damage. Cannot enter open pits. Emits
   * a lower-pitched death sound and uses random fight/idle AI.
   */
  public static MonsterBuilder<?> OGRE() {
    return createOgre();
  }

  private static MonsterBuilder<?> createOgre() {
    return new MonsterBuilder<>()
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
      .damageType(DamageType.PHYSICAL);
  }

  /**
   * A small, agile goblin. Fast and light, deals low collision damage. Cannot enter open pits. Uses
   * basic death and idle sounds with random fight/idle AI.
   */
  public static MonsterBuilder<?> GOBLIN() {
    return createGoblin();
  }

  private static MonsterBuilder<?> createGoblin() {
    return new MonsterBuilder<>()
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
      .damageType(DamageType.PHYSICAL);
  }

  /**
   * An undead ice-themed monster. Moderate speed and health. Cannot enter open pits. Emits a
   * lower-pitched death sound and uses random fight/idle AI.
   */
  public static MonsterBuilder<?> ICE_ZOMBIE() {
    return createIceZombie();
  }

  private static MonsterBuilder<?> createIceZombie() {
    return new MonsterBuilder<>()
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
      .damageType(DamageType.PHYSICAL);
  }

  /**
   * A magical orc shaman. Moderate speed and health, cannot enter open pits. Uses basic death
   * sound, lower-pitched idle sound, and random fight/idle AI.
   */
  public static MonsterBuilder<?> ORC_SHAMAN() {
    return createOrcShaman();
  }

  private static MonsterBuilder<?> createOrcShaman() {
    return new MonsterBuilder<>()
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
      .damageType(DamageType.PHYSICAL);
  }

  /**
   * Returns a random MonsterBuilder from this class.
   *
   * <p>This class uses reflection to get all public static no-arg methods that return
   * MonsterBuilder and returns a fresh builder from one chosen at random.
   *
   * @return a random MonsterBuilder
   */
  public static MonsterBuilder<?> RANDOM() {
    List<MonsterBuilder<?>> builders = new ArrayList<>();
    for (Method method : DungeonMonster.class.getDeclaredMethods()) {
      int mods = method.getModifiers();
      // public static no-arg methods returning MonsterBuilder
      if (Modifier.isStatic(mods)
        && Modifier.isPublic(mods)
        && method.getParameterCount() == 0
        && MonsterBuilder.class.equals(method.getReturnType())
        && !method.getName().equals("RANDOM")) {
        try {
          Object result = method.invoke(null);
          if (result instanceof MonsterBuilder) {
            builders.add((MonsterBuilder<?>) result);
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          LOGGER.log(Level.SEVERE, "Failed to invoke method: " + method.getName(), e);
        }
      }
    }
    if (builders.isEmpty()) {
      throw new RuntimeException("No MonsterBuilders found in DungeonMonster class.");
    }
    return builders.get(MonsterBuilder.RANDOM.nextInt(builders.size()));
  }

  private DungeonMonster() {
  }
}
