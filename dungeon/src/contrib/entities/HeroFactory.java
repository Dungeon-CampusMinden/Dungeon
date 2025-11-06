package contrib.entities;

import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.DialogUtils;
import contrib.systems.HealthSystem;
import contrib.systems.HudSystem;
import contrib.utils.components.health.Damage;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.*;
import core.level.loader.DungeonLoader;
import core.systems.VelocitySystem;
import core.utils.*;
import core.utils.components.draw.*;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import java.util.*;
import java.util.function.Consumer;

/** A utility class for building the player entity in the game world. */
public final class HeroFactory {

  private static final String DEATH_SOUND_ID = "death";

  private static final CharacterClass DEFAULT_HERO_CLASS = CharacterClass.WIZARD;
  private static final Consumer<Entity> DEFAULT_DEATH =
      (hero) ->
          DialogUtils.showTextPopup(
              "You died!",
              "Game Over",
              () -> {
                hero.fetch(HealthComponent.class)
                    .ifPresent(hc -> hc.currentHealthpoints(hc.maximalHealthpoints()));
                hero.fetch(ManaComponent.class).ifPresent(hc -> hc.currentAmount(hc.maxAmount()));
                hero.fetch(StaminaComponent.class)
                    .ifPresent(hc -> hc.currentAmount(hc.maxAmount()));

                // reset inventory
                hero.fetch(CharacterClassComponent.class)
                    .ifPresent(
                        characterClassComponent -> {
                          InventoryComponent invComp =
                              new InventoryComponent(
                                  characterClassComponent.characterClass().inventorySize());
                          characterClassComponent
                              .characterClass()
                              .startItems()
                              .forEach(invComp::add);
                          hero.add(invComp);
                        });

                // reset the animation queue
                hero.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
                DungeonLoader.reloadCurrentLevel();
              });

  /**
   * Creates a new HeroBuilder with default settings.
   *
   * @return A new HeroBuilder instance.
   */
  public static HeroBuilder builder() {
    return new HeroBuilder();
  }

  /** Builder class for creating Hero entities with custom configurations. */
  public static class HeroBuilder {
    private CharacterClass characterClass = DEFAULT_HERO_CLASS;
    private Consumer<Entity> deathCallback = DEFAULT_DEATH;
    private boolean persistent = true;

    private HeroBuilder() {}

    /**
     * Sets the character class for the hero.
     *
     * @param characterClass The character class to use.
     * @return This builder instance.
     */
    public HeroBuilder characterClass(CharacterClass characterClass) {
      this.characterClass = characterClass;
      return this;
    }

    /**
     * Sets the death callback for the hero.
     *
     * @param deathCallback The callback to execute when the hero dies.
     * @return This builder instance.
     */
    public HeroBuilder deathCallback(Consumer<Entity> deathCallback) {
      this.deathCallback = deathCallback;
      return this;
    }

    /**
     * Sets whether the hero entity is persistent.
     *
     * @param persistent True to make the hero persistent, false otherwise.
     * @return This builder instance.
     */
    public HeroBuilder persistent(boolean persistent) {
      this.persistent = persistent;
      return this;
    }

    /**
     * Builds and returns the Hero entity with the configured settings.
     *
     * @return A new Hero Entity.
     */
    public Entity build() {
      return buildHero(characterClass, deathCallback, persistent);
    }
  }

  /**
   * Internal method to build a hero entity with all components configured.
   *
   * @param characterClass The character class.
   * @param deathCallback The death callback.
   * @param persistent Whether the entity is persistent.
   * @return The configured hero entity.
   */
  private static Entity buildHero(
      CharacterClass characterClass, Consumer<Entity> deathCallback, boolean persistent) {
    Entity hero = new Entity("hero");
    hero.persistent(persistent);
    PlayerComponent pc = new PlayerComponent();
    hero.add(pc);
    CameraComponent cc = new CameraComponent();
    hero.add(cc);
    PositionComponent poc = new PositionComponent();
    hero.add(poc);

    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(characterClass.textures());
    State stIdle = new DirectionalState(StateMachine.IDLE_STATE, animationMap);
    State stMove = new DirectionalState(VelocitySystem.STATE_NAME, animationMap, "run");

    State stDead;
    if (animationMap.containsKey("die_down")) {
      stDead = new DirectionalState(HealthSystem.DEATH_STATE, animationMap, "die");
    } else if (animationMap.containsKey("die")) {
      stDead = new State(HealthSystem.DEATH_STATE, animationMap.get("die"));
    } else {
      stDead = new State(HealthSystem.DEATH_STATE, animationMap.get("idle_down"));
    }

    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove, stDead));
    sm.addTransition(stIdle, VelocitySystem.MOVE_SIGNAL, stMove);
    sm.addTransition(stIdle, VelocitySystem.IDLE_SIGNAL, stIdle);
    sm.addTransition(stMove, VelocitySystem.MOVE_SIGNAL, stMove);
    sm.addTransition(stMove, VelocitySystem.IDLE_SIGNAL, stIdle);
    sm.addTransition(stIdle, HealthSystem.DEATH_SIGNAL, stDead);
    sm.addTransition(stMove, HealthSystem.DEATH_SIGNAL, stDead);
    DrawComponent dc = new DrawComponent(sm);
    dc.depth(DepthLayer.Player.depth());
    hero.add(dc);

    hero.add(
        new VelocityComponent(
            Math.max(characterClass.speed().x(), characterClass.speed().y()),
            characterClass.mass(),
            (e) -> {},
            true));
    hero.add(
        new ManaComponent(
            characterClass.mana(), characterClass.mana(), characterClass.manaRestore()));
    hero.add(
        new StaminaComponent(
            characterClass.stamina(), characterClass.stamina(), characterClass.staminaRestore()));
    hero.add(new SkillComponent(characterClass.startSkills().toArray(new Skill[0])));

    HealthComponent hc =
        new HealthComponent(
            characterClass.hp(),
            entity -> {
              // play sound
              Game.soundPlayer().play(DEATH_SOUND_ID, 0.9f);

              // relink components for camera
              Entity cameraDummy = new Entity("heroCamera");
              cameraDummy.add(cc);
              cameraDummy.add(poc);
              Game.add(cameraDummy);

              deathCallback.accept(entity);
            });
    hc.currentHealthpoints(characterClass.hp());
    hero.add(hc);
    CollideComponent col = new CollideComponent();
    col.onHold(
        (you, other, direction) ->
            other
                .fetch(SpikyComponent.class)
                .ifPresent(
                    spikyComponent -> {
                      if (spikyComponent.isActive()) {
                        hc.receiveHit(
                            new Damage(
                                spikyComponent.damageAmount(), spikyComponent.damageType(), other));
                        spikyComponent.activateCoolDown();
                      }
                    }));
    hero.add(col);

    InputComponent inputComp = new InputComponent();
    hero.add(
        new CatapultableComponent(
            entity -> inputComp.deactivateControls(true),
            entity -> inputComp.deactivateControls(false)));
    hero.add(inputComp);

    InventoryComponent invComp = new InventoryComponent(characterClass.inventorySize());
    characterClass.startItems().forEach(invComp::add);
    hero.add(invComp);

    setupControls(inputComp, characterClass);

    return hero;
  }

  // region Bind Controls
  private static void setupControls(InputComponent inputComp, CharacterClass characterClass) {
    // WASD
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_UP.value(),
        (caller) -> HeroController.moveHero(caller, Direction.UP, characterClass.speed()));
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(),
        (caller) -> HeroController.moveHero(caller, Direction.DOWN, characterClass.speed()));
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(),
        (caller) -> HeroController.moveHero(caller, Direction.RIGHT, characterClass.speed()));
    inputComp.registerCallback(
        core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(),
        (caller) -> HeroController.moveHero(caller, Direction.LEFT, characterClass.speed()));

    // Skills
    inputComp.registerCallback(
        KeyboardConfig.USE_SKILL.value(),
        (caller) -> HeroController.useSkill(caller, SkillTools.cursorPositionAsPoint()));
    inputComp.registerCallback(
        KeyboardConfig.MOUSE_USE_SKILL.value(),
        (caller) -> HeroController.useSkill(caller, SkillTools.cursorPositionAsPoint()));
    inputComp.registerCallback(
        KeyboardConfig.NEXT_SKILL.value(),
        caller -> caller.fetch(SkillComponent.class).ifPresent(SkillComponent::nextSkill),
        false);
    inputComp.registerCallback(
        KeyboardConfig.PREV_SKILL.value(),
        caller -> caller.fetch(SkillComponent.class).ifPresent(SkillComponent::prevSkill),
        false);

    // Interact
    inputComp.registerCallback(
        KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
        (caller) -> HeroController.interact(caller, SkillTools.cursorPositionAsPoint()));
    inputComp.registerCallback(
        KeyboardConfig.INTERACT_WORLD.value(),
        (caller) -> HeroController.interact(caller, SkillTools.cursorPositionAsPoint()),
        false);

    // UI controls
    inputComp.registerCallback(
        KeyboardConfig.INVENTORY_OPEN.value(), HeroController::toggleInventory, false, true);
    inputComp.registerCallback(
        KeyboardConfig.CLOSE_UI.value(),
        (caller) ->
            Game.system(
                HudSystem.class,
                (hudSystem ->
                    hudSystem
                        .topmostCloseableUI()
                        .ifPresent(firstUI -> firstUI.a().remove(UIComponent.class)))),
        false,
        true);
  }
  // endregion
}
