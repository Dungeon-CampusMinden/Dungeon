package entities;

import antlr.BlocklyConditionVisitor;
import com.badlogic.gdx.audio.Sound;
import components.TintDirectionComponent;
import contrib.components.AIComponent;
import contrib.components.BlockViewComponent;
import contrib.entities.*;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
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
 * <p>Each monster type can be built into an entity using the builder pattern with {@link
 * #builder()}.
 *
 * @see BlocklyMonsterBuilder
 */
public enum BlocklyMonster {

  /** A static non-moving guard and shooting monster. */
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
              new InevitableFireballSkill(
                  // Adjust the position of the target to the center of the tile so the fireball
                  // flies in a straight line
                  () ->
                      Game.hero()
                          .flatMap(hero -> hero.fetch(PositionComponent.class))
                          .map(PositionComponent::position)
                          .map(Point::toCenteredPoint)
                          // offset for error with fireball path calculation (#2230)
                          .map(point -> point.translate(Vector2.of(0.5f, 0.5f)))
                          .orElse(null))),
      () -> entity -> {}, // no idle needed
      () -> entity -> true, // instant fight
      99999, // one hit kill
      0,
      MonsterIdleSound.BURP),
  /** A static non-moving guard monster. */
  HEDGEHOG(
      "Blockly Hedgehog",
      "character/monster/ogre",
      1,
      0.0f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> entity -> {},
      () -> entity -> {}, // no idle needed
      () -> entity -> false, // instant fight
      99999, // one hit kill
      0,
      MonsterIdleSound.BURP),
  /** The Boss of Produs Blockly. */
  BLACK_KNIGHT(
      "Blockly Black Knight",
      "character/knight",
      3,
      0f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      () -> entity -> {},
      () -> entity -> {}, // no idle needed
      () -> entity -> false, // instant fight
      99999, // one hit kill
      0,
      MonsterIdleSound.BURP);

  private static final Logger LOGGER =
      Logger.getLogger(BlocklyConditionVisitor.class.getSimpleName());

  private final String name;
  private final IPath texture;
  private final Sound deathSound;
  private final Supplier<Consumer<Entity>> fightAISupplier;
  private final Supplier<Consumer<Entity>> idleAISupplier;
  private final Supplier<Function<Entity, Boolean>> transitionAISupplier;
  private final int collideDamage;
  private final int collideCooldown;
  private final IPath idleSoundPath;
  private final int maxHealth;
  private final float speed;
  private final float itemChance; // 0.0f means no items, 1.0f means always items

  /**
   * Creates a new MonsterType with the given parameters.
   *
   * <p>You can use the {@link #builder()} method to create a new instance of this monster type.
   *
   * @param name The name of the monster.
   * @param texture The path to the texture to use for the monster.
   * @param maxHealth The amount of health the monster has.
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
      int maxHealth,
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
    this.maxHealth = maxHealth;
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
   * Creates a builder for this monster type.
   *
   * @return A new MonsterBuilder for this monster type.
   */
  public BlocklyMonsterBuilder builder() {
    return new BlocklyMonsterBuilder(this);
  }

  /** Builder class for creating Blockly monsters. */
  public static class BlocklyMonsterBuilder {
    private final BlocklyMonster monsterType;
    private Point spawnPoint = new Point(0, 0);
    private Direction viewDirection = Direction.DOWN;
    private int range = -1; // -1 means use default range
    private int maxHealth;
    private int collideDamage;

    private float speed = -1; // <0 means use default speed
    private boolean addToGame = false;

    /**
     * Creates a new builder for the specified monster type.
     *
     * @param monsterType The type of monster to build.
     */
    BlocklyMonsterBuilder(BlocklyMonster monsterType) {
      this.monsterType = monsterType;
      this.maxHealth = monsterType.maxHealth;
      this.collideDamage = monsterType.collideDamage;
    }

    /**
     * Sets the spawn position for the monster.
     *
     * @param position The position where the monster should spawn.
     * @return This builder for method chaining.
     */
    public BlocklyMonsterBuilder spawnPoint(Point position) {
      this.spawnPoint = position;
      return this;
    }

    /**
     * Sets the view direction for the monster.
     *
     * @param viewDirection The direction the monster should face.
     * @return This builder for method chaining.
     */
    public BlocklyMonsterBuilder viewDirection(Direction viewDirection) {
      this.viewDirection = viewDirection;
      return this;
    }

    /**
     * Sets the attack range for the monster (if it uses {@link StraightRangeAI}).
     *
     * @param range The range for the monster's attacks.
     * @return This builder for method chaining.
     */
    public BlocklyMonsterBuilder range(int range) {
      this.range = range;
      return this;
    }

    /**
     * Maximum movement speed of the monster.
     *
     * @param speed The movement speed to set for the monster
     * @return This builder instance for method chaining.
     */
    public BlocklyMonsterBuilder speed(float speed) {
      this.speed = speed;
      return this;
    }

    /**
     * Sets the maximum health for the monster.
     *
     * @param maxHealth The maximum health of the monster.
     * @return This builder for method chaining.
     */
    public BlocklyMonsterBuilder maxHealth(int maxHealth) {
      this.maxHealth = maxHealth;
      return this;
    }

    /**
     * Sets the collide damage for the monster.
     *
     * @param collideDamage The damage the monster inflicts upon collision.
     * @return This builder for method chaining.
     * @see contrib.components.SpikyComponent SpikyComponent
     */
    public BlocklyMonsterBuilder collideDamage(int collideDamage) {
      this.collideDamage = collideDamage;
      return this;
    }

    /**
     * Add this entity to the {@link core.Game Game} upon building it.
     *
     * @return This builder for method chaining.
     * @see Game#add(Entity)
     */
    public BlocklyMonsterBuilder addToGame() {
      this.addToGame = true;
      return this;
    }

    /**
     * Builds the monster entity with the configured parameters.
     *
     * @return An Optional containing the built monster entity, or an empty Optional if the build
     *     failed.
     */
    public Optional<Entity> build() {
      Entity monster;
      try {
        monster =
            MonsterFactory.buildMonster(
                monsterType.name,
                monsterType.texture,
                this.maxHealth,
                (this.speed < 0) ? monsterType.speed : this.speed,
                monsterType.itemChance,
                monsterType.deathSound,
                new AIComponent(
                    monsterType.fightAISupplier.get(),
                    monsterType.idleAISupplier.get(),
                    monsterType.transitionAISupplier.get()),
                this.collideDamage,
                monsterType.collideCooldown,
                monsterType.idleSoundPath);
      } catch (IOException e) {
        LOGGER.severe("Failed to load monster animation: " + e.getMessage());
        return Optional.empty();
      }

      monster.add(new BlockViewComponent());

      PositionComponent pc =
          monster
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(monster, PositionComponent.class));

      pc.viewDirection(viewDirection);
      pc.position(spawnPoint);

      AIComponent aic =
          monster
              .fetch(AIComponent.class)
              .orElseThrow(() -> MissingComponentException.build(monster, AIComponent.class));
      if (aic.fightBehavior() instanceof StraightRangeAI straightRangeAI) {
        if (range == -1) {
          range = straightRangeAI.range();
        }
        straightRangeAI.range(this.range);
        monster.add(new TintDirectionComponent(pc.coordinate(), this.range));
      }

      if (addToGame) {
        Game.add(monster);
      }

      return Optional.of(monster);
    }
  }
}
