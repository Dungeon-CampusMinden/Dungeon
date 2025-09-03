package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.DialogUtils;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.utils.components.health.Damage;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.*;
import core.game.PreRunConfiguration;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.InputMessage.Action;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** A utility class for building the hero entity in the game world. */
public final class HeroFactory {

  /**
   * The default size of the inventory.
   *
   * @see InventoryComponent
   */
  public static final int DEFAULT_INVENTORY_SIZE = 6;

  private static final IPath HERO_FILE_PATH = new SimpleIPath("character/wizard");
  private static final Vector2 STEP_SPEED = Vector2.of(5, 5);
  private static final int FIREBALL_COOL_DOWN = 500;
  private static final int HERO_HP = 25;
  private static final float HERO_MAX_SPEED = STEP_SPEED.x();
  private static final float HERO_MASS = 1.3f;
  private static Skill HERO_SKILL =
      new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), FIREBALL_COOL_DOWN);

  private static Consumer<Entity> HERO_DEATH =
      (hero) ->
          DialogUtils.showTextPopup(
              "You died!",
              "Game Over",
              () -> {
                hero.fetch(HealthComponent.class)
                    .ifPresent(hc -> hc.currentHealthpoints(hc.maximalHealthpoints()));
                // reset the animation queue
                hero.fetch(DrawComponent.class).ifPresent(DrawComponent::deQueueAll);
                DungeonLoader.reloadCurrentLevel();
              });

  /**
   * The default speed of the hero.
   *
   * @return Copy of the default speed of the hero.
   */
  public static Vector2 defaultHeroSpeed() {
    return Vector2.of(STEP_SPEED);
  }

  /**
   * Gets the current skill of the hero.
   *
   * @return The current skill of the hero.
   */
  public static Skill getHeroSkill() {
    return HERO_SKILL;
  }

  /**
   * Set the skill callback for the hero. The skill callback is executed when the hero uses the
   * skill.
   *
   * <p>By default, the hero uses the {@link FireballSkill}.
   *
   * @param skillCallback The skill callback.
   * @see Skill#Skill(Consumer, long)
   */
  public static void setHeroSkillCallback(Consumer<Entity> skillCallback) {
    HERO_SKILL = new Skill(skillCallback, FIREBALL_COOL_DOWN);
  }

  /**
   * Sets the callback to execute when the hero dies.
   *
   * @param deathCallback Callback that will be executed on the hero's death.
   */
  public static void heroDeath(Consumer<Entity> deathCallback) {
    HERO_DEATH = deathCallback;
  }

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link CameraComponent}, {@link PlayerComponent}{, {@link PlayerComponent},
   * {@link PositionComponent}, {@link VelocityComponent}, {@link DrawComponent}, {@link
   * CollideComponent} and {@link HealthComponent}.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero() throws IOException {
    return newHero(HERO_DEATH, true, PreRunConfiguration.username());
  }

  public static Entity newHero(boolean isLocal, String playerName) throws IOException {
    return newHero(HERO_DEATH, isLocal, playerName);
  }

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link CameraComponent}, {@link PlayerComponent}, {@link InputComponent}
   * {@link PositionComponent}, {@link VelocityComponent}, {@link DrawComponent}, {@link
   * CollideComponent} and {@link HealthComponent}.
   *
   * <p>If the hero, should be controlled by the local player, set {@code isLocal} to true.
   * Otherwise, it will be controlled by the server.
   *
   * @param deathCallback function that will be executed if the hero dies
   * @param isLocal if the hero is the local player
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero(Consumer<Entity> deathCallback, boolean isLocal, String playerName)
      throws IOException {
    Entity hero = new Entity("hero_" + playerName);
    hero.persistent(true);
    PlayerComponent pc = new PlayerComponent(isLocal, playerName);
    hero.add(pc);
    CameraComponent cc = new CameraComponent();
    if (isLocal) {
      hero.add(cc);
    }
    PositionComponent poc = new PositionComponent();
    hero.add(poc);
    hero.add(new VelocityComponent(HERO_MAX_SPEED, HERO_MASS, (e) -> {}, true));
    DrawComponent dc = new DrawComponent(HERO_FILE_PATH);
    dc.tintColor(isLocal ? -1 : 0x000077); // tint remote heroes blue
    hero.add(dc);
    HealthComponent hc =
        new HealthComponent(
            HERO_HP,
            entity -> {
              if (!entity
                  .fetch(PlayerComponent.class)
                  .map(PlayerComponent::isLocalHero)
                  .orElse(false)) return;

              // play sound
              Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
              long soundId = sound.play();
              sound.setLooping(soundId, false);
              sound.setVolume(soundId, 0.3f);
              sound.setLooping(soundId, false);
              sound.play();
              sound.setVolume(soundId, 0.9f);

              // relink components for camera
              Entity cameraDummy = new Entity("heroCamera");
              cameraDummy.add(cc);
              cameraDummy.add(poc);
              Game.add(cameraDummy);

              deathCallback.accept(entity);
            });
    hero.add(hc);
    CollideComponent col =
        new CollideComponent(
            (you, other, direction) ->
                other
                    .fetch(SpikyComponent.class)
                    .ifPresent(
                        spikyComponent -> {
                          if (spikyComponent.isActive()) {
                            hc.receiveHit(
                                new Damage(
                                    spikyComponent.damageAmount(),
                                    spikyComponent.damageType(),
                                    other));
                            spikyComponent.activateCoolDown();
                          }
                        }),
            CollideComponent.DEFAULT_COLLIDER);
    col.onHold(
        (you, other, direction) -> {
          if (other.isPresent(KineticComponent.class)) resolveCollisionWithMomentum(hero, other);
        });
    hero.add(col);

    InputComponent inputComp = new InputComponent();
    hero.add(
        new CatapultableComponent(
            entity -> inputComp.deactivateControls(true),
            entity -> inputComp.deactivateControls(false)));
    hero.add(inputComp);
    InventoryComponent invComp = new InventoryComponent(DEFAULT_INVENTORY_SIZE);
    hero.add(invComp);

    // hero movement
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_UP.value(), Direction.UP);
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(), Direction.DOWN);
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(), Direction.RIGHT);
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(), Direction.LEFT);

    if (HeroController.ENABLE_MOUSE_MOVEMENT) {
      // Mouse Left Click
      registerMouseLeftClick(inputComp);

      // Mouse Movement (Right Click)
      inputComp.registerCallback(
          KeyboardConfig.MOUSE_MOVE.value(),
          innerHero -> {
            // Small adjustment to get the correct tile
            Point mousePos =
                SkillTools.cursorPositionAsPoint().translate(Vector2.of(-0.5f, -0.25f));
            Game.network().sendInput(new InputMessage(Action.MOVE_PATH, mousePos));
          },
          false);
    }

    // UI controls
    inputComp.registerCallback(
        KeyboardConfig.INVENTORY_OPEN.value(),
        (entity) -> {
          toggleInventory(entity, inputComp, invComp, pc);
        },
        false,
        true);

    if (HeroController.ENABLE_MOUSE_MOVEMENT) {
      inputComp.registerCallback(
          KeyboardConfig.MOUSE_INVENTORY_TOGGLE.value(),
          (entity) -> {
            toggleInventory(entity, inputComp, invComp, pc);
          },
          false,
          true);
    }

    registerCloseUI(inputComp);

    inputComp.registerCallback(
        KeyboardConfig.INTERACT_WORLD.value(),
        entity -> {
          UIComponent uiComponent = entity.fetch(UIComponent.class).orElse(null);
          if (uiComponent != null
              && uiComponent.dialog() instanceof GUICombination
              && !InventoryGUI.inHeroInventory) {
            // if chest or cauldron
            entity.remove(UIComponent.class);
          } else {
            InteractionTool.interactWithClosestInteractable(entity);
          }
        },
        false);

    // skills
    inputComp.registerCallback(KeyboardConfig.FIRST_SKILL.value(), HeroFactory::executeHeroSkill);

    return hero;
  }

  /**
   * Registers a callback for closing the UI when the CLOSE_UI key is pressed.
   *
   * <p>This will close the topmost UI dialog that has the close key configured to close it.
   *
   * @param ic The PlayerComponent of the hero.
   */
  public static void registerCloseUI(InputComponent ic) {
    ic.registerCallback(
        KeyboardConfig.CLOSE_UI.value(),
        (e) -> {
          var firstUI =
              Game.levelEntities() // would be nice to directly access HudSystems
                  // stream (no access to the System object)
                  .filter(x -> x.isPresent(UIComponent.class)) // find all Entities
                  // that have a
                  // UIComponent
                  .map(
                      x ->
                          new Tuple<>(
                              x,
                              x.fetch(UIComponent.class)
                                  .orElseThrow(
                                      () ->
                                          MissingComponentException.build(
                                              x, UIComponent.class)))) // create a tuple to
                  // still have access to
                  // the UI Entity
                  .filter(x -> x.b().closeOnUICloseKey())
                  .max(Comparator.comparingInt(x -> x.b().dialog().getZIndex())) // find dialog
                  // with highest
                  // z-Index
                  .orElse(null);
          if (firstUI != null) {
            InventoryGUI.inHeroInventory = false;
            firstUI.a().remove(UIComponent.class);
            if (firstUI.a().componentStream().findAny().isEmpty()) {
              Game.remove(firstUI.a()); // delete unused Entity
            }
          }
        },
        false,
        true);
  }

  private static void toggleInventory(
      Entity entity, InputComponent ic, InventoryComponent invComp, PlayerComponent pc) {
    if (pc.openDialogs()) {
      return;
    }

    UIComponent uiComponent = entity.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null) {
      if (uiComponent.dialog() instanceof GUICombination) {
        InventoryGUI.inHeroInventory = false;
        entity.remove(UIComponent.class);
      }
    } else {
      InventoryGUI.inHeroInventory = true;
      entity.add(new UIComponent(new GUICombination(new InventoryGUI(invComp)), true));
    }
  }

  private static void registerMovement(InputComponent ic, int key, Direction direction) {
    ic.registerCallback(
        key,
        entity -> {
          Game.network()
              .sendInput(new InputMessage(Action.MOVE, new Point(0, 0).translate(direction)));
        });
  }

  private static void registerMouseLeftClick(InputComponent ic) {
    if (!Objects.equals(
        KeyboardConfig.MOUSE_FIRST_SKILL.value(), KeyboardConfig.MOUSE_INTERACT_WORLD.value())) {
      ic.registerCallback(
          KeyboardConfig.MOUSE_FIRST_SKILL.value(), HeroFactory::executeHeroSkill, true, false);
      ic.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          HeroFactory::handleInteractWithClosestInteractable,
          false,
          false);
    } else {
      // If interact and skill are the same, only one callback can be used, so we only interact if
      // interaction is possible
      ic.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          (hero) -> {
            Point mousePosition = SkillTools.cursorPositionAsPoint();
            Entity interactable = checkIfClickOnInteractable(mousePosition).orElse(null);
            if (interactable == null || !interactable.isPresent(InteractionComponent.class)) {
              executeHeroSkill(hero);
            } else {
              handleInteractWithClosestInteractable(hero);
            }
          },
          false,
          false);
    }
  }

  private static void executeHeroSkill(Entity hero) {
    // TODO: Implement logic to control skill_ids
    Game.network()
        .sendInput(new InputMessage(Action.CAST_SKILL, SkillTools.cursorPositionAsPoint()));
  }

  private static void handleInteractWithClosestInteractable(Entity hero) {
    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null && uiComponent.dialog() instanceof GUICombination) {
      // close the dialog if already open
      hero.remove(UIComponent.class);
      return;
    }
    Point mousePosition = SkillTools.cursorPositionAsPoint();
    Entity interactable = checkIfClickOnInteractable(mousePosition).orElse(null);
    if (interactable == null) return;
    InteractionComponent ic =
        interactable
            .fetch(InteractionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(interactable, InteractionComponent.class));
    PositionComponent pc =
        interactable
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(interactable, PositionComponent.class));
    PositionComponent heroPC =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    if (Point.calculateDistance(pc.position(), heroPC.position()) < ic.radius()) {
      Game.network().sendInput(new InputMessage(Action.INTERACT, pc.position()));
    }
  }

  private static Optional<Entity> checkIfClickOnInteractable(Point pos)
      throws MissingComponentException {
    pos = pos.translate(Vector2.of(-0.5f, -0.25f));

    Tile mouseTile = Game.tileAt(pos).orElse(null);
    if (mouseTile == null) return Optional.empty();

    return Game.entityAtTile(mouseTile)
        .filter(e -> e.isPresent(InteractionComponent.class))
        .findFirst();
  }

  private static void resolveCollisionWithMomentum(Entity hero, Entity other) {
    Optional<VelocityComponent> optVc1 = hero.fetch(VelocityComponent.class);
    Optional<VelocityComponent> optVc2 = other.fetch(VelocityComponent.class);

    if (optVc1.isEmpty() || optVc2.isEmpty()) return;

    VelocityComponent vc1 = optVc1.get();
    VelocityComponent vc2 = optVc2.get();

    Vector2 v1 = vc1.currentVelocity();
    Vector2 v2 = vc2.currentVelocity();
    float m1 = vc1.mass();
    float m2 = vc2.mass();

    Vector2 p1 = v1.scale(m1); // Impuls Entity 1
    Vector2 p2 = v2.scale(m2); // Impuls Entity 2

    Vector2 p = p1.add(p2); // Total Impuls
    Vector2 v = p.scale(1f / (m1 + m2)); // New velocity after collision

    double length = v.length(); // Länge des Vektors behalten (neue Geschwindigkeit)

    Direction d = v.direction(); // Richtung des Vektors nach Quadranten bestimmen

    Vector2 newVelocity = d.scale(length); // Neue Geschwindigkeit in Richtung des Vektors
    vc1.applyForce("Collision", newVelocity.scale(-1));
    vc2.applyForce("Collision", newVelocity);
  }
}
