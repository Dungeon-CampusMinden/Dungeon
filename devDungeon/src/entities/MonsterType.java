package entities;

import com.badlogic.gdx.audio.Sound;
import components.ReviveComponent;
import contrib.components.AIComponent;
import contrib.components.InteractionComponent;
import contrib.entities.AIFactory;
import contrib.entities.DialogFactory;
import contrib.entities.MonsterFactory;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
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
import level.devlevel.BossLevel;
import level.utils.LevelUtils;
import task.tasktype.Quiz;

public enum MonsterType {
  CHORT(
      "Chort",
      "character/monster/chort",
      16,
      2.5f,
      0.33f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new CollideAI(0.5f),
      () -> new RadiusWalk(2f, 2),
      () -> new RangeTransition(5),
      7,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  IMP(
      "Imp",
      "character/monster/imp",
      4,
      5.0f,
      0.1f,
      MonsterDeathSound.HIGH_PITCH,
      () ->
          new RangeAI(
              7f,
              0f,
              new Skill(
                  new FireballSkill(SkillTools::heroPositionAsPoint),
                  AIFactory.FIREBALL_COOL_DOWN)),
      () -> new RadiusWalk(5f, 2),
      () -> new RangeTransition(8),
      0,
      2 * Game.frameRate(), // While collideDamage is 0, this value is irrelevant
      MonsterIdleSound.HIGH_PITCH,
      0),
  ZOMBIE(
      "Zombie",
      "character/monster/zombie",
      10,
      3.5f,
      0.33f,
      MonsterDeathSound.LOW_PITCH,
      () -> new CollideAI(1.0f),
      () -> new RadiusWalk(3f, 4),
      () -> new RangeTransition(6),
      10,
      5 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      1),
  ORC_WARRIOR(
      "Orc Warrior",
      "character/monster/orc_warrior",
      8,
      3.0f,
      0.1f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new CollideAI(0.5f),
      () -> new RadiusWalk(3f, 2),
      () -> new RangeTransition(5),
      5,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  ORC_SHAMAN(
      "Orc Shaman",
      "character/monster/orc_shaman",
      4,
      3.0f,
      0.1f,
      MonsterDeathSound.LOWER_PITCH,
      () ->
          new RangeAI(
              3f,
              0f,
              new Skill(
                  new FireballSkill(SkillTools::heroPositionAsPoint),
                  AIFactory.FIREBALL_COOL_DOWN)),
      () -> new PatrolWalk(3f, 8, 5, PatrolWalk.MODE.BACK_AND_FORTH),
      () -> new RangeTransition(5, true),
      2,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
  TUTORIAL(
      "Tutorial",
      "character/monster/goblin",
      2,
      7.5f,
      0.0f,
      MonsterDeathSound.NONE,
      () -> new CollideAI(1.0f),
      () -> (entity) -> {}, // Stand still if not fighting
      () -> new RangeTransition(5, true),
      1,
      2 * Game.frameRate(),
      MonsterIdleSound.NONE,
      0),
  BRIDGE_MOB(
      "Bridge Mob",
      "character/monster/orc_warrior",
      999, // immortal
      3.5f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new CollideAI(0.5f),
      () -> entity -> {}, // no idle needed
      () -> (entity) -> true, // Always fight
      30, // one hit kill
      Game.frameRate(),
      MonsterIdleSound.NONE,
      0),
  DARK_GOO(
      "Dark Goo",
      "character/monster/elemental_goo",
      12,
      3.25f,
      0.1f,
      MonsterDeathSound.BASIC,
      () -> new CollideAI(0.5f),
      () -> new RadiusWalk(3f, 2),
      () -> new RangeTransition(7),
      3,
      Game.frameRate() / 2,
      MonsterIdleSound.BURP,
      0),
  SMALL_DARK_GOO(
      "Small Dark Goo",
      "character/monster/elemental_goo_small",
      6,
      4.0f,
      0.05f,
      MonsterDeathSound.HIGH_PITCH,
      () -> new CollideAI(1f),
      () -> new RadiusWalk(2f, 1),
      () -> new RangeTransition(4),
      1,
      Game.frameRate() / 2,
      MonsterIdleSound.BURP,
      0),
  DOC(
      "Doc",
      "character/monster/doc",
      6,
      4.0f,
      0.25f,
      MonsterDeathSound.LOW_PITCH,
      () ->
          new RangeAI(
              9f,
              0f,
              new Skill(
                  new TPBallSkill(
                      SkillTools::heroPositionAsPoint,
                      LevelUtils.getRandomTPTargetForCurrentLevel()),
                  AIFactory.FIREBALL_COOL_DOWN * 4)),
      () -> new PatrolWalk(3f, 8, 5, PatrolWalk.MODE.BACK_AND_FORTH),
      () -> new RangeTransition(6, false),
      5,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      0),
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
  PUMPKIN_BOI(
      "Pumpkin Boi",
      "character/monster/pumpkin_dude",
      16,
      7f,
      0.5f,
      MonsterDeathSound.LOW_PITCH,
      () ->
          new RangeAI(
              7, 6, BossAttackSkills.normalAttack((int) (AIFactory.FIREBALL_COOL_DOWN * 1.5f))),
      () -> new RadiusWalk(3f, 4),
      () -> new RangeTransition(6, true),
      10,
      Game.frameRate(),
      MonsterIdleSound.LOW_PITCH,
      1),
  ILLUSION_BOSS(
      "Illusion Boss",
      "character/monster/necromancer",
      50,
      0.0f,
      1.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new RangeAI(15f, 0f, BossAttackSkills.fireCone(35, 125, 12.0f, 3)),
      () -> entity -> {}, // no idle needed
      () -> new RangeTransition(7, true),
      10,
      2 * Game.frameRate(),
      MonsterIdleSound.BURP,
      0),
  FINAL_BOSS(
      "Final Boss",
      "character/monster/big_deamon",
      BossLevel.BOSS_HP,
      0.0f,
      0.0f, // Custom Logic
      MonsterDeathSound.LOWER_PITCH,
      () -> new RangeAI(17f, 0f, null),
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
    this.deathSound = deathSound.getSound();
    this.reviveCount = reviveCount;
    this.fightAISupplier = fightAISupplier;
    this.idleAISupplier = idleAISupplier;
    this.transitionAISupplier = transitionAISupplier;
    this.collideDamage = collideDamage;
    this.collideCooldown = collideCooldown;
    this.idleSoundPath = idleSound.getPath();
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
              DialogFactory.presentQuiz(quizIterator, onFinished);
            }));

    return bridgeGuard;
  }

  public Entity buildMonster() throws IOException {
    Entity newEntity =
        MonsterFactory.buildMonster(
            this.name,
            this.texture,
            this.health,
            this.speed,
            this.itemChance,
            this.deathSound,
            new AIComponent(
                this.fightAISupplier.get(),
                this.idleAISupplier.get(),
                this.transitionAISupplier.get()),
            this.collideDamage,
            this.collideCooldown,
            this.idleSoundPath);
    if (this.reviveCount > 0) {
      newEntity.add(new ReviveComponent(this.reviveCount));
    }
    return newEntity;
  }
}
