package entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import components.ReviveComponent;
import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.entities.AIFactory;
import contrib.entities.MonsterFactory;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.DamageProjectile;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import level.devlevel.BossLevel;
import level.utils.LevelUtils;
import systems.EventScheduler;

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
      5.5f,
      0.15f,
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
      () -> new RangeTransition(4, false),
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
  ILLUSION_BOSS(
      "Illusion Boss",
      "character/monster/necromancer",
      40,
      0.0f,
      1.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new RangeAI(15f, 0f, fireCone()),
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
      1.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> new RangeAI(15f, 0f, fireShockWave(15)),
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
   * Shoots a fire wall (made of fireballs) towards the hero.
   *
   * @param wallWidth The width of the wall. The wall will be centered on the boss.
   * @return The skill that shoots the fire wall.
   */
  private static Skill fireWall(int wallWidth) {
    return new Skill(
        (skillUser) -> {
          // Firewall
          Point heroPos = SkillTools.heroPositionAsPoint();
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Vector2 direction = new Vector2(heroPos.x - bossPos.x, heroPos.y - bossPos.y);
          // Main shoot is directly at the hero
          // every other fireball is offset left and right of the main shoot
          Vector2 right = new Vector2(direction).rotateDeg(90).nor();
          Vector2 left = new Vector2(direction).rotateDeg(-90).nor();
          for (int i = -wallWidth / 2; i < wallWidth / 2; i++) {
            if (i == 0) {
              launchFireBall(bossPos, heroPos, bossPos, skillUser);
            } else {
              launchFireBall(
                  new Point(bossPos.x + right.x * i, bossPos.y + right.y * i),
                  new Point(heroPos.x + right.x * i, heroPos.y + right.y * i),
                  bossPos,
                  skillUser);
              launchFireBall(
                  new Point(bossPos.x + left.x * i, bossPos.y + left.y * i),
                  new Point(heroPos.x + left.x * i, heroPos.y + left.y * i),
                  bossPos,
                  skillUser);
            }
          }
        },
        AIFactory.FIREBALL_COOL_DOWN * 2);
  }

  private static Skill fireShockWave(int radius) {
    return new Skill(
        (skillUser) -> {
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Tile bossTile = Game.currentLevel().tileAt(bossPos);
          if (bossTile == null) {
            return;
          }
          List<Coordinate> placedPositions = new ArrayList<>();
          LevelUtils.explosionAt(
              bossTile.coordinate(),
              radius,
              250L,
              (tile -> {
                if (tile == null
                    || tile.levelElement() == LevelElement.WALL
                    || tile.coordinate().equals(bossTile.coordinate())
                    || placedPositions.contains(tile.coordinate())) {
                  return;
                }
                placedPositions.add(tile.coordinate());

                Entity entity = new Entity("fire");
                PositionComponent posComp =
                    new PositionComponent(tile.coordinate().toCenteredPoint());
                entity.add(posComp);
                entity.add(new CollideComponent());
                try {
                  DrawComponent drawComp = new DrawComponent(new SimpleIPath("skills/fireball"));
                  drawComp.currentAnimation("run_down");
                  entity.add(drawComp);
                } catch (IOException e) {
                  throw new RuntimeException("Could not load fireball texture" + e);
                }
                entity.add(new SpikyComponent(1, DamageType.FIRE, Game.frameRate() / 2));
                Game.add(entity);

                EventScheduler.getInstance()
                    .scheduleAction(
                        () -> {
                          Game.remove(entity);
                        },
                        2000);
              }));
        },
        10 * 1000);
  }

  private static Skill fireCone() {
    return new Skill(
        (skillUser) -> {
          int degree = 40;
          Point heroPos = SkillTools.heroPositionAsPoint();
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();
          Vector2 direction = new Vector2(heroPos.x - bossPos.x, heroPos.y - bossPos.y).nor();

          // Function to calculate the fireball target position
          Function<Integer, Point> calculateFireballTarget =
              (angle) -> {
                Vector2 offset =
                    new Vector2(direction)
                        .rotateDeg(angle)
                        .scl(new Vector2(heroPos.x - bossPos.x, heroPos.y - bossPos.y).len());
                return new Point(bossPos.x + offset.x, bossPos.y + offset.y);
              };

          // Launch fireballs
          launchFireBall(bossPos, calculateFireballTarget.apply(degree), bossPos, skillUser);
          launchFireBall(bossPos, calculateFireballTarget.apply(0), bossPos, skillUser);
          launchFireBall(bossPos, calculateFireballTarget.apply(-degree), bossPos, skillUser);

          // Schedule another round of fireballs
          EventScheduler.getInstance()
              .scheduleAction(
                  () -> {
                    launchFireBall(
                        bossPos, calculateFireballTarget.apply(degree - 5), bossPos, skillUser);
                    launchFireBall(bossPos, calculateFireballTarget.apply(0), bossPos, skillUser);
                    launchFireBall(
                        bossPos, calculateFireballTarget.apply(-(degree - 5)), bossPos, skillUser);
                  },
                  125);
        },
        AIFactory.FIREBALL_COOL_DOWN * 2);
  }

  private static Skill fireStorm() {
    return new Skill(
        (skillUser) -> {
          int totalFireballs = 16;
          long delayBetweenFireballs = 100;
          // Fire Storm
          Point bossPos =
              skillUser
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(skillUser, PositionComponent.class))
                  .position();

          for (int i = 0; i < totalFireballs; i++) {
            final int degree = i * 360 / totalFireballs;
            EventScheduler.getInstance()
                .scheduleAction(
                    () -> {
                      Point target =
                          new Point(
                              (float) (bossPos.x + Math.cos(Math.toRadians(degree)) * 10),
                              (float) (bossPos.y + Math.sin(Math.toRadians(degree)) * 10));
                      launchFireBall(bossPos, target, bossPos, skillUser);
                    },
                    i * delayBetweenFireballs);
          }
        },
        AIFactory.FIREBALL_COOL_DOWN * 2);
  }

  private static void launchFireBall(Point start, Point target, Point bossPos, Entity skillUser) {
    Entity shooter;
    DamageProjectile skill = new FireballSkill(() -> target, 30f, 5.00f, 1);
    skill.ignoreEntity(skillUser);
    if (start.equals(bossPos)) {
      shooter = skillUser;
    } else {
      shooter = new Entity("Fireball Shooter");
      shooter.add(new PositionComponent(start));
      shooter.add(new CollideComponent());
    }

    skill.accept(shooter);
    EventScheduler.getInstance().scheduleAction(skill::disposeSounds, 1000);
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
