package entities;

import components.ReviveComponent;
import contrib.components.*;
import contrib.entities.*;
import contrib.hud.DialogUtils;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import contrib.utils.components.ai.fight.AIRangeBehaviour;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import contrib.utils.components.skill.projectileSkill.TPBallSkill;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import core.utils.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import level.utils.LevelUtils;
import task.tasktype.Quiz;

/**
 * Class representing the different types of monsters in DevDungeon.
 *
 * <p>Each type of monster has:
 *
 * <ul>
 *   <li>A name
 *   <li>A texture path
 *   <li>A health value
 *   <li>A speed value
 *   <li>A chance to drop an item
 *   <li>A death sound
 *   <li>A fight AI
 *   <li>An idle AI
 *   <li>A transition AI
 *   <li>A collide damage value
 *   <li>A collide cooldown value
 *   <li>An idle sound path
 *   <li>A revive count
 * </ul>
 *
 * <p>Each monster type can be built into an entity using the {@link Builder}.
 */
public enum DevDungeonMonster {
  /** A Chort monster. Slow but tanky and strong. */
  CHORT(
      () ->
          new Builder()
              .name("Chort")
              .speed(2.5f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/chort")
              .health(16)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.33f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(() -> new AIChaseBehaviour(0.5f))
              .idleAI(() -> new RadiusWalk(2f, 2))
              .transitionAI(() -> new RangeTransition(5))
              .collideDamage(7)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** An Imp monster. Fast, weak but annoying. */
  IMP(
      () ->
          new Builder()
              .name("Imp")
              .speed(5.0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/imp")
              .health(4)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of())
              .dropChance(0.1f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.HIGH_PITCH)
              .fightAI(
                  () ->
                      new AIRangeBehaviour(
                          7f,
                          0f,
                          new FireballSkill(
                              SkillTools::heroPositionAsPoint, AIFactory.FIREBALL_COOL_DOWN)))
              .idleAI(() -> new RadiusWalk(5f, 2))
              .transitionAI(() -> new RangeTransition(8))
              .collideDamage(0)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** A Zombie monster. Average speed and health but can revive itself. */
  ZOMBIE(
      () ->
          new Builder()
              .reviveCount(1)
              .name("Zombie")
              .speed(3.5f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/zombie")
              .health(10)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOW_PITCH)
              .drops(Set.of())
              .dropChance(0.33f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(() -> new AIChaseBehaviour(1.0f))
              .idleAI(() -> new RadiusWalk(3f, 4))
              .transitionAI(() -> new RangeTransition(6))
              .collideDamage(10)
              .collideCooldown(5 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** An Orc Warrior monster. Average speed and health but only melee attack. */
  ORC_WARRIOR(
      () ->
          new Builder()
              .reviveCount(0)
              .name("Orc Warrior")
              .speed(4f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/orc_warrior")
              .health(8)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.1f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(() -> new AIChaseBehaviour(0.5f))
              .idleAI(() -> new RadiusWalk(3f, 2))
              .transitionAI(() -> new RangeTransition(5))
              .collideDamage(5)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** Orc Shaman monster. Average speed and health but ranged attack. */
  ORC_SHAMAN(
      () ->
          new Builder()
              .name("Orc Shaman")
              .speed(3.0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/orc_shaman")
              .health(4)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.1f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(
                  () ->
                      new AIRangeBehaviour(
                          3f,
                          0f,
                          new FireballSkill(
                              SkillTools::heroPositionAsPoint, AIFactory.FIREBALL_COOL_DOWN)))
              .idleAI(() -> new PatrolWalk(3f, 8, 5, PatrolWalk.MODE.BACK_AND_FORTH))
              .transitionAI(() -> new RangeTransition(5, true))
              .collideDamage(2)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** The tutorial monster. Almost no health and attacks */
  TUTORIAL(
      () ->
          new Builder()
              .name("Tutorial Goblin")
              .speed(7.5f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/goblin")
              .health(2)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.NONE)
              .drops(Set.of())
              .dropChance(0.0f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.NONE)
              .fightAI(() -> new AIChaseBehaviour(1.0f))
              .idleAI(() -> (entity) -> {}) // Stand still if not fighting
              .transitionAI(() -> new RangeTransition(5, true))
              .collideDamage(1)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** The Bridge Mob monster. Immortal, no AI, no damage. */
  BRIDGE_MOB(
      () ->
          new Builder()
              .name("Bridge Mob")
              .speed(4f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/orc_warrior")
              .health(999)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.0f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.NONE)
              .fightAI(() -> new AIChaseBehaviour(0.5f))
              .idleAI(() -> (entity) -> {}) // no idle needed
              .transitionAI(() -> (entity) -> true) // Always fight
              .collideDamage(30)
              .collideCooldown(Game.frameRate())
              .damageType(DamageType.PHYSICAL)),

  /** Dark_Goo monster. Slow with fast weak melee attacks. */
  DARK_GOO(
      () ->
          new Builder()
              .name("Dark Goo")
              .speed(3.75f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/elemental_goo")
              .health(12)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.BASIC)
              .drops(Set.of())
              .dropChance(0.1f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> new AIChaseBehaviour(0.5f))
              .idleAI(() -> new RadiusWalk(3f, 2))
              .transitionAI(() -> new RangeTransition(7))
              .collideDamage(3)
              .collideCooldown(Game.frameRate() / 2)
              .damageType(DamageType.PHYSICAL)),
  /** Small Dark Goo monster. Fast with average fast melee attacks. */
  SMALL_DARK_GOO(
      () ->
          new Builder()
              .name("Small Dark Goo")
              .speed(5f)
              .mass(1.0f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/elemental_goo_small")
              .health(6)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.HIGH_PITCH)
              .drops(Set.of())
              .dropChance(0.05f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> new AIChaseBehaviour(1f))
              .idleAI(() -> new RadiusWalk(2f, 1))
              .transitionAI(() -> new RangeTransition(4))
              .collideDamage(1)
              .collideCooldown(Game.frameRate() / 2)
              .damageType(DamageType.PHYSICAL)),
  /** TP Doc monster. Average speed and health with ranged Teleportation attacks. */
  DOC(
      () ->
          new Builder()
              .name("Doc")
              .speed(4.0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/doc")
              .health(6)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOW_PITCH)
              .drops(Set.of())
              .dropChance(0.25f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(
                  () ->
                      new AIRangeBehaviour(
                          9f,
                          0f,
                          new TPBallSkill(
                              SkillTools::heroPositionAsPoint,
                              LevelUtils::getRandomTPTargetForCurrentLevel,
                              AIFactory.FIREBALL_COOL_DOWN * 4)))
              .idleAI(() -> new PatrolWalk(3f, 8, 5, PatrolWalk.MODE.BACK_AND_FORTH))
              .transitionAI(() -> new RangeTransition(6, false))
              .collideDamage(5)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** The Bridge Guard monster. Immortal, no AI, no damage. */
  BRIDGE_GUARD(
      () ->
          new Builder()
              .name("Bridge Guard")
              .speed(0.0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/big_zombie")
              .health(9999999)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.0f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.NONE)
              .fightAI(() -> (entity) -> {}) // no fight needed
              .idleAI(() -> (entity) -> {}) // no idle needed
              .transitionAI(() -> (entity) -> false) // never transition
              .collideDamage(0)
              .collideCooldown(500)
              .damageType(DamageType.PHYSICAL)),
  /** Pumpkin Boi monster. Small field boss, strong and special ranged attacks. */
  PUMPKIN_BOI(
      () ->
          new Builder()
              .name("Pumpkin Boi")
              .speed(6f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/pumpkin_dude")
              .health(16)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOW_PITCH)
              .drops(Set.of())
              .dropChance(0.5f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.LOWER_PITCH)
              .fightAI(
                  () ->
                      new AIRangeBehaviour(
                          7,
                          6,
                          BossAttackSkills.normalAttack(
                              (int) (AIFactory.FIREBALL_COOL_DOWN * 1.5f))))
              .idleAI(() -> new RadiusWalk(3f, 4))
              .transitionAI(() -> new RangeTransition(6, true))
              .collideDamage(10)
              .collideCooldown(Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /** The Illusion Boss monster. Very strong and special ranged attacks. */
  ILLUSION_BOSS(
      () ->
          new Builder()
              .name("Illusion Boss")
              .speed(0.0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/necromancer")
              .health(50)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(1.0f)
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(
                  () -> new AIRangeBehaviour(15f, 0f, BossAttackSkills.fireCone(35, 125, 12.0f, 3)))
              .idleAI(() -> (entity) -> {}) // no idle needed
              .transitionAI(() -> new RangeTransition(7, true))
              .collideDamage(10)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL)),
  /**
   * The Final Boss monster. Super strong and a lot of special ranged attacks.
   *
   * @see BossAttackSkills
   */
  FINAL_BOSS(
      () ->
          new Builder()
              .name("Final Boss")
              .speed(0.0f)
              .mass(1f)
              .onWallHit(e -> {})
              .canEnterOpenPits(false)
              .texturePath("character/monster/big_daemon")
              .health(100)
              .onDeath(e -> {})
              .removeOnDeath(true)
              .deathSound(MonsterDeathSound.LOWER_PITCH)
              .drops(Set.of())
              .dropChance(0.0f) // custom logic noted in original enum
              .guaranteedDrops(Set.of())
              .idleSound(MonsterIdleSound.BURP)
              .fightAI(() -> new AIRangeBehaviour(17f, 0f, null))
              .idleAI(() -> (entity) -> {}) // no idle needed
              .transitionAI(() -> new RangeTransition(7, true))
              .collideDamage(10)
              .collideCooldown(2 * Game.frameRate())
              .damageType(DamageType.PHYSICAL));

  private final Supplier<Builder> builderSupplier;

  /**
   * Constructor for DevDungeonMonster enum.
   *
   * @param builderSupplier A supplier that provides a new instance of Builder for the enum
   *     constant.
   */
  DevDungeonMonster(Supplier<Builder> builderSupplier) {
    this.builderSupplier = builderSupplier;
  }

  /**
   * Returns a new {@link Builder} for this enum constant.
   *
   * <p>Each call to this method returns a new instance of {@link Builder}, allowing for independent
   * configurations.
   *
   * @return a new {@link Builder} instance
   */
  public Builder builder() {
    return builderSupplier.get();
  }

  /**
   * Creates a bridge guard entity with a list of quizzes.
   *
   * @param pos The position where the bridge guard will be created.
   * @param quizzes The list of quizzes.
   * @param onFinished The function to execute when all quizzes have been solved.
   * @return The created bridge guard entity.
   * @see level.devlevel.riddleHandler.BridgeGuardRiddleHandler BridgeGuardRiddleHandler
   */
  public static Entity createBridgeGuard(Point pos, List<Quiz> quizzes, IVoidFunction onFinished) {
    Entity bridgeGuard = DevDungeonMonster.BRIDGE_GUARD.builder().build(pos);

    bridgeGuard.add(
        new InteractionComponent(
            InteractionComponent.DEFAULT_INTERACTION_RADIUS,
            true,
            (me, who) -> {
              Iterator<Quiz> quizIterator = quizzes.iterator();
              DialogUtils.presentQuiz(quizIterator, onFinished);
            }));

    return bridgeGuard;
  }

  /** Builder class for creating DevDungeon monsters. */
  public static class Builder extends MonsterBuilder<Builder> {

    private int reviveCount = 0;

    @Override
    public Entity build(Point spawnPoint) {
      Entity entity = super.build(spawnPoint);
      if (this.reviveCount() > 0) {
        entity.add(new ReviveComponent(this.reviveCount()));
      }

      entity.remove(InventoryComponent.class);
      entity.add(buildInventoryComponent());

      entity.remove(HealthComponent.class);
      entity.add(buildHealthComponent());

      return entity;
    }

    private int reviveCount() {
      return reviveCount;
    }

    private Builder reviveCount(int reviveCount) {
      this.reviveCount = reviveCount;
      return this;
    }

    private InventoryComponent buildInventoryComponent() {
      InventoryComponent ic = new InventoryComponent(1);
      if (DungeonMonster.RANDOM.nextFloat() < dropChance()) {
        ic.add(MiscFactory.randomItemGenerator().generateItemData());
      }
      return ic;
    }

    private HealthComponent buildHealthComponent() {
      Consumer<Entity> constructedOnDeath =
          entity -> {
            deathSound()
                .ifPresent(
                    deathSound ->
                        playDeathSoundIfNearby(
                            deathSound.path(), DEATH_SOUND_DISPOSE_DELAY, entity));

            entity
                .fetch(InventoryComponent.class)
                .ifPresent(inventoryComponent -> new DropItemsInteraction().accept(entity, null));
            Game.remove(entity);
          };

      return new HealthComponent(health(), constructedOnDeath);
    }
  }
}
