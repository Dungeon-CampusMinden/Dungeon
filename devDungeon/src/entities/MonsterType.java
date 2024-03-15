package entities;

import com.badlogic.gdx.audio.Sound;
import components.ReviveComponent;
import contrib.components.AIComponent;
import contrib.entities.AIFactory;
import contrib.entities.MonsterFactory;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum MonsterType {
  CHORT(
      "Chort",
      "character/monster/chort",
      16,
      2.5f,
      0.5f,
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
      0.2f,
      MonsterDeathSound.HIGH_PITCH,
      () ->
          new RangeAI(
              7f,
              2f,
              new Skill(
                  new FireballSkill(SkillTools::heroPositionAsPoint),
                  AIFactory.FIREBALL_COOL_DOWN)),
      () -> new StaticRadiusWalk(5f, 2),
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
      () -> new CollideAI(0.5f),
      () -> new RadiusWalk(5f, 5),
      () -> new RangeTransition(5),
      3,
      2 * Game.frameRate(),
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
      () -> new StaticRadiusWalk(5f, 2),
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
              2f,
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
