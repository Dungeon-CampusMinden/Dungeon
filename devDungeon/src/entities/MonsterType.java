package entities;

import com.badlogic.gdx.audio.Sound;
import components.ReviveComponent;
import contrib.components.AIComponent;
import contrib.components.InteractionComponent;
import contrib.entities.AIFactory;
import contrib.entities.MonsterDeathSound;
import contrib.entities.MonsterFactory;
import contrib.entities.MonsterIdleSound;
import contrib.hud.DialogUtils;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import contrib.utils.components.ai.fight.AIRangeBehaviour;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.damageSkill.projectile.FireballSkill;
import contrib.utils.components.skill.damageSkill.projectile.TPBallSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import level.utils.LevelUtils;
import task.tasktype.Quiz;

/**
 * Enum representing the different types of monsters in DevDungeon.
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
 * <p>Each monster type can be built into an entity using the {@link #buildMonster()} method.
 */
public enum MonsterType {
  /** A Chort monster. Slow but tanky and strong. */
  CHORT(
      "Chort",
      "character/monster/chort",
      16,
      2.5f,
      0.33f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new AIChaseBehaviour(0.5f),
      () -> new RadiusWalk(2f, 2),
      () -> new RangeTransition(5),
      7,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  /** An Imp monster. Fast, weak but annoying. */
  IMP(
      "Imp",
      "character/monster/imp",
      4,
      5.0f,
      0.1f,
      MonsterDeathSound.HIGH_PITCH,
      () -> new AIRangeBehaviour(7f, 0f, new FireballSkill(SkillTools::heroPositionAsPoint)),
      () -> new RadiusWalk(5f, 2),
      () -> new RangeTransition(8),
      0,
      2 * Game.frameRate(), // While collideDamage is 0, this value is irrelevant
      MonsterIdleSound.HIGH_PITCH,
      0),
  /** A Zombie monster. Average speed and health but can revive it self. */
  ZOMBIE(
      "Zombie",
      "character/monster/zombie",
      10,
      3.5f,
      0.33f,
      MonsterDeathSound.LOW_PITCH,
      () -> new AIChaseBehaviour(1.0f),
      () -> new RadiusWalk(3f, 4),
      () -> new RangeTransition(6),
      10,
      5 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      1),
  /** An Orc Warrior monster. Average speed and health but only melee attack. */
  ORC_WARRIOR(
      "Orc Warrior",
      "character/monster/orc_warrior",
      8,
      4f,
      0.1f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new AIChaseBehaviour(0.5f),
      () -> new RadiusWalk(3f, 2),
      () -> new RangeTransition(5),
      5,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  /** Orc Shaman monster. Average speed and health but ranged attack. */
  ORC_SHAMAN(
      "Orc Shaman",
      "character/monster/orc_shaman",
      4,
      3.0f,
      0.1f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new AIRangeBehaviour(3f, 0f, new FireballSkill(SkillTools::heroPositionAsPoint)),
      () -> new PatrolWalk(3f, 8, 5, PatrolWalk.MODE.BACK_AND_FORTH),
      () -> new RangeTransition(5, true),
      2,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  /** The tutorial monster. Almost no health and attacks. */
  TUTORIAL(
      "Tutorial Goblin",
      "character/monster/goblin",
      2,
      7.5f,
      0.0f,
      MonsterDeathSound.NONE,
      () -> new AIChaseBehaviour(1.0f),
      () -> (entity) -> {}, // Stand still if not fighting
      () -> new RangeTransition(5, true),
      1,
      2 * Game.frameRate(),
      MonsterIdleSound.NONE,
      0),
  /** The Bridge Mob monster. Immortal, no AI, no damage. */
  BRIDGE_MOB(
      "Bridge Mob",
      "character/monster/orc_warrior",
      999, // immortal
      4f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new AIChaseBehaviour(0.5f),
      () -> entity -> {}, // no idle needed
      () -> (entity) -> true, // Always fight
      30, // one hit kill
      Game.frameRate(),
      MonsterIdleSound.NONE,
      0),
  /** Dark_Goo monster. Slow with fast weak melee attacks. */
  DARK_GOO(
      "Dark Goo",
      "character/monster/elemental_goo",
      12,
      3.75f,
      0.1f,
      MonsterDeathSound.BASIC,
      () -> new AIChaseBehaviour(0.5f),
      () -> new RadiusWalk(3f, 2),
      () -> new RangeTransition(7),
      3,
      Game.frameRate() / 2,
      MonsterIdleSound.BURP,
      0),
  /** Small Dark Goo monster. Fast with average fast melee attacks. */
  SMALL_DARK_GOO(
      "Small Dark Goo",
      "character/monster/elemental_goo_small",
      6,
      5f,
      0.05f,
      MonsterDeathSound.HIGH_PITCH,
      () -> new AIChaseBehaviour(1f),
      () -> new RadiusWalk(2f, 1),
      () -> new RangeTransition(4),
      1,
      Game.frameRate() / 2,
      MonsterIdleSound.BURP,
      0),
  /** TP Doc monster. Average speed and health with ranged Teleportation attacks. */
  DOC(
      "Doc",
      "character/monster/doc",
      6,
      4.0f,
      0.25f,
      MonsterDeathSound.LOW_PITCH,
      () ->
          new AIRangeBehaviour(
              9f,
              0f,
              new TPBallSkill(
                  SkillTools::heroPositionAsPoint, LevelUtils::getRandomTPTargetForCurrentLevel)),
      () -> new PatrolWalk(3f, 8, 5, PatrolWalk.MODE.BACK_AND_FORTH),
      () -> new RangeTransition(6, false),
      5,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  /** The Bridge Guard monster. Immortal, no AI, no damage. */
  BRIDGE_GUARD(
      "Bridge Guard",
      "character/monster/big_zombie",
      9999999, // immortal
      0.0f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> entity -> {}, // no fight needed
      () -> entity -> {}, // no idle needed
      () -> entity -> false, // never transition
      0,
      500, // irrelevant
      MonsterIdleSound.NONE,
      0),
  /** Pumpkin Boi monster. Small field boss, strong and special ranged attacks. */
  PUMPKIN_BOI(
      "Pumpkin Boi",
      "character/monster/pumpkin_dude",
      16,
      6f,
      0.5f,
      MonsterDeathSound.LOW_PITCH,
      () ->
          new AIRangeBehaviour(
              7, 6, BossAttackSkills.normalAttack((int) (AIFactory.FIREBALL_COOL_DOWN * 1.5f))),
      () -> new RadiusWalk(3f, 4),
      () -> new RangeTransition(6, true),
      10,
      Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      1),
  /** The Illusion Boss monster. Very strong and special ranged attacks. */
  ILLUSION_BOSS(
      "Illusion Boss",
      "character/monster/necromancer",
      50,
      0.0f,
      1.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new AIRangeBehaviour(15f, 0f, BossAttackSkills.fireCone(35, 125, 12.0f, 3)),
      () -> entity -> {}, // no idle needed
      () -> new RangeTransition(7, true),
      10,
      2 * Game.frameRate(),
      MonsterIdleSound.BURP,
      0),
  /**
   * The Final Boss monster. Super strong and a lot of special ranged attacks.
   *
   * @see BossAttackSkills
   */
  FINAL_BOSS(
      "Final Boss",
      "character/monster/big_daemon",
      100,
      0.0f,
      0.0f, // Custom Logic
      MonsterDeathSound.LOWER_PITCH,
      () -> new AIRangeBehaviour(17f, 0f, null),
      () -> entity -> {}, // no idle needed
      () -> new RangeTransition(7, true),
      10,
      2 * Game.frameRate(),
      MonsterIdleSound.BURP,
      0);

  private final String name;
  private final IPath texture;
  private final Sound deathSound;
  private final Supplier<Consumer<Entity>> fightAISupplier;
  private final Supplier<Consumer<Entity>> idleAISupplier;
  private final Supplier<Function<Entity, Boolean>> transitionAISupplier;
  private final int collideDamage;
  private final int collideCooldown;
  private final IPath idleSoundPath;
  private final int health;
  private final float speed;
  private final float itemChance; // 0.0f means no items, 1.0f means always items
  private final int reviveCount;

  /**
   * Creates a new MonsterType with the given parameters.
   *
   * @param name The name of the monster.
   * @param texture The path to the texture to use for the monster.
   * @param health The amount of health the monster has.
   * @param speed The speed of the monster.
   * @param canHaveItems The chance that the monster will drop an item upon death. If 0, no item
   *     will be dropped. If 1, an item will always be dropped.
   * @param deathSound The sound to play when the monster dies.
   * @param fightAISupplier The supplier for the fight AI.
   * @param idleAISupplier The supplier for the idle AI.
   * @param transitionAISupplier The supplier for the transition AI.
   * @param collideDamage The damage the monster inflicts upon collision.
   * @param collideCooldown The cooldown time between monster's collision damage.
   * @param idleSound The sound to play when the monster is idle.
   * @param reviveCount The amount of times the monster can revive itself.
   */
  MonsterType(
      String name,
      String texture,
      int health,
      float speed,
      float canHaveItems,
      MonsterDeathSound deathSound,
      Supplier<Consumer<Entity>> fightAISupplier,
      Supplier<Consumer<Entity>> idleAISupplier,
      Supplier<Function<Entity, Boolean>> transitionAISupplier,
      int collideDamage,
      int collideCooldown,
      MonsterIdleSound idleSound,
      int reviveCount) {
    this.name = name;
    this.texture = new SimpleIPath(texture);
    this.health = health;
    this.speed = speed;
    this.itemChance = canHaveItems;
    this.deathSound = deathSound.sound();
    this.reviveCount = reviveCount;
    this.fightAISupplier = fightAISupplier;
    this.idleAISupplier = idleAISupplier;
    this.transitionAISupplier = transitionAISupplier;
    this.collideDamage = collideDamage;
    this.collideCooldown = collideCooldown;
    this.idleSoundPath = idleSound.path();
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
    Entity bridgeGuard;
    try {
      bridgeGuard = MonsterType.BRIDGE_GUARD.buildMonster();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create bridge guard");
    }
    bridgeGuard
        .fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(bridgeGuard, PositionComponent.class))
        .position(pos);

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

  /**
   * Builds a monster entity with the given parameters.
   *
   * @return A new Entity representing the monster.
   * @throws IOException if the animation could not be loaded.
   * @see MonsterFactory#buildMonster(String, IPath, int, float, float, Sound, AIComponent, int,
   *     int, IPath) MonsterFactory.buildMonster
   */
  public Entity buildMonster() throws IOException {
    Entity newEntity =
        MonsterFactory.buildMonster(
            name,
            texture,
            health,
            speed,
            itemChance,
            deathSound,
            new AIComponent(
                fightAISupplier.get(), idleAISupplier.get(), transitionAISupplier.get()),
            collideDamage,
            collideCooldown,
            idleSoundPath);
    if (reviveCount > 0) {
      newEntity.add(new ReviveComponent(reviveCount));
    }
    return newEntity;
  }
}
