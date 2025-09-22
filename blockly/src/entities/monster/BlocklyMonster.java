package entities.monster;

import coderunner.BlocklyCommands;
import components.TintDirectionComponent;
import contrib.components.*;
import contrib.entities.*;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Enum representing the different types of monsters in Blockly.
 *
 * <p>Each monster type can be built into an entity using the builder pattern with the {@link
 * Builder} class.
 *
 * @see Builder
 */
public enum BlocklyMonster {
  /** A static non-moving guard and shooting monster. */
  GUARD(
      () ->
          new Builder()
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
              .collideCooldown(0)),
  /** A static non-moving guard monster. */
  HEDGEHOG(
      () ->
          new Builder()
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
              .collideCooldown(0)),
  /** The Boss of Produs Blockly. */
  BLACK_KNIGHT(
      () ->
          new Builder()
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
              .collideCooldown(0));

  private final Supplier<Builder> builderSupplier;

  /**
   * Constructor for BlocklyMonster enum.
   *
   * @param builderSupplier A supplier that provides a new instance of Builder for the enum
   *     constant.
   */
  BlocklyMonster(Supplier<Builder> builderSupplier) {
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
     * Returns the attack range of the monster.
     *
     * @return the attack range
     */
    public int attackRange() {
      return this.attackRange;
    }

    @Override
    public Entity build(Point spawnPos) {
      Entity monster = name().isEmpty() ? new Entity() : new Entity(name());

      // hotfix for https://github.com/Dungeon-CampusMinden/Dungeon/issues/2413
      // the boss uses hero textures so we can copy the hero statemachine
      if (name().equals("Blockly Black Knight")) {
        Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(texture());
        State stIdle = new DirectionalState("idle", animationMap);
        State stMove = new DirectionalState("move", animationMap, "run");
        StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove));
        sm.addTransition(stIdle, "move", stMove);
        sm.addTransition(stMove, "move", stMove);
        sm.addTransition(stMove, "idle", stIdle);
        DrawComponent dc = new DrawComponent(sm);
        monster.add(dc);
      } else monster.add(new DrawComponent(texture()));

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
      monster.add(buildHealthComponent());
      monster.add(new VelocityComponent(speed()));
      monster.add(new CollideComponent());
      if (collideDamage() > 0)
        monster.add(new SpikyComponent(collideDamage(), DamageType.PHYSICAL, collideCooldown()));
      monster.add(new BlockViewComponent());
      if (addToGame) {
        Game.add(monster);
      }
      BlocklyCommands.turnEntity(monster, viewDirection());
      return monster;
    }

    private HealthComponent buildHealthComponent() {
      Consumer<Entity> constructedOnDeath =
          entity -> {
            onDeath().accept(entity);
            deathSound()
                .ifPresent(
                    deathSound ->
                        playDeathSoundIfNearby(
                            deathSound.path(), DEATH_SOUND_DISPOSE_DELAY, entity));

            entity
                .fetch(InventoryComponent.class)
                .ifPresent(inventoryComponent -> new DropItemsInteraction().accept(entity, null));

            if (removeOnDeath()) Game.remove(entity);
          };

      return new HealthComponent(health(), constructedOnDeath);
    }
  }
}
