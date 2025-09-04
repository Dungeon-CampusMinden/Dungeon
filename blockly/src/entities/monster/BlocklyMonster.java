package entities.monster;

import components.TintDirectionComponent;
import contrib.components.*;
import contrib.entities.*;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import java.io.IOException;

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
 *   <li>A fight AI
 *   <li>A collide damage value
 *   <li>A collide cooldown value
 * </ul>
 *
 * <p>Each monster type can be built into an entity using the builder pattern with the {@link
 * Builder} class.
 *
 * @see Builder
 */
public class BlocklyMonster {

  /**
   * A static non-moving guard and shooting monster.
   *
   * @return a builder for a guard monster
   */
  public static Builder GUARD() {
    return createGuard();
  }

  private static Builder createGuard() {
    return new Builder()
        .name("Blockly Guard")
        .speed(0.0f)
        .mass(4.0f)
        .onWallHit(e -> {})
        .canEnterOpenPits(false)
        .texturePath("character/monster/big_daemon")
        .health(1)
        .deathSound(MonsterDeathSound.LOWER_PITCH)
        .idleSound(MonsterIdleSound.NONE)
        .fightAI(() -> new StraightRangeAI(6, new InevitableFireballSkill()))
        .idleAI(() -> entity -> {}) // no idle needed
        .transitionAI(() -> entity -> true) // instant fight
        .collideDamage(99999) // one hit kill
        .collideCooldown(0);
  }

  /**
   * A static non-moving guard monster.
   *
   * @return a builder for a hedgehog monster
   */
  public static Builder HEDGEHOG() {
    return createHedgehog();
  }

  private static Builder createHedgehog() {
    return new Builder()
        .name("Blockly Hedgehog")
        .speed(0.0f)
        .mass(2.5f)
        .onWallHit(e -> {})
        .canEnterOpenPits(false)
        .texturePath("character/monster/ogre")
        .health(1)
        .deathSound(MonsterDeathSound.LOWER_PITCH)
        .idleSound(MonsterIdleSound.NONE)
        .fightAI(() -> (entity) -> {}) // static, no fight behavior provided
        .idleAI(() -> (entity) -> {})
        .transitionAI(() -> (entity) -> true) // instant fight/contact behavior
        .collideDamage(99999) // one hit kill
        .collideCooldown(0);
  }

  /**
   * The Boss of Produs Blockly.
   *
   * @return a builder for a black knight monster
   */
  public static Builder BLACK_KNIGHT() {
    return createBlackKnight();
  }

  private static Builder createBlackKnight() {
    return new Builder()
        .name("Blockly Black Knight")
        .speed(0.0f)
        .mass(3.0f)
        .onWallHit(e -> {})
        .canEnterOpenPits(false)
        .texturePath("character/knight")
        .health(3)
        .deathSound(MonsterDeathSound.LOWER_PITCH)
        .idleSound(MonsterIdleSound.NONE)
        .fightAI(() -> (entity) -> {}) // static, provided no fight behavior
        .idleAI(() -> (entity) -> {})
        .transitionAI(() -> (entity) -> true) // instant contact
        .collideDamage(99999) // one hit kill
        .collideCooldown(0);
  }

  /** Builder class for creating Blockly monsters. */
  public static class Builder extends MonsterBuilder<Builder> {

    private int attackRange = -1;

    /**
     * Sets the attack range for the monster.
     *
     * @param range the attack range
     * @return the builder instance
     */
    public Builder attackRange(int range) {
      this.attackRange = range;
      return this;
    }

    /**
     * Returns a the attack range of the monster.
     *
     * @return the attack range
     */
    public int attackRange() {
      return this.attackRange;
    }

    @Override
    public Entity build(Point spawnPos) throws IOException {
      Entity monster = new Entity(name());
      monster.add(new DrawComponent(texture()));

      PositionComponent pc = new PositionComponent(spawnPos, viewDirection());
      monster.add(pc);

      AIComponent aic = new AIComponent(fightAISupplier().get(), entity -> {}, entity -> true);
      monster.add(aic);
      if (aic.fightBehavior() instanceof StraightRangeAI straightRangeAI) {
        if (attackRange() == -1) {
          attackRange(straightRangeAI.range());
        }
        straightRangeAI.range(attackRange());
        monster.add(new TintDirectionComponent(pc.coordinate(), attackRange()));
      }
      monster.add(new HealthComponent(health()));
      monster.add(new VelocityComponent(speed()));
      monster.add(new CollideComponent());
      if (collideDamage() > 0)
        monster.add(new SpikyComponent(collideDamage(), DamageType.PHYSICAL, collideCooldown()));
      monster.add(new BlockViewComponent());
      if (addToGame) {
        Game.add(monster);
      }

      return monster;
    }
  }

  private BlocklyMonster() {}
}
