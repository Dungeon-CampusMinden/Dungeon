package entities;

import com.badlogic.gdx.audio.Sound;
import components.BlockFireBallComponent;
import components.TintDirectionComponent;
import contrib.components.AIComponent;
import contrib.entities.AIFactory;
import contrib.entities.MonsterDeathSound;
import contrib.entities.MonsterFactory;
import contrib.entities.MonsterIdleSound;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import utils.components.ai.fight.StraightRangeAI;
import utils.components.skill.InevitableFireballSkill;

/**
 * Enum representing the different types of monsters in Blockly.
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
 * </ul>
 *
 * <p>Each monster type can be built into an entity using the {@link #buildMonster(Point,
 * PositionComponent.Direction)} method.
 */
public enum BlocklyMonster {
  /** A static non-moving guard monster. */
  GUARD(
      "Blockly Guard",
      "character/monster/big_daemon",
      1,
      0.0f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      () ->
          new StraightRangeAI(
              6,
              new Skill(
                  new InevitableFireballSkill(EntityUtils::getHeroPosition),
                  AIFactory.FIREBALL_COOL_DOWN)),
      () -> entity -> {}, // no idle needed
      () -> entity -> true, // instant fight
      99999, // one hit kill
      0,
      MonsterIdleSound.BURP);

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
   */
  BlocklyMonster(
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
      MonsterIdleSound idleSound) {
    this.name = name;
    this.texture = new SimpleIPath(texture);
    this.health = health;
    this.speed = speed;
    this.itemChance = canHaveItems;
    this.deathSound = deathSound.sound();
    this.fightAISupplier = fightAISupplier;
    this.idleAISupplier = idleAISupplier;
    this.transitionAISupplier = transitionAISupplier;
    this.collideDamage = collideDamage;
    this.collideCooldown = collideCooldown;
    this.idleSoundPath = idleSound.path();
  }

  /**
   * Builds a monster entity with the given parameters.
   *
   * @param spawnPos The position where the monster should spawn.
   * @param viewDirection The direction the monster should face.
   * @return A new Entity representing the monster.
   * @throws IOException if the animation could not be loaded.
   * @see MonsterFactory#buildMonster(String, IPath, int, float, float, Sound, AIComponent, int,
   *     int, IPath) MonsterFactory.buildMonster
   */
  public Entity buildMonster(Point spawnPos, PositionComponent.Direction viewDirection)
      throws IOException {
    Entity monster =
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
    monster.add(new BlockFireBallComponent());
    PositionComponent pc =
        monster
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(monster, PositionComponent.class));
    pc.viewDirection(viewDirection);
    pc.position(spawnPos);
    if (fightAISupplier.get() instanceof StraightRangeAI straightRangeAI) {
      monster.add(
          new TintDirectionComponent(pc.position().toCoordinate(), straightRangeAI.range()));
    }
    return monster;
  }

  /**
   * Builds a monster entity with the given parameters.
   *
   * @param spawnPos The position where the monster should spawn.
   * @return A new Entity representing the monster.
   * @throws IOException if the animation could not be loaded.
   * @see MonsterFactory#buildMonster(String, IPath, int, float, float, Sound, AIComponent, int,
   *     int, IPath) MonsterFactory.buildMonster
   */
  public Entity buildMonster(Point spawnPos) throws IOException {
    return buildMonster(spawnPos, PositionComponent.Direction.DOWN);
  }
}
